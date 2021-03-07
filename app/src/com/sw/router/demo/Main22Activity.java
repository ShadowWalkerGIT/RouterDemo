package com.sw.router.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.sw.router.Route;
import com.sw.router.routerdemo.R;

@Route(group = "123", path = "/Main22Ac")
public class Main22Activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity2);
        Toast.makeText(this, "I'm 2", Toast.LENGTH_LONG).show();
    }
}
