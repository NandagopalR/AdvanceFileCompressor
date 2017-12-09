package com.nanda.filecompressor.helper;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.List;

import rx.Observable;

import static android.content.ContentValues.TAG;

public class FileCompress {

    private Context context;
    private String DEFAULT_DISK_CACHE_DIR = "app_disk_cache";
    private File originalFile;
    private List<File> originalFileList;

    public Observable<File> compress(Context context, File file) {
        this.context = context;
        this.originalFile = file;
        return asObservable(originalFile);
    }

    public Observable<List<File>> compress(Context context, List<File> fileList) {
        this.context = context;
        this.originalFileList = fileList;
        return asListObservable(originalFileList);
    }

    public Observable<File> asObservable(File originalFile) {
        FileCompressor compresser = new FileCompressor();
        return compresser.singleAction(originalFile);
    }

    public Observable<List<File>> asListObservable(List<File> originalFileList) {
        FileCompressor compresser = new FileCompressor();
        return compresser.multipleAction(originalFileList);
    }

    private File getPhotoCacheDir(Context context) {
        return getPhotoCacheDir(context, DEFAULT_DISK_CACHE_DIR);
    }

    private static File getPhotoCacheDir(Context context, String cacheName) {
        File cacheDir = context.getCacheDir();
        if (cacheDir != null) {
            File result = new File(cacheDir, cacheName);
            if (!result.mkdirs() && (!result.exists() || !result.isDirectory())) {
                // File wasn't able to create a directory, or the result exists but not a directory
                return null;
            }
            return result;
        }
        if (Log.isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG, "default disk cache dir is null");
        }
        return null;
    }

}
