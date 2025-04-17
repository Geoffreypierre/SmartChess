package com.example.smartchess.chess.playerinfos;

import android.os.CountDownTimer;


import android.os.CountDownTimer;

public class ChessTimer {

    private long millisLeft;
    private long interval;
    private CountDownTimer countDownTimer;

    private Runnable onTickRunnable;
    private OnTimerFinishedListener onTimerFinishedListener;

    public interface OnTimerFinishedListener {
        void onFinished();
    }

    public ChessTimer(long durationMillis, long intervalMillis) {
        this.millisLeft = durationMillis;
        this.interval = intervalMillis;
    }

    public void start() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(millisLeft, interval) {
            @Override
            public void onTick(long millisUntilFinished) {
                millisLeft = millisUntilFinished;
                if (onTickRunnable != null) onTickRunnable.run();
            }

            @Override
            public void onFinish() {
                millisLeft = 0;
                if (onTickRunnable != null) onTickRunnable.run();
                if (onTimerFinishedListener != null) onTimerFinishedListener.onFinished();
            }
        }.start();
    }

    public void pause() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    public void setOnTickRunnable(Runnable r) {
        this.onTickRunnable = r;
    }

    public void setOnTimerFinishedListener(OnTimerFinishedListener listener) {
        this.onTimerFinishedListener = listener;
    }

    public long getMillisLeft() {
        return millisLeft;
    }
}
