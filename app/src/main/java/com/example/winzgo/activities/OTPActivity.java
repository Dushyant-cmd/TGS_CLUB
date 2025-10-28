package com.example.winzgo.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.example.winzgo.MainActivity;
import com.example.winzgo.R;
import com.example.winzgo.models.UserDocumentModel;
import com.example.winzgo.sharedpref.SessionSharedPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {

    //Firebase Authentication product or library java class in android
    private FirebaseAuth mAuth;

    //verificationId store verificationId returns from firebase auth when otp sent successfully
    private String mVerificationId;

    private PhoneAuthProvider.ForceResendingToken mResendToken;

    //TextView phone shows the mobile number
    TextView phoneTV;
    String phone;
    //PinView chaos library widget in which user enter otp
    PinView otp;
    //Login Button
    Button login;
    String TAG, referCode;
    ProgressDialog dialog;
    //TextView of resend otp
    TextView resendOTPTV;
    TextView timerTV;
    TextView resendOtpBtn;
    Thread t;
    FirebaseFirestore mFirestore;
    SessionSharedPref sharedPreferences;
    //below variable is used to store document in firestore users collection
    long balance, id;

    //This below interface variable store same interface Anonymous class instance which is get called for send, verify, failed otp through firebase
//    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpactivity);
        //hide action bar

        mAuth = FirebaseAuth.getInstance();//Firebase Auth library java class instance

        timerTV = findViewById(R.id.otpTimer);
        TAG = "OTPActivity.java";

        resendOTPTV = findViewById(R.id.resendOTPTV);
        resendOtpBtn = findViewById(R.id.resendOtp);

        //spannable change some part of text of TextView to black color
//        Spannable spannable = new SpannableString(text2);
        /**
         * @param ForegroundColorSpan what are changes
         * @param text.length() start index to span from text
         * @param text2.length() end index to span from text
         * @param flag is flag*/
//        spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.purple_700)), text.length(), (text2).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        resendOTPTV.setText(spannable, TextView.BufferType.SPANNABLE);

        phoneTV = (TextView) findViewById(R.id.phone2);
        Intent i = getIntent();
        phone = "+91 " + i.getStringExtra("phone");
        referCode = i.getStringExtra("referCode");
        phoneTV.setText(phone);
        phoneTV.setVisibility(View.VISIBLE);
        otp = findViewById(R.id.otp);
        login = findViewById(R.id.login);
        login.setEnabled(false);
        sharedPreferences = new SessionSharedPref(OTPActivity.this);
        dialog = new ProgressDialog(OTPActivity.this);
        mFirestore = FirebaseFirestore.getInstance();

        //set the TextWatcher interface instance on otp pinView
        otp.addTextChangedListener(mTextWatcher);
        //Send otp code of user number comes from previous activity by Intent
        if (isNetworkConnected()) {
            sendOtp(phone);
            resendCountFunction();
        } else {
            openAlertDialog1();
        }

        //set OnClickListener and verify otp when user click on login button
        //if the automatic sms detection did not work, user can also enter the code manually
        //so adding a click listener to the button
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = Objects.requireNonNull(otp.getText()).toString().trim();
                if (code.isEmpty() || code.length() < 6) {
                    otp.setError("Enter valid code");
                    otp.requestFocus();
                    return;
                }
                dialog.setMessage("Please wait...");
                //verifying the code entered manually
                verifyVerificationCode(code);
            }
        });

        resendOtpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resendOTPTV.setVisibility(View.GONE);
                resendOtpBtn.setVisibility(View.GONE);
                resendCountFunction();
                Toast.makeText(OTPActivity.this, "Resending OTP", Toast.LENGTH_SHORT).show();
                sendOtp(phone);
            }
        });
    }


    //TextWatcher interface instance using Anonymous class and Override and implement onTextChanged() method which is called by android every
    //time the otp pinView text input comes from user so we simply check condition if user filled full otp if yes then enable login button
    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            //Do some work before text changed
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            //Doing some work when text is changing
            if (otp.getText().toString().length() == 6) {
                login.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            //Do some work after text changed
        }
    };

    //resendCount() method is start a timer and when the timer is up do certain operation
    private void resendCountFunction() {
        timerTV.setVisibility(View.VISIBLE);
        t = new Thread() {

            public void run() {
                for (int i = 0; i < 30; i++) {
                    try {
                        final int a = i;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (a == 29) {
                                    timerTV.setVisibility(View.GONE);
                                    resendOTPTV.setVisibility(View.VISIBLE);
                                    resendOtpBtn.setVisibility(View.VISIBLE);
                                    t.interrupt();
                                }
                                timerTV.setText("Resend Code in " + (30 - (a + 1)));
                            }
                        });
                        //   System.out.println("Value of i= " + i);
                        sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();

    }

    private void sendOtp(String phone) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth).setPhoneNumber(phone)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        //onVerificationCompleted method called by android when the verification of otp is verified inside this method we get sms and auto
        //verify the user by calling
        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:" + credential);

            //Getting the code sent by SMS
            String code = credential.getSmsCode();
            Log.v("OTPActivity.java", code + "");
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            if (code != null) {
                dialog.setMessage("Wait while generating account");
                dialog.show();
                otp.setText(code);
//              verifying the code
                verifyVerificationCode(code);
            } else {
                login.setEnabled(true);
            }
        }

        //when verification of user otp is failed or send otp is failed
        @Override
        public void onVerificationFailed(FirebaseException e) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e);
            if (dialog.isShowing()) dialog.dismiss();

            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Toast.makeText(OTPActivity.this, "Invalid request", Toast.LENGTH_SHORT).show();
            } else if (e instanceof FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Toast.makeText(OTPActivity.this, "Otp limits exceeded", Toast.LENGTH_SHORT).show();
            }
        }

        //when firebase successfully sent otp to user phone number and return verification id of otp and token
        @Override
        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:" + verificationId);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            Toast.makeText(OTPActivity.this, "Otp sent on " + phone + " successfully", Toast.LENGTH_SHORT).show();
            // Save verification ID and resending token so we can use them later
            mVerificationId = verificationId;
            mResendToken = token;
        }
    };

    //This method is create PhoneAuthCredential class object by pass verification id and otp then call signInWithPhoneAuthCredential() method
    //and pass credential(PhoneAuthCredential class object) in order to sign-in user
    private void verifyVerificationCode(String code) {
        //Get credential of user enter otp like code and verificationId
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        //sign in user with phone auth credential on firebase using below method
        signInWithPhoneAuthCredential(credential);
    }

    //this method called signInWithCredential() method with credential input param in it on FirebaseAuth class instance.
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success");

//                            FirebaseUser user = task.getResult().getUser();
//                            assert user != null;
////                            Log.v(TAG, user.getPhoneNumber() + "");
                    if (isNetworkConnected()) {
                        isUserExist(phone);
                    } else {
                        openAlertDialog();
                    }
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(OTPActivity.this, "OTP is invalid", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void openAlertDialog1() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(OTPActivity.this);
        builder.setMessage("Check network connection!");
        builder.setCancelable(false);
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (isNetworkConnected()) {
                    Toast.makeText(getApplicationContext(), "Network connected successfully", Toast.LENGTH_SHORT).show();
                    sendOtp(phone);
                    resendCountFunction();
                } else {
                    openAlertDialog1();
                }
            }
        });
        builder.create().show();//create() method returns AlertDialog class object which is subclass of Dialog class and show() to display
        //AlertDialog
    }

    private void openAlertDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(OTPActivity.this);
        builder.setMessage("Check network connection!");
        builder.setCancelable(false);
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (isNetworkConnected()) {
                    isUserExist(phone);
                    Toast.makeText(getApplicationContext(), "Network connected successfully", Toast.LENGTH_SHORT).show();
                } else {
                    openAlertDialog();
                }
            }
        });
        builder.create().show();//create() method returns AlertDialog class object which is subclass of Dialog class and show() to display
        //AlertDialog
    }

    private void isUserExist(String phone) {
        dialog.show();
        mFirestore.collection("users").whereEqualTo("mobile", phone).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    //user exist
                    //user document in DocumentSnapshot class variable.
                    DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                    UserDocumentModel userDoc = doc.toObject(UserDocumentModel.class);
                    //login status to true in SP file
                    sharedPreferences.setLoginStatus(true);
                    //save user document field in SharedPreferences file except bankDetails key value
                    assert userDoc != null;
                    sharedPreferences.setBalance(userDoc.getBalance());
                    sharedPreferences.setId(userDoc.getId());
                    sharedPreferences.setMobile(userDoc.getMobile());
                    sharedPreferences.setName(userDoc.getName());
                    sharedPreferences.setRefer(userDoc.getRefer());
                    Toast.makeText(OTPActivity.this, "Old users can't redeem referral code!", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(OTPActivity.this, MainActivity.class);
                    i.putExtra("phone", phone);//with +91 code in phone variable.
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                    dialog.dismiss();
                } else if (task.isSuccessful()) {
                    try {
                        //user does not exist
                        //read request for constants collection 1(document) signUpBonus field value.
                        mFirestore.collection("constants").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    //constants collection 1 document get from task object
                                    DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                    Object balance1 = document.get("signup_bonus", Long.class);
                                    //get the refer_bonus amount from firestore of firebase by make a Query or read request for one document.
                                    mFirestore.collection("constants").document("2").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot doc = task.getResult();
                                                long referBonus = doc.getLong("refer_bonus");
                                                //read request on firestore for ids(collection), user_id(document), field(id)
                                                mFirestore.collection("ids").document("user_id").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            //document that contains user id
                                                            DocumentSnapshot document = task.getResult();
                                                            Object id1 = document.get("id");
                                                            id = Long.parseLong(String.valueOf(id1)) + 1;
                                                            //update id field of ids collection 2nd index document with id global variable
                                                            HashMap<String, Object> map = new HashMap<>();
                                                            map.put("id", id);
                                                            mFirestore.collection("ids").document("user_id").update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Log.v(TAG, "success");
                                                                    balance = Long.parseLong(String.valueOf(balance1));//store balance object in long type variable
                                                                    id = Long.parseLong(String.valueOf(id1)) + 1;//store id object in long type variable.
                                                                    String mobile = phone;
                                                                    String name = "Guest_" + id;
                                                                    String refer = "0";
                                                                    //write request to store users collection user document
                                                                    HashMap<String, Object> map = new HashMap<>();
                                                                    map.put("balance", balance);
                                                                    map.put("id", id);
                                                                    map.put("bankDetails", null);
                                                                    map.put("mobile", mobile);
                                                                    map.put("name", name);
                                                                    map.put("refer", refer);
                                                                    Log.v(TAG, id1 + "");
                                                                    Log.v(TAG, "id: " + id);
                                                                    mFirestore.collection("users").document(String.valueOf(id)).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void unused) {
                                                                            //store or write an document in the promotion collection of firestore which contain HashMap as below
                                                                            /**
                                                                             * @param isRecharged false type value for is user recharged
                                                                             * @param referralId referCode that user entered and we have in Extra value containing variable.
                                                                             * @param user_id user id of which is sign up
                                                                             * @param name user name of which is sign up*/
                                                                            HashMap<String, Object> map = new HashMap<>();
                                                                            map.put("isRecharged", false);
                                                                            map.put("referralId", referCode);
                                                                            map.put("user_id", id);
                                                                            map.put("name", name);
                                                                            //firebase query for write operation or add document in collection.
                                                                            mFirestore.collection("promotion").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                                @Override
                                                                                public void onSuccess(DocumentReference documentReference) {
                                                                                    //update referral code user balance with balance + referBonus and refer with refer + referBonus
                                                                                    if(!referCode.isEmpty()) {
                                                                                        mFirestore.collection("users").whereEqualTo("id", Long.parseLong(referCode)).get()
                                                                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                                        if(task.isSuccessful() && !task.getResult().isEmpty()) {
                                                                                                            try {
                                                                                                                DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                                                                                                                long balance = doc.getLong("balance");
                                                                                                                String referAmt = doc.getString("refer");
                                                                                                                long newBal = balance + referBonus;
                                                                                                                if(referAmt.isEmpty()) {
                                                                                                                    referAmt = "0";
                                                                                                                }
                                                                                                                long newReferAmt = Long.parseLong(referAmt) + referBonus;
                                                                                                                HashMap<String, Object> map = new HashMap<>();
                                                                                                                map.put("balance", newBal);
                                                                                                                map.put("refer", String.valueOf(newReferAmt));
                                                                                                                mFirestore.collection("users").document(referCode).update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onSuccess(Void unused) {
                                                                                                                        Log.v("OTPActivity.java", "success");
                                                                                                                    }
                                                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                                                                    @Override
                                                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                                                        Log.v("OTPActivity.java", e + "");
                                                                                                                    }
                                                                                                                });
                                                                                                            } catch (Exception e) {

                                                                                                            }
                                                                                                        } else if(task.isSuccessful()) {
                                                                                                            Toast.makeText(OTPActivity.this, "Referral code is not valid.", Toast.LENGTH_SHORT).show();
                                                                                                        } else {
                                                                                                            Log.v("OTPActivity.java", task.getException() + "");
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                    dialog.dismiss();
                                                                                    //login status to true in SP file
                                                                                    sharedPreferences.setLoginStatus(true);
                                                                                    //save user document field in SharedPreferences file except bankDetails key value
                                                                                    sharedPreferences.setBalance(balance);
                                                                                    sharedPreferences.setId(id);
                                                                                    sharedPreferences.setMobile(mobile);
                                                                                    sharedPreferences.setName(name);
                                                                                    sharedPreferences.setRefer(refer);
                                                                                    Intent i = new Intent(OTPActivity.this, MainActivity.class);
                                                                                    i.putExtra("phone", phone);//with +91 code
                                                                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                    startActivity(i);
                                                                                    finish();
                                                                                    Toast.makeText(OTPActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    Log.v("OTPActivity.java", e + "");
                                                                                    Toast.makeText(OTPActivity.this, e.getMessage() + "", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Log.v(TAG, e.getMessage());
                                                                        }
                                                                    });
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.v(TAG, e.getMessage() + "");
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            } else {
                                                Log.v("OTPActivity.java", task.getException() + "");
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    } catch (Exception e) {
                        Log.v("OTPActivity.java", e + "");
                    }
                } else {
                    //read operation on firestore is not completed successfully means there is exception like network or auth problem.
                }
            }
        });
    }

    //check mobile device is connected to network or not.
    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
        return connected;
    }
}