package com.example.zeyad.cameraapplication;

import android.annotation.SuppressLint;
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
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
    private ImageButton edit;
    private ImageButton delete;
    public  final static String SER_KEY = "serial";
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_details);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(onMapReadyCallback);
        @SuppressLint("ResourceType") View zoomControls = mapFragment.getView().findViewById(0x1);

        if (zoomControls != null && zoomControls.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            // ZoomControl is inside of RelativeLayout
            RelativeLayout.LayoutParams params_zoom = (RelativeLayout.LayoutParams) zoomControls.getLayoutParams();

            // Align it to - parent top|left
            params_zoom.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params_zoom.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            // Update margins, set to 10dp
            final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                    getResources().getDisplayMetrics());
            params_zoom.setMargins(margin, margin, margin, margin);

        }

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

                if(element.file.getAbsolutePath()!=null)
                    new getImageFrmDb_imgPath().execute(element.file.getAbsolutePath());


            }

        }


        FloatingActionButton saveDetails = (FloatingActionButton) findViewById(R.id.saveDetails);
        FloatingActionButton uploadServer = (FloatingActionButton) findViewById(R.id.uploadServer);


        edit=(ImageButton) findViewById(R.id.edit);
        delete = (ImageButton) findViewById(R.id.delete);

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
                        new UpdateImageDetails().execute(element);
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
            //new GetAllImage().execute();
        }
    };

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
            title.setText(w.getImage().getTitle());
            description.setText(w.getImage().getDescription());
            LatLng position = new LatLng(w.getLocation().getLatitude(),w.getLocation().getLongitude());
            mMap.addMarker(new MarkerOptions().position(position).title(w.getImage().getTitle()));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
            mMap.getUiSettings().setZoomControlsEnabled(true);
            CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);
            mMap.animateCamera(zoom);

        }
    }

    private class UpdateImageDetails extends AsyncTask<ImageElement, Void, Void>{
        @Override
        protected Void doInBackground(ImageElement... img) {

            ImageElement e=img[0];
            db.imageDao().updateImageDetails(e.file.getAbsolutePath().toString(),e.getTitle(),e.getDescription());

            return null;
        }
    }


    private void initilaizeFields()
    {
        title.setText("Title");
        description.setText("Description");

    }
}
