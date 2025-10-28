package com.example.winzgo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.winzgo.R;
import com.example.winzgo.models.TransactionsModel;

import java.util.ArrayList;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.ViewHolder> {
    ArrayList<TransactionsModel> dataSrc;
    Context context;
    //Constructor function of Adapter which get data ArrayList of RecyclerView list items in its input params and assign to adapter
    //ArrayList of TransactionsModel class type object
    public TransactionsAdapter(Context context, ArrayList<TransactionsModel> list) {
        this.context = context;
        this.dataSrc = list;
    }
    @NonNull
    @Override
    public TransactionsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inflate an item layout file View hierarchy to java object hierarchy because android system only understand layout in object and class
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transactions_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionsAdapter.ViewHolder holder, int position) {
        TransactionsModel itemDataObj = dataSrc.get(position);
        holder.getDateAndTime().setText(itemDataObj.getDateAndTime());
        if(itemDataObj.getType().equalsIgnoreCase("recharge")) {
            if(itemDataObj.getStatus().equalsIgnoreCase("cancelled"))
                holder.status.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.red, context.getTheme()));
            else if(itemDataObj.getStatus().equalsIgnoreCase("completed"))
                holder.status.setTextColor(ResourcesCompat.getColor(context.getResources(), android.R.color.holo_green_light, context.getTheme()));
            else if(itemDataObj.getStatus().equalsIgnoreCase("pending"))
                holder.status.setTextColor(ResourcesCompat.getColor(context.getResources(), android.R.color.white, context.getTheme()));
            else
                holder.status.setTextColor(ResourcesCompat.getColor(context.getResources(), android.R.color.holo_blue_light, context.getTheme()));

        } else {
            if(itemDataObj.getStatus().equalsIgnoreCase("cancelled"))
                holder.status.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.red, context.getTheme()));
            else if(itemDataObj.getStatus().equalsIgnoreCase("completed"))
                holder.status.setTextColor(ResourcesCompat.getColor(context.getResources(), android.R.color.holo_green_light, context.getTheme()));
            else if(itemDataObj.getStatus().equalsIgnoreCase("pending"))
                holder.status.setTextColor(ResourcesCompat.getColor(context.getResources(), android.R.color.white, context.getTheme()));
            else
                holder.status.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.red, context.getTheme()));
        }

        holder.getStatus().setText(itemDataObj.getStatus());
        holder.getType().setText(itemDataObj.getType());
        holder.getAmount().setText(itemDataObj.getAmount() + "");

        if(position == dataSrc.size() - 1) {
            holder.getView().setVisibility(View.GONE);
        } else {
            holder.getView().setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return dataSrc.size();
    }

    //ViewHolder class which wrapped the item object hierarchy for more secure list object and declare
    //all the event listener here
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView status, type, amount, dateAndTime;
        View viewItem;
        public ViewHolder(View view) {
            super(view);
            dateAndTime = view.findViewById(R.id.transactionTime);
            status = view.findViewById(R.id.statusTransactions);
            type = view.findViewById(R.id.typeTransactions);
            amount = view.findViewById(R.id.amountTransactions);
            viewItem = view.findViewById(R.id.view);
        }

        public void setStatus(TextView status) {
            this.status = status;
        }

        public void setType(TextView type) {
            this.type = type;
        }

        public void setAmount(TextView amount) {
            this.amount = amount;
        }

        public void setDateAndTime(TextView dateAndTime) {
            this.dateAndTime = dateAndTime;
        }

        public View getView() {
            return viewItem;
        }

        public void setView(View view) {
            this.viewItem = view;
        }

        public TextView getDateAndTime() {
            return dateAndTime;
        }

        public TextView getStatus() {
            return status;
        }

        public TextView getType() {
            return type;
        }

        public TextView getAmount() {
            return amount;
        }
    }
}
