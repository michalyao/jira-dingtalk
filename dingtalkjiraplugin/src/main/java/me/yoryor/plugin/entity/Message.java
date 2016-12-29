package me.yoryor.plugin.entity;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
@EqualsAndHashCode
public class Message {
    // 消息卡连接
    private String url;
    // 消息卡头部
    private MessageHead head;
    // 消息卡内容
    private MessageBody body;
}
