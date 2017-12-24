package com.example.zeyad.cameraapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zeyad.cameraapplication.database.AppDatabase;
import com.example.zeyad.cameraapplication.database.Image;
import com.example.zeyad.cameraapplication.database.Location;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ShowDetails extends AppCompatActivity {

    private EditText title;
    private EditText description;
    private TextView date;
    private TextView latitude;
    private TextView longitude;
    private ImageElement element;
    private ImageView imageView;
    private  String reportDate;
    private static GoogleMap mMap;
    private static AppDatabase db;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_details);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this::onMapReady);

        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        // Get the date today using Calendar object.
        Date today = Calendar.getInstance().getTime();
        // Using DateFormat format method we can create a string
// representation of a date with the defined format.
        reportDate = df.format(today);

        title= (EditText) findViewById(R.id.title);
        description= (EditText) findViewById(R.id.details);
        date = (TextView) findViewById(R.id.date);
        latitude =(TextView) findViewById(R.id.latitude);
        longitude = (TextView) findViewById(R.id.longitude);

        Bundle b = getIntent().getExtras();
        int position=-1;
        title.setFocusableInTouchMode(true);
        description.setFocusableInTouchMode(true);
        if(b != null) {
            // this is the image position in the itemList
            position = b.getInt("position");
            if (position!=-1){
                imageView = (ImageView) findViewById(R.id.image_copy);
                element= MyAdapter.getItems().get(position);

                if (element.image!=-1) {
                    imageView.setImageResource(element.image);
                } else if (element.file!=null) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(element.file.getAbsolutePath());
                    imageView.setImageBitmap(myBitmap);

                }

                title.setText(element.getTitle());
                description.setText(element.getDescription());
                date.setText(element.getDate());

                latitude.setText(String.valueOf(element.getLatitude()));
                longitude.setText(String.valueOf(element.getLongitude()));
            }

        }


        FloatingActionButton saveDetails = (FloatingActionButton) findViewById(R.id.saveDetails);
        saveDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title.setFocusable(false);
                description.setFocusable(false);
                element.setTitle(title.getText().toString());
                element.setDescription(description.getText().toString());
                date.setText(reportDate);
                element.setDate(date.getText().toString());


                //save image
                //get the db
                db= MainActivity.getDB();
                new InsertIntoDatabaseTask().execute();
                new SearchDatabaseTask().execute();
                finish();

            }
        });
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(element.getLatitude(), element.getLongitude());
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }
    private class InsertIntoDatabaseTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            ///////////////////// GPS
            Location location = new Location(element.getLatitude(), element.getLongitude(), 20);
            db.imageDao().insertLocation(location);
            Image image=new Image(getApplicationContext(),element.getTitle(),element.getDescription(),element.getImagePath(),location.getId());
            db.imageDao().insertImage(image);

            return null;
        }
    }

    private class SearchDatabaseTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Log.i("MainActivity", "finding Joe1");
            List<Image> imageList = db.imageDao().findImageByTitle("moh");
            for (Image imageX : imageList) {
                Log.i("MainActivity", "title: " + imageX.getTitle() + "  description: " + imageX.getDescription());
                Log.i("MainActivity", "imgpath: " + imageX.getImagepath() + "  locid: " + imageX.getLocationId());

            }


           /* Log.i("MainActivity", "finding by area");
            imageList = db.imageDao().findImagesByArea(53.38297, 1.46590, 100);
            for (Image imageX : imageList) {
                Log.i("MainActivity", "title: " + imageX.getTitle() + "  description: " + imageX.getDescription());
            }*/
            return null;
        }
    }
    private void initilaizeFields()
    {
        title.setText("Title");
        description.setText("Description");

    }
}
