package me.yoryor.plugin.client;

import com.google.common.collect.Lists;
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
    private static final String ISSUE_GENERIC = "issue_generic";

    private final Map<String, Function<Issue, Message>> eventFuncMap = new HashMap<>();

    private EventStrategy() {
        super();
        initMap();
    }

    private void initMap() {
        // 输入: ClientVerticle 接收到消息解析出的issue实体
        // 输出: DingDing发送的消息体
        Function<Issue, Message> issueCreated = this::issueCreatedMsg;
        eventFuncMap.put(ISSUE_CREATED, issueCreated);
    }

    private Message issueCreatedMsg(Issue issue) {
        Form type = new Form("问题类型: ", issue.getType());
        Form version = new Form("影响版本: ", issue.getVersions().toString());
        Form assignee = new Form("经办人: ", issue.getAssignee());
        Form reporter = new Form("报告人: ", issue.getReporter());
        // FIXME: 2017/1/6 make me readable
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

    public Function<Issue, Message> getFuncByEventType(String eventType) {
        Function<Issue, Message> function = eventFuncMap.get(eventType);
        if (function != null) {
            return function;
        }
        return null;
    }


    public static EventStrategy getInstance() {
        return new EventStrategy();
    }

}
