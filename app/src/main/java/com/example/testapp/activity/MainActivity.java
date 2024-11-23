package com.example.testapp.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.testapp.R;
import com.example.testapp.controller.GameController;
import com.example.testapp.model.GameModel;
import com.example.testapp.view.GameView;

public class MainActivity extends AppCompatActivity {

    private ImageView character;
    private Button jumpButton, slideButton, pauseButton, resumeButton;
    private GameController gameController;
    private FrameLayout overlay;
    private int playerCount;
    //private FrameLayout otherScreen;
    //private TextView otherLabel;
    private View characterPosition;
    private View opponentPosition;
    private View progressLine;
    private Runnable updateRunnable;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 2인용 여부 확인
        // Intent에서 플레이어 수 전달받기
        playerCount = getIntent().getIntExtra("playerCount", 1);

        // 캐릭터 및 버튼 초기화
        character = findViewById(R.id.character);
        jumpButton = findViewById(R.id.jumpButton);
        slideButton = findViewById(R.id.slideButton);
        pauseButton = findViewById(R.id.pauseButton);
        resumeButton = findViewById(R.id.resumeButton);
        overlay = findViewById(R.id.overlay);
        characterPosition = findViewById(R.id.characterPosition);
        opponentPosition = findViewById(R.id.opponentPosition);
        progressLine = findViewById(R.id.progressLine);

        if (playerCount == 2) {
            opponentPosition.setVisibility(View.VISIBLE);
        } else {
            opponentPosition.setVisibility(View.GONE);
        }


        // OnClickListener 설정
        jumpButton.setOnClickListener(onClickListener);
        slideButton.setOnClickListener(onClickListener);
        pauseButton.setOnClickListener(onClickListener);
        resumeButton.setOnClickListener(onClickListener);

        // GameController 생성 todo : 초기 위치 설정해야 할듯?
        GameModel player1Model = new GameModel(this, playerCount); // 캐릭터 초기 위치
        GameView gameView = new GameView(this, playerCount, player1Model, character);
        gameController = new GameController(character, player1Model, gameView, playerCount);

        // shkim
        // GameView 생성 및 추가
        FrameLayout gameContainer = findViewById(R.id.mainFrame); // 기존 ConstraintLayout의 ID
        gameContainer.addView(gameView);
        gameView.invalidate();

        player1Model.move((character.getX() + (character.getWidth() / 2)), (character.getY() + character.getHeight()));

        // 주기적으로 캐릭터 위치 업데이트
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateCharacterPosition();
                handler.postDelayed(this, 100); // 0.1초마다 갱신
            }
        };
        handler.post(updateRunnable);
        // shkim
    }

    // OnClickListener 정의
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.jumpButton) {
                gameController.jumping();  // 점프 동작
            } else if (id == R.id.slideButton) {
                gameController.sliding();  // 슬라이드 동작
            } else if (id == R.id.pauseButton) {
                gameController.pauseGame(); // 게임 일시정지
                overlay.setVisibility(View.VISIBLE); // 오버레이 표시
                resumeButton.setVisibility(View.VISIBLE); // Resume 버튼 표시
                // 다른 버튼 비활성화
                jumpButton.setEnabled(false);
                slideButton.setEnabled(false);
                pauseButton.setEnabled(false);
            } else if (id == R.id.resumeButton) {
                gameController.pauseGame(); // 게임 재진행
                overlay.setVisibility(View.GONE); // 오버레이 숨김
                resumeButton.setVisibility(View.GONE); // Resume 버튼 숨김
                // 다른 버튼 활성화
                jumpButton.setEnabled(true);
                slideButton.setEnabled(true);
                pauseButton.setEnabled(true);
            }
        }
    };
    private void updateCharacterPosition() {
        // 게임 모델의 현재 거리와 총 거리를 가져와 계산
        int totalDistance = gameController.getGameModel().getTotalDistance();
        int currentDistance = gameController.getGameModel().getCurrentDistance();

        // 현재 거리의 비율 계산
        float progress = (float) currentDistance / totalDistance;

        // Progress Bar의 길이에 따라 점 위치 조정
        int progressLineWidth = progressLine.getWidth();
        int newPosition = (int) (progress * progressLineWidth);

        // 점의 위치 업데이트
        characterPosition.setTranslationX(newPosition);
    }
    // 상대방 캐릭터 위치 업데이트
    private void updateOpponentPosition(int distance) {
        // 총 거리와 상대방 거리 비율 계산
        float progress = (float) distance / gameController.getGameModel().getTotalDistance();

        // 상대방 Progress Line 위 점 위치 업데이트
        int progressLineWidth = progressLine.getWidth();
        int newPosition = (int) (progress * progressLineWidth);

        // 상대방 점의 View를 이동
        opponentPosition.setTranslationX(newPosition);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
    }
}

