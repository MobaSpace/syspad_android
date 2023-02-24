package com.example.mysyspad;

/**
 * Created by sergio on 22/04/21
 * MobaSpace
 */
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        Button validate = (Button) findViewById(R.id.val_settings);
        EditText topic = (EditText) findViewById(R.id.editTextText_topic);
        EditText url = (EditText) findViewById(R.id.editTextText_url);
        Switch subscrip = (Switch) findViewById(R.id.switch2);

        topic.setText(getIntent().getStringExtra("ehpad_topic"));
        url.setText(getIntent().getStringExtra("ehpad_url"));
        subscrip.setChecked(getIntent().getBooleanExtra("act", true));

        validate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // your handler code here
                Intent intent = getIntent();
                intent.putExtra("topic", topic.getText().toString());
                intent.putExtra("url", url.getText().toString());
                intent.putExtra("subscribe", subscrip.isChecked());
                setResult(RESULT_OK, intent);
                finish();
            }
        });


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }
}