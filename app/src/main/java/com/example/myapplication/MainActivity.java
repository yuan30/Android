package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private leDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private Button mBtn_on, mBtn_off, mBtn_startService;
    private TextView mTxtView;
    private RecyclerView mRecyclerView;
    private Handler mHandler;
    private boolean mScanning;
    private String mBluetoothLEDeviceName, mBluetoothLEDeviceAddress;

    public static final String SELECT_BLE_DEVICE = "BLE_DEVICE";
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private static final long SCAN_PERIOD = 3000;
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;

    private BluetoothLeService mBluetoothLeService = new BluetoothLeService() ;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Toast.makeText(MainActivity.this, "Service connected", Toast.LENGTH_LONG).show();
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private BroadcastReceiver mGattChangeData = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    private BroadcastReceiver mBluetoothDevice_select = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mBluetoothLEDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
            mBluetoothLEDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
            mTxtView = (TextView) findViewById(R.id.txtViewN);
            mTxtView.setText(mBluetoothLEDeviceName + "\n" + mBluetoothLEDeviceAddress);
            leDeviceOnSelect();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent it = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(it, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
            //判断是否具有权限
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //判断是否需要向用户解释为什么需要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                  Toast.makeText(this, "自Android 6.0开始需要打开位置权限才可以搜索到Ble设备", Toast.LENGTH_LONG);
                }
                //请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
            }
        }

        registerReceiver(mBluetoothDevice_select, new IntentFilter(SELECT_BLE_DEVICE) );//選到裝置後的廣播

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        /*if (mBluetoothAdapter == null) {
            Toast.makeText(this, "此裝置不支援BLE", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }*/
        mHandler = new Handler();

        mBtn_on = (Button) findViewById(R.id.Btn_on);
        mBtn_off = (Button) findViewById(R.id.Btn_off);
        mBtn_startService = (Button) findViewById(R.id.Btn_startService);

        mLeDeviceListAdapter = new leDeviceListAdapter();
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mBtn_on.setOnClickListener(Btn_onOnClick);
        mBtn_off.setOnClickListener(Btn_offOnClick);
        mBtn_startService.setOnClickListener(Btn_startServiceOnClick);
    }

    private View.OnClickListener Btn_onOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!mBluetoothAdapter.isEnabled()){
                Intent it = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(it, 0);
            }
            mRecyclerView.setAdapter(mLeDeviceListAdapter);
            scanLeDevice(true);
        }
    };

    private View.OnClickListener Btn_offOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mBluetoothAdapter.disable();
            scanLeDevice(false);
        }
    };
    private View.OnClickListener Btn_startServiceOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /*Intent it = new Intent(MainActivity.this , BluetoothLeService.class);
            //startService(it);
            bindService(it, mServiceConnection, BIND_AUTO_CREATE);
            //mTxtView.setText(mBluetoothAdapter.getName());*/
            Toast.makeText(MainActivity.this, "Test", Toast.LENGTH_SHORT).show();
        }
    };

    public void leDeviceOnSelect(){
        Intent it = new Intent(MainActivity.this , BluetoothLeService.class);
        bindService(it, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户允许改权限，0表示允许，-1表示拒绝 PERMISSION_GRANTED = 0， PERMISSION_DENIED = -1
                //permission was granted, yay! Do the contacts-related task you need to do.
                //这里进行授权被允许的处理
            } else {
                //permission denied, boo! Disable the functionality that depends on this permission.
                //这里进行权限被拒绝的处理
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mleScanCallback);
                    Toast.makeText(MainActivity.this, "Stop Scan", Toast.LENGTH_LONG).show();
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mleScanCallback);

        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mleScanCallback);
            Toast.makeText(MainActivity.this, "Stop Scan", Toast.LENGTH_LONG).show();
        }
    }

    private final BluetoothAdapter.LeScanCallback mleScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Toast.makeText(MainActivity.this, "進來CALLBACK啦", Toast.LENGTH_LONG).show();
                    mLeDeviceListAdapter.addDevice(bluetoothDevice);
                    mLeDeviceListAdapter.notifyDataSetChanged();    //確保Adapter內的list是同一個
                }
            });
        }
    };
}