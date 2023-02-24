package com.example.mysyspad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

/**
 * Created by sergio on 19/04/21
 * MobaSpace
 */
public class TtsReceiver extends BroadcastReceiver {
    private Context mContext;
    private String mTitle;
    private String mContent;

    private static final String TAG = "FirebaseMessage";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context != null) {
            mContext = context;
        }

        if (intent.getStringExtra("data_title") != null) {
            mTitle = intent.getStringExtra("data_title");
        }

        if (intent.getStringExtra("data_content") != null) {
            mContent = intent.getStringExtra("data_content");
        }

        Log.d(TAG, "Title: " + mTitle + ", Content: " + mContent);
        //showNotification(mTitle, mContent);
    }
}
