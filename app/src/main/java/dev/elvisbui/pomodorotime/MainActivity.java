package dev.elvisbui.pomodorotime;

import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final long POMODORO = 1500000;
    private static final long SHORT_BREAK = 600000;
    private static final long LONG_BREAK = 900000;

    private static final String START_TIME = "startTimeInMillis";
    private static final String PREFS = "prefs";
    private static final String MILLIS_LEFT = "millisLeft";
    private static final String TIMER_RUNNING = "timerRunning";
    private static final String END_TIME = "endTime";

    private TextView mTextViewCountDown;

    private Button mButtonStartPause;
    private Button mButtonShort;
    private Button mButtonLong;
    private Button mButtonReset;

    private CountDownTimer mCountDownTimer;

    private boolean mTimerRunning;

    private long mStartTimeInMillis;
    private long mTimeLeftInMillis;
    private long mEndTime;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mTextViewCountDown = findViewById(R.id.countdown);
        mButtonStartPause = findViewById(R.id.startPauseButton);
        mButtonReset = findViewById(R.id.resetButton);
        mButtonShort = findViewById(R.id.shortButton);
        mButtonLong = findViewById(R.id.longButton);
        mButtonStartPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mTimerRunning){
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });
        
        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTimer(POMODORO);
            }
        });

        mButtonShort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTimer(SHORT_BREAK);
                startTimer();
            }
        });

    }

    private void setTimer(long milliseconds){
        mStartTimeInMillis = milliseconds;
        resetTimer();
    }

    private void startTimer() {

        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                updateButtons();
            }
        }.start();

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
        mTimeLeftInMillis = mStartTimeInMillis;
        updateCountDownText();
        updateButtons();
    }

    private void updateButtons(){
        if(mTimerRunning){
            mButtonReset.setVisibility(View.INVISIBLE);
            mButtonShort.setVisibility(View.INVISIBLE);
            mButtonStartPause.setText("Pause");
        } else {
            mButtonStartPause.setText("Resume");
            if(mTimeLeftInMillis < 500){
                mButtonStartPause.setVisibility(View.INVISIBLE);
            } else {
                mButtonStartPause.setVisibility(View.VISIBLE);
            }

            if(mTimeLeftInMillis < mStartTimeInMillis){
                mButtonReset.setVisibility(View.VISIBLE);
            } else {
                mButtonReset.setVisibility(View.INVISIBLE);
            }

            if(mTimeLeftInMillis == mStartTimeInMillis){
                mButtonStartPause.setText("Start");
                mButtonShort.setVisibility(View.VISIBLE);
            }
        }
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

        editor.apply();

        if(mCountDownTimer != null){
            mCountDownTimer.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        mStartTimeInMillis = prefs.getLong(START_TIME, POMODORO);
        mTimeLeftInMillis = prefs.getLong(MILLIS_LEFT, mStartTimeInMillis);
        mTimerRunning = prefs.getBoolean(TIMER_RUNNING, false);

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
