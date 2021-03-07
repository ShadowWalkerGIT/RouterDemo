package com.sw.fm.router.navigator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sw.router.RouteHelper;

public final class Navigator {
    private Navigator() {
    }

    public static void navigate(Context context, String qualifiedPath) {
        navigate(context, qualifiedPath, new Bundle());
    }

    public static void navigate(Context context, String qualifiedPath, Bundle extras) {
        try {
            navigate(context, RouteHelper.getRealClass(qualifiedPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void navigate(Context context, String group, String path) {
        navigate(context, group, path, null);
    }

    public static void navigate(Context context, String group, String path, Bundle extras) {
        try {

            navigate(context, RouteHelper.getRealClass(group, path), extras);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void navigate(Context context, Class clazz) {
        navigate(context, clazz, null);
    }

    public static void navigate(Context context, Class clazz, Bundle extras) {
        if (extras == null) {
            extras = new Bundle();
        }
        Intent intent = new Intent(context, clazz);
        intent.putExtras(extras);
        context.startActivity(intent);
    }
}
