package com.example.mysyspad;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;
import java.util.UUID;

/**
 * Created by sergio on 19/04/21
 * MobaSpace
 */
public class MyTtsTalker implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private boolean ttsOk;

    //The constructor will create a TextToSpeech instance.
    MyTtsTalker(Context context) {
        tts = new TextToSpeech(context, this);

    }

    @Override
    //OnInitListener method to receive the TTS engine status
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            ttsOk = true;
            tts.setLanguage(Locale.FRENCH);
            tts.setSpeechRate(0.9f);
        }
        else {
            ttsOk = false;
        }
    }

    //A method to speak something
    @SuppressWarnings("deprecation")//Support older API levels too.
    public void speak(String text, Boolean override) {
        String utteranceId = UUID.randomUUID().toString();
        if (ttsOk) {
            if (override) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
            }
            else {
                tts.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId);
            }
        }
    }
}
