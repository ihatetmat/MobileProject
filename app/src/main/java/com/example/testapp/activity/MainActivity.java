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
import com.example.testapp.object.Obstacle;
import com.example.testapp.view.GameView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView character;
    private Button jumpButton, slideButton, pauseButton, resumeButton;
    private GameModel playerModel;
    private GameController gameController;
    private GameView gameView;
    private FrameLayout overlay;
    private int playerCount;
    private View characterPosition;
    private View opponentPosition;
    private View progressLine;
    private Runnable updateRunnable;
    private Handler handler = new Handler();
    private static final float CHARACTER_SPEED = 7.0f;
    private Runnable collisionCheckRunnable;
    public String sessionId; // 세션 ID 예시
    public String playerId;    // 또는 "player2"
    // 장애물 리스트 생성
    List<Obstacle> obstacles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this); // Firebase 초기화
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference gameRef = database.getReference("gameSession");
        // 2인용 여부 확인
        // Intent에서 플레이어 수 전달받기
        playerCount = getIntent().getIntExtra("playerCount", 1);
        // 세션 ID 자동 생성
        if (playerCount == 2) {
            sessionId = getIntent().getStringExtra("sessionId");
            if (sessionId == null) {
                sessionId = generateSessionId();
                playerId = "player1"; // 처음 생성한 플레이어는 player1
            } else {
                playerId = "player2"; // 이미 세션이 존재하면 player2로 설정
            }

            // Firebase에서 세션 데이터 추가
            gameRef.child(sessionId).child(playerId).child("isPlaying").setValue(true);
            gameRef.child(sessionId).child(playerId).child("distance").setValue(0);
        }
        // 1인용 게임일 경우
        else {
            sessionId = null;
            playerId = null;
        }
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
        playerModel = new GameModel(this, playerCount); // 캐릭터 초기 위치
        gameView = new GameView(this, playerCount, playerModel, character);
        gameController = new GameController(character, playerModel, gameView, playerCount, obstacles, null, null);

        // 레이아웃 완료 후 위치 가져오기
        character.post(() -> {
            playerModel.move((character.getX() + (character.getWidth() / 2)), (character.getY() + (character.getHeight() / 2)));
            playerModel.setWidthAndHeight((character.getWidth() / 2), (character.getHeight() / 2));
        });

        // shkim
        // GameView 생성 및 추가
        FrameLayout gameContainer = findViewById(R.id.mainFrame); // 기존 ConstraintLayout의 ID
        gameContainer.addView(gameView);
        gameView.invalidate();

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

        // 캐릭터 자동 이동 추가
        handler.post(new Runnable() {
            @Override
            public void run() {
                moveCharacterRight();
                handler.postDelayed(this, 16); // 약 60FPS (16ms 간격)
            }
        });

        // 충돌 검사 Runnable 설정
        collisionCheckRunnable = new Runnable() {
            @Override
            public void run() {
                playerModel.checkCollisions();
                handler.postDelayed(this, 16);
            }
        };

        handler.post(collisionCheckRunnable);
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
                showOverlay();
            } else if (id == R.id.resumeButton) {
                gameController.resumeGame(); // 게임 재진행
                hideOverlay();
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

    // 일시정지 오버레이 표시
    private void showOverlay() {
        //overlay 및 resume 버튼 활성화
        overlay.setVisibility(View.VISIBLE);
        resumeButton.setVisibility(View.VISIBLE);
        // 다른 버튼 비활성화
        jumpButton.setEnabled(false);
        slideButton.setEnabled(false);
        pauseButton.setEnabled(false);
    }
    // 일시정지 오버레이 숨김
    private void hideOverlay() {
        //overlay 및 resume 버튼 비활성화
        overlay.setVisibility(View.GONE);
        resumeButton.setVisibility(View.GONE);
        // 다른 버튼 활성화
        jumpButton.setEnabled(true);
        slideButton.setEnabled(true);
        pauseButton.setEnabled(true);
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (gameController != null) {
            gameController.pauseGame(); // 게임 일시정지
        }
        showOverlay(); // 오버레이 표시
    }
    private void startGame(boolean isMultiplayer, String sessionId, String playerId) {
        if (isMultiplayer) {
            gameController = new GameController(character, playerModel, gameView, playerCount, obstacles, sessionId, playerId);
            gameController.listenToOpponent(); // 상대방 데이터 수신 시작
        } else {
            gameController = new GameController(character, playerModel, gameView, playerCount, obstacles, null, null); // 1인용
        }
    }
    private String generateSessionId() {
        return "session_" + System.currentTimeMillis(); // 타임스탬프 기반 고유 ID
    }
    private void exitGame() {
        if (gameController != null) {
            gameController.endGame(); // 게임 종료 처리
        }
        // 메뉴 화면으로 돌아가기
        finish(); // 현재 액티비티 종료
    }

    // shkim
    private void moveCharacterRight() {
        // 캐릭터의 현재 X 위치 가져오기
        float currentX = character.getX();
        // 새로운 X 위치 계산
        float newX = currentX + CHARACTER_SPEED;
        // 캐릭터 이동
        character.setX(newX);
        // 게임 모델에 반영
        gameController.getGameModel().move(CHARACTER_SPEED, 0);
    }
}

