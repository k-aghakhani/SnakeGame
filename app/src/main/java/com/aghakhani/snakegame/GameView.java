package com.aghakhani.snakegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
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
    private ArrayList<int[]> obstacles; // List to store obstacle positions
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
        spawnObstacles(); // Spawn obstacles when resetting the game
        score = 0; // Reset score when game restarts
    }

    // Spawn food at a random location not occupied by snake or obstacles
    private void spawnFood() {
        int foodX, foodY;
        do {
            foodX = random.nextInt(numBlocksX);
            foodY = random.nextInt(numBlocksY); // numBlocksY is adjusted for control panel
        } while (isSnakeAt(foodX, foodY) || isObstacleAt(foodX, foodY));
        food = new int[]{foodX, foodY};
    }

    // Spawn a fixed number of obstacles randomly
    private void spawnObstacles() {
        obstacles = new ArrayList<>();
        int obstacleCount = 5; // Number of obstacles, adjustable
        for (int i = 0; i < obstacleCount; i++) {
            int obsX, obsY;
            do {
                obsX = random.nextInt(numBlocksX);
                obsY = random.nextInt(numBlocksY);
            } while (isSnakeAt(obsX, obsY) || (food != null && obsX == food[0] && obsY == food[1]));
            obstacles.add(new int[]{obsX, obsY});
        }
    }

    // Check if the snake occupies a given position
    private boolean isSnakeAt(int x, int y) {
        for (int[] part : snake) {
            if (part[0] == x && part[1] == y) return true;
        }
        return false;
    }

    // Check if an obstacle occupies a given position
    private boolean isObstacleAt(int x, int y) {
        if (obstacles == null) return false;
        for (int[] obs : obstacles) {
            if (obs[0] == x && obs[1] == y) return true;
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

    // Update snake position, obstacles, and game logic
    private void update() {
        // Move obstacles randomly with 20% chance
        for (int[] obs : obstacles) {
            if (random.nextInt(10) < 2) { // 20% chance to move
                int newObsX = obs[0] + (random.nextInt(3) - 1); // Move -1, 0, or 1 horizontally
                int newObsY = obs[1] + (random.nextInt(3) - 1); // Move -1, 0, or 1 vertically

                // Ensure obstacle stays within bounds and doesn't overlap with food or snake
                if (newObsX >= 0 && newObsX < numBlocksX && newObsY >= 0 && newObsY < numBlocksY &&
                        !isSnakeAt(newObsX, newObsY) && (food[0] != newObsX || food[1] != newObsY)) {
                    obs[0] = newObsX;
                    obs[1] = newObsY;
                }
            }
        }

        // Update snake position
        int newHeadX = snake.get(0)[0] + directionX;
        int newHeadY = snake.get(0)[1] + directionY;

        // Check for collision with walls, self, or obstacles
        if (newHeadX < 0 || newHeadX >= numBlocksX || newHeadY < 0 || newHeadY >= numBlocksY ||
                isSnakeAt(newHeadX, newHeadY) || isObstacleAt(newHeadX, newHeadY)) {
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

            // Draw the obstacles
            paint.setColor(Color.GRAY);
            for (int[] obs : obstacles) {
                canvas.drawRect(obs[0] * blockSize, obs[1] * blockSize, (obs[0] + 1) * blockSize, (obs[1] + 1) * blockSize, paint);
            }

            // Draw the score
            paint.setColor(Color.WHITE);
            paint.setTextSize(50);
            canvas.drawText("Score: " + score, 20, 50, paint);

            holder.unlockCanvasAndPost(canvas);
        }
    }

    // Control game speed based on snake length
    private void control() {
        try {
            // Base speed is 150ms, decreases by 5ms for each snake segment, minimum 50ms
            int speed = 150 - (snake.size() * 5);
            Thread.sleep(Math.max(speed, 50)); // Ensure speed doesn't go below 50ms
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Set snake direction based on user input (buttons or swipe)
    public void setDirection(int dx, int dy) {
        if ((dx != 0 && directionX == 0) || (dy != 0 && directionY == 0)) {
            directionX = dx;
            directionY = dy;
        }
    }

    // Handle touch events for swipe control
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float startX = 0, startY = 0;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                float endX = event.getX();
                float endY = event.getY();
                float deltaX = endX - startX;
                float deltaY = endY - startY;

                // Determine direction based on swipe
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    // Horizontal swipe
                    if (deltaX > 50) setDirection(1, 0); // Swipe right
                    else if (deltaX < -50) setDirection(-1, 0); // Swipe left
                } else {
                    // Vertical swipe
                    if (deltaY > 50) setDirection(0, 1); // Swipe down
                    else if (deltaY < -50) setDirection(0, -1); // Swipe up
                }
                break;
        }
        return true; // Indicate that the touch event is handled
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