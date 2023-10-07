package com.wegielek.signalychinese.utils;

public class Utils {

    public static boolean containsChinese(String input) {
        return input.matches(".*[\\u4e00-\\u9fff]+.*");
    }

}
