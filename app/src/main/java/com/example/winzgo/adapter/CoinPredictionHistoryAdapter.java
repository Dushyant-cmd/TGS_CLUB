package com.example.winzgo.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.winzgo.R;
import com.example.winzgo.databinding.CoinPredictionHistoryListItemBinding;
import com.example.winzgo.databinding.CoinTradeTransactionsListItemBinding;
import com.example.winzgo.databinding.TradeProHistoryListItemBinding;
import com.example.winzgo.models.CoinPredictionHistoryModel;
import com.example.winzgo.models.CoinPredictionHistoryModel;
import com.example.winzgo.models.TransactionsModel;
import com.example.winzgo.utils.Constants;

import java.util.List;

public class CoinPredictionHistoryAdapter extends RecyclerView.Adapter<CoinPredictionHistoryAdapter.ViewHolder> {

    private AsyncListDiffer<CoinPredictionHistoryModel> asyncListDiffer = new AsyncListDiffer<>(this, new DiffUtil.ItemCallback<CoinPredictionHistoryModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull CoinPredictionHistoryModel oldItem, @NonNull CoinPredictionHistoryModel newItem) {
            return oldItem.getTimestamp() == newItem.getTimestamp();
        }

        @Override
        public boolean areContentsTheSame(@NonNull CoinPredictionHistoryModel oldItem, @NonNull CoinPredictionHistoryModel newItem) {
            return oldItem.equals(newItem);
        }
    });

    public void submitList(List<CoinPredictionHistoryModel> list) {
        asyncListDiffer.submitList(list);
    }

    @Override
    public void onBindViewHolder(@NonNull CoinPredictionHistoryAdapter.ViewHolder holder, int position) {
        holder.bind(asyncListDiffer.getCurrentList().get(position));
    }

    @NonNull
    @Override
    public CoinPredictionHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CoinPredictionHistoryListItemBinding binding = CoinPredictionHistoryListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CoinPredictionHistoryAdapter.ViewHolder(binding);
    }

    @Override
    public int getItemCount() {
        return asyncListDiffer.getCurrentList().size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private CoinPredictionHistoryListItemBinding binding;

        public ViewHolder(CoinPredictionHistoryListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CoinPredictionHistoryModel data) {
            String time = data.getDateAndTime().split(",")[1].trim();
            /*binding.tvBetAmt.setText(Constants.RUPEE_ICON + data.getBet_amount());
            binding.tvTradeDesc.setText("Entry: " + Constants.RUPEE_ICON + data.getSelected() + "-> " + "Exit: " + Constants.RUPEE_ICON + data.getResult() + " | " + time);

            if (data.isUp()) {
                // on up
                binding.ivBetGraph.setImageResource(R.drawable.graph_up_ic);
                binding.ivBetGraph.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(binding.ivBetGraph.getContext(), R.color.green)));

                binding.tvUpStatus.setVisibility(View.VISIBLE);
                binding.tvDownStatus.setVisibility(View.GONE);
            } else {
                // on down
                binding.ivBetGraph.setImageResource(R.drawable.graph_down_ic);
                binding.ivBetGraph.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(binding.ivBetGraph.getContext(), R.color.red)));

                binding.tvDownStatus.setVisibility(View.VISIBLE);
                binding.tvUpStatus.setVisibility(View.GONE);
            }

            if (data.isWinner()) {
                binding.tvBetResult.setText("+" + Constants.RUPEE_ICON + data.getWin_amount());
                binding.tvBetResult.setTextColor(ContextCompat.getColor(binding.tvBetResult.getContext(), R.color.green));
            } else {
                binding.tvBetResult.setText("-" + Constants.RUPEE_ICON + data.getBet_amount());
                binding.tvBetResult.setTextColor(ContextCompat.getColor(binding.tvBetResult.getContext(), R.color.red));
            }*/
        }
    }
}
