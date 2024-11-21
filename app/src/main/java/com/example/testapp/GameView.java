package com.example.testapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class GameView extends View {
    private int playerCount; // 플레이어 수
    private Paint paintPlayer1; // 플레이어 1의 Paint 객체
    private Paint paintPlayer2; // 플레이어 2의 Paint 객체
    private GameModel model; // 게임 모델 객체

    // 생성자: 단일/다중 플레이어 설정
    public GameView(Context context, int playerCount, GameModel gameModel) {
        super(context);
        this.playerCount = playerCount;
        this.model = new GameModel(playerCount);

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
                model.generateRandomObstacles(7, getWidth(), getHeight(), 100, 150, 300, characterX, characterWidth);
            }
            // shkim

            // 플레이어 1 그리기
//            canvas.drawCircle(model.getPlayer1X(), model.getPlayer1Y(), 50, paintPlayer1);

            // 플레이어 2 그리기 (2인용일 때만)
//            if (playerCount > 1) {
//                canvas.drawCircle(model.getPlayer2X(), model.getPlayer2Y(), 50, paintPlayer2);
//            }

            // shkim
            // 장애물 이미지 로드 (한 번만 로드)
            Bitmap obstacleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.obstacle_image);

            // 장애물 그리기
            for (Obstacle obstacle : model.getObstacles()) {
                // 장애물 크기에 맞게 이미지 스케일링
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(obstacleBitmap, obstacle.getWidth(), obstacle.getHeight(), false);
                canvas.drawBitmap(scaledBitmap, obstacle.getX(), obstacle.getY(), null);
            }
            // shkim

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
            canvas.drawText("Score: " + model.getScore(), 50, 100, scorePaint);
        }
    }

    // 화면 갱신
    public void invalidateView(int player) {
        invalidate(); // onDraw 호출
    }
}
