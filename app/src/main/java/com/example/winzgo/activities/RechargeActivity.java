package com.example.winzgo.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.winzgo.R;
import com.example.winzgo.sharedpref.SessionSharedPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class RechargeActivity extends AppCompatActivity {
    FirebaseFirestore firestore;
    SessionSharedPref sharedPreferences;
    String rechargeAmt;
    String tag = "RechargeActivity.java";
    final int UPI_PAYMENT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge);
        firestore = FirebaseFirestore.getInstance();
        sharedPreferences = new SessionSharedPref(RechargeActivity.this);
        rechargeAmt = getIntent().getStringExtra("rechargeAmt");

        if (isNetworkConnected()) {
            queries();
        } else {
            openAlertDialog();
        }
    }

    public void  queries() {
        firestore.collection("appData").document("lowerSetting").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            try {
                                //Upi transaction with Intent
                                payUsingUpi(rechargeAmt, task.getResult().getString("upi"), "TGS club", "TGS club corporation");
                                //Upi transaction with EasyUpiPayment library.
//                                makePayment(Double.parseDouble(rechargeAmt), task.getResult().getString("upi"), "TGS club", "winzgo corporation", "123");
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "No Upi App is Found", Toast.LENGTH_LONG).show();
                                Log.v(tag, e + "");
                                onBackPressed();
                            }
                        } else {
                            Log.v(tag, task.getException() + "");
                        }
                    }
                });
    }

    public void openAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Check Internet Connection");
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (isNetworkConnected()) {
                    queries();
                } else {
                    openAlertDialog();
                }
            }
        });
        builder.create().show();
    }

    public void payUsingUpi(String amount, String upiId, String name, String note) {

        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", name)
                .appendQueryParameter("tn", note)
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                .build();


        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        // will always show a dialog to user to choose an app
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");

        // check if intent resolves
        if(null != chooser.resolveActivity(getPackageManager())) {
            startActivityForResult(chooser, UPI_PAYMENT);
        } else {
            Toast.makeText(this,"No UPI app found, please install one to continue",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case UPI_PAYMENT:
                Log.v(tag, resultCode + "");
                if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                    if (data != null) {
                        String trxt = data.getStringExtra("response");
                        //Log.d("UPI", "onActivityResult: " + trxt);
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add(trxt);
                        upiPaymentDataOperation(dataList);
                    } else {
                        //Log.d("UPI", "onActivityResult: " + "Return data is null");
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add("nothing");
                        upiPaymentDataOperation(dataList);
                    }
                } else {
                    //Log.d("UPI", "onActivityResult: " + "Return data is null"); //when user simply back without payment
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    upiPaymentDataOperation(dataList);
                }
                break;
        }
    }

    private void upiPaymentDataOperation(ArrayList<String> data) {
        if (isNetworkConnected()) {
            //result of returned launched Activity
            String str = data.get(0);
            Log.d(tag, "upiPaymentDataOperation: "+str);
            String paymentCancel = "";
            if(str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String response[] = str.split("&");
            for (int i = 0; i < response.length; i++) {
                String equalStr[] = response[i].split("=");
                if(equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    }
                    else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    }
                }
                else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }

            if (status.equals("success")) {
                //Code to handle successful transaction here.
                Toast.makeText(RechargeActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                // on below line we are getting details about transaction when completed.
                    String transactionDet = "done";
                    String transactionAmt = rechargeAmt;
                    //get current date and time
                    Calendar cal = Calendar.getInstance();
                    Date date = cal.getTime();
                    SimpleDateFormat spf = new SimpleDateFormat("dd-MM-yyyy, hh:mm:ss a", Locale.getDefault());
                    String dateAndTime = spf.format(date);
                    Log.v(tag, dateAndTime);
                    if (transactionAmt != null) {
                        long currentUserBalance = sharedPreferences.getBalance();
                        long updatedBalance = currentUserBalance + Long.parseLong(transactionAmt);
                        //Map interface subclass instance contains value object in key-value pair where key must be a String.
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("balance", updatedBalance);
                        firestore.collection("users").document(sharedPreferences.getId() + "").update(map)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        sharedPreferences.setBalance(updatedBalance);
                                        HashMap<String, Object> map = new HashMap<>();
                                        map.put("amount", transactionAmt);
                                        map.put("date", dateAndTime);
                                        map.put("info", null);
                                        map.put("name", sharedPreferences.getName());
                                        map.put("status", "completed");
                                        map.put("type", "recharge");
                                        map.put("timestamp", System.currentTimeMillis());
                                        map.put("user_id", sharedPreferences.getId());
                                        firestore.collection("transactions").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                firestore.collection("ids").document("balance").get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    DocumentSnapshot doc = task.getResult();
                                                                    long currentBalance = doc.getLong("amount");
                                                                    long updatedTotAmt = currentBalance + Long.parseLong(transactionAmt);
                                                                    HashMap<String, Object> map = new HashMap<>();
                                                                    map.put("amount", updatedTotAmt);
                                                                    firestore.collection("ids").document("balance").update(map)
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void unused) {
                                                                                    Log.v(tag, "success");
                                                                                    Toast.makeText(RechargeActivity.this, "Transaction done successfully", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Log.v(tag, e + "");
                                                                                }
                                                                            });

                                                                    //update isRecharged field of refer document to true
                                                                    firestore.collection("promotion").whereEqualTo("user_id",
                                                                            sharedPreferences.getId()).get()
                                                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                    if(task.isSuccessful() && !task.getResult().isEmpty()) {
                                                                                        //there is document
                                                                                        HashMap<String, Object> map2 = new HashMap<>();
                                                                                        map.put("isRecharged", true);
                                                                                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                                                                                        firestore.collection("promotion").document(doc.getId()).update(map2)
                                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onSuccess(Void unused) {
                                                                                                        Log.v(tag, "success");
                                                                                                    }
                                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                                                    @Override
                                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                                        Log.v(tag, e + "");
                                                                                                    }
                                                                                                });
                                                                                    } else if(task.isSuccessful()) {
                                                                                        //there is no document
                                                                                    } else {
                                                                                        //there is error
                                                                                        Log.v(tag, task.getException() + "");
                                                                                    }
                                                                                }
                                                                            });

                                                                } else {
                                                                    Log.v(tag, task.getException() + "");
                                                                }
                                                            }
                                                        });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.v(tag, e + "");
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.v(tag, e + "");
                                    }
                                });
                    }
                    Log.v(tag, transactionAmt);
                    Log.v(tag, transactionDet);
//                    onBackPressed();
                // Log.d("UPI", "responseStr: "+approvalRefNo);
//                Toast.makeText(this, "YOUR ORDER HAS BEEN PLACED\n THANK YOU AND ORDER AGAIN", Toast.LENGTH_LONG).show();
            }
            else if("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(RechargeActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(RechargeActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(RechargeActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }

        onBackPressed();
    }

    //check mobile device is connected to network or not.
    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
        return connected;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

//    private void makePayment(double amountDouble, String upi, String name, String desc, String transactionId) throws AppNotFoundException {
//        String rechargeAmount = String.valueOf(amountDouble);//because when build EasyUpiPayment it only allow type of String
//        // on below line we are calling an easy payment method and passing
//        // all parameters to it such as upi id,name, description and others.
//        //PaymentApp.ALL constant is to show user all the upi app
//        PaymentApp paymentApp = PaymentApp.ALL;
//        //Initialization of EasyUpiPayment
//        EasyUpiPayment.Builder builder = new EasyUpiPayment.Builder(this)
//                .with(paymentApp)
//                .setPayeeVpa(upi)
//                .setPayeeName(name)
//                .setTransactionId(transactionId)
//                .setTransactionRefId("transactionRefId")
//                .setPayeeMerchantCode("21222")
//                .setDescription(desc)
//                .setAmount(rechargeAmount);
//
//        try {
//            //On below line we are getting EasyUpiPayment class instance from Builder of it.
//            EasyUpiPayment easyUpiPayment = builder.build();
//            easyUpiPayment.startPayment();
//            //On below line we are setting listener for callback of Library upi app transaction completion or cancellation.
//            easyUpiPayment.setPaymentStatusListener(new PaymentStatusListener() {
//                @Override
//                public void onTransactionCompleted(@NonNull TransactionDetails transactionDetails) {
//                    // on below line we are getting details about transaction when completed.
//                    String transactionDet = transactionDetails.getTransactionStatus().toString() + "\n" + "Transaction ID : " + transactionDetails.getTransactionId();
//                    String transactionAmt = transactionDetails.getAmount();
//                    //get current date and time
//                    Calendar cal = Calendar.getInstance();
//                    Date date = cal.getTime();
//                    SimpleDateFormat spf = new SimpleDateFormat("dd-MM-yyyy, hh:mm:ss a", Locale.getDefault());
//                    String dateAndTime = spf.format(date);
//                    Log.v(tag, dateAndTime);
//                    if (transactionAmt != null) {
//                        long currentUserBalance = sharedPreferences.getBalance();
//                        long updatedBalance = currentUserBalance + Long.parseLong(transactionAmt);
//                        //Map interface subclass instance contains value object in key-value pair where key must be a String.
//                        HashMap<String, Object> map = new HashMap<>();
//                        map.put("balance", updatedBalance);
//                        firestore.collection("users").document(sharedPreferences.getId() + "").update(map)
//                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void unused) {
//                                        sharedPreferences.setBalance(updatedBalance);
//                                        HashMap<String, Object> map = new HashMap<>();
//                                        map.put("amount", transactionAmt);
//                                        map.put("date", dateAndTime);
//                                        map.put("info", null);
//                                        map.put("name", sharedPreferences.getName());
//                                        map.put("status", "completed");
//                                        map.put("type", "recharge");
//                                        map.put("user_id", sharedPreferences.getId());
//                                        firestore.collection("transactions").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                                            @Override
//                                            public void onSuccess(DocumentReference documentReference) {
//                                                firestore.collection("ids").document("balance").get()
//                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                                            @Override
//                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                                                if (task.isSuccessful()) {
//                                                                    DocumentSnapshot doc = task.getResult();
//                                                                    long currentBalance = doc.getLong("amount");
//                                                                    long updatedTotAmt = currentBalance + Long.parseLong(transactionAmt);
//                                                                    HashMap<String, Object> map = new HashMap<>();
//                                                                    map.put("amount", updatedTotAmt);
//                                                                    firestore.collection("ids").document("balance").update(map)
//                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                                                @Override
//                                                                                public void onSuccess(Void unused) {
//                                                                                    Log.v(tag, "success");
//                                                                                    Toast.makeText(RechargeActivity.this, "Transaction done successfully", Toast.LENGTH_SHORT).show();
//                                                                                }
//                                                                            }).addOnFailureListener(new OnFailureListener() {
//                                                                                @Override
//                                                                                public void onFailure(@NonNull Exception e) {
//                                                                                    Log.v(tag, e + "");
//                                                                                }
//                                                                            });
//                                                                } else {
//                                                                    Log.v(tag, task.getException() + "");
//                                                                }
//                                                            }
//                                                        });
//                                            }
//                                        }).addOnFailureListener(new OnFailureListener() {
//                                            @Override
//                                            public void onFailure(@NonNull Exception e) {
//                                                Log.v(tag, e + "");
//                                            }
//                                        });
//                                    }
//                                }).addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        Log.v(tag, e + "");
//                                    }
//                                });
//                    }
//                    Log.v(tag, transactionAmt);
//                    Log.v(tag, transactionDet);
//                    onBackPressed();
//                }
//
//                @Override
//                public void onTransactionCancelled() {
//                    // this method is called when transaction is cancelled.
//                    Toast.makeText(RechargeActivity.this, "Transaction cancelled..", Toast.LENGTH_SHORT).show();
//                    onBackPressed();
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(RechargeActivity.this, "Try again later server is down.", Toast.LENGTH_SHORT).show();
//            onBackPressed();
//            Log.v("RechargeActivity.java", e + "");
//        }
//    }
}