package com.siddhantkushwaha.raven.custom;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class CustomDialogFragment extends DialogFragment{

    public interface PositiveNegativeListener {
        void onPositiveButtonPressed(String key);
        void onNegativeButtonPressed(String key);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof PositiveNegativeListener)) {
            throw new ClassCastException(activity.toString() + " must implement PositiveNegativeListener");
        }
    }

    public static CustomDialogFragment newInstance(String key, String title, String message, String positiveLabel, String negativeLabel, Boolean cancellable) {

        CustomDialogFragment dialogFragment = new CustomDialogFragment();
        Bundle args = new Bundle();
        args.putString("KEY", key);
        args.putString("TITLE", title);
        args.putString("MESSAGE", message);
        args.putString("POSITIVE_LABEL", positiveLabel);
        args.putString("NEGATIVE_LABEL", negativeLabel);
        args.putBoolean("CANCELLABLE", cancellable);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Bundle args = this.getArguments();
        setCancelable(args.getBoolean("CANCELLABLE"));
        return new AlertDialog.Builder(getActivity())
                .setTitle(args.getString("TITLE"))
                .setMessage(args.getString("MESSAGE"))
                .setPositiveButton(args.getString("POSITIVE_LABEL"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((PositiveNegativeListener)getActivity()).onPositiveButtonPressed(args.getString("KEY"));
                    }
                })
                .setNegativeButton(args.getString("NEGATIVE_LABEL"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((PositiveNegativeListener)getActivity()).onNegativeButtonPressed(args.getString("KEY"));
                    }
                })
                .create();

    }
}