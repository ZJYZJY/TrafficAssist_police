package com.zjy.police.trafficassist.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zjy.police.trafficassist.R;

import java.util.ArrayList;

/**
 * Created by 73958 on 2016/11/12.
 */

public class AccidentPicAdapter extends RecyclerView.Adapter<AccidentPicAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Bitmap> accidentPics;

    public AccidentPicAdapter(Context context, ArrayList<Bitmap> accidentPics) {
        this.mContext = context;
        this.accidentPics = accidentPics;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_acc_pic, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.imageView.setImageBitmap(accidentPics.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return accidentPics == null ? 0 : accidentPics.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.acc_pic);
        }
    }
}
