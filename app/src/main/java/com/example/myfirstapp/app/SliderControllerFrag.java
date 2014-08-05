/**
 * Created by tommy on 7/24/14.
 */

package com.example.myfirstapp.app;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SliderControllerFrag extends Fragment{
    //Debugging
    private static final String TAG = "SliderControllerFrag";
    private static boolean D = false;

    //how much time to wait before each message
    private int normalInterval = 100;
    //Defines whether we are connected or not
    private Boolean mmConnectionStatus = false;

    private View v;
    //Sets up the scrollbars
    private SeekBar sb_acc;
    private SeekBar sb_turn;

    //Thread in charge of sending messages
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

        //Starts thread that sends messages
        messageHandler = new MessageHandler();
        if(messageHandler != null){
            messageHandler.set_connected(mmConnectionStatus);
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
        //Updates current connection status for the thread on resume
        if(messageHandler != null){
            messageHandler.set_connected(mmConnectionStatus);
        }
        normalInterval = ((HomeScreen)getActivity()).getNormalInterval();
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
        synchronized(mmConnectionStatus){
            if(mmConnectionStatus && mListener != null){
                mListener.onSliderControllerInteraction(message.toString());
            }
        }
    }

    public void change_connection_status(Boolean cChange) {
        if(D) Log.e(TAG, "attempt to change current connection_status for buttoncontroller to" + cChange.toString());
        synchronized (mmConnectionStatus) {
            mmConnectionStatus = cChange;
        }
        if (messageHandler != null) {
            messageHandler.set_connected(cChange);
        }
    }

    public interface OnSliderControllerInteractionListener {
        public void onSliderControllerInteraction(String message);
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
            if(D) Log.e(TAG, "set connected status");
            synchronized(connection_status){
                connection_status = cstatus;
            }
        }

        @Override
        public void run() {
            if(D) Log.e(TAG, "messaghandler run method called");
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
