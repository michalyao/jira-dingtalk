package me.yoryor.plugin.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class Util {
    private Util() {}

    /**
     * 从指定路径读取文件，结果输出到字符串中
     *
     * @param path 文件路径，可以是相对的，也可以是绝对的
     * @return string 文件中的字符串内容
     * @throws IOException
     */
    public static String readFileToString(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        Files.lines(Paths.get(path), StandardCharsets.UTF_8).forEach(sb::append);
        return sb.toString();
    }
}
