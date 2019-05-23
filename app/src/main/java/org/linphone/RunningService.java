package org.linphone;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

public class RunningService extends Service {
    NotificationChannel channel;

    public RunningService() {

    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
            channel = new NotificationChannel("cl-id", "cl-id", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager  = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getBooleanExtra("launch", false)) {
            NotificationCompat.Builder compat = new NotificationCompat.Builder(this, null);
            compat.setContent(new RemoteViews(getPackageName(), R.layout.notification));
            PendingIntent i = PendingIntent.getActivity(this, 100, new Intent(this, LinphoneActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), PendingIntent.FLAG_UPDATE_CURRENT);
            compat.setPriority(Notification.PRIORITY_HIGH);
            compat.setContentIntent(i);
            compat.setChannelId("cl-id");
            compat.setAutoCancel(false);
            compat.setSmallIcon(R.mipmap.ic_launcher_round);

            startForeground(200, compat.build());
        } else if (intent != null && !intent.getBooleanExtra("launch", false)) {
            stopForeground(true);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.cancel(200);
        }
        return START_STICKY;
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("taskRemoved", "onTaskRemoved: ");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
