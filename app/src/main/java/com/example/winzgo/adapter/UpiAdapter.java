package com.example.winzgo.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.winzgo.R;
import com.example.winzgo.databinding.UpiAdapterItemBinding;
import com.example.winzgo.models.UpiAdapterModel;

import java.util.List;

public class UpiAdapter extends RecyclerView.Adapter<UpiAdapter.ViewHolder> {
    private List<UpiAdapterModel> list;

    private Context context;

    public UpiAdapter(Context context, List<UpiAdapterModel> list) {
        this.list = list;
        this.context = context;
    }

    @Override
    public void onBindViewHolder(UpiAdapter.ViewHolder holder, int pos) {
        UpiAdapterModel data = list.get(pos);
        holder.bind(data);
    }

    @Override
    public UpiAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int whichType) {
        UpiAdapterItemBinding binding = UpiAdapterItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private UpiAdapterItemBinding binding;
        public ViewHolder(UpiAdapterItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(UpiAdapterModel data) {
            binding.tvUpi.setText(data.getUpiId());

            if(data.isCopied()) {
                binding.copyIc.setImageResource(R.drawable.checked);
            } else
                binding.copyIc.setImageResource(R.drawable.copy_tgs_ic);

            binding.copyIc.setOnClickListener(v -> {
                binding.copyIc.setImageResource(R.drawable.checked);
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied Upi", binding.tvUpi.getText().toString());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context, data.getUpiId() + " is Copied", Toast.LENGTH_SHORT).show();
                for(UpiAdapterModel model: list) {
                    model.setCopied(false);
                }

                data.setCopied(true);
                UpiAdapter.this.notifyDataSetChanged();
            });
        }
    }


}
