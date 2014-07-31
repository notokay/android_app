package com.example.myfirstapp.app;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothClass;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.example.myfirstapp.app.R;

public class HomeScreen extends FragmentActivity implements StartBluetoothFrag.startBluetoothFragListener, DeviceListFrag.OnListPressedListener, SelectControllerFrag.SelectControllerFragListener, BtChatFrag.OnBtChatInteractionListener, ButtonControllerFrag.OnButtonControllerInteractionListener {
    //Debugging
    private static final String TAG = "HomeScreen";
    private static final int NULL = 0;
    private static final int STARTBLUETOOTHFRAG = 1;
    private static final int SELECTCONTROLLERFRAG = 2;
    private static final int CONTROLLER_SELECTED = 3;
    private static final int BTCHAT_SELECTED = 4;
    private final FragmentManager fragmentManager = getFragmentManager();
    private int current_frag;
    private Boolean connection_status = false;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        fragmentManager.enableDebugLogging(true);
        Log.e(TAG, "fragmentmanager debugging enabled");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        mMenu = menu;
        getMenuInflater().inflate(R.menu.home_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.view_controllers) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            SelectControllerFrag selectControllerFrag = new SelectControllerFrag();
            fragmentTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
            fragmentTransaction.replace(R.id.homescreen_frag, selectControllerFrag, "selectControllerFrag");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            item.setEnabled(false);
            current_frag = SELECTCONTROLLERFRAG;
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void OnstartBluetoothFragInteraction(int action_code) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (action_code) {
            case StartBluetoothFrag.SCAN_BUTTON_PRESSED:
                Log.e(TAG, "Scan Button Pressed");
                DeviceListFrag deviceListFrag = new DeviceListFrag();
                fragmentTransaction.add(R.id.homescreen_frag, deviceListFrag, "deviceListFrag");
                fragmentTransaction.commit();
                break;
            case StartBluetoothFrag.DEVICE_CONNECTED:
                //Animate and switch to the select controller screen once a device is connected
                SelectControllerFrag selectControllerFrag = new SelectControllerFrag();
                fragmentTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
                fragmentTransaction.replace(R.id.homescreen_frag, selectControllerFrag, "selectControllerFrag");
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

                //Disable the "View Controllers" option in the menu since we are already switching to that screen
                mMenu.findItem(R.id.view_controllers).setEnabled(false);
                //set the current connection status so that any other fragment wishing to know the connection status can know
                synchronized(connection_status){
                    connection_status = true;
                }
                //set current_frag int so back button can be configured
                current_frag = SELECTCONTROLLERFRAG;
                break;
            case StartBluetoothFrag.RESTART_BLUETOOTH:
                //Removes whatever is in the homescreen fragment if Bluetooth is not connected onResume
                if (fragmentManager.findFragmentById(R.id.homescreen_frag) != null) {
                    fragmentTransaction.remove(fragmentManager.findFragmentById(R.id.homescreen_frag));
                    fragmentTransaction.commit();
                }
                break;
            case StartBluetoothFrag.NOT_CONNECTED:
                Log.e(TAG, "connection set to not connected");
                synchronized(connection_status){
                    connection_status = false;
                }
                if(fragmentManager.findFragmentById(R.id.homescreen_frag) != null) {
                    String current_frag_tag = fragmentManager.findFragmentById(R.id.homescreen_frag).getTag();
                    Log.e(TAG, "let's see what the current fragment is");
                    if(current_frag_tag.equals("buttonControllerFrag")){
                        //Communicates to the buttonControllerFrag that we are not connected
                        ButtonControllerFrag buttonControllerFrag = (ButtonControllerFrag) fragmentManager.findFragmentById(R.id.homescreen_frag);
                        buttonControllerFrag.change_status(connection_status);
                    }
                }
                break;
        }
    }

    public void OnListPressed(int result_code, String mac_address) {
        Log.e(TAG, "device selected to connect");
        StartBluetoothFrag startBluetoothFrag = (StartBluetoothFrag) fragmentManager.findFragmentById(R.id.start_bluetooth_frag);
        startBluetoothFrag.connect_device(result_code, mac_address);
    }

    public void selectFrag(int controller_code) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (controller_code) {
            case (SelectControllerFrag.BT_CHAT):
                BtChatFrag btChatFrag = new BtChatFrag();
                fragmentTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
                fragmentTransaction.replace(R.id.homescreen_frag, btChatFrag, "btChatFrag");
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                current_frag = BTCHAT_SELECTED;
                break;
            case (SelectControllerFrag.BUTTON_CONTROL):
                ButtonControllerFrag buttonControllerFrag = new ButtonControllerFrag();
                fragmentTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
                fragmentTransaction.replace(R.id.homescreen_frag, buttonControllerFrag, "buttonControllerFrag");
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                //Sets the current connection status in buttonControllerFrag to reflect the connection status
                //of bluetooth
                Log.e(TAG, "set connection_status for buttonControllerFrag" + connection_status.toString());
                buttonControllerFrag.change_status(connection_status);
                current_frag = CONTROLLER_SELECTED;
                break;
            case (SelectControllerFrag.SLIDER_CONTROL):
                SliderControllerFrag sliderControllerFrag = new SliderControllerFrag();
                fragmentTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
                fragmentTransaction.replace(R.id.homescreen_frag, sliderControllerFrag, "sliderControllerFrag");
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                current_frag = CONTROLLER_SELECTED;
                break;
            case (SelectControllerFrag.MOTION_CONTROL):
                MotionControllerFrag motionControllerFrag = new MotionControllerFrag();
                fragmentTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
                fragmentTransaction.replace(R.id.homescreen_frag, motionControllerFrag, "motionControllerFrag");
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                current_frag = CONTROLLER_SELECTED;
                break;
        }
    }

    @Override
    public void onBackPressed() {
        switch (current_frag) {
            case SELECTCONTROLLERFRAG:
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    current_frag = NULL;
                    fragmentManager.popBackStack();
                    mMenu.findItem(R.id.view_controllers).setEnabled(true);
                }
                return;
            case CONTROLLER_SELECTED:
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    current_frag = NULL;
                    fragmentManager.popBackStack();
                    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    return;
                }
            case BTCHAT_SELECTED:
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    current_frag = NULL;
                    fragmentManager.popBackStack();
                    return;
                }
        }
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    public boolean onBtChatInteraction(String message) {
        //Allows bluetooth chat to tell the startBluetoothFrag what message it wishes to send
        StartBluetoothFrag startBluetoothFrag = (StartBluetoothFrag) fragmentManager.findFragmentById(R.id.start_bluetooth_frag);
        return startBluetoothFrag.sendMessage(message);
    }

    public void startBluetoothFrag_MessageReceived(String device_name, String received_message) {
        BtChatFrag btChatFrag = (BtChatFrag) fragmentManager.findFragmentById(R.id.homescreen_frag);
        btChatFrag.receive_message(device_name, received_message);
    }

    public void onButtonControllerInteraction(byte message) {
        //Allows button controller to tell the startBluetoothFrag what message it wishes to send
        StartBluetoothFrag startBluetoothFrag = (StartBluetoothFrag) fragmentManager.findFragmentById(R.id.start_bluetooth_frag);
        startBluetoothFrag.sendMessage(message);
    }

    public void startBluetoothFrag_MakeToast(final String toast_text){
        this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplication(), toast_text, Toast.LENGTH_SHORT).show();
            }
        });

    }
}
