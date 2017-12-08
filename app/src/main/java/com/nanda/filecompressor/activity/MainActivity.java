package com.nanda.filecompressor.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.nanda.filecompressor.R;
import com.nanda.filecompressor.app.AppConstants;
import com.nanda.filecompressor.helper.AttachmentSelector;
import com.nanda.filecompressor.utils.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements AttachmentSelector.AttachmentSelectionListener {

    @BindView(R.id.layout_attachment)
    LinearLayout layoutAttachment;

    private AttachmentSelector attachmentSelector;
    private List<String> filePathList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }

    @OnClick(R.id.btn_gallery)
    public void onGalleryClicked() {
        attachmentSelector = new AttachmentSelector(String.valueOf(System.currentTimeMillis()), this, this);
        attachmentSelector.selectDocumentAttachment();
    }

    @OnClick(R.id.btn_camera)
    public void onCameraClicked() {
        attachmentSelector = new AttachmentSelector(String.valueOf(System.currentTimeMillis()), this, this);
        try {
            attachmentSelector.selectCameraAttachment();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        attachmentSelector.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onAttachmentSelected(Uri attachmentUri, int code) {
        if (attachmentUri == null) {
            return;
        }

        if (code == AppConstants.REQUEST_CAMERA_PICK) {
            filePathList.add(attachmentUri.toString());
            showAttachment(filePathList);
            return;
        }

        String filePath = FileUtils.getPath(this, attachmentUri);
        if (TextUtils.isEmpty(filePath))
            return;
        filePathList.add(filePath);
        showAttachment(filePathList);
    }

    private void showAttachment(List<String> filePathList) {
        if (filePathList == null) {
            return;
        }

        for (int i = 0, filePathListSize = filePathList.size(); i < filePathListSize; i++) {
            String item = filePathList.get(i);
            addAttachment(item);
        }

    }

    private void addAttachment(String path) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_attachment, null, false);
        ImageView imgAttachment = view.findViewById(R.id.img_attachment);

        Glide.with(view.getContext())
                .load(path)
                .into(imgAttachment);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutAttachment.addView(view, params);

    }

}
