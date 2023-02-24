package com.example.mysyspad;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.util.Log;;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;
import java.util.UUID;

import androidx.core.app.NotificationCompat;

/**
 * Created by sergio on 15/04/21
 * MobaSpace
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CANAL = "MyNotifiCanal";
    private static final String TAG = "FirebaseMessage";
    private static final Locale LANG = Locale.FRENCH;

    private boolean ready;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        super.onMessageReceived(remoteMessage);
        String myMessage = remoteMessage.getNotification().getBody();
        Log.d(TAG, "Nous avons reçu une notification " + myMessage);

        //creer une action lors que l'on touche la notification
        //dans notre cas ouvrir à nouveau l'application MobaSpace
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //creation de la notification
        NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(this, CANAL);
        //ajout du texte
        notiBuilder.setContentTitle("Alarme SYSPAD");
        notiBuilder.setContentText(myMessage);

        //ajout de l'action
        notiBuilder.setContentIntent(pendingIntent);

        //ajout de la vibration
        long[] vibrationPattern = {500, 1000};
        notiBuilder.setVibrate(vibrationPattern);

        //ajout LED
        notiBuilder.setLights(Color.RED, 3000, 3000);

        //ajout d'un audio
        //Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getApplicationContext().getPackageName() + "/" + R.raw.alarme_syspad);
        //Log.d(TAG, "The sound URI is: " + soundUri);
        //notiBuilder.setSound(soundUri);

        //ajout d'une icone
        notiBuilder.setSmallIcon(R.drawable.alert);

        //envoyer la notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //prendre en compte la version, on peut mettre ça par défaut car un seul canal suffit
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String channelId = getString(R.string.notification_chanel_id);
            String channelTitle = getString(R.string.notification_chanel_title);
            String channelDesc = getString(R.string.notification_chanel_desc);
            NotificationChannel channel = new NotificationChannel( channelId, channelTitle, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(channelDesc);
            channel.setLightColor(Color.GRAY);
            channel.enableLights(true);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            //channel.setSound(soundUri, audioAttributes);

            notificationManager.createNotificationChannel(channel);
            notiBuilder.setChannelId(channelId);


        }
        notificationManager.notify(1, notiBuilder.build());
    }
}
