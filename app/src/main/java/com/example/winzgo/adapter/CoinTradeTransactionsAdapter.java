package com.example.winzgo.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.winzgo.R;
import com.example.winzgo.databinding.CoinTradeTransactionsListItemBinding;
import com.example.winzgo.models.TransactionsModel;
import com.example.winzgo.utils.Constants;

import java.util.List;

public class CoinTradeTransactionsAdapter extends RecyclerView.Adapter<CoinTradeTransactionsAdapter.ViewHolder> {
    private Context context;
    private List<TransactionsModel> list;

    public CoinTradeTransactionsAdapter(Context context, List<TransactionsModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public CoinTradeTransactionsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CoinTradeTransactionsListItemBinding binding = CoinTradeTransactionsListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CoinTradeTransactionsAdapter.ViewHolder holder, int position) {
        TransactionsModel itemDataObj = list.get(position);
        holder.bind(itemDataObj);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CoinTradeTransactionsListItemBinding binding;

        public ViewHolder(CoinTradeTransactionsListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(TransactionsModel itemDataObj) {
            binding.tvStatus.setText(itemDataObj.getStatus());
            binding.tvDateAndTime.setText(itemDataObj.getDate());
            if (itemDataObj.getType().equalsIgnoreCase("recharge")) {
                binding.tvType.setText("Deposit");
                binding.tvAmt.setText("+" + Constants.checkAndReturnInSetCurrency(binding.tvAmt.getContext(), itemDataObj.getAmount()));

                if (itemDataObj.getStatus().equalsIgnoreCase("cancelled"))
                    binding.tvStatus.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.red, context.getTheme()));
                else if (itemDataObj.getStatus().equalsIgnoreCase("completed"))
                    binding.tvStatus.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.green, context.getTheme()));
                else if (itemDataObj.getStatus().equalsIgnoreCase("pending"))
                    binding.tvStatus.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.black, context.getTheme()));
                else {
                    binding.tvStatus.setText("Cancelled");
                    binding.tvStatus.setTextColor(ResourcesCompat.getColor(context.getResources(), android.R.color.holo_blue_light, context.getTheme()));
                }

                binding.getRoot().setBackgroundResource(R.drawable.dark_theme_silver_green_bg);
                binding.tvAmt.setTextColor(context.getResources().getColor(R.color.green));
                binding.ivCircle.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.pastel_green)));
                binding.ivCircle.setRotation(125f);
            } else {
                binding.tvType.setText("Withdrawal");
                binding.tvAmt.setText("-" + Constants.checkAndReturnInSetCurrency(binding.tvAmt.getContext(), itemDataObj.getAmount()));

                if (itemDataObj.getStatus().equalsIgnoreCase("cancelled"))
                    binding.tvStatus.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.red, context.getTheme()));
                else if (itemDataObj.getStatus().equalsIgnoreCase("completed"))
                    binding.tvStatus.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.green, context.getTheme()));
                else if (itemDataObj.getStatus().equalsIgnoreCase("pending"))
                    binding.tvStatus.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.black, context.getTheme()));
                else {
                    binding.tvStatus.setText("Cancelled");
                    binding.tvStatus.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.red, context.getTheme()));
                }

                binding.getRoot().setBackgroundResource(R.drawable.dark_theme_silver_red_bg);
                binding.tvAmt.setTextColor(context.getResources().getColor(R.color.dark_red));
                binding.ivCircle.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.pastel_red)));
                binding.ivCircle.setRotation(-45f);
            }
        }
    }
}
