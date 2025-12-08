package com.example.winzgo.fragments.withdrawal;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.winzgo.MainActivity;
import com.example.winzgo.R;
import com.example.winzgo.databinding.FragmentCoinTradeWithdrawalBinding;
import com.example.winzgo.fragments.AddBankDetailsFragment;
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
import java.util.HashMap;
import java.util.Locale;

public class CoinTradeWithdrawalFragment extends Fragment {
    private FragmentCoinTradeWithdrawalBinding binding;
    private MainActivity hostAct;
    private FirebaseFirestore firestore;
    private long type = Constants.TRADE_TYPE, balance = 0L, userId = 0L;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_coin_trade_withdrawal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding = FragmentCoinTradeWithdrawalBinding.bind(view);
        hostAct = (MainActivity) requireActivity();
        firestore = FirebaseFirestore.getInstance();
        userId = SessionSharedPref.getLong(getContext(), Constants.USER_ID_KEY, 0L);
        type = getArguments().getInt("type");

        balance = SessionSharedPref.getLong(getContext(), Constants.WIN_GO_BALANCE_KEY, 0);
        if (type == Constants.TRADE_TYPE) {
            balance = SessionSharedPref.getLong(getContext(), Constants.TRADE_PRO_BALANCE_KEY, 0L);
        } else if (type == Constants.COIN_TYPE) {
            balance = SessionSharedPref.getLong(getContext(), Constants.COIN_BALANCE_KEY, 0L);
        }

        hostAct.setupHeader("Withdrawal");
        setListeners();
    }

    private void setListeners() {
        binding.btnBankDetails.setOnClickListener(v -> {
            hostAct.loadFragment(new AddBankDetailsFragment(), true, "Bank Details");
        });

        binding.btnSubmit.setOnClickListener(v -> {
            String withdrawAmount = binding.etAmount.getText().toString();
            if (withdrawAmount.isEmpty()) {
                Constants.showEditTextError(binding.etAmount, "Required");
            } else if (Long.parseLong(withdrawAmount) > balance) {
                Toast.makeText(getActivity(), "Oops! Balance is too low..", Toast.LENGTH_SHORT).show();
            } else if (Long.parseLong(withdrawAmount) < 200) {
                Toast.makeText(getActivity(), "Minimum withdrawal amount must be 200", Toast.LENGTH_SHORT).show();
            } else {
                Dialog progressDialog1 = Constants.showProgressDialog(getContext());
                firestore.collection("users").document(String.valueOf(userId))
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot doc) {
                                progressDialog1.dismiss();
                                UserDocumentModel userDoc = doc.toObject(UserDocumentModel.class);
                                if (userDoc.getBankDetails() != null) {
                                    withdraw(withdrawAmount);
                                } else {
                                    Toast.makeText(getActivity(), "No bank details found", Toast.LENGTH_SHORT).show();
                                    hostAct.loadFragment(new AddBankDetailsFragment(), true, "Bank Details");
                                }
                            }
                        });
            }
        });

        binding.cancelBtn.setOnClickListener(v -> {
            hostAct.popCurrent();
        });
    }

    private void withdraw(String withdrawAmount) {
        long currentUserBalance = balance;
        long updatedBalance = currentUserBalance - Long.parseLong(withdrawAmount);
        String walletAddress = binding.etWalletAdd.getText().toString();

        Dialog progressDialog1 = Constants.showProgressDialog(requireContext());
        HashMap<String, Object> map = new HashMap<>();
        map.put(Constants.WIN_GO_BALANCE_KEY, updatedBalance);
        if (type == Constants.TRADE_TYPE) {
            map.put(Constants.TRADE_PRO_BALANCE_KEY, updatedBalance);
        } else if (type == Constants.COIN_TYPE)
            map.put(Constants.COIN_BALANCE_KEY, updatedBalance);

        firestore.collection("users").document(String.valueOf(userId)).update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Calendar cal = Calendar.getInstance();
                        SimpleDateFormat spf = new SimpleDateFormat("dd-MM-yyyy, hh:mm:ss a", Locale.getDefault());
                        SimpleDateFormat simpleDF = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        String dateAndTime = spf.format(cal.getTime());
                        String todayDate = simpleDF.format(cal.getTime());

                        String name = SessionSharedPref.getStr(getContext(), Constants.NAME_KEY, "");

                        HashMap<String, Object> map = new HashMap<>();
                        map.put("todayDate", todayDate);
                        map.put("amount", withdrawAmount);
                        map.put("date", dateAndTime);
                        map.put("info", null);
                        map.put("name", name);
                        map.put("status", "pending");
                        map.put("type", "withdraw");
                        map.put("timestamp", System.currentTimeMillis());
                        map.put("user_id", userId);
                        map.put("gameType", type);
                        map.put("walletAddress", walletAddress);
                        firestore.collection("transactions").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                progressDialog1.dismiss();
                                Constants.updateBalance(getActivity(), Long.parseLong(withdrawAmount), false, Constants.TRADE_PRO_BALANCE_KEY, () -> {
                                    if (type == Constants.TRADE_TYPE) {
                                        SessionSharedPref.setLong(getContext(), Constants.TRADE_PRO_BALANCE_KEY, updatedBalance);
                                    } else if (type == Constants.COIN_TYPE) {
                                        SessionSharedPref.setLong(getContext(), Constants.COIN_BALANCE_KEY, updatedBalance);
                                    }

                                    Toast.makeText(getActivity(), "Withdrawal done successfully", Toast.LENGTH_SHORT).show();
                                    hostAct.popCurrent();
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                                progressDialog1.dismiss();
                                Toast.makeText(getActivity(), e.getMessage() + "", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                        progressDialog1.dismiss();
                        Toast.makeText(getActivity(), e.getMessage() + "", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}