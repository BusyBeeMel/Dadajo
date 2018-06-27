package com.example.student.dadajo;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
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
import static com.example.student.dadajo.SettingActivity.dustSettingState;
import static com.example.student.dadajo.SettingActivity.rainSettingState;


public class MainActivity extends AppCompatActivity implements MqttCallback {
    RelativeLayout main;
    MqttClient client;
    /*static int rainSettingState;
    static int dustSettingState;*/
    Button settingBtn;
    FragmentPagerAdapter adapterViewPager;

    long mNow;
    Date mDate;
    SimpleDateFormat mFormat = new SimpleDateFormat("kk");
    String mTime;
    static int iTime;

    static String industValue="좋음";
    static String outdustValue="좋음";
    static Float rainValue = 0.00f;

    static Context context;

    static boolean isDustBad=true;
    static boolean isRainning=true;

    static NotificationManager mNotificationManager;
    NotificationCompat.Builder windowBuilderOpen;
    NotificationCompat.Builder windowBuilderClose;
    NotificationCompat.Builder dustBuilder;
    NotificationCompat.Builder rainBuilder;
    int notifyId=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, LoadingActivity.class);
        startActivity(intent);

        context=getApplicationContext();

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




        setContentView(R.layout.activity_main);
        main=(RelativeLayout)findViewById(R.id.main);


        restoreState();


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

        if (iTime >= 06 && iTime <= 12) {//낮
            main.setBackgroundColor(Color.parseColor("#DCE0DE"));
        }else{
            main.setBackgroundColor(Color.parseColor("#6D7170"));
        }

        Bitmap mLargeIconForNoti =
                BitmapFactory.decodeResource(getResources(), R.drawable.logo);

        PendingIntent mPendingIntent = PendingIntent.getActivity(MainActivity.this, 0,
                new Intent(getApplicationContext(), MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);




        // 알림 ID
        int notifyID = 1;

        // 위에서 생성한 채널 ID 이름
        String CHANNEL_ID = "my_channel_01";

        windowBuilderOpen =
                new NotificationCompat.Builder(MainActivity.this)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("창문 개폐 알림")
                        .setContentText("창문이 열렸어요")
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setLargeIcon(mLargeIconForNoti)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setContentIntent(mPendingIntent)
                        .setChannelId(CHANNEL_ID);

        windowBuilderClose =
                new NotificationCompat.Builder(MainActivity.this)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("창문 개폐 알림")
                        .setContentText("창문이 닫혔어요")
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setLargeIcon(mLargeIconForNoti)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setContentIntent(mPendingIntent)
                        .setChannelId(CHANNEL_ID);

        dustBuilder =
                new NotificationCompat.Builder(MainActivity.this)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("미세먼지 경보")
                        .setContentText("실외 미세먼지가 나빠요! 외부 활동에 주의하세요")
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setLargeIcon(mLargeIconForNoti)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setContentIntent(mPendingIntent)
                        .setChannelId(CHANNEL_ID);

        rainBuilder =
                new NotificationCompat.Builder(MainActivity.this)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("비 알림")
                        .setContentText("비가 오고 있어요!")
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setLargeIcon(mLargeIconForNoti)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setContentIntent(mPendingIntent)
                        .setChannelId(CHANNEL_ID)
                        .setWhen(System.currentTimeMillis());



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




    protected static void restoreState() {


        //   SharedPreferences pref = getSharedPreferences("preferences", Activity.MODE_PRIVATE);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
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
        client = new MqttClient("tcp://192.168.43.151:1883",
                MqttClient.generateClientId(),
                new MemoryPersistence());
        client.setCallback(this);
        client.connect();
        client.subscribe("home/out/#"); // 구독할 토픽 설정
        client.subscribe("home/in/#");
        client.subscribe("home/control/window");
        client.subscribe("home/control/air");
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
                    Log.d("윈도우", "윈도우 받음");
                    setWindow(sensor,value);
                    if(SettingActivity.windowAlarmState==1) {
                        showWindowNotification(value);
                    }
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

                if(value_int>=80.0&&isDustBad){
                    showDustNotification();
                    isDustBad=false;
                }else{
                    isDustBad=true;
                }
                break;

            case "rain":
                Float rainValue=Float.parseFloat(value);
                setWindow("rain",value);

                if(rainValue>=30.0&&isRainning){
                    showRainNotification();
                    isRainning=false;
                }else{
                    isRainning=true;
                }



            default:
                break;
        }
    }

    //창문 개폐 알림
    public void showWindowNotification(String windowState) {

        // 채널 ID
        String id = "my_channel_01";

        // 채널 이름
        CharSequence name = "test";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);

        // 알림 채널에 사용할 설정을 구성
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.BLUE);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        // 뱃지 사용 여부를 설정한다.(8.0부터는 기본이 true)
        mChannel.setShowBadge(false);
        mNotificationManager.createNotificationChannel(mChannel);

        if(windowState.equals("true")){
            mNotificationManager.notify(0, windowBuilderOpen.build());
        }else{
            mNotificationManager.notify(1, windowBuilderClose.build());
        }

    }

    //미세먼지 경보 알림
    public void showDustNotification() {
        // 채널 ID
        String id = "my_channel_01";

        // 채널 이름
        CharSequence name = "test";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);

        // 알림 채널에 사용할 설정을 구성
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.BLUE);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        // 뱃지 사용 여부를 설정한다.(8.0부터는 기본이 true)
        mChannel.setShowBadge(false);
        mNotificationManager.createNotificationChannel(mChannel);
        mNotificationManager.notify(2, dustBuilder.build());
    }

    //창문 개폐 알림
    public void showRainNotification() {

        // 채널 ID
        String id = "my_channel_01";

        // 채널 이름
        CharSequence name = "test";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);

        // 알림 채널에 사용할 설정을 구성
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.BLUE);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        // 뱃지 사용 여부를 설정한다.(8.0부터는 기본이 true)
        mChannel.setShowBadge(false);
        mNotificationManager.createNotificationChannel(mChannel);
        mNotificationManager.notify(3, rainBuilder.build());

    }


    public static void setWindow(String sensor, String value) {

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

        if (iTime >= 06 && iTime <= 12) {//낮

            restoreState();
            Log.d("window_state 값 다시받음", window_state + "");
            if (dustSettingState == 1 && rainSettingState == 0) {//미세먼지만 자동개폐 on
                if (window_state == 1) {
                    if ((industValue.equals("좋음") || industValue.equals("보통")) && (outdustValue.equals("좋음") || outdustValue.equals("보통"))) {
                        Glide.with(context)
                                .load(state_open_bright)
                                .into(FirstFragment.imageView);
                    } else {
                        Glide.with(context)
                                .load(dustOpenBright)
                                .into(FirstFragment.imageView);
                    }
                } else {
                    if ((industValue.equals("좋음") || industValue.equals("보통")) && (outdustValue.equals("좋음") || outdustValue.equals("보통"))) {
                        Glide.with(context)
                                .load(state_close_bright)
                                .into(FirstFragment.imageView);
                    } else {
                        Glide.with(context)
                                .load(dustCloseBright)
                                .into(FirstFragment.imageView);
                    }
                }
            } else if (dustSettingState == 0 && rainSettingState == 1) {//비만 자동개폐 on

                if (window_state == 1) {
                    if (rainValue > 0) {
                        Glide.with(context)
                                .load(rainOpenBright)
                                .into(FirstFragment.imageView);
                    } else {
                        Glide.with(context)
                                .load(state_open_bright)
                                .into(FirstFragment.imageView);
                    }
                } else if (window_state == 0) {
                    if (rainValue > 0) {
                        Glide.with(context)
                                .load(rainCloseBright)
                                .into(FirstFragment.imageView);
                    } else {
                        Glide.with(context)
                                .load(state_close_bright)
                                .into(FirstFragment.imageView);
                    }
                }
            }else if ((dustSettingState == 1 && rainSettingState == 1) || (dustSettingState == 0 && rainSettingState == 0) ) {//둘 다 자동개폐 on
                if (window_state == 1) {//비가 안 옴

                        if ((outdustValue.equals("좋음") || outdustValue.equals("보통"))) {
                            Glide.with(context)
                                    .load(state_open_bright)
                                    .into(FirstFragment.imageView);

                        } else {

                                Glide.with(context)
                                        .load(dustOpenBright)
                                        .into(FirstFragment.imageView);

                        }
                }else{//비가 오거나 비가 안 오는데 실외 미세먼지가 더 높음

                    Log.d("rainValue 상태", rainValue+"");
                        if(rainValue>=30) {
                            Glide.with(context)
                                    .load(rainCloseBright)
                                    .into(FirstFragment.imageView);
                        }else{
                            if ((outdustValue.equals("좋음") || outdustValue.equals("보통"))) {
                                Glide.with(context)
                                        .load(state_close_bright)
                                        .into(FirstFragment.imageView);

                            } else {

                                    Glide.with(context)
                                            .load(dustCloseBright)
                                            .into(FirstFragment.imageView);

                            }
                        }

                }
            }

        } else {//밤

            restoreState();
            Log.d("window_state 값 다시받음", window_state + "");
            if (dustSettingState == 1 && rainSettingState == 0) {//미세먼지만 자동개폐 on
                if (window_state == 1) {
                    if ((industValue.equals("좋음") || industValue.equals("보통")) && (outdustValue.equals("좋음") || outdustValue.equals("보통"))) {
                        Glide.with(context)
                                .load(state_open_dark)
                                .into(FirstFragment.imageView);
                    } else {
                        Glide.with(context)
                                .load(dustOpenDark)
                                .into(FirstFragment.imageView);
                    }
                } else {
                    if ((industValue.equals("좋음") || industValue.equals("보통")) && (outdustValue.equals("좋음") || outdustValue.equals("보통"))) {
                        Glide.with(context)
                                .load(state_close_dark)
                                .into(FirstFragment.imageView);
                    } else {
                        Glide.with(context)
                                .load(dustCloseDark)
                                .into(FirstFragment.imageView);
                    }
                }
            } else if (dustSettingState == 0 && rainSettingState == 1) {//비만 자동개폐 on

                if (window_state == 1) {
                    if (rainValue > 0) {
                        Glide.with(context)
                                .load(rainOpenDark)
                                .into(FirstFragment.imageView);
                    } else {
                        Glide.with(context)
                                .load(state_open_dark)
                                .into(FirstFragment.imageView);
                    }
                } else if (window_state == 0) {
                    if (rainValue > 0) {
                        Glide.with(context)
                                .load(rainCloseDark)
                                .into(FirstFragment.imageView);
                    } else {
                        Glide.with(context)
                                .load(state_close_dark)
                                .into(FirstFragment.imageView);
                    }
                }
            }else if ((dustSettingState == 1 && rainSettingState == 1)) {//둘 다 자동개폐 on
                if (window_state == 1) {//비가 안 옴

                    if ((outdustValue.equals("좋음") || outdustValue.equals("보통"))) {
                        Glide.with(context)
                                .load(state_open_dark)
                                .into(FirstFragment.imageView);

                    } else {

                        Glide.with(context)
                                .load(dustOpenDark)
                                .into(FirstFragment.imageView);

                    }
                }else{//비가 오거나 비가 안 오는데 실외 미세먼지가 더 높음

                    Log.d("rainValue 상태", rainValue+"");
                    if(rainValue>=30) {
                        Glide.with(context)
                                .load(rainCloseDark)
                                .into(FirstFragment.imageView);
                    }else{
                        if ((outdustValue.equals("좋음") || outdustValue.equals("보통"))) {
                            Glide.with(context)
                                    .load(state_close_dark)
                                    .into(FirstFragment.imageView);

                        } else {

                            Glide.with(context)
                                    .load(dustCloseDark)
                                    .into(FirstFragment.imageView);

                        }
                    }

                }
            }else if((dustSettingState == 0 && rainSettingState == 0)){//둘 다 자동개폐 off
                Log.d("rainValue 상태", rainValue+"");
                if (window_state == 1) {
                    if(rainValue>=30){
                        Glide.with(context)
                                .load(rainOpenDark)
                                .into(FirstFragment.imageView);
                    }else {
                        if ((outdustValue.equals("좋음") || outdustValue.equals("보통"))) {
                            Glide.with(context)
                                    .load(state_open_dark)
                                    .into(FirstFragment.imageView);

                        } else {

                            Glide.with(context)
                                    .load(dustOpenDark)
                                    .into(FirstFragment.imageView);

                        }
                    }
                }else{//비가 오거나 비가 안 오는데 실외 미세먼지가 더 높음
                    Log.d("rainValue 상태", rainValue+"");
                    if(rainValue>=30) {
                        Glide.with(context)
                                .load(rainCloseDark)
                                .into(FirstFragment.imageView);
                    }else{
                        if ((outdustValue.equals("좋음") || outdustValue.equals("보통"))) {
                            Glide.with(context)
                                    .load(state_close_dark)
                                    .into(FirstFragment.imageView);

                        } else {

                            Glide.with(context)
                                    .load(dustCloseDark)
                                    .into(FirstFragment.imageView);

                        }
                    }

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