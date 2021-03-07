package com.sw.fm.router.navigator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sw.router.RouteHelper;

public final class Navigator {
    private Navigator() {
    }

    public static void startActivity(Context context, String qualifiedPath) {
        startActivityForResult(context, qualifiedPath, -1);
    }

    public static void startActivityForResult(Context context, String qualifiedPath, int requestCode) {
        startActivityForResult(context, qualifiedPath, requestCode, new Bundle());
    }

    public static void startActivityForResult(Context context, String qualifiedPath, int requestCode, Bundle extras) {
        try {
            startActivityForResult(context, RouteHelper.getRealClass(qualifiedPath), requestCode, extras);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startActivity(Context context, String group, String path) {
        startActivityForResult(context, group, path, -1);
    }

    public static void startActivityForResult(Context context, String group, String path, int requestCode) {
        startActivityForResult(context, group, path, requestCode, null);
    }

    public static void startActivityForResult(Context context, String group, String path, int requestCode, Bundle extras) {
        try {
            startActivityForResult(context, RouteHelper.getRealClass(group, path), requestCode, extras);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startActivity(Context context, Class clazz) {
        startActivityForResult(context, clazz, -1);
    }

    public static void startActivityForResult(Context context, Class clazz, int requestCode) {
        startActivityForResult(context, clazz, requestCode, null);
    }

    public static void startActivityForResult(Context context, Class clazz, int requestCode, Bundle extras) {
        if (extras == null) {
            extras = new Bundle();
        }
        Intent intent = new Intent(context, clazz);
        intent.putExtras(extras);
        if (requestCode != -1) {
            if (context instanceof Activity) {
                ((Activity) context).startActivityForResult(intent, requestCode);
            } else {
                throw new IllegalArgumentException("Context must be instance of Activity");
            }
        } else {
            context.startActivity(intent);
        }
    }
}
