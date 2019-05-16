package dev.elvisbui.pomodorotime;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import static dev.elvisbui.pomodorotime.NotificationsWrapper.CHANNEL_1_ID;

public class MainActivity extends AppCompatActivity {
                                                            //03 Seconds = 3000
    private static final long POMODORO = 10000;           //25 Minutes = 1500000
    private static final long SHORT_BREAK = 3000;         //05 Minutes = 300000
    private static final long LONG_BREAK = 3000;          //15 Minutes = 900000

    private static final String START_TIME = "startTimeInMillis";
    private static final String PREFS = "prefs";
    private static final String MILLIS_LEFT = "millisLeft";
    private static final String TIMER_RUNNING = "timerRunning";
    private static final String END_TIME = "endTime";
    private static final String STATUS = "status";

    private TextView mTextViewCountDown;
    private TextView mTextViewStatus;

    private Button mButtonStartPause;
    private Button mButtonShort;
    private Button mButtonLong;
    private Button mButtonReset;

    private CountDownTimer mCountDownTimer;

    private boolean mPomodoro = true;

    private boolean mTimerRunning;

    private long mStartTimeInMillis;
    private long mTimeLeftInMillis;
    private long mEndTime;
    
    private MediaPlayer mAlarm;

    private NotificationManagerCompat notificationMananger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mTextViewCountDown = findViewById(R.id.countdown);
        mButtonStartPause = findViewById(R.id.startPauseButton);
        mButtonReset = findViewById(R.id.resetButton);
        mButtonShort = findViewById(R.id.shortButton);
        mButtonLong = findViewById(R.id.longButton);
        mTextViewStatus = findViewById(R.id.status);

        notificationMananger = NotificationManagerCompat.from(this);

        mButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mTimerRunning){
                    Toast.makeText(MainActivity.this, "Timer Paused!", Toast.LENGTH_SHORT).show();
                    pauseTimer();
                } else {
                    if(mStartTimeInMillis == POMODORO)
                        Toast.makeText(MainActivity.this, "Pomodoro Started!", Toast.LENGTH_SHORT).show();
                    startTimer();
                }
            }
        });
        
        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPomodoro = true;
                Toast.makeText(MainActivity.this, "Timer Reset!", Toast.LENGTH_SHORT).show();
                setTimer(POMODORO);
            }
        });

        mButtonShort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPomodoro = false;
                mTextViewStatus.setText("Short Break");
                setTimer(SHORT_BREAK);
                Toast.makeText(MainActivity.this, "Short Break Started!", Toast.LENGTH_SHORT).show();
                startTimer();
            }
        });

        mButtonLong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPomodoro = false;
                mTextViewStatus.setText("Long Break");
                setTimer(LONG_BREAK);
                Toast.makeText(MainActivity.this, "Long Break Started!", Toast.LENGTH_SHORT).show();
                startTimer();
            }
        });
    }

    private void setTimer(long milliseconds){
        mStartTimeInMillis = milliseconds;
        resetTimer();
    }

    private void startTimer() {
        if(mAlarm == null)
            if(mPomodoro)
                mAlarm = MediaPlayer.create(this, R.raw.pomodoro_alarm);
            else
                mAlarm = MediaPlayer.create(this, R.raw.break_alarm);
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 500) {

            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
                startService("Timer Started\n" + mTextViewCountDown.getText().toString());
            }

            @Override
            public void onFinish() {
                stopService();
                sendOnChannel1();
                mAlarm.start();
                mTimerRunning = false;
                updateButtons();
            }
        }.start();
        startService("Timer Started");
        mTimerRunning = true;
        updateButtons();
    }

    private void updateCountDownText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        mTextViewCountDown.setText(timeLeftFormatted);
    }

    private void pauseTimer() {
        mCountDownTimer.cancel();
        mTimerRunning = false;
        updateButtons();
    }

    private void resetTimer() {
        if(mAlarm != null){
            mAlarm.stop();
            mAlarm.release();
            mAlarm = null;
        }
        stopService();
        mTimeLeftInMillis = mStartTimeInMillis;
        updateCountDownText();
        updateButtons();
    }

    private void updateButtons(){
        if(mTimerRunning){
            mButtonReset.setVisibility(View.INVISIBLE);
            mButtonShort.setVisibility(View.INVISIBLE);
            mButtonLong.setVisibility(View.INVISIBLE);
            mButtonStartPause.setText("Pause");
        } else {
            mButtonStartPause.setText("Resume");
            if(mTimeLeftInMillis < 500){
                mButtonStartPause.setVisibility(View.INVISIBLE);
                mButtonShort.setVisibility(View.INVISIBLE);
                mButtonLong.setVisibility(View.INVISIBLE);
            } else {
                mButtonStartPause.setVisibility(View.VISIBLE);
            }

            if(mTimeLeftInMillis < mStartTimeInMillis){
                mButtonReset.setVisibility(View.VISIBLE);
                mButtonShort.setVisibility(View.INVISIBLE);
                mButtonLong.setVisibility(View.INVISIBLE);
            } else {
                mButtonReset.setVisibility(View.INVISIBLE);
            }

            if(mTimeLeftInMillis == mStartTimeInMillis){
                mButtonStartPause.setText("Start");
                if(mPomodoro)
                    mTextViewStatus.setText("Pomodoro");
                mButtonShort.setVisibility(View.VISIBLE);
                mButtonLong.setVisibility(View.VISIBLE);
            }
        }
    }

    public void startService(String message){

        Intent serviceIntent = new Intent(this, NotificationService.class);
        serviceIntent.putExtra("inputExtra" , message);

        if(mTimerRunning)
            serviceIntent.putExtra("timerStatus", true);
        else
            serviceIntent.putExtra("timerStatus", false);

        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService(){
        Intent serviceIntent = new Intent(this, NotificationService.class);
        stopService(serviceIntent);
    }

    /**
     * sendOnChannel1()
     * Sends a message on Notification Channel 1 - Alarms
     * notifying user if their pomodoro timer or break is
     * up
     */
    public void sendOnChannel1(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        String content = "Your time is up!";
        if(!mPomodoro)
            content = "Your break is up!";

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("PomodoroTime")
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSound(null,0)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        notificationMananger.notify(2, notification);
    }
    @Override
    protected void onStop(){
        super.onStop();
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong(START_TIME, mStartTimeInMillis);
        editor.putLong(MILLIS_LEFT, mTimeLeftInMillis);
        editor.putBoolean(TIMER_RUNNING, mTimerRunning);
        editor.putLong(END_TIME, mEndTime);
        editor.putString(STATUS, mTextViewStatus.getText().toString());
        editor.apply();


    }

    @Override
    protected void onStart() {
        super.onStart();
        
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        mStartTimeInMillis = prefs.getLong(START_TIME, POMODORO);
        mTimeLeftInMillis = prefs.getLong(MILLIS_LEFT, mStartTimeInMillis);
        mTimerRunning = prefs.getBoolean(TIMER_RUNNING, false);
        mTextViewStatus.setText(prefs.getString(STATUS, "Pomodoro"));
        updateCountDownText();
        updateButtons();

        if(mTimerRunning){
            mEndTime = prefs.getLong(END_TIME, 0);
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();
            if(mTimeLeftInMillis < 0){
                mTimeLeftInMillis = 0;
                mTimerRunning = false;
                updateCountDownText();
                updateButtons();
            } else {
                startTimer();
            }
        }
    }
}
