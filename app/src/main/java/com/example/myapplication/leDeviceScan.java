package com.example.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class leDeviceScan extends AppCompatActivity {

    private leDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private RecyclerView mRecyclerView;
    private Handler mHandler;

    public boolean mScanning = false;
    private static final long SCAN_PERIOD = 3000;
    private static final String SELECT_BLE_DEVICE = "BLE_DEVICE";

    private BroadcastReceiver mBluetoothDeviceSelectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledevice_scan);

        registerReceiver(mBluetoothDeviceSelectReceiver, new IntentFilter(SELECT_BLE_DEVICE) );//選到裝置後的廣播

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        mHandler = new Handler();

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 設置格線
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mLeDeviceListAdapter = new leDeviceListAdapter();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRecyclerView.setAdapter(mLeDeviceListAdapter);

        scanLeDevice(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }


    private void scanLeDevice(final boolean enable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("掃描藍牙裝置中" );
        builder.setView(R.layout.progress_bar);
        builder.setCancelable(false);
        final AlertDialog alertDialog = builder.create();
        if (enable) {
            //mLeDeviceListAdapter.clear(); 發現可以先別清，反正在別的頁面

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mScanning == true) {
                        mScanning = false;
                        mBluetoothAdapter.stopLeScan(mleScanCallback);
                        Toast.makeText(leDeviceScan.this, "Stop Scan", Toast.LENGTH_LONG).show();
                        alertDialog.dismiss();
                    }
                }
            }, SCAN_PERIOD);

            alertDialog.show();
            mScanning = true;
            mBluetoothAdapter.startLeScan(mleScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mleScanCallback);
            alertDialog.dismiss();
        }
    }

    private final BluetoothAdapter.LeScanCallback mleScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(bluetoothDevice);
                    mLeDeviceListAdapter.notifyDataSetChanged();    //確保Adapter內的list是同一個
                }
            });
        }
    };
}
