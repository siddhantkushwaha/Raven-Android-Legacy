package com.siddhantkushwaha.raven.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.siddhantkushwaha.raven.R;
import com.siddhantkushwaha.raven.ravenUtility.FirebaseStorageUtil;
import com.siddhantkushwaha.raven.ravenUtility.GlideUtils;

import java.util.ArrayList;

public class ChatBackgroundGalleryAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> data;
    private FirebaseStorageUtil firebaseStorageUtil;

    public ChatBackgroundGalleryAdapter(Context context, ArrayList<String> data) {

        this.context = context;
        this.data = data;
        this.firebaseStorageUtil = new FirebaseStorageUtil();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public String getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.layout_image, parent, false);

        View finalListItem = listItem;
        String fileRef = data.get(position);
        firebaseStorageUtil.getDownloadUrl(context, fileRef, uri -> {

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.hourglass);
            requestOptions.transform(new CenterCrop());

            GlideUtils.loadImage(context, uri, requestOptions, finalListItem.findViewById(R.id.image));
        });

        return listItem;
    }
}