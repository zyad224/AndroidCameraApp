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

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;


public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_READ_EXTERNAL_STORAGE = 2987;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 7829;
    private static final String TAG = "MainActivity";
    private static List<ImageElement> myPictureList = new ArrayList<>();
    private RecyclerView.Adapter  mAdapter;
    private RecyclerView mRecyclerView;
    private Activity activity;
    private static AppDatabase db;
    private static boolean databaseLoaded=false;
    //////////////////////////////
    private Bitmap bitmap;
    private ImageView imageView;

    private BroadcastReceiver broadcastReceiver; // getting tha data
    private double longitude;
    private double latitude;
    static boolean flag=true;
    private ImageElement element;

    /////----------------------
   // private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        activity= this;

        mRecyclerView = (RecyclerView) findViewById(R.id.grid_recycler_view);
        int numberOfColumns = 4;
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        mAdapter= new MyAdapter(myPictureList);
        mRecyclerView.setAdapter(mAdapter);

        ////////////

        //loadImageFromStorage("imageDir");

        if (myPictureList==null || myPictureList.size()==0) {
            initData();
        }

        // required by Android 6.0 +
        checkPermissions(getApplicationContext());

        initEasyImage();


        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
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
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        if (db==null)
            db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "images_database")
                    .addMigrations(AppDatabase.MIGRATION_1_2)
                    .build();



        // these functions will be used with the GPS
        // to insert and retrieves images from DB
        /*
        new InsertIntoDatabaseTask().execute();
        // search the database
        new SearchDatabaseTask().execute();*/
    }

    ///------///// Unique name for Images

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

    private void initData() {
        myPictureList.add(new ImageElement(R.drawable.joe1));
        myPictureList.add(new ImageElement(R.drawable.joe2));
        myPictureList.add(new ImageElement(R.drawable.joe3));
        myPictureList.add(new ImageElement(R.drawable.joe1));
        myPictureList.add(new ImageElement(R.drawable.joe2));
        myPictureList.add(new ImageElement(R.drawable.joe3));
        myPictureList.add(new ImageElement(R.drawable.joe1));
        myPictureList.add(new ImageElement(R.drawable.joe2));
        myPictureList.add(new ImageElement(R.drawable.joe3));
        myPictureList.add(new ImageElement(R.drawable.joe1));
        myPictureList.add(new ImageElement(R.drawable.joe2));
        myPictureList.add(new ImageElement(R.drawable.joe3));
        myPictureList.add(new ImageElement(R.drawable.joe1));
        myPictureList.add(new ImageElement(R.drawable.joe2));
        myPictureList.add(new ImageElement(R.drawable.joe3));
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
            imageElementList.add(element);
            // and call the save method with bitmap image
            // in database you said that we need the path of image
            // here giving the path of the image
           // imagePath=saveToInternalStorage(bitmap);
        }

        return imageElementList;
    }

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




    //////////////////////////////////////////// GPS

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
   //////////////////----------------------------- Saving Image
    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = wrapper.getDir("imageDir", Context.MODE_PRIVATE);
        String picName=getPictureName();
        // Create imageDir
        File mypath=new File(directory,picName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
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
        CharSequence text = directory.getAbsolutePath();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        // we need to convert uri when we want back it 
        return directory.getAbsolutePath();

    }


    // I added name because we also need a unique name
    private void loadImageFromStorage(String path,String name)
    {

        try {
            File f=new File(path, name);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            ImageView img=(ImageView)findViewById(R.id.image_item);
            img.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }


    }
}
