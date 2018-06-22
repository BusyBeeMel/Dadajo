package com.example.student.dadajo;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.student.dadajo.R;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Response;

import static com.example.student.dadajo.FirstFragment.dustCloseBright;
import static com.example.student.dadajo.FirstFragment.dustCloseDark;
import static com.example.student.dadajo.FirstFragment.dustOpenBright;
import static com.example.student.dadajo.FirstFragment.dustOpenDark;
import static com.example.student.dadajo.FirstFragment.switchWindow;
import static com.example.student.dadajo.FirstFragment.window_state;


public class MainActivity extends AppCompatActivity implements MqttCallback {
    RelativeLayout main;
    MqttClient client;
    int rainSettingState;
    int dustSettingState;
    Button settingBtn;
    FragmentPagerAdapter adapterViewPager;

    long mNow;
    Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("kk");
    String mTime;
    int iTime;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, LoadingActivity.class);
        startActivity(intent);


        try {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.custom_bar);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }



        mTime=getTime();
        iTime=Integer.parseInt(mTime);
        Log.d("앱 실행 시간",mTime);



        settingBtn=(Button)findViewById(R.id.settingBtn);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SettingActivity.class);
                startActivity(intent);
            }
        });



        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        main=(RelativeLayout)findViewById(R.id.main);


        if(iTime>=06&&iTime<=18){//낮
            main.setBackgroundColor(Color.parseColor("#DCE0DE"));
        }else{//밤
            main.setBackgroundColor(Color.parseColor("#6D7170"));
        }

        restoreState();


        try{
            connectMqtt(); // Mqtt broker 접속
            Log.d("connection", "접속성공");
        }catch (MqttException e){
            Log.d("notconnection", "접속 실패");
            e.printStackTrace();
        }


        ViewPager vpPager = (ViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);

        new Thread() {
            public void run() {
                try {
                    Response<Boolean> res = SensorApi.service.putDust(dustSettingState).execute(); // 현재 스레드에서 네트워크 작업 요청.
                    if (res.code() == 200) {
                        Boolean result = res.body();
                        if (result) {
                            //System.out.println("window 가져오기 실패");
                            Log.d("결과", "초기 dustSetting 보내기 성공" + dustSettingState);

                        } else {
                            // System.out.println("window 가져오기 성공");
                            Log.d("결과", "초기 dustSetting 보내기 실패 " + result);
                        }
                    } else {
                        // System.out.println("에러 코드: "+res.code());
                        Log.d("결과", "에러 코드: " + res.code());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("결과", "예외 발생: " + e.getMessage());
                }


                try {
                    Response<Boolean> res = SensorApi.service.putRain(rainSettingState).execute(); // 현재 스레드에서 네트워크 작업 요청.
                    if (res.code() == 200) {
                        Boolean result = res.body();
                        if (result) {
                            //System.out.println("window 가져오기 실패");
                            Log.d("결과", "초기 rainSetting 보내기 성공" + rainSettingState);
                        } else {
                            // System.out.println("window 가져오기 성공");
                            Log.d("결과", "초기 rainSetting 보내기 실패 " + result);
                        }
                    } else {
                        // System.out.println("에러 코드: "+res.code());
                        Log.d("결과", "에러 코드: " + res.code());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("결과", "예외 발생: " + e.getMessage());
                }
            }
        }.start();


        setWindow();


        }




    protected void restoreState() {


        //   SharedPreferences pref = getSharedPreferences("preferences", Activity.MODE_PRIVATE);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(pref.getBoolean("switch_preference_2", false)){
            //dust sensor is on. (TRUE)
            dustSettingState = 1;
        }else{
            dustSettingState = 0;
        }


        if(pref.getBoolean("switch_preference_3", false)){
            //dust sensor is on. (TRUE)
            rainSettingState = 1;
        }else{
            rainSettingState = 0;
        }
    }




    public void connectMqtt() throws MqttException{
        client = new MqttClient("tcp://70.12.112.61:1883",
                MqttClient.generateClientId(),
                new MemoryPersistence());
        client.setCallback(this);
        client.connect();
        client.subscribe("home/out/#"); // 구독할 토픽 설정
        client.subscribe("home/in/#");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            client.disconnect();
        }catch (MqttException e){
            e.printStackTrace();
        }
    }

    public void connectionLost(Throwable throwable){
        Log.d("connection", "Connection to MQTT broker lost!");
    }

    public void messageArrived(String topic, MqttMessage mqttMessage){
        Log.d("토픽 수신", topic);


        Log.d("topic_message", new String(mqttMessage.getPayload()));

        final String location = topic.substring(topic.indexOf("/")+1, topic.lastIndexOf("/")); // 센서의 위치(in 또는 out)
        final String sensor = topic.substring(topic.lastIndexOf("/")+1); // 센서명(temp/humid/dust/rain)
        final String value = new String(mqttMessage.getPayload());  // 센서값
        Log.d("lsv", location+")"+sensor+": "+value);

        runOnUiThread(new Runnable() { //UI 작업은 UIThread에서
            @Override
            public void run() {
                if(location.equals("in")) {
                    updateDataIn(sensor, value);
                }else if(location.equals("out")){
                    updateDataOut(sensor, value);
                }
            }
        });

    }

    // 집 안 센서값 표시
    public void updateDataIn(String sensor, String value){
        //int airState=0;

        switch(sensor){
            case "temp":
                FirstFragment.temp_in = Float.parseFloat(value);
                FirstFragment.tempInView.setText(value + "'C");
                break;
            case "humid":
                FirstFragment.humid_in = Float.parseFloat(value);
                FirstFragment.humidInView.setText(value);
                break;
            case "dust":
                FirstFragment.dust_in = Float.parseFloat(value);
                FirstFragment.dustInView.setText(value+"pm");

                float value_int= Float.parseFloat(value);

                if(value_int<=30.0){
                    FirstFragment.dustInSentence.setText("좋음");
                    FirstFragment.dustInSentence.setBackgroundColor(Color.parseColor("#A8DEF2"));
                }else if(value_int>30.0&&value_int<=80.0){
                    FirstFragment.dustInSentence.setText("보통");
                    FirstFragment.dustInSentence.setBackgroundColor(Color.parseColor("#6BEC62"));
                    //airState=1;
                }else if(value_int>80.0&&value_int<=150.0){
                    FirstFragment.dustInSentence.setText("나쁨");
                    FirstFragment.dustInSentence.setBackgroundColor(Color.parseColor("#FF9436"));
                   // airState=2;
                }else{
                    FirstFragment.dustInSentence.setText("매우 나쁨");
                    FirstFragment.dustInSentence.setBackgroundColor(Color.parseColor("#FF3636"));
                    //airState=3;
                }
                //firstWindowSet(airState);

                break;
            default:
                break;
        }



    }

    // 바깥 센서값 표시
    public void updateDataOut(String sensor, String value){
        switch(sensor){
            case "temp":
                FirstFragment.temp_out = Float.parseFloat(value);
                FirstFragment.tempOutView.setText(value + "'C");
                break;
            case "humid":
                FirstFragment.humid_out = Float.parseFloat(value);
                FirstFragment.humidOutView.setText(value);
                break;
            case "dust":
                FirstFragment.dust_out = Float.parseFloat(value);
                FirstFragment.dustOutView.setText(value+"pm");
                float value_int= Float.parseFloat(value);
                if(value_int<=30.0){
                    FirstFragment.dustOutSentence.setText("좋음");
                    FirstFragment.dustOutSentence.setBackgroundColor(Color.parseColor("#A8DEF2"));
                }else if(value_int>30.0&&value_int<=80.0){
                    FirstFragment.dustOutSentence.setText("보통");
                    FirstFragment.dustOutSentence.setBackgroundColor(Color.parseColor("#6BEC62"));
                }else if(value_int>80.0&&value_int<=150.0){
                    FirstFragment.dustOutSentence.setText("나쁨");
                    FirstFragment.dustOutSentence.setBackgroundColor(Color.parseColor("#FF9436"));
                }else{
                    FirstFragment.dustOutSentence.setText("매우 나쁨");
                    FirstFragment.dustOutSentence.setBackgroundColor(Color.parseColor("#FF3636"));
                }
                break;
            case "rain":
                int rainValue=Integer.parseInt(value);
                if(rainValue==0){

                }else{

                }
            default:
                break;
        }
    }

    public void setWindow(){
        //맨 처음 실행했을 때 창문 상태 가져오기
        //실외의 절대 수치가 높으면 먼지 창문
        // ->실외보다도 실내가 높으면 열린 먼지 창문
        // ->실외보다 실내가 낮으면 닫힌 먼지 창문

        Timer t = new Timer();

        t.scheduleAtFixedRate(
                new TimerTask()
                {
            public void run() {
                try {
                    Response<Integer> res = SensorApi.service.getWindow().execute(); // 현재 스레드에서 네트워크 작업 요청.
                    if (res.code() == 200) {
                        int result = res.body();
                        if (result == -1) {
                            //System.out.println("window 가져오기 실패");
                            Log.d("결과", "window 가져오기 실패");
                        } else {
                            // System.out.println("window 가져오기 성공");
                            Log.d("결과", "window 가져오기 성공 " + result);
                            window_state = result;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if ((window_state == 0) && iTime >= 06 && iTime <= 18) {//창문 닫혀있고 낮
                                        Log.d("결과", "window_state " + window_state);
                                        switchWindow.setChecked(false);
                                        Glide.with(getApplicationContext())
                                                .load(dustCloseBright)
                                                .into(FirstFragment.imageView);
                                    } else if ((window_state == 0) && iTime <= 06 && iTime >= 18) {//창문 닫혀있고 밤
                                        Log.d("결과", "window_state " + window_state);
                                        switchWindow.setChecked(false);
                                        Glide.with(getApplicationContext())
                                                .load(dustCloseDark)
                                                .into(FirstFragment.imageView);
                                    } else if ((window_state == 1) && iTime >= 06 && iTime <= 18) {//창문 열려있고 낮
                                        switchWindow.setChecked(true);
                                        Glide.with(getApplicationContext())
                                                .load(dustOpenBright)
                                                .into(FirstFragment.imageView);
                                    } else if ((window_state == 1) && iTime <= 06 && iTime >= 18) {//창문 열려있고 밤
                                        switchWindow.setChecked(true);
                                        Glide.with(getApplicationContext())
                                                .load(dustOpenDark)
                                                .into(FirstFragment.imageView);
                                    }
                                }
                            });


                        }
                    } else {
                        // System.out.println("에러 코드: "+res.code());
                        Log.d("결과", "에러 코드: " + res.code());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

                },
                0,
                5000);
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken){

    }

    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 2;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return FirstFragment.newInstance(0, "Page # 1");
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return SecondFragment.newInstance(1, "Page # 2");
                default:
                    return null;
            }
        }
        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }



    }

    private String getTime(){
        mNow = System.currentTimeMillis();
        mDate = new Date(mNow);
        return mFormat.format(mDate);
    }
}