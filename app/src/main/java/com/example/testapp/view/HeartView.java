package com.example.testapp.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import com.example.testapp.R;

public class HeartView extends View {
    private Paint paint;
    private int hearts = 3; // 하트 개수 (초기값 3)
    private Bitmap heartBitmap; // 하트 이미지

    public HeartView(Context context) {
        super(context);
        paint = new Paint();
        paint.setColor(Color.RED); // 하트 색깔
        heartBitmap = getHeartBitmap(); // 하트 이미지 로드
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 하트를 그릴 위치 계산
        int heartSize = 50; // 하트 크기 (작게 설정)
        int startX = 50; // 첫 번째 하트의 X 좌표
        int startY = 50; // Y 좌표

        // 하트 3개 그리기
        for (int i = 0; i < hearts; i++) {
            // 하트 이미지를 크기만큼 조정하여 그리기
            Bitmap scaledHeart = Bitmap.createScaledBitmap(heartBitmap, heartSize, heartSize, false);
            canvas.drawBitmap(scaledHeart, startX + (i * (heartSize + 20)), startY, paint);
        }
    }

    // 하트 이미지 가져오기
    private Bitmap getHeartBitmap() {
        // 하트 이미지를 리소스에서 불러옴
        return BitmapFactory.decodeResource(getResources(), R.drawable.heart);
    }

    // 하트 하나 깎는 메소드
    public void loseHeart() {
        if (hearts > 0) {
            hearts--;
            invalidate(); // 화면 갱신
        }
    }
}
