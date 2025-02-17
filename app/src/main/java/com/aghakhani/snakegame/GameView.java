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

    private Random random = new Random();
    private Handler handler = new Handler();

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        holder = getHolder();
        paint = new Paint();

        screenX = getResources().getDisplayMetrics().widthPixels;
        screenY = getResources().getDisplayMetrics().heightPixels;

        numBlocksX = screenX / blockSize;
        numBlocksY = screenY / blockSize;

        resetGame();
    }

    public void setControlPanelHeight(int controlPanelHeight) {
        if (!isControlPanelHeightSet) {
            numBlocksY = (screenY - controlPanelHeight) / blockSize;
            isControlPanelHeightSet = true;
        }
    }

    private void resetGame() {
        snake = new ArrayList<>();
        snake.add(new int[]{numBlocksX / 2, numBlocksY / 2});
        spawnFood();
    }

    private void spawnFood() {
        int foodX, foodY;
        do {
            foodX = random.nextInt(numBlocksX);
            foodY = random.nextInt(numBlocksY);
        } while (isSnakeAt(foodX, foodY));

        food = new int[]{foodX, foodY};
    }

    private boolean isSnakeAt(int x, int y) {
        for (int[] part : snake) {
            if (part[0] == x && part[1] == y) return true;
        }
        return false;
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            control();
        }
    }

    private void update() {
        int newHeadX = snake.get(0)[0] + directionX;
        int newHeadY = snake.get(0)[1] + directionY;

        // چک کردن برخورد با دیوار یا خودش
        if (newHeadX < 0 || newHeadX >= numBlocksX || newHeadY < 0 || newHeadY >= numBlocksY || isSnakeAt(newHeadX, newHeadY)) {
            resetGame();
            return;
        }

        // اضافه کردن سر جدید مار
        snake.add(0, new int[]{newHeadX, newHeadY});

        // چک کردن خوردن غذا
        if (newHeadX == food[0] && newHeadY == food[1]) {
            spawnFood();
        } else {
            // حذف دم مار
            snake.remove(snake.size() - 1);
        }
    }

    private void draw() {
        if (holder.getSurface().isValid()) {
            Canvas canvas = holder.lockCanvas();
            canvas.drawColor(Color.BLACK);

            paint.setColor(Color.GREEN);
            for (int[] part : snake) {
                canvas.drawRect(part[0] * blockSize, part[1] * blockSize, (part[0] + 1) * blockSize, (part[1] + 1) * blockSize, paint);
            }

            paint.setColor(Color.RED);
            canvas.drawCircle(food[0] * blockSize + blockSize / 2, food[1] * blockSize + blockSize / 2, blockSize / 2, paint);

            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setDirection(int dx, int dy) {
        if ((dx != 0 && directionX == 0) || (dy != 0 && directionY == 0)) {
            directionX = dx;
            directionY = dy;
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
}
