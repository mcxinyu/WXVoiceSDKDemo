package com.qq.wx.voice.demo;

import java.io.FileInputStream;
import java.io.IOException;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public class Player {
	private MediaPlayer mediaPlayer = new MediaPlayer();

	public void init(OnCompletionListener listener) {
		mediaPlayer.setOnCompletionListener(listener);
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	}

	public boolean playFile(String videoFile) {
		try {
			mediaPlayer.reset();
			FileInputStream fis = new FileInputStream(videoFile);
			mediaPlayer.setDataSource(fis.getFD());
			fis.close();
			mediaPlayer.prepare();
			return play();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public boolean play() {
		try {
			mediaPlayer.start();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean pause() {
		try {
			mediaPlayer.pause();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean stop() {
		try {
			mediaPlayer.stop();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void release() {
		mediaPlayer.release();
	}

	public boolean isPlaying() {
		return mediaPlayer.isPlaying();
	}
}
