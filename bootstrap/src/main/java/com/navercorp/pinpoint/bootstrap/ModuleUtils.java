package com.navercorp.pinpoint.bootstrap;

/**
 * 模块工具类
 * @author Woonduk Kang(emeroad)
 * @author dean
 */
final class ModuleUtils {

    private static final boolean moduleSupport = checkModuleClass();

    //判断模块功能是否支持
    private static boolean checkModuleClass() {
        try {
            Class.forName("java.lang.Module", false, null);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    //判断是是否支持模块
    static boolean isModuleSupported() {
        return moduleSupport;
    }
}
