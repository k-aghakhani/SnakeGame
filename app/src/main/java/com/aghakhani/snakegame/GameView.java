package com.aghakhani.snakegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    private Thread gameThread; // Thread for the game loop
    private boolean isPlaying; // Flag to control the game loop
    private SurfaceHolder holder; // SurfaceHolder to manage the canvas
    private Paint paint; // Paint object for drawing

    private int screenX, screenY; // Screen dimensions
    private int blockSize = 20; // Size of each block in the grid
    private int numBlocksX, numBlocksY; // Number of blocks in X and Y directions

    private ArrayList<Point> snake; // List of points representing the snake's body
    private Point food; // Position of the food
    private Random random; // Random number generator

    private int directionX = 1, directionY = 0; // Initial movement direction (right)
    private long lastUpdateTime; // Time of the last update
    private int score = 0; // Player's score

    public GameView(Context context) {
        super(context);

        // Initialize variables
        holder = getHolder();
        paint = new Paint();
        random = new Random();

        // Get screen dimensions
        screenX = getResources().getDisplayMetrics().widthPixels;
        screenY = getResources().getDisplayMetrics().heightPixels;

        // Calculate the number of blocks that fit on the screen
        numBlocksX = screenX / blockSize;
        numBlocksY = screenY / blockSize;

        // Initialize the snake
        snake = new ArrayList<>();
        snake.add(new Point(numBlocksX / 2, numBlocksY / 2)); // Start in the center of the screen

        // Generate initial food
        generateFood();
    }

    @Override
    public void run() {
        while (isPlaying) {
            update(); // Update game state
            draw(); // Draw game objects
            sleep(); // Control frame rate
        }
    }

    private void update() {
        // Get the current time
        long currentTime = System.currentTimeMillis();

        // Update the game state every 150 milliseconds
        if (currentTime - lastUpdateTime > 150) {
            lastUpdateTime = currentTime;

            // Move the snake
            Point head = new Point(snake.get(0));
            head.x += directionX;
            head.y += directionY;

            // Check for collisions with walls or itself
            if (head.x < 0 || head.x >= numBlocksX || head.y < 0 || head.y >= numBlocksY || snake.contains(head)) {
                stopGame(); // End the game on collision
                return;
            }

            // Add the new head to the snake
            snake.add(0, head);

            // Check if the snake eats the food
            if (head.equals(food)) {
                score++; // Increase score
                generateFood(); // Generate new food
            } else {
                snake.remove(snake.size() - 1); // Remove the tail if no food was eaten
            }
        }
    }

    private void draw() {
        if (holder.getSurface().isValid()) {
            Canvas canvas = holder.lockCanvas();

            // Clear the canvas
            canvas.drawColor(Color.BLACK);

            // Draw the snake
            paint.setColor(Color.GREEN);
            for (Point point : snake) {
                canvas.drawRect(
                        point.x * blockSize,
                        point.y * blockSize,
                        (point.x + 1) * blockSize,
                        (point.y + 1) * blockSize,
                        paint
                );
            }

            // Draw the food
            paint.setColor(Color.RED);
            canvas.drawRect(
                    food.x * blockSize,
                    food.y * blockSize,
                    (food.x + 1) * blockSize,
                    (food.y + 1) * blockSize,
                    paint
            );

            // Draw the score
            paint.setColor(Color.WHITE);
            paint.setTextSize(50);
            canvas.drawText("Score: " + score, 10, 50, paint);

            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void sleep() {
        try {
            Thread.sleep(16); // Approximately 60 frames per second
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pause() {
        try {
            isPlaying = false;
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopGame() {
        isPlaying = false;
        score = 0; // Reset score
        snake.clear(); // Clear the snake
        snake.add(new Point(numBlocksX / 2, numBlocksY / 2)); // Reset snake position
        generateFood(); // Generate new food
    }

    private void generateFood() {
        food = new Point(random.nextInt(numBlocksX), random.nextInt(numBlocksY));

        // Ensure the food does not spawn inside the snake
        while (snake.contains(food)) {
            food = new Point(random.nextInt(numBlocksX), random.nextInt(numBlocksY));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            // Change direction based on touch location
            if (x < screenX / 2 && directionX == 0) { // Left side of the screen
                directionX = -1;
                directionY = 0;
            } else if (x >= screenX / 2 && directionX == 0) { // Right side of the screen
                directionX = 1;
                directionY = 0;
            } else if (y < screenY / 2 && directionY == 0) { // Top half of the screen
                directionX = 0;
                directionY = -1;
            } else if (y >= screenY / 2 && directionY == 0) { // Bottom half of the screen
                directionX = 0;
                directionY = 1;
            }
        }
        return true;
    }
}