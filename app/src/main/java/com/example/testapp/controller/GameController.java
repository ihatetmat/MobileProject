package com.example.testapp.controller;

import android.os.Handler;
import android.widget.ImageView;

import com.example.testapp.model.GameModel;
import com.example.testapp.view.GameView;

public class GameController {

    private int playerCount;
    private ImageView player1View, player2View;
    private GameModel player1Model, player2Model;
    private GameView gameView;
    private boolean isPaused = false;
    private boolean isJumping = false; // 점프 상태 관리
    private boolean isSliding = false; // 슬라이딩 상태 관리
    private Handler handler = new Handler();
    private Runnable gameLoop;

    // 생성자: View와 Model 객체를 초기화
    public GameController(ImageView player1View, GameModel player1Model,GameView gameView, int playerCount) {
        this.player1View = player1View;
        this.player1Model = player1Model;
        this.playerCount = playerCount;
        this.gameView = gameView;

        if (playerCount != 1) {
            throw new IllegalArgumentException("playerCount 오류");
        }
    }

    public GameController(ImageView player1View, GameModel player1Model,
                          ImageView player2View, GameModel player2Model) {
        this.player1View = player1View;
        this.player1Model = player1Model;
        this.player2View = player2View;
        this.player2Model = player2Model;
        this.playerCount = 2;
    }

    // 점프
    public void jumping() {
        if (isJumping||isSliding)
            return; // 점프 및 슬라이딩 중이면 점프 방지
        isJumping = true;
        updateView(); // 점프 중 모션 적용했습니다.
        player1Model.move(0, -200); // y축 위로 200 이동
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

    // 게임 일시정지 또는 재개
    public void pauseGame() {
        isPaused = !isPaused;
        if (isPaused) {
            handler.removeCallbacks(gameLoop);
        } else {
            handler.post(gameLoop);
        }
    }

    // 게임 상태 업데이트
    private void updateGameState() {
        if (playerCount >= 1) {
            player1Model.update();
            updateView(player1View, player1Model);
        }
        if (playerCount == 2) {
            player2Model.update();
            updateView(player2View, player2Model);
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

    private void updateView(ImageView view, GameModel model) {
        //캐릭터 위치 설정
        //
    }

    // 게임 초기화
    public void resetGame() {
        player1Model.reset();
        if (playerCount == 2) {
            player2Model.reset();
        }
        updateView(player1View, player1Model);
        if (playerCount == 2) {
            updateView(player2View, player2Model);
        }
    }
    public void moveCharacter(int distance){
        player1Model.updateDistance(distance);
        if (playerCount==2){
            player2Model.updateDistance(distance);
        }
    }
    public GameModel getGameModel() {
        return player1Model; // GameModel 반환
    }

}
