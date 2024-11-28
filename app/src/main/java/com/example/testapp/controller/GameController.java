package com.example.testapp.controller;

import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.testapp.model.GameModel;
import com.example.testapp.object.Obstacle;
import com.example.testapp.view.GameView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private DatabaseReference gameRef;

    // 생성자: View와 Model 객체를 초기화
    public GameController(ImageView playerView, GameModel playerModel,GameView gameView, int playerCount, List<Obstacle> obstacles, String sessionId, String playerId) {
        this.playerView = playerView;
        this.playerModel = playerModel;
        this.playerCount = playerCount;
        this.gameView = gameView;
        this.obstacles = obstacles;
        this.sessionId = sessionId;
        this.playerId = playerId;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        this.sessionId = sessionId;
        this.playerId = playerId;
        this.gameRef = database.getReference("gameSession").child(sessionId);

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
            endGame(); // 종료 처리
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
    //플레이어의 거리를 Firebase에 실시간으로 업데이트
    public void updateDistance(int distance) {
        if (gameRef != null) {
            gameRef.child(playerId).child("distance").setValue(distance);
        }
    }
    // 상대방의 데이터를 실시간으로 수신하는 리스너
    public void listenToOpponent() {
        String opponentId = playerId.equals("player1") ? "player2" : "player1";
        gameRef.child(opponentId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int opponentDistance = snapshot.child("distance").getValue(Integer.class);
                    boolean isOpponentPlaying = snapshot.child("isPlaying").getValue(Boolean.class);
                    if (!isOpponentPlaying) {
                        pauseGame(); // 상대방이 게임을 중단했으면 나도 일시정지
                    } else {
                        resumeGame(); // 상대방이 재개하면 나도 재개
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("GameController", "Failed to read opponent data", error.toException());
            }
        });
    }
    public void endGame() {
        if (gameRef != null) {
            gameRef.removeValue(); // Firebase 데이터 삭제
        }
        // 추가로 UI 리셋이나 종료 동작 처리
        resetGame(); // 게임 모델 및 뷰 초기화
    }


}
