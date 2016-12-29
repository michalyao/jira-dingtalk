package me.yoryor.plugin.entity;

import lombok.*;

import java.util.List;


@Getter
@ToString
@EqualsAndHashCode
@Builder
public class MessageBody {
    // 正文标题
    private String title;
    // 表单
    private List<Form> form;
    // 文本内容
    private String content;
    // 作者
    private String author;
    // 当前不支持图片，文件等格式的消息
}
