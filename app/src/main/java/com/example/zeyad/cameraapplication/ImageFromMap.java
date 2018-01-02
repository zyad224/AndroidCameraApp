package com.example.zeyad.cameraapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class ImageFromMap extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_from_map);

        imageView = (ImageView) findViewById(R.id.imageFromMap);
        Bundle b = getIntent().getExtras();
        String imagePath="";

        if(b!=null) {
            imagePath = b.getString("image");
            Bitmap myBitmap = BitmapFactory.decodeFile(imagePath);
            imageView.setImageBitmap(myBitmap);
        }


    }
}
