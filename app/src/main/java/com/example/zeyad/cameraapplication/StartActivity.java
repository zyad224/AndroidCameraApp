package com.example.zeyad.cameraapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

/**
 *
 * This activity is splashing in the application
 *
 * It displays the application logo for welcoming
 *
 */
public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Log.i("try","out");

        Thread myThread = new Thread(){

            @Override
            public void run(){
                try {
                    Log.i("try","ın");
                    sleep(5000);
                    Intent intent = new Intent( getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        myThread.start();
    }
}
