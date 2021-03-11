package com.sw.fm.router.navigator;

import android.text.TextUtils;
import android.util.Log;

import com.sw.router.IRouteGroup;
import com.sw.router.RouteMeta;

import java.util.HashMap;
import java.util.Map;

public final class RouteHelperV2 {
    private static final String TAG = "RouteHelperV2";
    //groups.put("app", SwRouter$$Group$$app.class);对应key:groupName, value: group_class
    //SwRouter$$Group$$app.class里面添加了如下内容
    //routes.put("/Main22Ac", new RouteMeta("123","/Main22Ac","com.sw.router.demo.Main22Activity"));
    //routes.put("/Main2Ac", new RouteMeta("123","/Main2Ac","com.sw.router.demo.Main2Activity"));
    public static Map<String, Class<? extends IRouteGroup>> sGroupMap = new HashMap<>();
    public static Map<String, RouteMeta> sRouteMap = new HashMap<>();

    private RouteHelperV2() {
    }

    public static Class getRealClass(String path) throws Exception {
        checkPath(path);
        String group = getDefaultGroup(path);
        return getRealClass(group, path.substring(1 + group.length()));
    }

    public static Class getRealClass(String group, String path) throws Exception {
        if (!sGroupMap.containsKey(group)) {
            throw new RuntimeException("no group found : " + group);
        }
        if (!sRouteMap.containsKey(path)) {
            //load SwRouter$$Group$$**.class, ** is group
            ((IRouteGroup) (sGroupMap.get(group).newInstance())).loadInto(sRouteMap);
        }
        if (!sRouteMap.containsKey(path)) {
            throw new RuntimeException("no path found group : " + group + " ; path : " + path);
        }
        RouteMeta routeMeta = sRouteMap.get(path);
        if (routeMeta.getDestinationClass() == null) {
            routeMeta.setDestinationClass(Class.forName(routeMeta.getDestinationQualifiedName()));
        }
        return routeMeta.getDestinationClass();
    }

    private static String getDefaultGroup(String path) {
        checkPath(path);
        return path.substring(1, path.indexOf("/", 1));
    }

    private static void checkPath(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            String msg = "path cannot be null and must start with /";
            Log.d(TAG, msg);
            throw new RuntimeException(msg);
        }
    }
}
