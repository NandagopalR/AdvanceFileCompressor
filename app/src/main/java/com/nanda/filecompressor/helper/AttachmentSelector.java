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
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

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
    private Uri mCameraUri;
    private File mAudioFile;
    private String uniqueIdPerWorkOrder;

    public interface AttachmentSelectionListener {
        void onAttachmentSelected(Uri attachmentUri, int code);
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
        File parent = new File(Environment.getExternalStorageDirectory() + File.separator + "SanghaTApp");
        parent.mkdirs();
        String fileName = String.format("%s.jpg", uniqueIdPerWorkOrder);
        File file = new File(parent, fileName);
        if (!file.exists())
            file.createNewFile();

        return Uri.fromFile(file);
    }

    public void selectCameraAttachment() throws IOException {
        if (CommonUtils.checkCameraHardware(getContext())) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (cameraIntent.resolveActivity(getContext().getPackageManager()) != null) {
                mCameraUri = createUriForCameraIntent(getContext());
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraUri);
                cameraIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                // Workaround for Android bug.
                // grantUriPermission also needed for KITKAT,
                // see https://code.google.com/p/android/issues/detail?id=76683
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    List<ResolveInfo> resInfoList = getContext().getPackageManager().queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        getContext().grantUriPermission(packageName, mCameraUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                }
                if (activity != null) {
                    activity.startActivityForResult(cameraIntent, AppConstants.REQUEST_CAMERA_PICK);
                } else {
                    fragment.startActivityForResult(cameraIntent, AppConstants.REQUEST_CAMERA_PICK);
                }
            } else
                Toast.makeText(getContext(), getContext().getString(R.string.app_name), Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(getContext(), getContext().getString(R.string.no_app_to_perform), Toast.LENGTH_SHORT).show();
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
                        if (mCameraUri != null) {
                            listener.onAttachmentSelected(mCameraUri, AppConstants.REQUEST_CAMERA_PICK);
                        }
                    }
                } else {
                    attachmentUri = data.getData();
                    if (listener != null) {
                        listener.onAttachmentSelected(attachmentUri, AppConstants.REQUEST_DOCUMENT_PICK);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), getContext().getString(R.string.failed_to_attach), Toast.LENGTH_SHORT).show();
            }
            return true;
        } else {
            return false;
        }

    }


}
