package com.siddhantkushwaha.raven.common.utility;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;

public class Alerts {

    public static void showSnackbar(View view, String message, int duration) {

        Snackbar.make(view, message, duration).show();
    }

    public static void showToast(Context context, String message, int duration) {

        Toast.makeText(context, message, duration).show();
    }

    public static AlertDialog showDialogType1(Context ctx, String message, DialogInterface.OnClickListener positiveButtonCallback, String positiveLabel, Boolean cancellable) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(positiveLabel, positiveButtonCallback);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(cancellable);
        return alertDialog;
    }

    public static AlertDialog showDialogType2(Context ctx, String message, DialogInterface.OnClickListener positiveButtonCallback, String positiveLabel,
                                       DialogInterface.OnClickListener negativeButtonCallback, String negativeLabel, Boolean cancellable) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("Yes", positiveButtonCallback);
        alertDialogBuilder.setNegativeButton("No", negativeButtonCallback);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(cancellable);
        return alertDialog;
    }
}
