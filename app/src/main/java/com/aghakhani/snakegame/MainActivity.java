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

        // Initialize GameView and control panel
        gameView = findViewById(R.id.gameView);
        if (gameView == null) {
            throw new IllegalStateException("GameView not found in layout!");
        }
        View controlPanel = findViewById(R.id.controlPanel);

        // Set control panel height after UI is laid out
        controlPanel.post(() -> {
            if (gameView != null) {
                gameView.setControlPanelHeight(controlPanel.getHeight());
            }
        });

        // Set up button listeners for direction control
        findViewById(R.id.btnUp).setOnClickListener(v -> gameView.setDirection(0, -1));
        findViewById(R.id.btnDown).setOnClickListener(v -> gameView.setDirection(0, 1));
        findViewById(R.id.btnLeft).setOnClickListener(v -> gameView.setDirection(-1, 0));
        findViewById(R.id.btnRight).setOnClickListener(v -> gameView.setDirection(1, 0));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.resume(); // Resume the game when activity resumes
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.pause(); // Pause the game when activity pauses
        }
    }
}