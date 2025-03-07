package com.aghakhani.snakegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    private Thread gameThread;
    private boolean isPlaying;
    private SurfaceHolder holder;
    private Paint paint;
    private int screenX, screenY;
    private int blockSize = 30;
    private int numBlocksX, numBlocksY;

    private ArrayList<int[]> snake;
    private int[] food;
    private int directionX = 1, directionY = 0;
    private boolean isControlPanelHeightSet = false;
    private int score = 0; // Score variable to track player's points

    private Random random = new Random();
    private Handler handler = new Handler();

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize SurfaceHolder and Paint
        holder = getHolder();
        paint = new Paint();

        // Get screen dimensions
        screenX = getResources().getDisplayMetrics().widthPixels;
        screenY = getResources().getDisplayMetrics().heightPixels;

        // Calculate number of blocks based on screen size
        numBlocksX = screenX / blockSize;
        numBlocksY = screenY / blockSize;

        // Start a new game
        resetGame();
    }

    // Set the playable area height by subtracting control panel height
    public void setControlPanelHeight(int controlPanelHeight) {
        if (!isControlPanelHeightSet) {
            numBlocksY = (screenY - controlPanelHeight) / blockSize; // Adjust playable area
            isControlPanelHeightSet = true;
        }
    }

    // Reset the game state
    private void resetGame() {
        snake = new ArrayList<>();
        snake.add(new int[]{numBlocksX / 2, numBlocksY / 2}); // Start snake in the middle
        spawnFood();
        score = 0; // Reset score when game restarts
    }

    // Spawn food at a random location not occupied by the snake and above control panel
    private void spawnFood() {
        int foodX, foodY;
        do {
            foodX = random.nextInt(numBlocksX);
            // Ensure food spawns only in the visible area (above control panel)
            foodY = random.nextInt(numBlocksY); // numBlocksY is already adjusted for control panel
        } while (isSnakeAt(foodX, foodY));

        food = new int[]{foodX, foodY};
    }

    // Check if the snake occupies a given position
    private boolean isSnakeAt(int x, int y) {
        for (int[] part : snake) {
            if (part[0] == x && part[1] == y) return true;
        }
        return false;
    }

    @Override
    public void run() {
        while (isPlaying) {
            update(); // Update game state
            draw();   // Draw the game
            control(); // Control game speed
        }
    }

    // Update snake position and game logic
    private void update() {
        int newHeadX = snake.get(0)[0] + directionX;
        int newHeadY = snake.get(0)[1] + directionY;

        // Check for collision with walls or self
        if (newHeadX < 0 || newHeadX >= numBlocksX || newHeadY < 0 || newHeadY >= numBlocksY || isSnakeAt(newHeadX, newHeadY)) {
            resetGame();
            return;
        }

        // Add new head to snake
        snake.add(0, new int[]{newHeadX, newHeadY});

        // Check if snake eats the food
        if (newHeadX == food[0] && newHeadY == food[1]) {
            spawnFood();
            score += 10; // Increase score by 10 when food is eaten
        } else {
            // Remove tail if no food is eaten
            snake.remove(snake.size() - 1);
        }
    }

    // Draw the game elements on the canvas
    private void draw() {
        if (holder.getSurface().isValid()) {
            Canvas canvas = holder.lockCanvas();
            canvas.drawColor(Color.BLACK); // Clear screen with black background

            // Draw the snake
            paint.setColor(Color.GREEN);
            for (int[] part : snake) {
                canvas.drawRect(part[0] * blockSize, part[1] * blockSize, (part[0] + 1) * blockSize, (part[1] + 1) * blockSize, paint);
            }

            // Draw the food
            paint.setColor(Color.RED);
            canvas.drawCircle(food[0] * blockSize + blockSize / 2, food[1] * blockSize + blockSize / 2, blockSize / 2, paint);

            // Draw the score
            paint.setColor(Color.WHITE);
            paint.setTextSize(50);
            canvas.drawText("Score: " + score, 20, 50, paint);

            holder.unlockCanvasAndPost(canvas);
        }
    }

    // Control game speed
    private void control() {
        try {
            Thread.sleep(150); // Pause to control frame rate
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Set snake direction based on user input
    public void setDirection(int dx, int dy) {
        if ((dx != 0 && directionX == 0) || (dy != 0 && directionY == 0)) {
            directionX = dx;
            directionY = dy;
        }
    }

    // Resume the game
    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    // Pause the game
    public void pause() {
        try {
            isPlaying = false;
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}