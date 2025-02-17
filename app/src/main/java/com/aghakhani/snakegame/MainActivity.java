package com.aghakhani.snakegame;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameView = findViewById(R.id.gameView);
        View controlPanel = findViewById(R.id.controlPanel);

        // دریافت ارتفاع کنترل پنل بعد از تنظیم UI
        controlPanel.post(() -> {
            if (gameView != null) {
                gameView.setControlPanelHeight(controlPanel.getHeight());
            }
        });

        findViewById(R.id.btnUp).setOnClickListener(v -> gameView.setDirection(0, -1));
        findViewById(R.id.btnDown).setOnClickListener(v -> gameView.setDirection(0, 1));
        findViewById(R.id.btnLeft).setOnClickListener(v -> gameView.setDirection(-1, 0));
        findViewById(R.id.btnRight).setOnClickListener(v -> gameView.setDirection(1, 0));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.pause();
        }
    }
}
