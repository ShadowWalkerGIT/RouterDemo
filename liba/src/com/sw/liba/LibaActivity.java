package com.sw.liba;

import android.app.Activity;
import android.os.Bundle;

import com.sw.router.Route;

@Route(group = "liba", path = "/Liba")
public class LibaActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liba);
    }
}