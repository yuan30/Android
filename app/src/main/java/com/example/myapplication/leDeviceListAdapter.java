package com.example.myapplication;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class leDeviceListAdapter extends RecyclerView.Adapter<leDeviceListAdapter.ViewHolder>{

    private List<BluetoothDevice> mLeDeviceList;


    public leDeviceListAdapter(){
        super();
        mLeDeviceList = new ArrayList<BluetoothDevice>();
    }

    public void addDevice(BluetoothDevice device){
        if(!mLeDeviceList.contains(device))
            mLeDeviceList.add(device);
    }

    public void clear(){
        mLeDeviceList.clear();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item1, parent, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(mLeDeviceList.size() != 0) {
            holder.mTxtView.setText(mLeDeviceList.get(position).getName());
            holder.mTxtView2.setText(mLeDeviceList.get(position).getAddress());
        }
    }

    @Override
    public int getItemCount() {
        return mLeDeviceList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mTxtView, mTxtView2;

        public ViewHolder(View itemView) {
            super(itemView);
            //view component與ViewHolder中的元件屬性綁定
            mTxtView = (TextView) itemView.findViewById(R.id.txtView);
            mTxtView2 = (TextView) itemView.findViewById(R.id.txtView2);
            //處理按下的事件
            itemView.setOnClickListener(ViewHolder.this);
        }

        @Override
        public void onClick(View v) {
            /**按下後執行的程式碼*/

            if(mLeDeviceList.size() != 0)
            {Toast.makeText(v.getContext(), mLeDeviceList.get(getAdapterPosition()).getName()+"\n"+
                    mLeDeviceList.get(getAdapterPosition()).getAddress(), Toast.LENGTH_LONG).show();}
            if(mLeDeviceList.get(getAdapterPosition()).getName().equals("HMSoft") ||
                    mLeDeviceList.get(getAdapterPosition()).getName().equals("HM171001")){
                final BluetoothDevice device = mLeDeviceList.get(getAdapterPosition());
                if (device == null) return;

                Intent it = new Intent(MainActivity.SELECT_BLE_DEVICE);
                it.putExtra(MainActivity.EXTRAS_DEVICE_NAME, device.getName());
                it.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                //範例這有讓掃描停止，之後再看需不需要
                v.getContext().sendBroadcast(it);
            }
        }
    }
}
