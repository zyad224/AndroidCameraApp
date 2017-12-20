package com.example.zeyad.cameraapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ShowDetails extends AppCompatActivity {

    private EditText title;
    private EditText description;
    private TextView date;
    private ImageElement element;
    private ImageView imageView;
    private  String reportDate;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_details);

        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        // Get the date today using Calendar object.
        Date today = Calendar.getInstance().getTime();
        // Using DateFormat format method we can create a string
// representation of a date with the defined format.
        reportDate = df.format(today);

        title= (EditText) findViewById(R.id.title);
        description= (EditText) findViewById(R.id.details);
        date = (TextView) findViewById(R.id.date);


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

                title.setText(element.getTitle());
                description.setText(element.getDescription());
                date.setText(element.getDate());
            }

        }


        FloatingActionButton saveDetails = (FloatingActionButton) findViewById(R.id.saveDetails);
        saveDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title.setFocusable(false);
                description.setFocusable(false);
                element.setTitle(title.getText().toString());
                element.setDescription(description.getText().toString());
                date.setText(reportDate);
                element.setDate(date.getText().toString());

            }
        });
    }


    private void initilaizeFields()
    {
        title.setText("Title");
        description.setText("Description");

    }
}
