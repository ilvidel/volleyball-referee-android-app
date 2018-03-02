package com.tonkar.volleyballreferee.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.tonkar.volleyballreferee.R;

public class AlertDialogFragment extends DialogFragment {

    private AlertDialogListener mAlertDialogListener;

    public static AlertDialogFragment newInstance(String title, String message, String negativeButtonText) {
        AlertDialogFragment fragment = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        args.putString("negative", negativeButtonText);
        fragment.setArguments(args);
        return fragment;
    }

    public static AlertDialogFragment newInstance(String title, String message, String negativeButtonText, String positiveButtonText) {
        AlertDialogFragment fragment = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        args.putString("negative", negativeButtonText);
        args.putString("positive", positiveButtonText);
        fragment.setArguments(args);
        return fragment;
    }

    public static AlertDialogFragment newInstance(String title, String message, String negativeButtonText, String positiveButtonText, String neutralButtonText) {
        AlertDialogFragment fragment = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        args.putString("negative", negativeButtonText);
        args.putString("positive", positiveButtonText);
        args.putString("neutral", neutralButtonText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        String message = getArguments().getString("message");

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
        builder.setTitle(title).setMessage(message);

        String negativeButtonText = getArguments().getString("negative");
        if (negativeButtonText != null) {
            builder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mAlertDialogListener.onNegativeButtonClicked();
                }
            });
        }

        String positiveButtonText = getArguments().getString("positive");
        if (positiveButtonText != null) {
            builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mAlertDialogListener.onPositiveButtonClicked();
                }
            });
        }

        String neutralButtonText = getArguments().getString("neutral");
        if (neutralButtonText != null) {
            builder.setNeutralButton(neutralButtonText, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mAlertDialogListener.onNeutralButtonClicked();
                }
            });
        }

        setCancelable(false);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                if (isAdded()) {
                    UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
                }
            }
        });
        return alertDialog;
    }

    public void setAlertDialogListener(AlertDialogListener alertDialogListener) {
        mAlertDialogListener = alertDialogListener;
    }

    public interface AlertDialogListener {

        void onNegativeButtonClicked();

        void onPositiveButtonClicked();

        void onNeutralButtonClicked();

    }

}
