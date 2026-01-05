/**
 * 日志工具类
 */
@Slf4j
public final class LogUtil {

    public static final String TARGET_PACKAGE_PATH = "com.pinduoduo.cargod";
    public static final String LAMBDA_METHOD_NAME = "lambda";
    public static final String EMPTY_STR = "";
    public static final String BLANK_STR = "";

    public static void info(String msg) {
        log.info(getTag() + msg);
    }

    public static void info(String format, Object... arguments) {
        log.info(getTag() + format, arguments);
    }

    public static void warn(String msg) {
        log.warn(getTag() + msg);
    }

    public static void warn(String format, Object... arguments) {
        log.warn(getTag() + format, arguments);
    }

    public static void error(String msg) {
        log.error(getTag() + msg);
    }

    public static void error(String format, Object... arguments) {
        log.error(getTag() + format, arguments);
    }

    /**
     * 通过调用栈获取调用处的类名和方法名，去除lambda语句中的调用栈
     * 从调用栈中找到第一个既不是 Lambda 方法、又属于目标包的调用者，并返回其类名和方法名。
     * 例子
     * 层级	类名	方法名	说明
     * 0	java.lang.Thread	getStackTrace	JVM 内部方法（跳过，层级=1小于3）
     * 1	com.example.LoggerUtil	getTag	当前方法（跳过，层级=2小于3）
     * 2	com.example.OrderService	lambda$createOrder$0	（跳过，Lambda 方法）
     * 3	com.example.OrderService	createOrder	目标方法（命中，业务代码）
     * 4	com.example.OrderController	submitOrder	调用者（跳过，非目标包）
     * 5	org.springframework.web.Servlet	doDispatch	（跳过，非目标包）
     *
     * @return "${shortenClassName} ${methodName} "
     */
    private static String getTag() {
        // 获取调用类
        StackTraceElement stackTraceElement = null;
        // 从调用栈的第3层开始遍历（跳过前两层）
        for (int i = 3; i < Thread.currentThread().getStackTrace().length; i++) {
            stackTraceElement = Thread.currentThread().getStackTrace()[i];
            // 过滤掉 lambda 方法和非目标包（com.pinduoduo.cargod）的调用
            if (!stackTraceElement.getMethodName().startsWith(LAMBDA_METHOD_NAME) && stackTraceElement.getClassName().startsWith(TARGET_PACKAGE_PATH)) {
                break;
            }
        }

        // 调用类为空返回空字符串
        if (Objects.isNull(stackTraceElement)) {
            return EMPTY_STR;
        }

        // 全限定类名
        String className = stackTraceElement.getClassName();
        // 方法名
        String methodName = stackTraceElement.getMethodName();
        // 全限定类名最后一层
        String shortenClassName = className.substring(className.lastIndexOf(".") + 1);

        // 例如：className = "com.pinduoduo.cargod.service.OrderService" methodName = "createOrder"
        // 返回：OrderService createOrder
        return shortenClassName + BLANK_STR + methodName + BLANK_STR;
    }
}
