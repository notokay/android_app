package com.example.myfirstapp.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import java.lang.Math;

/**
 * Created by tommy on 7/24/14.
 */
public class MotionControllerFrag extends Fragment implements SensorEventListener {
    private final String TAG = "MotionControllerFrag";

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Boolean sensor_exists = false;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;

    private SeekBar sb_acc;
    private SeekBar sb_turn;
    private View v;

    private Boolean bt_connection_status = false;
    private final int normalInterval = 100;
    private MessageHandler messageHandler;

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
        View view = inflater.inflate(R.layout.fragment_motion_controls, container, false);
        v = view;
        //Configure seekbar
        sb_turn = (SeekBar)view.findViewById(R.id.slider_turning);
        sb_turn.setMax(180);
        sb_turn.setProgress(0);

        sb_acc = (SeekBar)view.findViewById(R.id.slider_acc);
        sb_acc.setMax(1000);
        sb_acc.setProgress(500);
        sb_acc.setOnSeekBarChangeListener(new VerticalSliderListener());

        Log.e(TAG, "start sensor detection");
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null){
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            sensor_exists = true;
            Log.e(TAG, "sensor exists");
        }
        else {
            mListener.motionControllerFrag_make_toast(getResources().getString(R.string.sensor_not_detected));
            Log.e(TAG, "no sensor");
            sensor_exists = false;
        }

        messageHandler = new MessageHandler();
        if(messageHandler != null) messageHandler.set_connected(bt_connection_status);
        messageHandler.start();

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
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
        Log.e(TAG, "sensor registered");
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
            Log.e(TAG, "MotionControllerFrag messageHandler took too long to shut down");
        }
        mListener = null;
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    @Override
    public final void onSensorChanged(SensorEvent event){
        float[] deltaRotationMatrix = new float[9];
        float[] orientation = new float[3];

        // This timestep's delta rotation to be multiplied by the current rotation
        // after computing it from the gyro sample data.
        Log.e(TAG, "Sensor timestamp" + ((Long)event.timestamp).toString());
        SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, event.values);
        SensorManager.getOrientation(deltaRotationMatrix, orientation);
        Integer current_rotation = (int)(orientation[1]*(180/Math.PI));
        // User code should concatenate the delta rotation we computed with the current rotation
        // in order to get the updated rotation.
        // rotationCurrent = rotationCurrent * deltaRotationMatrix;
        sb_turn.setProgress(current_rotation+90);
        if(messageHandler != null) messageHandler.set_turning_message(current_rotation+90);
        TextView tv = (TextView)v.findViewById(R.id.percent_turning);
        tv.setText(Integer.toString(current_rotation));
    }

    public interface OnMotionControllerInteractionListener {
        public void onMotionControllerInteraction(String message);
        public void motionControllerFrag_make_toast(String toast_text);
    }

    private class VerticalSliderListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean isUser){
            if(messageHandler != null) messageHandler.set_acc_message(progress);
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

    public void change_conncetion_status(Boolean cChange){
        Log.e(TAG, "attempt to change current connection_status for motioncontroller to" + cChange.toString());
        synchronized(bt_connection_status){
            bt_connection_status = cChange;
        }
        if(messageHandler != null){
            messageHandler.set_connected(cChange);
        }
    }

    private void sendCommand(Integer message) {
        synchronized(bt_connection_status){
            if(bt_connection_status && mListener != null){
                mListener.onMotionControllerInteraction(message.toString());
            }
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
