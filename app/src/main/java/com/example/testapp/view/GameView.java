package com.example.testapp.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.testapp.R;
import com.example.testapp.model.GameModel;
import com.example.testapp.object.Obstacle;

public class GameView extends View {
    private int playerCount; // 플레이어 수
    private Paint paintPlayer1; // 플레이어 1의 Paint 객체
    private Paint paintPlayer2; // 플레이어 2의 Paint 객체
    private GameModel model; // 게임 모델 객체
    private ImageView player;
    private PlayerState playerState;

    public enum PlayerState {
        DEFAULT(0), JUMPING(1), SLIDING(2);
        private final int value;
        private PlayerState(int value) { this.value = value; }
        public int value() { return value; }
    }

    // 생성자: 단일/다중 플레이어 설정
    public GameView(Context context, int playerCount, GameModel gameModel, ImageView player) {
        super(context);
        this.playerCount = playerCount;
        this.model = gameModel;
        this.player = player;
        this.playerState = PlayerState.DEFAULT;

        // 각 플레이어의 색상 설정
        paintPlayer1 = new Paint();
        paintPlayer1.setColor(Color.BLUE);

        if (playerCount > 1) {
            paintPlayer2 = new Paint();
            paintPlayer2.setColor(Color.RED);
        }
    }

    // XML 레이아웃 파일에서 사용하는 기본 생성자
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 게임 모델로부터 플레이어와 장애물 정보를 가져와 그리기
        if (model != null) {
            // shkim ( 장애물 생성 )
            // 캐릭터의 위치를 기반으로 장애물 생성
            int characterX = (int) model.getPlayer1X(); // 캐릭터의 현재 X 좌표
            int characterWidth = 50; // 캐릭터의 너비 (상수로 설정하거나 필요 시 동적으로 계산)

            if (model.getObstacles().isEmpty()) {
                Log.d("GameView", "No obstacles generated. Generating obstacles.");
                model.generateRandomObstacles(7, getWidth(), getHeight(), 100, 150, 300, player);
            }
            // shkim
            // 장애물 이미지 로드 (한 번만 로드)
            Bitmap obstacleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.obstacle_image);

            // 장애물 그리기
            for (Obstacle obstacle : model.getObstacles()) {
                Log.d("GameView", "Obstacle - X: " + obstacle.getX() + ", Y: " + (obstacle.getY() + obstacle.getHeight()) +
                        ", Width: " + obstacle.getWidth() + ", Height: " + obstacle.getHeight());

                // 장애물 크기에 맞게 이미지 스케일링
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(obstacleBitmap, obstacle.getWidth(), obstacle.getHeight(), false);
                canvas.drawBitmap(scaledBitmap, obstacle.getX(), obstacle.getY(), null);
            }
            // shkim


            if (playerState == PlayerState.DEFAULT) {
                player.setImageResource(R.drawable.character_image);
            } else if (playerState == PlayerState.JUMPING) {
                player.setImageResource(R.drawable.character_jump);
                // 점프 애니메이션 시작
                player.animate()
                        .translationYBy(-200f)  // 위로 200픽셀 이동
                        .setDuration(200)       // 200ms 동안 애니메이션
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                // 점프 후 내려오는 애니메이션 실행
                                player.animate()
                                        .translationYBy(200f)  // 아래로 200픽셀 이동
                                        .setDuration(200)
                                        .withEndAction(new Runnable() {
                                            @Override
                                            public void run() {
                                                // 모델 위치 복원 (y축 아래로 200만큼 이동)
                                                model.move(0, 200); // 모델 복귀
                                                invalidateView(PlayerState.DEFAULT);
                                            }
                                        });
                            }
                        });
            } else if (playerState == PlayerState.SLIDING) {
                // 슬라이드 중일 때 슬라이드 이미지 적용
                player.setImageResource(R.drawable.character_slide);
                Log.d("player", String.valueOf(player.getY()));
                Log.d("player", String.valueOf(player.getHeight()));
                // 슬라이드 상태 유지 시간 후에 원래 상태로 복원
                player.postDelayed(() -> {
                    invalidateView(PlayerState.DEFAULT);
                }, 200); // 슬라이드 유지 시간
                Log.d("player", String.valueOf(player.getY()));
                Log.d("player", String.valueOf(player.getHeight()));
            }
            // shkim

            // 플레이어 1 그리기
//            canvas.drawCircle(model.getPlayer1X(), model.getPlayer1Y(), 50, paintPlayer1);

            // 플레이어 2 그리기 (2인용일 때만)
//            if (playerCount > 1) {
//                canvas.drawCircle(model.getPlayer2X(), model.getPlayer2Y(), 50, paintPlayer2);
//            }

            // 장애물 그리기
//            Paint obstaclePaint = new Paint();
//            obstaclePaint.setColor(Color.GRAY);
//            for (GameModel.Obstacle obstacle : model.getObstacles()) {
//                canvas.drawRect(obstacle.getX(), obstacle.getY(),
//                        obstacle.getX() + obstacle.getWidth(),
//                        obstacle.getY() + obstacle.getHeight(),
//                        obstaclePaint);
//            }

            // 점수 표시
            Paint scorePaint = new Paint();
            scorePaint.setColor(Color.BLACK);
            scorePaint.setTextSize(60);
            canvas.drawText("Score: " + model.getScore1(), 50, 100, scorePaint);
        }
    }

    // 화면 갱신
    public void invalidateView(PlayerState playerState) {
        this.playerState = playerState;
        invalidate(); // onDraw 호출
    }
}
