package com.example.zeyad.cameraapplication;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import com.example.zeyad.cameraapplication.database.AppDatabase;
import com.example.zeyad.cameraapplication.database.Image;
import com.example.zeyad.cameraapplication.database.Location;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    private Image path;
    public  final static String SER_KEY = "serial";
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

                if(element.getImagePath()!=null)
                    new getImageFrmDb_imgPath().execute(element.getImagePath());


            }

        }


        FloatingActionButton saveDetails = (FloatingActionButton) findViewById(R.id.saveDetails);
        FloatingActionButton uploadServer = (FloatingActionButton) findViewById(R.id.uploadServer);

        saveDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title.setFocusable(false);
                description.setFocusable(false);
                element.setTitle(title.getText().toString());
                element.setDescription(description.getText().toString());
                date.setText(reportDate);
                element.setDate(date.getText().toString());

                finish();

            }
        });


        uploadServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent= new Intent(ShowDetails.this, ServerActivity.class);
                Bundle b=new Bundle();
                b.putSerializable(SER_KEY,element);
                intent.putExtras(b);

                startActivity(intent);



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

    private class getImageFrmDb_imgPath extends AsyncTask<String, Void, Image>{
        @Override
        protected Image doInBackground(String... s) {
           Image image= db.imageDao().findImageByPath(s[0]);
            return image;
        }

        @Override
        protected void onPostExecute(Image image) {
            super.onPostExecute(image);
            title.setText(image.getTitle());
            description.setText(image.getDescription());
           // date.setText(element.getDate());
            Location location=db.imageDao().findLocationById(image.getLocationId());
            latitude.setText(String.valueOf(location.getLatitude()));
            longitude.setText(String.valueOf(location.getLongitude()));

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

            //path = db.imageDao().findImageByPath(element.file.toString());

            //////////////

           /* path = db.imageDao().findImageByPath(element.getImagePath());

            if(path.getImagepath()!="") {
                title.setText(path.getTitle());
                description.setText(path.getDescription());

            }*/

           //////////////////////

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
