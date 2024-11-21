package com.example.testapp;

import android.os.Handler;
import android.widget.ImageView;

public class GameController {

    private int playerCount;
    private ImageView player1View, player2View;
    private GameModel player1Model, player2Model;
    private boolean isPaused = false;
    private boolean isJumping = false; // 점프 상태 관리
    private boolean isSliding = false; // 슬라이딩 상태 관리
    private Handler handler = new Handler();
    private Runnable gameLoop;

    // 생성자: View와 Model 객체를 초기화
    public GameController(ImageView player1View, GameModel player1Model, int playerCount) {
        this.player1View = player1View;
        this.player1Model = player1Model;
        this.playerCount = playerCount;

        if (playerCount == 2) {
            throw new IllegalArgumentException("아직 기능이 없음. 2인용은 추가해야 함.");
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
        if (isJumping)
            return; // 점프 중이면 추가 점프 방지
        isJumping = true;
        updateView(); // 점프 중 모션 적용했습니다.
        player1Model.move(0, -200); // y축 위로 200 이동
        // 점프 애니메이션 시작
        player1View.animate()
                .translationYBy(-200f)  // 위로 200픽셀 이동
                .setDuration(200)       // 200ms 동안 애니메이션
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        // 점프 후 내려오는 애니메이션 실행
                        player1View.animate()
                                .translationYBy(200f)  // 아래로 200픽셀 이동
                                .setDuration(200)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 모델 위치 복원 (y축 아래로 200만큼 이동)
                                        player1Model.move(0, 200); // 모델 복귀
                                        isJumping = false;  // 점프 상태 해제
                                        updateView();        // 뷰 업데이트
                                    }
                                });
                    }
                });
    }

    // 슬라이딩
    public void sliding() {
        isSliding = true; // 슬라이딩 상태 시작
        updateView(); // 슬라이드 이미지 적용
        player1Model.move(0, 500); // 모델 위치에서 y축 100만큼 이동

        // 슬라이드 상태 유지 시간 후에 원래 상태로 복원
        player1View.postDelayed(() -> {
            player1Model.move(0, -500); // 슬라이딩 위치에서 y축 100만큼 이동
            isSliding = false; // 슬라이딩 상태 해제
            updateView(); // 원래 이미지로 복원
        }, 200); // 슬라이드 유지 시간
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
        // 상태에 따른 이미지 변경
        if (isJumping) {
            // 점프 중일 때 점프 이미지 적용
            player1View.setImageResource(R.drawable.character_jump);
        } else if (isSliding) {
            // 슬라이드 중일 때 슬라이드 이미지 적용
            player1View.setImageResource(R.drawable.character_slide);
            // 슬라이드 이미지가 붕 뜨지 않도록 약간 아래로 이동
            player1View.setY(player1Model.getPlayer1Y() + 100);
        } else {
            // 기본 상태일 때 기본 이미지 적용
            player1View.setImageResource(R.drawable.character_image);
        }
        // 모델의 위치를 뷰에 적용
        //player1View.setX(player1Model.getX());
        //player1View.setY(player1Model.getY());
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
}
