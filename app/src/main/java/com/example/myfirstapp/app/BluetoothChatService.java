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
    //Constants that indicate the current connection state
    public static final int STATE_NONE = 0;         //nothing happening
    public static final int STATE_LISTEN = 1;       //now listening for incoming connections
    public static final int STATE_CONNECTING = 2;   //now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;    //now connected to a remote device
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

    //Constructor - prepares a new bluetoothChat session
    //context - the ui activity context
    //handler - handler to send messages back to ui activity in RemoteBluetooth class

    public BluetoothChatService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

    public synchronized int getState(){ //returns the current state of the connection
        return mState;
    }

    //private member function that sets current state of the chat connection
    //state - defines the current connection
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + "->" + state);
        // prints out current state by reading from member variable mState, then prints out what we are transitioning to
        mState = state; //changes the state
        mHandler.obtainMessage(RemoteBluetooth.MESSAGE_STATE_CHANGE, state, -1).sendToTarget(); //sends the message to the handler

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

    //start the ConnectThread to initiate a connection to a remote device
    //takes the parameter of which device to connect to
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        //Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        //Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        //Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectedThread.start();
        setState(STATE_CONNECTING);
    }

    //Start the ConnectedThread to begin managing a Bluetooth Connection
    //takes in socket parameter of which BluetoothSocket on which the connection was made
    //takes in a device parameter of which BluetoothDevice has been connected
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");

        //Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        //Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        //Cancel the AcceptThread because we only want to connect to one device
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        //Start the connection to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        //Send the name of the connected device back to the UI activity
        Message msg = mHandler.obtainMessage(RemoteBluetooth.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(RemoteBluetooth.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    //Stop all threads
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    //write to the ConnectedThread in an unsynchronized manner
    //out parameter is the bytes to write
    //used by ConnectedThread(byte[])
    public void write(byte[] out) {
        //Create temporary object
        ConnectedThread r;
        //Synchronize a copy of this class
        synchronized (this) {
            if (mState != STATE_CONNECTED)
                return; //don't do anything if we're not connected to a device
            r = mConnectedThread;
        }
        //perform the write unsynchronized
        r.write(out);
    }

    //indicate that the connection failed and notify the UI activity
    private void connectionFailed() {
        setState(STATE_LISTEN);

        //Send a fail message back to the main UI activity
        Message msg = mHandler.obtainMessage(RemoteBluetooth.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(RemoteBluetooth.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    //Indicate that the connection was lost and notify the UI activity
    private void connectionLost() {
        setState(STATE_LISTEN);

        //Send a fail message back to the main UI activity
        Message msg = mHandler.obtainMessage(RemoteBluetooth.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(RemoteBluetooth.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */

    private class AcceptThread extends Thread {
        //The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() { // constructor
            BluetoothServerSocket tmp = null;

            //Create a new server listening socket
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            if (D) Log.d(TAG, "BEGIN mAcceptThread" + this);

            setName("AcceptThread");
            BluetoothSocket socket = null;

            //Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    //this is a blocking call, and will only return on a
                    //successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }

                //If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothChatService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                //Situation is normal, start the connected thread
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                //either not ready or already connected, so terminate the new socket
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "END mAcceptThread"); //
        }

        public void cancel() {
            if (D) Log.d(TAG, "cancel" + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of socket failed", e);
            }
        }
    }

    //runs while attempting to make an outgoing connection
    //with a device. runs straight through; connection either
    //succeeds or fails
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            //Get a bluetoothsocket for a connection with the
            // given bluetoothdevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            //Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            //Make a connection to the BluetoothSocket
            try {
                //This is a blocking call and will only return on a
                //successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                //close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                //Start the service over to to restart listening mode
                BluetoothChatService.this.start();
                return;
            }

            //Reset the ConnectThread because we're done
            synchronized (BluetoothChatService.this) {
                mConnectedThread = null;
            }
            //Start the connected thread;
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    //Runs during a connection with a remote device
    //handles all incoming and outgoing transmissions

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "created ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //Get the bluetoothsocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            //Keep listening to the inputstream while connected
            //i.e. keep listening for incoming messages
            while (true) {
                try {
                    //Read from the input stream
                    bytes = mmInStream.read(buffer);

                    //Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(RemoteBluetooth.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        //write to the connected Outstream
        //buffer is the bytes to write
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                //Share the sent message back to the UI Activity
                mHandler.obtainMessage(RemoteBluetooth.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e){
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel(){
            try{
                mmSocket.close();
            } catch(IOException e){
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}