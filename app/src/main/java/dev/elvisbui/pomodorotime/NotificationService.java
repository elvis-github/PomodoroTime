package dev.elvisbui.pomodorotime;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import static dev.elvisbui.pomodorotime.NotificationsWrapper.CHANNEL_1_ID;

public class NotificationService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int priority;

        String content = intent.getStringExtra("inputExtra");
        boolean status = intent.getBooleanExtra("timerStatus", true);


        priority = NotificationCompat.PRIORITY_LOW;


        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("PomodoroTime")
                .setContentText(content)
                .setPriority(priority)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSound(null,0)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .build();


        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
