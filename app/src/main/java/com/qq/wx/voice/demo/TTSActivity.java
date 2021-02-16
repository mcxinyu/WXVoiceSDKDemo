package com.qq.wx.voice.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.qq.wx.voice.synthesizer.SpeechSynthesizer;
import com.qq.wx.voice.synthesizer.SpeechSynthesizerResult;
import com.qq.wx.voice.synthesizer.SpeechSynthesizerListener;
import com.qq.wx.voice.synthesizer.SpeechSynthesizerState;
import com.qq.wx.voice.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TTSActivity extends Activity implements OnCompletionListener {
    /***********************/
    /* synthesizer objects */
    /***********************/
    // appid
    private String appID = "wx7d02f7e92ea2884d";

    // listener
    private SyListener mListener = new SyListener();

    // 合成文本
    private String mSynWords = null;

    // 音频类型(0:mp3,1:wav,2:amr)
    private int voiceFormat = 0;

    // tts状态(0:未开始,1:正在合成,2:已合成未播放,3:正在播放,4:暂停)
    private int mState = 0;

    // 结果文件
    private String resFile = null;

    // 播放程序
    private Player mPlayer = new Player();

    /**************/
    /* UI objects */
    /**************/
    private EditText mWords = null;

    private Button mStartBtn = null;

    private Button mCancelBtn = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSynUI();
        initSyn();
        mPlayer.init(this);
        Log.d("onCreate", "Thread: " + android.os.Process.myTid() + " name: "
                + Thread.currentThread().getName());
    }

    @Override
    protected void onDestroy() {
        mPlayer.release();
        SpeechSynthesizer.shareInstance().destroy();
        super.onDestroy();
    }

    // 重置返回按钮
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mState = 0;
            updateBtn(mState);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initSynUI() {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.syn_demo);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);

        TextView mTitle = (TextView) findViewById(R.id.toptitle);
        mTitle.setText("语音合成");

        // 水平滚动设置为False
        mWords = (EditText) findViewById(R.id.et_words);
        String testText = "微信语音开放平台，免费给移动开发者提供语音云服务，目前开放的有语音识别、语音合成等。"
                + "其中语音识别可以让有文字输入的地方用语音输入来代替，准确率达90%以上；"
                + "语音合成支持把文字合成甜美女声。从此让你的用户与手机自由语音交互，体验别致的移动生活！";
        mWords.setText(testText);

        mStartBtn = (Button) findViewById(R.id.syn_start);
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mState == 0) {
                    mSynWords = mWords.getText().toString();
                    int ret = SpeechSynthesizer.shareInstance()
                            .start(mSynWords);
                    if (ret != 0) {
                        Toast.makeText(TTSActivity.this, "启动语音合成失败",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mState = 1;
                    updateBtn(mState);
                } else if (mState == 2 || mState == 4) {
                    if (mPlayer.play() == true) {
                        mState = 3;
                        updateBtn(mState);
                    }
                } else if (mState == 3) {
                    if (mPlayer.pause() == true) {
                        mState = 4;
                        updateBtn(mState);
                    }
                }
            }
        });

        mCancelBtn = (Button) findViewById(R.id.cancel);
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mState == 1)
                    SpeechSynthesizer.shareInstance().cancel();

                // stop the player
                if (mPlayer != null && mPlayer.isPlaying() == true)
                    mPlayer.stop();

                mState = 0;
                updateBtn(mState);
            }
        });
    }

    private void initSyn() {
        // 设置回调监听
        SpeechSynthesizer.shareInstance().setListener(mListener);

        // 设置合成音频格式
        SpeechSynthesizer.shareInstance().setFormat(voiceFormat);

        // 设置合成音量
        SpeechSynthesizer.shareInstance().setVolume(1.0f);

        // 打开日志
        SpeechSynthesizer.shareInstance().setOpenLogCat(true);

        // 用appID初始化
        int mInitSucc = SpeechSynthesizer.shareInstance().init(this, appID);
        if (mInitSucc != 0)
            Toast.makeText(this, "初始化语音合成失败", Toast.LENGTH_SHORT).show();
    }

    /* call back listener */
    class SyListener implements SpeechSynthesizerListener {
        @SuppressLint({"SimpleDateFormat"})
        @Override
        public void onGetResult(SpeechSynthesizerResult res) {
            // 把录音写入文件
            SpeechSynthesizerResult result = (SpeechSynthesizerResult) res;
            resFile = writeWavFile(result.speech);
            if (resFile == null) {
                mState = 0;
                updateBtn(mState);
                return;
            }

            // 合成完毕后播放
            try {
                mPlayer.playFile(resFile);
                mState = 3;
                updateBtn(mState);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                mState = 0;
                updateBtn(mState);
            }
        }

        @Override
        public void onGetError(int errorCode) {
            // TODO Auto-generated method stub
            Toast.makeText(TTSActivity.this, "ErrorCode = " + errorCode,
                    Toast.LENGTH_LONG).show();

            mState = 0;
            updateBtn(mState);
        }

        @Override
        public void onGetVoiceRecordState(SpeechSynthesizerState state) {
            // TODO Auto-generated method stub
        }

        @SuppressLint("SimpleDateFormat")
        private String writeWavFile(byte[] speech) {
            if (speech == null || speech.length == 0)
                return null;

            // 创建放录音文件的文件夹
            String filepath = Environment.getExternalStorageDirectory()
                    .getPath() + "/Tencent/mm";
            File outputpath = new File(filepath);
            if (!outputpath.exists())
                outputpath.mkdirs();

            // 生成录音文件名
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            String dateStr = dateFormat.format(date);
            String voiceType = ".mp3";
            if (voiceFormat == 0) {
                voiceType = ".mp3";
            } else if (voiceFormat == 1) {
                voiceType = ".wav";
            } else if (voiceFormat == 2) {
                voiceType = ".amr";
            }

            // 创建录音文件
            String voiceFileName = filepath + "/" + dateStr + voiceType;
            File voiceFile = new File(voiceFileName);
            if (!voiceFile.exists()) {
                try {
                    voiceFile.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            // 写入录音文件
            FileOutputStream voiceOutputStream = null;
            try {
                voiceOutputStream = new FileOutputStream(voiceFile);
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            if (voiceOutputStream == null)
                return null;

            try {
                voiceOutputStream.write(speech);
                voiceOutputStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return voiceFileName;
        }
    }

    private void updateBtn(int state) {
        if (state == 0) {
            if (mStartBtn != null) {
                mStartBtn.setEnabled(true);
                mStartBtn.setBackgroundResource(R.drawable.synplay);
            }
            if (mCancelBtn != null) {
                mCancelBtn.setEnabled(false);
                mCancelBtn.setBackgroundResource(R.drawable.recogcancelgray);
            }

            setWords(true);

            // delete the voice file
            if (resFile != null) {
                File voiceFile = new File(resFile);
                if (voiceFile.exists())
                    voiceFile.delete();
                resFile = null;
            }
        }

        if (state == 1) {
            if (mStartBtn != null) {
                mStartBtn.setEnabled(false);
                mStartBtn.setBackgroundResource(R.drawable.synplayp);
            }
            if (mCancelBtn != null) {
                mCancelBtn.setEnabled(true);
                mCancelBtn.setBackgroundResource(R.drawable.recogcancel);
            }
            setWords(true);
        }

        if (state == 2) {
            if (mStartBtn != null) {
                mStartBtn.setEnabled(true);
                mStartBtn.setBackgroundResource(R.drawable.synplay);
            }

            if (mCancelBtn != null) {
                mCancelBtn.setEnabled(true);
                mCancelBtn.setBackgroundResource(R.drawable.recogcancel);
            }
            setWords(true);
        }

        if (state == 3) {
            if (mStartBtn != null) {
                mStartBtn.setEnabled(true);
                mStartBtn.setBackgroundResource(R.drawable.synpause);
            }

            if (mCancelBtn != null) {
                mCancelBtn.setEnabled(true);
                mCancelBtn.setBackgroundResource(R.drawable.recogcancel);
            }
            setWords(false);
        }

        if (state == 4) {
            if (mStartBtn != null) {
                mStartBtn.setEnabled(true);
                mStartBtn.setBackgroundResource(R.drawable.synplay);
            }
            setWords(false);
        }
    }

    // set the UI of 'mWords'
    public void setWords(boolean enabled) {
        if (mWords == null)
            return;

        if (enabled == true) {
            mWords.setFocusableInTouchMode(true);
            mWords.setFocusable(true);
            mWords.setTextColor(Color.rgb(255, 255, 255));
        } else {
            mWords.setFocusable(false);
            mWords.setTextColor(Color.rgb(200, 200, 200));
        }
    }

    // 处理音频播放完成
    @Override
    public void onCompletion(MediaPlayer arg) {
        Log.d("onCompletion", "Thread: " + android.os.Process.myTid()
                + " name: " + Thread.currentThread().getName());
        mState = 2;
        updateBtn(mState);
    }
}