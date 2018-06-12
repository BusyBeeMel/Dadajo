package com.example.student.dadajo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.example.student.dadajo.R;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class MainActivity extends AppCompatActivity implements MqttCallback {
    MqttClient client;

    TextView WindowView;
    TextView tempInView;
    TextView humidInView;
    TextView dustInView;
    TextView tempOutView;
    TextView humidOutView;
    TextView dustOutView;

    float temp_in;          // 집 안 온도
    float humid_in;         // 집 안 습도
    float temp_out;         // 바깥 온도
    float humid_out;        // 바깥 습도
    float dust_in;
    float dust_out;
    int window_state = 0;   // 현재 창문 상태(1 이면 열림, 0 이면 닫힘)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try{
            connectMqtt(); // Mqtt broker 접속
            Log.d("connection", "접속성공");
        }catch (MqttException e){
            e.printStackTrace();
        }

        tempInView = (TextView)findViewById(R.id.tempInView);
        humidInView = (TextView)findViewById(R.id.humidInView);
        dustInView=(TextView)findViewById(R.id.dustInView);
        tempOutView = (TextView)findViewById(R.id.tempOutView);
        humidOutView = (TextView)findViewById(R.id.humidOutView);
        dustOutView=(TextView)findViewById(R.id.dustOutView);
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
        switch(sensor){
            case "temp":
                temp_in = Float.parseFloat(value);
                tempInView.setText(value + "'C");
                break;
            case "humid":
                humid_in = Float.parseFloat(value);
                humidInView.setText(value + "%");
                break;
            case "dust":
                dust_in = Float.parseFloat(value);
                dustInView.setText(value + "%");
                break;
            default:
                break;
        }

        // 창문 상태 설정
        setWindow();
    }

    // 바깥 센서값 표시
    public void updateDataOut(String sensor, String value){
        switch(sensor){
            case "temp":
                temp_out = Float.parseFloat(value);
                tempOutView.setText(value + "'C");
                break;
            case "humid":
                humid_out = Float.parseFloat(value);
                humidOutView.setText(value + "%");
                break;
            case "dust":
                dust_out = Float.parseFloat(value);
                dustOutView.setText(value + "%");
                break;

            default:
                break;
        }

        // 창문 상태 설정
        setWindow();
    }

    public void setWindow(){
        int state; // 변화될 창문 상태

        if(dust_in > dust_out){
            state = 1;
        }else{
            state = 0;
        }

        // 현재 창문 상태와 다를 경우에만 토픽 발행
        if(state != window_state){
            try {
                MqttMessage message = new MqttMessage();
                message.setPayload((state+"").getBytes());
                client.publish("home/control/window", message);
                Log.d("publish", message.toString());
                window_state = state;

                runOnUiThread(new Runnable() { //UI 작업은 UIThread에서
                    @Override
                    public void run() {
                        String value = (window_state == 1) ? "Open" : "Close";
                        WindowView.setText(value);
                    }
                });

            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken){

    }
}