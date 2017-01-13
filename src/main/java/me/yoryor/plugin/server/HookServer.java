package me.yoryor.plugin.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.List;
import java.util.stream.Collectors;

/**
 * HTTP 服务器，负责监听JIRA的Webhook回调
 *
 */
public class HookServer extends AbstractVerticle {
    private static final String CALLBACK_URL = "/jira/webhooks/test";
    private static final String MSG_ADDR = "jira.issue.status";
    private static final String JIRA_URL = "http://localhost:8080/"; // TODO: 2017/1/4 make me configurable plz.

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.post(CALLBACK_URL).handler(routingContext -> {
            // 获取jsonbody
            JsonObject bodyAsJson = routingContext.getBodyAsJson();
            JsonObject changeLog = bodyAsJson.getJsonObject("changelog");
            System.out.println(Json.encodePrettily(changeLog));
            JsonObject wrapJson = wrapJson(bodyAsJson);
            System.out.println(Json.encode(bodyAsJson)); // TODO: 2017/1/4 remove me plz.

            // 根据issue的类型处理，通过eventBus进行消息通信,传递给ClientVerticle
            vertx.eventBus().publish(MSG_ADDR, wrapJson);

        });
        // TODO: 2017/1/4 make ip port configurable
        httpServer.requestHandler(router::accept).listen(8200, "0.0.0.0", ar -> {
            if (ar.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(ar.cause());
            }
        });

    }

    // TODO: 2017/1/5 增加 resolved字段，标识任务是否完成
    // 处理jsonbody
    // 处理用户，名字 name key displayName 中找到与钉钉接口中对应的名字，以实现接口的调用
    //
    // 处理issue相关，
    // JIRA_URL/browse/key = issue的具体url地址，可以点击查看到具体的issue
    // fields中包含issue相关的详细信息
    // issuetype -- 问题类型
    // project -- 所属项目
    // fixversion
    // time -- 创建时间，更新时间，任务耗时等
    // creator -- 任务的创建者，通信的双方之一。
    // reporter -- 报告人
    // priority -- 优先级
    // status -- 任务的状态
    // assignee -- 任务的分配情况
    // TODO: 2017/1/6 增加changelog
    private JsonObject wrapJson(JsonObject bodyAsJson) {
        JsonObject issue = bodyAsJson.getJsonObject("issue");
        JsonObject issueFields = issue.getJsonObject("fields");
        JsonObject user = bodyAsJson.getJsonObject("user");

        String event = bodyAsJson.getString("webhookEvent");
        String issueEventType = bodyAsJson.getString("issue_event_type_name");
        String creatorName = issueFields.getJsonObject("creator").getString("displayName");
        String issueType = issueFields.getJsonObject("issuetype").getString("name");
        String userName = user.getString("name");
        String issueId = issue.getString("key");
        String issueDesc = issueFields.getString("description");
        String issueSummary = issueFields.getString("summary");
        String issuePriority = issueFields.getJsonObject("priority").getString("name");
        JsonObject issueAssignObj = issueFields.getJsonObject("assignee");
        String issueAssignee = issueAssignObj == null ? "" : issueAssignObj.getString("displayName");
        String issuesStatus = issueFields.getJsonObject("status").getString("name");
        String reporter = issueFields.getJsonObject("reporter").getString("name");
        JsonArray fixVersionsJsonArray = issueFields.getJsonArray("fixVersions");
        JsonArray versions = issueFields.getJsonArray("versions");
        String created = issueFields.getString("created");
        List<String> affectVersions = versions
                .stream()
                .map(o -> ((JsonObject) o).getString("name"))
                .collect(Collectors.toList());
        List<String> fixVersions = fixVersionsJsonArray
                .stream()
                .map(o -> ((JsonObject) o).getString("name"))
                .collect(Collectors.toList());
        JsonObject changelog = bodyAsJson.getJsonObject("changelog");
        JsonObject wrapJson = new JsonObject();

        wrapJson.put("hook_event", event)
                .put("issue_event", issueEventType)
                .put("issue", new JsonObject()
                        .put("id", issueId)
                        .put("type", issueType)
                        .put("summary", issueSummary)
                        .put("priority", issuePriority)
                        .put("assignee", issueAssignee)
                        .put("status", issuesStatus)
                        .put("user", userName)
                        .put("creator", creatorName)
                        .put("url", JIRA_URL + issueId)
                        .put("description", issueDesc)
                        .put("reporter", reporter)
                        .put("versions", affectVersions)
                        .put("fixVersions", fixVersions)
                        .put("created", created)
                ).put("changelog", changelog);
        return wrapJson;
    }

}
