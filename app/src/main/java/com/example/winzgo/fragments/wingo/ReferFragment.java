package com.example.winzgo.fragments.wingo;

import static com.example.winzgo.utils.Constants.isNetworkConnected;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.winzgo.MainActivity;
import com.example.winzgo.R;
import com.example.winzgo.adapter.ReferListAdapter;
import com.example.winzgo.models.ReferListModel;
import com.example.winzgo.sharedpref.SessionSharedPref;
import com.example.winzgo.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * Fragment display all the refer of user and button to refer.
 */
public class ReferFragment extends Fragment {

    public ReferFragment() {
        // Required empty public constructor
    }

    //recycler view list
    RecyclerView recyclerView;
    //ArrayList of list item data object class or model type
    ArrayList<ReferListModel> list;
    //Refer button
    FrameLayout referBtn;
    ImageView upBtn;
    SessionSharedPref sharedPreferences;
    TextView totalReferEarn;
    FirebaseFirestore firestore;
    String tag = "ReferFragment.java";
    TextView noRefer;
    private String appDownloadLink;
    private ProgressBar bar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_refer, container, false);
        View view = inflater.inflate(R.layout.fragment_refer, container, false);
        ((MainActivity) getActivity()).setupHeader("refer");
        recyclerView = view.findViewById(R.id.listRefer);
        sharedPreferences = new SessionSharedPref(getActivity());
        firestore = FirebaseFirestore.getInstance();
        totalReferEarn = view.findViewById(R.id.totalEarnRefer);
        totalReferEarn.setText("Total Referral Earning: " + sharedPreferences.getRefer());
        bar = view.findViewById(R.id.bar);
        noRefer = view.findViewById(R.id.noRefer);
        upBtn = view.findViewById(R.id.upBtn);
//        MainActivity.i = 101;
        //set data in recycler view adapter data source
        list = new ArrayList<>();
        if (isNetworkAvailable()) {
            getReferListData();
            queries();
        } else {
            Toast.makeText(getActivity(), "Check internet connection", Toast.LENGTH_SHORT).show();
        }
        referBtn = view.findViewById(R.id.shareRefer);

        getUserData();
        upBtn.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStackImmediate();
        });
        return view;
    }


    private void getReferListData() {
        firestore.collection("promotion").whereEqualTo("referralId", "" + sharedPreferences.getId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    ArrayList<DocumentSnapshot> list2 = (ArrayList<DocumentSnapshot>) task.getResult().getDocuments();
                    for (int i = 0; i < list2.size(); i++) {
                        DocumentSnapshot doc = list2.get(i);
                        if (doc.getBoolean("isRecharged")) {
                            list.add(new ReferListModel(doc.getString("name"), "yes", "yes"));
                        } else {
                            list.add(new ReferListModel(doc.getString("name"), "yes", "no"));
                        }
                        bar.setVisibility(View.GONE);
                        //layout manager which display all list item in linear 1 column and get item ViewHolder and bind
                        //data to it by calling methods on Adapter.
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                        ReferListAdapter adapter = new ReferListAdapter(list);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setLayoutManager(linearLayoutManager);
                    }
                } else if (task.isSuccessful()) {
                    bar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    noRefer.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(), "No referral found!", Toast.LENGTH_SHORT).show();
                } else {
                    bar.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), "Check internet connection", Toast.LENGTH_SHORT).show();
                    Log.v(tag, task.getException() + "");
                }
            }
        });
    }

    public void queries() {
        firestore.collection("constants").document("3").get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            appDownloadLink = task.getResult().getString("appDownloadLink");
                            setListener();
                        } else {
                            Toast.makeText(getActivity(), "Check internet connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cM = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cM.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                || cM.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;
    }

    private void setListener() {
        referBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //change refer btn background to grey when touch
                referBtn.setBackgroundResource(R.drawable.grey_circle_image);
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    String downloadLink = appDownloadLink;
                    //EXTRA_TEXT which is send with refer(share)
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Download The TGS club & Earn money. Use my referral code : " +
                            sharedPreferences.getId() + " | Download Now: "
                            + downloadLink);
                    //type of intent
                    shareIntent.setType("text/plain");
                    startActivity(shareIntent);
                    //change refer btn background to white when touch is up
                    referBtn.setBackgroundResource(R.drawable.white_circle_shape);
                }
                return true;//touch is handled(consumed)
            }
        });
    }

    private void getUserData() {
        if (isNetworkConnected(getActivity())) {
            long userId = SessionSharedPref.getLong(requireContext(), Constants.USER_ID_KEY, 0L);
            if (userId != 0L) {
                firestore.collection("users").document(String.valueOf(userId))
                        .get().addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                String refer = task.getResult().getString("refer");
                                totalReferEarn.setText("Total Referral Earning: " + refer);
                            }
                        });
            }
        } else {
            Toast.makeText(getContext(), "No internet", Toast.LENGTH_SHORT).show();
        }
    }
}