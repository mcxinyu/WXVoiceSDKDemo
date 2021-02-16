package com.qq.wx.voice.demo;

import java.util.Timer;
import java.util.TimerTask;

import com.qq.wx.voice.R;
import com.qq.wx.voice.recognizer.VoiceRecognizer;
import com.qq.wx.voice.recognizer.VoiceRecognizerListener;
import com.qq.wx.voice.recognizer.VoiceRecognizerResult;
import com.qq.wx.voice.recognizer.VoiceRecordState;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class RecognizeActivityCont extends Activity {
    // public final String TAG = "RecognizeActivity";

    /*********************/
    /* recognize objects */
    /*********************/
    // appid
    private String appID = "wx7d02f7e92ea2884d";

    // listener
    private VRListener mListener = new VRListener();

    // 0: free; 1: recording; 2: recognizing; 3: canceling;
    // no cancel: 0 --> 1 --> 2 --> 0
    // cancel: 0 --> (1) --> (2) --> 3 --> 0
    private int mRecoState = 0;

    private String mRecoResult = "";

    /**************/
    /* UI objects */
    /**************/
    private TextView mResultTv = null;

    private TextView mLogTv = null;

    private Button mCompleteBtn = null;

    private Button mCancelBtn = null;

    private Timer mFrameTimer = null;

    private TimerTask mFrameTask = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRecognizerUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        initRecognizer();
    }

    @Override
    public void onDestroy() {
        /*
         * 【重要】销毁录音识别
         */
        VoiceRecognizer.shareInstance().destroy();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 监控返回键
        if (keyCode == KeyEvent.KEYCODE_BACK)
            finish();
        return super.onKeyDown(keyCode, event);
    }

    private void initRecognizerUI() {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.voicerecognizer_demo);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);

        TextView mTitle = (TextView) findViewById(R.id.toptitle);
        mTitle.setText("语音识别");

        mResultTv = (TextView) findViewById(R.id.result);
        mResultTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        mLogTv = (TextView) findViewById(R.id.log);
        mLogTv.setMovementMethod(ScrollingMovementMethod.getInstance());

        mCompleteBtn = (Button) findViewById(R.id.complete);
        mCompleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRecoState == 0) {
                    /*
                     * 【重要】开始录音识别
                     */
                    int ret = VoiceRecognizer.shareInstance().start();
                    if (ret < 0) {
                        Toast.makeText(RecognizeActivityCont.this, "启动语音识别失败",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mLogTv.setText("");
                    mRecoState = 1;
                } else if (mRecoState == 1) {
                    /*
                     * 【重要】停止录音，等待识别结果
                     */
                    VoiceRecognizer.shareInstance().stop();
                    mRecoState = 2;
                }
            }
        });

        mCancelBtn = (Button) findViewById(R.id.cancel);
        mCancelBtn.setVisibility(View.INVISIBLE);
    }

    private void initRecognizer() {
        /*
         * 【重要】设置回调监听者
         */
        VoiceRecognizer.shareInstance().setListener(mListener);

        boolean isDebug = false;
        if (isDebug) {
            VoiceRecognizer.shareInstance().setOpenLogCat(true);
            VoiceRecognizer.shareInstance().setSaveVoice(true);
            VoiceRecognizer.shareInstance().setSaveSpeex(true);
        }

        VoiceRecognizer.shareInstance().setContRes(true);

        VoiceRecognizer.shareInstance().setContReco(true);

        VoiceRecognizer.shareInstance().setResultType(0x01);

        VoiceRecognizer.shareInstance().setSilentTime(500);

        /*
         * 【重要】初始化录音识别
         */
        int ret = VoiceRecognizer.shareInstance().init(this, appID);
        if (ret < 0) {
            Toast.makeText(RecognizeActivityCont.this, "初始化语音识别失败",
                    Toast.LENGTH_SHORT).show();
            return;
        }
    }

    /* call back listener */
    class VRListener implements VoiceRecognizerListener {
        @Override
        public void onGetResult(VoiceRecognizerResult result) {
            // TODO Auto-generated method stub
            if (result.text.isEmpty() == false)
                mResultTv.setText(mRecoResult + "\n" + result.text);

            if (result.isEnd) {
                if (result.text.isEmpty() == false)
                    mResultTv.setText(mRecoResult + "\n[" + result.startTime
                            + ", " + result.stopTime + "]" + result.text);
                mRecoResult = mResultTv.getText().toString();
            }

            if (result.isAllEnd) {
                mRecoState = 0;
                updateBtn(mRecoState);
            }
        }

        /*
         * Start-->Recording-->Complete-->GetResult
         * Start-->Recording-->(Complete)-->Canceling-->Canceled
         */
        @Override
        public void onGetVoiceRecordState(VoiceRecordState state) {
            // TODO Auto-generated method stub
            mLogTv.append(state + "\n");

            if (state == VoiceRecordState.Recording) {
                mRecoState = 1;
                updateBtn(mRecoState);
            }

            if (state == VoiceRecordState.Complete) {
                mRecoState = 2;
                updateBtn(mRecoState);
            }

            if (state == VoiceRecordState.Canceling) {
                mRecoState = 3;
                updateBtn(mRecoState);
            }

            if (state == VoiceRecordState.Canceled) {
                mResultTv.setText("点击开始说话");
                mRecoState = 0;
                updateBtn(mRecoState);
            }
        }

        @Override
        public void onVolumeChanged(int volume) {
            // TODO Auto-generated method stub
            /* mic drawable list */
            int mMicNum = 8;
            int[] mMic = new int[]{R.drawable.recog001, R.drawable.recog002,
                    R.drawable.recog003, R.drawable.recog004,
                    R.drawable.recog005, R.drawable.recog006,
                    R.drawable.recog007, R.drawable.recog008};

            int index = volume;
            if (index < 0)
                index = 0;
            if (index >= mMicNum)
                index = mMicNum - 1;
            if (mCompleteBtn != null && mRecoState == 1)
                mCompleteBtn.setBackgroundResource(mMic[index]);
        }

        @Override
        public void onGetError(int errorCode) {
            // TODO Auto-generated method stub
            Toast.makeText(RecognizeActivityCont.this,
                    String.valueOf(errorCode), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onGetVoicePackage(byte[] pack, String getArgs) {
            // TODO Auto-generated method stub
        }
    }

    private void updateBtn(int recoState) {
        if (recoState == 0) {
            cancelTask();
            mCompleteBtn.setEnabled(true);
            mCompleteBtn.setBackgroundResource(R.drawable.recogstart);

            mCancelBtn.setEnabled(false);
            mCancelBtn.setBackgroundResource(R.drawable.recogcancelgray);
        }

        if (recoState == 1) {
            mCancelBtn.setEnabled(true);
            mCancelBtn.setBackgroundResource(R.drawable.recogcancel);
        }

        if (recoState == 2) {
            mCompleteBtn.setEnabled(false);
            startTask();
        }

        if (recoState == 3) {
            mCompleteBtn.setEnabled(false);
            mCompleteBtn.setBackgroundResource(R.drawable.recoggray);

            mCancelBtn.setEnabled(false);
            mCancelBtn.setBackgroundResource(R.drawable.recogcancelgray);
        }
    }

    private void startTask() {
        mFrameTimer = new Timer(false);
        mFrameTask = new TimerTask() {
            int btnIndex = 0;

            @Override
            public void run() {
                int index = (btnIndex++) % 8;
                Message message = new Message();
                message.what = index;
                mHandler.sendMessage(message);
            }
        };
        mFrameTimer.schedule(mFrameTask, 200, 100);
    }

    private void cancelTask() {
        if (mFrameTask != null)
            mFrameTask.cancel();

        if (mFrameTimer != null)
            mFrameTimer.cancel();
    }

    private Handler mHandler = new Handler(new Callback() {
        /* mic drawable list */
        private final int mRecNum = 8;
        private final int[] mRec = new int[]{R.drawable.recowait001,
                R.drawable.recowait002, R.drawable.recowait003,
                R.drawable.recowait004, R.drawable.recowait005,
                R.drawable.recowait006, R.drawable.recowait007,
                R.drawable.recogstart};

        @Override
        public boolean handleMessage(Message msg) {
            int index = msg.what;
            if (index < 0 || index >= mRecNum)
                index = 7;
            mCompleteBtn.setBackgroundResource(mRec[index]);
            return false;
        }
    });
}