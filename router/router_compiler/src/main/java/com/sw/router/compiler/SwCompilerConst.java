package com.sw.router.compiler;

public final class SwCompilerConst {
    private SwCompilerConst(){}
    public static final String PACKAGE_NAME = "com.sw.router";
    public static final String I_ROUTE_ROOT = PACKAGE_NAME + ".IRouteRoot";
    public static final String I_ROUTE_GROUP = PACKAGE_NAME + ".IRouteGroup";
    public static final String JAVA_FILE_NAME_PREFIX_ROOT = "SwRouter$$Root$$";
    public static final String JAVA_FILE_NAME_PREFIX_GROUP = "SwRouter$$Group$$";
    public static final String KEY_MODULE_NAME = "SW_ROUTER_MODULE_NAME";
    public static final String NO_MODULE_NAME_TIPS = "SwRouter Compiler:: no module name found at 'build.gradle', like :\n" +
            "android {\n" +
            "    defaultConfig {\n" +
            "        ...\n" +
            "        javaCompileOptions {\n" +
            "            annotationProcessorOptions {\n" +
            "                arguments = [SW_ROUTER_MODULE_NAME: project.getName()]\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
}
