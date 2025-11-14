package com.example.winzgo.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.winzgo.BuildConfig;
import com.example.winzgo.MainActivity;
import com.example.winzgo.R;
import com.example.winzgo.databinding.FragmentDashboardBinding;
import com.example.winzgo.fragments.coin.CoinPredictionFragment;
import com.example.winzgo.fragments.tradePro.TradeProFragment;
import com.example.winzgo.fragments.wingo.HomeFragment;
import com.example.winzgo.sharedpref.SessionSharedPref;
import com.example.winzgo.utils.Constants;
import com.example.winzgo.utils.UtilsInterfaces;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private MainActivity activity;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        activity = (MainActivity) requireActivity();

        requireActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        requireActivity().findViewById(R.id.bottomNav).setVisibility(View.GONE);
        requireActivity().findViewById(R.id.mainHeaderLy).setVisibility(View.VISIBLE);

        activity.setupHeader("home");
        setListeners();
        return binding.getRoot();
    }

    private void setListeners() {
        binding.lyWinGo.setOnClickListener(v -> {
            activity.loadFragment(new HomeFragment(), true, "Win-Go");
        });

        binding.lyCoinPd.setOnClickListener(v -> {
            activity.loadFragment(new CoinPredictionFragment(), true, "Coin Prediction");
        });

        binding.lyTradeX.setOnClickListener(v -> {
            activity.loadFragment(new TradeProFragment(), true, "Trade Pro");
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!BuildConfig.DEBUG) {
            getIsUpdate();
        }
    }

    private void getIsUpdate() {
        FirebaseFirestore.getInstance().collection("constants").document("3").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    boolean isUpdate = documentSnapshot.getBoolean("isUpdate");
                    long versionCode = documentSnapshot.getLong("version");
                    if (isUpdate && BuildConfig.VERSION_CODE < versionCode) {
                        Constants.showAlerDialog(requireContext(), "Please update app", "Update", new UtilsInterfaces.Refresh() {
                            @Override
                            public void refresh() {
                                String link = SessionSharedPref.getStr(requireContext(), Constants.APP_DOWNLOAD_LINK, "");
                                if (!link.isEmpty()) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                                    startActivity(intent);
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}