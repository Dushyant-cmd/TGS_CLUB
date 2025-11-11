package com.example.winzgo.fragments.wingo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.winzgo.R;
import com.example.winzgo.adapter.GameHistoryAdapter;
import com.example.winzgo.models.GameHistoryModel;
import com.example.winzgo.sharedpref.SessionSharedPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment which display a RecyclerView list to display Game History of user
 */
public class GameHistoryFragment extends Fragment {
    ArrayList<GameHistoryModel> list;
    FirebaseFirestore firestore;
    RecyclerView recyclerView;
    SessionSharedPref sharedPreferences;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_game_history, container, false);
        list = new ArrayList<GameHistoryModel>();
        firestore = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recyclerViewHistory);
        sharedPreferences = new SessionSharedPref(requireActivity());
        getDataForList();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        //set layout manager on recycler view
        recyclerView.setLayoutManager(linearLayoutManager);
        return view;
    }

    public void getDataForList() {
        firestore.collection("bets").whereEqualTo("user_id", sharedPreferences.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful() && !task.getResult().isEmpty()) {
                    List<DocumentSnapshot> list1 = task.getResult().getDocuments();
                    for(int i=0; i<list1.size(); i++) {
                        DocumentSnapshot doc = list1.get(i);
                        list.add(new GameHistoryModel(doc.getLong("id"), doc.getLong("bet_amount"), doc.getLong("win_amount"),
                                doc.getLong("result"), doc.getString("resultImg"), doc.getString("createdAt"), doc.getLong("timestamp")));
                    }
                    //Adapter of recycler view list
                    GameHistoryAdapter adapter = new GameHistoryAdapter(list);
                    recyclerView.setAdapter(adapter);
                } else if(task.isSuccessful()){
                    Toast.makeText(getActivity(), "No bets found", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), task.getException() + "", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public boolean isNetworkAvailable() {
        //ConnectivityManager class object contains all the information about app connectivity state and it is the service of device
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        //check connectivity of mobile with data is connected or with wifi then return true if both false then false
        return connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.
                getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
    }

    @Override
    public void onStop() {
        super.onStop();
        super.onDestroy();
    }
}