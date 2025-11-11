package com.example.winzgo.fragments.wingo;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.winzgo.MainActivity;
import com.example.winzgo.R;
import com.example.winzgo.adapter.GameHistoryAdapter;
import com.example.winzgo.adapter.TransactionsAdapter;
import com.example.winzgo.databinding.FragmentMoneyBinding;
import com.example.winzgo.models.GameHistoryModel;
import com.example.winzgo.models.TransactionsModel;
import com.example.winzgo.sharedpref.SessionSharedPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


public class MoneyFragment extends Fragment {
    ProgressDialog dialog;
    static SessionSharedPref sharedPreferences;
    static FirebaseFirestore firestore;
    int i;
    static String tag = "MoneyFragment.java";
    private FragmentMoneyBinding binding;
    private GameHistoryAdapter gameHistoryAdapter;
    private List<GameHistoryModel> list = new ArrayList<>();
    private long prevGameId = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMoneyBinding.inflate(inflater, container, false);
        FragmentManager fm = getFragmentManager();
        firestore = FirebaseFirestore.getInstance();
        sharedPreferences = new SessionSharedPref(getActivity());
        MainActivity.i = 0;
        dialog = new ProgressDialog(getActivity());
        gameHistoryAdapter = new GameHistoryAdapter(new ArrayList<>());

        binding.swipeRefreshLy.setColorSchemeColors(getResources().getColor(R.color.light_tint));
        if (isNetworkAvailable()) {
            setListeners();
            getDataForGameHistoryList();
            getDataForTransactionsList();
        } else {
            showNoNetworkDialog();
        }

        i = 0;

//        binding.bankVG.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                binding.addBankAccount.setBackgroundResource(R.drawable.grey_circle_image);
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    loadFragment(new AddBankDetailsFragment());
//                    binding.addBankAccount.setBackgroundResource(R.drawable.bank_ic);
//                }
//                return true;
//            }
//        });
//
//        binding.withdrawVG.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                binding.withdraw.setBackgroundResource(R.drawable.grey_circle_image);
//                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//                    loadFragment(new WithdrawFragment());
//                    binding.withdraw.setBackgroundResource(R.drawable.withdraw_ic);
//                }
//                return true;
//            }
//        });

        return binding.getRoot();
    }

    public void getDataForGameHistoryList() {
        prevGameId = (Long.parseLong(sharedPreferences.getGameId()));
        firestore.collection("ids").document("game_id").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                long threeMinId = documentSnapshot.getLong("id");
                firestore.collection("ids").document("oneMinuteGameId").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        long oneMinId = documentSnapshot.getLong("oneMinId");
                        prevGameId = threeMinId + oneMinId;
                        Log.v(tag, "" + prevGameId);
                        firestore.collection("bets").whereEqualTo("user_id", sharedPreferences.getId()).whereLessThan("id", prevGameId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                binding.swipeRefreshLy.setRefreshing(false);
                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                    list = new ArrayList<>();
                                    List<DocumentSnapshot> list1 = task.getResult().getDocuments();
                                    if (list1.size() <= 0) {
                                        Toast.makeText(getActivity(), "No bets found", Toast.LENGTH_SHORT).show();
                                    } else {
                                        //Comparator is an interface of Collection framework which can sort the ArrayList on behalf of its object field.
                                        class idSortComparator implements Comparator<DocumentSnapshot> {
                                            @Override
                                            public int compare(DocumentSnapshot obj1, DocumentSnapshot obj2) {
                                                if (obj1.getLong("id") == obj2.getLong("id")) {
                                                    return 0;
                                                } else if (obj1.getLong("id") > obj2.getLong("id")) {
                                                    return 1;
                                                } else {
                                                    return -1;
                                                }
                                            }
                                        }
                                        Collections.sort(list1, new idSortComparator());
                                        for (int i = list1.size() - 1; i >= 0; i--) {
                                            DocumentSnapshot doc = list1.get(i);
                                            long id = doc.getLong("id");
                                            if ((id == oneMinId) || (id == threeMinId)) {
                                                continue;
                                            }

                                            list.add(new GameHistoryModel(id, doc.getLong("bet_amount"), doc.getLong("win_amount"), doc.getLong("result"), doc.getString("selected"), doc.getString("createdAt"), doc.getLong("timestamp")));
                                        }
                                        gameHistoryAdapter = new GameHistoryAdapter((ArrayList<GameHistoryModel>) list);
                                        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                                        binding.gameHistoryList.setAdapter(gameHistoryAdapter);
                                        binding.gameHistoryList.setLayoutManager(layoutManager);
                                    }
                                } else if (task.isSuccessful()) {
                                    Log.v("MoneyFragment.java", "no bets found");
                                } else {
                                    Log.v("MoneyFragment.java", task.getException() + "");
                                    Toast.makeText(getActivity(), task.getException().getMessage() + "", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    public void getDataForTransactionsList() {
        ArrayList<TransactionsModel> list = new ArrayList<>();

        firestore.collection("transactions").whereEqualTo("user_id", sharedPreferences.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    List<DocumentSnapshot> list2 = task.getResult().getDocuments();
                    if (list2.size() <= 0) {
                        Toast.makeText(getActivity(), "No Transactions Found", Toast.LENGTH_SHORT).show();
                    }
                    for (int i = list2.size() - 1; i >= 0; i--) {
                        DocumentSnapshot doc = list2.get(i);
                        list.add(new TransactionsModel(doc.getString("status"), doc.getString("type"), doc.getString("date"), doc.getString("amount")));
                    }
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                    TransactionsAdapter adapter = new TransactionsAdapter(requireActivity(), list);
                    binding.transactionsList.setAdapter(adapter);
                    binding.transactionsList.setLayoutManager(layoutManager);
                } else if (task.isSuccessful()) {
                    try {
                        Toast.makeText(getActivity(), "No transactions found", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.v(tag, e + "");
                    }

                } else {
                    Log.v(tag, task.getException() + "");
                }
            }
        });
    }

    private void filterBy(boolean allBets, boolean isWin, boolean isLosses, boolean isDate, long filterDateTS) {
        List<GameHistoryModel> filterList = new ArrayList<>();
        if (allBets) {
            if (isNetworkAvailable()) {
                getDataForGameHistoryList();
            } else {
                showNoNetworkDialog();
            }
        } else if (isWin) {
            for (int i = 0; i < list.size(); i++) {
                GameHistoryModel item = list.get(i);
                if (item.getWin() > 0) {
                    filterList.add(item);
                }
            }

        } else if (isLosses) {
            for (int i = 0; i < list.size(); i++) {
                GameHistoryModel item = list.get(i);
                if (item.getWin() == 0) {
                    filterList.add(item);
                }
            }
        } else if (isDate) {
            for (int i = 0; i < list.size(); i++) {
                GameHistoryModel item = list.get(i);
                if (item.getTimestamp() <= filterDateTS) {
                    filterList.add(item);
                }
            }
        }
        gameHistoryAdapter.filter((ArrayList<GameHistoryModel>) filterList);
    }

    public void setListeners() {
        binding.swipeRefreshLy.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isNetworkAvailable()) {
                    setListeners();
                    getDataForGameHistoryList();
                    getDataForTransactionsList();
                } else {
                    showNoNetworkDialog();
                }
            }
        });
        binding.filterDateTv.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog pickerDialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                    cal.set(y, m, d);
                    SimpleDateFormat spf = new SimpleDateFormat("dd:MM:yyyy", Locale.getDefault());
                    binding.filterDateTv.setText("Till - " + spf.format(cal.getTime()));
                    filterBy(false, false, false, true, cal.getTimeInMillis());
                }
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

            pickerDialog.getDatePicker().setMaxDate(cal.getTimeInMillis());

            pickerDialog.show();
        });

        binding.allBetsTV.setOnClickListener(v -> {
            filterBy(true, false, false, true, 0L);
        });

        binding.winsTv.setOnClickListener(v -> {
            filterBy(false, true, false, true, 0L);
        });

        binding.lossesTv.setOnClickListener(v -> {
            filterBy(false, false, true, true, 0L);
        });
        //setOnClickListener on gameHistory and do fragment transaction gameHistory replace container with GameHistoryFragment
        //and change background to grey_circle_image drawable shape
        binding.gameHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.gameHistory.setBackgroundResource(R.drawable.tint_round_shape);
                binding.transaction.setBackgroundResource(R.drawable.grey_circle_image);
                binding.gameHistoryLy.setVisibility(View.VISIBLE);
                binding.tradeHeadLy.setVisibility(View.VISIBLE);
                binding.transactionsLy.setVisibility(View.GONE);
                binding.transactionsHeadLy.setVisibility(View.GONE);
            }
        });

        binding.transaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.transaction.setBackgroundResource(R.drawable.tint_round_shape);
                binding.gameHistory.setBackgroundResource(R.drawable.grey_circle_image);
                binding.gameHistoryLy.setVisibility(View.GONE);
                binding.tradeHeadLy.setVisibility(View.GONE);
                binding.transactionsLy.setVisibility(View.VISIBLE);
                binding.transactionsHeadLy.setVisibility(View.VISIBLE);
            }
        });
    }

    public void showNoNetworkDialog() {
        //Create Builder class instance which is construct AlertDialog Ui or layout
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Check Network Connection!");
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (isNetworkAvailable()) {
                    setListeners();
                    getDataForGameHistoryList();
                    getDataForTransactionsList();
                } else {
                    showNoNetworkDialog();
                }
            }
        });
        builder.create().show();//call show() method to show AlertDialog which is returns from Builder class instance create() method.
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
    }

    @Override
    public void onPause() {
        Log.v("Money---", "paused");
        //it pop all fragment from this fragment back stack
        getActivity().getSupportFragmentManager().popBackStack("", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        super.onPause();
        super.onStop();
        super.onDestroyView();
        super.onDestroy();
        super.onDetach();
    }

    private void loadFragment(Fragment fragment) {
        if (!requireActivity().isFinishing())
            requireActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.container, fragment).commit();
    }
}
