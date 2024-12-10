package com.example.testapp.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import com.example.testapp.view.GameView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private ImageView character;
    private Button jumpButton, slideButton, pauseButton, resumeButton;
    private GameController gameController;
    private GameModel playerModel;
    private GameView playerView;
    private FrameLayout overlay;
    private int playerCount;
    private View characterPosition;
    private View opponentPosition;
    private View progressLine;
    private Runnable updateRunnable;
    private Handler handler = new Handler();
    private Runnable collisionCheckRunnable;
    private Socket socket = null;
    private final int maxWaitingTime = 3000; // 3 초 대기
    private DataOutputStream outStream = null;
    private DataInputStream inStream = null;
    private Object socketReady = new Object();
    private Object inStreamReady = new Object();
    private final String serverHost = "10.0.2.2"; // 서버 IP 주소
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
        playerModel = new GameModel(this); // 캐릭터 초기 위치
        playerView = new GameView(this, playerModel, character);
        gameController = new GameController(playerModel, playerView, playerCount);

        // 레이아웃 완료 후 위치 가져오기
        character.post(() -> {
            playerModel.move((character.getX() + (character.getWidth() / 2)), (character.getY() + (character.getHeight() / 2)));
            playerModel.setWidthAndHeight((character.getWidth() / 2), (character.getHeight() / 2));
            playerModel.generateRandomObstacles(5, 400, character);
        });

        // shkim
        // GameView 생성 및 추가
        FrameLayout gameContainer = findViewById(R.id.mainFrame); // 기존 ConstraintLayout의 ID
        gameContainer.addView(playerView);
        playerView.invalidate();
        // 종민
        gameController.startGame();
        startGameLoop();
        // shkim
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
    // 종민
    // 게임 시작 루프
    private void startGameLoop() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                // 캐릭터 업데이트
                gameController.updateGameState();

                // 종료조건 확인
                GameController.GameState gameState = gameController.getGameState();
                if (gameState == GameController.GameState.End) {
                    // GameState가 'End'일 경우 게임 종료
                    gameController.endGame();
                    finish(); // 액티비티 종료
                    return;
                }

                // 캐릭터 위치 업데이트 (Running 상태일 때만 실행)
                if (gameState == GameController.GameState.Running) {
                    gameController.getGameModel().update();
                    updateCharacterPosition();
                }

                handler.postDelayed(this, 100); // 0.1초마다 갱신
            }
        };
        handler.post(updateRunnable);
        // 2인용인 경우 송수신 스레드 실행
        // startGameLoop() 메서드 내
        if (playerCount == 2) {
            // 서버 연결을 별도의 스레드에서 실행
            new Thread(() -> {
                boolean isConnected = connectServer();
                runOnUiThread(() -> { // UI 작업은 메인 스레드에서 실행
                    if (!isConnected) {
                        Toast.makeText(MainActivity.this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
                        finish(); // 연결 실패 시 종료
                    } else {
                        // 수신 및 송신 스레드 시작
                        startThread(runnable4RecvThread);
                        startThread(runnable4SendThread);
                    }
                });
            }).start();
        }
    }
    // 종민
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
    // 종민
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
    // 종민
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
    // 종민
    //소멸자
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
        if (playerCount == 2)
            disconnectServer();
    }
    // 종민
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
    // 종민
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
    //종민
    // 일시정지
    @Override
    protected void onPause() {
        super.onPause();
        if (gameController != null) {
            gameController.pauseGame(); // 게임 일시정지
        }
        showOverlay(); // 오버레이 표시
    }
    // 종민
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
                while(true) {
                    gameController.pauseGame();
                    int startSignal = inStream.readInt(); // 서버에서 신호 수신
                    if (startSignal == 1) { // 1: 게임 시작 신호
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "게임이 시작되었습니다!", Toast.LENGTH_SHORT).show());
                        gameController.resumeGame();
                        break;
                    } else {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "상대를 기다리고 있습니다...", Toast.LENGTH_SHORT).show());
                    }
                }
                return true;
            } catch (Exception e) {
                _disconnectServer();
                e.printStackTrace();
                return false;
            }
        }
    }
    // 종민
    // 서버 연결 종료
    private  void disconnectServer(){
        synchronized (socketReady){
            _disconnectServer();
        }
    }
    // 종민
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
    // 종민
    // 수신 스레드
    private Runnable runnable4RecvThread = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    // 상대 데이터 수신
                    int opponentDistance = inStream.readInt(); // 거리
                    Log.d("opp dis", "opp dis" + opponentDistance);
                    int oppResult = inStream.readInt(); // 게임 상태 변수| 0: 진행중, 1: 승리, 2: 패배, 3: 무승부
                    Log.d("opp res", "opp res" + oppResult);
                    int myResult = inStream.readInt(); // 게임 상태 변수| 0: 진행중, 1: 승리, 2: 패배, 3: 무승부
                    Log.d("my res", "my res" + myResult);
                    // UI 업데이트
                    runOnUiThread(() -> {
                        updateOpponentPosition(opponentDistance); // 거리 업데이트
                        if (myResult == 1 || oppResult == 2) {
                            showGameResult("승리하였습니다!");
                            endGame();
                        } else if (myResult == 2 || oppResult == 1) {
                            showGameResult("패배하였습니다.");
                            endGame();
                        } else if (myResult == 3 || oppResult == 3) {
                            showGameResult("무승부입니다.");
                            endGame();
                        }

                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    disconnectServer();
                    break;
                }
            }
        }
    };
    //종민
    // 전송 스레드
    private Runnable runnable4SendThread = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    // 내 데이터 전송
                    synchronized (socketReady) {
                        if (outStream != null) {
                            outStream.writeInt(playerModel.getCurrentDistance());        // 현재 거리 송신
                            outStream.writeInt(playerModel.checkGameOver() ? 1 : 0);      // 현재 게임 상태 송신
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
    // UI에 결과 메시지 출력
    private void showGameResult(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    // 종민
    // 2인용 게임 종료 로직
    private void endGame() {
        handler.removeCallbacks(updateRunnable);
        disconnectServer();
        finish(); // 현재 Activity 종료
    }

}