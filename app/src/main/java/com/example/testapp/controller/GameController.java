package com.example.testapp.controller;

import android.os.Handler;

import com.example.testapp.model.GameModel;
import com.example.testapp.object.Obstacle;
import com.example.testapp.view.GameView;

import java.util.List;

public class GameController {

    private int playerCount;
    private GameModel playerModel; //, player2Model;
    private GameView gameView;
    private boolean isPaused = false;
    private boolean isJumping = false; // 점프 상태 관리
    private boolean isSliding = false; // 슬라이딩 상태 관리
    private Handler handler = new Handler();
    private Runnable gameLoop;
    private List<Obstacle> obstacles; // 장애물 목록
    public GameState gameState = GameState.Initial;

    // 생성자: View와 Model 객체를 초기화
    public GameController(GameModel playerModel, GameView gameView, int playerCount, List<Obstacle> obstacles) {
        this.playerModel = playerModel;
        this.playerCount = playerCount;
        this.gameView = gameView;
        this.obstacles = obstacles;

        if (playerCount != 1) {
            throw new IllegalArgumentException("playerCount 오류");
        }
    }
    // 점프
    public void jumping() {
        if (isJumping||isSliding)
            return; // 점프 및 슬라이딩 중이면 점프 방지
        isJumping = true;
        gameView.jump();
//        updateView(); // 점프 중 모션 적용했습니다.
        playerModel.move(0, -200); // y축 위로 200 이동
        isJumping = false;
    }

    // 슬라이딩
    public void sliding() {
        if (isSliding||isJumping)
            return; // 슬라이딩 중 및 점프 중이면 점프 방지
        isSliding = true; // 슬라이딩 상태 시작
        gameView.slide();
//        updateView(); // 슬라이드 이미지 적용
        isSliding = false;
    }

    // 게임 일시정지
    public void pauseGame() {
        if (!isPaused) {
            isPaused = true;
            gameState = GameState.Paused;
            handler.removeCallbacks(gameLoop); // 게임 루프 정지
            for (Obstacle obstacle : obstacles) {
                obstacle.pause(); // 모든 장애물 정지
            }
        }
    }
    // 게임 재게
    public void resumeGame() {
        if (isPaused) {
            isPaused = false;
            gameState = GameState.Running;
            for (Obstacle obstacle : obstacles) {
                obstacle.resume(); // 모든 장애물 이동
            }
            handler.post(gameLoop); // 게임 루프 재개
        }
    }

    // 게임 상태 업데이트
    private void updateGameState() {
        if (playerCount >= 1) {
            playerModel.update();
        }
        if (playerModel.checkGameOver()) {
            endGame(); // 종료 처리
        }
    }
    // 게임 초기화
    public void resetGame() {
        playerModel.reset();
    }
    public void moveCharacter(int distance){
        playerModel.updateDistance(distance);
    }
    public GameModel getGameModel() {
        return playerModel; // GameModel 반환
    }
    public void endGame() {
        // 필요한 게임 종료 처리
        handler.removeCallbacks(gameLoop); // 게임 루프 종료
        for (Obstacle obstacle : obstacles) {
            obstacle.pause(); // 장애물 정지
        }
        // 게임 모델 및 뷰 초기화 및 종료 상태 표시
        resetGame();
        gameState = GameState.End; // 상태 변경
        System.out.println("게임 종료");
    }
    // 게임 상태 관리 코드
    public enum GameState {
        Error(-1), Initial(0), Running(1), Paused(2), End(3);
        private final int value;
        private GameState(int value) {
            this.value = value;
        }
        public int value() {
            return value;
        }
        public static GameState stateFromInteger(int value) {
            switch (value) {
                case -1:
                    return Error;
                case 0:
                    return Initial;
                case 1:
                    return Running;
                case 2:
                    return Paused;
                case 3:
                    return End;
                default:
                    return null;
            }
        }
    }
}
