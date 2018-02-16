package com.nanda.filecompressor.activity;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.nanda.filecompressor.R;

import java.util.List;

/**
 * Created by Admin on 16-02-2018.
 */

public class StoredAdpter extends RecyclerView.Adapter<StoredAdpter.MyHolder> {
    public Context context;
    public List<String>list;

    public StoredAdpter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_attachment,parent,false);
        MyHolder myHolder=new MyHolder(view);
        return myHolder;
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
String jpglist=list.get(position);
        Glide.with(context).load(jpglist).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public MyHolder(View itemView) {
            super(itemView);
            imageView=(ImageView)itemView.findViewById(R.id.img_attachment);
        }
    }
}
