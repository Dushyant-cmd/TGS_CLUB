package com.example.winzgo.fragments.recharge;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.winzgo.MainActivity;
import com.example.winzgo.MyApplication;
import com.example.winzgo.R;
import com.example.winzgo.activities.ManualRechargeActivity;
import com.example.winzgo.adapter.UpiAdapter;
import com.example.winzgo.databinding.FragmentCoinTradeDepositBinding;
import com.example.winzgo.databinding.ManualInstructionDialogBinding;
import com.example.winzgo.models.UpiAdapterModel;
import com.example.winzgo.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CoinTradeDepositFragment extends Fragment {
    private MainActivity hostAct;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;
    private FragmentCoinTradeDepositBinding binding;
    private String walletAddress = "";
    private MyApplication application;
    private long type = 0; // 0 trade, 1 coin, 2 win-go

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_coin_trade_deposit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding = FragmentCoinTradeDepositBinding.bind(view);
        hostAct = (MainActivity) requireActivity();
        application = (MyApplication) getActivity().getApplication();
        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        type = getArguments().getLong("type");
        hostAct.setupHeader("Deposit");

        showTermsAlertDialog();
        getTransactionDetails();
        setListeners();
    }

    private void setListeners() {
        binding.ivCopyWalletAdd.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied Upi", walletAddress);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), walletAddress + " is Copied", Toast.LENGTH_SHORT).show();

            binding.ivCopyWalletAdd.setImageResource(R.drawable.checked);
        });

        binding.btnBet50.setOnClickListener(v -> {
            highlightBetAmtLayout(50);
        });

        binding.btnBet100.setOnClickListener(v -> {
            highlightBetAmtLayout(100);
        });

        binding.btnBet500.setOnClickListener(v -> {
            highlightBetAmtLayout(500);
        });

        binding.btnBet1000.setOnClickListener(v -> {
            highlightBetAmtLayout(1000);
        });

        binding.btnAddRecharge.setOnClickListener(v -> {
            addRecharge();
        });
    }

    private void addRecharge() {
        String id = binding.etUtr.getText().toString();
        String walletAddress = binding.etWalletAddress.getText().toString();
        String amount = binding.etAmount.getText().toString();
        if (!amount.isEmpty()) {
            if (id.length() > 4 && Long.parseLong(amount) > 0) {
                ProgressDialog dialog1 = new ProgressDialog(getContext());
                dialog1.setMessage("Please wait...");
                dialog1.show();
                binding.btnAddRecharge.setEnabled(false);
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
                map.put("gameType", type);
                map.put("walletAddress", walletAddress);
                application.firestore.collection("transactions").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        dialog1.dismiss();
                        Toast.makeText(application, "Recharge Added Successfully", Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().popBackStackImmediate();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        binding.btnAddRecharge.setEnabled(true);
                        dialog1.dismiss();
                        Log.v("Manual.java", e + "");
                    }
                });
            } else if (id.length() <= 4) {
                binding.etUtr.setError("Transaction Id Required");
                binding.etUtr.requestFocus();
            } else {
                binding.etAmount.setError("Must be greater than 0");
                binding.etAmount.requestFocus();
            }
        } else {
            binding.etAmount.setError("Recharge Amount Required");
            binding.etAmount.requestFocus();
        }
    }

    private void highlightBetAmtLayout(long amount) {
        int count = binding.betAmtLy.getChildCount();
        for (int i = 0; i < count; i++) {
            View item = binding.betAmtLy.getChildAt(i);
            if (item instanceof TextView) {
                TextView tv = (TextView) item;
                long tvAmt = Long.parseLong(tv.getText().toString().substring(1));
                if (tvAmt == amount) {
                    binding.etAmount.setText(String.valueOf(amount));
                    tv.setBackgroundResource(R.drawable.little_dark_violet_rect);
                } else {
                    tv.setBackgroundResource(R.drawable.light_silver_bg);
                }
            }
        }
    }

    private void getTransactionDetails() {
        Dialog dialog = Constants.showProgressDialog(requireContext());
        firestore.collection("appData").document("lowerSetting")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        dialog.dismiss();
                        if (task.isSuccessful()) {
                            List<UpiAdapterModel> list = new ArrayList<>();
                            String upi = task.getResult().getString("manualUpi");
                            walletAddress = task.getResult().getString("walletAddress");
                            String[] str = upi.split(",");
                            for (String s : str) {
                                UpiAdapterModel model = new UpiAdapterModel();
                                model.setUpiId(s);
                                model.setCopied(false);
                                list.add(model);
                            }

                            binding.tvWalletAddress.setText(walletAddress);
                            UpiAdapter adapter = new UpiAdapter(getContext(), list);
                            binding.upiRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            binding.upiRecyclerView.setAdapter(adapter);
                        }
                    }
                });

        StorageReference storageReference = firebaseStorage.getReference();
        StorageReference ref = storageReference.child("images/upiqr.jpeg");
        ref.getDownloadUrl().addOnSuccessListener(uri -> {
            String url = uri.toString();
            Glide.with(getContext()).load(url).placeholder(R.drawable.upiqr).into(binding.ivQrCode);
        }).addOnFailureListener(e -> Log.d("Manual.java", "onFailure: " + e));
    }

    private void showTermsAlertDialog() {
        ManualInstructionDialogBinding termsBinding = ManualInstructionDialogBinding.inflate(getLayoutInflater(), binding.getRoot(), false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(false);
        ManualRechargeActivity.AlertDialogs dialog = new ManualRechargeActivity.AlertDialogs(termsBinding, builder);
        dialog.show(getActivity().getSupportFragmentManager(), "Instructions");
    }
}