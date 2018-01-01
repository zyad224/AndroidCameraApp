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
import java.util.List;

/**
 * Created by sakin on 18.12.2017.
 */

public class MapActivity extends AppCompatActivity  implements GoogleMap.OnMarkerClickListener {
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

   private Marker m;


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


                     if(img!=null && img.getTitle()!=null) {
                         w = new Wrapper(img, locOnMap);
                         locationsAndTitles.add(w);
                     }

                 }
            }


            return locationsAndTitles;
        }

        @Override
        protected void onPostExecute(List<Wrapper> w) {
            Bitmap myBitmap;
            for(Wrapper wo: w) {

               m=   mMap.addMarker(new MarkerOptions().position(wo.getPositionOnMap()).title(wo.getImage().getTitle())
                       .icon(BitmapDescriptorFactory.fromBitmap(myBitmap =decodeSampledBitmapFromResource(wo.getImage().getImagepath(), 50, 50))));
               addListenerstoMarkers();
            }

            Toast.makeText(getBaseContext(), "Locations Updated on Map", Toast.LENGTH_LONG).show();

        }
    }


    OnMapReadyCallback onMapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            mMap.getUiSettings().setZoomControlsEnabled(true);
            CameraUpdate zoom=CameraUpdateFactory.zoomTo(0);
            mMap.animateCamera(zoom);


        }
    };

    public static Bitmap decodeSampledBitmapFromResource(String filePath, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public void addListenerstoMarkers(){

        mMap.setOnMarkerClickListener(this);
    }
    @Override
    public boolean onMarkerClick(final Marker marker) {

        LatLng markerPosition= marker.getPosition();
        String markerTitle=marker.getTitle();

        return false;
    }

}
