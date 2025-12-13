package com.example.winzgo.activities;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.winzgo.MyApplication;
import com.example.winzgo.R;
import com.example.winzgo.adapter.UpiAdapter;
import com.example.winzgo.databinding.ActivityManualRechargeBinding;
import com.example.winzgo.databinding.ManualInstructionDialogBinding;
import com.example.winzgo.models.UpiAdapterModel;
import com.example.winzgo.utils.Constants;
import com.example.winzgo.utils.UtilsInterfaces;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ManualRechargeActivity extends AppCompatActivity {
    private ActivityManualRechargeBinding binding;
    private ProgressDialog dialog;
    private List<UpiAdapterModel> list = new ArrayList<>();
    private MyApplication application;
    private ActivityResultLauncher<Intent> contract;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManualRechargeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        application = (MyApplication) getApplication();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait");

        if (!application.sharedPref.isInstructions()) {
            showTermsAlertDialog();
            application.sharedPref.setInstructions(false);
        }

        if (application.isNetworkConnected()) {
            getTransactionDetails();
        } else {
            application.openAlertDialog(this, new UtilsInterfaces.Refresh() {
                @Override
                public void refresh() {
                    getTransactionDetails();
                }
            });
        }
    }

    private void getTransactionDetails() {
        dialog.show();
        setListeners();
        application.firestore.collection("appData").document("lowerSetting")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        dialog.dismiss();
                        if (task.isSuccessful()) {
                            String upi = task.getResult().getString("manualUpi");
                            String[] str = upi.split(",");
                            for (String s : str) {
                                UpiAdapterModel model = new UpiAdapterModel();
                                model.setUpiId(s);
                                model.setCopied(false);
                                list.add(model);
                            }

                            UpiAdapter adapter = new UpiAdapter(ManualRechargeActivity.this, list, Constants.WINGO_TYPE);
                            binding.recyclerView.setLayoutManager(new LinearLayoutManager(ManualRechargeActivity.this));
                            binding.recyclerView.setAdapter(adapter);
                        }
                    }
                });

        StorageReference storageReference = application.firebaseStorage.getReference();
        StorageReference ref = storageReference.child("images/upiqr.jpeg");
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String url = uri.toString();
                Glide.with(ManualRechargeActivity.this).load(url).placeholder(R.drawable.upiqr).into(binding.qrIv);
            }


        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Manual.java", "onFailure: " + e);
            }
        });
    }

    private void setListeners() {
        binding.displayRecBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    application.scaleView(binding.displayRecBtn, true);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    application.scaleView(binding.displayRecBtn, false);
                    binding.addRechargeLl.setVisibility(View.VISIBLE);
                    binding.doRecLy.setVisibility(View.GONE);
                }
                return true;
            }
        });

        binding.addRecharge.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    application.scaleView(binding.displayRecBtn, true);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    application.scaleView(binding.displayRecBtn, false);
                    addRecharge();
                }
                return true;
            }
        });

        binding.gPayFm.setOnClickListener(v -> {
            payUsingUpi(0);
        });

        binding.phonePeFm.setOnClickListener(v -> {
            payUsingUpi(1);
        });

        binding.paytmFm.setOnClickListener(v -> {
            payUsingUpi(2);
        });
    }

    private void addRecharge() {
        String id = binding.utrEt.getText().toString();
        String amount = binding.amountEt.getText().toString();
        if (!amount.isEmpty()) {
            if (id.length() > 4 && Long.parseLong(amount) > 0) {
                ProgressDialog dialog1 = new ProgressDialog(this);
                dialog1.setMessage("Please wait...");
                dialog1.show();
                binding.addRecharge.setEnabled(false);
                HashMap<String, Object> map = new HashMap<>();
                map.put("amount", amount);
                map.put("date", application.getCurrDateAndTime());
                map.put("info", null);
                map.put("name", application.sharedPref.getName());
                map.put("status", "pending");
                map.put("type", "recharge");
                map.put("timestamp", System.currentTimeMillis());
                map.put("user_id", application.sharedPref.getId());
                map.put("utr", id);
                map.put("gameType", 2);
                application.firestore.collection("transactions").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        dialog1.dismiss();
                        Toast.makeText(application, "Recharge Added Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        binding.displayRecBtn.setEnabled(true);
                        dialog1.dismiss();
                        Log.v("Manual.java", e + "");
                    }
                });
            } else if (id.length() <= 4) {
                binding.utrEt.setError("Transaction Id Required");
                binding.utrEt.requestFocus();
            } else {
                binding.amountEt.setError("Must be greater than 0");
                binding.amountEt.requestFocus();
            }
        } else {
            binding.amountEt.setError("Recharge Amount Required");
            binding.amountEt.requestFocus();
        }
    }

    private void payUsingUpi(int upiCode) {
        String packageName = "";
        if (upiCode == 0)
            packageName = "com.google.android.apps.nbu.paisa.user";
        else if (upiCode == 1)
            packageName = "com.phonepe.app";
        else
            packageName = "net.one97.paytm";

        Intent intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(packageName);

        // check if intent resolves
        if (intent != null) {
            binding.displayRecBtn.setVisibility(View.VISIBLE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(ManualRechargeActivity.this, "No UPI app found, please install one to continue", Toast.LENGTH_SHORT).show();
        }
    }

    private void showTermsAlertDialog() {
        ManualInstructionDialogBinding termsBinding = ManualInstructionDialogBinding.inflate(getLayoutInflater(), binding.getRoot(), false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        ManualRechargeActivity.AlertDialogs dialog = new ManualRechargeActivity.AlertDialogs(termsBinding, builder);
        dialog.show(getSupportFragmentManager(), "Instructions");
    }

    public static class AlertDialogs extends DialogFragment {
        ManualInstructionDialogBinding binding;//contains custom layout of AlertDialog
        AlertDialog.Builder builder;
        AlertDialog dialog;

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            setCancelable(false);//to set AlertDialog to non-cancelable when user click outside of AlertDialog window(with layout by default
            // empty layout)
            builder.setView(binding.getRoot());
            binding.headOfDialog.setText("Instructions");
            dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            binding.okayTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    ManualRechargeActivity.AlertDialogs.super.onDestroy();
                }
            });
            return dialog;
        }

        public AlertDialogs(ManualInstructionDialogBinding binding, AlertDialog.Builder builder) {
            this.binding = binding;
            this.builder = builder;
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.addRechargeLl.getVisibility() == View.VISIBLE) {
            binding.addRechargeLl.setVisibility(View.GONE);
            binding.doRecLy.setVisibility(View.VISIBLE);
        } else
            super.onBackPressed();
    }
}