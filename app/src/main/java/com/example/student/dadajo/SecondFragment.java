package com.example.student.dadajo;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.numetriclabz.numandroidcharts.ChartData;
import com.numetriclabz.numandroidcharts.MultiLineChart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class SecondFragment extends Fragment {


    private String title;
    private int page;
    MultiLineChart multiLineChart;
    //List<ChartData> InDust;
    //List<ChartData> OutDust;
    private Handler mHandler;
    private Thread mThread;


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


        //InDust = new ArrayList<>();
        //OutDust = new ArrayList<>();

    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second, container, false);

        multiLineChart = (MultiLineChart) view.findViewById(R.id.chart);
        multiLineChart.setScaleY(1);
        multiLineChart.setScaleX(1);
        getDustValue();

        List<String> h_lables = new ArrayList<>();
        /*h_lables.add("11시간 전");
        h_lables.add("10");
        h_lables.add("9");
        h_lables.add("8");
        h_lables.add("7");
        h_lables.add("6");*/
        h_lables.add("5시간 전");
        h_lables.add("4");
        h_lables.add("3");
        h_lables.add("2");
        h_lables.add("1시간 전");
        h_lables.add("현재");

        multiLineChart.setHorizontal_label(h_lables);

        return view;
    }

    //한 시간마다 미세먼지 데이터 받아오는 메서드
    public void getDustValue() {
        Timer t = new Timer();

        t.scheduleAtFixedRate(
                new TimerTask()
                {
                    public void run()
                    {
                        Queue inDustValue=null;
                        Queue outDustValue=null;
                        try {
                            Response<Queue> inGraph = SensorApi.service.getChartIn().execute(); // 현재 스레드에서 네트워크 작업 요청.
                            Response<Queue> outGraph = SensorApi.service.getChartOut().execute(); //Out 차트 받아오기

                            if (inGraph.code() == 200) {
                                inDustValue = inGraph.body();
                                if (inDustValue == null) {
                                    //System.out.println("window 가져오기 실패");
                                    Log.d("결과", "Inchart 가져오기 실패");
                                } else {
                                    // System.out.println("window 가져오기 성공");
                                    Log.d("결과", "Inchart 가져오기 성공 " + inDustValue);

                                   /* for(Integer i=0; i<12; i++){
                                        Float y=Float.parseFloat(result.poll().toString()+"f");
                                        Float x=Float.parseFloat(i.toString()+"f");
                                        InDust.add(new ChartData(y,x));
                                        Log.d("차트값", "Inchart Y " + y);
                                        //Log.d("차트값", "Inchart X " + x);
                                    }*/
                                }
                            } else {
                                // System.out.println("에러 코드: "+res.code());
                                Log.d("결과", "에러 코드: " + inGraph.code());
                            }




                            if (outGraph.code() == 200) {
                                outDustValue = outGraph.body();



                                if (outDustValue == null) {
                                    //System.out.println("window 가져오기 실패");
                                    Log.d("결과", "Outchart 가져오기 실패");
                                } else {
                                    // System.out.println("window 가져오기 성공");
                                    Log.d("결과", "Outchart 가져오기 성공 " + outDustValue);
                                    /*for(Integer i=0; i<12; i++){
                                        Float y=Float.parseFloat(result2.poll()+"f");
                                        Float x=Float.parseFloat(i.toString()+"f");
                                        OutDust.add(new ChartData(y,x));
                                    }*/
                                }
                            } else {
                                // System.out.println("에러 코드: "+res.code());
                                Log.d("결과", "에러 코드: " + outGraph.code());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        showChart(inDustValue,outDustValue);

                    }
                },
                0,
                3600000);


    }


    //한 시간마다 차트 뿌려주는 메서드
    public void showChart(Queue inDustValue, Queue outDustValue){
        List<ChartData> InDust = new ArrayList<>();
        List<ChartData> OutDust = new ArrayList<>();
        List<ChartData> invisible = new ArrayList<>();
        //List<ChartData> invisible2 = new ArrayList<>();
        //List<ChartData> invisible3 = new ArrayList<>();

        for(Integer i=0; i<6; i++){
            //Float changeY=Float.parseFloat(inDustValue.poll().toString())/10;
            Float y=Float.parseFloat(inDustValue.poll().toString());
            Float x=Float.parseFloat(i.toString());
            InDust.add(new ChartData(y,x));
            Log.d("차트값", "Inchart X " + x);
            Log.d("차트값", "Inchart Y " + y);
            //Log.d("차트값", "Inchart X " + x);
        }


        for(Integer i=0; i<6; i++){
            //Float changeY=Float.parseFloat(outDustValue.poll().toString())/10;
            Float y=Float.parseFloat(outDustValue.poll().toString());
            Float x=Float.parseFloat(i.toString());
            OutDust.add(new ChartData(y,x));
            Log.d("차트값", "Outchart X " + x);
            Log.d("차트값", "Outchart Y " + y);
        }

        invisible.add(new ChartData(150f,0f));
        invisible.add(new ChartData(150f,1f));
        invisible.add(new ChartData(150f,2f));
        invisible.add(new ChartData(150f,3f));
        invisible.add(new ChartData(150f,4f));
        invisible.add(new ChartData(150f,5f));
        /*invisible.add(new ChartData(150f,60f));
        invisible.add(new ChartData(150f,70f));
        invisible.add(new ChartData(150f,80f));
        invisible.add(new ChartData(150f,90f));
        invisible.add(new ChartData(150f,100f));
        invisible.add(new ChartData(150f,110f));*/



        List<ChartData> chart = new ArrayList<>();
        chart.add(new ChartData(InDust));
        chart.add(new ChartData(OutDust));
        chart.add(new ChartData(invisible));
        //chart.add(new ChartData(invisible2));
        //chart.add(new ChartData(invisible3));

        //    mScaleFactor = jononoj  Math.max(.1f, Math.min(mScaleFactor, 10.0f));


        //define the lines
        List<String> legends = new ArrayList<>();
        legends.add("실내");
        legends.add("실외");
        multiLineChart.setLegends(legends);
        //string value of the x axis
        multiLineChart.setCircleSize(8f);
        multiLineChart.setData(chart);
    }
}