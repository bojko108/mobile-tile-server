package com.bojko108.mobiletileserver.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

/**
 * This class receives action requests for the TileService class
 *
 * <b>Available Actions:</b>
 * <ul>
 * <li><b>ACTION_STOP</b> - stops the server</li>
 * <li><b>ACTION_OPEN_ROOT_PATH</b> - opens the root directory in FileExplorer</li>
 * <li><b>ACTION_NAVIGATE_TO_SERVER</b> - opens the Server home page WebBrowser</li>
 * </ul>
 *
 * @see TileService TileServer class
 * <p>
 * Mobile Tile Server, Copyright (c) 2020 by bojko108
 * <p/>
 */
public class TileServiceReceiver extends BroadcastReceiver {
    /**
     * Use this key to trigger Start Server action as <i>String</i>
     */
    public static final String ACTION_START = "mobiletileserver.ACTION_START";
    /**
     * Use this key to trigger Stop Server action as <i>String</i>
     */
    public static final String ACTION_STOP = "mobiletileserver.ACTION_STOP";
    /**
     * Use this key to trigger Open Root Directory action in FileExplorer as <i>String</i>
     */
    public static final String ACTION_OPEN_ROOT_PATH = "mobiletileserver.ACTION_OPEN_ROOT_PATH";
    /**
     * Use this key to trigger Open Server Home Page action in WebBrowser as <i>String</i>
     */
    public static final String ACTION_NAVIGATE_TO_SERVER = "mobiletileserver.ACTION_NAVIGATE_TO_SERVER";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            try {
                String serverUrl = intent.getStringExtra(TileService.KEY_SERVER_PATH);
                String rootPath = intent.getStringExtra(TileService.KEY_ROOT_PATH);
                int serverPort = intent.getIntExtra(TileService.KEY_SERVER_PORT, 1886);
                switch (action) {
                    case ACTION_START:
                        Intent serviceToStart = new Intent(context, TileService.class);
                        serviceToStart.putExtra(TileService.KEY_SERVER_PORT, serverPort);
                        serviceToStart.putExtra(TileService.KEY_ROOT_PATH, rootPath);
                        context.startService(serviceToStart);
                        break;
                    case ACTION_STOP:
                        Intent serviceToStop = new Intent(context, TileService.class);
                        context.stopService(serviceToStop);
                        break;
                    case ACTION_OPEN_ROOT_PATH:
                        if (rootPath != null) {
                            Uri selectedUri = Uri.parse(rootPath);
                            Intent explorerIntent = new Intent(Intent.ACTION_VIEW);
                            explorerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            explorerIntent.setDataAndType(selectedUri, "resource/folder");
                            if (explorerIntent.resolveActivityInfo(context.getPackageManager(), 0) != null) {
                                context.startActivity(explorerIntent);
                                this.closeNotificationBar(context);
                            }
                        }
                        break;
                    case ACTION_NAVIGATE_TO_SERVER:
                        if (serverUrl != null) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(serverUrl));
                            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(browserIntent);
                            this.closeNotificationBar(context);
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Broadcasts <b>Intent.ACTION_CLOSE_SYSTEM_DIALOGS</b> message, which will close the notification bar.
     *
     * @param context context to be used for broadcasting <b>Intent.ACTION_CLOSE_SYSTEM_DIALOGS</b> message
     */
    private void closeNotificationBar(Context context) {
        //context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }
}