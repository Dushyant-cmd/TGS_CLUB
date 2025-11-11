package com.example.winzgo.fragments.wingo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.winzgo.R;
import com.example.winzgo.adapter.TransactionsAdapter;
import com.example.winzgo.models.TransactionsModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TransactionsFragment extends Fragment {
    RecyclerView recyclerView;
    ArrayList<TransactionsModel> list;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewTransactions);
        Date dateAndTime = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("'On:' dd-MM-yyyy, 'At:' HH:mm:ss", Locale.getDefault());
        String dateAndTimes = dateFormat.format(dateAndTime);
        list = new ArrayList<>();

        getDataForList(dateAndTimes);
        return view;
    }

    private void getDataForList(String dateAndTimes) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        TransactionsAdapter adapter = new TransactionsAdapter(requireActivity(), list);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void onStop() {
        super.onStop();
        super.onDestroy();
    }
}