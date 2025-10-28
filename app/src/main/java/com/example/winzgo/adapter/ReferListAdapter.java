package com.example.winzgo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.winzgo.R;
import com.example.winzgo.models.ReferListModel;

import java.util.ArrayList;

public class ReferListAdapter extends RecyclerView.Adapter<ReferListAdapter.ViewHolder> {

    //Data source of list
    ArrayList<ReferListModel> dataSrc;

    public ReferListAdapter(ArrayList<ReferListModel> list) {
        this.dataSrc = list;
    }

    @Override
    public ReferListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.refer_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReferListAdapter.ViewHolder holder, int position) {
        //item data object
        ReferListModel itemDataObj = dataSrc.get(position);
        holder.getName().setText(itemDataObj.getName());
        //bind data to list item name, signup, recharge
        holder.getSignUp().setText(itemDataObj.isSignUp());
        holder.getRecharge().setText(itemDataObj.isRecharge());
    }

    @Override
    public int getItemCount() {
        return dataSrc.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, signUp, recharge;

        public ViewHolder(View view) {//get the item Object hierarchy in its params
            super(view);//super called constructor of Super class.
            name = view.findViewById(R.id.referName);
            signUp = view.findViewById(R.id.referSignUp);
            recharge = view.findViewById(R.id.referRecharge);
        }

        public TextView getName() {
            return name;
        }

        public TextView getSignUp() {
            return signUp;
        }

        public TextView getRecharge() {
            return recharge;
        }
    }
}
