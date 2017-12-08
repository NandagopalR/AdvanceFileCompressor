package com.nanda.filecompressor.helper;

import java.io.File;

public class FileCompressBuilder {

    int maxSize;

    int maxWidth;

    int maxHeight;

    File cacheDir;

    FileCompressBuilder(File cacheDir) {
        this.cacheDir = cacheDir;
    }

}
