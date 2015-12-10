package com.example.audiorecordtest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.LinkedList;

import de.timroes.android.listview.EnhancedListView;

public class MainActivity extends ActionBarActivity {

	private static final String LOG_TAG = "AudioRecordTest";
	protected static String mFileName = null;
//	private static final String AUDIO_RECORDER_FOLDER = "/DCIM/AudioRecorder";
//	private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";

	protected static int COUNTER = 0; // ボタン用カウンター

	private MediaRecorder mRecorder = null;
	private MediaPlayer mPlayer = null;
//	private SoundPool soundPool = null;

	static boolean mStartRecording = true;
	static boolean mStartPlaying = true;

	protected static long startTime = 0L;

	private Handler customHandler = new Handler();

	private Drawable buttonStyle;
	private Resources res;
	private int color;


	// ミリ秒
	protected static long timeInMilliseconds = 0L;
	protected static long timeSwapBuff = 0L;
	protected static long updatedTime = 0L;

	private String tmpFileName;

	protected static TextView statusTime; // ステータスの時間表示
	protected static TextView statusText; // ステータスの情報表示
	protected static TextView statusLastTime; // ステータスのファイル時間表示

	protected static Button recordButton;
	protected static Button playButton;
//	private Button speedButton;

	protected static int fileLong;

	protected static File absolutePathFile; // DBからファイル名を読む時に使う

//	private float playbackSpeed = 1.3f; // 1.3倍速再生

//	private int soundId;

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
//			startRecording();
            Intent intent = new Intent(this, MyService.class);
            // StringをActionに設定する
            intent.setAction("ACTION_RECORD");
            // COUNTERをキーにCOUNTERの値をセットする
            intent.putExtra("COUNTER",COUNTER);
			intent.putExtra("ABSOLUTE_PATH", absolutePathFile.toString());
            startService(intent);
		}else{
//			stopRecording();
            Intent intent = new Intent(this, MyService.class);
            intent.setAction("ACTION_RECORD_STOP");
            startService(intent);
		}
	}
////// 再生ボタンのフラグ /////////////////////////////////////
	private void onPlay(boolean start, String filepath){
		if(start){
//			startPlaying(filepath);
            Intent intent = new Intent(this, MyService.class);
            intent.setAction("ACTION_PLAY");
            intent.putExtra("FILE_NAME", filepath);
			intent.putExtra("ABSOLUTE_PATH", absolutePathFile.toString());
            startService(intent);
		}else{
//			stopPlaying();
            Intent intent = new Intent(this, MyService.class);
            intent.setAction("ACTION_STOP");
            startService(intent);
		}
	}

//////// 倍速ボタンのフラグ //////////////////////////////////////
//	private void onSpeedPlay(boolean start, String filepath){
//		if(start){
//			startSpeedPlaying(filepath);
//		}else{
//			stopSpeedPlaying();
//		}
//	}
//////// 再生処理 //////////////////////////////////////////////
//	private void startPlaying(String filepath){
//		mPlayer = new MediaPlayer();
//		try{
//            mFileName = filepathname.getAbsolutePath();
////            mFileName += "/audiorecordtest"+COUNTER+".3gp";
//			// 再生ファイルの指定？
//			mPlayer.setDataSource(mFileName+"/"+filepath);
//            // アイドル時も動作させ続けることを保証
//            mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
//			// 再生準備
//			mPlayer.prepare();
////			// 再生開始
////			mPlayer.start();
//		}catch(IOException e){
//			Log.e(LOG_TAG, "prepare() failed");
//		}
//		mPlayer.start();
//	}
//////// 倍速再生処理 ////////////////////////////////////////////
//	private void startSpeedPlaying(String filepath){
//		// SoundPool(プールする最大数 , Streamタイプ , サンプリングレートのクオリティ)
//		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
//		// int(id) SoundPool.load(Context , int リソースID , int priority(互換性のため1))
//		// ローカルファイルの場合、 load(String path, int priority)
//		soundId = soundPool.load(Environment.getExternalStorageDirectory()+"/"+filepath, 1);
//		AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
//		final float volume = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//		// setOnLoadCompleteListener() 完了通知を受け取るためのリスナー (API8)
//		soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
//			@Override
//			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
//				// SoundPool.play(再生ファイルload時の戻り値 , leftVolume , rightVolume(0.0~1.0) , プライオリティ , ループ回数(-1:無限 0:しない) , 再生速度0.5~2.0)
//				soundPool.play(soundId, volume, volume, 1, 0, playbackSpeed);
//			}
//		});
//	}
//////// 再生停止 ///////////////////////////////////////////////
//	private void stopPlaying() {
//		// 再生停止
//		mPlayer.stop();
//		// オブジェクトを解放
//		mPlayer.release();
//		mPlayer = null;
//	}
////// 倍速再生停止 ///////////////////////////////////////////////
//	private void stopSpeedPlaying() {
//		// ファイルをメモリから解放
//		soundPool.release();
//		soundPool = null;
//	}
////// 録音開始 ///////////////////////////////////////////////
//	private void startRecording(){
//		mRecorder = new MediaRecorder();
//		// 入力ソースの指定
//		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//		// フォーマット、コーデックの指定
//		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); // 出力フォーマット
//		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); // オーディオのエンコーダ
//        mFileName = filepathname.getAbsolutePath();
//        mFileName += "/audiorecordtest"+COUNTER+".3gp";
//		// 保存先の指定
//		mRecorder.setOutputFile(mFileName);
//
//		try {
//			// 録音準備
//			mRecorder.prepare();
//		}catch(IOException e){
//			Log.e(LOG_TAG, "prepare() failed");
//		}
//		// 録音開始
//		mRecorder.start();
//	}
//////// 録音停止 ////////////////////////////////////////////////
//	private void stopRecording(){
//		mRecorder.stop();
//		// オブジェクトを解放
//		mRecorder.release();
//		mRecorder = null;
//    }

//// ファイル名入力ダイアログ ///////////////////////////////////

    //テキスト入力を受け付けるダイアログを作成します。
    private void textViewDialog() {
		// ダイアログのテキストビューを作成
		final EditText editView = new EditText(MainActivity.this);
		// 日付を自動挿入
//		long t = System.currentTimeMillis();
		// やり方が色々あります
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd, E, HH:mm:ss", Locale.ENGLISH);
//		String text = sdf.format(Calendar.getInstance().getTime());
		// 以下、変更
//		Time time = new Time("Asia/Tokyo");
//		time.setToNow();
//		String date =
//				time.year+"/"+(time.month+1)+"/"+time.monthDay+"/ "
//				+time.hour+":"+time.minute;//+":"+time.second;
		// 日付をセット 日本語はkk:mm
		// スラッシュ( / )を指定するとパスと混同してうまくいかない
		editView.setText(DateFormat.format("yyyy年MM月dd日 (E) kk:mm:ss", Calendar.getInstance()).toString());
        new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("保存する名前を入力してください")
                //setViewにてビューを設定します。
                .setView(editView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Fileオブジェクトの生成
						File oldFile = new File(absolutePathFile, "audiorecordtest" + COUNTER + ".3gp");

						// ファイルが存在するか判定
						if (oldFile.exists()) {
							// 数値を(文字列に変換して)TextViewに出力する場合には toString()が必要
							// エディットテキストからファイル名を取得 CharSequence
							String str = editView.getText().toString() + ".3gp";
							// 親ディレクトリまでのパスとファイル名を指定する
							File newFile = new File(absolutePathFile, str);
							// リネーム booleanを返す
							oldFile.renameTo(newFile);
							// トースト出力する
							Toast.makeText(MainActivity.this,
                                    "保存しましたCOUNTER:"+COUNTER,
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
//////// タイマー用スレッド ////////////////////////////////////////
//	private Runnable updateTimerThread = new Runnable() {
//		@Override
//		public void run() {
//			timeInMilliseconds = SystemClock.uptimeMillis()-startTime;
//			updatedTime = timeSwapBuff + timeInMilliseconds;
//
//			int secs = (int)(updatedTime/1000);
//			int mins = secs/60;
//			secs = secs%60;
//			int milliseconds = (int)(updatedTime%1000);
//			statusTime.setText("" + mins + "分"
//					+ String.format("%02d", secs) + "秒"
//					+ String.format("%03d", milliseconds));
//			customHandler.postDelayed(this, 0);
//
//			// プレイヤーの自動停止
//			if(!mStartPlaying && updatedTime>=fileLong){
//				// タイマー処理
//				timeSwapBuff += timeInMilliseconds;
//				customHandler.removeCallbacks(updateTimerThread);
//				mStartPlaying=true;
//				Resources res = getResources();
//				int color = res.getColor(R.color.main);
//				playButton.setBackgroundColor(color);
//				recordButton.setBackgroundColor(color);
////				speedButton.setBackgroundColor(color);
//				playButton.setEnabled(true);
//				recordButton.setEnabled(true);
////				speedButton.setEnabled(true);
//				playButton.setText("REPLAY");
//				statusText.setText("FINISHED STOP");
////				speedButton.setText("倍速 REPLAY");
//			}
//		}
//	};

////// コンストラクタ /////////////////////////////////////////////
	public MainActivity() {
		// 内部ストレージのパス情報を保持したFileインスタンスを取得
//		filepathname = new File(Environment.getExternalStorageDirectory().getPath());
//		if(!filepathname.exists()){
//            filepathname.mkdir();
//        }
//		absolutePathFile = new File(Environment.getDataDirectory().getPath()+"/audiorecordtest/");
//		absolutePathFile = new File("/storage/sdcard/audiorecordtest/");
//		filepathname = new File(Environment.getFilesDir().getPaht());
		absolutePathFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "audiorecordtest");
		if(! absolutePathFile.exists()){
			if(! absolutePathFile.mkdirs()) {
				Log.d("audiorecordtest", "failed to create directoryだよ");
			}
		}

		// 絶対パスを取得
//		mFileName = filepathname.getAbsolutePath();
//		mFileName += "/audiorecordtest"+COUNTER+".3gp";
	}

////// メディアファイルの再生時間を取得 ///////////////////////////////////////////
	public static int getDuration(File audioFile){
		MediaPlayer mp = new MediaPlayer();
		FileInputStream fs;
		FileDescriptor fd;
		int length = 0;
		try {
			fs = new FileInputStream(audioFile);
			fd = fs.getFD();
			mp.setDataSource(fd);
			mp.prepare();
			length = mp.getDuration();
			mp.release();
		}catch (IOException e){
			e.printStackTrace();
		}
		return length;
	}
////// ファイルサイズを取得 ///////////////////////////////////////
    public int getFileSize(File file){
        if(!file.isFile()){
            return 0;
        }
        int size = 0;
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            // File.length()はlongを返し,FileInputStream.available()はintを返す
            // 大きいサイズの時はlength()を使う方がいい
            size = stream.available();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(stream != null){
                try{
                    stream.close();
                }catch (IOException e){}
            }
        }
        return size;
    }
////// ファイルサイズ単位変換 //////////////////////////////////////////
    private String getSizeStr(int size){
        if(1024 > size){
            return size + " Byte";
        }else if(1024 * 1024 > size){
            double dsize = size;
            dsize = dsize / 1024;
            BigDecimal bi = new BigDecimal(String.valueOf(dsize));
            double value = bi.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            return value + " KB";
        }else{
            double dsize = size;
            dsize = dsize / 1024 / 1024;
            BigDecimal bi = new BigDecimal(String.valueOf(dsize));
            double value = bi.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            return value + " MB";
        }
    }
////// ボタンの色を変更する ////////////////////////////////////////
	public void buttonColorChange(){
		Resources res = getResources();
		int color;
		// 再生
		if(mStartPlaying == false && mStartRecording == true){
			color = res.getColor(R.color.on);
			playButton.setBackgroundColor(color);
			color = res.getColor(R.color.off);
			recordButton.setBackgroundColor(color);
			playButton.setText("TOUCH STOP");
			statusTime.setText("00分00秒000");
			statusText.setText("NOW PLAYING");
			recordButton.setEnabled(false);

		// 停止
		}else if(mStartPlaying == true && mStartRecording == true){
			color = res.getColor(R.color.main);
			playButton.setBackgroundColor(color);
			recordButton.setBackgroundColor(color);
			playButton.setText("PLAY");
			recordButton.setText("RECORD");
			statusText.setText("");
			statusLastTime.setText("");
			playButton.setEnabled(true);
			recordButton.setEnabled(true);

		// 録音
		}else if(mStartPlaying == true && mStartRecording == false){
			color = res.getColor(R.color.off);
			playButton.setBackgroundColor(color);
			color = res.getColor(R.color.on);
			recordButton.setBackgroundColor(color);
			playButton.setText("PLAY");
			recordButton.setText("TOUCH STOP");
			statusText.setText("NOW RECORDING");
			statusLastTime.setText("");
			playButton.setEnabled(false);
		}
	}

////// ステータスに情報を表示 //////////////////////////////////////
	public void showStatus(File mediaFile, String item) {
        statusText = (TextView)findViewById(R.id.statustext);
		statusTime = (TextView)findViewById(R.id.statustime);
		statusLastTime = (TextView)findViewById(R.id.statuslasttime);
		playButton = (Button) findViewById(R.id.playbutton);
        // 長さを取得
        fileLong = getDuration(mediaFile);
        // ファイルサイズを取得
        int size = getFileSize(mediaFile);
        // ファイルサイズを変換
        String sizeStr = getSizeStr(size);
		Resources res = getResources();
		int color = res.getColor(R.color.main);
		playButton.setBackgroundColor(color);
		playButton.setEnabled(true);
//		speedButton.setBackgroundColor(color);
//		speedButton.setEnabled(true);
		statusText.setText(item);
		statusTime.setText(""+sizeStr);
		playButton.setText("PLAY");

		// 再生ファイルの長さを表示
		int secs = (int)(fileLong/1000);
		int mins = secs/60;
		int hours = mins/60;
		secs = secs%60;
		int milliseconds = (int)(fileLong%1000);
		statusLastTime.setText(""+mins + "分"
				+ String.format("%02d", secs) + "秒"
				+ String.format("%03d", milliseconds));
	}

	// サービスから値を受け取ったら動かしたい内容を書く
	private Handler updateHandler = new Handler(){
		@Override
	public void handleMessage(Message msg){
			Bundle bundle = msg.getData();
			String message = bundle.getString("ハンドラーmessage");

			Log.d("MainActivity","ハンドラー"+message);
			statusLastTime.setText(message);
		}
	};


// onCreate /////////////////////////////////////////////////////
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		recordButton = (Button) findViewById(R.id.recordbutton);
		playButton = (Button) findViewById(R.id.playbutton);
//		speedButton = (Button)findViewById(R.id.speedbutton);
		res = getResources();
		color = res.getColor(R.color.off);
		playButton.setBackgroundColor(color);
		playButton.setEnabled(false);
//		speedButton.setBackgroundColor(color);
//		speedButton.setEnabled(false);
		// タイマーのデータフォーマットを指定
//		sdf1 = new SimpleDateFormat("HH:mm:ss SSS");
//		sdf2 = new SimpleDateFormat("mm:ss SSS");

////////// フォルダ内のファイル名一覧表示 ///////////////////////////////////////

		String sdPath = absolutePathFile.getAbsolutePath().toString();
//		String sdPath = Environment.getExternalStorageDirectory().getPath();
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
					// 絶対パスでアイテムを追加
//					voiceList.add(files[i].toString());
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
					File mediaFile = new File(absolutePathFile, item);
					showStatus(mediaFile,item);
                    tmpFileName = item;
				}
			});

            // スワイプで消す設定
            listView.setDismissCallback(new de.timroes.android.listview.EnhancedListView.OnDismissCallback(){
                @Override
                public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position){
                    final String item = (String) adapter.getItem(position);
                    // Fileオブジェクトの生成
                    final File removeFile = new File(absolutePathFile, item);
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
		statusLastTime = (TextView)findViewById(R.id.statuslasttime);

        // 録音ボタンクリック時の処理
		recordButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				// 録音開始
				onRecord(mStartRecording);
//				if (timeSwapBuff != 0) timeSwapBuff = 0; // タイマーリセット
				if (!mStartRecording)  textViewDialog();
				mStartRecording = !mStartRecording;
				buttonColorChange();
            }
		});

		// 再生ボタンクリック時の処理
		playButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				// 再生開始
				onPlay(mStartPlaying, tmpFileName);
				mStartPlaying = !mStartPlaying;
				buttonColorChange();
			}
		});

//		// 倍速ボタン
//		speedButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				onSpeedPlay(mStartPlaying, tmpFileName);
//
//				Resources res = getResources();
//				int color;
//				if (timeSwapBuff != 0) timeSwapBuff = 0; // タイマーリセット
//				if(mStartPlaying){
//					color = res.getColor(R.color.on);
//					speedButton.setBackgroundColor(color);
//					speedButton.setText("TOUCH STOP");
//					statusTime.setText("00分00秒000");
//					statusText.setText("NOW PLAYING");
//
//					// 録音ボタンを無効化して色を変更
//					recordButton.setEnabled(false);
//					res = recordButton.getResources();
//					color = res.getColor(R.color.off);
//					recordButton.setBackgroundColor(color);
//
//					// タイマー処理
//					startTime = SystemClock.uptimeMillis();
//					customHandler.postDelayed(updateTimerThread, 0);
//				}else{
//					color = res.getColor(R.color.main);
//					speedButton.setBackgroundColor(color);
//					speedButton.setText("PLAY");
//					statusText.setText("");
//					// 録音ボタンを有効化して色を変更
//					recordButton.setEnabled(true);
//					res = recordButton.getResources();
//					color = res.getColor(R.color.main);
//					recordButton.setBackgroundColor(color);
//					// タイマー処理
//					timeSwapBuff += timeInMilliseconds;
//					customHandler.removeCallbacks(updateTimerThread);
//				}
//				mStartPlaying = !mStartPlaying;
//			}
//		});
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

//	@Override
//	public void onPause() {
//		super.onPause();
//		if(mRecorder != null){
//			mRecorder.release();
//			mRecorder = null;
//		}
//		if(mPlayer != null){
////			mPlayer.pause();
//			mPlayer.release();
//			mPlayer = null;
//		}
//		if(soundPool != null){
//			soundPool.release();
//			soundPool = null;
//		}
//	}

//    @Override
//    protected void onResume(){
//        super.onResume();
//        // バックグラウンド再生を許可する
//        requestVisibleBehind(true);
//    }
//
//    public boolean onDestroy(MediaPlayer mp, int what, int extra){
//        // デバッグ情報
//        Log.d(LOG_TAG, "onDestroy");
//        if(mp != null){
//            mp.release();
//            mp = null;
//        }
//        return false;
//    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        // 使わなくなった時点でレコーダーリソースを解放
//		if(mRecorder != null){
//			mRecorder.release();
//			mRecorder = null;
//		}
//		if(mPlayer != null){
//			mPlayer.stop();
//			mPlayer.release();
//			mPlayer = null;
//		}
////		if(soundPool != null){
////			soundPool.release();
////			soundPool = null;
////		}
//    }

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

    // イヤホンからPLAY,PAUSEを操作する
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
//            case KeyEvent.KEYCODE_HEADSETHOOK:
//                // 再生ボタンクリック時の処理
//                playButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
////				// 再生開始
//                        onPlay(mStartPlaying, tmpFileName);
//
//                        if (timeSwapBuff != 0) timeSwapBuff = 0; // タイマーリセット
//                        if (mStartPlaying) {
//                            color = res.getColor(R.color.on);
//                            playButton.setBackgroundColor(color);
//                            playButton.setText("TOUCH STOP");
//                            statusTime.setText("00分00秒000");
//                            statusText.setText("NOW PLAYING");
//                            // 録音ボタンを無効化して色を変更
//                            recordButton.setEnabled(false);
//                            res = recordButton.getResources();
//                            color = res.getColor(R.color.off);
//                            recordButton.setBackgroundColor(color);
//                            // タイマー処理
//                            startTime = SystemClock.uptimeMillis();
//                            customHandler.postDelayed(updateTimerThread, 0);
//                        } else {
//                            color = res.getColor(R.color.main);
//                            playButton.setBackgroundColor(color);
//                            playButton.setText("PLAY");
//                            statusText.setText("");
//                            // 録音ボタンを有効化して色を変更
//                            recordButton.setEnabled(true);
//                            res = recordButton.getResources();
//                            color = res.getColor(R.color.main);
//                            recordButton.setBackgroundColor(color);
//                            // タイマー処理
//                            timeSwapBuff += timeInMilliseconds;
//                            customHandler.removeCallbacks(updateTimerThread);
//                        }
//                        mStartPlaying = !mStartPlaying;
//                    }
//                });
//                return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
//
//    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            customHandler.post(new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            });
//        }
//    };
}
