package com.example.zeyad.cameraapplication;

import com.example.zeyad.cameraapplication.database.Image;
import com.example.zeyad.cameraapplication.database.Location;
import com.google.android.gms.maps.model.LatLng;


/**
 *
 * The class provides to use image class and location class
 * together in an object
 *
 */
public class Wrapper {

    // declarations
   private  Image image;
   private Location location;
   private String imageTitle;
   private LatLng locOnMap;


   public Wrapper (Image image, Location location){
       this.image=image;
       this.location=location;

   }

    public Wrapper (Image image, LatLng locOnMap){
        this.image=image;
        this.locOnMap=locOnMap;

    }

    // getters and setters
    Image getImage(){
       return this.image;
    }
    Location getLocation(){
        return this.location;
    }
    LatLng getPositionOnMap(){return this.locOnMap;}
    String getImageTitle(){return this.imageTitle;}



}
