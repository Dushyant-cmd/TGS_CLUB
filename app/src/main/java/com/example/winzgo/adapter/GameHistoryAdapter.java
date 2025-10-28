package com.example.winzgo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.winzgo.R;
import com.example.winzgo.databinding.GameHistoryListItemBinding;
import com.example.winzgo.models.GameHistoryModel;
import com.example.winzgo.utils.Constants;

import java.util.ArrayList;

public class GameHistoryAdapter extends RecyclerView.Adapter<GameHistoryAdapter.ViewHolder> {
    ArrayList<GameHistoryModel> dataSrc;

    public GameHistoryAdapter(ArrayList<GameHistoryModel> list) {
        this.dataSrc = list;
    }

    @NonNull
    @Override
    public GameHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GameHistoryListItemBinding binding = GameHistoryListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GameHistoryAdapter.ViewHolder holder, int position) {
        GameHistoryModel itemDataObj = dataSrc.get(position);
//        holder.getDateAndTime().setText(itemDataObj.getDateAndTime());
        holder.binding.gameHistoryId.setText(itemDataObj.getId() + "");
        holder.binding.betGameHistory.setText(itemDataObj.getBet() + "Rs");
        holder.binding.winGameHistory.setText(itemDataObj.getWin() + "Rs");
        holder.binding.resultGameHistory.setText(itemDataObj.getResult() + "");

        holder.binding.resultImgCont.setVisibility(View.VISIBLE);
        holder.binding.resultNumTv.setVisibility(View.GONE);
        if (itemDataObj.getSelected().equals(Constants.RED)) {
            holder.binding.selectedGameHistory.setImageResource(R.drawable.red);
        } else if (itemDataObj.getSelected().equals(Constants.GREEN)) {
            holder.binding.selectedGameHistory.setImageResource(R.drawable.green);
        } else if (itemDataObj.getSelected().equals((Constants.BLUE))) {
            holder.binding.selectedGameHistory.setImageResource(R.drawable.blue);
        } else {
            holder.binding.resultImgCont.setVisibility(View.GONE);
            holder.binding.resultNumTv.setVisibility(View.VISIBLE);
            holder.binding.resultNumTv.setText(itemDataObj.getSelected());
        }
    }

    @Override
    public int getItemCount() {
        return dataSrc.size();
    }

    //ViewHolder class
    public class ViewHolder extends RecyclerView.ViewHolder {
        private GameHistoryListItemBinding binding;

        public ViewHolder(@NonNull GameHistoryListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void filter(ArrayList<GameHistoryModel> list) {
        this.dataSrc = list;
        notifyDataSetChanged();
    }
}
