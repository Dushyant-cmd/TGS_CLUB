package com.example.winzgo.fragments.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.winzgo.MainActivity;
import com.example.winzgo.R;
import com.example.winzgo.activities.SignUpAndSignIn;
import com.example.winzgo.databinding.FragmentSettingsCoinAndTradeXBinding;
import com.example.winzgo.fragments.wingo.ReferFragment;
import com.example.winzgo.sharedpref.SessionSharedPref;
import com.example.winzgo.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsCoinAndTradeXFragment extends Fragment {
    private FragmentSettingsCoinAndTradeXBinding binding;
    private MainActivity hostAct;
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

        hostAct.setupHeader("Settings");
        setListeners();
    }

    private void setListeners() {
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

        if(intent.resolveActivity(requireActivity().getPackageManager()) != null){
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), "Whatsapp not found", Toast.LENGTH_SHORT).show();
        }
    }
}