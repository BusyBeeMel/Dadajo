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
    ImageView imageView;
    Switch switchWindow;

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

        switchWindow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Glide.with(getContext())
                            .load(stateOpen)
                            .into(imageView);
                   /* runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(getContext())
                                    .load(stateOpen)
                                    .into(imageView);
                        }
                    });*/
                } else {
                    Glide.with(getContext())
                            .load(stateClose)
                            .into(imageView);
                    /*runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(getContext())
                                    .load(stateClose)
                                    .into(imageView);
                        }
                    });*/
                }
            }
        });

        return view;
    }

}
