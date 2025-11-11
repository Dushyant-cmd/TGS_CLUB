package com.example.winzgo.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.airbnb.lottie.utils.Utils;
import com.example.winzgo.activities.SignUpAndSignIn;
import com.example.winzgo.databinding.ProfileEditDialogBinding;

public class Constants {
    public static final String RED = "red";
    public static final String GREEN = "green";
    public static final String BLUE = "blue";
    // sp key start
    public static final String WHATSAPP_GROUP = "whatsappGroup";
    public static final String WINZGO_AGENT_LINK = "winzgoAgentLink";
    public static final String TELEGRAM_LINK = "telegramLink";
    public static final String VIP_BETS_SUB = "vipBetSubs";
    public static final String APP_DOWNLOAD_LINK = "appDownloadLink";
    public static final String NOTIFICATION_PERMISSION = "notificationPermission";
    public static final String DARK_MODE = "darkMode";
    public static final String IS_INR_USD = "isInrUsd";
    // sp key end
    public static final String RUPEE_ICON = "\u20b9";


    // Interface start

    public interface ProfileEditListener {
        void submit(String name);
    }
    // Interface end

    public static Dialog showDialog(Context context, String message) {
        ProgressDialog dialog1 = new ProgressDialog(context);
        dialog1.setMessage(message);
        dialog1.show();
        return dialog1;
    }

    public static AlertDialog showAlerDialog(Context context, String message, String buttonName, UtilsInterfaces.Refresh listener) {
        AlertDialog dialog = new AlertDialog.Builder(context).setMessage(message).setTitle("TGS club")
                .setPositiveButton(buttonName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.refresh();
                    }
                }).create();

        dialog.show();
        return dialog;
    }

    public static Dialog showProgressDialog(Context context) {
        ProgressDialog dialog1 = new ProgressDialog(context);
        dialog1.setMessage("Please wait...");
        dialog1.show();
        return dialog1;
    }

    public static Dialog showEditDialog(Activity activity, ProfileEditListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        ProfileEditDialogBinding binding = ProfileEditDialogBinding.inflate(activity.getLayoutInflater(), (ViewGroup) activity.getWindow().getDecorView().getRootView(), false);
        builder.setView(binding.getRoot());

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        binding.ivClose.setOnClickListener(v -> {
            dialog.dismiss();
        });

        binding.confirmBtn.setOnClickListener(v -> {
            String name = binding.etName.getText().toString();
            if (!name.isEmpty()) {
                listener.submit(name);
                dialog.dismiss();
            } else {
                binding.etName.setError("Required");
            }
        });

        dialog.show();

        return dialog;
    }


    public static void showLogoutAlertDialog(Context context, UtilsInterfaces.Refresh listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Log-out?");
        builder.setMessage("Are you sure you want to Log-out?");
        AlertDialog alertDialog = builder.create();
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
                listener.refresh();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                //close dialog
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }
}
