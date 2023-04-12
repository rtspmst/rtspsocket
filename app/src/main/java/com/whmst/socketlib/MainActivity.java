package com.whmst.socketlib;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.whmst.socketlibrary.TTTTT;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TTTTT.sdkj(5 , 66);
    }
}