package com.example.myapplication;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private String mBLE_DeviceAddress;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "connected";
    public final static String ACTION_GATT_DISCONNECTED =
            "disconnected";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth_30pix.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth_30pix.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth_30pix.le.EXTRA_DATA";

    public final static String ACTION_WRITE_DATA =
            "com.example.bluetooth_30pix.le.ACTION_DATA_AVAILABLE";
    public final static String WRITE_DATA =
            "com.example.bluetooth_30pix.le.WRITE_DATA";

    /*public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);*/


    public BluetoothLeService() {
        super();
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService(){
            return BluetoothLeService.this;
        }
    } LocalBinder mLocalBin = new LocalBinder();
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBin;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        disconnect();//斷掉以建立和嘗試連接的裝置後，在回撥內有關閉客戶端
                    //如果在這同時做，會發生沒進回撥的情況
        Log.v(TAG, "Unbind");
        return super.onUnbind(intent);
    }

    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBLE_DeviceAddress != null && address.equals(mBLE_DeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) { //再跟曾經連過的BLE重新連線
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        } //Toast.makeText(this, device.getName()+" dd", Toast.LENGTH_LONG).show();
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        //拿到裝置後，Gatt也建立成功，應該是會跳進mGattCallback.onConnectionStateChange
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBLE_DeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    private void close(){
        if(mBluetoothGatt == null){
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void disconnect(){
        if(mBluetoothGatt == null){
            return;
        }

        mBluetoothGatt.disconnect();
    }

    public void onCharacteristicRead(BluetoothGattCharacteristic characteristic){
        if(mBluetoothAdapter == null || mBluetoothGatt == null){
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void onCharacteristicWrite(BluetoothGattCharacteristic characteristic){
        if(mBluetoothAdapter == null || mBluetoothGatt == null){
            return;
        }
        //mBluetoothGatt.setCharacteristicNotification(characteristic, true);
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public void onSetCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);


        if (SampleGattAttributes.BLE_DEVICE_NOTIFY.equals(characteristic.getUuid().toString())) {
            Log.w(TAG, "BluetoothAdapter set config");
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) { //notification對應
            String intentAction;
            if(!(status == BluetoothGatt.GATT_SUCCESS)) {
                Log.v(TAG, "Can't connect to GATT server.");
                //return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction); //為了在畫面上顯示連線
                Log.v(TAG, "Connected to GATT server.");
                //Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.v(TAG, "Disconnected from GATT server.");
                close();//斷開連線也釋放
                broadcastUpdate(intentAction); //為了在畫面上顯示斷開
            }
        }

        @Override
        // New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {//Gatt.discoverServices對應
                //Toast.makeText(BluetoothLeService.this, "可以通訊", Toast.LENGTH_).show();
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                Log.w(TAG, "onServicesDiscovered yeah: " + status);
            } else {
                //Toast.makeText(this, "還不能通訊", Toast.LENGTH_LONG).show();
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) { //Gatt.readCharacteristic對應
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                Log.v(TAG, "on Read " + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                          int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.v(TAG, "on Write " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_WRITE_DATA, characteristic);
                Log.v(TAG, "on Write " + characteristic.getStringValue(0));
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            Log.v(TAG, "on Changed " + characteristic.getUuid().toString());
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%02X", byteChar)); //02X後有空白有差
            //intent.putExtra(EXTRA_DATA, "\""+ new String(data) +"\""+ "\nHEX :" +stringBuilder.toString());
            intent.putExtra(EXTRA_DATA, stringBuilder.toString());
            Log.v(TAG, "on Changed " + stringBuilder.toString());
        }
        sendBroadcast(intent);
    }

    public List<BluetoothGattService> gattServices() {
        return mBluetoothGatt.getServices();
    }
}
