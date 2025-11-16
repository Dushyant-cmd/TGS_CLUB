package com.example.winzgo.fragments.tradePro;

import static com.example.winzgo.utils.Constants.isNetworkConnected;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.winzgo.MainActivity;
import com.example.winzgo.MyApplication;
import com.example.winzgo.R;
import com.example.winzgo.activities.ManualRechargeActivity;
import com.example.winzgo.adapter.TradeHistoryAdapter;
import com.example.winzgo.databinding.FragmentTradeProBinding;
import com.example.winzgo.fragments.recharge.CoinTradeDepositFragment;
import com.example.winzgo.models.TradeHistoryModel;
import com.example.winzgo.sharedpref.SessionSharedPref;
import com.example.winzgo.utils.Constants;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TradeProFragment extends Fragment {
    private FragmentTradeProBinding binding;
    private FirebaseFirestore firestore;
    private long betAmt = 50, currentGameId, oneMinCounter;
    private CountDownTimer countDownTimer;
    private List<CandleEntry> entries = new ArrayList<>();
    private long currLastEntry = 0L;
    private DocumentSnapshot lastDoc;
    private List<TradeHistoryModel> tradeList = new ArrayList<>();
    private boolean isTradePageLoad = true;
    private TradeHistoryAdapter tradeHistoryAdapter;
    private int isUpOrDown = 1; //0(up), 1(down), 2 means nothing selected
    private boolean isUp = true;
    private int betInto = 10;
    private MainActivity hostAct;

    @Override
    public void onResume() {
        super.onResume();
        getSecondsAndStartCountDown();
        getTradeGraph();
        getUserData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTradeProBinding.inflate(getLayoutInflater());
        firestore = FirebaseFirestore.getInstance();

        setupViews();
        setListeners();
        return binding.getRoot();
    }

    private void setupViews() {
        hostAct = (MainActivity) getActivity();
        hostAct.setupHeader("Coin Prediction");

        tradeHistoryAdapter = new TradeHistoryAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(tradeHistoryAdapter);

        binding.nestedScroll.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                Log.v("TradePro.java", "game list scroll till bottom");
                if (isNetworkConnected(requireActivity()) && isTradePageLoad) {
                    binding.tradePB.setVisibility(View.VISIBLE);
                    isTradePageLoad = false;
                    getTradeHistory(false);
                } else if (!isNetworkConnected(requireActivity())) {
                    Constants.showSnackBarAction(binding.getRoot(), "No internet", "Try again", () -> {
                        // Ignore
                    });
                }
            }
        });
    }

    private void setListeners() {
        binding.swipeRefLy.setOnRefreshListener(() -> {
            getSecondsAndStartCountDown();
            getTradeGraph();
            getTradeHistory(true);
            getUserData();
        });

        binding.upBtn.setOnClickListener(v -> {
            isUpOrDown = 0;
        });

        binding.downBtn.setOnClickListener(v -> {
            isUpOrDown = 1;
        });

        binding.btnConfirmBet.setOnClickListener(v -> {
            checkAndPutBet();
        });

        binding.btnBet50.setOnClickListener(v -> {
            betAmt = 50;
            highlightBetAmtLayout(betAmt);
        });

        binding.btnBet100.setOnClickListener(v -> {
            betAmt = 100;
            highlightBetAmtLayout(betAmt);
        });

        binding.btnBet500.setOnClickListener(v -> {
            betAmt = 500;
            highlightBetAmtLayout(betAmt);
        });

        binding.btnBet1000.setOnClickListener(v -> {
            betAmt = 1000;
            highlightBetAmtLayout(betAmt);
        });

        binding.btnBet5000.setOnClickListener(v -> {
            betAmt = 5000;
            highlightBetAmtLayout(betAmt);
        });

        binding.iXBtnUp.setOnClickListener(v -> {
            betAmt = betAmt * betInto;
            betInto = betInto + 1;
        });

        binding.iXBtnDown.setOnClickListener(v -> {
            if (betInto > 10) {
                betAmt = betAmt * betInto;
                betInto = betInto - 1;
            }
        });
    }

    private void highlightBetAmtLayout(long amount) {
        int count = binding.betAmtLy.getChildCount();
        for (int i = 0; i < count; i++) {
            View item = binding.betAmtLy.getChildAt(i);
            if (item instanceof TextView) {
                TextView tv = (TextView) item;
                long tvAmt = Long.parseLong(tv.getText().toString().substring(1));
                if (tvAmt == amount) {
                    tv.setBackgroundResource(R.drawable.little_dark_violet_rect);
                } else {
                    tv.setBackgroundResource(R.drawable.white_border_rectangle);
                }
            }
        }
    }

    private void checkAndPutBet() {
        if (oneMinCounter > 10) {
            String message;
            if (isUpOrDown == 0) {
                isUp = true;
            } else if (isUpOrDown == 1) {
                isUp = false;
            } else {
                message = "No prediction selected yet!";
                Constants.showSnackBar(binding.getRoot(), message);
                return;
            }

            if (isNetworkConnected(requireActivity())) {
                long balance = SessionSharedPref.getLong(requireContext(), Constants.TRADE_PRO_BALANCE_KEY, 0L);
                long userId = SessionSharedPref.getLong(requireContext(), Constants.USER_ID_KEY, 0L);
                if (balance >= betAmt) {
                    Dialog dialog = Constants.showProgressDialog(requireContext());
                    firestore.collection("tradeBets").whereEqualTo("id", currentGameId).whereEqualTo("user_id", userId)
                            .limit(1).get().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    if (task.getResult().getDocuments().isEmpty()) {
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("bet_amount", betAmt);
                                        map.put("isUp", isUp);
                                        map.put("selected", currLastEntry);
                                        map.put("user_id", userId);
                                        map.put("timestamp", System.currentTimeMillis());
                                        map.put("dateAndTime", ((MyApplication) requireActivity().getApplication()).getCurrDateAndTime());
                                        map.put("name", SessionSharedPref.getStr(requireContext(), Constants.NAME_KEY, ""));
                                        map.put("result", 0);
                                        map.put("id", currentGameId);
                                        map.put("isWinner", false);
                                        map.put("win_amount", 0);
                                        firestore.collection("tradeBets").add(map)
                                                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                        dialog.dismiss();
                                                        if (task.isSuccessful()) {
                                                            binding.betDetailsCard.setVisibility(View.VISIBLE);
                                                            binding.betAmt.setText(Constants.RUPEE_ICON + betAmt);
                                                            binding.betEntryAmt.setText("Entry: " + Constants.RUPEE_ICON + currLastEntry);
                                                            if (isUp) {
                                                                binding.ivBetPredictionType.setImageResource(R.drawable.graph_up_ic);
                                                                binding.ivBetPredictionType.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                                                            } else {
                                                                binding.ivBetPredictionType.setImageResource(R.drawable.graph_down_ic);
                                                                binding.ivBetPredictionType.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                                                            }
                                                            Constants.updateBalance(requireActivity(), betAmt, false, Constants.TRADE_PRO_BALANCE_KEY, () -> {
                                                                getUserData();
                                                            });
                                                        } else {
                                                            Constants.showSnackBar(binding.getRoot(), task.getException().getMessage());
                                                        }
                                                    }
                                                });
                                    } else {
                                        dialog.dismiss();
                                        Constants.showAlerDialog(requireContext(), "Only one prediction submission is authorized within the contract timeframe.", "Okay", () -> {
                                        });
                                    }
                                } else {
                                    dialog.dismiss();
                                    Constants.showSnackBar(binding.getRoot(), "Something went wrong");
                                }
                            });
                } else {
                    Constants.showSnackBarAction(binding.getRoot(), "Low balance", "Do Recharge", () -> {
                        hostAct.loadFragment(new CoinTradeDepositFragment(), true, "Deposit");
                    });
                }
            } else {
                Constants.showSnackBarAction(binding.getRoot(), "No internet", "Try again", this::checkAndPutBet);
            }
        } else {
            Constants.showSnackBar(binding.getRoot(), "Wait for the next round");
        }

        isUpOrDown = 2; // reset game.
    }

    private void setupGraph(List<CandleEntry> entries) {
        try {
            CandleStickChart candleStickChart = binding.candleStick;

            candleStickChart.getDescription().setText("LootTrade");

            binding.candleStick.getDescription().setText("");

            CandleDataSet dataSet = new CandleDataSet(entries, "Trade");
            binding.candleStick.setHighlightPerDragEnabled(false);
            binding.candleStick.setDrawBorders(false);
            YAxis yAxis = binding.candleStick.getAxisLeft();
            YAxis rightAxis = binding.candleStick.getAxisRight();
            yAxis.setDrawGridLines(false);
            rightAxis.setDrawGridLines(false);
            binding.candleStick.requestDisallowInterceptTouchEvent(true);
            XAxis xAxis = binding.candleStick.getXAxis();

            xAxis.setDrawGridLines(false); // disable x axis grid lines
            xAxis.setDrawLabels(false);
            rightAxis.setTextColor(Color.WHITE);
            yAxis.setDrawLabels(false);
            xAxis.setGranularity(1f);
            xAxis.setGranularityEnabled(false);
            xAxis.setAxisLineColor(R.color.too_light_grey);
            xAxis.setAvoidFirstLastClipping(false);

            binding.candleStick.setGridBackgroundColor(128);
            binding.candleStick.setBorderColor(255);
            binding.candleStick.getAxisRight().setEnabled(false);
            YAxis leftAxis = binding.candleStick.getAxisLeft();
            leftAxis.setEnabled(false);
            binding.candleStick.setDrawGridBackground(true);
            binding.candleStick.setPinchZoom(false);
            binding.candleStick.setDoubleTapToZoomEnabled(false);
            binding.candleStick.getXAxis().setEnabled(true);
            binding.candleStick.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            dataSet.setDrawIcons(false);
            dataSet.setIncreasingColor(getResources().getColor(R.color.dark_green)); // Color for up (green) candlesticks
            dataSet.setIncreasingPaintStyle(Paint.Style.FILL); // Set the paint style to Fill for green candlesticks
            dataSet.setDecreasingColor(getResources().getColor(R.color.dark_red)); // Color for down (red) candlesticks
            dataSet.setShadowColorSameAsCandle(true); // Using the same color for shadows as the candlesticks
            dataSet.setDrawValues(false); // Hiding the values on the chart if not needed
            dataSet.setShadowWidth(1f); // control width of candle stack and its shadow.


            // Created a CandleData object from the CandleDataSet
            CandleData data = new CandleData(dataSet);

            binding.candleStick.setData(data);
            binding.candleStick.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getTradeGraph() {
        if (isNetworkConnected(getActivity())) {
            firestore.collection("ids").document("tradeProId").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> taskOne) {
                    binding.swipeRefLy.setRefreshing(false);
                    if (taskOne.isSuccessful()) {
                        currentGameId = taskOne.getResult().getLong("id");
                        firestore.collection("tradeProHistory").document(String.valueOf(currentGameId)).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot doc = task.getResult();
                                entries = new ArrayList<>();
                                List<Long> tradeGraphValues = (List<Long>) doc.get("tradeGraphValues");
                                if (tradeGraphValues != null) {
                                    long prevEntry = 0L;
                                    int i = 0;
                                    while (i < tradeGraphValues.size()) {
                                        long newCurrEntry = tradeGraphValues.get(i);
                                        float high = tradeGraphValues.get(tradeGraphValues.indexOf(Collections.max(tradeGraphValues)));
                                        float low = tradeGraphValues.get(tradeGraphValues.indexOf(Collections.min(tradeGraphValues)));

                                        if (i > 0) {
                                            high = entries.get(entries.size() - 1).getClose();
                                            low = entries.get(entries.size() - 1).getOpen();
                                        }
                                        entries.add(new CandleEntry(i, high, low, prevEntry, newCurrEntry));
                                        // previous candle.
                                        prevEntry = newCurrEntry;
                                        i++;
                                    }

                                    float growPercentage = 0f;
                                    long newCurrLastEntry = (long) entries.get(entries.size() - 1).getClose();
                                    if (currLastEntry > 0) {
                                        growPercentage = ((float) (newCurrLastEntry - currLastEntry) / currLastEntry) * 100;
                                    }

                                    currLastEntry = newCurrLastEntry;
                                    String formattedGrowPercentage = String.format("%.2f", growPercentage);

                                    binding.tvBtcAmt.setText(Constants.RUPEE_ICON + currLastEntry + ".00");
                                    binding.tvBtcPt.setText("(" + formattedGrowPercentage + "%)");
                                    binding.tvGraphCurrCandle.setText(Constants.RUPEE_ICON + currLastEntry + "(" + formattedGrowPercentage + "%)");
                                    binding.tvBtcPt.setTextColor(getResources().getColor(R.color.green));
                                    if (growPercentage < 0) {
                                        binding.tvBtcPt.setTextColor(getResources().getColor(R.color.dark_red));
                                    }

                                    binding.tvRoundNum.setText("Round #" + currentGameId);
                                    setupGraph(entries);
                                }
                            } else {
                                Constants.showSnackBar(binding.getRoot(), "Something went wrong");
                            }
                        });

                        getTradeHistory(true);
                    } else {
                        Constants.showSnackBar(binding.getRoot(), "Something went wrong");
                    }
                }
            });
        } else {
            Constants.showSnackBarAction(binding.getRoot(), "No internet", "Try again", this::getTradeGraph);
        }
    }

    private void getSecondsAndStartCountDown() {
        if (isNetworkConnected(getActivity())) {
            Dialog dialog = Constants.showProgressDialog(getActivity());
            firestore.collection("timer").document("tradeProTimer").get().addOnCompleteListener(task -> {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    try {
                        long dbTimestamp = task.getResult().getLong("sec");//it returns timestamp
                        long currentTimestamp = (System.currentTimeMillis() / 1000);
                        long timerSecGone = (currentTimestamp - dbTimestamp);//sec gone
                        long timerSecLeft = (60 - timerSecGone);//sec left
                        binding.minTv.setText("0");
                        long timerSecsInMillis = (timerSecLeft * 1000);
                        Log.v("Trade.java", "seconds: " + timerSecLeft);

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
                            binding.secTv1.setText("0");
                            binding.secTv2.setText(String.valueOf(oneMinCounter));
                        } else {
                            String str = String.valueOf(oneMinCounter);
                            binding.secTv1.setText(str.charAt(0) + "");
                            binding.secTv2.setText(str.charAt(1) + "");
                        }

                        if (oneMinCounter < 0) {
                            binding.secTv1.setText("0");
                            binding.secTv2.setText("0");
                        } else if (oneMinCounter % 5 == 0) {
                            Log.d("TradePro.java", "onTick: " + oneMinCounter);
                            getTradeGraph();
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
                        getTradeGraph();
                        binding.betDetailsCard.setVisibility(View.GONE);
                        getUserData();
                        getTradeHistory(true);
                    }
                }.start();
            } else {
                Constants.showSnackBarAction(binding.getRoot(), "Something went wrong", "try again", this::getSecondsAndStartCountDown);
            }
        } catch (Exception e) {
            Constants.showSnackBarAction(binding.getRoot(), "Something went wrong", "try again", this::getSecondsAndStartCountDown);
        }
    }

    private void getTradeHistory(boolean isFirstLoad) {
        if (isNetworkConnected(requireActivity())) {
            if (isFirstLoad) {
                lastDoc = null;
                tradeList.clear();
            }

            long userId = SessionSharedPref.getLong(getContext(), Constants.USER_ID_KEY, 0L);
            Query query = firestore.collection("tradeBets").whereLessThan("id", currentGameId).whereEqualTo("user_id", userId).orderBy("timestamp", Query.Direction.DESCENDING).limit(10);
            if (lastDoc != null) {
                query = firestore.collection("tradeBets").whereLessThan("id", currentGameId).whereEqualTo("user_id", userId).orderBy("timestamp", Query.Direction.DESCENDING).startAfter(lastDoc).limit(10);
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

                        TradeHistoryModel item = document.toObject(TradeHistoryModel.class);
                        boolean isUp = document.getBoolean("isUp");
                        boolean isWinner = document.getBoolean("isWinner");
                        item.setUp(isUp);
                        item.setWinner(isWinner);
                        tradeList.add(item);
                    }

                    tradeHistoryAdapter.submitList(tradeList);
                    tradeHistoryAdapter.notifyDataSetChanged();
                }

                binding.tradePB.setVisibility(View.GONE);
                isTradePageLoad = true;
            });
        } else {
            Constants.showSnackBarAction(binding.getRoot(), "No internet", "Try again", () -> {
                getTradeHistory(true);
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
                                long balance = task.getResult().getLong(Constants.TRADE_PRO_BALANCE_KEY);
                                SessionSharedPref.setLong(requireContext(), Constants.TRADE_PRO_BALANCE_KEY, balance);

                                MainActivity.binding.tvBalance.setText(Constants.RUPEE_ICON + balance);
                            }
                        });
            }
        } else {
            Constants.showSnackBarAction(binding.getRoot(), "No internet", "Try again", this::getUserData);
        }
    }

    private void loadFragment(Fragment fragment, boolean isAdd) {
        if (!requireActivity().isFinishing()) {
            FragmentTransaction ft = requireActivity().getSupportFragmentManager()
                    .beginTransaction();

            if (isAdd) {
                ft.add(R.id.container, fragment);
            } else {
                ft.replace(R.id.container, fragment);
            }

            ft.addToBackStack(null).commit();
        }
    }
}