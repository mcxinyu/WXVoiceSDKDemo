package com.qq.wx.voice.demo;

import com.qq.wx.voice.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity {
    // String TAG = "MainActivity";
    /**
     * 主页的组件变量
     */
    private Button mButton = null;

    private ImageView mImageView = null;

    /**
     * 连续识别
     */
    private boolean isShowCont = true;

    /**
     * 数据识别
     */
    private boolean isShowData = true;

    /**
     * TTS
     */
    private boolean isShowTTS = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMainUI();
    }

    private void initMainUI() {
        setContentView(R.layout.activity_main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);

        mImageView = (ImageView) findViewById(R.id.imagevoice1);
        mImageView.setVisibility(View.VISIBLE);
        mImageView = (ImageView) findViewById(R.id.imagevoiceend1);
        mImageView.setVisibility(View.VISIBLE);
        mButton = (Button) findViewById(R.id.recognizer);
        mButton.setVisibility(View.VISIBLE);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnToRecognize();
            }
        });

        if (isShowCont == true) {
            mImageView = (ImageView) findViewById(R.id.imagevoice2);
            mImageView.setVisibility(View.VISIBLE);
            mImageView = (ImageView) findViewById(R.id.imagevoiceend2);
            mImageView.setVisibility(View.VISIBLE);
            mButton = (Button) findViewById(R.id.recognizer_cont);
            mButton.setVisibility(View.VISIBLE);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    turnToRecognizeCont();
                }
            });
        }

        if (isShowData == true) {
            mImageView = (ImageView) findViewById(R.id.imagevoice3);
            mImageView.setVisibility(View.VISIBLE);
            mImageView = (ImageView) findViewById(R.id.imagevoiceend3);
            mImageView.setVisibility(View.VISIBLE);
            mButton = (Button) findViewById(R.id.recognizer_data);
            mButton.setVisibility(View.VISIBLE);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    turnToRecognizeData();
                }
            });
        }

        if (isShowData == true && isShowCont == true) {
            mImageView = (ImageView) findViewById(R.id.imagevoice4);
            mImageView.setVisibility(View.VISIBLE);
            mImageView = (ImageView) findViewById(R.id.imagevoiceend4);
            mImageView.setVisibility(View.VISIBLE);
            mButton = (Button) findViewById(R.id.recognizer_data_cont);
            mButton.setVisibility(View.VISIBLE);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    turnToRecognizeDataCont();
                }
            });
        }

        if (isShowTTS == true) {
            mImageView = (ImageView) findViewById(R.id.imagevoice8);
            mImageView.setVisibility(View.VISIBLE);
            mImageView = (ImageView) findViewById(R.id.imagevoiceend8);
            mImageView.setVisibility(View.VISIBLE);
            mButton = (Button) findViewById(R.id.tts);
            mButton.setVisibility(View.VISIBLE);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    turnToTTS();
                }
            });
        }
    }

    private void turnToRecognize() {
        Intent intent = new Intent();
        intent.setClass(this, RecognizeActivity.class);
        startActivity(intent);
    }

    private void turnToRecognizeCont() {
        Intent intent = new Intent();
        intent.setClass(this, RecognizeActivityCont.class);
        startActivity(intent);
    }

    private void turnToRecognizeData() {
        Intent intent = new Intent();
        intent.setClass(this, RecognizeActivityData.class);
        startActivity(intent);
    }

    private void turnToRecognizeDataCont() {
        Intent intent = new Intent();
        intent.setClass(this, RecognizeActivityDataCont.class);
        startActivity(intent);
    }

    private void turnToTTS() {
        Intent intent = new Intent();
        intent.setClass(this, TTSActivity.class);
        startActivity(intent);
    }
}