package com.example.testapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.testapp.R;

public class StartActivity extends AppCompatActivity {
    private Button buttonStart;
    private Button buttonHowTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        buttonStart = findViewById(R.id.buttonStart);
        buttonHowTo = findViewById(R.id.buttonHowTo);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });

        buttonHowTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartActivity.this, HowToActivity.class);
                startActivity(intent);
            }
        });
    }
}
