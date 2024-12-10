// shkim
package com.example.testapp.model;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.example.testapp.object.Obstacle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// 석환
public class GameModel {
    private float characterX, characterY; // 플레이어 1의 좌표
    private float characterWidth, characterHeight;
    private List<Obstacle> obstacles;
    private int score; // 각 플레이어의 점수
    private int playerHealth = 3; // 플레이어 체력
    private int totalDistance = 1000; // todo: 아직 예상이여서 추후에 정확한 값 정하기
    private int currentDistance = 0;
    private boolean isColliding = false;
    private boolean isEnd;

    public GameModel(Context context) {
        this.characterX = 0;
        this.characterY = 0;
        this.obstacles = new ArrayList<>();
        this.score = 0;
        this.isEnd = false;
    }

    public void checkCollisions() {
        if (detectCollision()) {
            if (!isColliding) {
                handleCollision();
                isColliding = true;
            }
        } else {
            isColliding = false;
        }
    }

    private void handleCollision() {
        reduceHealth();
        int health = getPlayerHealth();
        Log.d("health", String.valueOf(health));

        if (health <= 0) {
            isEnd = true;
            //endGame();
        }
    }

    private boolean detectCollision() {
        // 캐릭터 경계 사각형 계산
        float charLeft = this.characterX - (this.characterWidth / 2.0f);
        float charTop = this.characterY - (this.characterHeight / 2.0f);
        float charRight = this.characterX + (this.characterWidth / 2.0f);
        float charBottom = this.characterY + (this.characterHeight / 2.0f);

        for (Obstacle obstacle : this.obstacles) {
            // 장애물 경계 사각형 계산
            float obsLeft = obstacle.getX();
            float obsTop = obstacle.getY();
            float obsRight = obstacle.getX() + obstacle.getWidth();
            float obsBottom = obstacle.getY() + obstacle.getHeight();

            // AABB 충돌 판정
            if (charLeft > obsRight) continue;
            if (charTop > obsBottom) continue;
            if (charRight < obsLeft) continue;
            if (charBottom < obsTop) continue;

            return true; // 충돌 발생
        }

        return false; // 충돌 없음
    }

    // 특정 플레이어 위치 이동
    public void move(float dx, float dy) {
        this.characterX += dx;
        this.characterY += dy;
    }

    public void setWidthAndHeight(float width, float height) {
        this.characterWidth = width;
        this.characterHeight = height;
    }

    // 장애물 추가
    private void addObstacle(int x, int y, int width, int height) {
        obstacles.add(new Obstacle(x, y, width, height));
    }

    public void generateRandomObstacles(
            int obstacleCount,
            int gap,
            ImageView character
    ) {
        Random random = new Random();
        obstacles.clear();

        int groundY = (int) (character.getY() + character.getHeight());
        int previousX = (int) (character.getX() + character.getWidth()) + gap;

        for (int i = 0; i < obstacleCount; i++) {
            int width = random.nextInt(50) + 50;
            int height = random.nextInt(50) + 50;
            int x = previousX + gap;
            int y = groundY - height;

            addObstacle(x, y, width, height);
            previousX = x;
        }
    }

    public void reset() {
        this.score = 0;
        this.obstacles.clear();
    }

    public int getScore() {
        return this.score;
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public void reduceHealth() {
        playerHealth--;
    }

    public int getPlayerHealth() {
        return playerHealth;
    }

    // 종민
    public int getTotalDistance(){
        return totalDistance;
    }
    // 종민
    public int getCurrentDistance(){
        return currentDistance;
    }
    // 종민
    public void update() {
        // todo: 움직이는 거리는 정해지는 대로 바꾸기
        updateDistance(1); // 임시 거리 설정
        if (playerHealth <= 0) {
            isEnd = true;
        }
    }
    // 종민
    public void updateDistance(int distance) {
        currentDistance += distance;
        if (currentDistance > totalDistance) {
            currentDistance = totalDistance;
            isEnd = true;
        }
        // 은영
        updateScoreBasedOnDistance();

    }
    // 은영
    private void updateScoreBasedOnDistance() {
        // 단위 거리 * 점수
        score = currentDistance / 28 * 5; // 28 거리마다 5점
    }
    // 종민
    public boolean checkGameOver() {
        return isEnd;
    }
}