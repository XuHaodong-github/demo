package org.example.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 断言工具类
 */
public class DuplicateUtils {

    /**
     * 找出集合中重复的元素
     *
     * @param list list
     * @param <T>  泛型
     * @return List<T> 重复的元素
     */
    public static <T> List<T> findDuplicateElement(List<T> list) {
        Set<T> seenSet = new HashSet<>(); // 用于记录已遍历的元素
        List<T> duplicates = new ArrayList<>(); // 用于存储重复元素
        for (T element : list) {
            // 如果 Set 已经包含该元素，说明是重复的
            if (!seenSet.add(element)) {
                duplicates.add(element);
            }
        }
        return duplicates;
    }
}
