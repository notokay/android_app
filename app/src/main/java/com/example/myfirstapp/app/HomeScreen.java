package com.example.myfirstapp.app;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothClass;
import android.content.Intent;
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

import com.example.myfirstapp.app.R;

public class HomeScreen extends FragmentActivity implements StartBluetoothFrag.OnScanPressedListener, DeviceListFrag.OnListPressedListener {
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
    private static final String TAG = "HomeScreen";
    private final FragmentManager fragmentManager = getFragmentManager();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void launch_bt_chat(View view){
        Intent btchatIntent = new Intent(this, MainActivity.class);
        startActivity(btchatIntent);
    }

    public void launch_arrow_control(View view){
        Intent arrowIntent = new Intent(this, ArrowControl.class);
        startActivity(arrowIntent);
    }

    public void OnScanPressed() {
        Log.e(TAG, "Scan Button Pressed");
        DeviceListFrag deviceListFrag = new DeviceListFrag();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.homescreen_frag, deviceListFrag);
        fragmentTransaction.commit();
    }

    public void OnListPressed(int result_code, String mac_address) {
        StartBluetoothFrag startBluetoothFrag = (StartBluetoothFrag) fragmentManager.findFragmentById(R.id.homescreen_frag);
        startBluetoothFrag.connect_device(result_code, mac_address);
    }
}
