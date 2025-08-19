package org.example.utils;

import java.util.regex.Pattern;


/**
 * 字符串转数字工具类
 */
public class StringToDigitHelper {
    private static final Pattern NON_NEGATIVE_INTEGER_PATTERN = Pattern.compile("0|[1-9]\\d*");

    /**
     * 检测是不是非负整数
     * System.out.println(isNonNegativeInteger("0"));     // true
     * System.out.println(isNonNegativeInteger("123"));   // true
     * System.out.println(isNonNegativeInteger("0123"));  // false（不支持前导零）
     * System.out.println(isNonNegativeInteger("-123"));  // false
     * System.out.println(isNonNegativeInteger("1.23"));  // false
     * System.out.println(isNonNegativeInteger("abc"));   // false
     * System.out.println(isNonNegativeInteger(""));      // false
     * System.out.println(isNonNegativeInteger(null));    // false
     *
     * @param str 字符串
     * @return 字符串是不是非负整数
     */
    public static boolean isNonNegativeInteger(String str) {
        return str != null && NON_NEGATIVE_INTEGER_PATTERN.matcher(str).matches();
    }


}
