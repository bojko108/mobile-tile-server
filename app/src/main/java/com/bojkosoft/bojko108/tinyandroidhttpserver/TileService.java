package com.bojkosoft.bojko108.tinyandroidhttpserver;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.bojkosoft.bojko108.tinyandroidhttpserver.MainActivity;
import com.bojkosoft.bojko108.tinyandroidhttpserver.R;

import java.io.IOException;

import server.MyServer;

public class TileService extends Service {
    // tile server port
    public static int PORT;
    // tile server root directory path
    public static String ROOT_PATH;
    // no tile data
    public static byte[] NO_TILE;

    private MyServer server;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        try {
            this.server = new MyServer(PORT, ROOT_PATH, NO_TILE);
            startForeground(1, this.createNotification(intent));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (this.server != null) {
            stopForeground(true);
            this.server.stop();
        }
    }

    private Notification createNotification(Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setOngoing(true)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setStyle(new Notification.InboxStyle()
                        .addLine(getResources().getString(R.string.app_notif_local_tiles) + "localhost:" + PORT + "/")
                        .addLine(getResources().getString(R.string.app_notif_redirect) + "localhost:" + PORT + "/redirect/")
                        .setBigContentTitle(getResources().getString(R.string.app_name)))
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentIntent(pendingIntent);

        return builder.build();
    }
}
