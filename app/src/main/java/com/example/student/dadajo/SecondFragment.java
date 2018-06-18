package com.example.student.dadajo;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.numetriclabz.numandroidcharts.ChartData;
import com.numetriclabz.numandroidcharts.MultiLineChart;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SecondFragment extends Fragment {


    private String title;
    private int page;

    // newInstance constructor for creating fragment with arguments
    public static SecondFragment newInstance(int page, String title) {
        SecondFragment fragmentSecond = new SecondFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentSecond.setArguments(args);
        return fragmentSecond;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 1);
        title = getArguments().getString("someTitle");
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second, container, false);

        MultiLineChart multiLineChart = (MultiLineChart) view.findViewById(R.id.chart);
        List<String> h_lables = new ArrayList<>();
        h_lables.add("1");
        h_lables.add("2");
        h_lables.add("3");
        h_lables.add("4");
        h_lables.add("5");
        h_lables.add("6");
        h_lables.add("7");
        h_lables.add("8");
        h_lables.add("9");
        h_lables.add("10");
        h_lables.add("11");
        h_lables.add("12");

        multiLineChart.setHorizontal_label(h_lables);

        List<ChartData> InDust = new ArrayList<>();


        InDust.add(new ChartData(4f, 1f)); //values.add(new ChartData(y,x));<br />
        InDust.add(new ChartData(5f, 2f));
        InDust.add(new ChartData(6f, 3f));
        InDust.add(new ChartData(7f, 4f));
        InDust.add(new ChartData(9f, 5f));
        InDust.add(new ChartData(8f, 6f));
        InDust.add(new ChartData(6f, 7f));
        InDust.add(new ChartData(7f, 8f));
        InDust.add(new ChartData(10f, 9f));
        InDust.add(new ChartData(11f,10f));
        InDust.add(new ChartData(13f,11f));
        InDust.add(new ChartData(18f,12f));

        List<ChartData> OutDust = new ArrayList<>();
        OutDust.add(new ChartData(4f, 1f));
        OutDust.add(new ChartData(5f, 2f));
        OutDust.add(new ChartData(6f, 3f));
        OutDust.add(new ChartData(7f, 4f));
        OutDust.add(new ChartData(7f, 5f));
        OutDust.add(new ChartData(9f, 6f));
        OutDust.add(new ChartData(5f, 7f));
        OutDust.add(new ChartData(3f, 8f));
        OutDust.add(new ChartData(5f, 9f));
        OutDust.add(new ChartData(15f,10f));
        OutDust.add(new ChartData(15f,11f));
        OutDust.add(new ChartData(8f,12f));

        List<ChartData> chart = new ArrayList<>();
        chart.add(new ChartData(InDust));
        chart.add(new ChartData(OutDust));

        multiLineChart.setLeft(1);
        multiLineChart.setRight(50);
        //    mScaleFactor = Math.max(.1f, Math.min(mScaleFactor, 10.0f));


        //define the lines
        List<String> legends = new ArrayList<>();
        legends.add("In");
        legends.add("Out");
        multiLineChart.setLegends(legends);
        //string value of the x axis
        multiLineChart.setCircleSize(8f);
        multiLineChart.setData(chart);
        return view;
    }

}
