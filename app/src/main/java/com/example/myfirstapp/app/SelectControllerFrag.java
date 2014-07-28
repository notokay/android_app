package com.example.myfirstapp.app;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;


public class SelectControllerFrag extends Fragment {
    public static final int BT_CHAT = 0;
    public static final int BUTTON_CONTROL = 1;
    public static final int SLIDER_CONTROL = 2;
    public static final int MOTION_CONTROL = 3;
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Button bt_chat;
    private Button button_control;
    private Button slider_control;
    private Button motion_control;

    private String mParam1;
    private String mParam2;

    private SelectControllerFragListener mListener;

    public SelectControllerFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SelectControllerFrag.
     */
    public static SelectControllerFrag newInstance(String param1, String param2) {
        SelectControllerFrag fragment = new SelectControllerFrag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_controller, container, false);
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
            mListener.selectFrag(controller_code);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface SelectControllerFragListener {
        public void selectFrag(int controller_code);
    }

}
