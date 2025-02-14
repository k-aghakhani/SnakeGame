package com.aghakhani.snakegame;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize GameView
        gameView = findViewById(R.id.gameView);

        // Set up arrow buttons
        findViewById(R.id.btnUp).setOnClickListener(v -> gameView.setDirection(0, -1)); // Up
        findViewById(R.id.btnDown).setOnClickListener(v -> gameView.setDirection(0, 1)); // Down
        findViewById(R.id.btnLeft).setOnClickListener(v -> gameView.setDirection(-1, 0)); // Left
        findViewById(R.id.btnRight).setOnClickListener(v -> gameView.setDirection(1, 0)); // Right
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.resume(); // Start the game loop
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.pause(); // Stop the game loop
        }
    }
}