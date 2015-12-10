package com.example.audiorecordtest;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.RemoteControlClient;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import de.timroes.android.listview.EnhancedListView;

/**
 * Created by lena on 15/12/02.
 */
@SuppressLint("NewApi")
public class MyService extends Service {

    private RemoteControlClient mRemoteControlClient;
    private ComponentName mComponentName;
    public AudioManager mAudioManager;

    private static int counter = 0;

    // ミリ秒
    private static long timeInMilliseconds = 0L;
    private long timeSwapBuff = 0L;
    private long updatedTime = 0L;


    private static final String LOG_TAG = "AudioRecordTest";

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;

    private long startTime = 0L;

    private Handler customHandler = new Handler();

    private Drawable buttonStyle;
    private Resources res;
    private int color;

    private int fileLong;

    Intent mainIntent;

    private File filepathname; // DBからファイル名を読む時に使う
    private String absolutePath;
    private String fileName;
    private String filePath;

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

    static final String TAG = "LocalService";

    ////// コンストラクタ /////////////////////////////////////////////
    public MyService() {
        // 内部ストレージのパス情報を保持したFileインスタンスを取得
//        filepathname = new File(Environment.getExternalStorageDirectory().getPath()+"/soundfile/");
//        if(!filepathname.exists()){
//            filepathname.mkdir();
//        }
        // 絶対パスを取得
//		mFileName = filepathname.getAbsolutePath();
//		mFileName += "/audiorecordtest"+COUNTER+".3gp";
    }


    ////// サービス実装時のライフサイクルメソッド //////////////////////////////////////////////////

    // 生成時呼び出し
    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
//        Toast.makeText(this, "MyService#onCreate", Toast.LENGTH_SHORT).show();
        mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
    }

    /*
     開始時呼び出し
     startServiceで送られたIntentを受け取る。
      */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mainIntent = intent;
        Log.i(TAG, "onStartCommand Received start id " + startId + ": " + intent);
//        Toast.makeText(this, "MyService#onStartCommand", Toast.LENGTH_SHORT).show();
        if(intent != null) {
            String action = intent.getAction();
//        showNotification();

            if ("ACTION_PLAY".equals(action)) {
                fileName = intent.getExtras().getString("FILE_NAME");
                absolutePath = intent.getExtras().getString("ABSOLUTE_PATH");
//                Toast.makeText(this, "onStartCommand#ACTION_PLAY#filepath:"+filepath, Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "COUNTER:"+MainActivity.COUNTER, Toast.LENGTH_SHORT).show();
                startPlaying(fileName , absolutePath);
            } else if ("ACTION_STOP".equals(action)) {
                Toast.makeText(this, "onStartCommand#ACTION_STOP", Toast.LENGTH_SHORT).show();
                stopPlaying();
            } else if ("ACTION_RECORD".equals(action)) {
                counter = intent.getExtras().getInt("COUNTER");
                Toast.makeText(this, "onStartCommand#ACTION_RECORD", Toast.LENGTH_SHORT).show();
                absolutePath = intent.getExtras().getString("ABSOLUTE_PATH");
                startRecording(absolutePath, counter);
            } else if ("ACTION_RECORD_STOP".equals(action)) {
                Toast.makeText(this, "onStartCommand#ACTION_STOP", Toast.LENGTH_SHORT).show();
                stopRecording();
            }
        }
        // サービスが強制終了しても自動で再起動しない
        return START_NOT_STICKY;
    }

    // Notificationを作成
    private void showNotification(){
        // intentの作成
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        // LargeIcon の Bitmap を生成
//        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),R.drawable.dog);

        // NotificationBuilderを作成
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentIntent(contentIntent);
        // ステータスバーに表示されるテキスト
        builder.setTicker("ティッカー");
        // アイコン
//        builder.setSmallIcon(R.drawable.);
        // Notificationを開いた時に表示されるタイトル
        builder.setContentTitle("コンテントタイトル");
        // Notificationを開いた時に表示されるサブタイトル
        builder.setContentText("コンテントテキスト");
        // 表示されるアイコン
//        builder.setLargeIcon(largeIcon);
        // 通知時の音・バイブ・ライト
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
        // タップすると消える
        builder.setAutoCancel(true);

        // NotificationManagerを取得
        NotificationManager manager = (NotificationManager)getSystemService(Service.NOTIFICATION_SERVICE);
        // Notificationを作成して通知
        manager.notify(0, builder.build());

//        startForeground(ONGOING_NOTIFICATION, builder.build);
    }

    // Notificationを削除
    private void hideNotification(){
        // NotificationManagerを取得
        NotificationManager manager = (NotificationManager)getSystemService(Service.NOTIFICATION_SERVICE);
        // Notificationを作成して通知
        manager.cancel(0);
    }



    ////// バインド実装時のライフサイクルメソッド ////////////////////////////////////////////////

    //    // サービスに接続するためのBinder
//    public class MyServiceLocalBinder extends Binder {
//        // サービスの取得
//        MyService getService() {
//            return MyService.this;
//        }
//    }
//    // Binderの生成
//    private final IBinder mBinder = new MyServiceLocalBinder();
    // バインド接続時
    @Override
    public IBinder onBind(Intent intent) {
//        Toast.makeText(this, "MyService#onBind" + ": " + intent, Toast.LENGTH_SHORT).show();
        Log.i(TAG, "onBind" + ": " + intent);
//        return mBinder;
        return null;
    }

    ////// 再生処理 //////////////////////////////////////////////
    private void startPlaying(String fileName, String absolutePath) {
        if(mPlayer == null) {
            mPlayer = new MediaPlayer();
            try {
                // タイマー処理
                if (timeSwapBuff != 0) timeSwapBuff = 0; // タイマーリセット
                startTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 0);

//                MainActivity.mFileName = absolutePath.getAbsolutePath();
//            mFileName += "/audiorecordtest"+COUNTER+".3gp";
                // 再生ファイルの指定？
                filePath = absolutePath + File.separator + fileName;

                mPlayer.setDataSource(filePath);
                // アイドル時も動作させ続けることを保証
                mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
                // 再生準備
                mPlayer.prepare();
//			// 再生開始
			mPlayer.start();
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }
//            registerMediaButtonEventReceiver();
        }
//        File file = new File(filePath);
//        Toast.makeText(this, Environment.getExternalStorageState(file), Toast.LENGTH_LONG).show();
//        Toast.makeText(this, filePath, Toast.LENGTH_LONG).show();
//        Toast.makeText(this, file.toString(), Toast.LENGTH_LONG).show();
        mPlayer.start();
        // RemoteControlClientを登録
//        registerRemoteControlClient();
        // ロックスクリーンの状態を再生に設定
//        mRemoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        // ロックスクリーンに表示する再生情報を設定
//        mRemoteControlClient.editMetadata(true).putString(1,filePath).apply();
    }

    ////// 再生停止 ///////////////////////////////////////////////
    private void stopPlaying() {
        // タイマー処理
        timeSwapBuff += timeInMilliseconds;
        customHandler.removeCallbacks(updateTimerThread);
        // 再生停止
        mPlayer.stop();
        // オブジェクトを解放
        mPlayer.release();
        mPlayer = null;
//        unregisterMediaButtonEventReceiver();

        // RemoteControlClientの登録を解除
//        unregisterRemoteControlClient();
    }

    ////// 録音開始 ///////////////////////////////////////////////
    private void startRecording(String absolutePath, int counter) {
        // タイマー処理
        if (timeSwapBuff != 0) timeSwapBuff = 0; // タイマーリセット
        startTime = SystemClock.uptimeMillis();
        customHandler.postDelayed(updateTimerThread, 0);

        mRecorder = new MediaRecorder();
        // 入力ソースの指定
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // フォーマット、コーデックの指定
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); // 出力フォーマット
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); // オーディオのエンコーダ

        absolutePath += File.separator + "audiorecordtest" + counter + ".3gp";
        // 保存先の指定
        mRecorder.setOutputFile(absolutePath);

        try {
            // 録音準備
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        // 録音開始
        mRecorder.start();
        Toast.makeText(this, absolutePath, Toast.LENGTH_LONG).show();

    }

    ////// 録音停止 ////////////////////////////////////////////////
    private void stopRecording() {
        // タイマー処理
        timeSwapBuff += timeInMilliseconds;
        customHandler.removeCallbacks(updateTimerThread);

        mRecorder.stop();
        // オブジェクトを解放
        mRecorder.release();
        mRecorder = null;
    }


    // イベントレシーバーを登録
    private void registerMediaButtonEventReceiver(){
        if(mComponentName == null){
            // MediaButtonEventReceiverを登録する
            mComponentName = new ComponentName(this,Receiver.class);
            // MediaButtonEventReceiverをシステムに登録する
            mAudioManager.registerMediaButtonEventReceiver(mComponentName);
        }
    }
    // イベントレシーバーを登録解除
    private void unregisterMediaButtonEventReceiver(){
        if(mComponentName != null){
            // MediaButtonEventReceiverを登録解除
            mAudioManager.unregisterMediaButtonEventReceiver(mComponentName);
            mComponentName = null;
        }
    }

    private void registerRemoteControlClient(){
        if(mRemoteControlClient == null){
            // LOCKSCREENで押されたボタンのイベントを受け取るレシーバーのPendingIntentを生成
            Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            mediaButtonIntent.setComponent(mComponentName);
            PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(
                    this,0,mediaButtonIntent,0);
            // RemoteControlClientを生成し、PendingIntentを生成
            mRemoteControlClient = new RemoteControlClient(mediaPendingIntent);
            mRemoteControlClient
                    .setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY
                        | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
                        | RemoteControlClient.FLAG_KEY_MEDIA_NEXT
                        | RemoteControlClient.FLAG_KEY_MEDIA_STOP);
            // RemoteControlClientを登録
            mAudioManager.registerRemoteControlClient(mRemoteControlClient);
            // AudioFocusを取得する
            mAudioManager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    Log.d(TAG,"focusChanged:"+focusChange);
                }
            }, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    private void unregisterRemoteControlClient(){
        if(mRemoteControlClient != null){
            // RemoteControlClientの登録を解除
            mAudioManager.unregisterRemoteControlClient(mRemoteControlClient);
            mRemoteControlClient = null;
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        Toast.makeText(this, "MyService#onDestroy", Toast.LENGTH_SHORT).show();
        super.onDestroy();
        // 使わなくなった時点でレコーダーリソースを解放
        if(mRecorder != null){
            mRecorder.release();
            mRecorder = null;
        }
        if(mPlayer != null){
//            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
//		if(soundPool != null){
//			soundPool.release();
//			soundPool = null;
//		}
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
            MainActivity.statusTime.setText("" + mins + "分"
                    + String.format("%02d", secs) + "秒"
                    + String.format("%03d", milliseconds));
            customHandler.postDelayed(this, 0);

            // プレイヤーの自動停止
            if(!MainActivity.mStartPlaying && updatedTime>=MainActivity.fileLong){
                // タイマー処理
                timeSwapBuff += timeInMilliseconds;
                customHandler.removeCallbacks(updateTimerThread);
                res = getResources();
                int color = res.getColor(R.color.main);
                MainActivity.playButton.setBackgroundColor(color);
                MainActivity.recordButton.setBackgroundColor(color);
                MainActivity.playButton.setText("PLAY");
                MainActivity.recordButton.setText("RECORD");
                MainActivity.statusText.setText("");
                MainActivity.statusLastTime.setText("");
                MainActivity.playButton.setEnabled(true);
                MainActivity.recordButton.setEnabled(true);
                boolean bl = MainActivity.mStartPlaying;
                MainActivity.mStartPlaying = !bl;
                // 停止処理
                mainIntent.setAction("ACTION_STOP");
                startService(mainIntent);
            }
        }
    };
}
