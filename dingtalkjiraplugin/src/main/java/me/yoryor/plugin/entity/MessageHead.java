package me.yoryor.plugin.entity;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@Builder
public class MessageHead {
    // oa消息卡头部颜色
    private String bgcolor;
    // oa消息卡头部信息，是企业应用的名称。不好修改
    private String text;
}
