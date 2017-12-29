package com.example.zeyad.cameraapplication;

import com.example.zeyad.cameraapplication.database.Image;
import com.example.zeyad.cameraapplication.database.Location;

/**
 * Created by Zeyad on 12/29/2017.
 */

public class Wrapper {

   private  Image image;
   private Location location;


   public Wrapper (Image image, Location location){
       this.image=image;
       this.location=location;

   }

    Image getImage(){
       return this.image;
    }
    Location getLocation(){
        return this.location;
    }



}
