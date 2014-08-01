package com.example.myfirstapp.app;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;


/**
 * Created by tommy on 7/24/14.
 */
public class SliderControllerFrag extends Fragment{

    private static final String TAG = "SliderControllerFrag";
    private View v;
    private SeekBar sb_acc;
    private SeekBar sb_turn;
    private Boolean bt_connection_status = false;
    private final int normalInterval = 100;
    private MessageHandler messageHandler;

    private OnSliderControllerInteractionListener mListener;


    public SliderControllerFrag() {
        // Required empty public constructor
    }

    public static SliderControllerFrag newInstance(String param1, String param2) {
        SliderControllerFrag fragment = new SliderControllerFrag();
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
        v = view;
        //Configure seekbar
        sb_turn = (SeekBar)view.findViewById(R.id.slider_turning);
        sb_turn.setMax(1000);
        sb_turn.setProgress(500);
        sb_turn.setOnSeekBarChangeListener(new HorizontalSliderListener());

        sb_acc = (SeekBar)view.findViewById(R.id.slider_acc);
        sb_acc.setMax(1000);
        sb_acc.setProgress(500);
        sb_acc.setOnSeekBarChangeListener(new VerticalSliderListener());

        messageHandler = new MessageHandler();
        if(messageHandler != null){
            messageHandler.set_connected(bt_connection_status);
        }
        messageHandler.start();

        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnSliderControllerInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSliderControllerInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(messageHandler != null){
            messageHandler.set_connected(bt_connection_status);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        messageHandler.interrupt();
        try{
            messageHandler.join(500);
        } catch(InterruptedException e){
            Log.e(TAG, "SliderControllerFrag messageHandler took too long to shut down");
        }
        mListener = null;
    }


    private void sendCommand(Integer message) {
        synchronized(bt_connection_status){
            if(bt_connection_status && mListener != null){
                mListener.onSliderControllerInteraction(message.toString());
            }
        }
    }

    public interface OnSliderControllerInteractionListener {
        public void onSliderControllerInteraction(String message);
    }

    public void change_status(Boolean cChange){
        Log.e(TAG, "attempt to change current connection_status for buttoncontroller to" + cChange.toString());
        bt_connection_status = cChange;
        if(messageHandler != null){
            messageHandler.set_connected(cChange);
        }
    }


    private class VerticalSliderListener implements OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean isUser){
            TextView tv = (TextView)v.findViewById(R.id.percent_acc);
            tv.setText(Integer.toString(progress)+"%");
            messageHandler.set_acc_message(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            seekBar.setProgress(500);
        }
    }

    private class HorizontalSliderListener implements OnSeekBarChangeListener{

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean isUser){
            TextView tv = (TextView)v.findViewById(R.id.percent_turning);
            tv.setText(Integer.toString(progress)+"%");
            messageHandler.set_turning_message(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            seekBar.setProgress(500);
            messageHandler.set_turning_message(500);
        }
    }


    private class MessageHandler extends Thread {
        private Integer acc_message = 500;
        private Integer turning_message = 500;
        private Boolean connection_status = false;
        private Integer current_message = 0;

        public void set_acc_message(int message){
            synchronized (acc_message){
                acc_message = message;
            }
        }

        public void set_turning_message(int message){
            synchronized(turning_message){
                turning_message = message;
            }
        }

        public void set_connected(Boolean cstatus){
            Log.e(TAG, "set connected status");
            synchronized(connection_status){
                connection_status = cstatus;
            }
        }

        @Override
        public void run() {
            Log.e(TAG, "messaghandler run method called");
            while (connection_status) {
                try {
                    current_message = 0;
                    synchronized(turning_message){
                        current_message+=turning_message;
                    }
                    synchronized(acc_message){
                        current_message+=acc_message;
                    }
                    sendCommand(current_message);
                    this.sleep(normalInterval);
                } catch (InterruptedException e) {
                    Log.e(TAG, "MessageHandler thread interrupted");
                    return;
                }
            }
        }
    };

}
