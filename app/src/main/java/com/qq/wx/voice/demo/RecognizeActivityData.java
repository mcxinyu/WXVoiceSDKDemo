package com.qq.wx.voice.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import com.qq.wx.voice.R;
import com.qq.wx.voice.data.recognizer.VoiceRecognizer;
import com.qq.wx.voice.data.recognizer.VoiceRecognizerListener;
import com.qq.wx.voice.data.recognizer.VoiceRecognizerResult;
import com.qq.wx.voice.data.recognizer.VoiceRecordState;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
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

public class RecognizeActivityData extends Activity {
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
    // cancel: 0 --> 1 --> (2) --> 3 --> 0
    private int mRecoState = 0;

    // 识别handle号
    private static int RECO_HANDLE_ID = 100;

    // 把要识别的文件事先放这里
    String filePath = Environment.getExternalStorageDirectory().getPath()
            + "/Tencent/mm/";
    String fileName = "test.pcm";

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
                        Toast.makeText(RecognizeActivityData.this, "启动语音识别失败",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mResultTv.setText("recording...");
                    mLogTv.setText("");
                    mRecoState = 1;

                    Message message = new Message();
                    message.what = RECO_HANDLE_ID;
                    handler.sendMessage(message);
                }
            }
        });

        mCancelBtn = (Button) findViewById(R.id.cancel);
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mRecoState == 1 || mRecoState == 2) {
                    /*
                     * 【重要】取消录音识别
                     */
                    VoiceRecognizer.shareInstance().cancel();
                    mRecoState = 3;
                }
            }
        });
    }

    private void initRecognizer() {
        try {
            copyFileToLocal(this.getAssets(), fileName, filePath);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

        /*
         * 【重要】设置回调监听者
         */
        VoiceRecognizer.shareInstance().setListener(mListener);

        boolean isDebug = true;
        if (isDebug) {
            VoiceRecognizer.shareInstance().setOpenLogCat(true);
            VoiceRecognizer.shareInstance().setSaveVoice(true);
            VoiceRecognizer.shareInstance().setSaveSpeex(true);
        }

        VoiceRecognizer.shareInstance().setContRes(true);

        VoiceRecognizer.shareInstance().setContReco(false);

        VoiceRecognizer.shareInstance().setResultType(0x01);

        // 设置超时时间(5000毫秒=5秒)
        VoiceRecognizer.shareInstance().setTimeout(5000);

        /*
         * 【重要】初始化录音识别
         */
        int ret = VoiceRecognizer.shareInstance().init(this, appID);
        if (ret < 0) {
            Toast.makeText(RecognizeActivityData.this, "初始化语音识别失败",
                    Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public Handler handler = new Handler(new Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == RECO_HANDLE_ID) {
                File inFile = new File(filePath + fileName);
                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(inFile);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                try {
                    // 流式传入
                    int len = 0;
                    byte[] outByte = new byte[1024];
                    while ((len = inputStream.read(outByte, 0, 1024)) > 0) {
                        VoiceRecognizer.shareInstance().appendData(outByte, 0,
                                len, false);
                    }

                    // 告诉引擎传输完毕
                    VoiceRecognizer.shareInstance()
                            .appendData(null, 0, 0, true);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            return true;
        }
    });

    /* call back listener */
    class VRListener implements VoiceRecognizerListener {
        @Override
        public void onGetResult(VoiceRecognizerResult result) {
            // TODO Auto-generated method stub
            mResultTv.setText("(" + result.text + ")");

            if (result.isEnd) {
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
            if (errorCode == -307) {
                Toast.makeText(RecognizeActivityData.this, "5秒没说话自动关闭",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(RecognizeActivityData.this,
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

    private void copyFileToLocal(AssetManager am, String fileName,
                                 String filePath) throws IOException {
        if (filePath != null) {
            File dir = new File(filePath);
            if (!dir.exists())
                dir.mkdirs();
        }

        // in
        InputStream fin = am.open(fileName);

        // out
        String filePathName = filePath + fileName;
        File file = new File(filePathName);
        file.createNewFile();
        FileOutputStream fout = new FileOutputStream(file);

        byte[] buf = new byte[2048];
        int byteread = 0;
        while ((byteread = fin.read(buf)) != -1)
            fout.write(buf, 0, byteread);

        // out
        fout.close();

        // in
        fin.close();
    }
}