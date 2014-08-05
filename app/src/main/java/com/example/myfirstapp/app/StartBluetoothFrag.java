/**
 * Created by tommy on 7/24/14.
 */

package com.example.myfirstapp.app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class StartBluetoothFrag extends Fragment {
    //Debugging
    private static final String TAG = "StartBluetoothFrag";
    private static boolean D = false;

    // Message types sent from the ChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_CONNECTION_LOST = 6;

    //Action Codes for onStartBluetoothFragInteraction
    public static final int SCAN_BUTTON_PRESSED = 1;
    public static final int DEVICE_CONNECTED = 2;
    public static final int RESTART_BLUETOOTH = 3;
    public static final int NOT_CONNECTED = 4;

    // Key names received from the ChatService Handler for key to bundle data
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Bluetooth Chat service that handles all of our bluetooth connections
    private ChatService mChatService = null;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    // Name of the connected device
    private String mConnectedDeviceName = null;

    //Button and auxiliary items
    private Button mStatusButton;
    private Button mScanButton;
    private Menu mMenu;

    //Listener that connects with the main activity
    private startBluetoothFragListener mListener;

    //Handler used to receive information from ChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            Log.e(TAG, "startbluetoothfrag handlemessage");
            //Obtain first part of message... designates nature of message
            switch (msg.what) {
                //If the message is that we have changed states, continue
                //to find out what state to change to
                case MESSAGE_STATE_CHANGE:
                    Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case ChatService.STATE_CONNECTED:
                            //Set up status button for connected status
                            mStatusButton.setText(R.string.title_connected_to);
                            mStatusButton.append(" ");
                            mStatusButton.append(mConnectedDeviceName);
                            mStatusButton.setClickable(true);
                            mStatusButton.setEnabled(true);
                            //Enable long press for disconnect
                            mStatusButton.setOnClickListener(null);
                            registerForContextMenu(mStatusButton);
                            //Lets the main activity know that we are now connected
                            mListener.OnstartBluetoothFragInteraction(DEVICE_CONNECTED);
                            break;
                        case ChatService.STATE_CONNECTING:
                            mStatusButton.setText(R.string.title_connecting);
                            break;
                        case ChatService.STATE_LISTEN:
                        case ChatService.STATE_NONE:
                            //Sets up the status bar to display connection status
                            if (mStatusButton.isClickable()) mStatusButton.setClickable(false);
                            mStatusButton.setEnabled(false);
                            mStatusButton.setText(R.string.title_not_connected);
                            //Let the main activity know that we are not connected
                            if (mListener != null) {
                                mListener.OnstartBluetoothFragInteraction(NOT_CONNECTED);
                            }
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    //messageReceived informs the main activity that a message has been received
                    //awaits further processing in homescreen_frag
                    messageReceived(readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    if (mListener != null) {
                        mListener.startBluetoothFrag_MakeToast("Connected to " + mConnectedDeviceName);
                    }
                    break;
                case MESSAGE_TOAST:
                    if (mListener != null) {
                        mListener.startBluetoothFrag_MakeToast(msg.getData().getString(TOAST));
                    }
                    break;
                case MESSAGE_CONNECTION_LOST:
                    mStatusButton.setEnabled(false);
                    unregisterForContextMenu(mStatusButton);
                    break;
            }
        }
    };

    public StartBluetoothFrag() {
        // Required empty public constructor
    }

    public static StartBluetoothFrag newInstance() {
        StartBluetoothFrag fragment = new StartBluetoothFrag();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Debugging
        if(D) Log.e(TAG, "onCreate");
        //Allows the fragment to place items in the activity options menu
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Debugging
        if(D) Log.e(TAG, "onCreateview");

        View view = inflater.inflate(R.layout.fragment_start_bluetooth, container, false);

        //Sets up listeners for the buttons
        mStatusButton = (Button) view.findViewById(R.id.button_status);
        mStatusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                start_bluetooth();
            }
        });

        mScanButton = (Button) view.findViewById(R.id.scan);
        //mScanButton always disappears after you click on it
        mScanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((HomeScreen) getActivity()).get_current_frag() == HomeScreen.NULL){
                    mScanButton.setVisibility(View.GONE);
                    scan();
                }else mListener.startBluetoothFrag_MakeToast(getResources().getString(R.string.not_currently_available));
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Debugging
        if(D) Log.e(TAG, "onResume");

        //Check bluetooth status each time on resume so there is a way of restarting
        //the bluetooth if it was turned off while away.
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Log.e(TAG, "mbluetoothadapter is not enabled");
                //set up buttons for starting bluetooth
                if (mStatusButton.getText().toString() != getString(R.string.start_bluetooth)) {
                    mStatusButton.setEnabled(true);
                    if (!mStatusButton.isClickable()) mStatusButton.setClickable(true);
                    if (!mStatusButton.hasOnClickListeners()) {
                        mStatusButton.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                start_bluetooth();
                            }
                        });
                    }
                    mStatusButton.setText(R.string.start_bluetooth);
                    //remove the 'scan for devices' button if present
                    mScanButton.setVisibility(View.GONE);
                    //removes the 'ensure discoverable' option from the
                    //options menu as that should be only available with bluetooth on
                    mMenu.findItem(R.id.discoverable).setVisible(false);

                    //unregister status button for long press leftover form
                    //capabilities when there is a connected device
                    unregisterForContextMenu(mStatusButton);

                    //lets the main activity know that we have to restart bluetooth
                    mListener.OnstartBluetoothFragInteraction(RESTART_BLUETOOTH);
                }
            }
        }
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            // Should evaluate to true at startup because ChatService constructor sets state to STATE_NONE
            if (mChatService.getState() == ChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                Log.e(TAG, "starting ChatService()");
                mChatService.start();
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (startBluetoothFragListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement startBluetoothFrag");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mMenu = menu;
        inflater.inflate(R.menu.start_bluetooth_menu, menu);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean bluetooth_on_startup = sharedPref.getBoolean("pref_bluetooth_on_startup", true);
        Log.e(TAG, "onCreateOptionsMenu");
        if(bluetooth_on_startup){
            start_bluetooth();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.discoverable:
                ensureDiscoverable();
                return true;
        }
        return false;
    }

    //Sets up the context menu for long presses
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Action");
        menu.add(0, v.getId(), 0, R.string.disconnect);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle() == getString(R.string.disconnect)) {
            unregisterForContextMenu(mStatusButton);
            Log.e(TAG, "disconnect option");
            mChatService.disconnect();
            Log.e(TAG, "disconnect completed");
            unregisterForContextMenu(mStatusButton);
            mStatusButton.setEnabled(false);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Stop the Bluetooth chat services when the activity stops
        if (mChatService != null) mChatService.stop();
        mListener = null;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            //Bluetooth is supported but not enabled
            case REQUEST_ENABLE_BT:
                // Proceed if the user agreed to turn on bluetooth
                if (resultCode == Activity.RESULT_OK) {
                    //Configure buttons and menu items
                    mScanButton.setVisibility(View.VISIBLE);
                    mMenu.findItem(R.id.discoverable).setVisible(true);
                    // Bluetooth is now enabled, so set up a chat session
                    setupService();
                } else {
                    // User did not enable Bluetooth or an error occured
                    mListener.startBluetoothFrag_MakeToast(getResources().getString(R.string.bt_not_enabled_leaving));
                }
                break;
        }
    }

    //Starts the activities to establish bluetooth connection
    private void start_bluetooth() {
        //Debugging
        Log.e(TAG, "start_bluetooth");

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            if(mListener != null){
                mListener.startBluetoothFrag_MakeToast(getResources().getString(R.string.bluetooth_not_available));
            }
            return;
        }
        //Check to make sure bluetooth is turned on
        if (!mBluetoothAdapter.isEnabled()) {
            //Initiate activity to request permission to turn on bluetooth if it is not turned on
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the session
        } else {
            //Set up chatService to establish bluetooth connection
            if (mChatService == null) {
                //Configure buttons and menu items
                mScanButton.setVisibility(View.VISIBLE);
                mMenu.findItem(R.id.discoverable).setVisible(true);
                //Defines & instantiates the Chat Service
                setupService();
            }
        }

        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            // Should evaluate to true at startup because ChatService constructor sets state to STATE_NONE
            if (mChatService.getState() == ChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    private void setupService() {
        if(D) Log.d(TAG, "setupService()");
        // Initialize the ChatService to perform bluetooth connections
        mChatService = new ChatService(getActivity(), mHandler);
    }

    //Lets the main activity know that we want to show the device list
    public void scan() {
        Log.e(TAG, "Scan Button Pressed");
        if (mListener != null) {
            mListener.OnstartBluetoothFragInteraction(SCAN_BUTTON_PRESSED);
        }
    }

    //Communicates to the chatservice that the user has selected a device
    //to connect to
    public void connect_device(int resultCode, String mac_address) {
        if (resultCode == Activity.RESULT_OK) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mac_address);
            Log.e(TAG, "created device with remote device mac address");
            mChatService.connect(device);
        }
    }

    private void ensureDiscoverable() {
        Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    //Send a String message through bluetooth
    public boolean sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != ChatService.STATE_CONNECTED) {
            if(mListener != null){
                mListener.startBluetoothFrag_MakeToast(getString(R.string.not_connected));
            }
            return false;
        }
        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
            return true;
        }
        return false;
    }

    //Overloaded method: send a byte through bluetooth
    public boolean sendMessage(byte message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != ChatService.STATE_CONNECTED) {
            if(mListener != null){
                mListener.startBluetoothFrag_MakeToast(getString(R.string.not_connected));
            }
            return false;
        }
        // Get the message bytes and tell the BluetoothChatService to write
        mChatService.write(message);
        return true;
    }

    //Lets the main activity know that we have received a message through bluetooth
    private void messageReceived(String received_message) {
        if (mListener != null) {
            mListener.startBluetoothFrag_MessageReceived(mConnectedDeviceName, received_message);
        }
    }

    //Allows for the status button to be scaled in controller modes so it doesn't get in the way
    public void scale_status_button(float X, float Y){
        mStatusButton.setScaleX(X);
        mStatusButton.setScaleY(Y);
    }

    //Interface that must be implemented by the HomeScreen activity to facilitate communication
    //between this fragment and main activity/other fragments
    public interface startBluetoothFragListener {
        public void OnstartBluetoothFragInteraction(int action_code);
        public void startBluetoothFrag_MessageReceived(String device_name, String received_message);
        public void startBluetoothFrag_MakeToast(String toast_text);
    }
}
