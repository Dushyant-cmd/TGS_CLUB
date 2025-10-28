package com.example.winzgo.fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.winzgo.MainActivity;
import com.example.winzgo.R;
import com.example.winzgo.databinding.FragmentAddBankDetailsBinding;
import com.example.winzgo.databinding.FragmentHomeBinding;
import com.example.winzgo.sharedpref.SessionSharedPref;
import com.example.winzgo.utils.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class AddBankDetailsFragment extends Fragment {
    private FragmentAddBankDetailsBinding binding;
    private FirebaseFirestore firestore;
    private SessionSharedPref sharedPreferences;
    private String tag = "AddBankDetailsFragment.java";
    private MainActivity hostAct;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddBankDetailsBinding.inflate(inflater, container, false);
        firestore = FirebaseFirestore.getInstance();
        sharedPreferences = new SessionSharedPref(requireContext());
        hostAct = (MainActivity) requireActivity();

        setListeners();
        return binding.getRoot();
    }

    private void setListeners() {
        binding.submitBank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String accountNum = binding.accNumBank.getText().toString();
                String bankHolderName1 = binding.nameHolderBank.getText().toString();
                String bankName1 = binding.nameBank.getText().toString();
                String ifscCode1 = binding.ifscBank.getText().toString();
                String upiId1 = binding.upiBank.getText().toString();

                boolean isCheck = binding.terms.isChecked();
                if(isCheck) {
                    if (!accountNum.equals("") && !bankHolderName1.equals("") && !bankName1.equals("") && !ifscCode1.equals("") && !upiId1.equals("")) {
                        Toast.makeText(getActivity(), "Bank Details Saved Successfully", Toast.LENGTH_SHORT).show();
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("account number", accountNum);
                        map.put("bank holder name", bankHolderName1);
                        map.put("bank name", bankName1);
                        map.put("ifsc code", ifscCode1);
                        map.put("upi id", upiId1);

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
                                } catch (Exception e) {
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
                                } catch (Exception e1) {
                                    Log.v(tag, e1 + "");
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), "Please Fill All Details", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Please check terms", Toast.LENGTH_SHORT).show();
                }

            }
        });

        binding.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hostAct.popCurrent();
            }
        });
    }
}