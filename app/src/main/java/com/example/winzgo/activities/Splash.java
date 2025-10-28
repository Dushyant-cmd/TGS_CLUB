package com.example.winzgo.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.winzgo.MainActivity;
import com.example.winzgo.R;
import com.example.winzgo.sharedpref.SessionSharedPref;
import com.example.winzgo.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class Splash extends AppCompatActivity {

    private FirebaseAuth mAuth;
    SessionSharedPref sharedPreferences;
    private FirebaseFirestore mFirestore;
    String tag = "Splash.java";
    AlertDialog dialog;
    private ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = new SessionSharedPref(Splash.this);
        mFirestore = FirebaseFirestore.getInstance();
        bar = new ProgressBar(Splash.this);
        //for display layout of res file UI on all over the screen window of Activity including status
        //bar and action bar not hide because it is part of layout.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getConstants();
        if (sharedPreferences.getLoginStatus()) {
            if (isNetworkAvailable()) {
                getConstants();
                if (sharedPreferences.getId() != 0L) {
                    mFirestore.collection("users").whereEqualTo("id", sharedPreferences.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                                Log.v("Splash.java", doc + "");
                                sharedPreferences.setBalance(doc.getLong("balance"));
                                sharedPreferences.setRefer(doc.getString("refer"));
                                bar.setVisibility(View.GONE);
                                Log.v(tag, sharedPreferences.getBalance() + "");
                                //Delay in execution of code
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (sharedPreferences.getLoginStatus()) {
                                            Intent i = new Intent(Splash.this, MainActivity.class);
                                            i.putExtra("phone", sharedPreferences.getMobile());
                                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(i);
                                            finish();
                                        } else {
                                            Intent i = new Intent(Splash.this, SignUpAndSignIn.class);
                                            startActivity(i);
                                            finish();
                                        }
                                    }
                                }, 2000);
                            }
                        }
                    });
                } else {
                    //Delay in execution of code
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (sharedPreferences.getLoginStatus()) {
                                Intent i = new Intent(Splash.this, MainActivity.class);
                                i.putExtra("phone", sharedPreferences.getMobile());
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                                finish();
                            } else {
                                Intent i = new Intent(Splash.this, SignUpAndSignIn.class);
                                startActivity(i);
                                finish();
                            }
                        }
                    }, 2000);
                }
            } else {
                displayAlertDialog();
            }
        } else if (!sharedPreferences.getLoginStatus()) {
            //display rules dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(Splash.this);
            View view = LayoutInflater.from(Splash.this).inflate(R.layout.splash_terms, null);
            builder.setCancelable(false);
            builder.setView(view);
            TextView btn = view.findViewById(R.id.closeDialog);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (isNetworkAvailable()) {
                        if (sharedPreferences.getId() != 0L) {
                            //Delay in execution of code
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (sharedPreferences.getLoginStatus()) {
                                        Intent i = new Intent(Splash.this, MainActivity.class);
                                        i.putExtra("phone", sharedPreferences.getMobile());
                                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                        finish();
                                    } else {
                                        Intent i = new Intent(Splash.this, SignUpAndSignIn.class);
                                        startActivity(i);
                                        finish();
                                    }
                                }
                            }, 2000);
                        } else {
                            //Delay in execution of code
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (sharedPreferences.getLoginStatus()) {
                                        Intent i = new Intent(Splash.this, MainActivity.class);
                                        i.putExtra("phone", sharedPreferences.getMobile());
                                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                        finish();
                                    } else {
                                        Intent i = new Intent(Splash.this, SignUpAndSignIn.class);
                                        startActivity(i);
                                        finish();
                                    }
                                }
                            }, 2000);
                        }
                    } else {
                        displayAlertDialog();
                    }
                }
            });
            dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        }
    }

    private void displayAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Connect to network!");
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (isNetworkAvailable()) {
                    getConstants();
                } else {
                    displayAlertDialog();
                }
            }
        });
    }

    public void getConstants() {
        mFirestore.collection("constants").document("3").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot doc = task.getResult();
                        String whatsappGroup = doc.getString("group");
                        String winzgoAgentLink = doc.getString("winzgoAgentLink");
                        String telegramLink = doc.getString("telegramLink");
                        String vipBetSubs = doc.getString("vipBetSubs");
                        String appDownloadLink = doc.getString("appDownloadLink");

                        SessionSharedPref.setStr(Splash.this, Constants.WHATSAPP_GROUP, whatsappGroup);
                        SessionSharedPref.setStr(Splash.this, Constants.WINZGO_AGENT_LINK, winzgoAgentLink);
                        SessionSharedPref.setStr(Splash.this, Constants.TELEGRAM_LINK, telegramLink);
                        SessionSharedPref.setStr(Splash.this, Constants.VIP_BETS_SUB, vipBetSubs);
                        SessionSharedPref.setStr(Splash.this, Constants.APP_DOWNLOAD_LINK, appDownloadLink);
                    }
                });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() != null || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() != null;
    }
}