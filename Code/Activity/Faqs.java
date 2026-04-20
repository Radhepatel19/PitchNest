package com.example.businessidea.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.businessidea.R;

public class Faqs extends AppCompatActivity {
    private boolean[] isExpanded = {false, false, false, false, false};

    @Override
    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faqs);
         ImageView ArrowBack = findViewById(R.id.ArrowBack);
        ArrowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Faqs.this, HomeActivity.class);
                startActivity(intent);
            }
        });
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            getWindow().getDecorView().setSystemUiVisibility(0); // Default light icons
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.back)); // Black status bar
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.back)); // Black navigation bar
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // Dark icons
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white)); // White status bar
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.white)); // Black navigation bar
        }

        setupQuestion(findViewById(R.id.toggleArrow1), findViewById(R.id.Q1), 0);
        setupQuestion(findViewById(R.id.toggleArrow2), findViewById(R.id.Q2), 1);
        setupQuestion(findViewById(R.id.toggleArrow3), findViewById(R.id.Q3), 2);
        setupQuestion(findViewById(R.id.toggleArrow4), findViewById(R.id.Q4), 3);
        setupQuestion(findViewById(R.id.toggleArrow5), findViewById(R.id.Q5), 4);
    }
    private void setupQuestion(ImageView toggleArrow, RadioGroup answersGroup, int index) {
        toggleArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isExpanded[index]) {
                    collapse(answersGroup);
                    toggleArrow.setImageResource(R.drawable.baseline_arrow_downward_24);
                } else {
                    expand(answersGroup);
                    toggleArrow.setImageResource(R.drawable.baseline_arrow_upward_24);
                }
                isExpanded[index] = !isExpanded[index];
            }
        });
    }
    private void expand(View view) {
        view.setVisibility(View.VISIBLE);
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(300);
        view.startAnimation(fadeIn);
    }

    private void collapse(View view) {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(300);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        view.startAnimation(fadeOut);
    }

}