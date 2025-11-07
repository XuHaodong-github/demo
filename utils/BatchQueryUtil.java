import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * 批量查询工具类
 */
public class BatchQueryUtil {

    // 默认批次大小
    private static final int DEFAULT_BATCH_SIZE = 30;



    /**
     * 批次拆分入参
     * 拆分后的结果是[[1,2,3],[1,2,3]]
     *
     * @param items 待处理列表
     * @param <T>   输入数据类型
     * @return 拆分后的入参
     */
    public static <T> List<List<T>> splitParamList(List<T> items) {
        List<List<T>> result = new ArrayList<>();
        if (items == null || items.isEmpty()) {
            return result;
        }

        int size = items.size();
        for (int i = 0; i < size; i += DEFAULT_BATCH_SIZE) {
            int end = Math.min(i + DEFAULT_BATCH_SIZE, size);
            List<T> batch = items.subList(i, end);
            result.add(batch);
        }

        return result;
    }


    /**
     * 批次拆分入参
     * 拆分后的结果是 [["1,2,3"],["1,2,3"]]
     *
     * @param items 待处理列表
     * @param <T>   输入数据类型
     * @return 拆分后的入参
     */
    public static <T> List<String> splitParamListStringCommaJoin(List<T> items) {
        List<String> result = new ArrayList<>();
        if (items == null || items.isEmpty()) {
            return result;
        }

        int size = items.size();
        for (int i = 0; i < size; i += DEFAULT_BATCH_SIZE) {
            int end = Math.min(i + DEFAULT_BATCH_SIZE, size);
            List<T> batch = items.subList(i, end);
            String batchStrCommaJoin = batch.stream().map(String::valueOf).collect(Collectors.joining(","));
            result.add(batchStrCommaJoin);
        }

        return result;
    }


    /**
     * 同步分批查询（单线程）
     * 入参出参 1:n
     * @param items     待处理列表
     * @param processor 处理函数，接收一批数据，返回处理结果
     * @param <T>       输入数据类型
     * @param <R>       返回数据类型
     * @return 合并后的所有结果
     */
    public static <T, R> List<R> batchQuerySingleParamIn(List<T> items, Function<T, List<R>> processor) {
        List<R> result = new ArrayList<>();
        if (items == null || items.isEmpty()) {
            return result;
        }

        int size = items.size();

        for (int i = 0; i < size; i += DEFAULT_BATCH_SIZE) {
            int end = Math.min(i + DEFAULT_BATCH_SIZE, size);
            List<T> batchParam = items.subList(i, end);
            List<R> batchResult = batchParam.stream().map(processor).flatMap(List::stream).collect(Collectors.toList());
            result.addAll(batchResult);
        }

        return result;
    }


    /**
     * 同步分批查询（单线程）
     * 入参出参 1:n
     * @param items     待处理列表
     * @param batchSize 每批大小
     * @param processor 处理函数，接收一批数据，返回处理结果
     * @param <T>       输入数据类型
     * @param <R>       返回数据类型
     * @return 合并后的所有结果
     */
    public static <T, R> List<R> batchQuerySingleParamIn(List<T> items, Integer batchSize, Function<T, List<R>> processor) {
        List<R> result = new ArrayList<>();
        if (items == null || items.isEmpty()) {
            return result;
        }

        int size = items.size();
        batchSize = Objects.isNull(batchSize) || batchSize <= 0 ? DEFAULT_BATCH_SIZE : batchSize;

        for (int i = 0; i < size; i += batchSize) {
            int end = Math.min(i + batchSize, size);
            List<T> batchParam = items.subList(i, end);
            List<R> batchResult = batchParam.stream().map(processor).flatMap(List::stream).collect(Collectors.toList());
            result.addAll(batchResult);
        }

        return result;
    }



    /**
     * 同步分批查询（单线程）
     *
     * @param items     待处理列表
     * @param batchSize 每批大小
     * @param processor 处理函数，接收一批数据，返回处理结果
     * @param <T>       输入数据类型
     * @param <R>       返回数据类型
     * @return 合并后的所有结果
     */
    public static <T, R> List<R> batchQuerySync(List<T> items,
                                                int batchSize,
                                                Function<List<T>, List<R>> processor) {
        List<R> result = new ArrayList<>();
        if (items == null || items.isEmpty()) {
            return result;
        }

        int size = items.size();
        batchSize = batchSize <= 0 ? DEFAULT_BATCH_SIZE : batchSize;

        for (int i = 0; i < size; i += batchSize) {
            int end = Math.min(i + batchSize, size);
            List<T> batch = items.subList(i, end);
            List<R> batchResult = processor.apply(batch);
            if (batchResult != null) {
                result.addAll(batchResult);
            }
        }

        return result;
    }

    /**
     * 异步分批查询（多线程）
     *
     * @param items     待处理列表
     * @param batchSize 每批大小
     * @param processor 处理函数，接收一批数据，返回处理结果
     * @param executor  线程池（如果为则使用单线程）
     * @param <T>       输入数据类型
     * @param <R>       返回数据类型
     * @return 合并后的所有结果
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static <T, R> List<R> batchQueryAsync(List<T> items,
                                                 int batchSize,
                                                 Function<List<T>, List<R>> processor,
                                                 ExecutorService executor)
            throws ExecutionException, InterruptedException {
        List<R> result = new ArrayList<>();
        if (items == null || items.isEmpty()) {
            return result;
        }

        int size = items.size();
        batchSize = batchSize <= 0 ? DEFAULT_BATCH_SIZE : batchSize;

        // 如果没有提供线程池，使用单线程执行
        boolean useSingleThread = (executor == null);
        ExecutorService finalExecutor = useSingleThread ?
                Executors.newSingleThreadExecutor() : executor;

        try {
            List<Future<List<R>>> futures = new ArrayList<>();

            // 提交所有批次任务
            for (int i = 0; i < size; i += batchSize) {
                int end = Math.min(i + batchSize, size);
                List<T> batch = items.subList(i, end);

                Callable<List<R>> task = () -> processor.apply(batch);
                futures.add(finalExecutor.submit(task));
            }

            // 等待所有任务完成并收集结果
            for (Future<List<R>> future : futures) {
                List<R> batchResult = future.get();
                if (batchResult != null) {
                    result.addAll(batchResult);
                }
            }
        } finally {
            if (useSingleThread) {
                finalExecutor.shutdown();
            }
        }

        return result;
    }

    /**
     * 异步分批查询（带超时控制）
     */
    public static <T, R> List<R> batchQueryAsyncWithTimeout(List<T> items,
                                                            int batchSize,
                                                            Function<List<T>, List<R>> processor,
                                                            ExecutorService executor,
                                                            long timeout,
                                                            TimeUnit unit)
            throws ExecutionException, InterruptedException, TimeoutException {
        List<R> result = new ArrayList<>();
        if (items == null || items.isEmpty()) {
            return result;
        }

        int size = items.size();
        batchSize = batchSize <= 0 ? DEFAULT_BATCH_SIZE : batchSize;

        boolean useSingleThread = (executor == null);
        ExecutorService finalExecutor = useSingleThread ?
                Executors.newSingleThreadExecutor() : executor;

        try {
            List<Future<List<R>>> futures = new ArrayList<>();

            for (int i = 0; i < size; i += batchSize) {
                int end = Math.min(i + batchSize, size);
                List<T> batch = items.subList(i, end);

                Callable<List<R>> task = () -> processor.apply(batch);
                futures.add(finalExecutor.submit(task));
            }

            for (Future<List<R>> future : futures) {
                List<R> batchResult = future.get(timeout, unit);
                if (batchResult != null) {
                    result.addAll(batchResult);
                }
            }
        } finally {
            if (useSingleThread) {
                finalExecutor.shutdown();
            }
        }

        return result;
    }
}
