package com.delaroystudios.pizza.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ProgressBar; // Import ProgressBar

import androidx.appcompat.app.AppCompatActivity;

import com.delaroystudios.pizza.R;
import com.delaroystudios.pizza.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 15000; // 15 seconds
    private ImageView ivLogo;
    private TextView tvAppName;
    private SessionManager sessionManager;
    private ProgressBar progressBar; // Declare ProgressBar

    // Removed dot1, dot2, dot3, dot4 declarations

    // CHOOSE YOUR LOGO ROTATION STYLE:
    // 1 = Single smooth 360° rotation over 5 seconds
    // 2 = Continuous rotation (multiple spins)
    // 3 = Rotate with pulse effect
    // 4 = Rotate with bounce effect
    private static final int ROTATION_STYLE = 1;

    // The LOADING_ANIMATION switch is now obsolete, but kept the value.
    private static final int LOADING_ANIMATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(this);
        ivLogo = findViewById(R.id.iv_logo);
        tvAppName = findViewById(R.id.tv_app_name);

        // Initialize ProgressBar
        progressBar = findViewById(R.id.progress_bar);

        // Removed initialization of dot1, dot2, dot3, dot4

        // Start animations (Logo Rotation remains, Loading Animation removed)
        startLogoRotation();

        // No custom loading animation is started, the ProgressBar will just be visible

        // Navigate after delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateToNextScreen();
            }
        }, SPLASH_DURATION);
    }

    // ==================== LOGO ROTATION ANIMATIONS ====================

    private void startLogoRotation() {
        switch (ROTATION_STYLE) {
            case 1:
                startSmoothRotation();
                break;
            case 2:
                startContinuousRotation();
                break;
            case 3:
                startRotateWithPulse();
                break;
            case 4:
                startRotateWithBounce();
                break;
            default:
                startSmoothRotation();
        }
    }

    // ROTATION STYLE 1: Single smooth 360° rotation over 5 seconds (RECOMMENDED)
    private void startSmoothRotation() {
        RotateAnimation rotate = new RotateAnimation(
                0f, 360f,  // From 0° to 360°
                Animation.RELATIVE_TO_SELF, 0.5f,  // Pivot X at center
                Animation.RELATIVE_TO_SELF, 0.5f   // Pivot Y at center
        );

        rotate.setDuration(SPLASH_DURATION);  // Exactly 5 seconds
        rotate.setInterpolator(new LinearInterpolator());  // Smooth, constant speed
        rotate.setFillAfter(true);  // Stay at final position

        ivLogo.startAnimation(rotate);

        // Fade in text during rotation
        if (tvAppName != null) {
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            fadeIn.setDuration(2000);
            fadeIn.setStartOffset(1000);
            fadeIn.setFillAfter(true);
            tvAppName.startAnimation(fadeIn);
        }
    }

    // ROTATION STYLE 2: Continuous rotation (multiple spins in 5 seconds)
    private void startContinuousRotation() {
        RotateAnimation rotate = new RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );

        rotate.setDuration(2000);  // Each rotation takes 2 seconds
        rotate.setRepeatCount(2);  // Repeat 2 times (total 3 rotations in 6 seconds)
        rotate.setInterpolator(new LinearInterpolator());
        rotate.setFillAfter(true);

        ivLogo.startAnimation(rotate);

        // Fade in text
        if (tvAppName != null) {
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            fadeIn.setDuration(1500);
            fadeIn.setStartOffset(500);
            fadeIn.setFillAfter(true);
            tvAppName.startAnimation(fadeIn);
        }
    }

    // ROTATION STYLE 3: Rotate with pulse effect
    private void startRotateWithPulse() {
        AnimationSet animSet = new AnimationSet(false);

        // Rotation
        RotateAnimation rotate = new RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(SPLASH_DURATION);
        rotate.setInterpolator(new LinearInterpolator());

        // Scale/Pulse effect
        ScaleAnimation pulse = new ScaleAnimation(
                1.0f, 1.15f,
                1.0f, 1.15f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        pulse.setDuration(1000);
        pulse.setRepeatCount(4);  // Pulse 5 times during rotation
        pulse.setRepeatMode(Animation.REVERSE);

        animSet.addAnimation(rotate);
        animSet.addAnimation(pulse);
        animSet.setFillAfter(true);

        ivLogo.startAnimation(animSet);

        // Fade text
        if (tvAppName != null) {
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            fadeIn.setDuration(1500);
            fadeIn.setFillAfter(true);
            tvAppName.startAnimation(fadeIn);
        }
    }

    // ROTATION STYLE 4: Rotate with bounce effect
    private void startRotateWithBounce() {
        // First: Bounce in from top
        TranslateAnimation bounceIn = new TranslateAnimation(
                0, 0,
                -800, 0
        );
        bounceIn.setDuration(1500);
        bounceIn.setInterpolator(new BounceInterpolator());
        bounceIn.setFillAfter(true);

        ivLogo.startAnimation(bounceIn);

        // Then: Start rotating after bounce
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                RotateAnimation rotate = new RotateAnimation(
                        0f, 360f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                );
                rotate.setDuration(3500);  // Remaining time
                rotate.setInterpolator(new LinearInterpolator());
                rotate.setFillAfter(true);
                ivLogo.startAnimation(rotate);
            }
        }, 1500);

        // Fade text
        if (tvAppName != null) {
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            fadeIn.setDuration(1000);
            fadeIn.setStartOffset(2000);
            fadeIn.setFillAfter(true);
            tvAppName.startAnimation(fadeIn);
        }
    }

    // ==================== LOADING ANIMATIONS (All dot methods removed) ====================

    // The previous 'startLoadingAnimation' method and all dot-related sub-methods are removed.
    // The built-in ProgressBar handles the loading visual now.

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