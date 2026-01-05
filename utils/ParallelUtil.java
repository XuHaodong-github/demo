/**
 * 并行执行工具类
 */
@Service
public class ParallelUtil {

    /**
     * 默认并行超时时间，毫秒
     */
    private static final long DEFAULT_TIME_OUT_MILLISECONDS = 2000L;

    public static final String CAT_EVENT = "ParallelUtil";

    /**
     * 指定线程池批量查询
     * 返回值是单个的形式，入参和出参1:1
     *
     * @param paramList       入参List
     * @param function        执行方法
     * @param executorService 线程池
     * @param <T>             入参泛型
     * @param <U>             出参泛型
     * @return 查询结果
     */
    public static <T, U> List<U> parallelInvoke(List<T> paramList, Function<T, U> function, ExecutorService executorService) {

        if (CollectionUtils.isEmpty(paramList)) {
            Cat.logEvent(CAT_EVENT, "parallelInvokeList EMPTY PARAM");
            return new ArrayList<>();
        }

        // 最终执行结果
        Map<T, U> resultMap = new ConcurrentHashMap<>();
        // 并发执行中间结果
        List<CompletableFuture<Void>> allAsyncFutureList = new ArrayList<>();

        // 并发执行
        for (T param : paramList) {
            try {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    U result = function.apply(param);
                    LogUtil.info("param={}, result={}", JsonUtils.toJson(param), JsonUtils.toJson(result));
                    if (Objects.nonNull(result)) {
                        resultMap.put(param, result);
                    }
                }, executorService);
                allAsyncFutureList.add(future);
            } catch (Exception e) {
                // 捕获异常，单个执行失败不影响其他并行任务
                Cat.logEvent(CAT_EVENT, "parallelInvokeList runAsync Exception");
                LogUtil.error("Exception param={}", JsonUtils.toJson(param), e);
            }
        }

        try {
            CompletableFuture.allOf(allAsyncFutureList.toArray(new CompletableFuture[0])).get(DEFAULT_TIME_OUT_MILLISECONDS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Cat.logEvent(CAT_EVENT, "parallelInvokeList Exception");
            LogUtil.error("Exception paramList={}, e->", JsonUtils.toJson(paramList), e);
        }

        // 查询结果日志
        LogUtil.info("paramList={}, resultMap={}", JsonUtils.toJson(paramList), JsonUtils.toJson(resultMap));

        // 查询结果，移除重复参数查询
        return new ArrayList<>(resultMap.values());
    }

    /**
     * 指定线程池批量查询
     * 返回值是List的形式，入参和出参1:n
     *
     * @param paramList       入参List
     * @param function        执行方法
     * @param executorService 线程池
     * @param <T>             入参泛型
     * @param <U>             出参泛型
     * @return 查询结果
     */
    public static <T, U> List<U> parallelInvokeList(List<T> paramList, Function<T, List<U>> function, ExecutorService executorService) {

        if (CollectionUtils.isEmpty(paramList)) {
            Cat.logEvent(CAT_EVENT, "parallelInvokeBatchList EMPTY PARAM");
            return new ArrayList<>();
        }

        // 最终执行结果
        List<U> resultList = Collections.synchronizedList(new ArrayList<>());
        // 并发执行中间结果
        List<CompletableFuture<Void>> allAsyncFutureList = new ArrayList<>();

        // 并发执行
        for (T param : paramList) {
            try {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    List<U> result = function.apply(param);
                    LogUtil.info("parallelInvokeBatchList param={}, result={}", JsonUtils.toJson(param), JsonUtils.toJson(result));
                    if (CollectionUtils.isNotEmpty(result)) {
                        resultList.addAll(result);
                    }
                }, executorService);
                allAsyncFutureList.add(future);
            } catch (Exception e) {
                // 捕获异常，单个执行失败不影响其他并行任务
                Cat.logEvent(CAT_EVENT, "parallelInvokeBatchList runAsync Exception");
                LogUtil.error("Exception param={}, e->", JsonUtils.toJson(param), e);
            }
        }

        // 获取并发结果
        try {
            CompletableFuture.allOf(allAsyncFutureList.toArray(new CompletableFuture[0])).get(DEFAULT_TIME_OUT_MILLISECONDS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Cat.logEvent("parallelInvokeBatchList", "parallelInvokeList runAsync Exception");
            LogUtil.error("Exception paramList={}", JsonUtils.toJson(paramList), e);
        }

        // 查询结果日志
        LogUtil.info("paramList={}, resultList={}", JsonUtils.toJson(paramList), JsonUtils.toJson(resultList));

        // 查询结果
        return resultList;
    }


}
