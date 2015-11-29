package com.example.audiorecordtest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.app.Activity;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import de.timroes.android.listview.EnhancedListView;

public class MainActivity extends  Activity {

	private static final String LOG_TAG = "AudioRecordTest";
	private static String mFileName = null;
//	private static final String AUDIO_RECORDER_FOLDER = "/DCIM/AudioRecorder";
//	private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";

	private static int COUNTER = 0; // ボタン用カウンター

	private MediaRecorder mRecorder = null;
	private MediaPlayer mPlayer = null;

	boolean mStartRecording = true;
	boolean mStartPlaying = true;

	private long startTime = 0L;

	private Handler customHandler = new Handler();

	long timeInMilliseconds = 0L;
	long timeSwapBuff = 0L;
	long updatedTime = 0L;

	private String tmpFileName;

	private TextView statusTime; // ステータスの時間表示
	private TextView statusText; // ステータスの情報表示
	private TextView statusLastTime; // ステータスのファイル時間表示

	private Button recordButton;
	private Button playButton;

	private File filepathname; // DBからファイル名を読む時に使う

//    private String newFileName;

	// DB用
//	SQLiteEx sql;

    // テスト用 スワイプ対応リストビュー
    // リストビュー
    private EnhancedListView listView;

    // テスト用 フォルダ内のファイル一覧表示
	private File[] files;
	//private List<String> voiceList = new ArrayList<String>();
	//private ListView listView;
    // アダプター
    private ArrayAdapter<String> adapter;

    // リストビューに設定するリストとアダプター
    private LinkedList<String> voiceList = new LinkedList<String>();

////// 録音ボタンのフラグ //////////////////////////////////////
	private void onRecord(boolean start){
		if(start){
			startRecording();
		}else{
			stopRecording();
		}
	}
////// 再生ボタンのフラグ /////////////////////////////////////
	private void onPlay(boolean start, String filepath){
		if(start){
			startPlaying(filepath);
		}else{
			stopPlaying();
		}
	}
////// 再生処理 //////////////////////////////////////////////
	private void startPlaying(String filepath){
		mPlayer = new MediaPlayer();
		try{
            mFileName = filepathname.getAbsolutePath();
//            mFileName += "/audiorecordtest"+COUNTER+".3gp";
			// 再生ファイルの指定？
			mPlayer.setDataSource(mFileName+"/"+filepath);
			// 再生準備
			mPlayer.prepare();
			// 再生開始
			mPlayer.start();
		}catch(IOException e){
			Log.e(LOG_TAG, "prepare() failed");
		}
	}
////// 再生停止 ///////////////////////////////////////////////
	private void stopPlaying() {
		// オブジェクトを解放
		mPlayer.release();
		mPlayer = null;
	}
////// 録音開始 ///////////////////////////////////////////////
	private void startRecording(){
		mRecorder = new MediaRecorder();
		// 入力ソースの指定
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		// フォーマット、コーデックの指定
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); // 出力フォーマット
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); // オーディオのエンコーダ
        mFileName = filepathname.getAbsolutePath();
        mFileName += "/audiorecordtest"+COUNTER+".3gp";
		// 保存先の指定
		mRecorder.setOutputFile(mFileName);

		try {
			// 録音準備
			mRecorder.prepare();
		}catch(IOException e){
			Log.e(LOG_TAG, "prepare() failed");
		}
		// 録音開始
		mRecorder.start();
	}
////// 録音停止 ////////////////////////////////////////////////
	private void stopRecording(){
		mRecorder.stop();
		// オブジェクトを解放
		mRecorder.release();
		mRecorder = null;
    }
////// ファイル名入力ダイアログ ///////////////////////////////////

    //テキスト入力を受け付けるダイアログを作成します。
    private void textViewDialog() {
        final EditText editView = new EditText(MainActivity.this);
        new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("保存する名前を入力してください")
                //setViewにてビューを設定します。
                .setView(editView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Fileオブジェクトの生成
                        File oldFile = new File(filepathname, "audiorecordtest"+COUNTER+".3gp");

                        // ファイルが存在するか判定
                        if(oldFile.exists()){
                            // エディットテキストからファイル名を取得
                            String str = editView.getText().toString()+".3gp";
                            // 親ディレクトリまでのパスとファイル名を指定する
                            File newFile = new File(filepathname, str);
                            // リネーム booleanを返す
                            oldFile.renameTo(newFile);
                            //入力した文字をトースト出力する
                            Toast.makeText(MainActivity.this,
                                    editView.getText().toString()+":old:"+oldFile+":new:"+newFile,
                                    Toast.LENGTH_LONG).show();
                            // 再描画？
                            voiceList.add(str);
                            COUNTER = voiceList.size(); // 保険
                            adapter.notifyDataSetChanged();
                        }
                    }
                })
                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }
////// タイマー用スレッド ////////////////////////////////////////
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

////// コンストラクタ /////////////////////////////////////////////
	public MainActivity() {
		// 内部ストレージのパス情報を保持したFileインスタンスを取得
		filepathname = Environment.getExternalStorageDirectory();
		// 絶対パスを取得
//		mFileName = filepathname.getAbsolutePath();
//		mFileName += "/audiorecordtest"+COUNTER+".3gp";
	}

////// ステータスに情報を表示 //////////////////////////////////////
	public void showStatus(String str) {
        statusText = (TextView)findViewById(R.id.statustext);
		statusText.setText(str);
	}

// onCreate /////////////////////////////////////////////////////
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		recordButton = (Button) findViewById(R.id.recordbutton);
		playButton = (Button) findViewById(R.id.playbutton);
		// タイマーのデータフォーマットを指定
//		sdf1 = new SimpleDateFormat("HH:mm:ss SSS");
//		sdf2 = new SimpleDateFormat("mm:ss SSS");

////////// フォルダ内のファイル名一覧表示 ///////////////////////////////////////
		String sdPath = Environment.getExternalStorageDirectory().getPath();
//		files = new File(sdPath).listFiles();
//		if (files != null) {
//			for (int i = 0; i < files.length; i++) {
//				if (files[i].isFile() && files[i].getName().endsWith(".3gp")) {
//					voiceList.add(files[i].getName());
//				}
//			}
//            COUNTER = voiceList.size();
//			listView = (ListView) findViewById(R.id.listview);
//			adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, voiceList);
//			listView.setAdapter(adapter);
//            // 録音履歴リストのクリック時の処理
//			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//				@Override
//				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//					ListView list = (ListView) parent;
//					String item = (String) list.getItemAtPosition(position);
//					showStatus(item);
//                    tmpFileName = item;
//				}
//			});
//		}
        files = new File(sdPath).listFiles();
        if(files != null) {
            for (int i = 0; i < files.length; i++) {
				if (files[i].isFile() && files[i].getName().endsWith(".3gp")) {
                    // リストビューにアイテムを追加
					voiceList.add(files[i].getName());
				}
			}
            // リストビューのアイテム数を取得してカウンターに保存
            COUNTER = voiceList.size();
            // エンハンスリストビュー
			listView = (EnhancedListView) findViewById(R.id.listview);
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, voiceList);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					ListView list = (ListView) parent;
					String item = (String) list.getItemAtPosition(position);
					showStatus(item);
                    tmpFileName = item;
				}
			});

            // スワイプで消す設定
            listView.setDismissCallback(new de.timroes.android.listview.EnhancedListView.OnDismissCallback(){
                @Override
                public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position){
                    final String item = (String) adapter.getItem(position);
                    // Fileオブジェクトの生成
                    final File removeFile = new File(filepathname, item);
                    // ファイルが存在するか判定
                    if(removeFile.exists()){
                        // リネーム booleanを返す
                        removeFile.delete();
                        // 再描画？
//                        voiceList.add(str);
//                        COUNTER = voiceList.size(); // 保険
                        adapter.notifyDataSetChanged();
                    }
                    /// 消す処理
                    voiceList.remove(position);
                    adapter.notifyDataSetChanged();
                    // アイテム復帰処理
                    return new EnhancedListView.Undoable() {
                        @Override
                        public void undo() {
                            // 元に戻す処理
                            voiceList.add(position, item);
                            adapter.notifyDataSetChanged();
                        }
                    };
                }
            });
            listView.enableSwipeToDismiss();

        }

		//SQLインスタンス化
//		sql = new SQLiteEx(this);

		statusTime = (TextView)findViewById(R.id.statustime);
		statusText = (TextView)findViewById(R.id.statustext);

        // 録音ボタンクリック時の処理
		recordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				// 録音開始
				onRecord(mStartRecording);
				Resources res = getResources();
				int color;
				if (timeSwapBuff != 0) timeSwapBuff = 0; // タイマーリセット
				if (mStartRecording) {

					// ボタンを追加して録音開始
//					initButton();

					color = res.getColor(R.color.on);
					recordButton.setBackgroundColor(color);
					recordButton.setText("TOUCH STOP");
					statusTime.setText("00:00:00 000");
					statusText.setText("NOW RECORDING");
					// 再生ボタンを無効化して色を変更
					playButton.setEnabled(false);
					res = playButton.getResources();
					color = res.getColor(R.color.off);
					playButton.setBackgroundColor(color);
					// タイマー処理
					startTime = SystemClock.uptimeMillis();
					customHandler.postDelayed(updateTimerThread, 0);
				} else {
					color = res.getColor(R.color.main);
					recordButton.setBackgroundColor(color);
					recordButton.setText("RECORD");
					statusText.setText("");
					// 再生ボタンを有効化して色を変更
					playButton.setEnabled(true);
					res = playButton.getResources();
					color = res.getColor(R.color.main);
					playButton.setBackgroundColor(color);
					// タイマー処理
					timeSwapBuff += timeInMilliseconds;
					customHandler.removeCallbacks(updateTimerThread);
                    textViewDialog();
				}
				mStartRecording = !mStartRecording;
            }
		});

		// 再生ボタン
		statusTime = (TextView)findViewById(R.id.statustime);
		statusLastTime = (TextView)findViewById(R.id.statuslasttime);
		statusText = (TextView)findViewById(R.id.statustext);

		// 再生ボタンクリック時の処理
		playButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

//				// 再生開始
				onPlay(mStartPlaying, tmpFileName);

				Resources res = getResources();
				int color;
				long lasttime = timeSwapBuff;
				if (timeSwapBuff != 0) timeSwapBuff = 0; // タイマーリセット
				if(mStartPlaying){
					color = res.getColor(R.color.on);
					playButton.setBackgroundColor(color);
					playButton.setText("TOUCH STOP");
					statusTime.setText("00:00:00 000");

					statusText.setText("NOW PLAYING");

					// 再生ファイルの長さを表示
					int secs = (int)(lasttime/1000);
					int mins = secs/60;
					secs = secs%60;
					int milliseconds = (int)(lasttime%1000);
					statusLastTime.setText("" + mins + ":"
							+ String.format("%02d", secs) + ":"
							+ String.format("%03d", milliseconds));

					// 録音ボタンを無効化して色を変更
					recordButton.setEnabled(false);
					res = recordButton.getResources();
					color = res.getColor(R.color.off);
					recordButton.setBackgroundColor(color);

					// タイマー処理
					startTime = SystemClock.uptimeMillis();
					customHandler.postDelayed(updateTimerThread, 0);
				}else{
					color = res.getColor(R.color.main);
					playButton.setBackgroundColor(color);
					playButton.setText("PLAY");
					statusTime.setText("STATUS");
					statusText.setText("");
					statusLastTime.setText("");
					// 録音ボタンを有効化して色を変更
					recordButton.setEnabled(true);
					res = recordButton.getResources();
					color = res.getColor(R.color.main);
					recordButton.setBackgroundColor(color);
					// タイマー処理
					timeSwapBuff += timeInMilliseconds;
					customHandler.removeCallbacks(updateTimerThread);
				}
				mStartPlaying = !mStartPlaying;
			}
		});
	}

////// ScrollView にボタンを追加 ///////////////////////////////////////
//	private void initButton(){
//		LinearLayout linearLayout = (LinearLayout)findViewById(R.id.linearscrollview);
//
//		Button button = new Button(this);
//		// ボタンのIDにCOUNTERの値をセット
//		button.setId(COUNTER);
//		// ボタンのIDを取得
//		int buttonId = button.getId();
//		String filepath = "/audiorecordtest"+buttonId+".3gp";
//		try {
//			// DBへIDの書き込み
//			sql.write_id(buttonId);
//			// DBへファイル名の書き込み
//			sql.write_filename(filepath);
//			// DBへ日付の書き込み
//			sql.write_date("");
//			onRecord(mStartRecording,filepath);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		statusText = (TextView)findViewById(R.id.statustext);
//		statusText.setText("button.getId():" + buttonId);
//		button.setText(mFileName+"/storage/emulated/0/audiorecordtest"+buttonId+".3gp");
//
//		button.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// ビュー(ボタン)のIDを取得
//				int viewId = v.getId();
//				try {
//					if(viewId==sql.load_id(viewId)){
//						String filepath = sql.load_filename(viewId);
////						startPlaying(mFileName+filename);
//						// ステータス(ファイル名)を表示
//						statusText = (TextView) findViewById(R.id.statustext);
//						statusText.setText(mFileName + filepath);
//						tmpFileName = filepath;
////						onPlay(mStartPlaying,filepath);
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//		// ボタンの背景色
//		//button.setBackgroundColor(Color.WHITE);
//		linearLayout.addView(button);
//	}

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
    protected void onDestroy() {
        super.onDestroy();
        // 使わなくなった時点でレコーダーリソースを解放
        mRecorder.release();
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
