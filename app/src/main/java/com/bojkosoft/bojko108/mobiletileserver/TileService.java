package com.bojkosoft.bojko108.mobiletileserver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.io.IOException;

import server.MyServer;

public class TileService extends Service {
    // tile server port
    public static int PORT;
    // tile server root directory path
    public static String ROOT_PATH;
    // no tile data
    public static byte[] NO_TILE;

    private NotificationChannel notificationChannel;

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

        if (this.notificationChannel == null) {
            this.createNotificationChannel();
        }

        Notification.Builder builder = new Notification.Builder(this, this.notificationChannel.getId())
                .setOngoing(true)
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
//                .setContentTitle(getResources().getString(R.string.app_name))
                .setStyle(new Notification.InboxStyle()
                        .addLine(getResources().getString(R.string.app_notif_local_tiles))
                        .addLine("http://localhost:" + PORT)
                        .addLine(getResources().getString(R.string.app_notif_redirect))
                        .addLine(":" + PORT + "/redirect/?url=...&quadkey=true&x=&y=&z=")
                        .setBigContentTitle(getResources().getString(R.string.app_name)))
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_SERVICE);

        return builder.build();
    }

    private void createNotificationChannel() {
        String CHANNEL_NAME = "Mobile Tile Server";
        String CHANNEL_ID = "com.bojkosoft.bojko108.mobiletileserver";

        this.notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
        this.notificationChannel.setLightColor(Color.WHITE);
        this.notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        this.notificationChannel.setSound(null, null);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(this.notificationChannel);
    }
}
