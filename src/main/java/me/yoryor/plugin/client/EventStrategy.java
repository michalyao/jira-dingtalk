package me.yoryor.plugin.client;

import com.google.common.collect.Lists;
import io.vertx.core.json.JsonObject;
import me.yoryor.plugin.entity.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 事件处理策略
 * 根据JIRA的hook推送过来的事件类型构建钉钉通知消息
 *
 */
public class EventStrategy {
    private static final String JIRA_ISSUE_CREATED = "jira:issue_created";
    private static final String JIRA_ISSUE_UPDATED = "jira:issue_updated";
    private static final String JIRA_ISSUE_DELETED = "jira:issue_deleted";

    /************************** issue event type *****************************/
    private static final String ISSUE_CREATED = "issue_created";
    private static final String ISSUE_UPDATED = "issue_updated";
    private static final String ISSUE_RESOLVED = "issue_resolved";
    private static final String ISSUE_CLOSED = "issue_closed";
    // 任务有新评论
    private static final String ISSUE_COMMENTED = "issue_commented";
    // 任务评论重新编辑
    private static final String ISSUE_COMMENT_EDITED = "issue_comment_edited";
    private static final String ISSUE_REOPENED = "issue_reopened";
    private static final String ISSUE_DELETED = "issue_deleted";
    private static final String ISSUE_ASSIGNED = "issue_assigned";


    private static final String ISSUE_GENERIC = "issue_generic";

    private final Map<String, Function<JsonObject, Message>> eventFuncMap = new HashMap<>();

    private EventStrategy() {
        super();
        initMap();
    }

    private void initMap() {
        // 输入: ClientVerticle 从eventbus接收到的消息
        // 输出: DingDing发送的消息体
        Function<JsonObject, Message> issueCreated = this::issueCreatedMsg;
        Function<JsonObject, Message> issueAssigned = this::issueAssignedMsg;


        eventFuncMap.put(ISSUE_CREATED, issueCreated);
        eventFuncMap.put(ISSUE_ASSIGNED, issueAssigned);
    }

    // 只能指定唯一一个经办人
    private Message issueAssignedMsg(JsonObject wrapJson) {
        Issue issue = Issue.fromJson(wrapJson.getJsonObject("issue"));
        JsonObject changelog = wrapJson.getJsonObject("changelog");
        ChangeItem item = ChangeItem.fromJson(changelog.getJsonArray("items").getJsonObject(0));
        Form from = new Form("From: ", item.getFrom());
        Form to = new Form("To: ", item.getTo());
        MessageBody messageBody = MessageBody.builder()
                .content(String.format("%s 更新了任务的经办人", issue.getUser()))
                .form(Lists.newArrayList(from, to))
                .title("Monitor/" + issue.getId()) // TODO: 2017/1/6 get name from board or project.
                .build();
        MessageHead messageHead = MessageHead.builder()
                .bgcolor("FFDC123B")
                .text("")
                .build();
        return  Message.builder().url("http://www.uyun.cn")
                .body(messageBody)
                .head(messageHead)
                .build();
    }

    private Message issueCreatedMsg(JsonObject wrapJson) {
        Issue issue = Issue.fromJson(wrapJson.getJsonObject("issue"));
        Form type = new Form("问题类型: ", issue.getType());
        Form version = new Form("影响版本: ", issue.getVersions().toString());
        Form assignee = new Form("经办人: ", issue.getAssignee());
        Form reporter = new Form("报告人: ", issue.getReporter());
        Form created = new Form("创建时间: ", issue.getCreated().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")));
        Form priority = new Form("优先级: ", issue.getPriority());

        MessageBody messageBody = MessageBody.builder()
                .author(issue.getCreator())
                .content(String.format("%s 新建了一个任务", issue.getCreator()))
                .form(Lists.newArrayList(type, version, assignee, reporter, created, priority))
                .title("Monitor/" + issue.getId())
                .build();
        MessageHead messageHead = MessageHead.builder()
                .bgcolor("FFDC123B")
                .text("")
                .build();
        return Message.builder().url("http://www.uyun.cn")
                .body(messageBody)
                .head(messageHead)
                .build();
    }

    public Function<JsonObject, Message> getFuncByEventType(String eventType) {
        Function<JsonObject, Message> function = eventFuncMap.get(eventType);
        if (function != null) {
            return function;
        }
        return null;
    }


    public static EventStrategy getInstance() {
        return new EventStrategy();
    }

}
