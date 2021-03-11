package com.sw.fm.router.navigator;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.sw.router.IRouteRoot;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public final class _SwRoute {
    private static final String TAG = "_SwRoute";
    private static final String DOT = ".";
    private static final String AUTO_GENERATE_FILE_PACKAGE = "com.sw.router";
    private static final String CLASS_NAME_PREFIX_ROOT = AUTO_GENERATE_FILE_PACKAGE + DOT + "SwRouter$$Root$$";
    private static final String CLASS_NAME_PREFIX_GROUP = AUTO_GENERATE_FILE_PACKAGE + DOT + "SwRouter$$Group$$";

    private _SwRoute() {
    }

    public synchronized static void init(Context context) {
        long startTime = System.currentTimeMillis();
        try {
            Set<String> classNameSet;
            if (Util.isNewVersion(context) || BuildConfig.DEBUG) {
                //版本更新或者debug模式，每次都直接读文件列表
                classNameSet = loadClassNames(context);
            } else {
                //优先从缓存读取，比如sp
                classNameSet = Util.getClassNameSet(context);
                if (classNameSet == null) {
                    classNameSet = loadClassNames(context);
                }
            }
            for (String className : classNameSet) {
                if (className.startsWith(CLASS_NAME_PREFIX_ROOT)) {
                    ((IRouteRoot) (Class.forName(className).newInstance())).loadInto(RouteHelperV2.sGroupMap);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        long costTime = System.currentTimeMillis() - startTime;
        Log.i(TAG, "init complete, cost " + costTime + "ms");
    }

    private static Set<String> loadClassNames(Context context) throws PackageManager.NameNotFoundException, IOException, InterruptedException {
        Set<String> allClassNameSet = ClassUtils.getFileNameByPackageName(context.getApplicationContext(), AUTO_GENERATE_FILE_PACKAGE);
        Set<String> classNameSet = getRootClassNameSet(allClassNameSet);
        Util.saveClassNameSet(context, classNameSet);
        return classNameSet;
    }

    private static Set<String> getRootClassNameSet(Set<String> classNameSet) {
        Set<String> result = new HashSet<>();
        for (String className : classNameSet) {
            if (className.startsWith(CLASS_NAME_PREFIX_ROOT)) {
                result.add(className);
            }
        }
        return result;
    }
}
