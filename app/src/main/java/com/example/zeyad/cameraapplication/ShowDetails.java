package com.example.zeyad.cameraapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class ShowDetails extends AppCompatActivity {

    private RecyclerView.Adapter  mAdapter;
    private EditText title;
    private EditText description;
    private TextView date;
    private TextView length;
    private TextView width;
    private ImageElement element;
    private ImageView imageView;
    private  String reportDate;
    private static GoogleMap mMap;
    private static AppDatabase db;
    private static List<ImageElement> picList ;
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

        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);

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
        length =(TextView) findViewById(R.id.length);
        width = (TextView) findViewById(R.id.width);


        Bundle b = getIntent().getExtras();
        position=-1;
        //title.setFocusableInTouchMode(true);
        title.setEnabled(false);
        //title.setFocusable(false);
        description.setEnabled(false);
        //description.setFocusableInTouchMode(true);

        db=MainActivity.getDB();
        picList=MainActivity.getImageList();
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
                        /// ikisinden biri boşsa save yapma
                        String titleT=title.getText().toString();
                        String descriptionT =description.getText().toString();
                        if(titleT.isEmpty() && descriptionT.isEmpty()){
                            Context context = getApplicationContext();
                            CharSequence text = "Title and description cannot be empty!";
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }
                        else{
                        title.setFocusable(false);
                        description.setFocusable(false);
                        element.setTitle(title.getText().toString());
                        element.setDescription(description.getText().toString());
                        element.setDate(date.getText().toString());
                        new UpdateImageDetails().execute(element);



                        finish();
                        }

                    }
                });

            }
        });
        delete.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {

                dlgAlert.setMessage("Do you want to delete the image");
                dlgAlert.setTitle("Delete Image");
                dlgAlert.setPositiveButton("Yes", null);
                dlgAlert.setNegativeButton("No", null);
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();

                dlgAlert.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //dismiss the dialog
                                Context context = getApplicationContext();
                                new DeleteIntoDatabaseTask().execute(element);
                                File file =new File(element.file.getAbsolutePath());
                                /*for(ImageElement e : picList ){
                                    if(e.getImagePath().equals(element.file.getAbsolutePath()))
                                        picList.remove(e);
                                }
                                mAdapter.notifyDataSetChanged();*/
                                Toast.makeText(getBaseContext(), file.toString(), Toast.LENGTH_LONG).show();
                                file.delete();

                                Intent intent = new Intent(ShowDetails.this, MainActivity.class);
                                MainActivity m = new MainActivity();
                                intent.putExtra(EXTRA_MESSAGE, file);
                                startActivity(intent);

                            }
                        });
                dlgAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Toast.makeText(getBaseContext(), "Image is not deleted in DataBase", Toast.LENGTH_LONG).show();
                    }
                });

                AlertDialog alert = dlgAlert.create();
                alert.show();

            }
        });


        uploadServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String titleT=title.getText().toString();
                String descriptionT =description.getText().toString();

                if(isConnected()) {
                    if (titleT.isEmpty() && descriptionT.isEmpty()) {
                        Context context = getApplicationContext();
                        CharSequence text = "Title and description cannot be empty!";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                    else {
                        new SendToServer().execute(element);
                        finish();
                    }
                }
                else {

                    if (titleT.isEmpty() && descriptionT.isEmpty()) {
                        Context context = getApplicationContext();
                        CharSequence text = "Title and description cannot be empty!";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }

                    else {
                        Log.e("adding image", "onClick: ");
                        Context context = getApplicationContext();
                        CharSequence text = "No Internet Available, Image will be sent when internet connection is available";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                        MainActivity.imagesToBeSendWhenOnline.add(element);
                    }
                }
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

            length.setText(String.valueOf(w.getImage().getImageLength()));
            width.setText(String.valueOf(w.getImage().getImageWidth()));
            title.setText(w.getImage().getTitle());
            description.setText(w.getImage().getDescription());
            date.setText(w.getImage().getDate());
            LatLng position = new LatLng(w.getLocation().getLatitude(),w.getLocation().getLongitude());
            mMap.addMarker(new MarkerOptions().position(position).title(w.getImage().getTitle()).snippet(w.getImage().getDescription()));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
            mMap.getUiSettings().setZoomControlsEnabled(true);
            CameraUpdate zoom=CameraUpdateFactory.zoomTo(0);
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

    private class DeleteIntoDatabaseTask extends AsyncTask<ImageElement, Void, Void> {

        @Override
        protected Void doInBackground(ImageElement... img) {
            ///////////////////// GPS
            ImageElement e=img[0];
            Image image =db.imageDao().findImageByPath(e.file.getAbsolutePath());
            db.imageDao().deleteImage(image);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getBaseContext(), "Image deleted in DataBase", Toast.LENGTH_LONG).show();

        }
    }

    private void initilaizeFields()
    {
        title.setText("Title");
        description.setText("Description");

    }

    private  class SendToServer extends AsyncTask<ImageElement, Void, String>{

        @Override
        protected String doInBackground(ImageElement... img) {


            String url="http://wesenseit-vm1.shef.ac.uk:8091/uploadImages";
            String serverResult="";
            MultipartRequest multipartRequest;
            ImageElement jsonData=img[0];

            try{

                JSONObject jsonObject=new JSONObject();
                jsonObject.put("title",jsonData.getTitle());
                jsonObject.put("description",jsonData.getDescription());
                jsonObject.put("latitude",jsonData.getLatitude());
                jsonObject.put("longitude",jsonData.getLongitude());
                jsonObject.put("date",jsonData.getDate());
                jsonObject.put("image length",jsonData.getImageLength());
                jsonObject.put("image width",jsonData.getImageWidth());
                jsonObject.put("image path",jsonData.file.getAbsolutePath());

                multipartRequest = new MultipartRequest(getApplicationContext());
                multipartRequest.addFile("image",jsonData.file.getAbsolutePath(),jsonObject.toString());
                serverResult=multipartRequest.execute(url);

            }catch(Exception e){
                e.printStackTrace();
            }

            return serverResult;
        }

        @Override
        protected void onPostExecute(String serverResult) {
            Log.i("serverResult", "Result is:"+serverResult);
            Toast.makeText(getBaseContext(), "Image Sent to server!", Toast.LENGTH_LONG).show();
        }


    }
    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }


}
