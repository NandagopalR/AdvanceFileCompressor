package com.nanda.filecompressor.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.nanda.filecompressor.BuildConfig;
import com.nanda.filecompressor.R;
import com.nanda.filecompressor.app.AppConstants;
import com.nanda.filecompressor.utils.CommonUtils;
import com.nanda.filecompressor.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AttachmentSelector {

    private AppCompatActivity activity;
    private Fragment fragment;
    private AttachmentSelectionListener listener;
    private File mAudioFile;
    private File mCameraFile;
    private String uniqueIdPerWorkOrder;

    public interface AttachmentSelectionListener {
        void onAttachmentSelected(String filePath, int code);
    }

    private AttachmentSelector(String uniqueIdPerWorkOrder, AttachmentSelectionListener listener) {
        this.uniqueIdPerWorkOrder = uniqueIdPerWorkOrder;
        this.listener = listener;
    }

    public AttachmentSelector(String uniqueIdPerWorkOrder, AppCompatActivity activity, AttachmentSelectionListener listener) {
        this.uniqueIdPerWorkOrder = uniqueIdPerWorkOrder;
        this.activity = activity;
        this.listener = listener;
    }

    public AttachmentSelector(String uniqueIdPerWorkOrder, Fragment fragment, AttachmentSelectionListener listener) {
        this(uniqueIdPerWorkOrder, listener);
        this.fragment = fragment;
    }


    public Context getContext() {
        if (activity != null)
            return activity;
        else
            return fragment.getContext();
    }

    private Uri createUriForCameraIntent(Context context) throws IOException {
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String fileName = String.format("%s", uniqueIdPerWorkOrder);

        mCameraFile = File.createTempFile(
                fileName,  /* prefix */
                ".jpg",        /* suffix */
                storageDir      /* directory */
        );
        Uri imgUri = FileProvider.getUriForFile(getContext(),
                "com.nanda.filecompressor", mCameraFile);

        return imgUri;
    }

    public void selectCameraAttachment() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            Uri mCameraUri = createUriForCameraIntent(getContext());
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraUri);
            if (activity != null) {
                activity.startActivityForResult(takePictureIntent, AppConstants.REQUEST_CAMERA_PICK);
            } else {
                fragment.startActivityForResult(takePictureIntent, AppConstants.REQUEST_CAMERA_PICK);
            }
        } else
            Toast.makeText(getContext(), getContext().getString(R.string.app_name), Toast.LENGTH_SHORT).show();
    }

    public void selectDocumentAttachment() {
        Intent target = FileUtils.createGetContentIntent();
        boolean isDocAvailable = CommonUtils.isAvailable(getContext(), target);
        if (isDocAvailable)
            if (activity != null) {
                activity.startActivityForResult(target, AppConstants.REQUEST_DOCUMENT_PICK);
            } else {
                fragment.startActivityForResult(target, AppConstants.REQUEST_DOCUMENT_PICK);
            }
        else
            Toast.makeText(getContext(), getContext().getString(R.string.no_app_to_perform), Toast.LENGTH_SHORT).show();
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return false;

        if (requestCode == AppConstants.REQUEST_CAMERA_PICK
                || requestCode == AppConstants.REQUEST_GALLERY_PICK
                || requestCode == AppConstants.REQUEST_DOCUMENT_PICK
                || requestCode == AppConstants.REQUEST_AUDIO_PICK) {

            Uri attachmentUri;
            try {
                if (requestCode == AppConstants.REQUEST_CAMERA_PICK) {
                    if (listener != null) {
                        if (mCameraFile != null) {
                            listener.onAttachmentSelected(mCameraFile.getAbsolutePath(), AppConstants.REQUEST_CAMERA_PICK);
                        }
                    }
                } else {
                    attachmentUri = data.getData();
                    if (listener != null) {
                        listener.onAttachmentSelected(attachmentUri.toString(), AppConstants.REQUEST_DOCUMENT_PICK);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(),  getContext().getString(R.string.failed_to_attach), Toast.LENGTH_SHORT).show();
            }
            return true;
        } else {
            return false;
        }

    }


}
