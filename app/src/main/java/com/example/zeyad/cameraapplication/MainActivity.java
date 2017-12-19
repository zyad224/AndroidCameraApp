package com.example.zeyad.cameraapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.arch.persistence.room.Room;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import java.util.List;
import com.example.zeyad.cameraapplication.database.*;

import pl.aprilapps.easyphotopicker.EasyImage;


public class MainActivity extends AppCompatActivity {


    private static AppDatabase db;
    private static boolean databaseLoaded=false;

    private Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        activity= this;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        if (db==null)
            db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "images_database")
                    .addMigrations(AppDatabase.MIGRATION_1_2)
                    .build();



        // these functions will be used with the GPS
        // to insert and retrieves images from DB
        /*
        new InsertIntoDatabaseTask().execute();
        // search the database
        new SearchDatabaseTask().execute();*/


        FloatingActionButton fabCamera = (FloatingActionButton) findViewById(R.id.fab_camera);
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyImage.openCamera(getActivity(), 0);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class InsertIntoDatabaseTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Location location = new Location(53.3829700, -1.4659000, 20);
            db.imageDao().insertLocation(location);
           // Image image= new Image(getApplicationContext(), R.drawable.joe1, "joe1", "this is Joe 1", location.getId());
           // db.imageDao().insertImage(image);


            return null;
        }
    }

    private class SearchDatabaseTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Log.i("MainActivity", "finding Joe1");
            List<Image> imageList = db.imageDao().findImageByTitle("joe1");
            for (Image imageX : imageList) {
                Log.i("MainActivity", "title: " + imageX.getTitle() + "  description: " + imageX.getDescription());
            }


            Log.i("MainActivity", "finding by area");
            imageList = db.imageDao().findImagesByArea(53.38297, 1.46590, 100);
            for (Image imageX : imageList) {
                Log.i("MainActivity", "title: " + imageX.getTitle() + "  description: " + imageX.getDescription());
            }
            return null;
        }
    }


    @Override
    protected void onResume(){
        super.onResume();

    }

    public Activity getActivity(){return activity;}

}
