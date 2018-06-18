package com.example.student.dadajo;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface SensorApi {
    //int window = FirstFragment.window_state;



    @GET("window")
    Call<Integer> getWindow();

    @PUT("window")
    Call<Integer> putWindow();

    static SensorApi service =
            new Retrofit.Builder()
                    .baseUrl("http://70.12.112.61:8080/dadajo2/api/sensor/")  //통신할 서버 주소
                    .addConverterFactory(GsonConverterFactory.create())     //문자열->객체 변환시 gson converter를 쓰겠다.
                    .build()   //클래스 생성
                    .create(SensorApi.class);     //타입지정하여 인스턴스 생성

}

