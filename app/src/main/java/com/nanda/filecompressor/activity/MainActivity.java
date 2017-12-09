package com.nanda.filecompressor.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.nanda.filecompressor.R;
import com.nanda.filecompressor.app.AppConstants;
import com.nanda.filecompressor.app.AppController;
import com.nanda.filecompressor.helper.AttachmentSelector;
import com.nanda.filecompressor.utils.FileUtils;
import com.nanda.filecompressor.utils.RxJavaUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity implements AttachmentSelector.AttachmentSelectionListener {

    @BindView(R.id.layout_attachment)
    LinearLayout layoutAttachment;

    private AttachmentSelector attachmentSelector;
    private List<String> filePathList = new ArrayList<>();
    private boolean isSingleCompression = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }

    @OnCheckedChanged({R.id.rb_multiple, R.id.rb_single})
    public void onCompressTypeChecked(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()) {
            case R.id.rb_single:
                isSingleCompression = true;
                break;
            case R.id.rb_multiple:
                isSingleCompression = false;
                break;
        }

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
            String filePath = attachmentUri.toString();
            filePath = filePath.replace("file://", "");
            compressImage(filePath);
            return;
        }

        String filePath = FileUtils.getPath(this, attachmentUri);
        if (TextUtils.isEmpty(filePath))
            return;
        compressImage(filePath);
    }

    private void compressImage(String filePath) {
        AppController.getInstance().getFileCompress()
                .compress(this, new File(filePath))
                .compose(RxJavaUtils.<File>applyObserverSchedulers())
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(File file) {
                        filePathList.add(file.getAbsolutePath());
                        if (isSingleCompression)
                            addAttachment(file.getAbsolutePath());
                        else
                            showAttachment(filePathList);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
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

        imgAttachment.setImageResource(0);
        Glide.with(view.getContext())
                .load(path)
                .into(imgAttachment);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutAttachment.addView(view, params);

    }

}
