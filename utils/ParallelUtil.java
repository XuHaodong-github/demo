public class ParallelUtil {
    private static final long TIME_OUT = 1000L;

    public <T, U> Map<T, U> parallelInvokeMap(List<T> paramList, Function<T, U> function, ThreadPoolExecutor threadPoolExecutor) {
        if (CollectionUtils.isEmpty(paramList)) {
            return new HashMap<>();
        }

        Map<T, U> resultMap = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> allAsyncFutureList = new ArrayList<>();
        paramList.forEach(param -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    U result = function.apply(param);
                    LogUtil.info("parallelInvokeMap param={}, result={}", JsonUtils.toJson(param), JsonUtils.toJson(result));
                    if (Objects.nonNull(result)) {
                        resultMap.put(param, result);
                    }
                } catch (Exception e) {
                    Cat.logEvent("parallelInvokeMap", "Exception");
                    LogUtil.error("parallelInvokeMap Exception param={}", JsonUtils.toJson(param), e);
                }

            }, threadPoolExecutor);
            allAsyncFutureList.add(future);
        });

        try {
            CompletableFuture.allOf(allAsyncFutureList.toArray(new CompletableFuture[0])).get(TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Cat.logEvent("parallelInvokeMap", "CompletableFutureException");
            LogUtil.error("CompletableFutureException paramList={}", JsonUtils.toJson(paramList), e);
        }

        LogUtil.info("paramList={}, resultMap={}", JsonUtils.toJson(paramList), JsonUtils.toJson(resultMap));
        return resultMap;
    }

    public <T, U> List<U> parallelInvokeList(List<T> paramList, Function<T, U> function, ThreadPoolExecutor threadPoolExecutor) {
        Map<T, U> resultMap = parallelInvokeMap(paramList, function, threadPoolExecutor);
        return new ArrayList<>(resultMap.values());
    }

    public <T, U> List<U> parallelInvokeBatchList(List<T> paramList, Function<T, List<U>> function, ThreadPoolExecutor threadPoolExecutor) {
        if (CollectionUtils.isEmpty(paramList)) {
            return new ArrayList<>();
        }

        List<U> resultList = Collections.synchronizedList(new ArrayList<>());
        List<CompletableFuture<Void>> allAsyncFutureList = new ArrayList<>();
        paramList.forEach(param -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    List<U> result = function.apply(param);
                    LogUtil.info("parallelInvokeBatchList param={}, result={}", JsonUtils.toJson(param), JsonUtils.toJson(result));
                    if (CollectionUtils.isNotEmpty(result)) {
                        resultList.addAll(result);
                    }
                } catch (Exception e) {
                    Cat.logEvent("parallelInvokeBatchList", "Exception");
                    LogUtil.error("parallelInvokeBatchList Exception param={}", JsonUtils.toJson(param), e);
                }

            }, threadPoolExecutor);
            allAsyncFutureList.add(future);
        });

        try {
            CompletableFuture.allOf(allAsyncFutureList.toArray(new CompletableFuture[0])).get(TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Cat.logEvent("parallelInvokeBatchList", "CompletableFutureException");
            LogUtil.error("CompletableFutureException paramList={}", JsonUtils.toJson(paramList), e);
        }

        LogUtil.info("paramList={}, resultList={}", JsonUtils.toJson(paramList), JsonUtils.toJson(resultList));
        return resultList;
    }
}
