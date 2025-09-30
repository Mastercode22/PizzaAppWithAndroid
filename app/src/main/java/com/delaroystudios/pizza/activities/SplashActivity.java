package com.delaroystudios.pizza.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 5000; // 5 seconds
    private ImageView ivLogo;
    private TextView tvAppName;
    private SessionManager sessionManager;

    // CHOOSE YOUR ANIMATION STYLE HERE:
    // 1 = Bouncing/Pulsing (recommended)
    // 2 = Fade In with Scale
    // 3 = Bounce from top
    // 4 = Rotate and Scale
    private static final int ANIMATION_STYLE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(this);
        ivLogo = findViewById(R.id.iv_logo);
        tvAppName = findViewById(R.id.tv_app_name);

        // Start animation based on selected style
        startAnimation();

        // Navigate after delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateToNextScreen();
            }
        }, SPLASH_DURATION);
    }

    private void startAnimation() {
        switch (ANIMATION_STYLE) {
            case 1:
                startBouncingAnimation();
                break;
            case 2:
                startFadeInScaleAnimation();
                break;
            case 3:
                startBounceFromTopAnimation();
                break;
            case 4:
                startRotateScaleAnimation();
                break;
            default:
                startBouncingAnimation();
        }
    }

    // ANIMATION STYLE 1: Continuous bouncing/pulsing effect
    private void startBouncingAnimation() {
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1.0f, 1.2f,  // X axis: from 100% to 120%
                1.0f, 1.2f,  // Y axis: from 100% to 120%
                Animation.RELATIVE_TO_SELF, 0.5f,  // Pivot X at center
                Animation.RELATIVE_TO_SELF, 0.5f   // Pivot Y at center
        );

        scaleAnimation.setDuration(800);
        scaleAnimation.setRepeatCount(Animation.INFINITE);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        scaleAnimation.setInterpolator(new BounceInterpolator());

        ivLogo.startAnimation(scaleAnimation);

        // Subtle fade animation for text
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.7f, 1.0f);
        alphaAnimation.setDuration(1000);
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        alphaAnimation.setRepeatMode(Animation.REVERSE);
        if (tvAppName != null) {
            tvAppName.startAnimation(alphaAnimation);
        }
    }

    // ANIMATION STYLE 2: Fade in with scale
    private void startFadeInScaleAnimation() {
        AnimationSet animationSet = new AnimationSet(true);

        // Fade in
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1500);

        // Scale up
        ScaleAnimation scaleUp = new ScaleAnimation(
                0.5f, 1.0f,
                0.5f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleUp.setDuration(1500);

        animationSet.addAnimation(fadeIn);
        animationSet.addAnimation(scaleUp);
        animationSet.setFillAfter(true);

        ivLogo.startAnimation(animationSet);

        // Delayed animation for text
        if (tvAppName != null) {
            AlphaAnimation textFadeIn = new AlphaAnimation(0.0f, 1.0f);
            textFadeIn.setDuration(1000);
            textFadeIn.setStartOffset(800);
            tvAppName.startAnimation(textFadeIn);
        }
    }

    // ANIMATION STYLE 3: Bounce from top
    private void startBounceFromTopAnimation() {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                0, 0,  // X axis: no movement
                -1000, 0  // Y axis: from -1000px to 0
        );
        translateAnimation.setDuration(1500);
        translateAnimation.setInterpolator(new BounceInterpolator());
        translateAnimation.setFillAfter(true);

        ivLogo.startAnimation(translateAnimation);

        // Add continuous gentle pulsing after bounce
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ScaleAnimation pulse = new ScaleAnimation(
                        1.0f, 1.1f,
                        1.0f, 1.1f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                );
                pulse.setDuration(1000);
                pulse.setRepeatCount(Animation.INFINITE);
                pulse.setRepeatMode(Animation.REVERSE);
                ivLogo.startAnimation(pulse);
            }
        }, 1500);
    }

    // ANIMATION STYLE 4: Rotate and scale
    private void startRotateScaleAnimation() {
        AnimationSet animationSet = new AnimationSet(true);

        // Scale animation
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0.0f, 1.0f,
                0.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(2000);

        animationSet.addAnimation(scaleAnimation);
        animationSet.setFillAfter(true);

        ivLogo.startAnimation(animationSet);

        // Add gentle pulsing after initial animation
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ScaleAnimation pulse = new ScaleAnimation(
                        1.0f, 1.15f,
                        1.0f, 1.15f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                );
                pulse.setDuration(600);
                pulse.setRepeatCount(Animation.INFINITE);
                pulse.setRepeatMode(Animation.REVERSE);
                ivLogo.startAnimation(pulse);
            }
        }, 2000);
    }

    private void navigateToNextScreen() {
        Intent intent;

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            // Go directly to menu
            intent = new Intent(SplashActivity.this, PizzaMenuActivity.class);
        } else {
            // Go to login
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        finish();

        // Optional: Add fade transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        // Disable back button on splash screen
    }
}