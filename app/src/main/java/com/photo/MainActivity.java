package com.photo;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int TRIGGER_DELAY_MS = 3500;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private ImageView screamFace;
    private View loadingOverlay;
    private TextView loadingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        screamFace = findViewById(R.id.scream_face);
        loadingOverlay = findViewById(R.id.loading_overlay);
        loadingText = findViewById(R.id.loading_text);
        screamFace.setVisibility(View.INVISIBLE);
        handler = new Handler(Looper.getMainLooper());
        animateLoadingText();
        handler.postDelayed(this::triggerScreamer, TRIGGER_DELAY_MS);
    }

    private void animateLoadingText() {
        final String[] states = {"Загрузка фото...", "Загрузка фото..", "Загрузка фото.", "Анализ...", "Синхронизация..."};
        final int[] index = {0};
        Runnable cycle = new Runnable() {
            @Override
            public void run() {
                loadingText.setText(states[index[0] % states.length]);
                index[0]++;
                handler.postDelayed(this, 500);
            }
        };
        handler.post(cycle);
    }

    private void triggerScreamer() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (am != null) am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.screamer);
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(1f, 1f);
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mp -> mp.release());
            }
        } catch (Exception ignored) {}
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (v != null && v.hasVibrator()) {
            v.vibrate(VibrationEffect.createWaveform(new long[]{0, 500, 100, 500, 100, 500}, -1));
        }
        loadingOverlay.setVisibility(View.GONE);
        screamFace.setVisibility(View.VISIBLE);
        flickerFace();
    }

    private void flickerFace() {
        final boolean[] visible = {true};
        final int[] count = {0};
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (count[0] < 10) {
                    screamFace.setVisibility(visible[0] ? View.INVISIBLE : View.VISIBLE);
                    visible[0] = !visible[0];
                    count[0]++;
                    handler.postDelayed(this, 80);
                } else {
                    screamFace.setVisibility(View.VISIBLE);
                }
            }
        }, 80);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) handler.removeCallbacksAndMessages(null);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    }
