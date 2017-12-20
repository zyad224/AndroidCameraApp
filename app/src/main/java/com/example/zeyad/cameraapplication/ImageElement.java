package com.example.zeyad.cameraapplication;

import java.io.File;

/**
 * Created by Zeyad on 12/19/2017.
 */

public class ImageElement {

    int image=-1;
    File file=null;
    private String title;
    private String description;
    private String date;


    //drawable
    public ImageElement(int image) {
        this.image = image;

    }

    //file
    public ImageElement(File fileX) {
         file= fileX;
    }

    public String getTitle(){
        return title;
    }
    public String getDescription(){
        return description;
    }
    public String getDate(){
        return date;
    }

    public void setTitle(String title){
        this.title=title;
    }

    public void setDescription(String description){
        this.description=description;
    }
    public void setDate(String date){
        this.date=date;
    }
}
