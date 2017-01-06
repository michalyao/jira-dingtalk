package me.yoryor.plugin.entity;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeTest {
    @Test
    public void testFormat() {
        System.out.println(LocalDateTime.now());
        LocalDateTime.parse("2017-01-06T13:57:17.802+0800", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSx"));
    }
}
