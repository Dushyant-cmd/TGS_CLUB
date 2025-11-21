package com.example.winzgo.fragments.settings;

import static com.example.winzgo.utils.Constants.isNetworkConnected;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.winzgo.MainActivity;
import com.example.winzgo.R;
import com.example.winzgo.activities.SignUpAndSignIn;
import com.example.winzgo.databinding.FragmentSettingsCoinAndTradeXBinding;
import com.example.winzgo.fragments.utils.CurrencyChangeDialog;
import com.example.winzgo.fragments.wingo.ReferFragment;
import com.example.winzgo.sharedpref.SessionSharedPref;
import com.example.winzgo.utils.Constants;
import com.example.winzgo.utils.UtilsInterfaces;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsCoinAndTradeXFragment extends Fragment {
    private FragmentSettingsCoinAndTradeXBinding binding;
    private MainActivity hostAct;
    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings_coin_and_trade_x, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsCoinAndTradeXBinding.bind(view);
        hostAct = (MainActivity) requireActivity();
        firestore = FirebaseFirestore.getInstance();

        hostAct.setupHeader("Settings");
        setupViews();
        getUserData();
        setListeners();
    }

    private void setupViews() {
        boolean currencyType = SessionSharedPref.getBoolean(getContext(), Constants.IS_INR, false);
        if (currencyType)
            binding.tvCurrType.setText("INR");
        else
            binding.tvCurrType.setText("USD");

        boolean isDarkMode = SessionSharedPref.getBoolean(getContext(), Constants.DARK_MODE_KEY, false);
        binding.switchDarkMode.setChecked(isDarkMode);

        boolean isNotificationEnabled = SessionSharedPref.getBoolean(getContext(), Constants.NOTIFICATION_PERMISSION, false);
        binding.switchNotifications.setChecked(isNotificationEnabled);
    }

    private void setListeners() {
        binding.switchDarkMode.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

            requireActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.white));
            SessionSharedPref.setBoolean(getContext(), Constants.DARK_MODE_KEY, b);
        });
        binding.lyLogout.setOnClickListener(v -> {
            Constants.showLogoutAlertDialog(requireContext(), () -> {
                FirebaseAuth.getInstance().signOut(); // sign-out from firebase
                SessionSharedPref.clearFile(requireContext()); // clear local cache.
                Intent intent = new Intent(getActivity(), SignUpAndSignIn.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                requireActivity().finish();
            });
        });

        binding.lyRefer.setOnClickListener(v -> {
            hostAct.loadFragment(new ReferFragment(), true, "refer");
        });

        binding.lySupport.setOnClickListener(v -> {
            String group = SessionSharedPref.getStr(requireContext(), Constants.WHATSAPP_GROUP, "");
            fireWhatsapp(group);
        });

        binding.lyTeamBonus.setOnClickListener(v -> {
            showTeamBonusAlertDialog();
        });

        binding.lyCurrChange.setOnClickListener(v -> {
            CurrencyChangeDialog dialog = new CurrencyChangeDialog();
            dialog.addListeners(new UtilsInterfaces.AllClickListener() {
                @Override
                public void onClick(Object data) {
                    String currencySelected = (String) data;
                    binding.tvCurrType.setText(currencySelected);
                }
            });
            dialog.show(getChildFragmentManager(), "");
        });

        binding.switchNotifications.setOnCheckedChangeListener((btn, b) -> {
            SessionSharedPref.setBoolean(getContext(), Constants.NOTIFICATION_PERMISSION, b);
        });
    }

    private void getUserData() {
        if (isNetworkConnected(getActivity())) {
            long userId = SessionSharedPref.getLong(requireContext(), Constants.USER_ID_KEY, 0L);
            if (userId != 0L) {
                firestore.collection("users").document(String.valueOf(userId))
                        .get().addOnCompleteListener(task -> {
                            long balance = task.getResult().getLong(Constants.WIN_GO_BALANCE_KEY);
                            long tradeBalance = task.getResult().getLong(Constants.TRADE_PRO_BALANCE_KEY);
                            long coinBalance = task.getResult().getLong(Constants.COIN_BALANCE_KEY);

                            long totalBalance = balance + tradeBalance + coinBalance;

                            binding.tvWalletAmt.setText(Constants.RUPEE_ICON + totalBalance);
                        });
            }
        } else {
            Constants.showSnackBarAction(binding.getRoot(), "No internet", "Try again", this::getUserData);
        }
    }

    private void showTeamBonusAlertDialog() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.terms_custom_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        SettingsFragment.AlertDialogs dialogs = new SettingsFragment.AlertDialogs(view, builder);
        dialogs.show(requireActivity().getSupportFragmentManager(), "team bonus dialog");
    }

    private void fireWhatsapp(String group) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(group));
        intent.setPackage("com.whatsapp");

        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), "Whatsapp not found", Toast.LENGTH_SHORT).show();
        }
    }
}