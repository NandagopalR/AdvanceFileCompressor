package com.nanda.filecompressor.helper;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;

import static android.content.ContentValues.TAG;

public class FileCompress {

    private static String DEFAULT_DISK_CACHE_DIR = "app_disk_cache";

    private FileCompressBuilder builder;

    private File mFile;

    private List<File> mFileList;

    public FileCompress(File file) {
        builder = new FileCompressBuilder(file);
    }

    public static FileCompress compress(File file, File cacheDir) {
        if (!isCacheDirValid(cacheDir)) {
            throw new IllegalArgumentException("The cacheDir must be Directory");
        }
        FileCompress fileCompress = new FileCompress(cacheDir);
        fileCompress.mFile = file;
        fileCompress.mFileList = Collections.singletonList(file);
        return fileCompress;
    }

    public static FileCompress compress(Context context, File file) {
        FileCompress fileCompress = new FileCompress(FileCompress.getPhotoCacheDir(context));
        fileCompress.mFile = file;
        fileCompress.mFileList = Collections.singletonList(file);
        return fileCompress;
    }

    public static FileCompress compress(Context context, List<File> files) {
        FileCompress fileCompress = new FileCompress(FileCompress.getPhotoCacheDir(context));
//        FileCompress fileCompress = new FileCompress(new File(Environment.getExternalStorageDirectory() + File.separator + "FileCompress_Compress"));
        fileCompress.mFileList = new ArrayList<>(files);
        fileCompress.mFile = files.get(0);
        return fileCompress;
    }

    public Observable<File> asObservable() {
        FileCompressor compresser = new FileCompressor(builder);
        return compresser.singleAction(mFile);
    }

    public Observable<List<File>> asListObservable() {
        FileCompressor compresser = new FileCompressor(builder);
        return compresser.multipleAction(mFileList);
    }

    private static boolean isCacheDirValid(File cacheDir) {
        return cacheDir.isDirectory() && (cacheDir.exists() || cacheDir.mkdirs());
    }

    private static File getPhotoCacheDir(Context context) {
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
