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
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
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

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

/**
 *
 * The class that diplayes the details of an image
 *
 * In this class, firstly, image position is taken from other activity(ShowImageActivity)
 * and we get the image path. After that with using image path we are getting details from database
 * and filling the spaces and also adding it on small map.
 *
 * This class also includes some features like editting, deleting the image and sending it to the server.
 *
 */
public class ShowDetails extends AppCompatActivity {

    private EditText title;
    private EditText description;
    private TextView date;
    private TextView length;
    private TextView width;
    private ImageElement element;
    private ImageView imageView;
    private Image path;
    private ImageButton edit;
    private ImageButton delete;
    private  String reportDate;
    private static GoogleMap mMap;
    private static AppDatabase db;
    private static List<ImageElement> picList ;
    private int position;

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

        title.setFocusable(false);
        description.setFocusable(false);

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
                    new getImageFromDb_imgPath().execute(element.file.getAbsolutePath());


                Log.e("path", "onCreate: "+element.file.getAbsolutePath() );
            }

        }


        FloatingActionButton saveDetails = (FloatingActionButton) findViewById(R.id.saveDetails);
        FloatingActionButton uploadServer = (FloatingActionButton) findViewById(R.id.uploadServer);


        edit=(ImageButton) findViewById(R.id.edit);
        delete = (ImageButton) findViewById(R.id.delete);

        edit.setOnClickListener(new View.OnClickListener(){
            /**
             * The method that edit the image details. User can edit title or
             * description alone but cannot save if both of them empty.
             *
             * @param view
             */
            public void onClick(View view){

                title.setFocusableInTouchMode(true);
                description.setFocusableInTouchMode(true);
                title.setEnabled(true);
                description.setEnabled(true);
                saveDetails.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String titleT=title.getText().toString();
                        String descriptionT =description.getText().toString();

                        /// If both of them(title and description are empty, do not save it)
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

        // when user want to delete the image, there is a message box is coming and
        // asking again.
        delete.setOnClickListener(new View.OnClickListener(){
            /**
             * The method that deletes the image with its path.
             * when we delete the image, we are also delete it from
             * internal storage and database
             *
             * @param view
             */
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
                                new DeleteFromDatabaseTask().execute(element);
                                File file =new File(element.file.getAbsolutePath());

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

        // uploading to the server

        ////////////////////
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

                        new UpdateImageUploadingMode().execute(element);

                    }
                }
            }
        });
    }



    /**
     * This Async Task recieves an image that failed to be sent to the server due to
     * internet connectivity.
     *
     * This task updates the offline parameter in the image table to true indicating
     * that this image should be sent again to the server after the connection comes back
     *
     *
     *
     *
     */

    private class UpdateImageUploadingMode extends AsyncTask<ImageElement, Void, Void> {

        @Override
        /**
         * This method receives an ImageElement and update the offline parameter
         * of this image to true to indicate that this image should be sent to the
         * server after the internt connection comes back
         */
        protected Void doInBackground(ImageElement... img) {


            ImageElement e= img[0];
            db.imageDao().updateImageOffline(element.file.getAbsolutePath(),"true");
            return null;
        }

        @Override
        /**
         * This method show to a user a text to indicate that the image offline status
         * is updated in the database
         */
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getBaseContext(), "Image mode is update", Toast.LENGTH_LONG).show();

        }
    }


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
        /**
         *
         * Create a map
         *
         * @param googleMap
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
        }
    };

    /**
     *
     * The class that gets the image details from database in background
     * with using image path and the spaces are filled on execute time.
     *
     * The image location is added on google map on execute time.
     *
     */
    private class getImageFromDb_imgPath extends AsyncTask<String, Void, Wrapper>{
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

            List<String> tokens = new ArrayList<>();
            StringTokenizer tokenizer = new StringTokenizer(w.getImage().getImagepath().toString(),"/");
            while (tokenizer.hasMoreElements()){
                tokens.add(tokenizer.nextToken());
            }

            length.setText(String.valueOf(w.getImage().getImageLength()));
            width.setText(String.valueOf(w.getImage().getImageWidth()));
            if(w.getImage().getTitle()==null) {
                title.setText(tokens.get(tokens.size() - 1));
            }
            else{
                title.setText(w.getImage().getTitle());
            }
            description.setText(w.getImage().getDescription());
            date.setText(w.getImage().getDate());
            LatLng position = new LatLng(w.getLocation().getLatitude(),w.getLocation().getLongitude());
            mMap.addMarker(new MarkerOptions().position(position).title("LatLng:"+w.getLocation().getLatitude()+","+w.getLocation().getLongitude()));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
            mMap.getUiSettings().setZoomControlsEnabled(true);
            CameraUpdate zoom=CameraUpdateFactory.zoomTo(0);
            mMap.animateCamera(zoom);
        }
    }


    /**
     *
     * The class that updates image details in database in background when the iamge is editted
     *
     */
    private class UpdateImageDetails extends AsyncTask<ImageElement, Void, Void>{
        @Override
        protected Void doInBackground(ImageElement... img) {

            ImageElement e=img[0];
            db.imageDao().updateImageDetails(e.file.getAbsolutePath().toString(),e.getTitle(),e.getDescription());

            return null;
        }
    }

    /**
     * The class that deletes the image in database in background when user delete the image
     *
     */
    private class DeleteFromDatabaseTask extends AsyncTask<ImageElement, Void, Void> {

        @Override
        protected Void doInBackground(ImageElement... img) {

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


    /**
     * This Async Task send to the server images that the user
     * want to send to the server
     *
     * It uses the MultipartRequest class to create a okHttpClient to
     * send requests and recieve responses to/from the server
     *
     *
     */
    private  class SendToServer extends AsyncTask<ImageElement, Void, String>{

        @Override

        /**
         * This method receives an ImageElement and create
         * a JSON object for the ImageElement in order to send
         * the JSON Object to the server.
         *
         * After that it sends the JSON Object + the image path to the server using
         * the multipartRequest.addFile() method
         *
         * @param ImageElement  image to be sent to the server
         * @return serverResult the result from the server
         *
         */
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
        /**
         * This method recieves the result from background function
         * and show to the user a text to indicate that the image has been
         * sent to the server
         */
        protected void onPostExecute(String serverResult) {
            Log.i("serverResult", "Result is:"+serverResult);
            Toast.makeText(getBaseContext(), "Image Sent to server!", Toast.LENGTH_LONG).show();
        }


    }


    /**
     *
     * Method that controls internet connection and network informations
     *
     * @return the status of internet
     */
    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }


}
