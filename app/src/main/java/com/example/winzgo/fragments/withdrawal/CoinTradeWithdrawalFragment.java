package com.example.winzgo.fragments.withdrawal;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.winzgo.MainActivity;
import com.example.winzgo.R;
import com.example.winzgo.databinding.FragmentCoinPredictionBinding;
import com.example.winzgo.databinding.FragmentCoinTradeWithdrawalBinding;
import com.example.winzgo.fragments.AddBankDetailsFragment;

public class CoinTradeWithdrawalFragment extends Fragment {
    private FragmentCoinTradeWithdrawalBinding binding;
    private MainActivity hostAct;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_coin_trade_withdrawal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding = FragmentCoinTradeWithdrawalBinding.bind(view);
        hostAct = (MainActivity) requireActivity();

        setListeners();
    }

    private void setListeners() {
        binding.btnBankDetails.setOnClickListener(v -> {
            hostAct.loadFragment(new AddBankDetailsFragment(), true, "Bank Details");
        });
    }
}