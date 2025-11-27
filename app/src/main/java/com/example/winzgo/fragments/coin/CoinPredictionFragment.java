package com.example.winzgo.fragments.coin;

import static com.example.winzgo.utils.Constants.isNetworkConnected;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.winzgo.MainActivity;
import com.example.winzgo.R;
import com.example.winzgo.adapter.CoinPredictionHistoryAdapter;
import com.example.winzgo.adapter.TradeHistoryAdapter;
import com.example.winzgo.databinding.FragmentCoinPredictionBinding;
import com.example.winzgo.fragments.CoinAndTradeWalletFragment;
import com.example.winzgo.models.CoinPredictionHistoryModel;
import com.example.winzgo.models.TradeHistoryModel;
import com.example.winzgo.sharedpref.SessionSharedPref;
import com.example.winzgo.utils.Constants;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CoinPredictionFragment extends Fragment {
    private FragmentCoinPredictionBinding binding;
    private MainActivity hostAct;
    private FirebaseFirestore firestore;
    private long currentGameId, currBtcLastEntry = 0L, currEthLastEntry = 0L, currSolLastEntry = 0L, oneMinCounter;
    private long betAmt = 50, betIntoAmt = 50, betInto = 10;
    private CountDownTimer countDownTimer;
    private List<Float> btcList = new ArrayList<>(), ethList = new ArrayList<>(), solList = new ArrayList<>();
    private DocumentSnapshot lastDoc = null;
    private boolean isCoinHistoryLoaded = false;
    private CoinPredictionHistoryAdapter coinHistoryAdapter;
    private List<CoinPredictionHistoryModel> coinList = new ArrayList<>();

    @Override
    public void onResume() {
        super.onResume();
        getSecondsAndStartCountDown();
        getCoinGraphDetails();
        getUserData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_coin_prediction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding = FragmentCoinPredictionBinding.bind(view);
        hostAct = (MainActivity) requireActivity();
        firestore = FirebaseFirestore.getInstance();

        hostAct.setupHeader("Coin Prediction");
        setupViews();
        setListeners();
    }

    private void setListeners() {
        binding.swipeRefLy.setOnRefreshListener(() -> {
            getSecondsAndStartCountDown();
            getCoinGraphDetails();
            getCoinPredictionHistory(true);
            getUserData();
        });

        binding.cardBitcoin.setOnClickListener(v -> {
        });

        binding.cardEthereum.setOnClickListener(v -> {
        });

        binding.cardSolana.setOnClickListener(v -> {
        });

        binding.btnConfirmBet.setOnClickListener(v -> {
//            checkAndPutBet();
        });

        binding.btnBet50.setOnClickListener(v -> {
            betAmt = 50;
            betIntoAmt = 50;
            highlightBetAmtLayout(betAmt);
        });

        binding.btnBet100.setOnClickListener(v -> {
            betAmt = 100;
            betIntoAmt = 100;
            highlightBetAmtLayout(betAmt);
        });

        binding.btnBet500.setOnClickListener(v -> {
            betAmt = 500;
            betIntoAmt = 500;
            highlightBetAmtLayout(betAmt);
        });

        binding.btnBet1000.setOnClickListener(v -> {
            betAmt = 1000;
            betIntoAmt = 1000;
            highlightBetAmtLayout(betAmt);
        });

        binding.btnBet5000.setOnClickListener(v -> {
            betAmt = 5000;
            betIntoAmt = 5000;
            highlightBetAmtLayout(betAmt);
        });

        binding.iXBtnUp.setOnClickListener(v -> {
            betInto = betInto + 1;
            betAmt = betIntoAmt * betInto;
            binding.tvBetIntoNum.setText("x" + betInto);
            binding.tvInvestAmt.setText(Constants.RUPEE_ICON + betAmt);
        });

        binding.iXBtnDown.setOnClickListener(v -> {
            if (betInto > 10) {
                betInto = betInto - 1;
                betAmt = betIntoAmt * betInto;
                binding.tvBetIntoNum.setText("x" + betInto);
                binding.tvInvestAmt.setText(Constants.RUPEE_ICON + betAmt);
            }
        });
    }

    private void highlightBetAmtLayout(long amount) {
        int count = binding.betAmtSelectionLy.getChildCount();
        for (int i = 0; i < count; i++) {
            View item = binding.betAmtSelectionLy.getChildAt(i);
            if (item instanceof TextView) {
                TextView tv = (TextView) item;
                long tvAmt = Long.parseLong(tv.getText().toString().substring(1));
                if (tvAmt == amount) {
                    binding.tvInvestAmt.setText(Constants.RUPEE_ICON + amount);
                    tv.setBackgroundResource(R.drawable.little_dark_violet_rect);
                } else {
                    tv.setBackgroundResource(R.drawable.light_silver_bg);
                }
            }
        }
    }

    private void setupViews() {
        coinHistoryAdapter = new CoinPredictionHistoryAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(coinHistoryAdapter);

        binding.nestedScroll.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                Log.v("Coin.java", "game list scroll till bottom");
                if (isNetworkConnected(requireActivity()) && isCoinHistoryLoaded) {
                    binding.coinPB.setVisibility(View.VISIBLE);
                    isCoinHistoryLoaded = false;
                    getCoinPredictionHistory(false);
                } else if (!isNetworkConnected(requireActivity())) {
                    Constants.showSnackBarAction(binding.getRoot(), "No internet", "Try again", () -> {
                        // Ignore
                    });
                }
            }
        });
    }

    private void getCoinGraphDetails() {
        if (isNetworkConnected(getActivity())) {
            firestore.collection("ids").document("coinPredictionId").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> taskOne) {
                    binding.swipeRefLy.setRefreshing(false);
                    if (taskOne.isSuccessful()) {
                        currentGameId = taskOne.getResult().getLong("id");
                        firestore.collection("coinPredictionHistory").document(String.valueOf(currentGameId)).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot doc = task.getResult();
                                btcList = new ArrayList<>();
                                ethList = new ArrayList<>();
                                solList = new ArrayList<>();
                                List<Long> btcGraphValues = (List<Long>) doc.get("btcGraphValues");
                                List<Long> ethGraphValues = (List<Long>) doc.get("ethGraphValues");
                                List<Long> solGraphValues = (List<Long>) doc.get("solGraphValues");
                                if (btcGraphValues != null) {
                                    int i = 0;
                                    while (i < btcGraphValues.size()) {
                                        float newCurrEntry = Float.parseFloat(String.valueOf(btcGraphValues.get(i)));

                                        btcList.add(newCurrEntry);
                                        i++;
                                    }

                                    float growPercentage = 0f;
                                    long newCurrLastEntry = Long.parseLong(String.valueOf(btcList.get(btcList.size() - 1)));
                                    if (currBtcLastEntry > 0) {
                                        growPercentage = ((float) (newCurrLastEntry - currBtcLastEntry) / currBtcLastEntry) * 100;
                                    }

                                    currBtcLastEntry = newCurrLastEntry;
                                    String formattedGrowPercentage = String.format("%.2f", growPercentage);

                                    binding.tvBtcAmt.setText(Constants.RUPEE_ICON + currBtcLastEntry + ".00");
                                    binding.tvMomentumBtc.setText("(" + formattedGrowPercentage + "%)");
                                    binding.tvMomentumBtc.setTextColor(getResources().getColor(R.color.green));
                                    if (growPercentage < 0) {
                                        binding.tvMomentumBtc.setTextColor(getResources().getColor(R.color.dark_red));
                                    }

                                    binding.tvRoundNum.setText("Round #" + currentGameId);
                                    setupGraph(binding.chartBitcoin, btcList);
                                }

                                if (ethGraphValues != null) {
                                    int i = 0;
                                    while (i < ethGraphValues.size()) {
                                        float newCurrEntry = Float.parseFloat(String.valueOf(ethGraphValues.get(i)));

                                        ethList.add(newCurrEntry);
                                        i++;
                                    }

                                    float growPercentage = 0f;
                                    long newCurrLastEntry = Long.parseLong(String.valueOf(ethList.get(ethList.size() - 1)));
                                    if (currEthLastEntry > 0) {
                                        growPercentage = ((float) (newCurrLastEntry - currEthLastEntry) / currEthLastEntry) * 100;
                                    }

                                    currEthLastEntry = newCurrLastEntry;
                                    String formattedGrowPercentage = String.format("%.2f", growPercentage);

                                    binding.tvEthAmt.setText(Constants.RUPEE_ICON + currEthLastEntry + ".00");
                                    binding.tvMomentumEth.setText("(" + formattedGrowPercentage + "%)");
                                    binding.tvMomentumEth.setTextColor(getResources().getColor(R.color.green));
                                    if (growPercentage < 0) {
                                        binding.tvMomentumEth.setTextColor(getResources().getColor(R.color.dark_red));
                                    }

                                    binding.tvRoundNum.setText("Round #" + currentGameId);
                                    setupGraph(binding.chartEthereum, ethList);
                                }

                                if (solGraphValues != null) {
                                    int i = 0;
                                    while (i < solGraphValues.size()) {
                                        float newCurrEntry = Float.parseFloat(String.valueOf(solGraphValues.get(i)));

                                        solList.add(newCurrEntry);
                                        i++;
                                    }

                                    float growPercentage = 0f;
                                    long newCurrLastEntry = Long.parseLong(String.valueOf(solList.get(solList.size() - 1)));
                                    if (currSolLastEntry > 0) {
                                        growPercentage = ((float) (newCurrLastEntry - currSolLastEntry) / currSolLastEntry) * 100;
                                    }

                                    currSolLastEntry = newCurrLastEntry;
                                    String formattedGrowPercentage = String.format("%.2f", growPercentage);

                                    binding.tvSolAmt.setText(Constants.RUPEE_ICON + currSolLastEntry + ".00");
                                    binding.tvMomentumSol.setText("(" + formattedGrowPercentage + "%)");
                                    binding.tvMomentumSol.setTextColor(getResources().getColor(R.color.green));
                                    if (growPercentage < 0) {
                                        binding.tvMomentumSol.setTextColor(getResources().getColor(R.color.dark_red));
                                    }

                                    binding.tvRoundNum.setText("Round #" + currentGameId);
                                    setupGraph(binding.chartSolana, solList);
                                }
                            } else {
                                Constants.showSnackBar(binding.getRoot(), "Something went wrong");
                            }
                        });

                        getUserCurrBet();
                        getCoinPredictionHistory(true);
                    } else {
                        Constants.showSnackBar(binding.getRoot(), "Something went wrong");
                    }
                }
            });
        } else {
            Constants.showSnackBarAction(binding.getRoot(), "No internet", "Try again", this::getCoinGraphDetails);
        }
    }

    private void getUserCurrBet() {
        firestore.collection("coinPredictionBets").whereEqualTo("user_id", SessionSharedPref.getLong(getContext(), Constants.USER_ID_KEY, 0L))
                .whereEqualTo("id", currentGameId).limit(1).get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        List<DocumentSnapshot> list = task.getResult().getDocuments();
                        if(!list.isEmpty()) {
                            DocumentSnapshot doc = list.get(0);
                            long amt = doc.getLong("bet_amount");
                            boolean isWinner = doc.getBoolean("isWinner");
                            String selected = doc.getString("selected");

                            binding.betLy.setVisibility(View.VISIBLE);
                            binding.tvSelected.setText(selected + "(" + Constants.RUPEE_ICON + amt + ")");
                        }
                    }
                });
    }

    private void setupGraph(LineChart lineChart, List<Float> values) {
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < values.size(); i++) {
            entries.add(new Entry(i, values.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Price");

        // 🎯 ---- Styling like your image ----
        dataSet.setColor(getResources().getColor(R.color.black)); // Line color
        dataSet.setLineWidth(1f);                   // Thickness
        dataSet.setDrawCircles(false);              // No dots
        dataSet.setDrawValues(false);               // No text on points
        dataSet.setMode(LineDataSet.Mode.LINEAR);   // Sharp zigzag
        dataSet.setDrawFilled(false);

        LineData lineData = new LineData(dataSet);

        lineChart.setData(lineData);

        // Remove grid, background, borders for clean look
        lineChart.getXAxis().setEnabled(false);
        lineChart.getAxisLeft().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        // remove default padding
        lineChart.setViewPortOffsets(0f, 0f, 0f, 0f);
        lineChart.setExtraOffsets(0f, 0f, 0f, 0f);

        // Smooth animation
        lineChart.animateX(1000);

        lineChart.invalidate();
    }

    private void getSecondsAndStartCountDown() {
        if (isNetworkConnected(getActivity())) {
            Dialog dialog = Constants.showProgressDialog(getActivity());
            firestore.collection("timer").document("coinPredictionTimer").get().addOnCompleteListener(task -> {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    try {
                        long dbTimestamp = task.getResult().getLong("sec");//it returns timestamp
                        long currentTimestamp = (System.currentTimeMillis() / 1000);
                        long timerSecGone = (currentTimestamp - dbTimestamp);//sec gone
                        long timerSecLeft = (60 - timerSecGone);//sec left
                        binding.tvTimerMin2.setText("0");
                        long timerSecsInMillis = (timerSecLeft * 1000);

                        startCountDownOneMin(timerSecGone, timerSecLeft, timerSecsInMillis);
                    } catch (Exception e) {
                        Constants.showSnackBar(binding.getRoot(), "Something went wrong");
                    }
                } else {
                    Constants.showSnackBar(binding.getRoot(), "Something went wrong");
                }
            });
        } else {
            Constants.showSnackBarAction(binding.getRoot(), "No internet", "Try again", this::getSecondsAndStartCountDown);
        }
    }

    private void startCountDownOneMin(long timerSecGone, long timerSecLeft, long timerSecInMillis) {
        timerSecInMillis += 5000;
        oneMinCounter = timerSecLeft;
        try {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }

            if (oneMinCounter > -6) {
                countDownTimer = new CountDownTimer(timerSecInMillis, 1000) {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onTick(long l) {
                        oneMinCounter--;
                        if (oneMinCounter < 10) {
                            binding.tvTimerSec1.setText("0");
                            binding.tvTimerSec2.setText(String.valueOf(oneMinCounter));
                        } else {
                            String str = String.valueOf(oneMinCounter);
                            binding.tvTimerSec1.setText(str.charAt(0) + "");
                            binding.tvTimerSec2.setText(str.charAt(1) + "");
                        }

                        if (oneMinCounter < 0) {
                            binding.tvTimerSec1.setText("0");
                            binding.tvTimerSec2.setText("0");
                        } else if (oneMinCounter % 5 == 0) {
                            Log.d("TradePro.java", "onTick: " + oneMinCounter);
                            getCoinGraphDetails();
                        }

                        if (getActivity() == null) {
                            // because of thread safe program
                            countDownTimer.cancel();
                        }
                    }

                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onFinish() {
                        getSecondsAndStartCountDown();
                        getCoinGraphDetails();
                        binding.betLy.setVisibility(View.GONE);
                        getUserData();
                        getCoinPredictionHistory(true);
                    }
                }.start();
            } else {
                Constants.showSnackBarAction(binding.getRoot(), "Something went wrong", "try again", this::getSecondsAndStartCountDown);
            }
        } catch (Exception e) {
            Constants.showSnackBarAction(binding.getRoot(), "Something went wrong", "try again", this::getSecondsAndStartCountDown);
        }
    }

    private void getCoinPredictionHistory(boolean isFirstLoad) {
        if (isNetworkConnected(requireActivity())) {
            if (isFirstLoad) {
                lastDoc = null;
                coinList.clear();
            }

            long userId = SessionSharedPref.getLong(getContext(), Constants.USER_ID_KEY, 0L);
            Query query = firestore.collection("coinPredictionBets").whereLessThan("id", currentGameId).whereEqualTo("user_id", userId).orderBy("timestamp", Query.Direction.DESCENDING).limit(10);
            if (lastDoc != null) {
                query = firestore.collection("coinPredictionBets").whereLessThan("id", currentGameId).whereEqualTo("user_id", userId).orderBy("timestamp", Query.Direction.DESCENDING).startAfter(lastDoc).limit(10);
            }

            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    //Query returns documents in result inside Task class instance of DocumentSnapshot class object
                    List<DocumentSnapshot> snapshotList = task.getResult().getDocuments();
                    for (int i = 0; i < snapshotList.size(); i++) {
                        DocumentSnapshot document = snapshotList.get(i);
                        if (i == snapshotList.size() - 1) {
                            lastDoc = document;
                        }

                        CoinPredictionHistoryModel item = document.toObject(CoinPredictionHistoryModel.class);
                        boolean isWinner = document.getBoolean("isWinner");
                        item.setWinner(isWinner);
                        coinList.add(item);
                    }

                    coinHistoryAdapter.submitList(coinList);
                    coinHistoryAdapter.notifyDataSetChanged();
                }

                binding.coinPB.setVisibility(View.GONE);
                isCoinHistoryLoaded = true;
            });
        } else {
            Constants.showSnackBarAction(binding.getRoot(), "No internet", "Try again", () -> {
                getCoinPredictionHistory(true);
            });
        }
    }

    private void getUserData() {
        if (isNetworkConnected(requireActivity())) {
            long userId = SessionSharedPref.getLong(requireContext(), Constants.USER_ID_KEY, 0L);
            if (userId != 0L) {
                ProgressDialog dialog1 = new ProgressDialog(requireActivity());
                dialog1.setMessage("Please wait...");
                dialog1.show();
                firestore.collection("users").document(String.valueOf(userId))
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                dialog1.dismiss();
                                long balance = task.getResult().getLong(Constants.COIN_BALANCE_KEY);
                                SessionSharedPref.setLong(requireContext(), Constants.COIN_BALANCE_KEY, balance);

                                MainActivity.binding.tvBalance.setText(Constants.RUPEE_ICON + balance);
                            }
                        });
            }
        } else {
            Constants.showSnackBarAction(binding.getRoot(), "No internet", "Try again", this::getUserData);
        }
    }
}