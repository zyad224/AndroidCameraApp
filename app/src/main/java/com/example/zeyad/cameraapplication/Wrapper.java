package com.example.zeyad.cameraapplication;

import com.example.zeyad.cameraapplication.database.Image;
import com.example.zeyad.cameraapplication.database.Location;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Zeyad on 12/29/2017.
 */

public class Wrapper {

   private  Image image;
   private Location location;
   private String imageTitle;
   private LatLng locOnMap;


   public Wrapper (Image image, Location location){
       this.image=image;
       this.location=location;

   }

    public Wrapper (String imageTitle, LatLng locOnMap){
        this.imageTitle=imageTitle;
        this.locOnMap=locOnMap;

    }

    Image getImage(){
       return this.image;
    }
    Location getLocation(){
        return this.location;
    }
    LatLng getPositionOnMap(){return this.locOnMap;}
    String getImageTitle(){return this.imageTitle;}



}
