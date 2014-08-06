/**
 * Created by tommy on 7/24/14.
 */

package com.example.myfirstapp.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

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

    List<Controller_Button> mControllerList;
    private ControllersArrayAdapter mControllerArrayAdapter;
    private ListView mControllerView;

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

        String[] titles = new String[]{
                getResources().getString(R.string.launch_bt_chat),
                getResources().getString(R.string.launch_button_controller),
                getResources().getString(R.string.launch_slider_controller),
                getResources().getString(R.string.launch_motion_controller)
        };
        Integer[] controller_id = new Integer[]{
                BT_CHAT,
                BUTTON_CONTROL,
                SLIDER_CONTROL,
                MOTION_CONTROL
        };

        mControllerList = new ArrayList<Controller_Button>();
        for(int i = 0; i < titles.length; i++){
            Controller_Button controllerButton = new Controller_Button(titles[i], controller_id[i]);
            mControllerList.add(controllerButton);
        }
        mControllerView = (ListView) view.findViewById(R.id.controllers_list);
        mControllerArrayAdapter = new ControllersArrayAdapter(getActivity(), R.layout.controller_button, mControllerList);
        mControllerView.setAdapter(mControllerArrayAdapter);

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

    public class Controller_Button{
        private int controller_id;
        private String controller_name;

        public Controller_Button(String new_controller_name, int new_controller_id){
            controller_id = new_controller_id;
            controller_name = new_controller_name;
        }

        public String getController_name(){
            return controller_name;
        }
        public int getController_id(){
            return controller_id;
        }
    }

    private class ControllersArrayAdapter extends ArrayAdapter<Controller_Button> {
        private final Context context;

        public ControllersArrayAdapter(Context context, int resourceId, List<Controller_Button> items){
            super(context, resourceId, items);
            this.context = context;
        }

        /*private view holder class*/
        private class ViewHolder {
            Button mButton;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            ViewHolder holder = null;
            final Controller_Button controller_button = getItem(position);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(convertView == null){
                convertView = inflater.inflate(R.layout.controller_button, null);
                holder = new ViewHolder();
                holder.mButton = (Button) convertView.findViewById(R.id.controller_button);
                convertView.setTag(holder);
            }else
                holder = (ViewHolder)convertView.getTag();

            holder.mButton.setText(controller_button.getController_name());
            holder.mButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    select_controller(controller_button.getController_id());
                }
            });
            return convertView;
        }
    }
}
