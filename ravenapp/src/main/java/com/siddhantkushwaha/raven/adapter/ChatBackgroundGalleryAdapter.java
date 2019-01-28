package com.siddhantkushwaha.raven.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.siddhantkushwaha.raven.R;
import com.siddhantkushwaha.raven.entity.WallpaperMetadata;
import com.siddhantkushwaha.raven.utility.FirebaseStorageUtil;
import com.siddhantkushwaha.raven.utility.GlideUtils;

import java.util.ArrayList;

public class ChatBackgroundGalleryAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<WallpaperMetadata> data;
    private FirebaseStorageUtil firebaseStorageUtil;

    public ChatBackgroundGalleryAdapter(Context context, ArrayList<WallpaperMetadata> data) {

        this.context = context;
        this.data = data;
        this.firebaseStorageUtil = new FirebaseStorageUtil();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public WallpaperMetadata getItem(int position) {
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
        String fileRef = data.get(position).getLowResRef();
        firebaseStorageUtil.getDownloadUrl(context, fileRef, uri -> {

            GlideUtils.loadImage(context, uri, finalListItem.findViewById(R.id.image), R.drawable.hourglass, R.drawable.bug);
        });

        return listItem;
    }
}