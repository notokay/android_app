package com.example.myfirstapp.app;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by tommy on 7/24/14.
 */
public class MotionControllerFrag extends Fragment {
    private SeekBar sb_acc;
    private SeekBar sb_turn;
    private View v;

    private OnMotionControllerInteractionListener mListener;

    public MotionControllerFrag() {
        // Required empty public constructor
    }

    public static MotionControllerFrag newInstance(String param1, String param2) {
        MotionControllerFrag fragment = new MotionControllerFrag();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_slider_controls, container, false);
        //Configure seekbar
        sb_turn = (SeekBar)view.findViewById(R.id.slider_turning);
        sb_turn.setMax(1000);
        sb_turn.setProgress(500);

        sb_acc = (SeekBar)view.findViewById(R.id.slider_acc);
        sb_acc.setMax(1000);
        sb_acc.setProgress(500);
        sb_acc.setOnSeekBarChangeListener(new VerticalSliderListener());

        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnMotionControllerInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMotionControllerInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnMotionControllerInteractionListener {
        public void onMotionControllerInteraction(String message);
    }

    private class VerticalSliderListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean isUser){
            TextView tv = (TextView)v.findViewById(R.id.percent_acc);
            tv.setText(Integer.toString(progress)+"%");
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            seekBar.setProgress(500);
        }
    }


}
