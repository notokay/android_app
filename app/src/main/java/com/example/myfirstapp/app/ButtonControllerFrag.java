package com.example.myfirstapp.app;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;

/**
 * Created by tommy on 7/24/14.
 */
public class ButtonControllerFrag extends Fragment {

    private static final String TAG = "ButtonControllerFrag";
    private final static int UP_BUTTON = 1;
    private final static int DOWN_BUTTON = 2;
    private final static int LEFT_BUTTON = 4;
    private final static int RIGHT_BUTTON = 8;
    private final static int START_BUTTON = 16;
    private final static int STOP_BUTTON = 32;
    private final int normalInterval = 100;
    private Button up_button;
    private Button down_button;
    private Button left_button;
    private Button right_button;
    private Button start_button;
    private Button stop_button;
    private MessageHandler messageHandler;
    private Boolean bt_connection_status;

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
        final View view = inflater.inflate(R.layout.fragment_button_controls, container, false);
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
            Log.e(TAG, "ButtonControllerFrag messageHandler took too long to shut down");
        }
        mListener = null;
    }

    private void sendCommand(int message) {
        synchronized(bt_connection_status){
            if(bt_connection_status && mListener != null){
                mListener.onButtonControllerInteraction((byte)message);
            }
        }
    }

    public void change_status(Boolean cChange) {
        Log.e(TAG, "attempt to change current connection_status for buttoncontroller to" + cChange.toString());
        bt_connection_status = cChange;
        if (messageHandler != null) {
            messageHandler.set_connected(cChange);
        }
    }

    public interface OnButtonControllerInteractionListener {
        public void onButtonControllerInteraction(byte message);
    }

    private class MessageHandler extends Thread {
        private Boolean button_pressed = false;
        private Integer current_message = 0;
        private Boolean connection_status = false;

        public void or_message(int message){
            Log.e(TAG, "xor_message method called");
            synchronized (current_message){
                current_message |= message;
            }
        }

        public void xor_message(int message){
            Log.e(TAG, "xor_message method called");
            synchronized(current_message){
                current_message ^= message;
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
