// shkim
package com.example.testapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameModel {

    private int playerCount; // 플레이어 수
    private float character1X, character1Y; // 플레이어 1의 좌표
    private float character2X, character2Y; // 플레이어 2의 좌표
    private float speedX, speedY; // 이동 속도
    private List<Obstacle> obstacles; // 장애물 리스트
    private int score; // 점수
    private String gameState; // 게임 상태

    // 생성자: 1인용 또는 2인용 초기화
    public GameModel(int playerCount) {
        this.playerCount = playerCount;
        this.character1X = 100; // 초기 x 위치
        this.character1Y = 500; // 초기 y 위치
        if (playerCount == 2) {
            this.character2X = 100;
            this.character2Y = 500;
        }
        this.obstacles = new ArrayList<>();
        this.score = 0;
        this.gameState = "시작";
    }

    // 플레이어 이동
    public void movePlayer(int player, int dx, int dy) {
        if (player == 1) {
            character1X += dx;
            character1Y += dy;
        } else if (player == 2 && playerCount == 2) {
            character2X += dx;
            character2Y += dy;
        }
    }

    // 특정 플레이어 위치 이동
    public void move(float dx, float dy) {
        this.character1X += dx;
        this.character1Y += dy;
    }

    // 충돌 검사
    public boolean checkCollision(int player) {
        float playerX, playerY;

        if (player == 1) {
            playerX = character1X;
            playerY = character1Y;
        } else if (player == 2 && playerCount == 2) {
            playerX = character2X;
            playerY = character2Y;
        } else {
            return false; // 잘못된 플레이어
        }

        for (Obstacle obstacle : obstacles) {
            if (playerX < obstacle.getX() + obstacle.getWidth() &&
                    playerX + 50 > obstacle.getX() &&
                    playerY < obstacle.getY() + obstacle.getHeight() &&
                    playerY + 50 > obstacle.getY()) {
                return true;
            }
        }

        return false;
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
            int characterX,
            int characterWidth
    ) {
        Random random = new Random();
        obstacles.clear();

        int previousX = characterX + characterWidth + 100; // 캐릭터의 끝 지점부터 시작
        for (int i = 0; i < obstacleCount; i++) {
            int width = random.nextInt(50) + 50; // 장애물 너비 (50~100)
            int height = random.nextInt(50) + 50; // 장애물 높이 (50~100)
            int gap = random.nextInt(maxGap - minGap + 1) + minGap; // minGap ~ maxGap 범위의 간격 생성

            // 장애물의 X 좌표는 이전 장애물의 끝 지점 + 간격
            int x = previousX + gap;

            // 화면 너비를 초과하면 화면 끝에 생성
            if (x + width > screenWidth) {
                x = screenWidth - width;
            }

            int y = screenHeight - groundHeight - height; // 땅 위에 위치

            addObstacle(x, y, width, height);
            previousX = x; // 현재 X 좌표를 다음 계산을 위해 저장
        }
    }

    // 장애물 제거
    public boolean removeObstacle(int x, int y) {
        return obstacles.removeIf(obstacle -> obstacle.getX() == x && obstacle.getY() == y);
    }

    // 점수 업데이트
    public void updateScore(int player) {
        score++;
    }

    // 게임 상태 설정
    public void setGameState(String state) {
        this.gameState = state;
    }

    // 상태 업데이트
    public void update() {
        this.character1X += speedX;
        this.character1Y += speedY;
    }

    // 상태 초기화
    public void reset() {
        this.character1X = 100;
        this.character1Y = 500;
        if (playerCount == 2) {
            this.character2X = 100;
            this.character2Y = 500;
        }
        this.score = 0;
        this.obstacles.clear();
    }

    // Getter 및 Setter
    public float getPlayer1X() {
        return character1X;
    }

    public float getPlayer1Y() {
        return character1Y;
    }

    public float getPlayer2X() {
        return character2X;
    }

    public float getPlayer2Y() {
        return character2Y;
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public int getScore() {
        return score;
    }
}
