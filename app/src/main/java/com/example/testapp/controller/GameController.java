package com.example.testapp.controller;

import android.os.Handler;
import android.widget.ImageView;

import com.example.testapp.model.GameModel;
import com.example.testapp.object.Obstacle;
import com.example.testapp.view.GameView;

import java.util.List;

public class GameController {

    private int playerCount;
    private ImageView player1View; //, player2View;
    private GameModel player1Model; //, player2Model;
    private GameView gameView;
    private boolean isPaused = false;
    private boolean isJumping = false; // 점프 상태 관리
    private boolean isSliding = false; // 슬라이딩 상태 관리
    private Handler handler = new Handler();
    private Runnable gameLoop;
    private List<Obstacle> obstacles; // 장애물 목록

    // 생성자: View와 Model 객체를 초기화
    public GameController(ImageView player1View, GameModel player1Model,GameView gameView, int playerCount, List<Obstacle> obstacles) {
        this.player1View = player1View;
        this.player1Model = player1Model;
        this.playerCount = playerCount;
        this.gameView = gameView;
        this.obstacles = obstacles;

        if (playerCount != 1) {
            throw new IllegalArgumentException("playerCount 오류");
        }
    }
    /*
    public GameController(ImageView player1View, GameModel player1Model,
                          ImageView player2View, GameModel player2Model) {
        this.player1View = player1View;
        this.player1Model = player1Model;
        this.player2View = player2View;
        this.player2Model = player2Model;
        this.playerCount = 2;
    }
    */

    // 점프
    public void jumping() {
        if (isJumping||isSliding)
            return; // 점프 및 슬라이딩 중이면 점프 방지
        isJumping = true;
        gameView.jump();
//        updateView(); // 점프 중 모션 적용했습니다.
        player1Model.move(0, -200); // y축 위로 200 이동
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
        if (playerCount >= 1) {
            player1Model.update();
            updateView(player1View, player1Model);
        }
        /*
        if (playerCount == 2) {
            player2Model.update();
            updateView(player2View, player2Model);
        }*/
        // todo: 충돌, 종료 조건, 스코어 계산 등 추가
    }

    private void updateView(ImageView view, GameModel model) {
        //캐릭터 위치 설정
        //
    }

    // 게임 초기화
    public void resetGame() {
        player1Model.reset();
        /*
        if (playerCount == 2) {
            player2Model.reset();
        }
        */
        updateView(player1View, player1Model);
        /*
        if (playerCount == 2) {
            updateView(player2View, player2Model);
        }*/
    }
    public void moveCharacter(int distance){
        player1Model.updateDistance(distance);
        /*
        if (playerCount==2){
            player2Model.updateDistance(distance);
        }*/
    }
    public GameModel getGameModel() {
        return player1Model; // GameModel 반환
    }

}
