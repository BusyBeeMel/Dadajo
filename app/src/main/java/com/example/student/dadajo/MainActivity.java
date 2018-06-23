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
import static com.example.student.dadajo.FirstFragment.rainCloseBright;
import static com.example.student.dadajo.FirstFragment.rainCloseDark;
import static com.example.student.dadajo.FirstFragment.rainOpenBright;
import static com.example.student.dadajo.FirstFragment.rainOpenDark;
import static com.example.student.dadajo.FirstFragment.state_close_bright;
import static com.example.student.dadajo.FirstFragment.state_close_dark;
import static com.example.student.dadajo.FirstFragment.state_open_bright;
import static com.example.student.dadajo.FirstFragment.state_open_dark;
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

    String industValue="좋음";
    String outdustValue="좋음";



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


        restoreState();

        if (iTime >= 06 && iTime <= 15) {//낮
            main.setBackgroundColor(Color.parseColor("#DCE0DE"));
        }else{
            main.setBackgroundColor(Color.parseColor("#6D7170"));
        }


        try{
            connectMqtt(); // Mqtt broker 접속
            Log.d("connection", "접속성공");
        }catch (MqttException e){
            Log.d("notconnection", "접속 실패");
            e.printStackTrace();
            Log.d("결과", "mqtt 예외 발생: " + e.getMessage());
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
        client = new MqttClient("tcp://192.168.0.4:1883",
                MqttClient.generateClientId(),
                new MemoryPersistence());
        client.setCallback(this);
        client.connect();
        client.subscribe("home/out/#"); // 구독할 토픽 설정
        client.subscribe("home/in/#");
        client.subscribe("home/control/window");
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
                    //setWindow(sensor,value);
                }else if(location.equals("out")){
                    updateDataOut(sensor, value);
                    //setWindow(sensor,value);
                }else if(sensor.equals("window")){
                    setWindow(sensor,value);
                }
            }
        });

    }

    // 집 안 센서값 표시
    public void updateDataIn(String sensor, String value){

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
                    setWindow("inDust","좋음");
                }else if(value_int>30.0&&value_int<=80.0){
                    FirstFragment.dustInSentence.setText("보통");
                    FirstFragment.dustInSentence.setBackgroundColor(Color.parseColor("#6BEC62"));
                    setWindow("inDust","보통");
                }else if(value_int>80.0&&value_int<=150.0){
                    FirstFragment.dustInSentence.setText("나쁨");
                    FirstFragment.dustInSentence.setBackgroundColor(Color.parseColor("#FF9436"));
                    setWindow("inDust","나쁨");
                }else{
                    FirstFragment.dustInSentence.setText("매우 나쁨");
                    FirstFragment.dustInSentence.setBackgroundColor(Color.parseColor("#FF3636"));
                    setWindow("inDust","매우나쁨");
                }


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
                    setWindow("outDust","좋음");
                }else if(value_int>30.0&&value_int<=80.0){
                    FirstFragment.dustOutSentence.setText("보통");
                    FirstFragment.dustOutSentence.setBackgroundColor(Color.parseColor("#6BEC62"));
                    setWindow("outDust","보통");
                }else if(value_int>80.0&&value_int<=150.0){
                    FirstFragment.dustOutSentence.setText("나쁨");
                    FirstFragment.dustOutSentence.setBackgroundColor(Color.parseColor("#FF9436"));
                    setWindow("outDust","나쁨");
                }else{
                    FirstFragment.dustOutSentence.setText("매우 나쁨");
                    FirstFragment.dustOutSentence.setBackgroundColor(Color.parseColor("#FF3636"));
                    setWindow("outDust","매우나쁨");
                }
                break;

            case "rain":
                //int rainValue=Integer.parseInt(value);
                setWindow("rain",value);
            default:
                break;
        }
    }

    public void setWindow(String sensor, String value) {
        Float rainValue = 0.00f;
        if (sensor.equals("rain")) {
            rainValue = Float.parseFloat(value);
        }


        if (sensor.equals("inDust")) {
            industValue = value;
            Log.d("dust 상태", industValue);
        }

        if (sensor.equals("outDust")) {
            outdustValue = value;
            Log.d("dust 상태", outdustValue);
        }

        if (sensor.equals("window")) {
            if (value.equals("true")) {
                window_state = 1;
                Log.d("window_state 값", value);
            } else {
                window_state = 0;
                Log.d("window_state 값", "닫힘" + window_state);
            }

        }

        if (iTime >= 06 && iTime <= 15) {//낮

            restoreState();
            Log.d("window_state 값 다시받음", window_state + "");
            if (dustSettingState == 1 && rainSettingState == 0) {//미세먼지만 자동개폐 on
                if (window_state == 1) {
                    if ((industValue.equals("좋음") || industValue.equals("보통")) && (outdustValue.equals("좋음") || outdustValue.equals("보통"))) {
                        Glide.with(getApplicationContext())
                                .load(state_open_bright)
                                .into(FirstFragment.imageView);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(dustOpenBright)
                                .into(FirstFragment.imageView);
                    }
                } else {
                    if ((industValue.equals("좋음") || industValue.equals("보통")) && (outdustValue.equals("좋음") || outdustValue.equals("보통"))) {
                        Glide.with(getApplicationContext())
                                .load(state_close_bright)
                                .into(FirstFragment.imageView);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(dustCloseBright)
                                .into(FirstFragment.imageView);
                    }
                }
            } else if (dustSettingState == 0 && rainSettingState == 1) {//비만 자동개폐 on

                if (window_state == 1) {
                    if (rainValue > 0) {
                        Glide.with(getApplicationContext())
                                .load(rainOpenBright)
                                .into(FirstFragment.imageView);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(state_open_bright)
                                .into(FirstFragment.imageView);
                    }
                } else if (window_state == 0) {
                    if (rainValue > 0) {
                        Glide.with(getApplicationContext())
                                .load(rainCloseBright)
                                .into(FirstFragment.imageView);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(state_close_bright)
                                .into(FirstFragment.imageView);
                    }
                }
            }else if (dustSettingState == 1 && rainSettingState == 1) {//둘 다 자동개폐 on
                if (window_state == 1) {
                        if ((outdustValue.equals("좋음") || outdustValue.equals("보통"))) {
                            Glide.with(getApplicationContext())
                                    .load(state_open_bright)
                                    .into(FirstFragment.imageView);

                        } else {
                            Glide.with(getApplicationContext())
                                    .load(dustOpenBright)
                                    .into(FirstFragment.imageView);
                        }

                }else{
                            Glide.with(getApplicationContext())
                                    .load(rainCloseBright)
                                    .into(FirstFragment.imageView);


                }
            }

        } else {//밤

            restoreState();
            Log.d("window_state 값 다시받음", window_state + "");
            if (dustSettingState == 1 && rainSettingState == 0) {//미세먼지만 자동개폐 on
                if (window_state == 1) {
                    if ((industValue.equals("좋음") || industValue.equals("보통")) && (outdustValue.equals("좋음") || outdustValue.equals("보통"))) {
                        Glide.with(getApplicationContext())
                                .load(state_open_dark)
                                .into(FirstFragment.imageView);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(dustOpenDark)
                                .into(FirstFragment.imageView);
                    }
                } else {
                    if ((industValue.equals("좋음") || industValue.equals("보통")) && (outdustValue.equals("좋음") || outdustValue.equals("보통"))) {
                        Glide.with(getApplicationContext())
                                .load(state_close_dark)
                                .into(FirstFragment.imageView);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(dustCloseDark)
                                .into(FirstFragment.imageView);
                    }
                }
            } else if (dustSettingState == 0 && rainSettingState == 1) {//비만 자동개폐 on

                if (window_state == 1) {
                    if (rainValue > 0) {
                        Glide.with(getApplicationContext())
                                .load(rainOpenDark)
                                .into(FirstFragment.imageView);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(state_open_dark)
                                .into(FirstFragment.imageView);
                    }
                } else if (window_state == 0) {
                    if (rainValue > 0) {
                        Glide.with(getApplicationContext())
                                .load(rainCloseDark)
                                .into(FirstFragment.imageView);
                    } else {
                        Glide.with(getApplicationContext())
                                .load(state_close_dark)
                                .into(FirstFragment.imageView);
                    }
                }
            }else if (dustSettingState == 1 && rainSettingState == 1) {//둘 다 자동개폐 on
                if (window_state == 1) {
                    if ((outdustValue.equals("좋음") || outdustValue.equals("보통"))) {
                        Glide.with(getApplicationContext())
                                .load(state_open_dark)
                                .into(FirstFragment.imageView);

                    } else {
                        Glide.with(getApplicationContext())
                                .load(dustOpenDark)
                                .into(FirstFragment.imageView);
                    }

                }else{
                    Glide.with(getApplicationContext())
                            .load(rainCloseDark)
                            .into(FirstFragment.imageView);
                }
            }
        }
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