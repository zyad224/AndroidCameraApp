package com.example.zeyad.cameraapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class ShowImageActivity extends AppCompatActivity {

    int position;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        //getSupportActionBar().setTitle();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();
        position=-1;
        if(b != null) {
            // this is the image position in the itemList
            position = b.getInt("position");
            if (position!=-1){
                ImageView imageView = (ImageView) findViewById(R.id.image);
                ImageElement element= MyAdapter.getItems().get(position);
                if (element.image!=-1) {
                    imageView.setImageResource(element.image);
                } else if (element.file!=null) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(element.file.getAbsolutePath());
                    imageView.setImageBitmap(myBitmap);
                }
                Button btn= (Button) findViewById(R.id.button);
                btn.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View view){
                        Intent intent  = new Intent(getApplicationContext(),ShowDetails.class);
                        intent.putExtra("position",position);
                        startActivity(intent);

                    }
                });
            }

        }

    }
}
