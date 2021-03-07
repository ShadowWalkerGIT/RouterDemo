package com.sw.router;

import java.lang.reflect.Method;

public class RouteHelper {
    private static final String ROUTER_CLASS_NAME = "com.sw.router.Router";
    private static final String METHOD_NAME = "getClassByRoute";
    private static Method sGetClassByPathMethod;
    private static Method sGetClassByQualifiedPathMethod;
    private static Class sRouterClass;

    private RouteHelper() {
    }

    public static Class getRealClass(String path) throws Exception {
        return (Class) getClassByPathMethod().invoke(getRouterClass(), path);
    }

    public static Class getRealClass(String group, String path) throws Exception {
        return (Class) getClassByQualifiedPathMethod().invoke(getRouterClass(), group, path);
    }

    private static Class getRouterClass() throws Exception {
        if (sRouterClass == null) {
            sRouterClass = Class.forName(ROUTER_CLASS_NAME);
        }
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
}
