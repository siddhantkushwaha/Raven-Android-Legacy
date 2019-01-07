package com.siddhantkushwaha.raven.ravenUtility;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.siddhantkushwaha.raven.R;
import com.siddhantkushwaha.raven.commonUtility.GlideApp;

public class GlideUtils {

    public static void loadProfilePhotoCircle(Context context, ImageView imageView, String uri) {

        GlideApp.with(context.getApplicationContext())
                .load(uri)
                .apply(RequestOptions.circleCropTransform())
                .apply(new RequestOptions().placeholder(R.drawable.image_unknown_user_circle))
                .apply(new RequestOptions().error(R.drawable.image_unknown_user_circle))
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .into(imageView);
    }

    public static void loadProfilePhotoSquare(Context context, ImageView imageView, String uri) {

        GlideApp.with(context.getApplicationContext())
                .load(uri)
                .apply(new RequestOptions().placeholder(R.drawable.image_unknown_user_rectangle))
                .apply(new RequestOptions().error(R.drawable.image_unknown_user_rectangle))
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .into(imageView);
    }

    public static void loadImageAsBitmap(Context context, String uri, RequestOptions requestOptions, SimpleTarget<Bitmap> callback) {

        GlideApp.with(context.getApplicationContext())
                .asBitmap()
                .load(uri)
                .apply(requestOptions)
                .into(callback);
    }

    public static void loadChatBackground(Context context, String uri, RequestOptions requestOptions, ImageView imageView) {

        GlideApp.with(context.getApplicationContext())
                .load(uri)
                .apply(requestOptions)
                .into(imageView);
    }

    public static void loadImageInChat(Context context, String uri, ImageView imageView) {

        GlideApp.with(context.getApplicationContext())
                .load(uri)
                .apply(new RequestOptions().placeholder(R.drawable.hourglass))
                .apply(new RequestOptions().error(R.drawable.bug))
                .transforms(new CenterCrop(), new RoundedCorners(72))
                .into(imageView);
    }
}
