package com.example.myfirstapp.app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class StartBluetoothFrag extends Fragment {
    // Message types sent from the ChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the ChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    //Debugging
    private static final String TAG = "StartBluetoothFrag";
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // Member object for the chat services
    private ChatService mChatService = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Name of the connected device
    private String mConnectedDeviceName = null;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case ChatService.STATE_CONNECTED:
                            status_button.setText(R.string.title_connected_to);
                            status_button.append(" ");
                            status_button.append(mConnectedDeviceName);
                            break;
                        case ChatService.STATE_CONNECTING:
                            status_button.setText(R.string.title_connecting);
                            break;
                        case ChatService.STATE_LISTEN:
                        case ChatService.STATE_NONE:
                            status_button.setText(R.string.title_not_connected);
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
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getActivity(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getActivity(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private Button status_button;
    private Button scan_button;
    private Menu mMenu;
    private String mParam1;
    private String mParam2;

    private OnScanPressedListener mScanListener;

    public StartBluetoothFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StartBluetoothFrag.
     */
    public static StartBluetoothFrag newInstance(String param1, String param2) {
        StartBluetoothFrag fragment = new StartBluetoothFrag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_bluetooth, container, false);

        status_button = (Button) view.findViewById(R.id.button_status);
        status_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                start_bluetooth();
            }
        });
        scan_button = (Button) view.findViewById(R.id.scan);
        scan_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                scan_button.setVisibility(View.GONE);
                scan();
            }
        });
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                if (status_button.getText().toString() != getString(R.string.start_bluetooth)) {
                    status_button.setEnabled(true);
                    status_button.setClickable(true);
                    status_button.setText(R.string.start_bluetooth);
                    scan_button.setVisibility(View.GONE);
                    mMenu.findItem(R.id.discoverable).setVisible(false);
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

    public void scan() {
        Log.e(TAG, "Scan Button Pressed");
        if (mScanListener != null) {
            mScanListener.OnScanPressed();
        }
/**        if (resultCode == Activity.RESULT_OK) {
 // Get the device MAC address
 String address = data.getExtras()
 .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
 // Get the BluetoothDevice object
 BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
 // Attempt to connect to the device
 mChatService.connect(device);
 }**/
    }

    public void connect_device(int resultCode, String mac_address) {
        if (resultCode == Activity.RESULT_OK) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mac_address);
            mChatService.connect(device);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mScanListener = (OnScanPressedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnScanPressedListener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mMenu = menu;
        inflater.inflate(R.menu.start_bluetooth_menu, menu);
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

    @Override
    public void onDetach() {
        super.onDetach();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();

        mScanListener = null;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Log.e(TAG, "Bluetooth now enabled, calling setupService()");
                    // Bluetooth is now enabled, so set up a chat session
                    setupService();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void setupService() {
        Log.d(TAG, "setupService()");

        // Initialize the ChatService to perform bluetooth connections
        Log.e(TAG, "Initalizing ChatService");
        mChatService = new ChatService(getActivity(), mHandler);

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

    //Buttons
    private void start_bluetooth() {
        status_button.setClickable(false);
        status_button.setText(R.string.connection_status);
        status_button.setEnabled(true);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.e(TAG, "StartBluetooth Button Pressed");
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
            return;
        }
        Log.e(TAG, "Checked for bluetooth availability");
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the session
        } else {
            if (mChatService == null) {
                Log.e(TAG, "mChatService is null");
                scan_button.setVisibility(View.VISIBLE);
                Log.e(TAG, "set scan button to visible");
                mMenu.findItem(R.id.discoverable).setVisible(true);
                Log.e(TAG, "set discoverable option to visible");
                setupService();
            }
        }
        Log.e(TAG, "Checked if Bluetooth is enabled");

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
    public interface OnScanPressedListener {
        public void OnScanPressed();

    }


}
