package com.sw.router;

import java.lang.reflect.Method;

public class RouterHelper {
    private static volatile RouterHelper sInstance;
    private static final String ROUTER_CLASS_NAME = "com.sw.router.Router";
    private static final String METHOD_NAME = "getClassByRoute";
    private static Method sGetClassByPathMethod;
    private static Method sGetClassByQualifiedPathMethod;
    private static Class sRouterClass;
    private static Object sRouter;

    private RouterHelper() {
    }

    public static RouterHelper getInstance() {
        if (sInstance == null) {
            synchronized (RouterHelper.class) {
                if (sInstance == null) {
                    sInstance = new RouterHelper();
                }
            }
        }
        return sInstance;
    }

    public static Class getRealClass(String path) throws Exception {
        return (Class) getClassByPathMethod().invoke(getRouter(), path);
    }

    public static Class getRealClass(String group, String path) throws Exception {
        return (Class) getClassByQualifiedPathMethod().invoke(getRouter(), group, path);
    }

    private static Class getRouterClass() throws Exception {
        if (sRouterClass != null) {
            return sRouterClass;
        }
        sRouterClass = Class.forName(ROUTER_CLASS_NAME);
        return sRouterClass;
    }

    private static Method getClassByPathMethod() throws Exception {
        if (sGetClassByPathMethod == null) {
            sGetClassByPathMethod = getRouterClass().getMethod(METHOD_NAME, String.class);
        }
        return sGetClassByPathMethod;
    }

    private static Method getClassByQualifiedPathMethod() throws Exception {
        if (sGetClassByQualifiedPathMethod == null) {
            sGetClassByQualifiedPathMethod = getRouterClass().getMethod(METHOD_NAME, String.class, String.class);
        }
        return sGetClassByQualifiedPathMethod;
    }

    private static Object getRouter() throws Exception {
        if (sRouter == null) {
            sRouter = getRouterClass().newInstance();
        }
        return sRouter;
    }
}
