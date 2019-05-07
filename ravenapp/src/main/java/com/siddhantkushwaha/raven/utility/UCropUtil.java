package com.siddhantkushwaha.raven.utility;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.siddhantkushwaha.raven.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;

public class UCropUtil {

    public static void startCrop(@NonNull Activity activity, @NonNull Uri uri) {

        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(activity.getCacheDir(), "file_to_send.png")));
        uCrop.withMaxResultSize(1024, 1024);

        UCrop.Options options = new UCrop.Options();
        options.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
        options.setToolbarColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        options.setActiveWidgetColor(ContextCompat.getColor(activity, R.color.colorAccent));
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        uCrop.withOptions(options);
        uCrop.start(activity);
    }

    public static void startCrop(@NonNull Activity activity, @NonNull Uri uri, @NonNull Uri destUri) {

        UCrop uCrop = UCrop.of(uri, destUri);
        uCrop.withMaxResultSize(1024, 1024);

        UCrop.Options options = new UCrop.Options();
        options.setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimaryDark));
        options.setToolbarColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        options.setActiveWidgetColor(ContextCompat.getColor(activity, R.color.colorAccent));
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        uCrop.withOptions(options);
        uCrop.start(activity);
    }
}
