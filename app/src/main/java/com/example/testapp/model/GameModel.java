// shkim
package com.example.testapp.model;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.example.testapp.controller.GameController;
import com.example.testapp.object.Obstacle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameModel {
    private float characterX, characterY; // 플레이어 1의 좌표
    private float characterWidth, characterHeight;
    private List<Obstacle> obstacles;
    private int score; // 각 플레이어의 점수
    private int playerHealth = 3; // 플레이어 체력
    private String gameState; // 게임 상태
    private int totalDistance = 1000; // todo: 아직 예상이여서 추후에 정확한 값 정하기
    private int currentDistance = 0;
    private boolean isColliding = false;
    private GameController gameController;
    private boolean isEnd;

    // 생성자: 1인용 또는 2인용 초기화
    public GameModel(Context context) {
        // todo: 초기 위치는 임시로 설정했습니다.
        this.characterX = 0;
        this.characterY = 0;
        this.obstacles = new ArrayList<>();
        this.score = 0;
        this.gameState = "시작";
        this.isEnd = false;
    }

    public void checkCollisions() {
        if (detectCollision()) {
            if (!isColliding) { // 새로 충돌한 경우에만 처리
                handleCollision();
                isColliding = true;
            }
        } else {
            isColliding = false; // 충돌이 해소되면 다시 감지 가능
        }
    }

    private void handleCollision() {
        reduceHealth();
        int health = getPlayerHealth();
        Log.d("health", String.valueOf(health));

        if (health <= 0) {
            isEnd = true;
            endGame();
        }
    }

    public boolean detectCollision() {
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

    private void endGame() {
        // 게임 종료 로직
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
    public void addObstacle(int x, int y, int width, int height) {
        obstacles.add(new Obstacle(x, y, width, height));
    }

    public void generateRandomObstacles(
            int obstacleCount,
            int screenWidth,
            int screenHeight,
            int groundHeight,
            int minGap,
            int maxGap,
            ImageView character
    ) {
        Random random = new Random();
        obstacles.clear();

        // 땅의 Y 좌표를 기준으로 계산
        int groundY = screenHeight - groundHeight; // 땅의 높이 기준
        int previousX = (int) (character.getX() + character.getWidth()) + 100; // 캐릭터 끝 지점부터 시작

        for (int i = 0; i < obstacleCount; i++) {
            int width = random.nextInt(50) + 50; // 장애물 너비 (50~100)
            int height = random.nextInt(50) + 50; // 장애물 높이 (50~100)
            int gap = random.nextInt(maxGap - minGap + 1) + minGap; // 장애물 간 간격

            // 장애물의 X 좌표는 이전 장애물의 끝 지점 + 간격
            int x = previousX + gap;

            // 장애물의 Y 좌표는 땅 위에 위치
            int y = groundY - height;

            addObstacle(x, y, width, height);
            previousX = x; // 현재 장애물의 끝 지점을 다음 계산을 위해 저장
        }
    }
    // 장애물 제거
    public boolean removeObstacle(int x, int y) {
        return obstacles.removeIf(obstacle -> obstacle.getX() == x && obstacle.getY() == y);
    }
    // 게임 상태 설정
    public void setGameState(String state) {
        this.gameState = state;
    }
    // 상태 초기화
    public void reset() {
        this.characterX = 100;
        this.characterY = 500;
        this.score = 0;
        this.obstacles.clear();
    }
    public void update() {
        // todo: 움직이는 거리는 정해지는 대로 바꾸기
        updateDistance(10); // 임시 거리 설정
    }

    public int getScore() {
        return this.score;
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public void removeObstacle(Obstacle obstacle) {
        obstacles.remove(obstacle);
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
    public int getCurrentDistance(){
        return currentDistance;
    }
    public void updateDistance(int distance) {
        currentDistance += distance;
        if (currentDistance > totalDistance) {
            currentDistance = totalDistance;
            isEnd = true;
            endGame();
        }
        //gameController.updateDistance(currentDistance);
    }
    public boolean checkGameOver() {
        return isEnd;
    }
    public int getHealth(){
        return playerHealth;
    }

    // 종민
}
