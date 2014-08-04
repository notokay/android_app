package com.example.myfirstapp.app;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.widget.Toast;

import com.example.myfirstapp.app.R;

public class HomeScreen extends FragmentActivity implements StartBluetoothFrag.startBluetoothFragListener, DeviceListFrag.OnListPressedListener, SelectControllerFrag.SelectControllerFragListener, BtChatFrag.OnBtChatInteractionListener, ButtonControllerFrag.OnButtonControllerInteractionListener, SliderControllerFrag.OnSliderControllerInteractionListener, MotionControllerFrag.OnMotionControllerInteractionListener {
    //Debugging
    private static final String TAG = "HomeScreen";
    //Possible values for current_frag
    private static final int NULL = 0;
    private static final int STARTBLUETOOTHFRAG = 1;
    private static final int SELECTCONTROLLERFRAG = 2;
    private static final int DEVICELISTFRAG = 5;
    private static final int CONTROLLER_SELECTED = 3;
    private static final int BTCHAT_SELECTED = 4;
    //defines the fragment manager, which is used to manage the fragments in this activity
    private final FragmentManager fragmentManager = getFragmentManager();
    //variables that help communicate what the current fragment
    //in homescreen_frag is
    private int current_frag;
    private StartBluetoothFrag startBluetoothFrag;

    //boolean defining whether we are currently connected to a bluetooth device
    private Boolean connection_status = false;

    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
          //For debugging
//        Log.e(TAG, "fragmentmanager debugging enabled");
//        fragmentManager.enableDebugLogging(true);

        //sets up what is to be displayed in this activity
        setContentView(R.layout.activity_home_screen);
        startBluetoothFrag = (StartBluetoothFrag) fragmentManager.findFragmentById(R.id.start_bluetooth_frag);
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
            //Allows the user to jump to view the controllers without having established a
            //connection by switching the current fragment to selectControllerFrag
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            SelectControllerFrag selectControllerFrag = new SelectControllerFrag();
            fragmentTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
            fragmentTransaction.replace(R.id.homescreen_frag, selectControllerFrag, "selectControllerFrag");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            //Disables the view controllers menu option if we are currently in it already
            item.setEnabled(false);
            //sets the current frag
            current_frag = SELECTCONTROLLERFRAG;
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Method that is called by startBluetoothFrag when something happens
    public void OnstartBluetoothFragInteraction(int action_code) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (action_code) {
            //Called when the "scan for devices" option is selected
            case StartBluetoothFrag.SCAN_BUTTON_PRESSED:
                Log.e(TAG, "Scan Button Pressed");
                //Starts and shows the process that detects bluetooth devices
                //that are currently discoverable
                DeviceListFrag deviceListFrag = new DeviceListFrag();
                fragmentTransaction.add(R.id.homescreen_frag, deviceListFrag, "deviceListFrag");
                fragmentTransaction.commit();
                //sets the current_frag int so that back button can be configured
                current_frag = DEVICELISTFRAG;
                break;
            //Called when a bluetooth device is now connected
            case StartBluetoothFrag.DEVICE_CONNECTED:
                //Starts and shows the process that allows the user to
                //select the desired controller
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
            //Called if we ever need to restart bluetooth detection
            //from the very beginning after the first time we start
            //the app
            case StartBluetoothFrag.RESTART_BLUETOOTH:
                //Removes whatever is in the homescreen fragment if Bluetooth is not connected onResume
                if (fragmentManager.findFragmentById(R.id.homescreen_frag) != null) {
                    fragmentTransaction.remove(fragmentManager.findFragmentById(R.id.homescreen_frag));
                    fragmentTransaction.commit();
                }
                break;
            //Called whenever we transition from a state to the 'not connected' state
            case StartBluetoothFrag.NOT_CONNECTED:
                //Sets the connection_status boolean so other fragments can know
                //about the current connection status and act accordingly
                synchronized(connection_status){
                    connection_status = false;
                }
                //specific instructions depending on what the current fragment is
                if(fragmentManager.findFragmentById(R.id.homescreen_frag) != null) {
                    //get the tag attached to a fragment whenever we added it or replaced it
                    String current_frag_tag = fragmentManager.findFragmentById(R.id.homescreen_frag).getTag();
                    //if the current fragment is the buttonControllerFrag, we want to stop the sending of any
                    //bits and bytes since in the button controller there is an automatic null message sent
                    if(current_frag_tag.equals("buttonControllerFrag")){
                        //Communicates to the buttonControllerFrag that we are not connected
                        ButtonControllerFrag buttonControllerFrag = (ButtonControllerFrag) fragmentManager.findFragmentById(R.id.homescreen_frag);
                        buttonControllerFrag.change_status(connection_status);
                        return;
                    } else if(current_frag_tag.equals("sliderControllerFrag")){
                        SliderControllerFrag sliderControllerFrag = (SliderControllerFrag) fragmentManager.findFragmentById(R.id.homescreen_frag);
                        sliderControllerFrag.change_status(connection_status);
                        return;
                    } else if(current_frag_tag.equals("motionControllerFrag")){
                        MotionControllerFrag motionControllerFrag = (MotionControllerFrag) fragmentManager.findFragmentById(R.id.homescreen_frag);
                        motionControllerFrag.change_conncetion_status(connection_status);
                    }
               }
                break;
        }
    }

    //Method called by the Device List that communicates the device we desire
    //to connect to, which we then communicate to the startBluetoothFrag
    //who then initiates the connection
    public void OnListPressed(int result_code, String mac_address) {
        Log.e(TAG, "device selected to connect");
        startBluetoothFrag.connect_device(result_code, mac_address);
    }

    //method called by the selectcontroller frag once the user has decided
    //which controller to proceed to
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

                startBluetoothFrag.scale_status_button((float)0.5, (float)0.5);
                current_frag = CONTROLLER_SELECTED;
                break;
            case (SelectControllerFrag.SLIDER_CONTROL):
                SliderControllerFrag sliderControllerFrag = new SliderControllerFrag();
                fragmentTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
                fragmentTransaction.replace(R.id.homescreen_frag, sliderControllerFrag, "sliderControllerFrag");
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                sliderControllerFrag.change_status(connection_status);
                startBluetoothFrag.scale_status_button((float)0.5, (float)0.5);
                current_frag = CONTROLLER_SELECTED;
                break;
            case (SelectControllerFrag.MOTION_CONTROL):
                MotionControllerFrag motionControllerFrag = new MotionControllerFrag();
                fragmentTransaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);
                fragmentTransaction.replace(R.id.homescreen_frag, motionControllerFrag, "motionControllerFrag");
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                motionControllerFrag.change_conncetion_status(connection_status);
                startBluetoothFrag.scale_status_button((float)0.5, (float)0.5);
                current_frag = CONTROLLER_SELECTED;
                break;
        }
    }

    //Method that defines what to do when the back button is pressed on the screen
    //Necessary because at the time of this writing the default action is to quit the
    //application instead of popBackStack
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
                    startBluetoothFrag.scale_status_button((float)1.0, (float)1.0);
                    //most of the controllers have landscape orientation so when returning
                    //to the select controller screen we want to return the screen orientation
                    //to portrait
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
        return startBluetoothFrag.sendMessage(message);
    }

    public void startBluetoothFrag_MessageReceived(String device_name, String received_message) {
        //since messages for bluetooth chat could come in at any time, the startBluetoothFrag uses
        //this method to let the BtChatFrag know whenever messages come in so the received messages
        //can be displayed
        BtChatFrag btChatFrag = (BtChatFrag) fragmentManager.findFragmentById(R.id.homescreen_frag);
        btChatFrag.receive_message(device_name, received_message);
    }

    //Method called by the Button Controller to send a command through bluetooth to the connected device
    //through startBluetoothFrag
    public void onButtonControllerInteraction(byte message) {
        startBluetoothFrag.sendMessage(message);
    }

    public void onSliderControllerInteraction(String message){
        startBluetoothFrag.sendMessage(message);
    }

    //method that is called whenever the startBluetoothFrag wants to make a toast
    //ensures that the toast is made on the UI thread to prevent the app from crashing
    public void startBluetoothFrag_MakeToast(final String toast_text){
        this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplication(), toast_text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onMotionControllerInteraction(String message){
        startBluetoothFrag.sendMessage(message);
    }
    public void motionControllerFrag_make_toast(final String toast_text){
        this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplication(), toast_text, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
