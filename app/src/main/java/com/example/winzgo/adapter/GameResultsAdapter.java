package com.example.winzgo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.winzgo.BuildConfig;
import com.example.winzgo.R;
import com.example.winzgo.models.GameResult;
import com.example.winzgo.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class GameResultsAdapter extends RecyclerView.Adapter<GameResultsAdapter.ViewHolder> {
    ArrayList<GameResult> dataSrc;
    FirebaseFirestore firestore;
    //Track the index of item in list to display result heading
    private int mTrackItemPosition;
    private Context context;

    //GameResultAdapter constructor which is construct its object
    public GameResultsAdapter(ArrayList<GameResult> listDataSrc) {
        firestore = FirebaseFirestore.getInstance();
        dataSrc = listDataSrc;
    }

    //Returns item object wraps with ViewHolder class object
    @NonNull
    @Override
    public GameResultsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        mTrackItemPosition = 0;//assign with 0 which indicate that Layout Manager is request for
        //ViewHolder of 0th position or bind data for 0th position.
        //item layout resource file java object hierarchy
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_item, parent, false);
        //ViewHolder class object of item layout hierarchy that contains item object hierarchy
        return new ViewHolder(item);
    }

    //bind data to list items
    @Override
    public void onBindViewHolder(@NonNull GameResultsAdapter.ViewHolder holder, int position) {
        GameResult itemDataObject = dataSrc.get(position);
        //trade id of game result.
        holder.getTradeId().setText(itemDataObject.getTradeId() + "");
        //number of game result.
        holder.getNumberResult().setText(itemDataObject.getNumberResult() + "");

//        Log.v("imgId", itemDataObject.getImgResult() + "," + R.id.resultGold);
        if(itemDataObject.getImgResult().equals(Constants.RED)) {
            if(itemDataObject.getHalfWin().matches(Constants.BLUE)) {
                holder.getHalfWin().setImageResource(R.drawable.blue);
                holder.getHalfWin().setVisibility(View.VISIBLE);
            }
            holder.getGreenResult().setImageResource(R.drawable.red);
        } else if(itemDataObject.getImgResult().equals(Constants.GREEN)) {
            if(itemDataObject.getHalfWin().matches(Constants.BLUE)) {
                holder.getHalfWin().setImageResource(R.drawable.blue);
                holder.getHalfWin().setVisibility(View.VISIBLE);
            }
            //if result is gold then add gold drawable in result
            holder.getGreenResult().setImageResource(R.drawable.green);
        } else if(itemDataObject.getImgResult().equals((Constants.BLUE))) {
            holder.getGreenResult().setImageResource(R.drawable.blue);
        }

        if(position == dataSrc.size() - 1) {
            holder.getView().setVisibility(View.GONE);
        } else {
            holder.getView().setVisibility(View.VISIBLE);
        }
//        holder.getResultDate().setText(itemDataObject.getCreatedAt());
    }

    //returns total number of items in list
    @Override
    public int getItemCount() {
        return dataSrc.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tradeId, numberResult, resultDate, resultTime;
        ImageView greenResult;
        ImageView halfWin;
        View view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tradeId = itemView.findViewById(R.id.tradeId);
            numberResult = itemView.findViewById(R.id.numberResult);
            greenResult = itemView.findViewById(R.id.resultGreen);
            halfWin = itemView.findViewById(R.id.halfWin);
            view = itemView.findViewById(R.id.view);
        }

        public void setResultDate(TextView resultDate) {
            this.resultDate = resultDate;
        }

        public TextView getResultTime() {
            return resultTime;
        }

        public void setResultTime(TextView resultTime) {
            this.resultTime = resultTime;
        }

        public void setHalfWin(ImageView halfWin) {
            this.halfWin = halfWin;
        }

        public View getView() {
            return view;
        }

        public void setView(View view) {
            this.view = view;
        }

        public TextView getResultDate() {
            return resultDate;
        }

        public void setGreenResult(ImageView greenResult) {
            this.greenResult = greenResult;
        }

        public ImageView getGreenResult() {
            return greenResult;
        }

        public ImageView getHalfWin() {
            return halfWin;
        }
        public TextView getNumberResult() {
            return numberResult;
        }

        public void setNumberResult(TextView numberResult) {
            this.numberResult = numberResult;
        }

        public TextView getTradeId() {
            return tradeId;
        }

        public void setTradeId(TextView tradeId) {
            this.tradeId = tradeId;
        }
    }
}
