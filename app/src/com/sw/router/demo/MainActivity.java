package com.sw.router.demo;

import android.app.Activity;
import android.os.Bundle;

import com.sw.fm.router.navigator.Navigator;
import com.sw.router.Route;
import com.sw.router.routerdemo.R;

@Route(group = "app", path = "/MainAc")
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_main).setOnClickListener(v -> Navigator.navigate(this, "/123/Main22Ac"));
    }
}
