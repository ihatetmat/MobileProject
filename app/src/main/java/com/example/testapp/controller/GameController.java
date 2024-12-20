package com.example.testapp.controller;

import android.os.Handler;

import com.example.testapp.model.GameModel;
import com.example.testapp.view.GameView;

public class GameController {

    private int playerCount; // 플레이어 수
    private GameModel playerModel;
    private GameView gameView;
    private boolean isPaused = false; // Pause 기능 관리
    private boolean isJumping = false; // 점프 상태 관리
    private boolean isSliding = false; // 슬라이딩 상태 관리
    private Handler handler = new Handler();
    private Runnable gameLoop;
    private GameState gameState = GameState.Initial;

    // 생성자: View와 Model 객체를 초기화
    public GameController(GameModel playerModel, GameView gameView, int playerCount) {
        this.playerModel = playerModel;
        this.playerCount = playerCount;
        this.gameView = gameView;
    }
    // 점프
    public void jumping() {
        if (isJumping||isSliding)
            return; // 점프 및 슬라이딩 중이면 점프 방지
        isJumping = true;
        gameView.jump();
        playerModel.move(0, -200); // y축 위로 200 이동
        isJumping = false;
    }

    // 슬라이딩
    public void sliding() {
        if (isSliding||isJumping)
            return; // 슬라이딩 중 및 점프 중이면 점프 방지
        isSliding = true; // 슬라이딩 상태 시작
        gameView.slide();
        isSliding = false;
    }

    public void startGame(){
        gameState = GameState.Running;
    }
    // 게임 일시정지
    public void pauseGame() {
        if (!isPaused) {    
            isPaused = true;
            gameState = GameState.Paused;
            gameView.setPause(true);
            handler.removeCallbacks(gameLoop); // 게임 루프 정지
        }
    }
    // 게임 재게
    public void resumeGame() {
        if (isPaused) {
            isPaused = false;
            gameState = GameState.Running;
            gameView.setPause(false);
            handler.post(gameLoop); // 게임 루프 재개
        }
    }

    // 게임 상태 업데이트
    public void updateGameState() {
        if(gameState == GameState.Running){
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
    public GameModel getGameModel() {
        return playerModel; // GameModel 반환
    }
    public void endGame() {
        // 필요한 게임 종료 처리
        handler.removeCallbacks(gameLoop); // 게임 루프 종료
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
    // GameState 반환 함수
    public GameState getGameState() {
        return gameState;
    }
}