import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 集合List工具类
 */
public class ListUtils {

    /**
     * 将满足特定条件的元素放到集合最前面
     * 如果满足特定条件的元素只有一个,就是把这个元素放到第一位
     *
     * @param list      list
     * @param condition 移动到集合前面的条件
     * @param <T>       集合元素类型
     */
    public static <T> void moveElementToListFirstPlace(List<T> list, Predicate<T> condition) {
        List<T> matched = new ArrayList<>();
        List<T> unmatched = new ArrayList<>();

        // 分离满足条件和不满足条件的元素
        for (T item : list) {
            if (condition.test(item)) {
                matched.add(item);
            } else {
                unmatched.add(item);
            }
        }

        // 清空原列表并重新添加
        list.clear();
        list.addAll(matched);
        list.addAll(unmatched);
    }

}
