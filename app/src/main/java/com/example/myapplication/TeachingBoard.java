package com.example.myapplication;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TeachingBoard extends AppCompatActivity implements

        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    //之後聲音要背景的話要用Service

    private ImageView mImageView_tree;
    private MediaPlayer mMediaPlayer;
    private SeekBar mSeekBar;
    private Button mBtn_media;

    private Boolean mIsInitialised = true;

    private int treeSelect;

    //或許從.txt檔拉10行介紹的文字進來 (java的方式
    //也可以是送int 然後直接[int]拿文字
    private String[] treeIntroduction = new String[11];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teachingboard);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);

        mImageView_tree = findViewById(R.id.imageView_tree);

        mBtn_media = findViewById(R.id.Btn_media);
        mBtn_media.setOnClickListener(Btn_media_OnClickListener);

        //mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        //Reader Reader = new Reader() ;
        //BufferedReader BufferedReader = new BufferedReader();

    }

    @Override
    protected void onResume() {
        super.onResume();
        treeSelect = getIntent().getIntExtra("tree", -1);
        treeSelection();
        mediaPlay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private View.OnClickListener Btn_media_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mBtn_media.setBackgroundResource(android.R.drawable.ic_media_play);
            }else{
                mediaPlay();
                mBtn_media.setBackgroundResource(android.R.drawable.ic_media_pause);
            }
        }
    };

    /*public void run() {
        int currentPosition= 0;
        int total = mMediaPlayer.getDuration();
        while (mMediaPlayer!=null && currentPosition < total) {
            try {
                Thread.sleep(1000);
                currentPosition= mMediaPlayer.getCurrentPosition();
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                return;
            }
            mSeekBar.setProgress(currentPosition);
        }
    }*/

    @Override
    public void onCompletion(MediaPlayer mp) {
        //Toast.makeText(this, "撥放完畢", Toast.LENGTH_SHORT).show();
        mBtn_media.setBackgroundResource(android.R.drawable.ic_media_play);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.release();
        mp = null;
        Toast.makeText(this, "發生錯誤、停止撥放", Toast.LENGTH_SHORT).show();

        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.seekTo(0);
        //mSeekBar.setProgress(0);
        mp.start();
        //mSeekBar.setMax(mp.getDuration());
        //Toast.makeText(this, "開始撥放", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void treeSelection(){
        Uri uri = Uri.parse("");
        if( treeSelect == 1){
            uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tree_01);
            mImageView_tree.setImageResource(R.drawable.tree_1);
        }else if( treeSelect == 2){
            uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tree_dirty);
            mImageView_tree.setImageResource(R.drawable.tree_2);
        }else if( treeSelect == 3){
            uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tree_poinciana);
            mImageView_tree.setImageResource(R.drawable.tree_3);
        }else if( treeSelect == 4){
            uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.tree_04);
            mImageView_tree.setImageResource(R.drawable.tree_4);
        }

        try{
            mMediaPlayer.setDataSource(this, uri);
        }catch (Exception e){
            Toast.makeText(this, "指定的檔案錯誤", Toast.LENGTH_LONG).show();
        }

    }
    private void mediaPlay(){
        if(mIsInitialised){
            mMediaPlayer.prepareAsync();
            mIsInitialised = false;
        }else{
            //mSeekBar.setProgress(0);
            mMediaPlayer.start();
            //mSeekBar.setMax(mMediaPlayer.getDuration()); //for milliseconds
            //new Thread(this).start();
        }
    }
}
/*mSoundPool= new SoundPool(10, AudioManager.STREAM_SYSTEM,5);
    tree01 = mSoundPool.load(this,R.raw.tree_dirty,1);
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_Soundpool:
                mSoundPool.play(tree01,5, 5, 0, 0, 1);
                Toast.makeText(this, "可撥", Toast.LENGTH_LONG).show();
                break;

                        }
     }*/

