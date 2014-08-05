/**
 * Created by tommy on 7/24/14.
 */

package com.example.myfirstapp.app;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class SelectControllerFrag extends Fragment {
    //Debugging
    private static final String TAG = "SelectControllerFrag";
    private static boolean D = false;

    //Controller codes defining which controller has been selected
    public static final int BT_CHAT = 0;
    public static final int BUTTON_CONTROL = 1;
    public static final int SLIDER_CONTROL = 2;
    public static final int MOTION_CONTROL = 3;

    //Buttons for the different controllers
    private Button bt_chat;
    private Button button_control;
    private Button slider_control;
    private Button motion_control;

    private SelectControllerFragListener mListener;

    public SelectControllerFrag() {
        // Required empty public constructor
    }

    public static SelectControllerFrag newInstance(String param1, String param2) {
        SelectControllerFrag fragment = new SelectControllerFrag();
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

        View view = inflater.inflate(R.layout.fragment_select_controller, container, false);

        //Configure buttons
        bt_chat = (Button) view.findViewById(R.id.launch_bt_chat);
        bt_chat.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                select_controller(BT_CHAT);
            }
        });
        button_control = (Button) view.findViewById(R.id.launch_button_controller);
        button_control.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                select_controller(BUTTON_CONTROL);
            }
        });
        slider_control = (Button) view.findViewById(R.id.launch_slider_controller);
        slider_control.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                select_controller(SLIDER_CONTROL);
            }
        });
        motion_control = (Button) view.findViewById(R.id.launch_motion_controller);
        motion_control.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                select_controller(MOTION_CONTROL);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (SelectControllerFragListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SelectControllerFragListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void select_controller(int controller_code) {
        if (mListener != null) {
            mListener.selectControllerFrag_selectFrag(controller_code);
        }
    }

    public interface SelectControllerFragListener {
        public void selectControllerFrag_selectFrag(int controller_code);
    }
}
