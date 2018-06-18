package com.example.student.dadajo;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * A simple {@link Fragment} subclass.
 */
public class FirstFragment extends Fragment {
    private String title;
    private int page;
    static ImageView imageView;
    Switch switchWindow;

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
    static int window_state = 0;   // 현재 창문 상태(1 이면 열림, 0 이면 닫힘)]

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



    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_first, container, false);

        imageView=(ImageView)view.findViewById(R.id.img_window);
        switchWindow=(Switch)view.findViewById(R.id.switch_window);

        final int stateClose=R.drawable.window_close;
        final int stateOpen=R.drawable.window_open;
        Glide.with(view.getContext())
                .load(stateClose)
                .into(imageView);

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

}
