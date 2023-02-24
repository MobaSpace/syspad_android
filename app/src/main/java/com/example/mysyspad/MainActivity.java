package com.example.mysyspad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;



public class MainActivity extends AppCompatActivity {

    private WebView webView = null;
    private static final String TAG = "FirebaseMessage";
    private static final String FILE_NAME = "mysyspad.conf";

    private static final int VIBRATE_PERMISSION_CODE = 100;
    private static final int MICRO_PERMISSION_CODE = 101;
    private static final int AUDIO_PERMISSION_CODE = 102;

    public final static int REQUEST_CODE_SETTINGS = 1;
    private String ehpad_url = "https://mysyspad.mobaspace.com";
    private String ehpad_topic = "demo";
    private boolean subs2notis = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //manange permissions
        checkPermission(
                Manifest.permission.VIBRATE,
                VIBRATE_PERMISSION_CODE);
        checkPermission(
                Manifest.permission.RECORD_AUDIO,
                MICRO_PERMISSION_CODE);

        checkPermission(
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                AUDIO_PERMISSION_CODE);

        File file = new File(getApplicationContext().getFilesDir(), FILE_NAME);
        Log.d(TAG, "Le fichier est dans" + getApplicationContext().getFilesDir().toString());
        Intent myIntent = new Intent(this, SettingsActivity.class);
        myIntent.putExtra("ehpad_url", ehpad_url);
        myIntent.putExtra("ehpad_topic", ehpad_topic);
        myIntent.putExtra("act", subs2notis);

        if (!file.exists()) {
            Log.d(TAG, "Il n'y a pas de fichier de configuration");
            this.startActivityForResult(myIntent, REQUEST_CODE_SETTINGS);
        }
        else{
            try {
                JSONObject obj = new JSONObject(loadJSONConfig());
                ehpad_url = obj.getString("ehpad_url");
                ehpad_topic = obj.getString("ehpad_topic");
                subs2notis = obj.getBoolean("act");
                Log.d(TAG, "La lecture du fichier contient: " + obj.toString());
            }catch (JSONException e) {
                Log.d(TAG, e.toString());
                Log.d(TAG, "Le fichier de configuration n'a pas le bon contenu");
                this.startActivityForResult(myIntent, REQUEST_CODE_SETTINGS);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SETTINGS:
                //you just got back from activity B - deal with resultCode
                //use data.getExtra(...) to retrieve the returned data
                String new_ehpad_topic = data.getStringExtra("topic");
                ehpad_url = data.getStringExtra("url");
                subs2notis = data.getBooleanExtra("subscribe", true);

                if (subs2notis) {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(ehpad_topic);
                    ehpad_topic = new_ehpad_topic;
                    //manages notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(ehpad_topic)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    String msg = getString(R.string.msg_subscribed);
                                    if (!task.isSuccessful()) {
                                        msg = getString(R.string.msg_subscribe_failed);
                                    }
                                    Log.d(TAG, msg);
                                }
                            });
                }
                else
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(ehpad_topic)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    String msg = getString(R.string.msg_unsubscribed);
                                    if (!task.isSuccessful()) {
                                        msg = getString(R.string.msg_unsubscribe_failed);
                                    }
                                    Log.d(TAG, msg);
                                }
                            });

                JSONObject myConf = new JSONObject();
                try {
                    myConf.put("ehpad_url", ehpad_url);
                    myConf.put("ehpad_topic", ehpad_topic);
                    myConf.put("act", subs2notis);
                    saveJSONConfig(myConf.toString());

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                }

                break;
            default:
                //you just got back from activity C - deal with resultCode
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.clearCache(true);
        webView.clearHistory();
        webView.clearFormData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_main);

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // your handler code here
                Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
                myIntent.putExtra("ehpad_url", ehpad_url);
                myIntent.putExtra("ehpad_topic", ehpad_topic);
                startActivityForResult(myIntent, REQUEST_CODE_SETTINGS);
            }
        });

        this.webView = (WebView) findViewById(R.id.webview);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                // Generally you want to check which permissions you are granting
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });

        //Add JS interface to play TTS from web
        webView.addJavascriptInterface(new MyJSAndroid(this), "JSAndroid");

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setSupportMultipleWindows(true);
        //this allow to play sounds
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        webView.clearHistory();
        webView.clearFormData();
        webView.clearCache(true);
        webView.loadUrl(ehpad_url);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && this.webView.canGoBack()) {
            this.webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void saveJSONConfig(String text) {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
            fos.write(text.getBytes());

            //Toast.makeText(this, "Saved to " + getFilesDir() + "/" + FILE_NAME,
            //        Toast.LENGTH_LONG).show();
            Log.d(TAG, "Saved to " + getFilesDir() + "/" + FILE_NAME);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String loadJSONConfig() {
        FileInputStream fis = null;
        StringBuilder sb = new StringBuilder();
        try {
            fis = openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String text;
            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }
            //TODO put here the values of config file
            Log.d(TAG, sb.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    // Function to check and request permission.
    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.requestPermissions(
                        new String[] { permission },
                        requestCode);
            }
        }
        else {
            Log.d(TAG, "Permission already granted");
        }
    }

    // This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super
                .onRequestPermissionsResult(requestCode,
                        permissions,
                        grantResults);

        if (requestCode == VIBRATE_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,
                        "Vibrate Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            }
            else {
                Toast.makeText(MainActivity.this,
                        "Vibrate Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
        else if (requestCode == MICRO_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,
                        "Micro Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            }
            else {
                Toast.makeText(MainActivity.this,
                        "Micro Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }

        else if (requestCode == AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this,
                        "Audio Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            }
            else {
                Toast.makeText(MainActivity.this,
                        "Audio Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
}