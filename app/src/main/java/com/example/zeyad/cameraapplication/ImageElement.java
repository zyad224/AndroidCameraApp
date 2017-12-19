package com.example.zeyad.cameraapplication;

import java.io.File;

/**
 * Created by Zeyad on 12/19/2017.
 */

public class ImageElement {

    int image=-1;
    File file=null;


    //drawable
    public ImageElement(int image) {
        this.image = image;

    }

    //file
    public ImageElement(File fileX) {
         file= fileX;
    }
}
