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
import android.widget.ProgressBar;
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
        getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.activity_main);

        screamFace = findViewById(R.id.scream_face);
        loadingOverlay = findViewById(R.id.loading_overlay);
        loadingText = findViewById(R.id.loading_text);
        screamFace.setVisibility(View.INVISIBLE);

        animateLoadingText();

        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(this::triggerScreamer, TRIGGER_DELAY_MS);
    }

    private void animateLoadingText() {
        final String[] states = {"Загрузка фото...", "Загрузка фото..", "Загрузка фото.", "Анализ библиотеки...", "Синхронизация..."};
        final int[] index = {0};
        handler = new Handler(Looper.getMainLooper());
        Runnable cycle = new Runnable() {
            @Override public void run() {
                if (loadingText != null) {
                    loadingText.setText(states[index[0] % states.length]);
                    index[0]++;
                    handler.postDelayed(this, 500);
                }
            }
        };
        handler.post(cycle);
    }

    private void triggerScreamer() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (am != null) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        }
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.screamer);
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(1f, 1f);
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(mp -> mp.release());
            }
        } catch (Exception ignored) {}

        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
