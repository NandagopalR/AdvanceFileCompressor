package com.nanda.filecompressor.app;

import android.app.Application;

import com.nanda.filecompressor.helper.FileCompress;

public class AppController extends Application {

    private static AppController appController;
    private FileCompress fileCompress;

    @Override
    public void onCreate() {
        super.onCreate();
        appController = this;
        fileCompress = new FileCompress();
    }

    public static AppController getInstance() {
        return appController;
    }

    public FileCompress getFileCompress() {
        return fileCompress;
    }
}
