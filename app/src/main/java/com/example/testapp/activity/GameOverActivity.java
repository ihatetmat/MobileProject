package com.example.testapp.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.testapp.R;
import com.example.testapp.view.GameView;

public class GameOverActivity extends AppCompatActivity {
    private GameView gameView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameover);

        gameView = findViewById(R.id.gameView);


        // 게임 종료 콜백 설정
//        gameView.getModel().(() -> runOnUiThread(this::showRestartDialog));
    }

    private void showRestartDialog() {
        findViewById(R.id.restartButton).setVisibility(View.VISIBLE);
        findViewById(R.id.restartButton).setOnClickListener(v -> restartGame());
    }

    private void restartGame() {
        gameView.resetGame(); // 게임 초기화 로직 호출
        findViewById(R.id.restartButton).setVisibility(View.GONE);
    }
}
