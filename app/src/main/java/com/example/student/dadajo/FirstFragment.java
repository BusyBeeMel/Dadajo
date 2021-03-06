package com.example.student.dadajo;


import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Retrofit;
import retrofit2.Response;

import static com.example.student.dadajo.MainActivity.iTime;
import static com.example.student.dadajo.SettingActivity.dustSettingState;
import static com.example.student.dadajo.SettingActivity.rainSettingState;


/**
 * A simple {@link Fragment} subclass.
 */
public class FirstFragment extends Fragment {
    private String title;
    private int page;
    static ImageView imageView;
    static Switch switchWindow;
    Retrofit retrofit;
    SensorApi service;


    static TextView WindowView;
    static TextView tempInView;
    static TextView humidInView;
    static TextView dustInView;
    static TextView dustInSentence;
    static TextView tempOutView;
    static TextView humidOutView;
    static TextView dustOutView;
    static TextView dustOutSentence;
    static TextView text_open;


    static  float temp_in;          // 집 안 온도
    static float humid_in;         // 집 안 습도
    static float temp_out;         // 바깥 온도
    static float humid_out;        // 바깥 습도
    static float dust_in;
    static float dust_out;



    public static int window_state = 0;   // 현재 창문 상태(1 이면 열림, 0 이면 닫힘)]
    final static int state_close_bright=R.drawable.window_close_bright;
    final static int state_open_bright=R.drawable.window_open_bright;
    final static int state_close_dark=R.drawable.window_close_dark;
    final static int state_open_dark=R.drawable.window_open_dark;


    final static int dustCloseDark=R.drawable.dust_close_dark;
    final static int dustCloseBright=R.drawable.dust_close_bright;
    final static int dustOpenDark=R.drawable.dust_open_dark;
    final static int dustOpenBright=R.drawable.dust_open_bright;

    final static int rainCloseDark=R.drawable.rain_close_dark;
    final static int rainCloseBright=R.drawable.rain_close_bright;
    final static int rainOpenDark=R.drawable.rain_open_dark;
    final static int rainOpenBright=R.drawable.rain_open_bright;

    boolean onSwitch=false;
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


        //창문 초기 상태 가져오기
        new Thread() {
            public void run() {
                int windowState = 0;
                try {
                    Response<Integer> window = SensorApi.service.getWindow().execute(); // 현재 스레드에서 네트워크 작업 요청.

                    if (window.code() == 200) {
                        windowState = window.body();
                        if (windowState == 0) {
                            //System.out.println("window 가져오기 실패");
                            // Log.d("결과", "Inchart 가져오기 실패");
                            window_state = 0;
                        } else {
                            // System.out.println("window 가져오기 성공");
                            // Log.d("결과", "Inchart 가져오기 성공 " + inDustValue);
                            window_state = 1;
                        }
                    } else {
                        // System.out.println("에러 코드: "+res.code());
                        Log.d("결과", "에러 코드: " + window.code());
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
        dustInSentence=(TextView)view.findViewById(R.id.dustInSentence);
        tempOutView = (TextView)view.findViewById(R.id.tempOutView);
        humidOutView = (TextView)view.findViewById(R.id.humidOutView);
        dustOutView=(TextView)view.findViewById(R.id.dustOutView);
        dustOutSentence=(TextView)view.findViewById(R.id.dustOutSentence);
        text_open=(TextView)view.findViewById(R.id.text_open);

        //창문 상태에 따라 스위치 버튼 초기 상태 지정
        if(window_state==1){
            switchWindow.setChecked(true);
            MainActivity.setWindow("window","true");
        }else{
            switchWindow.setChecked(false);
            MainActivity.setWindow("window","false");
        }

        if(dustSettingState==1 || rainSettingState==1){
            switchWindow.setEnabled(false);
        }else{
            switchWindow.setEnabled(true);
        }

        if (iTime <= 06 || iTime >= 12) {//밤
            text_open.setTextColor(Color.parseColor("#ededed"));
        }



        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Boolean rain = prefs.getBoolean("switch_preference_2",false);
        Boolean dust = prefs.getBoolean("switch_preference_3",false);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("안내");
        builder.setMessage("자동 개폐 기능을 종료할까요?");
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                String message = "자동개폐 기능을 종료합니다.";
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

                prefs.edit().putBoolean("switch_preference_2",false).apply();
                prefs.edit().putBoolean("switch_preference_3",false).apply();


                //수동 기능 예 눌렀을 때 자동 끈 상태 서버로 보내기
                new Thread() {
                    public void run() {
                        try {
                            Response<Boolean> res = SensorApi.service.putDust(0).execute(); // 현재 스레드에서 네트워크 작업 요청.
                            Response<Boolean> res1 = SensorApi.service.putRain(0).execute();

                            if(res.code()==200) {
                                Boolean result = res.body();
                                if(result) {
                                    //System.out.println("window 가져오기 실패");
                                    Log.d("결과","수동-dustSetting 보내기 성공");
                                }else {
                                    // System.out.println("window 가져오기 성공");
                                    Log.d("결과","수동-dustSetting 보내기 실패 " + result);
                                }
                            }else {
                                // System.out.println("에러 코드: "+res.code());
                                Log.d("결과","에러 코드: "+res.code());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d("결과","예외 발생: "+e.getMessage());
                        }
                    }
                }.start();



            }
        }).setNegativeButton("아니오",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
               /* String message = "취소했습니다.";
                Switch temp = switchWindow;
                switchWindow.setOnCheckedChangeListener(null);
                boolean currentSwitch=switchWindow.isChecked();
                switchWindow.setChecked(!currentSwitch);
                switchWindow = temp;*/


            }
        });

        /*switchWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MainActivity.dustSettingState==1 || MainActivity.rainSettingState==1) {//둘 중 하나라도 켜졌으면
                    Log.d("먼지 자동",MainActivity.dustSettingState+"");
                    Log.d("비 자동",MainActivity.rainSettingState+"");
                    builder.show();
                }



                //예 눌러서 자동개폐 기능 꺼졌을 때 창문 이미지 바꾸기
*//*
                if (isChecked) {

                    //window_state = 1;
                    //MainActivity.setWindow("window", "on");

                } else {
                    //window_state = 0;
                    //MainActivity.setWindow("window", "off");

                }*//*
            }
        });*/


        //수동으로 스위치 버튼 상태 바뀔 때
        switchWindow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                /*if(MainActivity.dustSettingState==1 || MainActivity.rainSettingState==1) {//둘 중 하나라도 켜졌으면
                    Log.d("먼지 자동",MainActivity.dustSettingState+"");
                    Log.d("비 자동",MainActivity.rainSettingState+"");
                    builder.show();
                }*/

                //예 눌러서 자동개폐 기능 꺼졌을 때 창문 이미지 바꾸기

                    if (isChecked) {

                        window_state = 1;
                        MainActivity.setWindow("window", "true");

                    } else {
                        window_state = 0;
                        MainActivity.setWindow("window", "false");

                    }



                new Thread() {
                    public void run() {
                        try {
                            Response<Boolean> res = SensorApi.service.putWindow(window_state).execute(); // 현재 스레드에서 네트워크 작업 요청.
                            if(res.code()==200) {
                                boolean result = res.body();
                                if(result == true) {
                                    //System.out.println("window 가져오기 실패");
                                    Log.d("결과","window 보내기 성공");
                                }else {
                                    // System.out.println("window 가져오기 성공");
                                    Log.d("결과","window 보내기 실패 " + result);


                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(window_state == 0){
                                                Log.d("결과","window_state " + window_state);
                                                Glide.with(getContext())
                                                        .load(dustCloseDark)
                                                        .into(imageView);
                                            }else{
                                                Log.d("결과","window_state " + window_state);
                                                Glide.with(getContext())
                                                        .load(dustOpenDark)
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
        });


        return view;
    }



}