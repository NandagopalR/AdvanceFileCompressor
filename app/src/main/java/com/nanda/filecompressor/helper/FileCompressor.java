package com.nanda.filecompressor.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.annotation.NonNull;

import com.nanda.filecompressor.utils.RxJavaUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.functions.Func1;

import static com.nanda.filecompressor.helper.Preconditions.checkNotNull;

public class FileCompressor {

    private static final String TAG = "Fixeous";

    private Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;

    private ByteArrayOutputStream mByteArrayOutputStream;

    private FileCompressBuilder builder;

    public FileCompressor(FileCompressBuilder builder) {
        this.builder = builder;
    }

    Observable<File> singleAction(final File file) {
        return Observable.fromCallable(new Callable<File>() {
            @Override
            public File call() throws Exception {
                return thirdCompress(file);
            }
        }).compose(RxJavaUtils.<File>applyObserverSchedulers());
    }

    Observable<List<File>> multipleAction(final List<File> fileList) {

        return Observable.from(fileList)
                .flatMapIterable(new Func1<File, Iterable<File>>() {
                    @Override
                    public Iterable<File> call(File file) {
                        try {
                            return (Iterable<File>) thirdCompress(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .map(new Func1<File, File>() {
                    @Override
                    public File call(File file) {
                        return file;
                    }
                }).toList();


    }

//    Observable<List<File>> multiAction(final List<File> files) {
//
//        List<Observable<File>> observables = new ArrayList<>(files.size());
//        for (final File file : files) {
//            observables.add(Observable.fromCallable(new Callable<File>() {
//                @Override
//                public File call() throws Exception {
//                    return thirdCompress(file);
//                }
//            }));
//        }
//
//        return Observable.zip(observables, new FuncN<Object[], List<File>>() {
//            @Override
//            public List<File> apply(Object[] args) throws Exception {
//                List<File> files = new ArrayList<>(args.length);
//                for (Object o : args) {
//                    files.add((File) o);
//                }
//                return files;
//            }
//        }).compose(RxJavaUtils.applyObserverSchedulers());
//    }

    private File thirdCompress(@NonNull File file) throws IOException {
        String thumb = getCacheFilePath();

        double size;
        String filePath = file.getAbsolutePath();

        int angle = getImageSpinAngle(filePath);
        int width = getImageSize(filePath)[0];
        int height = getImageSize(filePath)[1];
        boolean flip = width > height;
        int thumbW = width % 2 == 1 ? width + 1 : width;
        int thumbH = height % 2 == 1 ? height + 1 : height;

        width = thumbW > thumbH ? thumbH : thumbW;
        height = thumbW > thumbH ? thumbW : thumbH;

        double scale = ((double) width / height);

        if (scale <= 1 && scale > 0.5625) {
            if (height < 1664) {
                if (file.length() / 1024 < 150) {
                    return file;
                }

                size = (width * height) / Math.pow(1664, 2) * 150;
                size = size < 60 ? 60 : size;
            } else if (height >= 1664 && height < 4990) {
                thumbW = width / 2;
                thumbH = height / 2;
                size = (thumbW * thumbH) / Math.pow(2495, 2) * 300;
                size = size < 60 ? 60 : size;
            } else if (height >= 4990 && height < 10240) {
                thumbW = width / 4;
                thumbH = height / 4;
                size = (thumbW * thumbH) / Math.pow(2560, 2) * 300;
                size = size < 100 ? 100 : size;
            } else {
                int multiple = height / 1280 == 0 ? 1 : height / 1280;
                thumbW = width / multiple;
                thumbH = height / multiple;
                size = (thumbW * thumbH) / Math.pow(2560, 2) * 300;
                size = size < 100 ? 100 : size;
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            if (height < 1280 && file.length() / 1024 < 200) {
                return file;
            }

            int multiple = height / 1280 == 0 ? 1 : height / 1280;
            thumbW = width / multiple;
            thumbH = height / multiple;
            size = (thumbW * thumbH) / (1440.0 * 2560.0) * 400;
            size = size < 100 ? 100 : size;
        } else {
            int multiple = (int) Math.ceil(height / (1280.0 / scale));
            thumbW = width / multiple;
            thumbH = height / multiple;
            size = ((thumbW * thumbH) / (1280.0 * (1280 / scale))) * 500;
            size = size < 100 ? 100 : size;
        }

        return compress(filePath, thumb, flip ? thumbH : thumbW, flip ? thumbW : thumbH, angle,
                (long) size);
    }

    private File compressImage(File file) throws IOException {
        int minSize = 60;
        int longSide = 720;
        int shortSide = 1280;

        String thumbFilePath = getCacheFilePath();
        String filePath = file.getAbsolutePath();

        long size = 0;
        long maxSize = file.length() / 5;

        int angle = getImageSpinAngle(filePath);
        int[] imgSize = getImageSize(filePath);
        int width = 0, height = 0;
        if (imgSize[0] <= imgSize[1]) {
            double scale = (double) imgSize[0] / (double) imgSize[1];
            if (scale <= 1.0 && scale > 0.5625) {
                width = imgSize[0] > shortSide ? shortSide : imgSize[0];
                height = width * imgSize[1] / imgSize[0];
                size = minSize;
            } else if (scale <= 0.5625) {
                height = imgSize[1] > longSide ? longSide : imgSize[1];
                width = height * imgSize[0] / imgSize[1];
                size = maxSize;
            }
        } else {
            double scale = (double) imgSize[1] / (double) imgSize[0];
            if (scale <= 1.0 && scale > 0.5625) {
                height = imgSize[1] > shortSide ? shortSide : imgSize[1];
                width = height * imgSize[0] / imgSize[1];
                size = minSize;
            } else if (scale <= 0.5625) {
                width = imgSize[0] > longSide ? longSide : imgSize[0];
                height = width * imgSize[1] / imgSize[0];
                size = maxSize;
            }
        }

        return compress(filePath, thumbFilePath, width, height, angle, size);
    }

    public static int[] getImageSize(String imagePath) {
        int[] res = new int[2];

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;
        BitmapFactory.decodeFile(imagePath, options);

        res[0] = options.outWidth;
        res[1] = options.outHeight;

        return res;
    }

    private int getImageSpinAngle(String path) {
        int degree = 0;
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(path);
        } catch (IOException e) {
            return 0;
        }
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
        }
        return degree;
    }

    private String getCacheFilePath() {
        StringBuilder name = new StringBuilder("compressed_" + System.currentTimeMillis());
        if (compressFormat == Bitmap.CompressFormat.WEBP) {
            name.append(".webp");
        } else {
            name.append(".jpg");
        }
        return builder.cacheDir.getAbsolutePath() + File.separator + name;
    }

    private File compress(String largeImagePath, String thumbFilePath, int width, int height,
                          int angle, long size) throws IOException {
        Bitmap thbBitmap = compress(largeImagePath, width, height);

        thbBitmap = rotatingImage(angle, thbBitmap);

        return saveImage(thumbFilePath, thbBitmap, size);
    }

    private Bitmap compress(String imagePath, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);

        int outH = options.outHeight;
        int outW = options.outWidth;
        int inSampleSize = 1;

        while (outH / inSampleSize > height || outW / inSampleSize > width) {
            inSampleSize *= 2;
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(imagePath, options);
    }

    private static Bitmap rotatingImage(int angle, Bitmap bitmap) {
        //rotate image
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        //create a new image
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
                true);
    }

    private File saveImage(String filePath, Bitmap bitmap, long size) throws IOException {
        checkNotNull(bitmap, TAG + "bitmap cannot be null");

        File result = new File(filePath.substring(0, filePath.lastIndexOf("/")));

        if (!result.exists() && !result.mkdirs()) {
            return null;
        }

        if (mByteArrayOutputStream == null) {
            mByteArrayOutputStream = new ByteArrayOutputStream(
                    bitmap.getWidth() * bitmap.getHeight());
        } else {
            mByteArrayOutputStream.reset();
        }

        int options = 100;
        bitmap.compress(compressFormat, options, mByteArrayOutputStream);

        while (mByteArrayOutputStream.size() / 1024 > size && options > 6) {
            mByteArrayOutputStream.reset();
            options -= 6;
            bitmap.compress(compressFormat, options, mByteArrayOutputStream);
        }
        bitmap.recycle();

        FileOutputStream fos = new FileOutputStream(filePath);
        mByteArrayOutputStream.writeTo(fos);
        fos.close();

        return new File(filePath);
    }

}
