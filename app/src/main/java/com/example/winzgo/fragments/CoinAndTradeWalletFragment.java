package com.example.winzgo.fragments;

import static com.example.winzgo.utils.Constants.checkAndReturnInSetCurrency;
import static com.example.winzgo.utils.Constants.isNetworkConnected;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.winzgo.MainActivity;
import com.example.winzgo.R;
import com.example.winzgo.adapter.CoinTradeTransactionsAdapter;
import com.example.winzgo.databinding.FragmentCoinAndTradeWalletBinding;
import com.example.winzgo.fragments.recharge.CoinTradeDepositFragment;
import com.example.winzgo.fragments.withdrawal.CoinTradeWithdrawalFragment;
import com.example.winzgo.models.TransactionsModel;
import com.example.winzgo.sharedpref.SessionSharedPref;
import com.example.winzgo.utils.Constants;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class CoinAndTradeWalletFragment extends Fragment {
    private FragmentCoinAndTradeWalletBinding binding;
    private MainActivity hostActivity;
    private FirebaseFirestore firestore;
    private int type = Constants.TRADE_TYPE;// 0 trade, 1 coin, 2 win-go
    private CoinTradeTransactionsAdapter adapter;
    private List<TransactionsModel> list = new ArrayList<>();

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

        adapter = new CoinTradeTransactionsAdapter(getContext(), list);
        binding.rvTransactions.setAdapter(adapter);
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));

        queries();
        setListeners();
    }

    private void queries() {
        if (isNetworkConnected(getActivity())) {
            getAllTransactions();
            getUserData();
            getAllRecharge();
            getAllWithdrawals();
            getAllBets();
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

        binding.swipeRefLy.setOnRefreshListener(this::queries);
    }

    private void getAllBets() {
        String collection = "tradeBets";
        if (type == 1) {
            collection = "coinPredictionBets";
        }
        binding.tvTotalBets.setVisibility(View.GONE);
        binding.pbBets.setVisibility(View.VISIBLE);
        binding.tvTotalProfits.setVisibility(View.GONE);
        binding.pbProfits.setVisibility(View.VISIBLE);
        firestore.collection(collection).whereEqualTo("user_id", SessionSharedPref.getLong(getContext(), Constants.USER_ID_KEY, 0L))
                .get().addOnCompleteListener(task -> {
                    binding.swipeRefLy.setRefreshing(false);
                    long profit = 0;
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> list = task.getResult().getDocuments();
                        for (DocumentSnapshot doc : list) {
                            boolean isWinner = doc.getBoolean("isWinner");
                            if (isWinner) {
                                profit += doc.getLong("win_amount");
                            }
                        }

                        binding.tvTotalBets.setVisibility(View.VISIBLE);
                        binding.pbBets.setVisibility(View.GONE);
                        binding.tvTotalBets.setText(String.valueOf(list.size()));
                    }
                    binding.tvTotalProfits.setVisibility(View.VISIBLE);
                    binding.pbProfits.setVisibility(View.GONE);

                    binding.tvTotalProfits.setText(checkAndReturnInSetCurrency(getContext(), String.valueOf(profit)));
                });
    }

    private void getAllWithdrawals() {
        binding.tvTotalWithdrawals.setVisibility(View.GONE);
        binding.pbWithdrawals.setVisibility(View.VISIBLE);
        firestore.collection("transactions")
                .whereEqualTo("gameType", type)
                .whereEqualTo("type", "withdraw").whereEqualTo("status", "completed")
                .whereEqualTo("user_id", SessionSharedPref.getLong(getContext(), Constants.USER_ID_KEY, 0L))
                .get().addOnCompleteListener(task -> {
                    long withdrawals = 0;
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> list = task.getResult().getDocuments();
                        for (DocumentSnapshot doc : list) {
                            long amt = Long.parseLong(doc.getString("amount"));
                            withdrawals += amt;
                        }

                    }

                    binding.tvTotalWithdrawals.setVisibility(View.VISIBLE);
                    binding.pbWithdrawals.setVisibility(View.GONE);

                    binding.tvTotalWithdrawals.setText(checkAndReturnInSetCurrency(getContext(), String.valueOf(withdrawals)));
                });
    }

    private void getAllRecharge() {
        binding.tvTotalDeposits.setVisibility(View.GONE);
        binding.pbDeposit.setVisibility(View.VISIBLE);
        firestore.collection("transactions")
                .whereEqualTo("gameType", type)
                .whereEqualTo("type", "recharge").whereEqualTo("status", "completed")
                .whereEqualTo("user_id", SessionSharedPref.getLong(getContext(), Constants.USER_ID_KEY, 0L))
                .get().addOnCompleteListener(task -> {
                    long deposit = 0;
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> list = task.getResult().getDocuments();
                        for (DocumentSnapshot doc : list) {
                            long amt = Long.parseLong(doc.getString("amount"));
                            deposit += amt;
                        }

                    }
                    binding.tvTotalDeposits.setVisibility(View.VISIBLE);
                    binding.pbDeposit.setVisibility(View.GONE);

                    binding.tvTotalDeposits.setText(checkAndReturnInSetCurrency(getContext(), String.valueOf(deposit)));
                });
    }

    private void getAllTransactions() {
        binding.pbTxnHistory.setVisibility(View.VISIBLE);
        binding.rvTransactions.setVisibility(View.GONE);
        long userId = SessionSharedPref.getLong(getContext(), Constants.USER_ID_KEY, 0L);
        firestore.collection("transactions")
                .whereEqualTo("gameType", type)
                .whereEqualTo("user_id", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        list.clear();
                        List<DocumentSnapshot> docList = task.getResult().getDocuments();
                        for (DocumentSnapshot doc : docList) {
                            TransactionsModel transactionsModel = doc.toObject(TransactionsModel.class);
                            list.add(transactionsModel);
                        }

                        binding.pbTxnHistory.setVisibility(View.GONE);
                        binding.rvTransactions.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void getUserData() {
        long userId = SessionSharedPref.getLong(requireContext(), Constants.USER_ID_KEY, 0L);
        if (userId != 0L) {
            firestore.collection("users").document(String.valueOf(userId))
                    .get().addOnCompleteListener(task -> {
                        long balance = task.getResult().getLong(Constants.TRADE_PRO_BALANCE_KEY);

                        if (type == Constants.TRADE_TYPE) {
                            SessionSharedPref.setLong(requireContext(), Constants.TRADE_PRO_BALANCE_KEY, balance);
                        } else if (type == Constants.COIN_TYPE) {
                            balance = task.getResult().getLong(Constants.COIN_BALANCE_KEY);
                            SessionSharedPref.setLong(requireContext(), Constants.COIN_BALANCE_KEY, balance);
                        }

                        MainActivity.binding.tvBalance.setText(checkAndReturnInSetCurrency(getContext(), String.valueOf(balance)));
                        binding.tvBalance.setText(checkAndReturnInSetCurrency(getContext(), String.valueOf(balance)));
                    });
        }
    }
}