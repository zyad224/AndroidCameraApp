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
 *
 * Activity for showing all of the pictures on google map
 *
 * This class takes the pictures path and
 * also the details of images from database and
 * showing them on google map with using markers.
 *
 * When we click markers, we can see picture and picture details
 * (title, description, date etc.)
 *
 */

public class MapActivity extends AppCompatActivity   {

    // declarations
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

        // database
        db=MainActivity.getDB();

        // execute the thread to get all locations from database
        new GetLocations().execute();

    }

    /**
     *
     * The class ,which extends AsyncTask, gets the all of pictures location
     * from database in Background and with these locations, adding markers
     * and getting the details of images from database on execute time.
     *
     *
     */
    private class GetLocations extends AsyncTask<Void, Void, List<Wrapper> > {

        /**
         * Method that get all of information in background
         * @param Voids
         * @return a list of the images details
         */
        @Override
        protected List<Wrapper> doInBackground(Void... Voids) {

            // declare list for locations
            List<Location> locations=new ArrayList<>();
            List<Image>images=new ArrayList<>();
            // declare list for all image details
            List<Wrapper>locationsAndTitles=new ArrayList<>();
            // get all locations from database
            locations=db.imageDao().loadLocations();
            Wrapper w;

            if(!locations.isEmpty()){
                 for(Location lo:locations){

                     // get the image with location id
                     Image img=db.imageDao().findImageByLocationID(lo.getId());
                     LatLng locOnMap = new LatLng(lo.getLatitude(), lo.getLongitude());

                     if(img!=null ) {
                         // add image and location in Wrapper activity object
                         w = new Wrapper(img, locOnMap);
                         locationsAndTitles.add(w);
                     }

                 }
            }
            return locationsAndTitles;
        }

        /**
         * Method that works on execute time and adds the markers on map
         *
         * @param w
         */
        @Override
        protected void onPostExecute(List<Wrapper> w) {

            for(Wrapper wo: w) {

                // create a marker and get the informations(list of the wrapper objects) from background method and add in marker
                // in title we gave image path to use it in screen
                // in snippet we gave the all details of image
                m=   mMap.addMarker(new MarkerOptions().position(wo.getPositionOnMap()).title(wo.getImage().getImagepath())

                        .snippet(wo.getImage().getTitle()+"\n"+wo.getImage().getDescription()+"\n"+wo.getImage().getDate()+"\n"+wo.getImage().getImageLength()+","+wo.getImage().getImageWidth())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                );


                if (mMap != null) {

                    // With this function, we are filling the screen form in map

                    mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                        @Override
                        public View getInfoWindow(Marker marker) {
                            return null;
                        }

                        // take the information from marker and add them in layout.
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

    /**
     *
     * This method is used to create a map
     *
     */
    OnMapReadyCallback onMapReadyCallback = new OnMapReadyCallback() {
        /**
         * Method that creates the object of google map and also
         * zoom feature.
         *
         * @param googleMap
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            mMap.getUiSettings().setZoomControlsEnabled(true);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(0);
            mMap.animateCamera(zoom);


        }
    };
}
