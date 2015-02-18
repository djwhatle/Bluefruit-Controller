package com.derekwhatley.bluefruitcontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class TubeControl extends Activity {
    private SeekBar mRedSeek;
    private SeekBar mGreenSeek;
    private SeekBar mBlueSeek;
    private SeekBar mBrightSeek;
    private TextView mTxtRedVal;
    private TextView mTxtGreenVal;
    private TextView mTxtBlueVal;
    private TextView mTxtMAC;
    private TextView mTxtFFTData;
    private ToggleButton mBluetoothToggle;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;

    //callbacks
    final int REQUEST_ENABLE_BT = 1;

    //constants
    final byte MODE_BRIGHT = 0;
    final byte MODE_RED = 1;
    final byte MODE_GREEN = 2;
    final byte MODE_BLUE = 3;

    final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tube_control);

        mRedSeek = (SeekBar) findViewById(R.id.seekRedVal);
        mGreenSeek = (SeekBar) findViewById(R.id.seekGreenVal);
        mBlueSeek = (SeekBar) findViewById(R.id.seekBlueVal);
        mBrightSeek = (SeekBar) findViewById(R.id.seekBrightVal);
        mTxtRedVal = (TextView) findViewById(R.id.txtRedVal);
        mTxtGreenVal = (TextView) findViewById(R.id.txtGreenVal);
        mTxtBlueVal = (TextView) findViewById(R.id.txtBlueVal);
        mTxtMAC = (TextView) findViewById(R.id.txtMAC);
        mBluetoothToggle = (ToggleButton) findViewById(R.id.btnBluetoothToggle);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mTxtFFTData = (TextView) findViewById(R.id.txtFFTData);


        if(mBluetoothSocket != null) {
            if(mBluetoothAdapter.isEnabled()) {
                setMACaddress();
                mBluetoothToggle.setChecked(true);
            }
        }

        mBluetoothToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                if(mBluetoothToggle.isChecked()) {
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                    else {
                        mBluetoothToggle.setEnabled(false);
                        Toast.makeText(getBaseContext(), "Connecting", Toast.LENGTH_SHORT).show();
                        setMACaddress();
                    }
                }
                else {
                    try {
                        mBluetoothToggle.setEnabled(false);
                        mBluetoothSocket.close();
                    }
                    catch (IOException e) {
                        //not much to be done
                        Toast.makeText(getBaseContext(), "Failed to close connection.", Toast.LENGTH_SHORT).show();
                        //don't change the button state if we failed to connect
                        mBluetoothToggle.setChecked(true);
                    }
                    mBluetoothToggle.setEnabled(true);
                }
            }
        });

        mBrightSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(mBluetoothSocket != null) {
                    byte data[] = {MODE_BRIGHT, (byte) seekBar.getProgress()};
                    sendBTData(data);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mRedSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mTxtRedVal.setText(Integer.toString(i));
                if(mBluetoothSocket != null) {
                    byte data[] = {MODE_RED, (byte) seekBar.getProgress()};
                    sendBTData(data);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                 //not needed
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //not needed
            }
        });

        mGreenSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mTxtGreenVal.setText(Integer.toString(i));
                if(mBluetoothSocket != null) {
                    byte data[] = {MODE_GREEN, (byte) seekBar.getProgress()};
                    sendBTData(data);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //not needed
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //not needed
            }
        });

        mBlueSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mTxtBlueVal.setText(Integer.toString(i));
                if(mBluetoothSocket != null) {
                    byte data[] = {MODE_BLUE, (byte) seekBar.getProgress()};
                    sendBTData(data);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //not needed
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //not needed
            }
        });

    }

    private void sendBTData(byte[] data) {

        if(mBluetoothSocket != null) {
            if(mBluetoothSocket.isConnected()) {
                try {
                    OutputStream btOut = mBluetoothSocket.getOutputStream();
                    btOut.write(data);
                }
                catch (IOException e) {
                    //not much to do here
                    System.out.println("failed to send data :: " + e.getMessage());
                }
            }
        }
    }

    private void setMACaddress() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                String name = device.getName();
                if(device.getName().contains("022f")) {
                    mTxtMAC.setText(device.getAddress());
                    try {
                        mBluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
                    }
                    catch (IOException e) {
                        Toast.makeText(getBaseContext(), "Failed to create service record", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        mBluetoothSocket.connect();
                    }
                    catch (IOException e) {
                        Toast.makeText(getBaseContext(), "Failed to connect to device", Toast.LENGTH_SHORT).show();
                        mBluetoothToggle.setChecked(false);
                    }
                    mBluetoothToggle.setEnabled(true);
                }
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_ENABLE_BT) {
            if(resultCode == RESULT_OK){
                setMACaddress();
                Toast.makeText(getBaseContext(), "Bluetooth enabled", Toast.LENGTH_SHORT).show();
                mBluetoothToggle.setEnabled(true);
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getBaseContext(), "Bluetooth enable failed", Toast.LENGTH_SHORT).show();
                mBluetoothToggle.setEnabled(true);
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tube_control, menu);
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
        return super.onOptionsItemSelected(item);
    }
}
