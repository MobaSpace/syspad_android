package com.example.mysyspad;

import android.app.Service;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;
import java.util.UUID;

/**
 * Created by sergio on 19/04/21
 * MobaSpace
 */
public class NotificationListener extends NotificationListenerService {

    private MyTtsTalker myTtsTalker;
    private String mContent;
    private static final String TAG = "FirebaseMessage";

    @Override
    public void onCreate() {
        super.onCreate();
        myTtsTalker = new MyTtsTalker(getApplicationContext());

    }@Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        Log.d(TAG, "Je rentre dans onNotificationPosted: " + sbn.getPackageName());
        if (sbn.getPackageName().equals("com.example.mysyspad")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mContent = "Alarme SySPAD reçue!. ";
                mContent += sbn.getNotification().extras.getString("android.text") + ". ";
                mContent += "Vérifiez s'il vous plaît, puis aquittez";
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                int pre_setting = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
                am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION), 0);
                myTtsTalker.speak(mContent, true);
                am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, pre_setting, 0);
            }
        }
    }
}
