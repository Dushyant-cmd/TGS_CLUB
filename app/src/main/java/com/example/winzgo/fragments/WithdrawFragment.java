package com.example.winzgo.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.winzgo.MainActivity;
import com.example.winzgo.databinding.FragmentWithdrawBinding;
import com.example.winzgo.models.UserDocumentModel;
import com.example.winzgo.sharedpref.SessionSharedPref;
import com.example.winzgo.utils.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class WithdrawFragment extends Fragment {
    private FragmentWithdrawBinding binding;
    private FirebaseFirestore firestore;
    private SessionSharedPref sharedPreferences;
    private String tag = "withdrawal.java";
    private MainActivity hostAct;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWithdrawBinding.inflate(inflater, container, false);
        firestore = FirebaseFirestore.getInstance();
        sharedPreferences = new SessionSharedPref(requireContext());
        hostAct = (MainActivity) requireActivity();

        setListeners();
        return binding.getRoot();
    }

    private void setListeners() {
        binding.upBtn.setOnClickListener(v -> {
            hostAct.popCurrent();
        });

        binding.submitWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String accountNum = binding.accNumBank.getText().toString();
                String bankHolderName1 = binding.nameHolderBank.getText().toString();
                String bankName1 = binding.nameBank.getText().toString();
                String ifscCode1 = binding.ifscBank.getText().toString();

                boolean isCheck = binding.terms.isChecked();
                if (isCheck) {
                    String withdrawAmount = binding.withdrawAmt.getText().toString();
                    if (withdrawAmount.isEmpty()) {
                        Toast.makeText(getActivity(), "Please Fill All Details", Toast.LENGTH_SHORT).show();
                    } else if (Long.parseLong(withdrawAmount) > sharedPreferences.getBalance()) {
                        Toast.makeText(getActivity(), "Oops! Balance is too low..", Toast.LENGTH_SHORT).show();
                    } else if (Long.parseLong(withdrawAmount) < 200) {
                        Toast.makeText(getActivity(), "Minimum withdrawal amount must be 200", Toast.LENGTH_SHORT).show();
                    } else {
                        ProgressDialog progressDialog1 = new ProgressDialog(getActivity());
                        progressDialog1.setMessage("Please wait...");
                        progressDialog1.show();
                        firestore.collection("users").document(String.valueOf(sharedPreferences.getId()))
                                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot doc) {
                                        progressDialog1.dismiss();
                                        UserDocumentModel userDoc = doc.toObject(UserDocumentModel.class);
                                        if (userDoc.getBankDetails() == null) {
                                            if (!accountNum.isEmpty() && !bankHolderName1.isEmpty() && !bankName1.isEmpty() && !ifscCode1.isEmpty()) {
                                                withdraw(withdrawAmount, true, accountNum, bankHolderName1, bankName1, ifscCode1);
                                            } else {
                                                Toast.makeText(getActivity(), "Please Fill All Details", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            withdraw(withdrawAmount, false, accountNum, bankHolderName1, bankName1, ifscCode1);
                                        }
                                    }
                                });
                    }
                } else {
                    Toast.makeText(requireContext(), "Please check terms", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void withdraw(String withdrawAmount, boolean isAddBankDetails, String accountNum, String bankHolderName1, String bankName1, String ifscCode1) {
        Dialog progressDialog1 = Constants.showProgressDialog(requireContext());
        Calendar cal1 = Calendar.getInstance();
        Date d = cal1.getTime();
        SimpleDateFormat spf1 = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        long currentUserBalance = sharedPreferences.getBalance();
        long updatedBalance = currentUserBalance - Long.parseLong(withdrawAmount);
        //Map interface subclass instance contains value object in key-value pair where key must be a String.
        HashMap<String, Object> map = new HashMap<>();
        map.put("balance", updatedBalance);
        firestore.collection("users").document(sharedPreferences.getId() + "").update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Calendar cal = Calendar.getInstance();
                        SimpleDateFormat spf = new SimpleDateFormat("dd-MM-yyyy, hh:mm:ss a", Locale.getDefault());
                        SimpleDateFormat simpleDF = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        String dateAndTime = spf.format(cal.getTime());
                        String todayDate = simpleDF.format(cal.getTime());
                        sharedPreferences.setBalance(updatedBalance);
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("todayDate", todayDate);
                        map.put("amount", withdrawAmount);
                        map.put("date", dateAndTime);
                        map.put("info", null);
                        map.put("name", sharedPreferences.getName());
                        map.put("status", "pending");
                        map.put("type", "withdraw");
                        map.put("timestamp", System.currentTimeMillis());
                        map.put("user_id", sharedPreferences.getId());
                        firestore.collection("transactions").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                if (!getActivity().isFinishing()) {
                                    Toast.makeText(getActivity(), "Withdrawal done successfully", Toast.LENGTH_SHORT).show();
                                }
                                sharedPreferences.setBalance(updatedBalance);
                                progressDialog1.dismiss();

                                if (isAddBankDetails) {
                                    // add bank details
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("account number", accountNum);
                                    map.put("bank holder name", bankHolderName1);
                                    map.put("bank name", bankName1);
                                    map.put("ifsc code", ifscCode1);

                                    HashMap<String, Object> map1 = new HashMap<>();
                                    map1.put("bankDetails", map);

                                    Dialog dialog = Constants.showProgressDialog(requireContext());
                                    firestore.collection("users").document(sharedPreferences.getId() + "").update(map1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            dialog.dismiss();
                                            hostAct.popCurrent();
                                            try {
                                                Toast.makeText(getActivity(), "Bank Details Added Successfully", Toast.LENGTH_SHORT).show();
                                            } catch (
                                                    Exception e) {
                                                Log.v(tag, e + "");
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            dialog.dismiss();
                                            hostAct.popCurrent();
                                            try {
                                                Log.v("MoneyFragment.java", e + "");
                                                Toast.makeText(getActivity(), "Failed\nCheck Network Connection!", Toast.LENGTH_SHORT).show();
                                            } catch (
                                                    Exception e1) {
                                                Log.v(tag, e1 + "");
                                            }
                                        }
                                    });
                                } else
                                    hostAct.popCurrent();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.v(tag, e + "");
                                progressDialog1.dismiss();
                                Toast.makeText(getActivity(), e.getMessage() + "", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v(tag, e + "");
                        progressDialog1.dismiss();
                        Toast.makeText(getActivity(), e.getMessage() + "", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}