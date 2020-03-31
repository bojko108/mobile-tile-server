package com.bojkosoft.bojko108.mobiletileserver;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(String.format(textView.getText().toString(), BuildConfig.VERSION_NAME));
    }
}
