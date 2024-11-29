package com.example.testapp.object;

public class Obstacle {
    private int x, y, width, height;
    private boolean isPaused = false;  // 장애물 상태 변수

    public Obstacle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // 종민
    // 장애물 이동
    public void move() {
        if (isPaused)
            return; // 일시정지 상태에서는 이동 금지.
        // todo: 장애물 이동 코드 작성
    }
    // 장애물 일시정지
    public void pause() {
        isPaused = true;
    }
    // 장애물 재개
    public void resume() {
        isPaused = false;
    }
    // 종민
}