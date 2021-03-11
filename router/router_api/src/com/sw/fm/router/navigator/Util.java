package com.sw.fm.router.navigator;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.util.Set;

public class Util {
    private static final String SP_NAME = "sw_router_routes";
    private static final String KEY_LAST_VERSION_CODE = "LAST_VERSION_CODE";
    private static final String KEY_LAST_VERSION_NAME = "LAST_VERSION_NAME";
    private static final String KEY_ROUTE_SET = "SW_CLASS_NAME_SET";

    private Util() {
    }

    public static boolean isNewVersion(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
            if (pi.versionCode != sp.getInt(KEY_LAST_VERSION_CODE, -1) || !TextUtils.equals(pi.versionName, sp.getString(KEY_LAST_VERSION_NAME, ""))) {
                sp.edit().putInt(KEY_LAST_VERSION_CODE, pi.versionCode).putString(KEY_LAST_VERSION_NAME, pi.versionName).apply();
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return true;
        }
    }

    public static Set<String> getClassNameSet(Context context) {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).getStringSet(KEY_ROUTE_SET, null);
    }

    public static void saveClassNameSet(Context context, Set<String> classNameSet) {
        if (classNameSet != null) {
            context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).edit().putStringSet(KEY_ROUTE_SET, classNameSet).apply();
        }
    }
}
