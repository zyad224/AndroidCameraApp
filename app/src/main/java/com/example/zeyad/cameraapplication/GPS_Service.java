package com.example.zeyad.cameraapplication;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by sakin on 18.12.2017.
 */

public class GPS_Service extends Service {
    private LocationListener listener;
    private LocationManager locationManager;
    boolean locationChanged;

    @Nullable
    @Override
    public IBinder onBind(Intent ıntent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    public void onCreate() {

        listener = new LocationListener() {
            // transfer the data from this method to main activity
            // broadcast reciever class

            @Override
            public void onLocationChanged(Location location) {
                // intent filter
                Log.e("s", "onLocationChanged: "+locationChanged );
                Log.e("s", "onLocationChanged: "+location.getLatitude()+"  "+location.getLongitude());

               // myLastLocation.set(location);
               // locationChanged=true;
                Intent i = new Intent("location_update");
                i.putExtra("Longitude",location.getLongitude());
                i.putExtra("Latitude",location.getLatitude());
                sendBroadcast(i);

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            // initialize the location
            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        // update location
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
    }
    public void onDestroy(){
        super.onDestroy();
        if(locationManager != null){
            locationManager.removeUpdates(listener);
        }
    }
}
