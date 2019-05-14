package dev.elvisbui.pomodorotime;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class NotificationsWrapper extends Application {
    public static final String CHANNEL_1_ID = "alarms";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    @TargetApi(26)
    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Alarms",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This channel shows your alarms");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }
}
