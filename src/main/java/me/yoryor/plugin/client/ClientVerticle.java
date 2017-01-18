package me.yoryor.plugin.client;

import com.google.common.collect.Lists;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import me.yoryor.plugin.entity.*;

import java.time.LocalDateTime;
import java.util.function.Function;

public class ClientVerticle extends AbstractVerticle{
    private static final String CORP_ID = System.getProperty("dingtalk.corpid");
    private static final String CORP_SECRET = System.getProperty("dingtalk.corpsecret");

    private static final long CACHE_TIME = 2 * 55 * 60 * 1000l; // accessToken 缓存时间110分钟

    private static final Logger LOG = LoggerFactory.getLogger(ClientVerticle.class);
    private DingClient dingClient;


    private LocalMap<String, JsonObject> tokenCache;
    private LocalMap<String, String> userIdCache;
    private SharedData sd;
    private static final EventStrategy strategy = EventStrategy.getInstance();

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        initConfig();
        Future<Void> initTokenCache = initCache(sd);
        Future<Void> initUserMap = initTokenCache.compose(initCache -> {
            String token = getTokenFromCache();
            Future<Void> userMap = dingClient.initUserMap(token);
            return userMap;
        });
        // TODO: 2017/1/9 缓存失效判断 
        vertx.eventBus().<JsonObject>consumer("jira.issue.status", message -> {
            System.out.println(Json.encode(message.body()));
            JsonObject wrapBody = message.body();
            String hookEvent = wrapBody.getString("hook_event");
            String issueEvent = wrapBody.getString("issue_event");
            Issue issue = Issue.fromJson(wrapBody.getJsonObject("issue"));
            String userId = userIdCache.get("姚尧");

            Function<JsonObject, Message> function = strategy.getFuncByEventType(issueEvent);
            dingClient.corpOaMsgTo(getTokenFromCache(), "35330198", new String[]{userId}, new String[0], function.apply(wrapBody));

            // create an issue
//            if (Objects.equals(hookEvent, JIRA_ISSUE_CREATED) && Objects.equals(issueEvent, ISSUE_CREATED)) {
//                Issue issue = Issue.fromJson(wrapBody.getJsonObject("issue"));
//                System.out.println(issue);
//                System.out.println(sd.getLocalMap("allUser").size());
//                System.out.println(getTokenFromCache());
////                System.out.println(userIdCache.size());
////                System.out.println(getTokenFromCache());
//                String userId = userIdCache.get("姚尧");
//                System.out.println(userId);
////                System.out.println(userId);
//                dingClient.corpTextMsgTo(getTokenFromCache(), "35330198", new String[]{userId}, new String[0], "test");
//////                String accessToken = (String) dingCache.get("access_token");
////                System.out.println(getTokenFromCache());
//            }
            // updated an issue -- 任务分配变更
//            if (Objects.equals(hookEvent, JIRA_ISSUE_UPDATED) && Objects.equals(issueEvent, ISSUE_UPDATED)) {
//
//            }
//            // updated an issue -- 评论变更
//            if (Objects.equals(hookEvent, JIRA_ISSUE_UPDATED) && Objects.equals(issueEvent, ISSUE_COMMENTED)) {
//
//            }
        });
    }

    private void initConfig() {
        HttpClientOptions options = new HttpClientOptions();
        options.setSsl(true);
        options.setTrustAll(true);
        options.setKeepAlive(false);
        options.setDefaultPort(443);
        options.setDefaultHost("oapi.dingtalk.com");
        options.setLogActivity(true);
        this.dingClient = new DingClient(vertx, options);
        this.sd = vertx.sharedData();
        this.userIdCache = sd.getLocalMap("allUser");
        this.tokenCache = sd.getLocalMap("tokenCache");
    }

    // 缓存钉钉api调用token
    private Future<Void> initCache(SharedData sd) {
        Future<Void> result = Future.future();

        long now = System.currentTimeMillis();
        // 无缓存或者缓存超时失效，需要执行请求获取
        Future<String> token = dingClient.getToken(CORP_ID, CORP_SECRET);
        token.setHandler(ar -> {
            if (ar.succeeded()) {
                // 将token缓存到localMap中，同一个vertx实例共享。缓存格式
                //                {
                //                  "token": {
                //                    "access_token": xxx,
                //                    "begin_time": xxx
                //                  }
                //                }
                JsonObject cacheJson = new JsonObject();
                cacheJson.put("access_token", token.result()).put("begin_time", now);
                tokenCache.put("token", cacheJson);
                result.complete();
            } else {
                result.fail(ar.cause());
                LOG.error(ar.cause());
            }
        });
        return result;
    }

    // 缓存token校验，不存在(程序第一次启动)；失效(超时)
    private boolean isExpired(String token) {
        long now = System.currentTimeMillis();
        return getTokenFromCache() == null ||
                now - (long) tokenCache.get("token").getLong("begin_time") >= CACHE_TIME;
    }

    private String getTokenFromCache() {
        return tokenCache.get("token").getString("access_token");
    }


   @Override
    public void stop() throws Exception {
        dingClient.close();
    }

    // for test
    private Message buildMsg(JsonObject wrapJson) {
        JsonObject issue = wrapJson.getJsonObject("issue");
        Form form = new Form("issue", "bat-527");
        Form issueType = new Form("任务类型", issue.getString("type"));
        Form issuePriority = new Form("重要级别", issue.getString("priority"));
        Form status = new Form("任务状态", issue.getString("status"));
        Form assignee = new Form("分配", issue.getString("assignee"));
        Form summary = new Form("任务描述", issue.getString("summary"));
        Form issueUrl = new Form("任务地址", "http://localhost/browse/" + issue.getString("id"));
        MessageBody messageBody = MessageBody.builder()
                .author(wrapJson.getString("creator", ""))
                .content(String.format("这是一封测试内容, 创建时间: %s%n, 任务地址: %s%n ", LocalDateTime.now().toString(), "http://jira.uyunsoft.cn/browse/BAT-189"))
                .form(Lists.newArrayList(form, summary, issueUrl,assignee, issueType, issuePriority, status))
                .title(issue.getString("id")).build();
        MessageHead messageHead = MessageHead.builder()
                .bgcolor("FFDC143C")
                .text("优云-JIRA").build();
        return Message.builder()
                .url("http://jira.uyunsoft.cn/browse/BAT-189")
                .body(messageBody)
                .head(messageHead)
                .build();

    }
}
