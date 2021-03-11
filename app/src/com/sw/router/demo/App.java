package com.sw.router.demo;

import android.app.Application;

import com.sw.fm.router.navigator._SwRoute;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        _SwRoute.init(this);
    }
}
