package com.example.jaegeraegerpc.lab2;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static android.app.PendingIntent.getActivity;

public class MainActivity extends AppCompatActivity {

    ToggleButton toggleButton, toggleButton2, toggleButton3, toggleButton4;
    TextView textView;
    Button btnPaired, btnPairedNew;
    ListView devicelist, devicelistNew;
    String address=null;
    ArrayList listOld;
    ArrayList listNew;
    private ProgressDialog progress;
    private BluetoothAdapter mBluetoothAdapter = null;
    private boolean isBtConnected = false;
    BluetoothSocket btSocket = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        btnPaired = (Button)findViewById(R.id.button);
        btnPairedNew = (Button)findViewById(R.id.button2);
        devicelist = (ListView)findViewById(R.id.listView);
        devicelistNew = (ListView)findViewById(R.id.listViewNew);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        textView = (TextView)findViewById(R.id.text2);
        textView.setVisibility(View.GONE);

        listOld = new ArrayList();
        listNew = new ArrayList();
        //listNew.add("Test-05"+ "\n" + "21:23:23:23:23:23");



        if(mBluetoothAdapter == null) //no bluetooth adapter
        {
            Toast.makeText(getApplicationContext(), "No Bluetooth Device", Toast.LENGTH_LONG).show();
            finish();
        }
        else if(!mBluetoothAdapter.isEnabled())
        {
            //Bluetooth on
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,1);
        }

        //show yourself!
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        btnPaired.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                pairedDevicesList();
            }
        });
        btnPairedNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                displayListOfFoundDevices();
            }
        });

        toggleButton = (ToggleButton)findViewById(R.id.toggleButt);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    turnOnLed();
                } else {
                    turnOffLed();
                }
            }
        });

        toggleButton2 = (ToggleButton)findViewById(R.id.toggleButt3);
        toggleButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    asyncOn();
                } else {
                    asyncOff();
                }
            }
        });
        toggleButton3 = (ToggleButton)findViewById(R.id.toggleButt2);
        toggleButton3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    blinkOn();
                } else {
                    blinkOff();
                }
            }
        });

        toggleButton4 = (ToggleButton)findViewById(R.id.toggleButt4);
        toggleButton4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    soundOn();
                    textView.setVisibility(View.VISIBLE);
                } else {
                    soundOff();
                    textView.setVisibility(View.GONE);
                }
            }
        });
    }


    private void displayListOfFoundDevices()
    {

        //Toast.makeText(getApplicationContext(), "1", Toast.LENGTH_LONG).show();
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        // start looking for bluetooth devices
        mBluetoothAdapter.startDiscovery();
        }

        // Discover new devices
        private final BroadcastReceiver mReceiver = new BroadcastReceiver()
        {

            @Override
            public void onReceive(Context context, Intent intent)
            {
                //Toast.makeText(getApplicationContext(), "2", Toast.LENGTH_LONG).show();
                counter++;
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    //Toast.makeText(getApplicationContext(), "3 in if", Toast.LENGTH_LONG).show();
                    // Get the bluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    listNew.add(deviceName + "\n" + deviceHardwareAddress);
                    //listNew.add(counter + "\n" + "21:23:23:23:23:23");
                    final ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, listNew);

                    devicelistNew.setAdapter(adapter);
                    devicelistNew.setOnItemClickListener(myListClickListener); //Method called when the device from the listOld is clicked

                }
            }
        };
    @Override protected void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    @Override protected void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
    }

    private void pairedDevicesList()
    {
        // mReceiver.onReceive(this, getIntent().getParcelableArrayExtra(BluetoothDevice.EXTRA_DEVICE));

        pairedDevices = mBluetoothAdapter.getBondedDevices();


        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                listOld.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }
        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, listOld);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the listOld is clicked

    }
    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
        {
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            address = info.substring(info.length() - 17);
            new ConnectBT().execute();  //Connect to chosen device

        }
    };
    private void turnOffLed()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("b".getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }
    private void turnOnLed()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("a".getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void asyncOn()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("e".getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }
    private void asyncOff()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("f".getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }
    private void blinkOn()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("c".getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }
    private void blinkOff()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("d".getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }
    private void soundOn()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("g".getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }
    private void soundOff()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("h".getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait.");  //progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try

            {
                if (btSocket == null || !isBtConnected)
                {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = mBluetoothAdapter.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection

                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                //finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }

}
