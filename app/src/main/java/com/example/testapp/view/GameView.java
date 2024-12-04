package com.example.testapp.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.example.testapp.R;
import com.example.testapp.model.GameModel;
import com.example.testapp.object.Obstacle;

import java.util.List;

public class GameView extends View {
    private GameModel model; // 게임 모델 객체
    private ImageView playerView;
    private PlayerState playerState;
    private int hearts; // 하트 개수
    private Bitmap obstacleBitmap;
    private Bitmap heartBitmap;
    Paint scorePaint;

    public enum PlayerState {
        DEFAULT(0), JUMPING(1), SLIDING(2);
        private final int value;
        private PlayerState(int value) { this.value = value; }
        public int value() { return value; }
    }

    // 생성자: 단일/다중 플레이어 설정
    public GameView(Context context, GameModel gameModel, ImageView player) {
        super(context);
        this.model = gameModel;
        this.playerView = player;
        this.playerState = PlayerState.DEFAULT;
        scorePaint = new Paint();
        scorePaint.setColor(Color.BLACK);
        scorePaint.setTextSize(60);
        heartBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.heart);
        obstacleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.obstacle_image);
        model.generateRandomObstacles(5, getWidth(), getHeight(), 100, 300, 500, player);
    }

    // XML 레이아웃 파일에서 사용하는 기본 생성자
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private long lastFrameTime = System.currentTimeMillis();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastFrameTime) / 1000f; // 초 단위
        lastFrameTime = currentTime;

        if (model != null) {
            hearts = model.getPlayerHealth();
            List<Obstacle> obstacles = model.getObstacles();
            for (Obstacle obstacle : obstacles) {
                // 장애물 X 좌표 이동 (속도 300px/초로 고정)
                obstacle.setX((int) (obstacle.getX() - 500 * deltaTime));

                // 장애물이 화면 밖으로 나가면 재배치
                if (obstacle.getX() + obstacle.getWidth() < 0) {
                    obstacle.setX(getWidth());
                    int playerTop = playerView.getTop();
                    int playerHeight = playerView.getHeight();
                    int obstacleY = (int) (Math.random() * 2) == 0
                            ? playerTop + (playerHeight / 2)
                            : playerTop - obstacle.getHeight() + 50;

                    obstacle.setY(obstacleY);
                }

                // Bitmap 최적화
                Bitmap scaledObstacleBitmap = Bitmap.createScaledBitmap(obstacleBitmap, obstacle.getWidth(), obstacle.getHeight(), false);
                canvas.drawBitmap(scaledObstacleBitmap, obstacle.getX(), obstacle.getY(), null);
            }

            // 하트 그리기
            int heartSize = 100;
            int startX = getWidth() - (heartSize * hearts + 20 * (hearts - 1) + 50);
            int startY = 150;

            for (int i = 0; i < hearts; i++) {
                Bitmap scaledHeartBitmap = Bitmap.createScaledBitmap(heartBitmap, heartSize, heartSize, false);
                canvas.drawBitmap(scaledHeartBitmap, startX + (i * (heartSize + 20)), startY, null);
            }

            // 캐릭터 상태 처리
            if (playerState == PlayerState.DEFAULT) {
                playerView.setImageResource(R.drawable.character_image);
            } else if (playerState == PlayerState.JUMPING) {
                playerView.setImageResource(R.drawable.character_jump);
            } else if (playerState == PlayerState.SLIDING) {
                playerView.setImageResource(R.drawable.character_slide);
            }

            // 점수 표시
            canvas.drawText("Score: " + model.getScore1(), 50, 100, scorePaint);

            // 일정 시간 간격으로 화면 다시 그리기
            invalidate();
        }
    }

    // 점프 기능
    public void jump() {
        if (playerState == PlayerState.DEFAULT) {
            playerState = PlayerState.JUMPING;

            playerView.animate()
                    .translationYBy(-300f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        playerView.animate()
                                .translationYBy(300f)
                                .setDuration(200)
                                .withEndAction(() -> {
                                    model.move(0, 200);
                                    invalidateView(PlayerState.DEFAULT);
                                });
                    });
        }
    }

    // 슬라이드 기능
    public void slide() {
        if (playerState == PlayerState.DEFAULT) {
            playerState = PlayerState.SLIDING;

            model.move(0, 50);

            playerView.postDelayed(() -> {
                invalidateView(PlayerState.DEFAULT);
                model.move(0, -50);
            }, 300);
        }
    }

    // 화면 갱신
    public void invalidateView(PlayerState playerState) {
        this.playerState = playerState;
        invalidate(); // onDraw 호출
    }}


