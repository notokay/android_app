/**
 * Created by tommy on 7/24/14.
 */

package com.example.myfirstapp.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;

public class ButtonControllerFrag extends Fragment {

    //Debugging
    private static final String TAG = "ButtonControllerFrag";
    private static boolean D = false;

    //Values for buttons (assigned for bit purposes)
    private int UP_BUTTON = 1;
    private int DOWN_BUTTON = 2;
    private int LEFT_BUTTON = 4;
    private int RIGHT_BUTTON = 8;
    private int START_BUTTON = 16;
    private int STOP_BUTTON = 32;
    private int OPTION_1 = 0;
    private int OPTION_2 = 0;
    private int OPTION_3 = 0;
    private int OPTION_4 = 0;
    private int OPTION_5 = 0;
    private int OPTION_6 = 0;

    //Buttons for each action
    private Button up_button;
    private Button down_button;
    private Button left_button;
    private Button right_button;
    private Button start_button;
    private Button stop_button;
    private Button op1_button;
    private Button op2_button;
    private Button op3_button;
    private Button op4_button;
    private Button op5_button;
    private Button op6_button;

    //Time between each message
    private int normalInterval = 100;

    //Thread in charge of sending messages
    private MessageHandler messageHandler;

    //Communicates whether we are connected to bluetooth or not
    private Boolean mmConnectionStatus;

    private OnButtonControllerInteractionListener mListener;

    public ButtonControllerFrag() {
        // Required empty public constructor
    }

    public static ButtonControllerFrag newInstance() {
        ButtonControllerFrag fragment = new ButtonControllerFrag();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Debugging
        if(D) Log.e(TAG, "onCreateView");

        final View view = inflater.inflate(R.layout.fragment_button_controls, container, false);

        //Configure buttons
        up_button = (Button) view.findViewById(R.id.up);
        up_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        Log.e(TAG, "up button is pressed down");
                        messageHandler.or_message(UP_BUTTON);
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.e(TAG, "up button is released");
                        messageHandler.xor_message(UP_BUTTON);
                        break;
                }
                return false;
            }
        });

        down_button = (Button) view.findViewById(R.id.down);
        down_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        Log.e(TAG, "down button is pressed down");
                        messageHandler.or_message(DOWN_BUTTON);
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.e(TAG, "down button is released");
                        messageHandler.xor_message(DOWN_BUTTON);
                        break;
                }
                return false;
            }
        });
        left_button = (Button) view.findViewById(R.id.left);
        left_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        Log.e(TAG, "left button is pressed down");
                        messageHandler.or_message(LEFT_BUTTON);
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.e(TAG, "left button is released");
                        messageHandler.xor_message(LEFT_BUTTON);
                        break;
                }
                return false;
            }
        });

        right_button = (Button) view.findViewById(R.id.right);
        right_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        Log.e(TAG, "right button is pressed down");
                        messageHandler.or_message(RIGHT_BUTTON);
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.e(TAG, "right button is released");
                        messageHandler.xor_message(RIGHT_BUTTON);
                        break;
                }
                return false;
            }
        });
        start_button = (Button) view.findViewById(R.id.start);
        start_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        Log.e(TAG, "start button is pressed down");
                        messageHandler.or_message(START_BUTTON);
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.e(TAG, "start button is released");
                        messageHandler.xor_message(START_BUTTON);
                        break;
                }
                return false;
            }
        });
        stop_button = (Button) view.findViewById(R.id.stop);
        stop_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        Log.e(TAG, "stop button is pressed down");
                        messageHandler.or_message(STOP_BUTTON);
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.e(TAG, "stop button is released");
                        messageHandler.xor_message(STOP_BUTTON);
                        break;
                }
                return false;
            }
        });

        //Start thread to start sending command messages
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
            mListener = (OnButtonControllerInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnButtonControllerInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(messageHandler != null){
            messageHandler.set_connected(mmConnectionStatus);
        }
        normalInterval = ((HomeScreen)getActivity()).getNormalInterval();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        UP_BUTTON = Integer.valueOf(sharedPref.getString(getResources().getString(R.string.pref_button_controller_up_key), "1"));
        DOWN_BUTTON = Integer.valueOf(sharedPref.getString(getResources().getString(R.string.pref_button_controller_down_key), "2"));
        LEFT_BUTTON = Integer.valueOf(sharedPref.getString(getResources().getString(R.string.pref_button_controller_left_key), "4"));
        RIGHT_BUTTON = Integer.valueOf(sharedPref.getString(getResources().getString(R.string.pref_button_controller_right_key), "8"));
        START_BUTTON = Integer.valueOf(sharedPref.getString(getResources().getString(R.string.pref_button_controller_start_key), "16"));
        STOP_BUTTON = Integer.valueOf(sharedPref.getString(getResources().getString(R.string.pref_button_controller_stop_key), "32"));

        op1_button = (Button) getView().findViewById(R.id.button_controls_option_button_tl);
        if(sharedPref.getBoolean(getResources().getString(R.string.pref_buttonController_optional_1_key), false)){
            OPTION_1 = Integer.valueOf(sharedPref.getString(getResources().getString(R.string.pref_button_controller_optional_1_message_key), "0"));
            op1_button.setText(sharedPref.getString(getResources().getString(R.string.pref_button_controller_optional_1_text_key), "Option 1"));
            op1_button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch(event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            messageHandler.or_message(OPTION_1);
                            break;
                        case MotionEvent.ACTION_UP:
                            messageHandler.xor_message(OPTION_1);
                            break;
                    }
                    return false;
                }
            });
            op1_button.setVisibility(View.VISIBLE);
        }else op1_button.setVisibility(View.GONE);
        op2_button = (Button) getView().findViewById(R.id.button_controls_option_button_tc);
        if(sharedPref.getBoolean(getResources().getString(R.string.pref_buttonController_optional_2_key), false)){
            OPTION_2 = Integer.valueOf(sharedPref.getString(getResources().getString(R.string.pref_button_controller_optional_2_message_key), "0"));
            op2_button.setText(sharedPref.getString(getResources().getString(R.string.pref_button_controller_optional_2_text_key), "Option 2"));
            op2_button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch(event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            messageHandler.or_message(OPTION_2);
                            break;
                        case MotionEvent.ACTION_UP:
                            messageHandler.xor_message(OPTION_2);
                            break;
                    }
                    return false;
                }
            });
            op2_button.setVisibility(View.VISIBLE);
        }else op2_button.setVisibility(View.GONE);

        op3_button = (Button) getView().findViewById(R.id.button_controls_option_button_tr);
        if(sharedPref.getBoolean(getResources().getString(R.string.pref_buttonController_optional_3_key), false)){
            OPTION_3 = Integer.valueOf(sharedPref.getString(getResources().getString(R.string.pref_button_controller_optional_3_message_key), "0"));
            op3_button.setText(sharedPref.getString(getResources().getString(R.string.pref_button_controller_optional_3_text_key), "Option 3"));
            op3_button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch(event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            messageHandler.or_message(OPTION_3);
                            break;
                        case MotionEvent.ACTION_UP:
                            messageHandler.xor_message(OPTION_3);
                            break;
                    }
                    return false;
                }
            });
            op3_button.setVisibility(View.VISIBLE);
        }else op3_button.setVisibility(View.GONE);
        op4_button = (Button) getView().findViewById(R.id.button_controls_option_button_bl);
        if(sharedPref.getBoolean(getResources().getString(R.string.pref_buttonController_optional_4_key), false)){
            OPTION_1 = Integer.valueOf(sharedPref.getString(getResources().getString(R.string.pref_button_controller_optional_4_message_key), "0"));
            op4_button.setText(sharedPref.getString(getResources().getString(R.string.pref_button_controller_optional_4_text_key), "Option 4"));
            op4_button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch(event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            messageHandler.or_message(OPTION_4);
                            break;
                        case MotionEvent.ACTION_UP:
                            messageHandler.xor_message(OPTION_4);
                            break;
                    }
                    return false;
                }
            });
            op4_button.setVisibility(View.VISIBLE);
        }else op4_button.setVisibility(View.GONE);

        op5_button = (Button) getView().findViewById(R.id.button_controls_option_button_bc);
        if(sharedPref.getBoolean(getResources().getString(R.string.pref_buttonController_optional_5_key), false)){
            OPTION_5 = Integer.valueOf(sharedPref.getString(getResources().getString(R.string.pref_button_controller_optional_5_message_key), "0"));
            op5_button.setText(sharedPref.getString(getResources().getString(R.string.pref_button_controller_optional_5_text_key), "Option 5"));
            op5_button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch(event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            messageHandler.or_message(OPTION_5);
                            break;
                        case MotionEvent.ACTION_UP:
                            messageHandler.xor_message(OPTION_5);
                            break;
                    }
                    return false;
                }
            });
            op5_button.setVisibility(View.VISIBLE);
        }else op5_button.setVisibility(View.GONE);

        op6_button = (Button) getView().findViewById(R.id.button_controls_option_button_br);
        if(sharedPref.getBoolean(getResources().getString(R.string.pref_buttonController_optional_6_key), false)){
            OPTION_6 = Integer.valueOf(sharedPref.getString(getResources().getString(R.string.pref_button_controller_optional_6_message_key), "0"));
            op6_button.setText(sharedPref.getString(getResources().getString(R.string.pref_button_controller_optional_6_text_key), "Option 6"));
            op6_button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch(event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            messageHandler.or_message(OPTION_6);
                            break;
                        case MotionEvent.ACTION_UP:
                            messageHandler.xor_message(OPTION_6);
                            break;
                    }
                    return false;
                }
            });
            op6_button.setVisibility(View.VISIBLE);
        }else op6_button.setVisibility(View.GONE);
    }

        @Override
    public void onDetach() {
        super.onDetach();
        messageHandler.interrupt();
        try{
            messageHandler.join(500);
        } catch(InterruptedException e){
            Log.e(TAG, "ButtonControllerFrag messageHandler took too long to shut down");
        }
        mListener = null;
    }

    //if we are connected, have the main activity tell the bluetooth to send the message
    private void sendCommand(int message) {
        synchronized(mmConnectionStatus){
            if(mmConnectionStatus && mListener != null){
                mListener.onButtonControllerInteraction((byte)message);
            }
        }
    }

    //Public method allowing the main activity to update connection status
    public void change_connection_status(Boolean cChange) {
        if(D) Log.e(TAG, "attempt to change current connection_status for buttoncontroller to" + cChange.toString());
        mmConnectionStatus = cChange;
        //Update the thread as well, because it only loops when we are connected
        if (messageHandler != null) {
            messageHandler.set_connected(cChange);
        }
    }

    public interface OnButtonControllerInteractionListener {
        public void onButtonControllerInteraction(byte message);
    }

    private class MessageHandler extends Thread {
        private Integer current_message = 0;
        private Boolean connection_status = false;

        public void or_message(int message){
            if(D) Log.e(TAG, "xor_message method called");
            synchronized (current_message){
                current_message |= message;
            }
        }

        public void xor_message(int message){
            if(D) Log.e(TAG, "xor_message method called");
            synchronized(current_message){
                current_message ^= message;
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
            while (connection_status) {
                try {
                    synchronized(current_message){
                        sendCommand(current_message);
                    }
                    this.sleep(normalInterval);
                } catch (InterruptedException e) {
                    Log.e(TAG, "MessageHandler thread interrupted");
                    return;
                }
            }
        }
    };

}
