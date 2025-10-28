package com.example.winzgo;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.animation.Animator;
import androidx.core.animation.AnimatorSet;

import com.example.winzgo.sharedpref.SessionSharedPref;
import com.example.winzgo.utils.UtilsInterfaces;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MyApplication extends Application {
    public SessionSharedPref sharedPref;
    public FirebaseFirestore firestore;
    public FirebaseStorage firebaseStorage;
    @Override
    public void onCreate() {
        super.onCreate();
        sharedPref = new SessionSharedPref(this);
        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
    }

    public void scaleView(View view, Boolean isDown) {
        if (isDown) {
            Animator scaleDownX = androidx.core.animation.ObjectAnimator.ofFloat(
                    view,
                    "scaleX", 0.9f
            );
            Animator scaleDownY = androidx.core.animation.ObjectAnimator.ofFloat(
                    view,
                    "scaleY", 0.9f
            );
            scaleDownX.setDuration(500);
            scaleDownY.setDuration(500);
            AnimatorSet scaleDown = new AnimatorSet();
            scaleDown.play(scaleDownX).with(scaleDownY);
            scaleDown.start();
        } else {
            Animator scaleDownX2 = androidx.core.animation.ObjectAnimator.ofFloat(
                    view, "scaleX", 1f
            );
            Animator scaleDownY2 = androidx.core.animation.ObjectAnimator.ofFloat(
                    view, "scaleY", 1f
            );
            scaleDownX2.setDuration(500);
            scaleDownY2.setDuration(500);
            AnimatorSet scaleDown2 = new AnimatorSet();
            scaleDown2.play(scaleDownX2).with(scaleDownY2);
            scaleDown2.start();
        }
    }

    public String getCurrDateAndTime() {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        SimpleDateFormat spf = new SimpleDateFormat("dd-MM-yyyy, hh:mm:ss a", Locale.getDefault());
        String dateAndTime = spf.format(date);
        return dateAndTime;
    }

    public boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
        return connected;
    }

    public void openAlertDialog(Context context, UtilsInterfaces.Refresh listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Check network connection!");
        builder.setCancelable(false);
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (isNetworkConnected()) {
                    Toast.makeText(getApplicationContext(), "Network connected successfully", Toast.LENGTH_SHORT).show();
                    listener.refresh();
                } else {
                    openAlertDialog(context, listener);
                }
            }
        });
        builder.create().show();
    }
}
