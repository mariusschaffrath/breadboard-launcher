package me.jxl.breadboard;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import me.jxl.breadboard.net.HassClient;
import me.jxl.breadboard.ui.AppleMusicView;
import me.jxl.breadboard.ui.HomeDashboardView;
import me.jxl.breadboard.ui.MariusHomeView;
import me.jxl.breadboard.ui.RecipeView;
import me.jxl.breadboard.ui.RecognizingView;
import me.jxl.breadboard.ui.SiriView;
import me.jxl.breadboard.ui.Theme;
import me.jxl.breadboard.ui.faces.FaceThumbnailView;
import me.jxl.breadboard.ui.faces.FacesGalleryMarqueeView;
import me.jxl.breadboard.ui.faces.FullFaceContainerView;

public class MainActivity extends Activity {
    private static final String TAG = "HomeViewOS";

    private FrameLayout rootContainer;
    private FrameLayout contentContainer;

    private FacesGalleryMarqueeView facesGallery;
    private FullFaceContainerView fullFaceView;
    private RecognizingView recognizingView;
    private MariusHomeView mariusHomeView;
    private SiriView siriView;
    private RecipeView recipeView;
    private HomeDashboardView homeDashboard;
    private AppleMusicView appleMusicView;

    private View currentActiveView;
    private float density;

    private enum TransitionStyle {
        NONE,
        SLIDE_UP,
        ZOOM_IN,
        ZOOM_OUT,
        FADE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen on 24/7 for smart display
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideSystemUI();

        // Enforce sticky immersive mode on Fire OS 5
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                getWindow().getDecorView().postDelayed(this::hideSystemUI, 300);
            }
        });

        density = getResources().getDisplayMetrics().density;

        rootContainer = new FrameLayout(this);
        rootContainer.setBackgroundColor(Color.BLACK);

        // Content Container (Edge-to-Edge 1024x600, 0 padding, 0 margin)
        contentContainer = new FrameLayout(this);
        rootContainer.addView(contentContainer, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Initialize All Apple HomeView Native Screens
        facesGallery = new FacesGalleryMarqueeView(this);
        fullFaceView = new FullFaceContainerView(this);
        recognizingView = new RecognizingView(this);
        mariusHomeView = new MariusHomeView(this);
        siriView = new SiriView(this);
        recipeView = new RecipeView(this);
        homeDashboard = new HomeDashboardView(this);
        appleMusicView = new AppleMusicView(this);

        // Screen 1: Faces Gallery Navigation
        facesGallery.setOnFaceSelectedListener(kind -> {
            fullFaceView.setFace(this, kind);
            transitionTo(fullFaceView, TransitionStyle.ZOOM_IN);
        });

        // Screen 2: Full Face Interactions
        fullFaceView.setOnFaceInteractionListener(new FullFaceContainerView.OnFaceInteractionListener() {
            @Override
            public void onTapToRecognize() {
                transitionTo(recognizingView, TransitionStyle.FADE);
                recognizingView.startRecognitionFlow();
            }

            @Override
            public void onBackToGallery() {
                transitionTo(facesGallery, TransitionStyle.ZOOM_OUT);
            }

            @Override
            public void onOpenMusic() {
                transitionTo(appleMusicView, TransitionStyle.SLIDE_UP);
            }
        });

        // Screen 3: Recognition Complete -> Marius Home
        recognizingView.setOnRecognizedListener(() -> {
            transitionTo(mariusHomeView, TransitionStyle.FADE);
        });

        // Screen 4: Marius Home Navigation
        mariusHomeView.setOnNavigateListener(new MariusHomeView.OnNavigateListener() {
            @Override
            public void onOpenMusic() {
                transitionTo(appleMusicView, TransitionStyle.SLIDE_UP);
            }

            @Override
            public void onOpenHome() {
                transitionTo(homeDashboard, TransitionStyle.SLIDE_UP);
            }

            @Override
            public void onOpenRecipe() {
                transitionTo(recipeView, TransitionStyle.SLIDE_UP);
            }

            @Override
            public void onTriggerSiri() {
                transitionTo(siriView, TransitionStyle.FADE);
                siriView.startSiriFlow();
            }

            @Override
            public void onResetToGallery() {
                transitionTo(facesGallery, TransitionStyle.ZOOM_OUT);
            }
        });

        // Screen 5: Home Dashboard Dismiss -> Marius Home
        homeDashboard.setOnCloseListener(() -> {
            transitionTo(mariusHomeView, TransitionStyle.ZOOM_OUT);
        });

        // Screen 6: Music Dismiss -> Marius Home
        appleMusicView.setOnCloseListener(() -> {
            transitionTo(mariusHomeView, TransitionStyle.ZOOM_OUT);
        });

        // Screen 7: Siri Dismiss -> Marius Home
        siriView.setOnDismissListener(() -> {
            transitionTo(mariusHomeView, TransitionStyle.ZOOM_OUT);
        });

        // Screen 8: Recipe Dismiss -> Marius Home
        recipeView.setOnDismissListener(() -> {
            transitionTo(mariusHomeView, TransitionStyle.ZOOM_OUT);
        });

        setContentView(rootContainer);

        // Launch directly into Faces Gallery (or Dad & Son Full Face)
        transitionTo(facesGallery, TransitionStyle.NONE);

        // Connect Home Assistant client in background
        HassClient.getInstance().start();
    }

    private void transitionTo(View target, TransitionStyle style) {
        if (target == null) return;
        currentActiveView = target;

        contentContainer.removeAllViews();
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        contentContainer.addView(target, lp);

        hideSystemUI();

        switch (style) {
            case SLIDE_UP:
                target.setTranslationY(Theme.dp(160, density));
                target.setScaleX(0.94f);
                target.setScaleY(0.94f);
                target.setAlpha(0f);
                target.animate()
                        .translationY(0f)
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .alpha(1.0f)
                        .setDuration(360)
                        .setInterpolator(new OvershootInterpolator(1.1f))
                        .start();
                break;

            case ZOOM_IN:
                target.setTranslationY(0f);
                target.setScaleX(0.85f);
                target.setScaleY(0.85f);
                target.setAlpha(0f);
                target.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .alpha(1.0f)
                        .setDuration(380)
                        .setInterpolator(new DecelerateInterpolator(2.0f))
                        .start();
                break;

            case ZOOM_OUT:
                target.setTranslationY(0f);
                target.setScaleX(1.08f);
                target.setScaleY(1.08f);
                target.setAlpha(0f);
                target.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .alpha(1.0f)
                        .setDuration(320)
                        .setInterpolator(new DecelerateInterpolator(1.8f))
                        .start();
                break;

            case FADE:
                target.setTranslationY(0f);
                target.setScaleX(0.98f);
                target.setScaleY(0.98f);
                target.setAlpha(0f);
                target.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .alpha(1.0f)
                        .setDuration(400)
                        .setInterpolator(new DecelerateInterpolator(1.5f))
                        .start();
                break;

            case NONE:
            default:
                target.setTranslationY(0f);
                target.setScaleX(1.0f);
                target.setScaleY(1.0f);
                target.setAlpha(1.0f);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HassClient.getInstance().stop();
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentActiveView == homeDashboard || currentActiveView == appleMusicView
                || currentActiveView == recipeView || currentActiveView == siriView) {
            transitionTo(mariusHomeView, TransitionStyle.ZOOM_OUT);
            return;
        }
        if (currentActiveView == mariusHomeView || currentActiveView == recognizingView) {
            transitionTo(facesGallery, TransitionStyle.ZOOM_OUT);
            return;
        }
        if (currentActiveView == fullFaceView) {
            transitionTo(facesGallery, TransitionStyle.ZOOM_OUT);
            return;
        }
        super.onBackPressed();
    }
}
