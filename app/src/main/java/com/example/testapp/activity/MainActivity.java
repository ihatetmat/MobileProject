package com.example.testapp.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView character;
    private Button jumpButton, slideButton, pauseButton, resumeButton;
    private GameController gameController;
    private GameModel player1Model;
    private GameView player1View;
    private FrameLayout overlay;
    private int playerCount;
    private View characterPosition;
    private View opponentPosition;
    private View progressLine;
    private Runnable updateRunnable;
    private Handler handler = new Handler();
    private static final float CHARACTER_SPEED = 7.0f;
    private Runnable collisionCheckRunnable;
    private Runnable checkState;
    private Socket socket = null;
    private final int maxWaitingTime = 3000; // 3 초 대기
    private DataOutputStream outStream = null;
    private DataInputStream inStream = null;
    private Object socketReady = new Object();
    private Object inStreamReady = new Object();
    private final String serverHost = "서버 IP주소"; // 서버 IP 주소
    private final int serverPort = 9999; // 서버 포트 번호

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
        //todo: 2인용인 경우 1인용인 경우 기능 추가
        // 장애물 리스트 생성
        List<Obstacle> obstacles = new ArrayList<>();
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
        player1Model = new GameModel(this, playerCount); // 캐릭터 초기 위치
        player1View = new GameView(this, player1Model, character);
        gameController = new GameController(player1Model, player1View, playerCount, obstacles);

        // 레이아웃 완료 후 위치 가져오기
        character.post(() -> {
            player1Model.move((character.getX() + (character.getWidth() / 2)), (character.getY() + (character.getHeight() / 2)));
            player1Model.setWidthAndHeight((character.getWidth() / 2), (character.getHeight() / 2));
        });

        // shkim
        // GameView 생성 및 추가
        FrameLayout gameContainer = findViewById(R.id.mainFrame); // 기존 ConstraintLayout의 ID
        gameContainer.addView(player1View);
        player1View.invalidate();

        startGameLoop();
        // shkim
        // 충돌 검사 Runnable 설정
        collisionCheckRunnable = new Runnable() {
            @Override
            public void run() {
                player1Model.checkCollisions();
                handler.postDelayed(this, 16);
            }
        };
        handler.post(collisionCheckRunnable);

    }
    // 게임 시작 루프
    private void startGameLoop() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                // 캐릭터 위치 업데이트
                updateCharacterPosition();
                handler.postDelayed(this, 100); // 0.1초마다 갱신
            }
        };
        handler.post(updateRunnable);
        // 2인용인 경우 송수신 스레드 실행
        if (playerCount == 2) {
            // 서버 연결
            boolean isConnected = connectServer();
            if (!isConnected) {
                Toast.makeText(this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
                finish(); // 연결 실패 시 종료
                return;
            }

            // 수신 및 송신 스레드 시작
            startThread(runnable4RecvThread);
            startThread(runnable4SendThread);
        }
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
    //소멸자
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
        if (playerCount == 2)
            disconnectServer();
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
    // 일시정지
    @Override
    protected void onPause() {
        super.onPause();
        if (gameController != null) {
            gameController.pauseGame(); // 게임 일시정지
        }
        showOverlay(); // 오버레이 표시
    }
    ////// 서버 연결 준비
    private boolean connectServer() {
        synchronized (socketReady) {
            try {
                if (socket!=null)
                    _disconnectServer();
                socket = new Socket();
                socket.connect(new InetSocketAddress(serverHost, serverPort), maxWaitingTime);
                outStream = new DataOutputStream(socket.getOutputStream());
                inStream = new DataInputStream(socket.getInputStream());
                synchronized (inStreamReady) {
                    inStreamReady.notify(); // 데이터 수신 준비
                }
                return true;
            } catch (Exception e) {
                _disconnectServer();
                e.printStackTrace();
                return false;
            }
        }
    }
    // 서버 연결 종료
    private  void disconnectServer(){
        synchronized (socketReady){
            _disconnectServer();
        }
    }
    private void _disconnectServer() {
        synchronized (socketReady) {
            try {
                if (outStream != null) outStream.close();
                if (inStream != null) inStream.close();
                if (socket != null) socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            outStream = null;
            inStream = null;
            socket = null;
        }
    }
    // 수신 스레드
    private Runnable runnable4RecvThread = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    // 상대 데이터 수신
                    int opponentDistance = inStream.readInt(); // 거리
                    //int opponentGameState = inStream.readInt(); // 게임 상태
                    // UI 업데이트
                    runOnUiThread(() -> {
                        updateOpponentPosition(opponentDistance); // 거리 업데이트
                        //updateOpponentState(opponentGameState);   // 상태 반영
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    disconnectServer();
                    break;
                }
            }
        }
    };
    // 송신 스레드
    private Runnable runnable4SendThread = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    // 내 데이터 송신
                    synchronized (socketReady) {
                        if (outStream != null) {
                            outStream.writeInt(player1Model.getCurrentDistance()); // 현재 거리
                            //outStream.writeInt(getGameState());                    // 게임 상태
                            //outStream.writeInt(player1Model.getHealth());          // 체력 상태
                            outStream.flush();
                        }
                    }
                    Thread.sleep(100); // 0.1초 간격
                } catch (Exception e) {
                    e.printStackTrace();
                    disconnectServer();
                    break;
                }
            }
        }
    };
    private void startThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();
    }
}

