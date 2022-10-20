package com.bojko108.mobiletileserver;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bojko108.mobiletileserver.server.TileService;
import com.bojko108.mobiletileserver.server.TileServiceReceiver;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // used to ping TileService to see if it's running
    private LocalBroadcastManager manager;

    // server port and root path: stored in shared preferences
    private int serverPort;
    private String rootPath;
    private boolean running;
    int PERMISSION_REQUEST = 1;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.manager = LocalBroadcastManager.getInstance(this);

        findViewById(R.id.buttonStart).setOnClickListener(this);
        findViewById(R.id.buttonStop).setOnClickListener(this);
        findViewById(R.id.buttonSettings).setOnClickListener(this);

        this.checkPermissions(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        this.manager.registerReceiver(this.localReceiver, new IntentFilter(TileService.ACTION_RUNNING));

        // will be evaluated in the local broadcast
        this.running = false;
        this.manager.sendBroadcastSync(new Intent(TileService.ACTION_PING));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        this.serverPort = Integer.parseInt(prefs.getString("serverport", getResources().getString(R.string.settings_server_port_default)));
        this.rootPath = prefs.getString("rootpath", getResources().getString(R.string.settings_root_path_default));

        this.prepareUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.manager.unregisterReceiver(this.localReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonStart:
                this.startTileService();
                break;
            case R.id.buttonStop:
                this.stopTileService();
                break;
            case R.id.buttonSettings:
                // OPEN settings dialog
                Intent preferencesIntent = new Intent(this, SettingsActivity.class);
                startActivity(preferencesIntent);
                break;
            default:
                break;
        }
    }

    private void startTileService() {
        Intent startServerIntent = new Intent(this, TileServiceReceiver.class);
        startServerIntent.setAction(TileServiceReceiver.ACTION_START);
        startServerIntent.putExtra(TileService.KEY_SERVER_PORT, this.serverPort);
        startServerIntent.putExtra(TileService.KEY_ROOT_PATH, this.rootPath);

        sendBroadcast(startServerIntent);

        this.running = true;

        this.prepareUI();
    }

    private void stopTileService() {
        Intent stopServerIntent = new Intent(this, TileServiceReceiver.class);
        stopServerIntent.setAction(TileServiceReceiver.ACTION_STOP);

        sendBroadcast(stopServerIntent);

        this.running = false;

        this.prepareUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.startInfo == item.getItemId()) {
            Intent intent = new Intent(this, InfoActivity.class);
            startActivity(intent);
        }
        return true;
    }

    private void prepareUI() {
        ImageView buttonTile = findViewById(R.id.imageTiles);
        MaterialButton buttonStart = (MaterialButton) findViewById(R.id.buttonStart);
        MaterialButton buttonStop = (MaterialButton) findViewById(R.id.buttonStop);

        if (this.running) {
            buttonTile.setImageAlpha(255);

            buttonStart.setAlpha(.3f);
            buttonStop.setAlpha(1f);

            buttonStart.setClickable(false);
            buttonStop.setClickable(true);
        } else {
            buttonTile.setImageAlpha(50);

            buttonStart.setAlpha(1f);
            buttonStop.setAlpha(.3f);

            buttonStart.setClickable(true);
            buttonStop.setClickable(false);
        }
    }

    /**
     * Checks if the app has permission to read/write device storage
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity this activity
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    private void checkPermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission1 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (PackageManager.PERMISSION_GRANTED != permission) {
            // ask user for permissions
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
        if (Environment.isExternalStorageManager()) {
            //todo when permission is granted
        } else {
            //request for the permission
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }
    }

    /**
     * This local broadcast is used to simply ping {@link TileService} to see if
     * it is running
     */
    protected BroadcastReceiver localReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TileService.ACTION_RUNNING.equals(intent.getAction())) {
                running = true;
            }
        }
    };
}
