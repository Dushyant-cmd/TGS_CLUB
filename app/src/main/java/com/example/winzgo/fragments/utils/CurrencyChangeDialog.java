package com.example.winzgo.fragments.utils;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.winzgo.R;
import com.example.winzgo.databinding.CurrencyChangeDialogBinding;
import com.example.winzgo.sharedpref.SessionSharedPref;
import com.example.winzgo.utils.Constants;

public class CurrencyChangeDialog extends DialogFragment {
    private CurrencyChangeDialogBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.currency_change_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding = CurrencyChangeDialogBinding.bind(view);
        setListeners();
    }

    private void setListeners() {
        binding.btnClose.setOnClickListener(v -> {
            dismiss();
        });

        binding.cardInr.setOnClickListener(v -> {
            binding.cardInr.setCardBackgroundColor(getResources().getColor(R.color.sky_blue));
            binding.ivTickInr.setVisibility(View.VISIBLE);

            binding.cardUsd.setCardBackgroundColor(getResources().getColor(android.R.color.transparent));
            binding.ivTickUsd.setVisibility(View.INVISIBLE);

            SessionSharedPref.getBoolean(getContext(), Constants.IS_INR, true);
        });

        binding.cardUsd.setOnClickListener(v -> {
            binding.cardUsd.setCardBackgroundColor(getResources().getColor(R.color.sky_blue));
            binding.ivTickUsd.setVisibility(View.VISIBLE);

            binding.cardInr.setCardBackgroundColor(getResources().getColor(android.R.color.transparent));
            binding.ivTickInr.setVisibility(View.INVISIBLE);

            SessionSharedPref.getBoolean(getContext(), Constants.IS_INR, false);
        });
    }
}
