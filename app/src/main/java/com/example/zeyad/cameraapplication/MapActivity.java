package com.example.zeyad.cameraapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.zeyad.cameraapplication.database.AppDatabase;
import com.example.zeyad.cameraapplication.database.Image;
import com.example.zeyad.cameraapplication.database.Location;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sakin on 18.12.2017.
 */

public class MapActivity extends AppCompatActivity {
    private Button mButtonStart;
    private Button mButtonStop;
    private TextView textView;
    private static GoogleMap mMap;
    private static final int MY_ACCESS_FINE_LOCATION = 123;
    private BroadcastReceiver broadcastReceiver; // getting tha data
    //static MapsActivity activity;
    private static AppDatabase db;

    private double longitude;
    private double latitude;

    protected void onResume() {

        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver(){

                @Override
                public void onReceive(Context context, Intent intent) {

                    textView.append("\n" + intent.getExtras().get("Longitude"));
                    textView.append("\n" + intent.getExtras().get("Latitude"));
                    longitude =(double)intent.getExtras().get("Longitude");
                    latitude = (double)intent.getExtras().get("Latitude");
                    LatLng sydney = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
    }

    protected void onDestroy(){
        super.onDestroy();
        if(broadcastReceiver !=null){
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(onMapReadyCallback);

        textView = (TextView) findViewById(R.id.textView);
        mButtonStart = (Button) findViewById(R.id.button_start);

        //mButtonStart.setEnabled(true);

        mButtonStop = (Button) findViewById(R.id.button_stop);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db=MainActivity.getDB();

        //new GetAllImage().execute();

        //mButtonStop.setEnabled(false);
        if(!runtime_permission()){
            enable_button();
        }
    }
    private void enable_button(){

        mButtonStart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), GPS_Service.class);
                startService(i);
                //startLocationUpdates();
                //if(mButtonStop!=null)
                //  mButtonStop.setEnabled(true);
                //mButtonStart.setEnabled(true);
            }
        });

        mButtonStop.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),GPS_Service.class);
                stopService(i);
                //stopLocationUpdates();
                //if(mButtonStart!=null)
                //    mButtonStart.setEnabled(true);
                //mButtonStop.setEnabled(true);
            }
        });
    }

    private boolean runtime_permission(){
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission
                .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest
                .permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission
                    .ACCESS_COARSE_LOCATION},100);

            return true; // if we need permission checking
        }
        return false; // if we do not need permission checking
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch (requestCode) {case MY_ACCESS_FINE_LOCATION: {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager
                    .PERMISSION_GRANTED) {
                enable_button();
                //startLocationUpdatesAux(getApplicationContext());
            } else {
                runtime_permission();
            }
            return;
        }
        // other 'case' lines to check for other
        // permissions this app might request

        }
    }

    private class GetAllImage extends AsyncTask<Void, Void, Void > {
        @Override
        protected Void doInBackground(Void... Voids) {

            //This Part will take the images from db and write in main page
            List<Image> imageList=new ArrayList<>();

            if(db.imageDao().imageCount()!=0) {
                imageList = db.imageDao().loadImages();
                for (Image img : imageList) {

                    Location location=db.imageDao().findLocationById(img.getLocationId());
                    LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                    Log.i("latitude", "value: " + location.getLatitude());
                    Log.i("latitude", "value: " + location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(position).title("gfj"));

                }
            }

            Log.i("imgCount", "count: " + db.imageDao().imageCount());

            return null;
        }

    }


    OnMapReadyCallback onMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            // Add a marker in Sydney and move the camera
            LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            mMap.getUiSettings().setZoomControlsEnabled(true);
            CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);
            mMap.animateCamera(zoom);

        }
    };
}
