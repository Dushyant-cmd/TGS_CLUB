package com.example.winzgo.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.winzgo.MainActivity;
import com.example.winzgo.R;
import com.example.winzgo.databinding.FragmentCoinAndTradeWalletBinding;
import com.example.winzgo.fragments.recharge.CoinTradeDepositFragment;
import com.example.winzgo.fragments.withdrawal.CoinTradeWithdrawalFragment;

public class CoinAndTradeWalletFragment extends Fragment {
    private FragmentCoinAndTradeWalletBinding binding;
    private MainActivity hostActivity;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_coin_and_trade_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding = FragmentCoinAndTradeWalletBinding.bind(view);
        hostActivity = (MainActivity) requireActivity();

        hostActivity.setupHeader("Wallet");
        setListeners();
    }

    private void setListeners() {
        binding.btnDeposit.setOnClickListener(v -> {
            hostActivity.loadFragment(new CoinTradeDepositFragment(), true, "Deposit");
        });

        binding.btnWithdraw.setOnClickListener(v -> {
            hostActivity.loadFragment(new CoinTradeWithdrawalFragment(), true, "Withdrawal");
        });
    }
}