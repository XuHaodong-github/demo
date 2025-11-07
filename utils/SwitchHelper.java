@Slf4j
public class SwitchHelper {

    public static final String CAT_EVENT_TYPE = "SwitchHelper";


    public static String returnNotDigestedMallNameGray(String mallName, Long mallId, Supplier<String> supplier) {
        Cat.logEvent(CAT_EVENT_TYPE, "returnNotDigestedMallNameGray enter");
        log.info(">>>>>>> returnNotDigestedMallNameGray, mallName:{}, mallId:{}", mallName, mallId);
        // 如果在灰度内，则返回原始名称
        if (GrayHelper.isInGray(mallId, MERCHANT_NAME_NOT_DIGEST_GRAY)) {
            log.info("<<<<<<< returnNotDigestedMallNameGray, mallName:{}, mallId:{}, return mallName:{}", mallName, mallId, mallName);
            return mallName;
        }
        // 如果不在灰度，则返回被打码的名称
        String supplierGetMallName = null;
        try {
            supplierGetMallName = supplier.get();
        } catch (Exception e) {
            Cat.logEvent(CAT_EVENT_TYPE, "returnNotDigestedMallNameGray fail");
            log.error("returnNotDigestedMallNameGray fail, e->", e);
            // 原打码方法报错，抛出异常
            throw e;
        }
        log.info("<<<<<<< returnNotDigestedMallNameGray, mallName:{}, mallId:{}, return mallName:{}", mallName, mallId, supplierGetMallName);
        return supplierGetMallName;
    }


    public static <T> T returnNotDigestedMallNameMethodGray(Supplier<T> newSupplier, Long mallId, Supplier<T> oldSupplier) {
        Cat.logEvent(CAT_EVENT_TYPE, "returnNotDigestedMallNameMethodGray enter");
        log.info(">>>>>>> returnNotDigestedMallNameMethodGray, mallId:{}", mallId);
        // 如果在灰度内，则返回 sentry 提供的名称
        if (GrayHelper.isInGray(mallId, MERCHANT_NAME_NOT_DIGEST_GRAY)) {
            T newMallName = null;
            try {
                newMallName = newSupplier.get();
            } catch (Exception e) {
                Cat.logEvent(CAT_EVENT_TYPE, "returnNotDigestedMallNameMethodGray newSupplier get fail");
                log.error("returnNotDigestedMallNameMethodGray newSupplier get fail, e->", e);
                throw e;
            }
            log.info("<<<<<<< returnNotDigestedMallNameMethodGray newSupplier get, mallId:{}, return mallName:{}", mallId, newMallName);
            return newMallName;
        }
        // 如果不在灰度，则返回 montrealSupplier 提供的名称
        T oldMallName = null;
        try {
            oldMallName = oldSupplier.get();
        } catch (Exception e) {
            Cat.logEvent(CAT_EVENT_TYPE, "returnNotDigestedMallNameMethodGray oldSupplier get fail");
            log.error("returnNotDigestedMallNameMethodGray oldSupplier get fail, e->", e);
            throw e;
        }
        log.info("<<<<<<< returnNotDigestedMallNameMethodGray oldSupplier get, mallId:{}, return mallName:{}", mallId, oldMallName);
        return oldMallName;
    }


}
