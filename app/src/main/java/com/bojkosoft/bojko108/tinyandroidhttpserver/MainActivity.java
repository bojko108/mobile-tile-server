package com.bojkosoft.bojko108.tinyandroidhttpserver;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity implements View.OnClickListener {

    // server port and root path: stored in shared preferences
    private int serverPort;
    private String rootPath;
    // internal use - true when tile server is up and running
    private boolean running;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.buttonStart).setOnClickListener(this);
        findViewById(R.id.buttonStop).setOnClickListener(this);
        findViewById(R.id.buttonSettings).setOnClickListener(this);

        this.checkPermissions(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        this.serverPort = Integer.parseInt(prefs.getString("serverport", getResources().getString(R.string.settings_server_port_default)));
        this.rootPath = prefs.getString("rootpath", getResources().getString(R.string.settings_root_path_default));
        this.running = prefs.getBoolean("running", false);

        this.setUpUI();
    }

    @Override
    public void onClick(View v) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();

        switch (v.getId()) {
            case R.id.buttonStart:

                // START the service
                TileService.PORT = this.serverPort;
                TileService.ROOT_PATH = this.rootPath;
                TileService.NO_TILE = this.getNoTileFile();
                Intent serviceToStart = new Intent(this, TileService.class);
                startService(serviceToStart);

                this.running = true;
                prefs.putBoolean(getString(R.string.running), true);
                prefs.apply();

                this.setUpUI();

                break;
            case R.id.buttonStop:

                // STOP the service
                Intent serviceToStop = new Intent(this, TileService.class);
                stopService(serviceToStop);

                this.running = false;
                prefs.putBoolean(getString(R.string.running), false);
                prefs.apply();

                this.setUpUI();

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

    private void setUpUI() {
        ImageView buttonTile = findViewById(R.id.imageTiles);
        ImageView buttonStart = (ImageButton) findViewById(R.id.buttonStart);
        ImageView buttonStop = (ImageButton) findViewById(R.id.buttonStop);

        if (this.running) {
            buttonTile.setImageAlpha(255);

            buttonStart.setImageAlpha(50);
            buttonStop.setImageAlpha(255);

            buttonStart.setEnabled(false);
            buttonStop.setEnabled(true);
        } else {
            buttonTile.setImageAlpha(50);

            buttonStart.setImageAlpha(255);
            buttonStop.setImageAlpha(50);

            buttonStart.setEnabled(true);
            buttonStop.setEnabled(false);
        }
    }

    /**
     * Get no data tile as byte[]
     *
     * @return byte[]
     */
    private byte[] getNoTileFile() {
        InputStream stream = getResources().openRawResource(R.raw.no_tile);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];

        try {
            while ((nRead = stream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer.toByteArray();
    }

    /**
     * Checks if the app has permission to read/write device storage
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    private void checkPermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // ask user for permissions
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
