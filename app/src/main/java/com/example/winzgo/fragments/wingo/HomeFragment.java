package com.example.winzgo.fragments.wingo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.winzgo.BuildConfig;
import com.example.winzgo.MainActivity;
import com.example.winzgo.R;
import com.example.winzgo.adapter.GameResultsAdapter;
import com.example.winzgo.databinding.BetBottomSheetBinding;
import com.example.winzgo.databinding.FragmentHomeBinding;
import com.example.winzgo.fragments.recharge.RechargeFragment;
import com.example.winzgo.fragments.withdrawal.WithdrawFragment;
import com.example.winzgo.models.GameResult;
import com.example.winzgo.sharedpref.SessionSharedPref;
import com.example.winzgo.utils.Constants;
import com.example.winzgo.utils.UtilsInterfaces;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class HomeFragment extends Fragment {
    long currentGameId;
    long counter, num, num2, oneMinCounter;
    //bet on game variables for selectedNum, selectedBetAmt, betInto
    int selectedNum;
    long selectedBetAmt, betInto;
    String selectedImg;
    String gameResultImg;
    long gameResultNumber = -1;
    ProgressDialog dialog;
    SessionSharedPref sharedPreferences;
    long secs = 181000;
    ProgressBar bar;
    FirebaseFirestore firestore;
    String tag = "HomeFragment.java";
    ArrayList<GameResult> list;
    CountDownTimer countDownTimer;
    private boolean isComeFromStop = false;
    //limit of items data get from Firestore for GameResult
    private final long limit = 8;
    private DocumentSnapshot lastItem;
    GameResultsAdapter gameResultsAdapter;
    private FragmentHomeBinding binding;
    private boolean isThreeMin = true;

    public HomeFragment() {
        //do work
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        bar = binding.progressBar1;
        //default values of bet variables
        selectedNum = -1;
        selectedBetAmt = 10;
        betInto = 1;
        selectedImg = Constants.GREEN;
        sharedPreferences = new SessionSharedPref(getActivity());
        String amtRupeeSymbol = Utils.getCurrencySymbol("INR") + sharedPreferences.getBalance();
        binding.walletAmt.setText(amtRupeeSymbol);
        binding.username.setText("Name: " + sharedPreferences.getName());
        binding.userId.setText("ID: " + sharedPreferences.getId());
        firestore = FirebaseFirestore.getInstance();
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Fetching all results");
        list = new ArrayList<>();
        MainActivity.i = 101;
        requireActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.light_tint));
        requireActivity().findViewById(R.id.bottomNav).setVisibility(View.VISIBLE);
        requireActivity().findViewById(R.id.headerLy).setVisibility(View.GONE);

        setListener();
        if (isNetworkConnected()) {
            getUserData();
            getGameResult();
            getSecondsAndStartCountDown();
            Log.v("HomeFragment.java", "net connected");
        } else {
            Log.v("HomeFragment.java", "net not connected");
            openAlertDialog();//show AlertDialog(a layout displayed on some part of screen)
        }
        return binding.getRoot();
    }

    private void getUserData() {
        if (sharedPreferences.getId() != 0L) {
            ProgressDialog dialog1 = new ProgressDialog(requireActivity());
            dialog1.setMessage("Please wait...");
            dialog1.show();
            firestore.collection("users").document("" + sharedPreferences.getId())
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            dialog1.dismiss();
                            sharedPreferences.setBalance(task.getResult().getLong("balance"));
                            sharedPreferences.setId(task.getResult().getLong("id"));
                            sharedPreferences.setRefer(task.getResult().getString("refer"));
                            binding.walletAmt.setText(Utils.getCurrencySymbol("INR") + "" + task.getResult().getLong("balance"));
                            Log.v(tag, task.getResult().getLong("balance") + " bal");
                        }
                    });
        }
    }

    private void getSecondsAndStartCountDown() {
        ProgressDialog dialog1 = new ProgressDialog(requireActivity());
        dialog1.setMessage("Please wait...");
        dialog1.show();
        if (isThreeMin) {
            firestore.collection("timer").document("seconds").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    dialog1.dismiss();
                    if (task.isSuccessful()) {
                        try {
                            long dbTimestamp = task.getResult().getLong("sec");//it returns timestamp
                            long currentTimestamp = (System.currentTimeMillis() / 1000);
                            long timerSecGone = (currentTimestamp - dbTimestamp);//sec gone
                            long timerSecLeft = (180 - timerSecGone);//sec left
                            num = timerSecGone;
                            if (timerSecLeft < 60) {
                                binding.minTv1.setText("0");
                                binding.minTv2.setText("0");
                                num2 = 0;//it is min track
                                counter = timerSecLeft;//it is seconds track in CountDown
                                Log.v(tag, timerSecLeft + " 0 min");
                            } else if (timerSecLeft < 120) {
                                binding.minTv1.setText("0");
                                binding.minTv2.setText("1");
                                num2 = 1;
                                counter = (timerSecLeft - 60);
                                Log.v(tag, timerSecLeft + " 1 min");
                            } else if (timerSecLeft < 180) {
                                binding.minTv1.setText("0");
                                binding.minTv2.setText("2");
                                num2 = 2;
                                counter = (timerSecLeft - 120);
                                Log.v(tag, timerSecLeft + " 2 min");
                            }
                            secs = (timerSecLeft * 1000);
                            Log.v(tag, "seconds: " + timerSecLeft);
                            firestore.collection("ids").document("game_id").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    currentGameId = task.getResult().getLong("id");
                                    binding.tradeIdHead.setText(String.valueOf(currentGameId));
                                }
                            });
                            startCountDown();

                        } catch (Exception e) {
                            Log.v("HomeFragment.java", "sec err: " + e);
                        }
                    } else {
                        Log.v("HomeFragment.java", "error: " + task.getException());
                    }
                }
            });
        } else {
            firestore.collection("timer").document("seconds2").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    dialog1.dismiss();
                    if (task.isSuccessful()) {
                        try {
                            long dbTimestamp = task.getResult().getLong("sec");//it returns timestamp
                            long currentTimestamp = (System.currentTimeMillis() / 1000);
                            long timerSecGone = (currentTimestamp - dbTimestamp);//sec gone
                            long timerSecLeft = (60 - timerSecGone);//sec left
                            binding.minTv1.setText("0");
                            binding.minTv2.setText("0");
                            long timerSecsInMillis = (timerSecLeft * 1000);
                            Log.v(tag, "seconds: " + timerSecLeft);
                            firestore.collection("ids").document("oneMinuteGameId").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    currentGameId = task.getResult().getLong("oneMinId");
                                    binding.tradeIdHead.setText(String.valueOf(currentGameId));
                                }
                            });
                            startCountDownOneMin(timerSecGone, timerSecLeft, timerSecsInMillis);
                        } catch (Exception e) {
                            Log.v("HomeFragment.java", "sec err: " + e);
                        }
                    } else {
                        Log.v("HomeFragment.java", "error: " + task.getException());
                    }
                }
            });
        }
    }

    private void startCountDownOneMin(long timerSecGone, long timerSecLeft, long timerSecInMillis) {
        timerSecInMillis += 5000;
        oneMinCounter = timerSecLeft;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (oneMinCounter > -6) {
            try {
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

                        if (oneMinCounter <= 30) {
                            binding.timerLy.setBackgroundResource(R.drawable.grey_circle_image);
                        } else {
                            binding.timerLy.setBackgroundResource(R.drawable.tabs_gradient);
                        }

                        if (oneMinCounter <= 0) {
                            binding.secTv1.setText("0");
                            binding.secTv2.setText("0");
                        }
                    }

                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onFinish() {
                        getSecondsAndStartCountDown();
                        getGameResult();
                        getUserData();
                        try {
                            binding.timerLy.setBackgroundResource(R.drawable.tabs_gradient);
                        } catch (Exception e) {
                            Log.v(tag, e + "");
                        }
                    }
                }.start();
            } catch (Exception e) {
                Log.v(tag, "count down err: " + e);
            }
        } else {
            Snackbar bar = Snackbar.make(binding.getRoot(), "Something went wrong", Snackbar.LENGTH_INDEFINITE);
            bar.setAction("Try again", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getSecondsAndStartCountDown();
                    getGameResult();
                    getUserData();
                    try {
                        binding.timerLy.setBackgroundResource(R.drawable.tabs_gradient);
                    } catch (Exception e) {
                        Log.v(tag, e + "");
                    }
                    bar.dismiss();
                }
            });

            bar.show();
        }
    }

    private void startCountDown() {
        secs += 5000;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (secs > -6) {
            try {
                countDownTimer = new CountDownTimer(secs, 1000) {
                    //                long num3 = secs/1000;
                    Context context1 = getActivity();

                    //every time sec change in timer onTick(long l) is called
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onTick(long l) {
//                    Log.v(tag, "last sec: " + + secs/1000 + ", " + num);
                        num++;
                        if (num > 149) {
                            try {
                                binding.timerLy.setBackgroundResource(R.drawable.grey_circle_image);
                            } catch (Exception e) {
                                Log.v(tag, e + "");
                            }
                        } else {
                            binding.timerLy.setBackgroundResource(R.drawable.tab_gradient);
                        }
                        if (num == 60 || num == 120) {
                            num2 -= 1;
                            binding.minTv2.setText(String.valueOf(num2));
                        }
                        if (counter == 0) {
                            counter = 60;
                        }
                        if (num < 180) {
                            counter--;
                            if (counter < 10) {
                                binding.secTv1.setText("0");
                                binding.secTv2.setText(String.valueOf(counter));
                            } else {
                                String str = String.valueOf(counter);
                                binding.secTv1.setText(str.charAt(0) + "");
                                binding.secTv2.setText(str.charAt(1) + "");
                            }
                        }
                        if (num == 180) {
                            binding.secTv1.setText("0");
                            binding.secTv2.setText("0");
                            Log.v("HomeFragment.java", num + "");
                        }
                    }

                    //when CountDownTimer is reach end value passed in 1st input params of CountDownTimer
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onFinish() {
                        Log.v(tag, "count down finished");
                        getSecondsAndStartCountDown();
                        getGameResult();
                        getUserData();
                        try {
                            binding.timerLy.setBackgroundResource(R.drawable.tabs_gradient);
                        } catch (Exception e) {
                            Log.v(tag, e + "");
                        }
                    }
                }.start();
            } catch (Exception e) {
                Log.v(tag, "count down err: " + e);
            }
        } else {
            Snackbar bar = Snackbar.make(binding.getRoot(), "Something went wrong", Snackbar.LENGTH_INDEFINITE);
            bar.setAction("Try again", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getSecondsAndStartCountDown();
                    getGameResult();
                    getUserData();
                    try {
                        binding.timerLy.setBackgroundResource(R.drawable.tabs_gradient);
                    } catch (Exception e) {
                        Log.v(tag, e + "");
                    }
                    bar.dismiss();
                }
            });

            bar.show();
        }
    }

    private int page = 0;

    private void getGameResult() {
        ProgressDialog dialog1 = new ProgressDialog(requireActivity());
        dialog1.setMessage("Please wait...");
        dialog1.show();
        DocumentReference docRef = firestore.collection("ids").document("game_id");
        if (!isThreeMin)
            docRef = firestore.collection("ids").document("oneMinuteGameId");

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (isThreeMin) {
                        currentGameId = task.getResult().getLong("id");
                        sharedPreferences.setGameId(currentGameId + "");
                    } else {
                        currentGameId = task.getResult().getLong("oneMinId");
                        sharedPreferences.setGameId(currentGameId + "");
                    }

                    long totalNumberOfReads = (currentGameId - 11);
                    Query documentReference = firestore.collection("gameHistory").whereGreaterThan("id", totalNumberOfReads);
                    if (!isThreeMin) {
                        documentReference = firestore.collection("gameHistoryOneMin").whereGreaterThan("id", totalNumberOfReads);
                    }
                    documentReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            dialog1.dismiss();
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                List<DocumentSnapshot> documentList = task.getResult().getDocuments();
                                System.out.println("total: doc" + documentList.size());
                                list.clear();
                                for (int i = (documentList.size() - 1); i >= 0; i--) {
                                    if ((i == (documentList.size() - 1))) {
                                        DocumentSnapshot doc = documentList.get(i);
                                        gameResultImg = doc.getString("resultImg");
                                        gameResultNumber = doc.getLong("resultNumber");
                                        Log.v(tag, gameResultNumber + "");
                                        lastItem = documentList.get(0);
                                        continue;
                                    }
                                    DocumentSnapshot document = documentList.get(i);
                                    if (document.contains("halfWin")) {
                                        list.add(new GameResult(document.getLong("id"), document.getLong("resultNumber"), document.getString("resultImg"), document.getString("createdAt"), document.getString("halfWin")));
                                    } else
                                        list.add(new GameResult(document.getLong("id"), document.getLong("resultNumber"), document.getString("resultImg"), document.getString("createdAt"), "no"));
                                }
                                bar.setVisibility(View.GONE);
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity()) {
                                    @Override
                                    public boolean canScrollVertically() {
                                        return false;
                                    }
                                };
                                binding.gameResults.setLayoutManager(linearLayoutManager);
                                binding.gameResults.setVisibility(View.VISIBLE);
                                gameResultsAdapter = new GameResultsAdapter(list);
                                binding.gameResults.setAdapter(gameResultsAdapter);
                                binding.gameResults.setHasFixedSize(true);
                                binding.nestedScroll.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                                    @Override
                                    public void onScrollChange(@NonNull NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                                        //Check if user scrolled till bottom
                                        if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                                            Log.v(tag, "game list scroll till bottom");
                                            if (isNetworkConnected() && page == 0) {
                                                bar.setVisibility(View.VISIBLE);
                                                page++;
                                                loadMoreGameResult();
                                            } else if (!isNetworkConnected()) {
                                                Toast.makeText(getActivity(), "Check internet connection", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                            } else {
                                dialog.dismiss();
                                Toast.makeText(getActivity(), "Server Error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void loadMoreGameResult() {
        Log.v(tag, "runned");
        Query query = firestore.collection("gameHistory").orderBy("id", Query.Direction.DESCENDING).startAfter(lastItem).limit(limit);
        if (!isThreeMin) {
            query = firestore.collection("gameHistoryOneMin").orderBy("id", Query.Direction.DESCENDING).startAfter(lastItem).limit(limit);
        }
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    Log.v(tag, "fetched1");
                    //Query returns documents in result inside Task class instance of DocumentSnapshot class object
                    List<DocumentSnapshot> snapshotList = task.getResult().getDocuments();
                    Log.v(tag, snapshotList.size() + "");
                    for (int i = 0; i < snapshotList.size(); i++) {
                        DocumentSnapshot document = snapshotList.get(i);
                        if (i == snapshotList.size() - 1) {
                            lastItem = document;
                        }
                        if (document.contains("halfWin")) {
                            list.add(new GameResult(document.getLong("id"), document.getLong("resultNumber"), document.getString("resultImg"), document.getString("createdAt"), document.getString("halfWin")));
                        } else
                            list.add(new GameResult(document.getLong("id"), document.getLong("resultNumber"), document.getString("resultImg"), document.getString("createdAt"), "no"));
                    }
                    page--;
                    bar.setVisibility(View.GONE);
                    gameResultsAdapter.notifyDataSetChanged();

                } else if (task.isSuccessful()) {
                    bar.setVisibility(View.GONE);
                    page--;
                    Log.v(tag, "fetched2");
                } else {
                    bar.setVisibility(View.GONE);
                    page--;
                    Log.v(tag, task.getException() + "");
                }
            }
        });
    }

    private void openAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Check network connection!");
        builder.setCancelable(false);
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (isNetworkConnected()) {
                    Toast.makeText(getActivity(), "Network connected successfully", Toast.LENGTH_SHORT).show();
                    getGameResult();
                    getSecondsAndStartCountDown();
                } else {
                    openAlertDialog();
                }
            }
        });
        builder.create().show();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
        return connected;
    }

    public static class AlertDia extends DialogFragment {
        AlertDialog dialog;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View views = LayoutInflater.from(getActivity()).inflate(R.layout.game_rules_alert_dialog, null);
            views.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //1st dismiss() then cancel AlertDialog. so AlertDialog will cancel if multiple times it opens up.
                    dialog.dismiss();
                    dialog.cancel();
                }
            });
            builder.setView(views);
            dialog = builder.create();
            return dialog;
        }
    }

    static class Utils {
        public static SortedMap<Currency, Locale> currencyLocaleMap;

        static {
            currencyLocaleMap = new TreeMap<Currency, Locale>(new Comparator<Currency>() {
                public int compare(Currency c1, Currency c2) {
                    return c1.getCurrencyCode().compareTo(c2.getCurrencyCode());
                }
            });
            for (Locale locale : Locale.getAvailableLocales()) {
                try {
                    Currency currency = Currency.getInstance(locale);
                    currencyLocaleMap.put(currency, locale);
                } catch (Exception e) {
                }
            }
        }


        public static String getCurrencySymbol(String currencyCode) {
            Currency currency = Currency.getInstance(currencyCode);
            return currency.getSymbol(currencyLocaleMap.get(currency));
        }

    }

    //stopped state
    @Override
    public void onStop() {
        super.onStop();
        Log.v(tag, "stopped");
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
            isComeFromStop = true;
        }

    }

    //resumed state
    @Override
    public void onResume() {
        super.onResume();
        getIsUpdate();
        if (isNetworkConnected() && isComeFromStop) {
            getSecondsAndStartCountDown();
            getGameResult();

            binding.greenCont.setBackgroundResource(R.drawable.btn_number_green);
            binding.redCont.setBackgroundResource(R.drawable.btn_number_red);
            binding.blueCont.setBackgroundResource(R.drawable.btn_number_blue);

            binding.gameNumLy.setBackgroundResource(R.drawable.coin_background);
        } else if (isNetworkConnected() == false) {
            openAlertDialog();
        }
    }

    private void getIsUpdate() {
        firestore.collection("constants").document("3").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
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

    //    //started state
    @Override
    public void onStart() {
        super.onStart();
        Log.v(tag, "started");
        if (countDownTimer != null) {
            countDownTimer.start();
        }
    }

    String betAmtSelected = "10";

    @SuppressLint("ClickableViewAccessibility")
    public void openBetBottomSheet(int resultNum, String resultImage) {
        betInto = 1;
        BottomSheetDialog dialog = new BottomSheetDialog(requireActivity());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        BetBottomSheetBinding dialogBinding = BetBottomSheetBinding.inflate(getLayoutInflater(), binding.getRoot(), false);
        String result2;
        if (resultNum >= 0) {
            result2 = String.valueOf(resultNum);
            dialogBinding.tvResultNum.setText("Selected: " + result2);
            dialogBinding.resultNumCont.setVisibility(View.VISIBLE);
            dialogBinding.resultImgCont.setVisibility(View.GONE);
        } else {
            result2 = resultImage;

            switch (result2) {
                case Constants.RED:
                    dialogBinding.resultImgCont.setBackgroundResource(R.drawable.btn_number_red);
                    dialogBinding.resultImgTv.setText("Selected: Red");
                    break;
                case Constants.GREEN:
                    dialogBinding.resultImgCont.setBackgroundResource(R.drawable.btn_number_green);
                    dialogBinding.resultImgTv.setText("Selected: Green");
                    break;
                case (Constants.BLUE):
                    dialogBinding.resultImgCont.setBackgroundResource(R.drawable.btn_number_blue);
                    dialogBinding.resultImgTv.setText("Selected: Blue");
                    break;
            }
            dialogBinding.resultNumCont.setVisibility(View.GONE);
            dialogBinding.resultImgCont.setVisibility(View.VISIBLE);
        }

        String betAmtSymbol = Utils.getCurrencySymbol("INR") + selectedBetAmt;
        dialogBinding.betAmt.setText(betAmtSymbol);

        dialogBinding.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                dialog.cancel();
            }
        });

        dialogBinding.confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("Please wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                long amount = (selectedBetAmt * betInto);
                if (sharedPreferences.getBalance() < amount) {
                    Toast.makeText(getActivity(), "Low Balance", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    progressDialog.cancel();
                } else {
                    Map<String, Object> map = new HashMap<>();
                    long amt = (sharedPreferences.getBalance() - amount);
                    map.put("balance", amt);
                    firestore.collection("users").document(sharedPreferences.getId() + "")
                            .update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.v(tag, "success");
                                    //Check user win and change isWinner, winAmt, status accordingly
                                    //HashMap is an sub class of Map interface which contains object in key/value pair just like json object
                                    //hierarchy we store bets collection document fields and there corresponding values in it and write in firestore.
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("bet_amount", amount);
                                    map.put("selected", result2);
                                    map.put("id", currentGameId);
                                    map.put("isWinner", false);
                                    map.put("status", false);
                                    Calendar cal = Calendar.getInstance();
                                    SimpleDateFormat spf = new SimpleDateFormat("dd-MM-yyyy, hh:mm:ss", Locale.getDefault());
                                    String dateAndTime = spf.format(cal.getTime());//it format date and time that returns from cal.getTime() to
                                    //passed format of constructor.
                                    map.put("createdAt", dateAndTime);
                                    map.put("name", sharedPreferences.getName());
                                    map.put("result", gameResultNumber);
                                    map.put("timestamp", System.currentTimeMillis());
                                    map.put("user_id", sharedPreferences.getId());
                                    map.put("win_amount", 0);
                                    map.put("isThreeMin", isThreeMin);
                                    firestore.collection("bets").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            //first get bet amount on user selected number or image then get that amount + user bet amount and update selected number or image
                                            //field in gameHistory document.
                                            DocumentReference docRef1 = firestore.collection("gameHistory").document(currentGameId + "");
                                            if (!isThreeMin) {
                                                docRef1 = firestore.collection("gameHistoryOneMin").document(currentGameId + "");
                                            }
                                            DocumentReference finalDocRef = docRef1;
                                            docRef1.get()
                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            long selectedPrevBetAmt = task.getResult().getLong(result2);
                                                            long newTotalBetAmt = (selectedPrevBetAmt + amount);
                                                            HashMap<String, Object> map = new HashMap<>();
                                                            map.put(result2, newTotalBetAmt);
                                                            finalDocRef.update(map)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void unused) {
                                                                            Log.v(tag, "done");
                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Toast.makeText(getActivity(), "Check internet connection!", Toast.LENGTH_SHORT).show();
                                                                            Log.v(tag, e + "");
                                                                        }
                                                                    });
                                                        }
                                                    });
                                            Toast.makeText(getActivity(), "Bet Successfully Done!", Toast.LENGTH_SHORT).show();
                                            sharedPreferences.setBalance(amt);
                                            binding.walletAmt.setText(Utils.getCurrencySymbol("INR") + amt);
                                            progressDialog.dismiss();
                                            progressDialog.cancel();
                                            dialog.dismiss();
                                            dialog.cancel();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.v(tag, "error-- " + e);
                                            dialog.dismiss();
                                            Toast.makeText(getActivity(), "Check Network Connectivity!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    progressDialog.cancel();
                                    Log.v(tag, "failure");
                                    Toast.makeText(getActivity(), "Check Network Connectivity!", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
        dialogBinding.increment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                dialogBinding.increment.setBackgroundResource(R.drawable.grey_circle_image);
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    dialogBinding.increment.setBackgroundResource(R.drawable.violet_gradient_circle);
                    betInto += 1;
                    //selected bet amount is multiplied by betInto which is incremented
                    dialogBinding.betAmt.setText(Utils.getCurrencySymbol("INR") + (selectedBetAmt * betInto));
                    dialogBinding.betInto.setText("x " + betInto);
                }
                return true;
            }
        });

        dialogBinding.decrement.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                dialogBinding.decrement.setBackgroundResource(R.drawable.grey_circle_image);
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    dialogBinding.decrement.setBackgroundResource(R.drawable.violet_gradient_circle);
                    if (betInto > 1) {
                        betInto -= 1;
                        String amount = (selectedBetAmt * betInto) + "";
                        //selected bet amount is multiplied by betInto which is incremented
                        dialogBinding.betAmt.setText(Utils.getCurrencySymbol("INR") + amount);
                        dialogBinding.betInto.setText("x " + betInto);
                    }
                }
                return true;
            }
        });

        dialogBinding.ten.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                dialogBinding.ten.setBackgroundResource(R.drawable.grey_rectangle);
                selectedBetAmt = 10;
                String amount = (selectedBetAmt * betInto) + "";
                dialogBinding.betAmt.setText(Utils.getCurrencySymbol("INR") + amount);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (betAmtSelected.matches("10")) {
                        dialogBinding.ten.setBackgroundResource(R.drawable.grey_rectangle);
                    } else if (betAmtSelected.matches("100")) {
                        dialogBinding.hundred.setBackgroundResource(R.drawable.grey_rectangle);

                    } else if (betAmtSelected.matches("1000")) {
                        dialogBinding.thousand.setBackgroundResource(R.drawable.grey_rectangle);

                    } else if (betAmtSelected.matches("10000")) {
                        dialogBinding.tenThousand.setBackgroundResource(R.drawable.grey_rectangle);

                    } else if (betAmtSelected.matches("100000")) {
                        dialogBinding.oneLac.setBackgroundResource(R.drawable.grey_rectangle);
                    }
                    dialogBinding.ten.setBackgroundResource(R.drawable.violet_top_to_bottom_gradient);
                    betAmtSelected = "10";
                }
                return true;//touch is consumed or handled successfully
            }
        });

        dialogBinding.hundred.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                dialogBinding.hundred.setBackgroundResource(R.drawable.grey_rectangle);
                selectedBetAmt = 100;
                String amount = (selectedBetAmt * betInto) + "";
                dialogBinding.betAmt.setText(Utils.getCurrencySymbol("INR") + amount);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (betAmtSelected.matches("10")) {
                        dialogBinding.ten.setBackgroundResource(R.drawable.grey_rectangle);
                    } else if (betAmtSelected.matches("100")) {
                        dialogBinding.hundred.setBackgroundResource(R.drawable.grey_rectangle);

                    } else if (betAmtSelected.matches("1000")) {
                        dialogBinding.thousand.setBackgroundResource(R.drawable.grey_rectangle);

                    } else if (betAmtSelected.matches("10000")) {
                        dialogBinding.tenThousand.setBackgroundResource(R.drawable.grey_rectangle);

                    } else if (betAmtSelected.matches("100000")) {
                        dialogBinding.oneLac.setBackgroundResource(R.drawable.grey_rectangle);
                    }
                    dialogBinding.hundred.setBackgroundResource(R.drawable.violet_top_to_bottom_gradient);
                    betAmtSelected = "100";
                }
                return true;//touch is consumed or handled successfully
            }
        });

        dialogBinding.thousand.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                dialogBinding.thousand.setBackgroundResource(R.drawable.grey_rectangle);
                selectedBetAmt = 1000;
                String amount = (selectedBetAmt * betInto) + "";
                dialogBinding.betAmt.setText(Utils.getCurrencySymbol("INR") + amount);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (betAmtSelected.matches("10")) {
                        dialogBinding.ten.setBackgroundResource(R.drawable.grey_rectangle);
                    } else if (betAmtSelected.matches("100")) {
                        dialogBinding.hundred.setBackgroundResource(R.drawable.grey_rectangle);

                    } else if (betAmtSelected.matches("1000")) {
                        dialogBinding.thousand.setBackgroundResource(R.drawable.grey_rectangle);

                    } else if (betAmtSelected.matches("10000")) {
                        dialogBinding.tenThousand.setBackgroundResource(R.drawable.grey_rectangle);
                    } else if (betAmtSelected.matches("100000")) {
                        dialogBinding.oneLac.setBackgroundResource(R.drawable.grey_rectangle);
                    }
                    dialogBinding.thousand.setBackgroundResource(R.drawable.violet_top_to_bottom_gradient);
                    betAmtSelected = "1000";
                }
                return true;//touch is consumed or handled successfully
            }
        });

        dialogBinding.tenThousand.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                dialogBinding.tenThousand.setBackgroundResource(R.drawable.grey_rectangle);
                selectedBetAmt = 10000;
                String amount = (selectedBetAmt * betInto) + "";
                dialogBinding.betAmt.setText(Utils.getCurrencySymbol("INR") + amount);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (betAmtSelected.matches("10")) {
                        dialogBinding.ten.setBackgroundResource(R.drawable.grey_rectangle);
                    } else if (betAmtSelected.matches("100")) {
                        dialogBinding.hundred.setBackgroundResource(R.drawable.grey_rectangle);

                    } else if (betAmtSelected.matches("1000")) {
                        dialogBinding.thousand.setBackgroundResource(R.drawable.grey_rectangle);

                    } else if (betAmtSelected.matches("10000")) {
                        dialogBinding.tenThousand.setBackgroundResource(R.drawable.grey_rectangle);

                    } else if (betAmtSelected.matches("100000")) {
                        dialogBinding.oneLac.setBackgroundResource(R.drawable.grey_rectangle);
                    }
                    dialogBinding.tenThousand.setBackgroundResource(R.drawable.violet_top_to_bottom_gradient);

                    betAmtSelected = "10000";
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    dialogBinding.tenThousand.setBackgroundResource(R.drawable.violet_top_to_bottom_gradient);
                }
                return true;//touch is consumed or handled successfully
            }
        });

        dialogBinding.oneLac.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                dialogBinding.oneLac.setBackgroundResource(R.drawable.grey_rectangle);
                selectedBetAmt = 100000;
                String amount = (selectedBetAmt * betInto) + "";
                dialogBinding.betAmt.setText(Utils.getCurrencySymbol("INR") + amount);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (betAmtSelected.matches("10")) {
                        dialogBinding.ten.setBackgroundResource(R.drawable.grey_rectangle);
                    } else if (betAmtSelected.matches("100")) {
                        dialogBinding.hundred.setBackgroundResource(R.drawable.grey_rectangle);

                    } else if (betAmtSelected.matches("1000")) {
                        dialogBinding.thousand.setBackgroundResource(R.drawable.grey_rectangle);

                    } else if (betAmtSelected.matches("10000")) {
                        dialogBinding.tenThousand.setBackgroundResource(R.drawable.grey_rectangle);

                    } else if (betAmtSelected.matches("100000")) {
                        dialogBinding.oneLac.setBackgroundResource(R.drawable.grey_rectangle);
                    }
                    dialogBinding.oneLac.setBackgroundResource(R.drawable.violet_top_to_bottom_gradient);

                    betAmtSelected = "100000";
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    dialogBinding.oneLac.setBackgroundResource(R.drawable.violet_top_to_bottom_gradient);
                }
                return true;//touch is consumed or handled successfully
            }
        });

        dialogBinding.oneXTV.setOnClickListener(v -> {
            betInto = 1;
            String amount = (selectedBetAmt * betInto) + "";
            dialogBinding.betAmt.setText(Utils.getCurrencySymbol("INR") + amount);
            dialogBinding.betInto.setText("x " + betInto);
        });

        dialogBinding.fiveXTV.setOnClickListener(v -> {
            betInto = 5;
            String amount = (selectedBetAmt * betInto) + "";
            dialogBinding.betAmt.setText(Utils.getCurrencySymbol("INR") + amount);
            dialogBinding.betInto.setText("x " + betInto);
        });

        dialogBinding.tenXTV.setOnClickListener(v -> {
            betInto = 10;
            String amount = (selectedBetAmt * betInto) + "";
            dialogBinding.betAmt.setText(Utils.getCurrencySymbol("INR") + amount);
            dialogBinding.betInto.setText("x " + betInto);
        });

        dialogBinding.twentyXTV.setOnClickListener(v -> {
            betInto = 20;
            String amount = (selectedBetAmt * betInto) + "";
            dialogBinding.betAmt.setText(Utils.getCurrencySymbol("INR") + amount);
            dialogBinding.betInto.setText("x " + betInto);
        });

        dialogBinding.fiftyXTV.setOnClickListener(v -> {
            betInto = 50;
            String amount = (selectedBetAmt * betInto) + "";
            dialogBinding.betAmt.setText(Utils.getCurrencySymbol("INR") + amount);
            dialogBinding.betInto.setText("x " + betInto);
        });

        dialogBinding.hundredXTV.setOnClickListener(v -> {
            betInto = 100;
            String amount = (selectedBetAmt * betInto) + "";
            dialogBinding.betAmt.setText(Utils.getCurrencySymbol("INR") + amount);
            dialogBinding.betInto.setText("x " + betInto);
        });

        dialog.setContentView(dialogBinding.getRoot());
        dialog.show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListener() {

        binding.withdraw.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                binding.withdraw.setBackgroundResource(R.drawable.grey_rectangle);
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    loadFragment(new WithdrawFragment(), false);
                    binding.withdraw.setBackground(null);
                }
                return true;
            }
        });

        binding.winGo1minBox.setOnClickListener(v -> {
            isThreeMin = false;
            if (countDownTimer != null)
                countDownTimer.cancel();

            binding.winGo1minBox.setBackgroundResource(R.drawable.tint_stroke);
            binding.winGo1minBox.setBackgroundTintList(null);

            binding.winGo3minBox.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));


            if (isNetworkConnected()) {
                getSecondsAndStartCountDown();
                getGameResult();
            } else {
                openAlertDialog();
            }
        });

        binding.winGo3minBox.setOnClickListener(v -> {
            isThreeMin = true;
            if (countDownTimer != null)
                countDownTimer.cancel();

            binding.winGo3minBox.setBackgroundResource(R.drawable.tint_stroke);
            binding.winGo3minBox.setBackgroundTintList(null);

            binding.winGo1minBox.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));

            if (isNetworkConnected()) {
                getSecondsAndStartCountDown();
                getGameResult();
            } else {
                openAlertDialog();
            }
        });
        //OnTouchListener for recharge button
        binding.recharge.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                binding.recharge.setBackgroundResource(R.drawable.grey_rectangle);
                //if touch is up then execute below if block
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    loadFragment(new RechargeFragment(), false);
                    binding.recharge.setBackground(null);
                }
                return true;//touch event is consumed successfully.
            }
        });

        //OnTouchListener for game rules TextView
        binding.gameRules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDia dialog = new AlertDia();
                dialog.show(getChildFragmentManager(), "tag");
            }
        });
        /*new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                binding.gameRules.setBackgroundColor(getResources().getColor(R.color.bitcoinColor));
                //if touch is up then
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    binding.gameRules.setBackgroundColor(getResources().getColor(R.color.white));
                    AlertDia dialog = new AlertDia();
                    dialog.show(getChildFragmentManager(), "tag");
                }

                return true;//return true means the touch event is successfully handled or consumed
            }
        });*/

        //set game numbers and image touch listener.
        binding.greenCont.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                /*if (BuildConfig.DEBUG) {
                    openBetBottomSheet(-1, Constants.GREEN);
                }*/
                if (isThreeMin) {
                    if (num < 150) {
                        selectedImg = Constants.GREEN;
                        selectedNum = 2;
                        binding.greenCont.setBackgroundResource(R.color.bitcoinColor);
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                            openBetBottomSheet(-1, selectedImg);
                            binding.greenCont.setBackgroundResource(R.drawable.btn_number_green);
                        } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    binding.greenCont.setBackgroundResource(R.drawable.btn_number_green);
                                }
                            }, 200);
                        }
                    }
                } else if (oneMinCounter > 30) {
                    selectedImg = Constants.GREEN;
                    selectedNum = 2;
                    binding.greenCont.setBackgroundResource(R.color.bitcoinColor);
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                        openBetBottomSheet(-1, selectedImg);
                        binding.greenCont.setBackgroundResource(R.drawable.btn_number_green);
                    } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                binding.greenCont.setBackgroundResource(R.drawable.btn_number_green);
                            }
                        }, 200);
                    }
                }
                return true;//touch event is consumed
            }
        });
        binding.blueCont.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isThreeMin) {
                    if (num < 150) {
                        selectedImg = Constants.BLUE;
                        selectedNum = 0;
                        binding.blueCont.setBackgroundResource(R.color.bitcoinColor);
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                            openBetBottomSheet(-1, selectedImg);
                            binding.blueCont.setBackgroundResource(R.drawable.btn_number_blue);
                        } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    binding.blueCont.setBackgroundResource(R.drawable.btn_number_blue);
                                }
                            }, 200);
                        }
                    }
                } else if (oneMinCounter > 30) {
                    selectedImg = Constants.BLUE;
                    selectedNum = 0;
                    binding.blueCont.setBackgroundResource(R.color.bitcoinColor);
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                        openBetBottomSheet(-1, selectedImg);
                        binding.blueCont.setBackgroundResource(R.drawable.btn_number_blue);
                    } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                binding.blueCont.setBackgroundResource(R.drawable.btn_number_blue);
                            }
                        }, 200);
                    }
                }
                return true;//touch event is consumed
            }
        });
        binding.redCont.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isThreeMin) {
                    if (num < 150) {
                        selectedImg = Constants.RED;
                        selectedNum = 3;
                        binding.redCont.setBackgroundResource(R.color.bitcoinColor);
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                            openBetBottomSheet(-1, selectedImg);
                            binding.redCont.setBackgroundResource(R.drawable.btn_number_red);
                        } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    binding.redCont.setBackgroundResource(R.drawable.btn_number_red);
                                }
                            }, 200);
                        }
                    }
                } else if (oneMinCounter > 30) {
                    selectedImg = Constants.RED;
                    selectedNum = 3;
                    binding.redCont.setBackgroundResource(R.color.bitcoinColor);
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                        openBetBottomSheet(-1, selectedImg);
                        binding.redCont.setBackgroundResource(R.drawable.btn_number_red);
                    } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                binding.redCont.setBackgroundResource(R.drawable.btn_number_red);
                            }
                        }, 200);
                    }
                }
                return true;//touch event is consumed
            }
        });
        binding.zero.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isThreeMin) {
                    if (num < 150) {
                        selectedImg = Constants.BLUE;
                        selectedNum = 0;
                        binding.zero.setBackgroundResource(R.color.bitcoinColor);
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            openBetBottomSheet(selectedNum, selectedImg);
                            binding.zero.setBackground(null);
                        } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    binding.zero.setBackground(null);
                                }
                            }, 200);
                        }
                    }
                } else if (oneMinCounter > 30) {
                    selectedImg = Constants.BLUE;
                    selectedNum = 0;
                    binding.zero.setBackgroundResource(R.color.bitcoinColor);
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                        openBetBottomSheet(selectedNum, selectedImg);
                        binding.zero.setBackground(null);
                    } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                binding.zero.setBackground(null);
                            }
                        }, 200);
                    }
                }
                return true;//touch event is consumed
            }
        });
        binding.one.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isThreeMin) {
                    if (num < 150) {
                        selectedImg = Constants.GREEN;
                        selectedNum = 1;
                        binding.one.setBackgroundResource(R.color.bitcoinColor);
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                            openBetBottomSheet(selectedNum, selectedImg);
                            binding.one.setBackground(null);
                        } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    binding.one.setBackground(null);
                                }
                            }, 200);
                        }
                    }
                } else if (oneMinCounter > 30) {
                    selectedImg = Constants.GREEN;
                    selectedNum = 1;
                    binding.one.setBackgroundResource(R.color.bitcoinColor);
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                        openBetBottomSheet(selectedNum, selectedImg);
                        binding.one.setBackground(null);
                    } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                binding.one.setBackground(null);
                            }
                        }, 200);
                    }
                }
                return true;//touch event is consumed
            }
        });
        binding.two.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isThreeMin) {
                    if (num < 150) {
                        selectedImg = Constants.RED;
                        selectedNum = 2;
                        binding.two.setBackgroundResource(R.color.bitcoinColor);
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                            openBetBottomSheet(selectedNum, selectedImg);
                            binding.two.setBackground(null);
                        } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    binding.two.setBackground(null);
                                }
                            }, 200);
                        }
                    }
                } else if (oneMinCounter > 30) {
                    selectedImg = Constants.RED;
                    selectedNum = 2;
                    binding.two.setBackgroundResource(R.color.bitcoinColor);
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                        openBetBottomSheet(selectedNum, selectedImg);
                        binding.two.setBackground(null);
                    } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                binding.two.setBackground(null);
                            }
                        }, 200);
                    }
                }
                return true;//touch event is consumed
            }
        });
        binding.three.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isThreeMin) {
                    if (num < 150) {
                        selectedImg = Constants.GREEN;
                        selectedNum = 3;
                        binding.three.setBackgroundResource(R.color.bitcoinColor);
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                            openBetBottomSheet(selectedNum, selectedImg);
                            binding.three.setBackground(null);
                        } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    binding.three.setBackground(null);
                                }
                            }, 200);
                        }
                    }
                } else if (oneMinCounter > 30) {
                    selectedImg = Constants.GREEN;
                    selectedNum = 3;
                    binding.three.setBackgroundResource(R.color.bitcoinColor);
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                        openBetBottomSheet(selectedNum, selectedImg);
                        binding.three.setBackground(null);
                    } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                binding.three.setBackground(null);
                            }
                        }, 200);
                    }
                }
                return true;//touch event is consumed
            }
        });
        binding.four.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isThreeMin) {
                    if (num < 150) {
                        selectedImg = Constants.RED;
                        selectedNum = 4;
                        binding.four.setBackgroundResource(R.color.bitcoinColor);
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                            openBetBottomSheet(selectedNum, selectedImg);
                            binding.four.setBackground(null);
                        } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    binding.four.setBackground(null);
                                }
                            }, 200);
                        }
                    }
                } else if (oneMinCounter > 30) {
                    selectedImg = Constants.RED;
                    selectedNum = 4;
                    binding.four.setBackgroundResource(R.color.bitcoinColor);
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                        openBetBottomSheet(selectedNum, selectedImg);
                        binding.four.setBackground(null);
                    } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                binding.four.setBackground(null);
                            }
                        }, 200);
                    }
                }
                return true;//touch event is consumed
            }
        });
        binding.five.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isThreeMin) {
                    if (num < 150) {
                        selectedImg = Constants.BLUE;
                        selectedNum = 5;
                        binding.five.setBackgroundResource(R.color.bitcoinColor);
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                            openBetBottomSheet(selectedNum, selectedImg);
                            binding.five.setBackground(null);
                        } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    binding.five.setBackground(null);
                                }
                            }, 200);
                        }
                    }
                } else if (oneMinCounter > 30) {
                    selectedImg = Constants.BLUE;
                    selectedNum = 5;
                    binding.five.setBackgroundResource(R.color.bitcoinColor);
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                        openBetBottomSheet(selectedNum, selectedImg);
                        binding.five.setBackground(null);
                    } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                binding.five.setBackground(null);
                            }
                        }, 200);
                    }
                }
                return true;//touch event is consumed
            }
        });
        binding.six.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isThreeMin) {
                    if (num < 150) {
                        selectedImg = Constants.RED;
                        selectedNum = 6;
                        binding.six.setBackgroundResource(R.color.bitcoinColor);
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                            openBetBottomSheet(selectedNum, selectedImg);
                            binding.six.setBackground(null);
                        } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    binding.six.setBackground(null);
                                }
                            }, 200);
                        }
                    }
                } else if (oneMinCounter > 30) {
                    selectedImg = Constants.RED;
                    selectedNum = 6;
                    binding.six.setBackgroundResource(R.color.bitcoinColor);
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                        openBetBottomSheet(selectedNum, selectedImg);
                        binding.six.setBackground(null);
                    } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                binding.six.setBackground(null);
                            }
                        }, 200);
                    }
                }
                return true;//touch event is consumed
            }
        });
        binding.seven.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isThreeMin) {
                    if (num < 150) {
                        selectedImg = Constants.GREEN;
                        selectedNum = 7;
                        binding.seven.setBackgroundResource(R.color.bitcoinColor);
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                            openBetBottomSheet(selectedNum, selectedImg);
                            binding.seven.setBackground(null);
                        } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    binding.seven.setBackground(null);
                                }
                            }, 200);
                        }
                    }
                } else if (oneMinCounter > 30) {
                    selectedImg = Constants.GREEN;
                    selectedNum = 7;
                    binding.seven.setBackgroundResource(R.color.bitcoinColor);
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                        openBetBottomSheet(selectedNum, selectedImg);
                        binding.seven.setBackground(null);
                    } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                binding.seven.setBackground(null);
                            }
                        }, 200);
                    }
                }
                return true;//touch event is consumed
            }
        });
        binding.eight.setOnTouchListener((view, motionEvent) -> {
            if (isThreeMin) {
                if (num < 150) {
                    selectedImg = Constants.RED;
                    selectedNum = 8;
                    binding.eight.setBackgroundResource(R.color.bitcoinColor);
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                        openBetBottomSheet(selectedNum, selectedImg);
                        binding.eight.setBackground(null);
                    } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                binding.eight.setBackground(null);
                            }
                        }, 200);
                    }
                }
            } else if (oneMinCounter > 30) {
                selectedImg = Constants.RED;
                selectedNum = 8;
                binding.eight.setBackgroundResource(R.color.bitcoinColor);
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                    openBetBottomSheet(selectedNum, selectedImg);
                    binding.eight.setBackground(null);
                } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            binding.eight.setBackground(null);
                        }
                    }, 200);
                }
            }
            return true;//touch event is consumed
        });
        binding.nine.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isThreeMin) {
                    if (num < 150) {
                        selectedImg = Constants.GREEN;
                        selectedNum = 9;
                        binding.nine.setBackgroundResource(R.color.bitcoinColor);
                        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                            openBetBottomSheet(selectedNum, selectedImg);
                            binding.nine.setBackground(null);
                        } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    binding.nine.setBackground(null);
                                }
                            }, 200);
                        }
                    }
                } else if (oneMinCounter > 30) {
                    selectedImg = Constants.GREEN;
                    selectedNum = 9;
                    binding.nine.setBackgroundResource(R.color.bitcoinColor);
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        //openBetBottomSheet() method is open BottomSheetDialog Ui Component
                        openBetBottomSheet(selectedNum, selectedImg);
                        binding.nine.setBackground(null);
                    } else if (motionEvent.getAction() != MotionEvent.ACTION_DOWN && motionEvent.getAction() != MotionEvent.ACTION_UP) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                binding.nine.setBackground(null);
                            }
                        }, 200);
                    }
                }
                return true;//touch event is consumed
            }
        });
    }

    private void loadFragment(Fragment fragment, boolean isAdd) {
        if (!requireActivity().isFinishing()) {
            FragmentTransaction ft = requireActivity()
                    .getSupportFragmentManager()
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
