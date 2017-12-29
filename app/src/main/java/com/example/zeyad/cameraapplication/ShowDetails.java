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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
    private int position;
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

        //getSupportActionBar().setTitle();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        position=-1;
        //title.setFocusableInTouchMode(true);
        title.setEnabled(false);
        //title.setFocusable(false);
        description.setEnabled(false);
        //description.setFocusableInTouchMode(true);

        db=MainActivity.getDB();
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

                Log.i("showdetails", "imagePath"+element.file);

                if(element.getImagePath()!=null)
                    // when you change element.getimagepath to element.file.toString, you will be able to see the path
                    //the problem is when you open and close the app and try to show details of image
                    new getImageFrmDb_imgPath().execute(element.getImagePath());


            }

        }


        FloatingActionButton saveDetails = (FloatingActionButton) findViewById(R.id.saveDetails);
        FloatingActionButton uploadServer = (FloatingActionButton) findViewById(R.id.uploadServer);

        Button edit = (Button) findViewById(R.id.button);

        edit.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){

                title.setFocusableInTouchMode(true);
                description.setFocusableInTouchMode(true);
                title.setEnabled(true);
                description.setEnabled(true);
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

    /////////// for giving back the position
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent intent = new Intent(this, ShowImageActivity.class);
                intent.putExtra("position",position);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(element.getLatitude(), element.getLongitude());
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }

    private class getImageFrmDb_imgPath extends AsyncTask<String, Void, Wrapper>{
        @Override
        protected Wrapper doInBackground(String... s) {
            String imagePath=s[0];
            Image image= db.imageDao().findImageByPath(imagePath);
            Location location=db.imageDao().findLocationById(image.getLocationId());
            Wrapper w=new Wrapper(image,location);
            return w;
        }

        @Override
        protected void onPostExecute(Wrapper w) {

            latitude.setText(String.valueOf(w.getLocation().getLatitude()));
            longitude.setText(String.valueOf(w.getLocation().getLongitude()));

        }
    }



    private void initilaizeFields()
    {
        title.setText("Title");
        description.setText("Description");

    }
}
