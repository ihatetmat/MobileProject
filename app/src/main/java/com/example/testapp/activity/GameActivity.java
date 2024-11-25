package com.example.testapp.activity;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.example.testapp.view.HeartView;

public class GameActivity extends AppCompatActivity {
    private HeartView heartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        heartView = new HeartView(this);

        // HeartView의 크기 설정 (작게 표시)
        heartView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        setContentView(heartView);

        // 예시로 충돌을 감지해서 하트를 깎는 코드
        // 충돌 감지 로직에 따라 호출
        heartView.loseHeart(); // 하트 하나 깎기
    }
}
