package com.bojkosoft.bojko108.mobiletileserver;

import android.Manifest;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

public class InfoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(String.format(textView.getText().toString(), BuildConfig.VERSION_NAME));
    }
}
