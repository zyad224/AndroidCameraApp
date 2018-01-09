package com.example.zeyad.cameraapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zeyad.cameraapplication.database.AppDatabase;
import com.example.zeyad.cameraapplication.database.Image;
import com.example.zeyad.cameraapplication.database.Location;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sakin on 18.12.2017.
 */

public class MapActivity extends AppCompatActivity   {

    private TextView textView;
    private static GoogleMap mMap;
    private static final int MY_ACCESS_FINE_LOCATION = 123;
    private BroadcastReceiver broadcastReceiver; // getting tha data
    private static AppDatabase db;
    public Marker m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(onMapReadyCallback);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db=MainActivity.getDB();

        new GetLocations().execute();

    }

    private class GetLocations extends AsyncTask<Void, Void, List<Wrapper> > {
        @Override
        protected List<Wrapper> doInBackground(Void... Voids) {

            List<Location> locations=new ArrayList<>();
            List<Image>images=new ArrayList<>();
            List<Wrapper>locationsAndTitles=new ArrayList<>();
            locations=db.imageDao().loadLocations();
            Wrapper w;
            if(!locations.isEmpty()){
                 for(Location lo:locations){

                     Image img=db.imageDao().findImageByLocationID(lo.getId());
                     LatLng locOnMap = new LatLng(lo.getLatitude(), lo.getLongitude());


                     if(img!=null ) {
                         w = new Wrapper(img, locOnMap);
                         locationsAndTitles.add(w);
                     }

                 }
            }


            return locationsAndTitles;
        }

        @Override
        protected void onPostExecute(List<Wrapper> w) {

            for(Wrapper wo: w) {

                m=   mMap.addMarker(new MarkerOptions().position(wo.getPositionOnMap()).title(wo.getImage().getImagepath())

                        .snippet(wo.getImage().getTitle()+"\n"+wo.getImage().getDescription()+"\n"+wo.getImage().getDate()+"\n"+wo.getImage().getImageLength()+","+wo.getImage().getImageWidth())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                );

                if (mMap != null) {

                    mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                        @Override
                        public View getInfoWindow(Marker marker) {
                            return null;
                        }

                        @Override
                        public View getInfoContents(Marker marker) {

                            View v = getLayoutInflater().inflate(R.layout.info_window, null);
                            TextView description = (TextView) v.findViewById(R.id.description_map);
                            ImageView imageView = (ImageView) v.findViewById(R.id.imageMap);

                            LatLng ll = marker.getPosition();
                            description.setText(marker.getSnippet());
                            imageView.setImageBitmap(MyAdapter.decodeSampledBitmapFromResource(marker.getTitle(), 50, 50));
                            return v;
                        }
                    });
                }

            }
            Toast.makeText(getBaseContext(), "Locations Updated on Map", Toast.LENGTH_LONG).show();
        }
    }

    OnMapReadyCallback onMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            mMap.getUiSettings().setZoomControlsEnabled(true);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(0);
            mMap.animateCamera(zoom);


        }
    };
}
