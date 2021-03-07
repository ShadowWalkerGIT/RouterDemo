package com.sw.router.demo;

import android.app.Activity;
import android.os.Bundle;

import com.sw.router.Route;
import com.sw.router.routerdemo.R;

@Route(group = "123", path = "/Main2Ac")
public class Main2Activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
