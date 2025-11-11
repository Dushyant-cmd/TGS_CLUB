package com.example.winzgo.fragments.settings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.winzgo.BuildConfig;
import com.example.winzgo.MainActivity;
import com.example.winzgo.R;
import com.example.winzgo.activities.SignUpAndSignIn;
import com.example.winzgo.databinding.FragmentSettingsBinding;
import com.example.winzgo.fragments.wingo.ReferFragment;
import com.example.winzgo.sharedpref.SessionSharedPref;
import com.example.winzgo.utils.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SettingsFragment extends Fragment {
    private FirebaseAuth mAuth;
    public String TAG;
    AlertDialog alertDialog;
    SessionSharedPref sharedPreferences;
    FirebaseFirestore firestore;

    private FragmentSettingsBinding binding;

    public SettingsFragment() {
        //must be empty constructor here can execute other code.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = new SessionSharedPref(getActivity());
        firestore = FirebaseFirestore.getInstance();
        TAG = "SettingsFragment.java";
        binding.tvVersion.setText("Version: " + BuildConfig.VERSION_NAME);

        MainActivity.i = 0;
        setupViews();
        setListeners();
        return binding.getRoot();
    }

    private void setupViews() {
        binding.tvName.setText(sharedPreferences.getName());
        binding.tvId.setText(sharedPreferences.getId() + "");
        binding.tvMobileNum.setText(sharedPreferences.getMobile());
    }

    private void setListeners() {
        binding.editSettings.setOnClickListener(view -> Constants.showEditDialog(requireActivity(), updatedUserName -> {
            Dialog dialog = Constants.showProgressDialog(requireContext());
            HashMap<String, Object> map = new HashMap<>();
            map.put("name", updatedUserName);
            firestore.collection("users")
                    .document(sharedPreferences.getId() + "")
                    .update(map)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            binding.tvName.setText(updatedUserName);
                            dialog.dismiss();
                            Toast.makeText(getActivity(), "Data Saved Successfully", Toast.LENGTH_SHORT).show();
                            sharedPreferences.setName(updatedUserName);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "Check Internet Connection!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }));

        binding.termsSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTermsAlertDialog();
            }
        });

        binding.teamSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTeamBonusAlertDialog();
            }
        });

        binding.logoutSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                showLogoutAlertDialog(builder);
            }
        });

        binding.whatsappSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String group = SessionSharedPref.getStr(requireContext(), Constants.WHATSAPP_GROUP, "");
                fireWhatsapp(group);
            }
        });

        binding.referSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragmentForRefer(new ReferFragment());
            }
        });

        binding.tvWinzgoAgentLink.setOnClickListener(v -> {
            String winzgoAgentLink = SessionSharedPref.getStr(requireContext(), Constants.WINZGO_AGENT_LINK, "");
            fireWhatsapp(winzgoAgentLink);
        });

        binding.tvTeleLink.setOnClickListener(v -> {
            String telegramLink = SessionSharedPref.getStr(requireContext(), Constants.TELEGRAM_LINK, "");
            fireTelegram(telegramLink);
        });

        binding.tvVipBetSubs.setOnClickListener(v -> {
            String group = SessionSharedPref.getStr(requireContext(), Constants.VIP_BETS_SUB, "");
            fireWhatsapp(group);
        });

        binding.supportLy.setOnClickListener(v -> {
            String group = SessionSharedPref.getStr(requireContext(), Constants.WHATSAPP_GROUP, "");
            fireWhatsapp(group);
        });

        binding.telegramLy.setOnClickListener(v -> {
            String telegramLink = SessionSharedPref.getStr(requireContext(), Constants.TELEGRAM_LINK, "");
            fireTelegram(telegramLink);
        });
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

    private void fireTelegram(String telegram) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(telegram));
        startActivity(intent);
    }

    private void loadFragmentForRefer(Fragment fragment) {
        getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.container, fragment).commit();
    }

    private void showLogoutAlertDialog(AlertDialog.Builder builder) {
        builder.setTitle("Log-out?");//it is title in dialog
        builder.setMessage("Are you sure you want to Log-out?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.v(TAG, "alert3");
                mAuth.signOut();
                sharedPreferences.clearFile();
                Intent intent = new Intent(getActivity(), SignUpAndSignIn.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
                getActivity().getSupportFragmentManager().popBackStack("", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("No", (dialog, i) -> {
            //close dialog
            alertDialog.dismiss();
        });
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void showTeamBonusAlertDialog() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.terms_custom_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialogs dialogs = new AlertDialogs(view, builder);
        dialogs.show(getActivity().getSupportFragmentManager(), "team bonus dialog");
    }

    private void showTermsAlertDialog() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.terms_custom_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialogs dialog = new AlertDialogs(view, builder);
        dialog.show(getActivity().getSupportFragmentManager(), "terms and condition dialog");
    }

    public static class AlertDialogs extends DialogFragment {
        View view;//contains custom layout of AlertDialog
        AlertDialog.Builder builder;
        AlertDialog dialog;

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            setCancelable(false);//to set AlertDialog to non-cancelable when user click outside of AlertDialog window(with layout by default
            // empty layout)
            builder.setView(view);
            TextView okay = view.findViewById(R.id.closeDialog);
            TextView headOfDialog = view.findViewById(R.id.headOfDialog);
            headOfDialog.setText("Team bonus");
            dialog = builder.create();
            okay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();//close or dismiss AlertDialog.
                    AlertDialogs.super.onDestroy();//Clear Fragment from Fragment stack or make AlertDialogs(DialogFragment) fragment to Destroyed
                    //state using its lifecycle callback onDestroy().
                }
            });
            return dialog;
        }

        public AlertDialogs(View view, AlertDialog.Builder builder) {
            this.view = view;
            this.builder = builder;
        }
    }
}