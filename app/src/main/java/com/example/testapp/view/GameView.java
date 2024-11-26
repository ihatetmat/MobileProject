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

import java.util.ArrayList;
import java.util.List;

public class GameView extends View {
    private int playerCount; // 플레이어 수
    private Paint paintPlayer1; // 플레이어 1의 Paint 객체
    private Paint paintPlayer2; // 플레이어 2의 Paint 객체
    private GameModel model; // 게임 모델 객체
    private ImageView player;
    private PlayerState playerState;
    private int initialState = 1;
    private int hearts = 3; // 하트 개수
    private Bitmap heartBitmap;
    private List<Bitmap> scaledHearts;

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

        initializeHearts();
    }

    private void initializeHearts() {
        heartBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.heart);
        scaledHearts = new ArrayList<>();
        int heartSize = 100;

        for (int i = 0; i < 3; i++) {
            Bitmap scaledHeart = Bitmap.createScaledBitmap(heartBitmap, heartSize, heartSize, false);
            scaledHearts.add(scaledHeart);
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
            if (model.getObstacles().isEmpty()) {
                model.generateRandomObstacles(3, getWidth(), getHeight(), 100, 300, 500, player);
            }

            // 장애물 이미지 로드 (한 번만 로드)
            Bitmap obstacleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.obstacle_image);

            // 장애물 그리기
            for (Obstacle obstacle : model.getObstacles()) {
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(obstacleBitmap, obstacle.getWidth(), obstacle.getHeight(), false);
                canvas.drawBitmap(scaledBitmap, obstacle.getX(), obstacle.getY(), null);
            }

            // 하트 그리기
            int heartSize = 100;
            int startX = getWidth() - (heartSize * hearts + 20 * (hearts - 1) + 50);
            int startY = 150;

            for (int i = 0; i < hearts; i++) {
                Bitmap heart = scaledHearts.get(i);
                canvas.drawBitmap(heart, startX + (i * (heartSize + 20)), startY, null);
            }

            // 플레이어 상태에 따른 애니메이션 및 상태 변경
            if (playerState == PlayerState.DEFAULT) {
                player.setImageResource(R.drawable.character_image);
            } else if (playerState == PlayerState.JUMPING) {
                player.setImageResource(R.drawable.character_jump);
                player.animate()
                        .translationYBy(-300f)
                        .translationXBy(100f)
                        .setDuration(200)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                player.animate()
                                        .translationYBy(300f)
                                        .setDuration(200)
                                        .withEndAction(new Runnable() {
                                            @Override
                                            public void run() {
                                                model.move(0, 200);
                                                invalidateView(PlayerState.DEFAULT);
                                            }
                                        });
                            }
                        });
            } else if (playerState == PlayerState.SLIDING) {
                player.setImageResource(R.drawable.character_slide);
                player.postDelayed(() -> {
                    invalidateView(PlayerState.DEFAULT);
                }, 200);
            }

            // 점수 그리기
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

        // 하트 하나 깎는 메소드
        public void loseHeart() {
            if (hearts > 0) {
                hearts--;
                invalidate(); // 화면 갱신
        }
    }
}
