package com.example.ourbookapplication;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinyinUtils {

    private static HanyuPinyinOutputFormat format;

    static {
        format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    public static boolean enhancedMatch(String text, String query) {
        if (text == null || query == null) return false;

        String lowerText = text.toLowerCase();
        String lowerQuery = query.toLowerCase();

        // 1. 直接文本匹配
        if (lowerText.contains(lowerQuery)) {
            return true;
        }

        // 2. 拼音全拼匹配
        String pinyin = getPinyin(text);
        if (pinyin.contains(lowerQuery)) {
            return true;
        }

        // 3. 拼音首字母匹配
        String initials = getInitials(text);
        if (initials.contains(lowerQuery)) {
            return true;
        }

        return false;
    }

    public static String getPinyin(String text) {
        if (text == null) return "";

        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.toString(c).matches("[\\u4E00-\\u9FA5]")) {
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        result.append(pinyinArray[0]);
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    result.append(c);
                }
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }

    public static String getInitials(String text) {
        if (text == null) return "";

        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.toString(c).matches("[\\u4E00-\\u9FA5]")) {
                try {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinArray != null && pinyinArray.length > 0) {
                        result.append(pinyinArray[0].charAt(0));
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    result.append(c);
                }
            } else if (Character.isLetter(c)) {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }
}