package com.example.testapp.controller;

import android.os.Handler;
import android.widget.ImageView;

import com.example.testapp.model.GameModel;
import com.example.testapp.object.Obstacle;
import com.example.testapp.view.GameView;

import java.util.List;

public class GameController {

    private int playerCount;
    private ImageView playerView; //, player2View;
    private GameModel playerModel; //, player2Model;
    private GameView gameView;
    private boolean isPaused = false;
    private boolean isJumping = false; // 점프 상태 관리
    private boolean isSliding = false; // 슬라이딩 상태 관리
    private Handler handler = new Handler();
    private Runnable gameLoop;
    private List<Obstacle> obstacles; // 장애물 목록
    private String sessionId; // 세션 ID
    private String playerId;  // "player1" 또는 "player2"

    // 생성자: View와 Model 객체를 초기화
    public GameController(ImageView playerView, GameModel playerModel,GameView gameView, int playerCount, List<Obstacle> obstacles, String sessionId, String playerId) {
        this.playerView = playerView;
        this.playerModel = playerModel;
        this.playerCount = playerCount;
        this.gameView = gameView;
        this.obstacles = obstacles;
        this.sessionId = sessionId;
        this.playerId = playerId;
        this.sessionId = sessionId;
        this.playerId = playerId;

        if (playerCount != 1 ) {
            throw new IllegalArgumentException("playerCount 오류");
        }
    }
    // 점프
    public void jumping() {
        if (isJumping||isSliding)
            return; // 점프 및 슬라이딩 중이면 점프 방지
        isJumping = true;
        updateView(); // 점프 중 모션 적용했습니다.
        playerModel.move(0, -200); // y축 위로 200 이동
        isJumping = false;

    }
    // 슬라이딩
    public void sliding() {
        if (isSliding||isJumping)
            return; // 슬라이딩 중 및 점프 중이면 점프 방지
        isSliding = true; // 슬라이딩 상태 시작
        updateView(); // 슬라이드 이미지 적용
        isSliding = false;
    }
    // 게임 일시정지
    public void pauseGame() {
        if (!isPaused) {
            isPaused = true;
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
            for (Obstacle obstacle : obstacles) {
                obstacle.resume(); // 모든 장애물 이동
            }
            handler.post(gameLoop); // 게임 루프 재개
        }
    }
    // 게임 상태 업데이트
    private void updateGameState() {
        playerModel.update();
        updateView();
        if (playerModel.checkGameOver()) {
            //endGame(); // 종료 처리
        }
        // todo: 충돌, 종료 조건, 스코어 계산 등 추가
    }
    // 뷰 업데이트
    private void updateView(){
        if (isJumping) {
            gameView.invalidateView(GameView.PlayerState.JUMPING);
        } else if (isSliding) {
            gameView.invalidateView(GameView.PlayerState.SLIDING);
        } else {
            gameView.invalidateView(GameView.PlayerState.DEFAULT);
        }
    }

    // 게임 초기화
    public void resetGame() {
        playerModel.reset();
        updateView();
    }
    public void moveCharacter(int distance){
        playerModel.updateDistance(distance);
    }
    public GameModel getGameModel() {
        return playerModel; // GameModel 반환
    }

}
