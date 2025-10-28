package com.example.winzgo.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.winzgo.MainActivity;
import com.example.winzgo.R;
import com.example.winzgo.activities.ManualRechargeActivity;
import com.example.winzgo.activities.RechargeActivity;
import com.example.winzgo.activities.VideoActivity;
import com.example.winzgo.databinding.FragmentMoneyBinding;
import com.example.winzgo.databinding.FragmentRechargeBinding;
import com.example.winzgo.databinding.RechargeBottomSheetBinding;
import com.example.winzgo.sharedpref.SessionSharedPref;
import com.google.firebase.firestore.FirebaseFirestore;

public class RechargeFragment extends Fragment {
    private FragmentRechargeBinding binding;
    private FirebaseFirestore firestore;
    private SessionSharedPref sharedPreferences;
    private MainActivity hostAct;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRechargeBinding.inflate(inflater, container, false);
        firestore = FirebaseFirestore.getInstance();
        sharedPreferences = new SessionSharedPref(requireContext());
        hostAct = (MainActivity) requireActivity();

        binding.balance.setText(HomeFragment.Utils.getCurrencySymbol("INR") + sharedPreferences.getBalance());
        setListeners();

        return binding.getRoot();
    }

    private void setListeners() {
        binding.addMoneyBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), VideoActivity.class);
            startActivity(intent);
        });

        binding.upBtn.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStackImmediate();
        });

        binding.manualRecBtn.setOnClickListener(v -> {
            Intent i = new Intent(requireActivity(), ManualRechargeActivity.class);
            startActivity(i);
        });

//        binding.cancelRec.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                hostAct.popCurrent();
//            }
//        });

        binding.oneKTV.setOnClickListener(v -> {
            binding.rechargeAmt.setText("1000");
        });

        binding.fiveKTv.setOnClickListener(v -> {
            binding.rechargeAmt.setText("5000");
        });

        binding.tenKTV.setOnClickListener(v -> {
            binding.rechargeAmt.setText("10000");
        });

        binding.twentyKTV.setOnClickListener(v -> {
            binding.rechargeAmt.setText("20000");
        });

        binding.fiftyKTv.setOnClickListener(v -> {
            binding.rechargeAmt.setText("50000");
        });

        binding.oneLacTV.setOnClickListener(v -> {
            binding.rechargeAmt.setText("100000");
        });

//        binding.confirmRec.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try {
//                    //EditText class is subclass of TextView.
//                    String rechargeAmtText = binding.rechargeAmt.getText().toString();
//                    if (rechargeAmtText.isEmpty()) {
//                        binding.rechargeAmt.setError("Required");
//                        binding.rechargeAmt.requestFocus();
//                    } else if (Long.parseLong(rechargeAmtText) < 50) {
//                        Toast.makeText(getActivity(), "Recharge amount must 100 or more", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Intent iForRecharge = new Intent(getActivity(), RechargeActivity.class);
//                        iForRecharge.putExtra("rechargeAmt", rechargeAmtText);
//                        requireActivity().startActivity(iForRecharge);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }
}