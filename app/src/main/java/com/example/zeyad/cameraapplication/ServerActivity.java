package com.example.zeyad.cameraapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

//import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.example.zeyad.cameraapplication.ShowDetails.SER_KEY;

public class ServerActivity extends AppCompatActivity {

    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //getSupportActionBar().setTitle();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        TextView upload= (TextView) findViewById(R.id.upload);

        ImageElement img = (ImageElement)getIntent().getSerializableExtra(SER_KEY);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((isConnected())&&(img!=null))
                   new SendToServer().execute(img);
                else{
                    Log.i("Error", "error in server activity");

                }
            }

        });

        if(isConnected()){
            upload.setBackgroundColor(Color.GREEN);
            upload.setText("conncted to the internet");
        }
        else{
            upload.setBackgroundColor(Color.RED);
            upload.setText("Not connected to internet");
        }


    }

    //// for giving back to the position
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent intent = new Intent(this, ShowDetails.class);
                intent.putExtra("position", position);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class SendToServer extends AsyncTask<ImageElement, Void, String>{

        @Override
        protected String doInBackground(ImageElement... img) {

            InputStream in;
            String serverResult="";
            HttpClient httpclient;
            HttpPost httpPost;
            HttpResponse httpResponse;
            String url="http://wesenseit-vm1.shef.ac.uk:8091/uploadImages";
            ImageElement jsonData=img[0];

            try{
                httpclient = new DefaultHttpClient();
                httpPost = new HttpPost(url);
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("title",jsonData.getTitle());
                jsonObject.put("description",jsonData.getDescription());
                jsonObject.put("latitude",jsonData.getLatitude());
                jsonObject.put("longitude",jsonData.getLongitude());
                jsonObject.put("image path",jsonData.getImagePath());

                StringEntity s=new StringEntity(jsonObject.toString());
                httpPost.setEntity(s);
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                httpResponse=httpclient.execute(httpPost);
                in = httpResponse.getEntity().getContent();

                if(in==null){
                    serverResult="recieve nothing from server";
                }
                else{

                    serverResult = inputstreamToString(in);

                }

            }catch(Exception e){
               e.printStackTrace();
            }

            return serverResult;
        }

        @Override
        protected void onPostExecute(String serverResult) {
            Log.i("serverResult", "serverResuly:"+serverResult);
            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
        }

        protected String inputstreamToString(InputStream in) throws IOException {
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
            return total.toString();
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
