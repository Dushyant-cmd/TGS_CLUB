package com.example.winzgo.fragments;

import static com.example.winzgo.utils.Constants.checkAndReturnInSetCurrency;
import static com.example.winzgo.utils.Constants.isNetworkConnected;
import static com.example.winzgo.utils.Constants.setDarkMode;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;
    private MainActivity activity;
    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        activity = (MainActivity) requireActivity();
        firestore = FirebaseFirestore.getInstance();

        boolean isDarkMode = SessionSharedPref.getBoolean(getContext(), Constants.DARK_MODE_KEY, false);
        setDarkMode(getContext(), isDarkMode);

        requireActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        requireActivity().findViewById(R.id.bottomNav).setVisibility(View.GONE);
        requireActivity().findViewById(R.id.mainHeaderLy).setVisibility(View.VISIBLE);

        activity.setupHeader("home");
        getCoinGraphDetails();
        setListeners();
        return binding.getRoot();
    }

    private void getCoinGraphDetails() {
        if (isNetworkConnected(getActivity())) {
            firestore.collection("ids").document("coinPredictionId").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> taskOne) {
                    if (taskOne.isSuccessful()) {
                        long currentGameId = taskOne.getResult().getLong("id");
                        firestore.collection("coinPredictionHistory").document(String.valueOf(currentGameId)).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot doc = task.getResult();
                                List<Long> btcGraphValues = (List<Long>) doc.get("btcGraphValues");
                                List<Long> ethGraphValues = (List<Long>) doc.get("ethGraphValues");
                                List<Long> solGraphValues = (List<Long>) doc.get("solGraphValues");

                                if (btcGraphValues != null) {
                                    float growPercentage = 0f;
                                    float prevEntry = Float.parseFloat(String.valueOf(btcGraphValues.get(btcGraphValues.size() - 2)));
                                    float newCurrLastEntry = Float.parseFloat(String.valueOf(btcGraphValues.get(btcGraphValues.size() - 1)));
                                    if (newCurrLastEntry > 0) {
                                        growPercentage = ((prevEntry - newCurrLastEntry) / newCurrLastEntry) * 100;
                                    }

                                    binding.tvBtcAmt.setText(checkAndReturnInSetCurrency(getContext(), String.valueOf((long) newCurrLastEntry)));
                                    String formattedGrowPercentage = String.format("%.2f", growPercentage);

                                    binding.tvBtcPt.setText(formattedGrowPercentage + "%");
                                    binding.tvBtcPt.setTextColor(activity.getResources().getColor(R.color.green));
                                    if (growPercentage < 0) {
                                        binding.tvBtcPt.setTextColor(activity.getResources().getColor(R.color.dark_red));
                                    }
                                }

                                if (ethGraphValues != null) {
                                    float growPercentage = 0f;
                                    float prevEntry = Float.parseFloat(String.valueOf(ethGraphValues.get(ethGraphValues.size() - 2)));
                                    float newCurrLastEntry = Float.parseFloat(String.valueOf(ethGraphValues.get(ethGraphValues.size() - 1)));
                                    if (newCurrLastEntry > 0) {
                                        growPercentage = ((prevEntry - newCurrLastEntry) / newCurrLastEntry) * 100;
                                    }

                                    binding.tvEthAmt.setText(checkAndReturnInSetCurrency(getContext(), String.valueOf((long) newCurrLastEntry)));
                                    String formattedGrowPercentage = String.format("%.2f", growPercentage);

                                    binding.tvEthPt.setText(formattedGrowPercentage + "%");
                                    binding.tvEthPt.setTextColor(activity.getResources().getColor(R.color.green));
                                    if (growPercentage < 0) {
                                        binding.tvEthPt.setTextColor(activity.getResources().getColor(R.color.dark_red));
                                    }
                                }

                                if (solGraphValues != null) {
                                    float growPercentage = 0f;
                                    float prevEntry = Float.parseFloat(String.valueOf(solGraphValues.get(solGraphValues.size() - 2)));
                                    float newCurrLastEntry = Float.parseFloat(String.valueOf(solGraphValues.get(solGraphValues.size() - 1)));
                                    if (newCurrLastEntry > 0) {
                                        growPercentage = ((prevEntry - newCurrLastEntry) / newCurrLastEntry) * 100;
                                    }

                                    binding.tvSolAmt.setText(checkAndReturnInSetCurrency(getContext(), String.valueOf((long) newCurrLastEntry)));
                                    String formattedGrowPercentage = String.format("%.2f", growPercentage);

                                    binding.tvSolPt.setText(formattedGrowPercentage + "%");
                                    binding.tvSolPt.setTextColor(activity.getResources().getColor(R.color.green));
                                    if (growPercentage < 0) {
                                        binding.tvSolPt.setTextColor(activity.getResources().getColor(R.color.dark_red));
                                    }
                                }
                            } else {
                                Constants.showSnackBar(binding.getRoot(), "Something went wrong");
                            }
                        });
                    } else {
                        Constants.showSnackBar(binding.getRoot(), "Something went wrong");
                    }
                }
            });
        } else {
            Constants.showSnackBarAction(binding.getRoot(), "No internet", "Try again", this::getCoinGraphDetails);
        }
    }

    private void setListeners() {
        binding.lyWinGo.setOnClickListener(v -> {
            activity.loadFragment(new HomeFragment(), true, "Win-Go");
        });

        binding.lyCoinPd.setOnClickListener(v -> {
            activity.loadFragment(new CoinPredictionFragment(), true, "Crypto Streak");
        });

        binding.lyTradeX.setOnClickListener(v -> {
            activity.loadFragment(new TradeProFragment(), true, "Trade Pro");
        });
    }
}