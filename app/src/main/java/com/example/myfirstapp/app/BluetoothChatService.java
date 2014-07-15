package com.example.myfirstapp.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

// Sets up and manages Bluetooth connections with other devices.
// Has threads for: listening for incoming connections, connecting with a device,
// performing data transmissions when connected.

public class BluetoothChatService {
    //Debugging
    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;

    //Name for Service Discovery Protocol (SDP) record when creating server socket
    //identifiable name of your device, which the system will write to the record
    private static final String NAME = "BluetoothChat";

    //Unique UUID for this application
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    //Member Fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    //Constants that indicate the current connection state
    public static final int STATE_NONE = 0;         //nothing happening
    public static final int STATE_LISTEN = 1;       //now listening for incoming connections
    public static final int STATE_CONNECTING = 2;   //now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;    //now connected to a remote device

    //Constructor - prepares a new bluetoothChat session
    //context - the ui activity context
    //handler - handler to send messages back to ui activity in RemoteBluetooth class

    public BluetoothChatService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

    //private member function that sets current state of the chat connection
    //state - defines the current connection
    private synchronized void setState(int state){
        if(D) Log.d(TAG, "setState() " + mState + "->" + state);
        // prints out current state by reading from member variable mState, then prints out what we are transitioning to
        mState = state; //changes the state
        mHandler.obtainMessage(RemoteBluetooth.MESSAGE_STATE_CHANGE, state, -1).sendToTarget(); //sends the message to the handler

    }

    public synchronized int getState(){ //returns the current state of the connection
        return mState;
    }

    //Start the chat service... start AcceptThread to begin a session in listening (server) mode
    //Called by Activity onResume()
    public synchronized void start() {
        if (D) Log.d(TAG, "Start");

        //Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectedThread = null;
        }

        //Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        //Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread(); //constructor
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }



}