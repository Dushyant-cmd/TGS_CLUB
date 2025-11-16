package com.example.winzgo.fragments;

import static com.example.winzgo.utils.Constants.isNetworkConnected;

import android.app.Dialog;
import android.app.ProgressDialog;
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
import com.example.winzgo.sharedpref.SessionSharedPref;
import com.example.winzgo.utils.Constants;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CoinAndTradeWalletFragment extends Fragment {
    private FragmentCoinAndTradeWalletBinding binding;
    private MainActivity hostActivity;
    private FirebaseFirestore firestore;
    private int type = 0;// 0 trade, 1 coin, 2 win-go
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
        firestore = FirebaseFirestore.getInstance();
        Bundle bundle = getArguments();
        type = bundle.getInt("type");

        hostActivity.setupHeader("Wallet");

        getUserData();
        getAllRecharge();
        getAllWithdrawals();
        getAllBets();
        setListeners();
    }

    private void getAllBets() {
        if(isNetworkConnected(getActivity())) {
            Dialog dialog = Constants.showProgressDialog(getContext());
            String collection = "tradeBets";
            if(type == 1) {
                collection = "coinBets";
            }
            firestore.collection(collection).whereEqualTo("user_id", SessionSharedPref.getLong(getContext(), Constants.USER_ID_KEY, 0L))
                    .get().addOnCompleteListener(task -> {
                        dialog.dismiss();
                        long profit = 0;
                        if(task.isSuccessful()) {
                            List<DocumentSnapshot> list = task.getResult().getDocuments();
                            for(DocumentSnapshot doc: list) {
                                boolean isWinner = doc.getBoolean("isWinner");
                                if(isWinner) {
                                    profit += doc.getLong("win_amount");
                                }
                            }

                            binding.tvTotalBets.setText(String.valueOf(list.size()));
                        }
                        binding.tvTotalProfits.setText(Constants.RUPEE_ICON + profit);
                    });
        } else {
            Constants.showSnackBarAction(binding.getRoot(), "No internet", "Try again", this::getUserData);
        }
    }

    private void getAllWithdrawals() {
        if(isNetworkConnected(getActivity())) {
            Dialog dialog = Constants.showProgressDialog(getContext());
            firestore.collection("transactions")
                    .whereEqualTo("gameType", type)
                    .whereEqualTo("type", "withdraw")
                    .whereEqualTo("user_id", SessionSharedPref.getLong(getContext(), Constants.USER_ID_KEY, 0L))
                    .get().addOnCompleteListener(task -> {
                        dialog.dismiss();
                        long withdrawals = 0;
                        if(task.isSuccessful()) {
                            List<DocumentSnapshot> list = task.getResult().getDocuments();
                            for(DocumentSnapshot doc: list) {
                                long amt = Long.parseLong(doc.getString("amount"));
                                withdrawals += amt;
                            }

                        }
                        binding.tvTotalWithdrawals.setText(Constants.RUPEE_ICON + withdrawals);
                    });
        } else {
            Constants.showSnackBarAction(binding.getRoot(), "No internet", "Try again", this::getUserData);
        }
    }

    private void getAllRecharge() {
        if(isNetworkConnected(getActivity())) {
            Dialog dialog = Constants.showProgressDialog(getContext());
            firestore.collection("transactions")
                    .whereEqualTo("gameType", type)
                    .whereEqualTo("type", "recharge")
                    .whereEqualTo("user_id", SessionSharedPref.getLong(getContext(), Constants.USER_ID_KEY, 0L))
                    .get().addOnCompleteListener(task -> {
                        dialog.dismiss();
                        long deposit = 0;
                        if(task.isSuccessful()) {
                            List<DocumentSnapshot> list = task.getResult().getDocuments();
                            for(DocumentSnapshot doc: list) {
                                long amt = Long.parseLong(doc.getString("amount"));
                                deposit += amt;
                            }

                        }
                        binding.tvTotalDeposits.setText(Constants.RUPEE_ICON + deposit);
                    });
        } else {
            Constants.showSnackBarAction(binding.getRoot(), "No internet", "Try again", this::getUserData);
        }
    }

    private void setListeners() {
        binding.btnDeposit.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("type", type);
            CoinTradeDepositFragment fragment = new CoinTradeDepositFragment();
            fragment.setArguments(bundle);
            hostActivity.loadFragment(fragment, true, "Deposit");
        });

        binding.btnWithdraw.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("type", type);
            CoinTradeWithdrawalFragment fragment = new CoinTradeWithdrawalFragment();
            fragment.setArguments(bundle);
            hostActivity.loadFragment(fragment, true, "Withdrawal");
        });
    }

    private void getUserData() {
        if (isNetworkConnected(requireActivity())) {
            long userId = SessionSharedPref.getLong(requireContext(), Constants.USER_ID_KEY, 0L);
            if (userId != 0L) {
                ProgressDialog dialog1 = new ProgressDialog(requireActivity());
                dialog1.setMessage("Please wait...");
                dialog1.show();
                firestore.collection("users").document(String.valueOf(userId))
                        .get().addOnCompleteListener(task -> {
                            dialog1.dismiss();
                            long balance = task.getResult().getLong(Constants.TRADE_PRO_BALANCE_KEY);

                            if(type == 0) {
                                SessionSharedPref.setLong(requireContext(), Constants.TRADE_PRO_BALANCE_KEY, balance);
                            } else if(type == 1) {
                                balance = task.getResult().getLong(Constants.COIN_BALANCE_KEY);
                                SessionSharedPref.setLong(requireContext(), Constants.COIN_BALANCE_KEY, balance);
                            }

                            MainActivity.binding.tvBalance.setText(Constants.RUPEE_ICON + balance);
                            binding.tvBalance.setText(Constants.RUPEE_ICON + balance);
                        });
            }
        } else {
            Constants.showSnackBarAction(binding.getRoot(), "No internet", "Try again", this::getUserData);
        }
    }
}