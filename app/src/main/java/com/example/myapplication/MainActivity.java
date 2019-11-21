package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
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
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic mGattCharacteristicRead, mGattCharacteristicWrite,
            mGattCharacteristicNotify, mGattCharacteristicBroadcast,mGattCharacteristicOther;

    private Button mBtn_on, mBtn_off, mBtn_stopService, mBtn_write, mBtn_write2;
    private TextView mTxtViewN, mTxtViewS, mTxtViewD;
    private MenuItem mMenuItem_bluetooth; //for action Bar

    private AlertDialog alertDialog_connectBLE = null;
    private Boolean mIsPlaying = false;
    public String mBLE_DeviceName = null, mBLE_DeviceAddress;
    public static final String SELECT_BLE_DEVICE = "BLE_DEVICE";
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private static final int MENU_BLUETOOTH = Menu.FIRST;
    private final static String TAG = MainActivity.class.getSimpleName();

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) iBinder).getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            if(mBluetoothLeService.connect(mBLE_DeviceAddress) ) {
                //Toast.makeText(MainActivity.this, "Device connect", Toast.LENGTH_LONG).show();
                mMenuItem_bluetooth.setIcon(R.drawable.bluetooth_30pix);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {}
    };
    private void currentState(String str){
        String str1 ="State :"+ str;
        mTxtViewS.setText(str1);
        if(str == "disconnected"){
            mTxtViewD.setText("Data :");
            mMenuItem_bluetooth.setIcon(R.drawable.bluetooth_off_30pix);
            mBtn_stopService.callOnClick();

            alertDialog_connectBLE.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("藍牙連線失敗");
            alertDialog_connectBLE = builder.create();
            alertDialog_connectBLE.show();
        }
    }
    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();
            if(mAction.equals(mBluetoothLeService.ACTION_GATT_CONNECTED)){
                currentState(mAction);
            }else if(mAction.equals(mBluetoothLeService.ACTION_GATT_DISCONNECTED)){
                currentState(mAction);
            }else if(mAction.equals(mBluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)){
                Toast.makeText(MainActivity.this, "可以通訊", Toast.LENGTH_LONG).show();
                showDeviceService();
            }else if(mAction.equals(mBluetoothLeService.ACTION_DATA_AVAILABLE)){//read 廣播時才來
                String str = intent.getStringExtra(mBluetoothLeService.EXTRA_DATA);
                mTxtViewD.setText("Data :"+str);
            }else if(mAction.equals(mBluetoothLeService.ACTION_WRITE_DATA)){//write
                String str = intent.getStringExtra(mBluetoothLeService.EXTRA_DATA);
                //mTxtViewB.setText(str);
                Toast.makeText(MainActivity.this, str+"", Toast.LENGTH_LONG).show();
            }
        }
    };

    private BroadcastReceiver mBluetoothDeviceSelectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mBLE_DeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
            mBLE_DeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

            mTxtViewN.setText(mBLE_DeviceName + "\n" + mBLE_DeviceAddress);
            if(mBLE_DeviceName != null){ //原本放onResume，後來想想應該是先進那才進這，但進這才有值
                mBtn_write2.setEnabled(true);
            }
            if(mBluetoothLeService == null){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("建立藍牙連線中" );
                builder.setView(R.layout.progress_bar);
                builder.setCancelable(false);
                alertDialog_connectBLE = builder.create();
                alertDialog_connectBLE.show();
                leDeviceOnSelect();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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



        mBtn_on = findViewById(R.id.Btn_on);
        mBtn_off = findViewById(R.id.Btn_off);
        mBtn_stopService = findViewById(R.id.Btn_stopService);
        mBtn_write = findViewById(R.id.Btn_write);
        mBtn_write2 = findViewById(R.id.Btn_write2);
        mBtn_write2.setEnabled(false);

        mTxtViewN = findViewById(R.id.txtViewN);
        mTxtViewS = findViewById(R.id.txtViewS);
        mTxtViewD = findViewById(R.id.txtViewD);

        mBtn_on.setOnClickListener(Btn_onOnClick);
        mBtn_off.setOnClickListener(Btn_offOnClick);
        mBtn_stopService.setOnClickListener(Btn_stopServiceOnClick);
        mBtn_write.setOnClickListener(Btn_writeOnClick);
        mBtn_write2.setOnClickListener(Btn_write2OnClick);

    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if(mIsPlaying){
            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            mIsPlaying = false;
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        //if(mIsPlaying) //改個方法只有按下教學或對戰才true(舊方法mBluetoothLeService != null)
         //   unregisterReceiver(mGattUpdateReceiver); //要確定服務有建起來後，才會因為遊玩才取消註冊
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBluetoothDeviceSelectReceiver);//保險用著
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_main, menu);//使用靜態建立的menu_main_xml

        menu.add(0, MENU_BLUETOOTH, 0, "");
        mMenuItem_bluetooth = menu.getItem(0); //menu.findItem(0);
        mMenuItem_bluetooth.setIcon(R.drawable.bluetooth_off_30pix);
        mMenuItem_bluetooth.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    //就算沒在onCreateOptionsMenu實作別，變成actionItem也可以進來這
    //不知484沒設actionLayout或actionViewClass，like Region and Search
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case MENU_BLUETOOTH:
                if(mBLE_DeviceName != null)
                    Toast.makeText(MainActivity.this, "已連上"+mBLE_DeviceName
                            , Toast.LENGTH_LONG).show();
                mBtn_on.callOnClick();
                return true;
            case android.R.id.home:
                Toast.makeText(this, "nothing", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode != 0)
            return;
        while(!mBluetoothAdapter.isEnabled()){}
        if(resultCode == RESULT_OK){
            //Toast.makeText(MainActivity.this, "12", Toast.LENGTH_SHORT).show();

            //Toast.makeText(MainActivity.this, "45", Toast.LENGTH_SHORT).show();
            Intent it = new Intent(MainActivity.this , leDeviceScan.class);
            startActivity(it);
        }else{}
    }*/

    private View.OnClickListener Btn_onOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!mBluetoothAdapter.isEnabled()){
                //Intent it = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                //startActivityForResult(it, 0); //到onActivityResult去
                mBluetoothAdapter.enable();
                while(!mBluetoothAdapter.isEnabled()){}
                Intent ite = new Intent(MainActivity.this , leDeviceScan.class);
                startActivity(ite);
            }else{
                Intent it = new Intent(MainActivity.this , leDeviceScan.class);
                startActivity(it);
            }
        }
    };

    private View.OnClickListener Btn_offOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //mBluetoothAdapter.stopLeScan(mleScanCallback);

            while(!mBluetoothAdapter.disable()){}
            Toast.makeText(MainActivity.this, "確定已關閉藍牙服務", Toast.LENGTH_LONG).show();

        }
    };

    private View.OnClickListener Btn_stopServiceOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mBluetoothLeService != null) {
                mBluetoothLeService = null;
                mGattCharacteristicNotify = null;
                unbindService(mServiceConnection);  //unbind裡有呼叫gatt.disconnect，再回回撥去
                Toast.makeText(MainActivity.this, "stop service", Toast.LENGTH_SHORT).show();

                mBtn_write2.setEnabled(false); //把對戰模式按鈕暗掉
                mBLE_DeviceName = null;
                mBLE_DeviceAddress = null;
            }
        }
    };

    private View.OnClickListener Btn_writeOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mGattCharacteristicNotify != null) {
               /* mGattCharacteristicNotify.setValue(new byte[]{0x1});
                mGattCharacteristicNotify.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                mBluetoothLeService.onCharacteristicWrite(mGattCharacteristicNotify);
                mIsPlaying = true;//進到教學或對戰，為了gatt的廣播接收器(有進來為前提->有連線)
                                  //不然onPause()那會出錯*/
            }
            Intent it = new Intent(MainActivity.this , ModeTeaching.class);
            startActivity(it);
        }
    };

    private View.OnClickListener Btn_write2OnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mGattCharacteristicNotify != null) {
                mGattCharacteristicNotify.setValue(new byte[]{0x2});
                mGattCharacteristicNotify.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                mBluetoothLeService.onCharacteristicWrite(mGattCharacteristicNotify);
                mIsPlaying = true;//進到教學或對戰，為了gatt的廣播接收器(有進來為前提->有連線)
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
        //int i=0; //0000ffe0-0000-1000-8000-00805f9b34fb i==2

        mGattCharacteristicNotify = gattServices.
                                    get(2).
                                    getCharacteristic(UUID.
                                            fromString(SampleGattAttributes.BLE_DEVICE_NOTIFY));
        mBluetoothLeService.onSetCharacteristicNotification(mGattCharacteristicNotify,
                true);
        alertDialog_connectBLE.dismiss();
        return ;
        /*for(BluetoothGattService gattService : gattServices){

            Map<String,Object> groupMap = new HashMap<String,Object>();
            groupMap.put("title", "Unknown Service :"+ i);
            groupMap.put("uuid", gattService.getUuid().toString());
            Log.d(TAG, gattService.getUuid().toString());
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

            for(BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics){
                int charaProp = gattCharacteristic.getProperties();
                if(!gattCharacteristic.getUuid().toString().equals(SampleGattAttributes.BLE_DEVICE_NOTIFY)){
                    continue;
                }
                if( (charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) >0 ){
                    if(i==2){
                        mGattCharacteristicNotify = gattCharacteristic;
                        mBluetoothLeService.onSetCharacteristicNotification(mGattCharacteristicNotify,
                                true);
                    }
                }
            }i++;
        }*/
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
