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


public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_READ_EXTERNAL_STORAGE = 2987;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 7829;
    private static final String TAG = "MainActivity";
    private static List<ImageElement> myPictureList ;
    private RecyclerView.Adapter  mAdapter;
    private RecyclerView mRecyclerView;
    private Activity activity;
    private static AppDatabase db;
    private static boolean databaseLoaded=false;
    //////////////////////////////
    private Bitmap bitmap;
    private String picName;
    private ImageView imageView;

    public File directory;
    private static final int MY_ACCESS_FINE_LOCATION = 123;

    private BroadcastReceiver broadcastReceiver; // getting tha data
    private double longitude;
    private double latitude;
    static boolean flag=true;
    private ImageElement element;
    private ProgressBar progressBar;
    private int progressStatus =0;
    public static List<ImageElement> imagesToBeSendWhenOnline=new ArrayList<>();


   // private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        activity= this;


        ///////////////////////////////////
        // for count just one time to images
        myPictureList = new ArrayList<>();


        ////////////////////////////////////

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


        FloatingActionButton fabMap = (FloatingActionButton) findViewById(R.id.fab2);
        FloatingActionButton fabGallery = (FloatingActionButton) findViewById(R.id.fab_gallery);
        FloatingActionButton fabCamera = (FloatingActionButton) findViewById(R.id.fab_camera);

        fabGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EasyImage.openGallery(getActivity(), 0);
                flag=false;
            }
        });

        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EasyImage.openCamera(getActivity(), 0);
                flag=true;
                Intent i = new Intent(getApplicationContext(), GPS_Service.class);
                startService(i);

            }
        });
        fabMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        if (db==null)
            db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "images_database")
                    .addMigrations(AppDatabase.MIGRATION_2_4)
                    .build();



        new Thread(new loadImagesFromStorage()).start();

        if(isConnected()){
            Log.e("inside connected", "onCreate: " );
            if(!imagesToBeSendWhenOnline.isEmpty()){
                Log.e("offline images count", "onCreate:"+imagesToBeSendWhenOnline.size());

                progressBar.setVisibility(View.VISIBLE);
                new SendToServer().execute(imagesToBeSendWhenOnline);
                imagesToBeSendWhenOnline.clear();
                progressBar.setVisibility(View.GONE);

            }
        }




    }


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



    private class loadImagesFromStorage implements Runnable{

        @Override
        public void run() {
            try {

                progressBar.setVisibility(View.VISIBLE);
                myPictureList.clear();
                mAdapter.notifyDataSetChanged();
                File fileDir = new File(directory.toString());
                String[] SavedFiles=  fileDir.list();

                for(String img: SavedFiles){
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



    private String getPictureName(){
        // provide unique name for us
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timeStamp = sdf.format(new Date());
        return "MyImages"+timeStamp+".jpg";
    }
    ///----///////

    private void initEasyImage() {
        EasyImage.configuration(this)
                .setImagesFolderName("EasyImage sample")
                .setCopyTakenPhotosToPublicGalleryAppFolder(false)
                .setCopyPickedImagesToPublicGalleryAppFolder(false)
                .setAllowMultiplePickInGallery(true);
    }
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
        myPictureList.addAll(getImageElements(returnedPhotos));
        // we tell the adapter that the data is changed and hence the grid needs
        // refreshing
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(returnedPhotos.size() - 1);
    }

    /**
     * given a list of photos, it creates a list of myElements
     * @param returnedPhotos
     * @return
     */
    private List<ImageElement> getImageElements(List<File> returnedPhotos) {
        List<ImageElement> imageElementList= new ArrayList<>();

        for (File file: returnedPhotos){

            ImageElement element= new ImageElement(file);
            // take the image
            bitmap = BitmapFactory.decodeFile(element.file.getAbsolutePath());

            if(flag){
                element.setLatitude(latitude);
                element.setLongitude(longitude);
            }
            element.setImagePath(saveToInternalStorage(bitmap));
            new InsertIntoDatabaseTask().execute(element);
            new Thread(new loadImagesFromStorage()).start();
            Log.i("MainActivity", "imgpath: " + element.getImagePath());

            imageElementList.add(element);



        }
        flag=false;

        return imageElementList;
    }

    ///////// Taking Images from Database -----------------

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

    public static AppDatabase getDB(){return db;}
    public static List<ImageElement> getImageList(){return myPictureList;}

    private class AllImageTask extends AsyncTask<Void, Void, List<Image> >{
        @Override
        protected List<Image> doInBackground(Void... Voids) {

            //This Part will take the images from db and write in main page
            List<Image> imageList=new ArrayList<>();

            if(db.imageDao().imageCount()!=0) {
                imageList = db.imageDao().loadImages();
                for (Image img : imageList) {
                    File file = new File(img.getImagepath());
                    ImageElement imgFromStorage = new ImageElement(file);
                    myPictureList.add(imgFromStorage);
                }
            }

            Log.i("imgCount", "count: " + db.imageDao().imageCount());

            return imageList;
        }

        @Override
        protected void onPostExecute(List<Image> imageList) {

            //here you should update the grid
            if(!imageList.isEmpty()) {

                mAdapter.notifyDataSetChanged();

            }

            progressBar.setVisibility(View.GONE);


        }


    }
    private class InsertIntoDatabaseTask extends AsyncTask<ImageElement, Void, Void> {

        @Override
        protected Void doInBackground(ImageElement... img) {
            ///////////////////// GPS
             ImageElement e=img[0];
             Location location = new Location(e.getLatitude(), e.getLongitude(), 20.0);
             db.imageDao().insertLocation(location);
             Image image=new Image(getApplicationContext(),e.getTitle(),e.getDescription(),e.getImagePath(),location.getId());
             db.imageDao().insertImage(image);

            List<Image> imageList = db.imageDao().loadImages();

           // for(Image im:imageList)
           //     Log.i("MainActivity", "imgs: "+ im.getImagepath() );
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getBaseContext(), "Image Saved in DataBase", Toast.LENGTH_LONG).show();

        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver(){

                @Override
                public void onReceive(Context context, Intent intent) {

                    longitude =(double)intent.getExtras().get("Longitude");
                    latitude = (double)intent.getExtras().get("Latitude");
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));

    }
    protected void onDestroy(){
        super.onDestroy();
        if(broadcastReceiver !=null){
            unregisterReceiver(broadcastReceiver);
        }
    }
    private String saveToInternalStorage(Bitmap bitmapImage){

        picName=getPictureName();
        // Create imageDir
        File mypath=new File(directory,picName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            /*Context context = getApplicationContext();
            CharSequence text = "Hello toast!"+bitmapImage;
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();*/
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

       // Toast toast = Toast.makeText(context, text, duration);
        //toast.show();

        // we need to convert uri when we want back it 
        return mypath.getAbsolutePath();

    }

    private  class SendToServer extends AsyncTask<List<ImageElement>, Void, List<String> >{

        @Override
        protected List<String> doInBackground(List<ImageElement>... img) {


            String url="http://wesenseit-vm1.shef.ac.uk:8091/uploadImages";
            String serverResult="";
            List<String>serverResults=new ArrayList<>();
            MultipartRequest multipartRequest;
            List<ImageElement> jsonData=img[0];

            try{

                for(ImageElement e:jsonData) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("title", e.getTitle());
                    jsonObject.put("description", e.getDescription());
                    jsonObject.put("latitude", e.getLatitude());
                    jsonObject.put("longitude", e.getLongitude());
                    jsonObject.put("image path", e.file.getAbsolutePath());

                    multipartRequest = new MultipartRequest(getApplicationContext());
                    multipartRequest.addFile("image", e.file.getAbsolutePath(), jsonObject.toString());
                    serverResult = multipartRequest.execute(url);
                    serverResults.add(serverResult);
                }

            }catch(Exception e){
                e.printStackTrace();
            }

            return serverResults;
        }

        @Override
        protected void onPostExecute(List<String> serverResults) {
            Log.i("serverResult", "Result is:"+serverResults);
            Toast.makeText(getBaseContext(), "Images Have Been Sent to the Server!", Toast.LENGTH_LONG).show();
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
