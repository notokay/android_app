package com.example.myfirstapp.app;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;

/**
 * Created by tommy on 7/24/14.
 */
public class ButtonControllerFrag extends Fragment {

    private Button up_button;
    private Button down_button;
    private Button left_button;
    private Button right_button;
    private Button start_button;
    private Button stop_button;

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
        up_button.setOnTouchListener(new RepeatListener(400, 100, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("up");
            }
        }));

        down_button = (Button) view.findViewById(R.id.down);
        down_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand("down");
            }
        });
        left_button = (Button) view.findViewById(R.id.left);
        left_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand("left");
            }
        });
        right_button = (Button) view.findViewById(R.id.right);
        right_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand("right");
            }
        });
        start_button = (Button) view.findViewById(R.id.start);
        start_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand("start");
            }
        });
        stop_button = (Button) view.findViewById(R.id.stop);
        stop_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand("stop");
            }
        });

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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void sendCommand(String message) {
        mListener.onButtonControllerInteraction(message);
    }

    public interface OnButtonControllerInteractionListener {
        public void onButtonControllerInteraction(String message);
    }

}
