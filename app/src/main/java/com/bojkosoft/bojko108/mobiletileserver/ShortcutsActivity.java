package com.bojkosoft.bojko108.mobiletileserver;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.bojkosoft.bojko108.mobiletileserver.server.TileService;
import com.bojkosoft.bojko108.mobiletileserver.server.TileServiceReceiver;

public class ShortcutsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, TileServiceReceiver.class);

        if (TileServiceReceiver.ACTION_START.equals(getIntent().getAction())) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            int serverPort = Integer.parseInt(prefs.getString("serverport", getResources().getString(R.string.settings_server_port_default)));
            String rootPath = prefs.getString("rootpath", getResources().getString(R.string.settings_root_path_default));

            intent.setAction(TileServiceReceiver.ACTION_START);
            intent.putExtra(TileService.KEY_SERVER_PORT, serverPort);
            intent.putExtra(TileService.KEY_ROOT_PATH, rootPath);
        } else if (TileServiceReceiver.ACTION_STOP.equals(getIntent().getAction())) {
            intent.setAction(TileServiceReceiver.ACTION_STOP);
        }

        if (intent.getAction() != null) {
            sendBroadcast(intent);
        }

        finish();
    }
}
