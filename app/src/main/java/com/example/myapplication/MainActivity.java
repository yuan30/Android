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
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private leDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattService mGattService;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    /*private BluetoothGattCharacteristic mGattCharacteristicRead, mGattCharacteristicWrite,
                                        mGattCharacteristicNotify;*/

    private ExpandableListView mExpandableListView_gatt_services;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private Button mBtn_on, mBtn_off, mBtn_stopService;
    private TextView mTxtViewN, mTxtViewB;
    private RecyclerView mRecyclerView;
    private Handler mHandler;
    public static boolean mScanning; public boolean maa = false;
    private String mBLE_DeviceName, mBLE_DeviceAddress;

    public static final String SELECT_BLE_DEVICE = "BLE_DEVICE";
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private static final long SCAN_PERIOD = 3000;
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //Toast.makeText(MainActivity.this, "Service connected", Toast.LENGTH_LONG).show();
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) iBinder).getService();
            if (!mBluetoothLeService.initialize()) {
                //Log.d(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            if(mBluetoothLeService.connect(mBLE_DeviceAddress) )
                Toast.makeText(MainActivity.this, "Device connect", Toast.LENGTH_LONG).show();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();
            if(mAction.equals(mBluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)){
                Toast.makeText(MainActivity.this, "可以通訊", Toast.LENGTH_LONG).show();
                showDeviceService();
            }else if(mAction.equals(mBluetoothLeService.ACTION_DATA_AVAILABLE)){

            }
        }
    };

    private BroadcastReceiver mBluetoothDeviceSelectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mBLE_DeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
            mBLE_DeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

            mTxtViewN.setText(mBLE_DeviceName + "\n" + mBLE_DeviceAddress + "Data: ");
            if(mBluetoothLeService == null){
                leDeviceOnSelect();
            }
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
                    Toast.makeText(this, "自Android 6.0开始需要打开位置权限才可以搜索到Ble设备", Toast.LENGTH_LONG).show();
                }
                //请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
            }
        }

        registerReceiver(mBluetoothDeviceSelectReceiver, new IntentFilter(SELECT_BLE_DEVICE) );//選到裝置後的廣播

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothLeService = null;

        mHandler = new Handler();

        mBtn_on = (Button) findViewById(R.id.Btn_on);
        mBtn_off = (Button) findViewById(R.id.Btn_off);
        mBtn_stopService = (Button) findViewById(R.id.Btn_stopService);

        mLeDeviceListAdapter = new leDeviceListAdapter();
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mTxtViewN = (TextView) findViewById(R.id.txtViewN);
        mTxtViewB = (TextView) findViewById(R.id.txtViewB);
        mTxtViewB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mBluetoothLeService.
            }
        });

        mBtn_on.setOnClickListener(Btn_onOnClick);
        mBtn_off.setOnClickListener(Btn_offOnClick);
        mBtn_stopService.setOnClickListener(Btn_stopServiceOnClick);

        mExpandableListView_gatt_services = (ExpandableListView) findViewById(R.id.expandableListView_gatt_services);
    }

    private View.OnClickListener Btn_onOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!mBluetoothAdapter.isEnabled()){
                Intent it = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(it, 0);
            }
            while(!mBluetoothAdapter.isEnabled()){}
            mRecyclerView.setAdapter(mLeDeviceListAdapter);
            scanLeDevice(true);
        }
    };

    private View.OnClickListener Btn_offOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mBluetoothAdapter.disable();
            while(!mBluetoothAdapter.isEnabled()){}
            Toast.makeText(MainActivity.this, "確定已關閉藍牙服務", Toast.LENGTH_LONG).show();
            mTxtViewN.setText("尚未連接");
            mTxtViewB.setText("停止服務");
            mRecyclerView.setAdapter(null); //@Nullable
        }
    };
    private View.OnClickListener Btn_stopServiceOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mBluetoothLeService != null) {
                mBluetoothLeService = null;
                unbindService(mServiceConnection);
                Toast.makeText(MainActivity.this, "stop service", Toast.LENGTH_SHORT).show();
                mTxtViewN.setText("尚未連接");
                mTxtViewB.setText("停止服務");
                mBluetoothAdapter.stopLeScan(mleScanCallback);
            }
        }
    };

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

    public void leDeviceOnSelect(){
        mBluetoothLeService = null;
        Intent it = new Intent(MainActivity.this , BluetoothLeService.class);
        bindService(it, mServiceConnection, BIND_AUTO_CREATE);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    private void showDeviceService(){
        List<BluetoothGattService> gattServices = mBluetoothLeService.gattServices();
        String allOfTerm = "";
        int i=0;
        ArrayList<Map<String,Object>> gattServiceData = new ArrayList<Map<String,Object>>();
        ArrayList<ArrayList<Map<String, Object>>> gattCharacteristicData = new ArrayList<ArrayList<Map<String, Object>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        for(BluetoothGattService gattService : gattServices){
            //allOfTerm = allOfTerm + "Unknow Service :"+(i)+"\n";
            Map<String,Object> groupMap = new HashMap<String,Object>();
            groupMap.put("title", "Unknown Service :"+ i);
            groupMap.put("uuid", gattService.getUuid().toString());
            if(i == 0){
                 mGattService = gattService;
            }
            gattServiceData.add(groupMap);

            ArrayList<Map<String, Object>>  gattCharacteristicGroupData =
                    new ArrayList<Map<String, Object>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> gattCharas =
                    new ArrayList<BluetoothGattCharacteristic>();

            for(BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics){
                int charaProp = gattCharacteristic.getProperties();
                gattCharas.add(gattCharacteristic);
                HashMap<String, Object> currentCharaData = new HashMap<String, Object>();
                //currentCharaData.put("title", SampleGattAttributes.lookup(gattCharacteristic.getUuid().toString(), "Test"));
                currentCharaData.put("uuid", gattCharacteristic.getUuid().toString());

                if( (charaProp | BluetoothGattCharacteristic.PROPERTY_READ) >0 ){
                    allOfTerm = allOfTerm + "可讀、";
                }
                if( (charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) >0 ){
                    allOfTerm = allOfTerm + "可寫、";
                    /*if(i == 0){
                        mGattCharacteristicWrite = gattCharacteristic;
                    }*/
                }
                if( (charaProp | BluetoothGattCharacteristic.PROPERTY_BROADCAST) >0 ){
                    allOfTerm = allOfTerm + "可廣播、";
                }
                if( (charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) >0 ){
                    allOfTerm = allOfTerm + "具備通知屬性\n";
                }
                currentCharaData.put("text", allOfTerm);
                gattCharacteristicGroupData.add(currentCharaData);
            }i++;
            mGattCharacteristics.add(gattCharas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {"title", "uuid"},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {"uuid", "text"},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mExpandableListView_gatt_services.setAdapter(gattServiceAdapter);
        mExpandableListView_gatt_services.setOnChildClickListener(servicesListClickListner);
    }

    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                    int childPosition, long id) {
            if (mGattCharacteristics != null) {
                final BluetoothGattCharacteristic characteristic =
                        mGattCharacteristics.get(groupPosition).get(childPosition);
                final int charaProp = characteristic.getProperties();
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    // If there is an active notification on a characteristic, clear
                    // it first so it doesn't update the data field on the user interface.
                    if (mNotifyCharacteristic != null) {
                        mBluetoothLeService.onSetCharacteristicNotification(
                                mNotifyCharacteristic, false);
                        mNotifyCharacteristic = null;
                    }
                    mBluetoothLeService.onCharacteristicRead(characteristic);
                }
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    mNotifyCharacteristic = characteristic;
                    mBluetoothLeService.onSetCharacteristicNotification(
                            characteristic, true);
                }
                return true;
            }
            return false;
        }
    };

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mLeDeviceListAdapter.clear();

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mleScanCallback);
                    Toast.makeText(MainActivity.this, "Stop Scan", Toast.LENGTH_LONG).show();
                }
            }, SCAN_PERIOD);
            Toast.makeText(MainActivity.this, "Start Scan", Toast.LENGTH_LONG).show();

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

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
