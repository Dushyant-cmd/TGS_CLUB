package com.example.winzgo.fragments.coin;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.winzgo.MainActivity;
import com.example.winzgo.R;
import com.example.winzgo.databinding.FragmentCoinPredictionBinding;
import com.example.winzgo.fragments.CoinAndTradeWalletFragment;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class CoinPredictionFragment extends Fragment {
    private FragmentCoinPredictionBinding binding;
    private MainActivity hostAct;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_coin_prediction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding = FragmentCoinPredictionBinding.bind(view);
        hostAct = (MainActivity) requireActivity();

        hostAct.setupHeader("Coin Prediction");
        List<Double> list = new ArrayList<>();
        list.add(100.00);
        list.add(102.00);
        list.add(103.00);
        list.add(104.00);
        setDataForWeeksWise(binding.chartBitcoin, list);
        setListeners();
    }

    private void setDataForWeeksWise(LineChart chart, List<Double> amounts) {
        ArrayList<Entry> values = new ArrayList<>();
        values.add(new Entry(1, amounts.get(0).floatValue()));
        values.add(new Entry(2, amounts.get(1).floatValue()));
        values.add(new Entry(3, amounts.get(2).floatValue()));
        values.add(new Entry(4, amounts.get(3).floatValue()));


        LineDataSet set1;
        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            set1 = new LineDataSet(values, "Total volume");
            set1.setDrawCircles(true);
            set1.enableDashedLine(10f, 0f, 0f);
            set1.enableDashedHighlightLine(10f, 0f, 0f);
            set1.setColor(getResources().getColor(R.color.green));
            set1.setCircleColor(getResources().getColor(R.color.red));
            set1.setLineWidth(2f);//line size
            set1.setCircleRadius(5f);
            set1.setDrawCircleHole(true);
            set1.setValueTextSize(10f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(5f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(5.f);

            if (Utils.getSDKInt() >= 18) {
//                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.blue_bg);
//                set1.setFillDrawable(drawable);
                set1.setFillColor(Color.WHITE);

            } else {
                set1.setFillColor(Color.WHITE);
            }
            set1.setDrawValues(true);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            LineData data = new LineData(dataSets);

            chart.setData(data);
        }
    }

    private void setListeners() {
    }
}