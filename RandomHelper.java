package org.example.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 随机工具类
 */
public class RandomHelper {

    public static List<Integer> generateRandomIntegerList(int size, int bound) {
        List<Integer> randomList = new ArrayList<>(size);
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            randomList.add(random.nextInt(bound)); // 生成 [0, bound) 的随机整数
        }
        return randomList;
    }


}