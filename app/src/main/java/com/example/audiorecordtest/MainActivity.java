package com.example.audiorecordtest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import android.os.Handler;
import java.util.logging.LogRecord;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends  Activity {

	private static final String LOG_TAG = "AudioRecordTest";
	private static String mFileName = null;

//	private RecordButton mRecordButton = null;
	private MediaRecorder mRecorder = null;

//	private PlayButton mPlayButton = null;
	private MediaPlayer mPlayer = null;

	boolean mStartRecording = true;
	boolean mStartPlaying = true;

	private long startTime = 0L;
	private long stopTime = 0L;

	private Handler customHandler = new Handler();

	long timeInMilliseconds = 0L;
	long timeSwapBuff = 0L;
	long updatedTime = 0L;

	private TextView statusTime;
	private TextView statusText;

	// データフォーマット
	private SimpleDateFormat sdf1;
	private SimpleDateFormat sdf2;




	private void onRecord(boolean start){
		if(start){
			startRecording();
		}else{
			stopRecording();
		}
	}

	private void onPlay(boolean start){
		if(start){
			startPlaying();
		}else{
			stopPlaying();
		}
	}

	private void startPlaying(){
		mPlayer = new MediaPlayer();
		try{
			mPlayer.setDataSource(mFileName);
			mPlayer.prepare();
			mPlayer.start();
		}catch(IOException e){
			Log.e(LOG_TAG, "prepare() failed");
		}
	}

	private void stopPlaying() {
		mPlayer.release();
		mPlayer = null;
	}

	private void startRecording(){
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(mFileName);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		try {
			mRecorder.prepare();
		}catch(IOException e){
			Log.e(LOG_TAG, "prepare() failed");
		}
		mRecorder.start();
	}

	private void stopRecording(){
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
	}

	// 録音のタイマー
	private void recordTimer(){
		if(!mStartRecording) {

		}else{

		}
	}

	// タイマー用スレッド
	private Runnable updateTimerThread = new Runnable() {
		@Override
		public void run() {
			timeInMilliseconds = SystemClock.uptimeMillis()-startTime;
			updatedTime = timeSwapBuff + timeInMilliseconds;

			int secs = (int)(updatedTime/1000);
			int mins = secs/60;
			secs = secs%60;
			int milliseconds = (int)(updatedTime%1000);
			statusTime.setText("" + mins + ":"
					+ String.format("%02d", secs) + ":"
					+ String.format("%03d", milliseconds));
			customHandler.postDelayed(this, 0);
		}
	};

	public MainActivity() {
		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		mFileName += "/audiorecordtest.3gp";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// タイマーのデータフォーマットを指定
		sdf1 = new SimpleDateFormat("HH:mm:ss SSS");
		sdf2 = new SimpleDateFormat("mm:ss SSS");

		initButton();

		// 録音ボタン
		final Button recordButton = (Button)findViewById(R.id.recordbutton);
		statusTime = (TextView)findViewById(R.id.statustime);
		statusText = (TextView)findViewById(R.id.statustext);
		recordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onRecord(mStartRecording);
				Resources res = getResources();
				int color;
				if (timeSwapBuff != 0) timeSwapBuff = 0; // タイマーリセット
				if (mStartRecording) {
					color = res.getColor(R.color.on);
					recordButton.setBackgroundColor(color);
					recordButton.setText("TOUCH STOP");
					statusTime.setText("00:00:00 000");
					statusText.setText("NOW RECORDING");
					// タイマー処理
					startTime = SystemClock.uptimeMillis();
					customHandler.postDelayed(updateTimerThread, 0);
				} else {
					color = res.getColor(R.color.main);
					recordButton.setBackgroundColor(color);
					recordButton.setText("RECORD");
					statusText.setText("");
					// タイマー処理
					timeSwapBuff += timeInMilliseconds;
					customHandler.removeCallbacks(updateTimerThread);
				}
				mStartRecording = !mStartRecording;
			}
		});

		// 再生ボタン
		final Button playButton = (Button)findViewById(R.id.playbutton);
		playButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onPlay(mStartPlaying);
				Resources res = getResources();
				int color;
				if(mStartPlaying){
					color = res.getColor(R.color.on);
					playButton.setBackgroundColor(color);
					playButton.setText("TOUCH STOP");
					statusTime.setText("00:00:00 000");
					statusText.setText("NOW PLAYING");
				}else{
					color = res.getColor(R.color.main);
					playButton.setBackgroundColor(color);
					playButton.setText("PLAY");
					statusTime.setText("STATUS");
				}
				mStartPlaying = !mStartPlaying;
			}
		});
	}

	// ScrollView にボタンを追加
	private void initButton(){
		LinearLayout linearLayout = (LinearLayout)findViewById(R.id.linearscrollview);
		ScrollView scrollView = (ScrollView)findViewById(R.id.scrollview);
		for(int i=0; i<10; i++) {
			Button button = new Button(this);
			button.setText("履歴"+(i+1));
			// ボタンの背景色
			//button.setBackgroundColor(Color.WHITE);
			linearLayout.addView(button);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if(mRecorder != null){
			mRecorder.release();
			mRecorder = null;
		}
		if(mPlayer != null){
			mPlayer.release();
			mPlayer = null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
