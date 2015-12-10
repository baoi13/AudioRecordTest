package com.example.audiorecordtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by lena on 15/12/03.
 */
public class Receiver extends BroadcastReceiver {
    public static final String TAG = "BroadcastReceiverだよ";
    private static final int KEYCODE_MEDIA_PLAY = 126;
    private static final int KEYCODE_MEDIA_PAUSE = 127;

    @Override
    public void onReceive(Context context, Intent intent){
        if(Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())){
            KeyEvent keyEvent = (KeyEvent) intent
                    .getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if(keyEvent.getAction() == KeyEvent.ACTION_UP){
                Intent service = new Intent(context, MyService.class);
                switch (keyEvent.getKeyCode()){
                    case KEYCODE_MEDIA_PAUSE:
                    case KEYCODE_MEDIA_PLAY:
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    case KeyEvent.KEYCODE_HEADSETHOOK:
                        // 再生または一時停止またはフックボタン
                        service.setAction("ACTION_PLAY");
                        context.startService(service);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_STOP:
                        // 停止
                        service.setAction("ACTION_STOP");
                        context.startService(service);
                        break;
                    default:
                        Log.w(TAG, "予期しないコードが呼ばれたよ："+keyEvent.getKeyCode());
                }
            }
        }
    }
}
