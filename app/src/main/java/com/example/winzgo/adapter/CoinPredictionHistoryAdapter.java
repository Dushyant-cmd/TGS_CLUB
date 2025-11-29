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
            binding.tvAmtSummary.setText(Constants.RUPEE_ICON + data.getBet_amount() + " on " + data.getSelected());
            binding.tvResult.setText("Winner : " + data.getResult());

            if (data.isWinner()) {
                binding.tvWinAmt.setText("+" + Constants.RUPEE_ICON + data.getWin_amount());
                binding.tvWinAmt.setTextColor(ContextCompat.getColor(binding.tvWinAmt.getContext(), R.color.green));
                binding.ivWinType.setImageResource(R.drawable.graph_up_ic);
                binding.ivWinType.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(binding.ivWinType.getContext(), R.color.green)));

                binding.tvResultType.setText("You Won!");
                binding.tvResultType.setTextColor(ContextCompat.getColor(binding.tvWinAmt.getContext(), R.color.green));
            } else {
                binding.tvWinAmt.setText("-" + Constants.RUPEE_ICON + data.getBet_amount());
                binding.tvWinAmt.setTextColor(ContextCompat.getColor(binding.tvWinAmt.getContext(), R.color.red));
                binding.ivWinType.setImageResource(R.drawable.graph_down_ic);
                binding.ivWinType.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(binding.ivWinType.getContext(), R.color.red)));

                binding.tvResultType.setText("You Loss!");
                binding.tvResultType.setTextColor(ContextCompat.getColor(binding.tvWinAmt.getContext(), R.color.green));
            }
        }
    }
}
