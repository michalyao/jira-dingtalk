package me.yoryor.plugin.entity;

import com.google.common.collect.Lists;
import org.junit.Test;

public class MessageBodyTest {

    @Test
    public void builderTest() {
        Form form = new Form("issue", "bat-527");
        MessageBody messageBody = MessageBody.builder()
                .author("yaoyao")
                .content("这是一封测试内容")
                .form(Lists.newArrayList(form))
                .title("BAT-527").build();
        System.out.println(messageBody);
    }

}