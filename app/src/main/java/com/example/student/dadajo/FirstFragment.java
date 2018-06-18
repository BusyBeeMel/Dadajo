package com.example.student.dadajo;


import android.app.Activity;
import android.hardware.Sensor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.Observable;

import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;


/**
 * A simple {@link Fragment} subclass.
 */
public class FirstFragment extends Fragment {
    private String title;
    private int page;
    static ImageView imageView;
    Switch switchWindow;
    Retrofit retrofit;
    SensorApi service;

    static TextView WindowView;
    static TextView tempInView;
    static TextView humidInView;
    static TextView dustInView;
    static TextView tempOutView;
    static TextView humidOutView;
    static TextView dustOutView;


    static  float temp_in;          // 집 안 온도
    static float humid_in;         // 집 안 습도
    static float temp_out;         // 바깥 온도
    static float humid_out;        // 바깥 습도
    static float dust_in;
    static float dust_out;

    public int window_state = 0;   // 현재 창문 상태(1 이면 열림, 0 이면 닫힘)]
    final int stateClose=R.drawable.window_close;
    final int stateOpen=R.drawable.window_open;


    // newInstance constructor for creating fragment with arguments
    public static FirstFragment newInstance(int page, String title) {
        FirstFragment fragmentFirst = new FirstFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");

        new Thread() {
            public void run() {
                try {
                    Response<Integer> res = SensorApi.service.getWindow().execute(); // 현재 스레드에서 네트워크 작업 요청.
                    if(res.code()==200) {
                        int result = res.body();
                        if(result == -1) {
                            //System.out.println("window 가져오기 실패");
                            Log.d("결과","window 가져오기 실패");
                        }else {
                            // System.out.println("window 가져오기 성공");
                            Log.d("결과","window 가져오기 성공 " + result);
                            window_state = result;

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(window_state == 0){
                                        Log.d("결과","window_state " + window_state);
                                        Glide.with(getContext())
                                                .load(stateClose)
                                                .into(imageView);
                                    }else{
                                        Log.d("결과","window_state " + window_state);
                                        Glide.with(getContext())
                                                .load(stateOpen)
                                                .into(imageView);
                                    }
                                }
                            });


                        }
                    }else {
                        // System.out.println("에러 코드: "+res.code());
                        Log.d("결과","에러 코드: "+res.code());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }



    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_first, container, false);

        imageView=(ImageView)view.findViewById(R.id.img_window);
        switchWindow=(Switch)view.findViewById(R.id.switch_window);


        tempInView = (TextView)view.findViewById(R.id.tempInView);
        humidInView = (TextView)view.findViewById(R.id.humidInView);
        dustInView=(TextView)view.findViewById(R.id.dustInView);
        tempOutView = (TextView)view.findViewById(R.id.tempOutView);
        humidOutView = (TextView)view.findViewById(R.id.humidOutView);
        dustOutView=(TextView)view.findViewById(R.id.dustOutView);







        switchWindow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Glide.with(getContext())
                            .load(stateOpen)
                            .into(imageView);

                } else {
                    Glide.with(getContext())
                            .load(stateClose)
                            .into(imageView);
                }
            }
        });

        return view;
    }

    public void setWindow(){
        if(window_state == 0){
            Log.d("결과","window_state " + window_state);
            Glide.with(getContext())
                    .load(stateClose)
                    .into(imageView);
        }else{
            Log.d("결과","window_state " + window_state);
            Glide.with(getContext())
                    .load(stateOpen)
                    .into(imageView);
        }
    }

}
