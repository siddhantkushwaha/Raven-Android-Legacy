package com.siddhantkushwaha.raven.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.siddhantkushwaha.raven.R;

public class GlideUtil {

    public static void preload(Context context, String uri, RequestOptions requestOptions) {

        Glide.with(context).load(uri).preload();
    }

    public static void loadImage(Context context, String uri, ImageView imageView, int placeHolder, int error) {

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(placeHolder);
        requestOptions.error(error);

        Glide.with(context.getApplicationContext())
                .load(uri)
                .apply(requestOptions)
                .into(imageView);
    }

    public static void loadProfilePhotoCircle(Context context, ImageView imageView, String uri) {

        Glide.with(context.getApplicationContext())
                .load(uri)
                .apply(RequestOptions.circleCropTransform())
                .apply(new RequestOptions().placeholder(R.drawable.image_unknown_user_circle))
                .apply(new RequestOptions().error(R.drawable.image_unknown_user_circle))
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .into(imageView);
    }

    public static void loadProfilePhotoSquare(Context context, ImageView imageView, String uri) {

        Glide.with(context.getApplicationContext())
                .load(uri)
                .apply(new RequestOptions().placeholder(R.drawable.image_unknown_user_rectangle))
                .apply(new RequestOptions().error(R.drawable.image_unknown_user_rectangle))
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .into(imageView);
    }

    public static void loadImageAsBitmap(Context context, String uri, RequestOptions requestOptions, SimpleTarget<Bitmap> callback) {

        Glide.with(context.getApplicationContext())
                .asBitmap()
                .load(uri)
                .apply(requestOptions)
                .into(callback);
    }

    public static void loadChatBackground(Context context, String uri, RequestOptions requestOptions, ImageView imageView) {

        Glide.with(context.getApplicationContext())
                .load(uri)
                .apply(requestOptions)
                .into(imageView);
    }

    public static void loadImageInChat(Context context, String uri, ImageView imageView) {

        Glide.with(context.getApplicationContext())
                .load(uri)
                .apply(new RequestOptions().placeholder(R.drawable.hourglass))
                .apply(new RequestOptions().error(R.drawable.bug))
                .transforms(new CenterCrop(), new RoundedCorners(24))
                .into(imageView);
    }
}
