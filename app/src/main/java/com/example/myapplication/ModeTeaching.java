package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class ModeTeaching extends AppCompatActivity {

    private BluetoothLeService mBluetoothLeService;


    private Button mBtn_tree01, mBtn_tree02, mBtn_tree03, mBtn_tree04, mBtn_tree05;
    //private TextView mTxtViewN, mTxtViewS, mTxtViewD;

    //private Boolean mBool_treeTag[] = new Boolean[11];

    private void currentTree(String str){

        Toast.makeText(this, "\""+str+"\"", Toast.LENGTH_LONG).show();
        if(str.equals("01") ){ //&& (!mBool_treeTag[2-1])
            mBtn_tree01.callOnClick();
            //mBool_treeTag[2-1] = true;
        }
        else if(str.equals("02")){
            mBtn_tree02.callOnClick();
        }
        else if(str.equals("03")){
            mBtn_tree03.callOnClick();
        }
        else if(str.equals("04")){
            mBtn_tree04.callOnClick();
        }
    }
    private BroadcastReceiver mGattDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mAction = intent.getAction();
            if(mAction.equals(mBluetoothLeService.ACTION_DATA_AVAILABLE)){//tag碰到時
                String str = intent.getStringExtra(mBluetoothLeService.EXTRA_DATA);
                currentTree(str);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_teaching);

        mBtn_tree01 =  findViewById(R.id.Btn_tree01);
        mBtn_tree02 =  findViewById(R.id.Btn_tree02);
        mBtn_tree03 =  findViewById(R.id.Btn_tree03);
        mBtn_tree04 =  findViewById(R.id.Btn_tree04);
        mBtn_tree05 =  findViewById(R.id.Btn_tree05);

        mBtn_tree01.setOnClickListener(Btn_tree01_OnClickListener);
        mBtn_tree02.setOnClickListener(Btn_tree02_OnClickListener);
        mBtn_tree03.setOnClickListener(Btn_tree03_OnClickListener);
        mBtn_tree04.setOnClickListener(Btn_tree04_OnClickListener);
        mBtn_tree05.setOnClickListener(Btn_tree05_OnClickListener);

        registerReceiver(mGattDataReceiver, makeGattDataIntentFilter());

        /*for(int i=0; i<mBool_treeTag.length; i++){
            mBool_treeTag[i] = false;
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattDataReceiver);//保險用著
    }

    private View.OnClickListener Btn_tree01_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ModeTeaching.this , TeachingBoard.class);
            intent.putExtra("tree",1);//intent.putExtra("tree","01");
            startActivity(intent);
        }
    };
    private View.OnClickListener Btn_tree02_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ModeTeaching.this , TeachingBoard.class);
            intent.putExtra("tree",2);
            startActivity(intent);
        }
    };
    private View.OnClickListener Btn_tree03_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ModeTeaching.this , TeachingBoard.class);
            intent.putExtra("tree",3);
            startActivity(intent);
        }
    };
    private View.OnClickListener Btn_tree04_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ModeTeaching.this , TeachingBoard.class);
            intent.putExtra("tree",4);
            startActivity(intent);
        }
    };
    private View.OnClickListener Btn_tree05_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };


    private static IntentFilter makeGattDataIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
