package com.example.mysyspad;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

/**
 * Created by sergio on 19/04/21
 * MobaSpace
 */
public class MyJSAndroid {
    //Voice reading
    Context mContext;
    MyTtsTalker talker;
    private static final String TAG = "FirebaseMessage";

    public MyJSAndroid(Context c){
        mContext = c;
        talker = new MyTtsTalker(mContext);
    }

    @JavascriptInterface
    public void speak (String word) {
        Log.d(TAG, "On m'a dit de parler...");
        talker.speak (word, true);
    }
}
