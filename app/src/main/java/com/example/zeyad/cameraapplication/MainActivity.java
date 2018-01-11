package com.example.zeyad.cameraapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.arch.persistence.room.Room;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.example.zeyad.cameraapplication.database.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.base.Converter;

import org.json.JSONObject;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

/**
 *
 * MainActivity.java
 *
 * This activity is used to display all pictures in the main page of the application
 * and it includes layout to go essential functions like taking a picture from camera and
 * phone gallery and also showing all pictures including their details on google map .
 *
 *
 * @author Sakine Yalman, Zeyad Abdelwahab (syalman1@sheffield.ac.uk, zyabdelwahab1@sheffield.ac.uk)
 * @version 1 10 January 2018
 *
 */

public class MainActivity extends AppCompatActivity {

    // declarations
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 2987;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 7829;
    private static final String TAG = "MainActivity";
    private static List<ImageElement> myPictureList ;
    private RecyclerView.Adapter  mAdapter;
    private RecyclerView mRecyclerView;
    private Activity activity;
    private static AppDatabase db;
    private static boolean databaseLoaded=false;
    private Bitmap bitmap;
    private String picName;

    public File directory;
    private static final int MY_ACCESS_FINE_LOCATION = 123;

    private BroadcastReceiver broadcastReceiver; // getting tha data
    private double longitude;
    private double latitude;
    static boolean flag=true;
    private ProgressBar progressBar;
    private int progressStatus =0;


    /**
     *
     * In onCreate, the main methods are declared like showing pictures, connecting the server and dtabase
     * and going another activities
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity= this;

        Log.e(TAG, "onCreate:enter main " );

        // For list of pictures
        myPictureList = new ArrayList<>();

        // create a directory for saving pictures in it
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        directory = wrapper.getDir("imageDir", Context.MODE_PRIVATE);


        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        mRecyclerView = (RecyclerView) findViewById(R.id.grid_recycler_view);
        int numberOfColumns = 4;
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        mAdapter= new MyAdapter(myPictureList);
        mRecyclerView.setAdapter(mAdapter);


        if(!runtime_permission()){

        }

        // required by Android 6.0 +
        checkPermissions(getApplicationContext());

        initEasyImage();

        // start GPS Service for getting location
        Intent i = new Intent(getApplicationContext(), GPS_Service.class);
        startService(i);

        FloatingActionButton fabMap = (FloatingActionButton) findViewById(R.id.fab2);
        FloatingActionButton fabGallery = (FloatingActionButton) findViewById(R.id.fab_gallery);
        FloatingActionButton fabCamera = (FloatingActionButton) findViewById(R.id.fab_camera);

        // for open gallery and take pictures
        fabGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EasyImage.openGallery(getActivity(), 0);
                flag=false;
            }
        });

        // for open camera and take picture
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyImage.openCamera(getActivity(), 0);
                flag=true;
            }
        });

        // for showing pictures in google map
        fabMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        // control of database
        if (db==null)
            db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "images_database")
                .addMigrations( AppDatabase.MIGRATION_13_16)
                .build();


        // start the thread for load images in main page in application
        new Thread(new loadImagesFromStorage()).start();

        // check the network connection, if we have
        if(isConnected()){

                Log.e("inside connected", "onCreate: " );
                progressBar.setVisibility(View.VISIBLE);
                // execute the server
                new SendToServer().execute();
                progressBar.setVisibility(View.GONE);

        }

    }


    /**
     * Method that checks runtime permission
     * @return status of permission checking
     */
    private boolean runtime_permission(){
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission
                .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest
                .permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission
                    .ACCESS_COARSE_LOCATION},100);

            return true; // if we need permission checking
        }
        return false; // if we do not need permission checking
    }

    /**
     *
     * Method that checks permissions for camera
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch (requestCode) {case MY_ACCESS_FINE_LOCATION: {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager
                    .PERMISSION_GRANTED) {

                //startLocationUpdatesAux(getApplicationContext());
            } else {
                runtime_permission();
            }
            return;
        }
        // other 'case' lines to check for other
        // permissions this app might request

        }
    }


    /**
     *
     * Thread that provide load images from internal storage
     * to main page in application
     *
     */
    private class loadImagesFromStorage implements Runnable{

        @Override
        public void run() {
            try {

                // give message to user that images are loading
                progressBar.setVisibility(View.VISIBLE);
                myPictureList.clear();
                mAdapter.notifyDataSetChanged();
                // take the directory of internal storage
                File fileDir = new File(directory.toString());
                String[] SavedFiles=  fileDir.list();

                for(String img: SavedFiles){
                    // add all images into picture list
                    File file = new File(directory.toString()+"/"+img);
                    ImageElement imgFromStorage = new ImageElement(file);
                    myPictureList.add(imgFromStorage);
                }


                progressBar.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();

            }catch(Exception e){

                e.printStackTrace();
            }
        }
    }


    /**
     * Method that create unique name for every image
     * with using SimpleDateFormat
     *
     * @return Image name to use in internal storage (the path)
     */
    private String getPictureName(){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timeStamp = sdf.format(new Date());
        return "MyImages"+timeStamp+".jpg";
    }


    /**
     * Pick to image from gallery application folder
     *
     */
    private void initEasyImage() {
        EasyImage.configuration(this)
                .setImagesFolderName("EasyImage sample")
                .setCopyTakenPhotosToPublicGalleryAppFolder(false)
                .setCopyPickedImagesToPublicGalleryAppFolder(false)
                .setAllowMultiplePickInGallery(true);
    }


    /**
     * Method that checks permissions for storage and gallery
     * @param context
     */
    private void checkPermissions(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    android.support.v7.app.AlertDialog.Builder alertBuilder = new android.support.v7.app.AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
                    android.support.v7.app.AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
                }

            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    android.support.v7.app.AlertDialog.Builder alertBuilder = new android.support.v7.app.AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("Writing external storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
                        }
                    });
                    android.support.v7.app.AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
                e.printStackTrace();
            }

            @Override
            public void onImagesPicked(List<File> imageFiles, EasyImage.ImageSource source, int type) {
                onPhotosReturned(imageFiles);
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {

            }
        });
    }

    /**
     * add the selected images to the grid
     * @param returnedPhotos
     */
    private void onPhotosReturned(List<File> returnedPhotos) {
        getImageElements(returnedPhotos);
        // the data is changed and hence the grid needs refreshing
        new Thread(new loadImagesFromStorage()).start();

       mAdapter.notifyDataSetChanged();
    }


    /**
     * Method that gets a list of files from camera or gallery functions
     * and create an element object after that calling saveToInternalStorage method
     * to create a place for this image and calling InsertIntoDatabaseTask to insert
     * into database.
     *
     * @param returnedPhotos is a list of files
     */
    private void getImageElements(List<File> returnedPhotos) {

        // declare a list for saving exif information
        List<String> exefInfo=new ArrayList<>();
        float []latLong = new float[2];

        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        // Get the date today using Calendar object.
        Date today = Calendar.getInstance().getTime();
        // Using DateFormat format method we can create a string
        // representation of a date with the defined format.
        String reportDate = df.format(today);

        int counter=0;
        String pictureName;
        for (File file: returnedPhotos){
            ImageElement element= new ImageElement(file);
            // take the image full path and convert bitmap
            bitmap = BitmapFactory.decodeFile(element.file.getAbsolutePath());
            try{

                // to take latitude and longitude values from ExifInterface
                ExifInterface exif = new ExifInterface(element.file.getAbsolutePath());
                latLong=new float[2];
                exefInfo=ShowExifInfo(exif);
                exif.getLatLong(latLong);

            }catch(IOException e){
                e.printStackTrace();
                Toast.makeText(this, "Error!", Toast.LENGTH_LONG).show();

            }

            // if the image is taken from camera
           if(flag){
               Log.e("long:", "getImageElements: "+longitude );
               Log.e("latit:", "getImageElements: "+latitude );

               // fill the element object with values
               element.setLatitude(latitude);
               element.setLongitude(longitude);
               element.setDate("DateTime : "+reportDate);
               element.setImageLength((exefInfo.get(1)));
               element.setImageWidth((exefInfo.get(2)));
               Log.e("in 3", "getImageElements: "+ exefInfo.get(0));
               Log.e("in 3", "getImageElements: "+(exefInfo.get(1)) );
               Log.e("in 3", "getImageElements: "+(exefInfo.get(2)) );

               pictureName=getPictureName();
            }
           // if the image is taken from gallery
            else{


               Log.e("in 2", "getImageElements: " );
               element.setDate(exefInfo.get(0));
               element.setLatitude(latLong[0]);
               element.setLongitude(latLong[1]);
               element.setImageLength((exefInfo.get(1)));
               element.setImageWidth((exefInfo.get(2)));

               Log.e("in 2", "getImageElements: " +exefInfo.get(0));
               Log.e("in 2", "getImageElements: " +(exefInfo.get(1)));
               Log.e("in 2", "getImageElements: " +(exefInfo.get(2)));
               Log.e("in 2", "getImageElements: " +(latLong[0]));
               Log.e("in 2", "getImageElements: " +(latLong[1]));

               counter++;
               // add the counter in image name
               // because we are taking more than one images at the same time.
               pictureName=counter+getPictureName();


            }

           // to save image in internal storage
           element.setImagePath(saveToInternalStorage(bitmap,pictureName));

           // to insert the image details into database
           new InsertIntoDatabaseTask().execute(element);

            Log.i("MainActivity", "imgpath: " + element.getImagePath());

        }
        flag=false;
    }

    /**
     * Method that gets exif file information and
     * save in an array list
     *
     * @param exif to get information from exif file
     * @return the exif information list
     */
    private List<String> ShowExifInfo(ExifInterface exif)
    {
        List<String>exefInfo=new ArrayList<>();
        String myAttribute="Exif information ---\n";
        exefInfo.add( getTagString(ExifInterface.TAG_DATETIME, exif));
        exefInfo.add( getTagString(ExifInterface.TAG_IMAGE_LENGTH, exif));
        exefInfo.add( getTagString(ExifInterface.TAG_IMAGE_WIDTH, exif));

        return exefInfo;
    }


    /**
     *
     * Method that creates the tag string for exif file
     *
     * @param tag
     * @param exif
     *
     * @return a tag string
     */
    private String getTagString(String tag, ExifInterface exif)
    {
        return(tag + " : " + exif.getAttribute(tag) + "\n");
    }


    /**
     *
     * Method that is a getter method for activity
     *
     * @return activiy
     */
    public Activity getActivity() {
        return activity;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Method that is a getter method to get database in activity
     * @return database
     */
    public static AppDatabase getDB(){return db;}

    /**
     * Method that is a getter method to get picture list
     * @return picture list
     */
    public static List<ImageElement> getImageList(){return myPictureList;}

    /**
     *
     * The class, which extends Async task, inserts the image
     * into database on background
     *
     */
    private class InsertIntoDatabaseTask extends AsyncTask<ImageElement, Void, Void> {

        @Override
        protected Void doInBackground(ImageElement... img) {

             ImageElement e=img[0];
             // create a location class object
             Location location = new Location(e.getLatitude(), e.getLongitude(), 20.0);
             // insert the image location into database
             db.imageDao().insertLocation(location);
             // get the values from ImageElement object and create a image class object
             Image image=new Image(getApplicationContext(),e.getTitle(),
                     e.getDescription(),e.getDate(),e.getImagePath(),e.getImageLength(),e.getImageWidth(),"false",location.getId());
             // insert the image information into database with using location id (from location database)
             db.imageDao().insertImage(image);

            List<Image> imageList = db.imageDao().loadImages();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getBaseContext(), "Image Saved in DataBase", Toast.LENGTH_LONG).show();

        }
    }


    /**
     *
     * In onResume, broadCastReceiver is controlled and
     * is getting latitude and longitude values with broadCastReceiver
     * when the location is updated.
     *
     */
    @Override
    protected void onResume(){
        super.onResume();
        Log.e("f", "onResume: entered after close" );
        if(broadcastReceiver == null){
            // create a BroadcastReceiver object
            broadcastReceiver = new BroadcastReceiver(){

                @Override
                public void onReceive(Context context, Intent intent) {

                    longitude =(double)intent.getExtras().get("Longitude");
                    latitude = (double)intent.getExtras().get("Latitude");
                    Log.e("long:", "broadcast: "+longitude );
                    Log.e("latit:", "broadcast: "+latitude );
                }
            };
        }
        // update it
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));

    }


    protected void onDestroy(){
        super.onDestroy();
        if(broadcastReceiver !=null){
            unregisterReceiver(broadcastReceiver);
        }
    }

    /**
     *
     * Method that creates a place for the image(with using image name)
     * in the internal storage and saves them in it.
     *
     * @param bitmapImage to compress bitmap
     * @param pictureName to add the name
     * @return resulting the full path of the image
     */
    private String saveToInternalStorage(Bitmap bitmapImage, String pictureName){

        // take the unique name
        picName=pictureName;

        // Create imageDir
        File mypath=new File(directory,picName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Context context = getApplicationContext();
        CharSequence text = mypath.getAbsolutePath();
        int duration = Toast.LENGTH_SHORT;


        // return the full path of the image
        return mypath.getAbsolutePath();

    }


    /**
     * This Async Task recieves a list of images sent to the server from the SendToServer Async Task
     * This task updates the offline parameter in the image table to false indicating
     * that these images are already sent to the server
     *
     *
     */
    private class UpdateImageUploadingMode extends AsyncTask<List<Image>, Void, Void> {

        @Override
        /**
         * This method recieves a List of images that is alreadey
         * sent to the server and update the change the offline parameter
         * for each image in the database to false to indicate that this image
         * is already sent to the server
         *
         * @param List<Image>  Images to update in the database
         */
        protected Void doInBackground(List<Image>... img) {

            List<Image> e= img[0];
            for(Image ee:e)
                 db.imageDao().updateImageOffline(ee.getImagepath(),"false");
            return null;
        }

        @Override
        /**
         * This method is called after finishing the background
         * activity. it shows a text to the user to tell him that
         * the images are updated in the database
         */
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getBaseContext(), "Image mode is updated again", Toast.LENGTH_LONG).show();

        }
    }


    /**
     * This Async Task send to the server images that are failed
     * to be sent due to network unavailability.
     * It uses the MultipartRequest class to create a okHttpClient to
     * send requests and recieve responses to/from the server
     *
     *
     */
    private  class SendToServer extends AsyncTask<Void, Void, Boolean>{

        @Override
        /**
         * This method is responsible to get all the images that
         * failed to be sent to the server and load them in List<Image> offlineImages.
         *
         * After that it creates a JSON Object for each image and saves the title,description,latitude,
         * longitude,date,image lenght, image width, and image path in that JSOB Object.
         *
         * After that it sends the JSON Object + the image path to the server using
         * the multipartRequest.addFile() method
         *
         * @return flg which indicates that images have been sent to the server
         */
        protected Boolean doInBackground(Void... voids) {


            String url="http://wesenseit-vm1.shef.ac.uk:8091/uploadImages";
            String serverResult="";
            List<String>serverResults=new ArrayList<>();
            MultipartRequest multipartRequest;
            List<Image> offlineImages= db.imageDao().loadOfflineImages("true");
            Boolean flg=false;

            if(!offlineImages.isEmpty()) {
                try {
                    Log.e("in", "doInBackground: " );
                    flg=true;
                    for (Image e : offlineImages) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("title", e.getTitle());
                        jsonObject.put("description", e.getDescription());
                        jsonObject.put("latitude", db.imageDao().findLocationById(e.getLocationId()).getLatitude());
                        jsonObject.put("longitude", db.imageDao().findLocationById(e.getLocationId()).getLongitude());
                        jsonObject.put("date", e.getDate());
                        jsonObject.put("image length", e.getImageLength());
                        jsonObject.put("image width", e.getImageWidth());
                        jsonObject.put("image path", e.imagepath);

                        multipartRequest = new MultipartRequest(getApplicationContext());
                        multipartRequest.addFile("image", e.imagepath, jsonObject.toString());
                        serverResult = multipartRequest.execute(url);
                        serverResults.add(serverResult);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                new UpdateImageUploadingMode().execute(offlineImages);
            }


            return flg;
        }

        @Override
        /**
         * This method receives  a boolean from the background function
         * if this boolean is true then this method will show to the user
         * a text which indicates that images have been sent to the server
         *
         */
        protected void onPostExecute(Boolean flg) {

            if(flg)
              Toast.makeText(getBaseContext(), "Images Have Been Sent to the Server!", Toast.LENGTH_LONG).show();
        }


    }

    /**
     * In isConnected method, network connection is controlled
     * and got the information from NetworkInfo
     *
     * @return the resulting the status of network service
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
