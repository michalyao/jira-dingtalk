package me.yoryor.plugin.client;

import io.vertx.core.*;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import me.yoryor.plugin.entity.Message;
import me.yoryor.plugin.entity.MessageBody;
import me.yoryor.plugin.entity.MessageHead;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class DingClient implements Closeable, DingService {
    private final Vertx vertx;
    private final HttpClient httpClient;
    private final HttpClientOptions options;
    private final LocalMap<String, String> allUsers;

    private static final Logger LOG = LoggerFactory.getLogger(DingClient.class);
    private static final long CACHE_TIME = 2 * 55 * 60 * 1000l; // accessToken 缓存时间110分钟


    public DingClient(Vertx vertx, HttpClientOptions options) {
        this.vertx = vertx;
        this.options = options;
        this.httpClient = vertx.createHttpClient(options);
        this.allUsers = vertx.sharedData().getLocalMap("allUser");
    }



    public DingClient(HttpClientOptions options) {
        this(Vertx.vertx(), options);
    }


    public LocalMap<String, String> getAllUsers() {
        return this.allUsers;
    }

    @Override
    public void close() throws IOException {
        this.httpClient.close();
    }

    @Override
    public Future<String> getToken(String corpID, String corpSecret) {
        Future<String> result = Future.future();
        StringBuilder sb = new StringBuilder();
        sb.append("/gettoken?")
                .append("corpid=" + corpID)
                .append("&corpsecret=" + corpSecret);
        String url = sb.toString();
        httpClient.get(url, responseHandler(result, url, responseBody -> responseBody.getString("access_token")))
                .putHeader("Accept", "application/json")
                .exceptionHandler(result::fail)
                .end();
        return result;
    }

    /**
     *
     * @param result 异步处理结果
     * @param url 请求url
     * @param bodyHandler 响应消息体的处理函数; JsonObject()
     * @return Http响应的处理函数，用做httpclient方法中的回调参数
     */
    private <U> Handler<HttpClientResponse> responseHandler(Future result, String url, Function<JsonObject, U> bodyHandler) {
        return response -> {
            response.exceptionHandler(result::fail);
            response.bodyHandler(buffer -> {
                JsonObject responseBody = buffer.toJsonObject();
                Integer errcode = responseBody.getInteger("errcode");
                // 请求正确 -> 打印日志，标记成功，并传入结果
                if (errcode == 0) {
                    LOG.info(String.format("请求: %s 执行成功，返回信息: %s%n", url, Json.encodePrettily(responseBody)));
                    result.complete(bodyHandler.apply(responseBody));
                } else {
                    // 请求失败 -> 获取errcode，errmsg并打印；所有异常都用uncheckedException包装成DingDingException
                    String errmsg = responseBody.getString("errmsg");
                    LOG.error(String.format("请求: %s 执行失败，errcode: %s, errmsg: %s%n",
                            url, responseBody.getInteger("errcode"), errmsg));
                    result.fail(new DingDingException(errmsg));
                }
            });
        };
    }

    @Override
    public Future<String> corpMsgTo(String token, String agentId, String[] userId, String[] partyId, JsonObject msg) {
        final Future<String> result = Future.future();
        String url = "/message/send?access_token=" + token;
        JsonObject payload = new JsonObject();
        payload.put("touser", join(userId, "|"))
                .put("toparty", join(partyId, "|"))
                .put("agentid", agentId)
                .mergeIn(msg);
        httpClient.post(url, responseHandler(result, url, response ->
                response.getString("messageId")
        ))
                .putHeader("Content-Type", "application/json")
                .exceptionHandler(result::fail)
                .end(payload.toString());
        return result;
    }

    // 目前OA消息支持的不是很好，主要是不能实现页面跳转，需要通过
    public Future<String> corpOaMsgTo(String token, String agentId, String[] userId,
                                       String[] partyId, Message message) {
        MessageBody body = message.getBody();
        MessageHead head = message.getHead();
        JsonObject oaMsg = new JsonObject();
        JsonObject oa = new JsonObject();
        oa.put("message_url", message.getUrl())
                .put("head", new JsonObject()
                        .put("bgcolor", head.getBgcolor())
                        .put("text", head.getText()))
                .put("body", new JsonObject().put("title", body.getTitle())
                        .put("form", new JsonArray(body.getForm()))
                        .put("content", body.getContent())
                        .put("author", body.getAuthor()));
        oaMsg.put("msgtype", "oa")
                .put("oa", oa);
        return this.corpMsgTo(token, agentId, userId, partyId, oaMsg);

    }

    public Future<String> corpTextMsgTo(String token, String agentId, String[] userId, String[] partyId, String content) {
        JsonObject textMsg = new JsonObject();
        textMsg.put("msgtype", "text")
                .put("text", new JsonObject().put("content", content));
        return this.corpMsgTo(token, agentId, userId, partyId, textMsg);
    }

    private String join(String[] strArray, String delimiter) {
        if (strArray.length == 0 || strArray == null) {
            return "";
        }
        StringJoiner sj = new StringJoiner(delimiter);
        Stream.of(strArray).forEach(sj::add);
        return sj.toString();
    }

    @Override
    public Future<Void> getUserMap(String token) {
        Future<Void> result = Future.future();
        List<Future> futures = new ArrayList<>();
        // 1. 获取公司所有部门的id
        Future<List<Long>> departments = getDepartments(token);
        // 2. 获取公司部门下的成员详情。一个串行(compose)，一个是组合(CompositeFuture.all)
        Future<Void> composed = departments.compose(departmentIdList -> {
            Future<Void> future = Future.future();
            for (Long id : departmentIdList) {
                Future<JsonArray> getUsers = getDepUserList(token, id);
                futures.add(getUsers);
            }
            CompositeFuture all = CompositeFuture.all(futures);
            all.setHandler(ar -> {
                if (ar.succeeded()) {
                    future.complete();
                } else {
                    future.fail(ar.cause());
                }
            });
            return future;
        });
        composed.setHandler(voidAsyncResult -> {
            if (voidAsyncResult.succeeded()) {
                result.complete();
            } else {
                result.fail(voidAsyncResult.cause());
            }
        });
        return result;
    }

    // 暂时声明为public方便接口测试
    // 获取公司的部门列表
    // 返回departmentId列表
    private Future<List<Long>> getDepartments(String token) {
        Future<List<Long>> result = Future.future();

        String url = "/department/list?access_token=" + token + "&id=1";
        httpClient.get(url, responseHandler(result, url, body -> {
            List<Long> departmentIdList = new ArrayList<>();
            JsonArray departments = body.getJsonArray("department");
            for (Object obj : departments) {
                JsonObject department = (JsonObject) obj;
                departmentIdList.add(department.getLong("id"));
            }
            return departmentIdList;
        }))
                .putHeader("Accept", "application/json")
                .exceptionHandler(result::fail)
                .end();

        return result;
    }


    // 获取指定部门下的用户详情 -> userName = userId
    // 并且存储到LocalMap中作为缓存
    // 返回的结果是已经插入的用户详情
    private Future<JsonArray> getDepUserList(String token, long departmentID) {
        Future<JsonArray> result = Future.future();
        String url = "/user/simplelist?access_token=" + token + "&department_id=" + departmentID;
        httpClient.get(url, responseHandler(result, url, body -> {
            JsonArray userList = body.getJsonArray("userlist");
            for (Object obj : userList) {
                JsonObject user = (JsonObject) obj;
                // 只有不存在才会插入
                allUsers.putIfAbsent(user.getString("name"), user.getString("userid"));
            }
            return userList;
        }))
                .putHeader("Accept", "application/json")
                .exceptionHandler(result::fail)
                .end();
        return result;
    }
}
