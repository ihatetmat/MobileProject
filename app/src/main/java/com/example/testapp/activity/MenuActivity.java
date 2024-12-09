package com.example.testapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.testapp.R;

public class MenuActivity extends AppCompatActivity { // 종민

    private Button onePlayerButton, twoPlayerButton;

    @Override
    // 종민
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        onePlayerButton = findViewById(R.id.onePlayerButton);
        twoPlayerButton = findViewById(R.id.twoPlayerButton);

        onePlayerButton.setOnClickListener(onClickListener);
        twoPlayerButton.setOnClickListener(onClickListener);
    }
    // 종민
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.onePlayerButton) {
                putPlayerCount(1);
            } else if (id == R.id.twoPlayerButton) {
                putPlayerCount(2);  // 슬라이드 동작
            }
        }
    };
    // 종민
    // 1~2인용 intent 메시지 전달
    private void putPlayerCount(int playerCount){
        Intent intent = new Intent(MenuActivity.this, MainActivity.class);
        intent.putExtra("playerCount", playerCount); // playerCount용
        startActivity(intent);
    }
}
