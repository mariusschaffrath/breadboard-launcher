package me.jxl.breadboard;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import me.jxl.breadboard.net.HassClient;
import me.jxl.breadboard.ui.AppleMusicView;
import me.jxl.breadboard.ui.CupBoardDrawer;
import me.jxl.breadboard.ui.HomeDashboardView;
import me.jxl.breadboard.ui.MariusHomeView;
import me.jxl.breadboard.ui.RecipeView;
import me.jxl.breadboard.ui.RecognizingView;
import me.jxl.breadboard.ui.SiriView;
import me.jxl.breadboard.ui.SystemStatusBarView;
import me.jxl.breadboard.ui.Theme;
import me.jxl.breadboard.ui.faces.WatchFaceGalleryView;

public class MainActivity extends Activity {
    private static final String TAG = "HomeViewOS";

    private FrameLayout rootContainer;
    private FrameLayout contentContainer;

    private SystemStatusBarView statusBar;
    private WatchFaceGalleryView watchFaceGallery;
    private RecognizingView recognizingView;
    private MariusHomeView mariusHomeView;
    private SiriView siriView;
    private RecipeView recipeView;
    private HomeDashboardView homeDashboard;
    private AppleMusicView appleMusicView;
    private CupBoardDrawer cupBoardDrawer;

    private float density;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen on 24/7 for dedicated smart display
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemUI();

        density = getResources().getDisplayMetrics().density;

        rootContainer = new FrameLayout(this);
        rootContainer.setBackgroundColor(Color.BLACK);

        // 1. Content Container (Full Edge-to-Edge)
        contentContainer = new FrameLayout(this);
        FrameLayout.LayoutParams lpContent = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        rootContainer.addView(contentContainer, lpContent);

        // 2. Initialize Views
        watchFaceGallery = new WatchFaceGalleryView(this);
        recognizingView = new RecognizingView(this);
        mariusHomeView = new MariusHomeView(this);
        siriView = new SiriView(this);
        recipeView = new RecipeView(this);
        homeDashboard = new HomeDashboardView(this);
        appleMusicView = new AppleMusicView(this);

        // Floating System Status Bar at Top
        statusBar = new SystemStatusBarView(this);
        FrameLayout.LayoutParams lpStatus = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        lpStatus.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        lpStatus.topMargin = Theme.dp(10, density);
        lpStatus.leftMargin = Theme.dp(20, density);
        lpStatus.rightMargin = Theme.dp(20, density);
        rootContainer.addView(statusBar, lpStatus);

        // Navigation Interactions
        // A. Tap on WatchFace (DadSon or any other) -> starts recognizing
        watchFaceGallery.setOnRecognizeTriggerListener(() -> {
            showView(recognizingView, false);
            recognizingView.startRecognitionFlow();
        });
        watchFaceGallery.setOnOpenMusicListener(() -> {
            statusBar.setActiveTab(SystemStatusBarView.TAB_MUSIC);
            showView(appleMusicView, true);
        });

        // B. Recognition Complete -> opens Marius Home
        recognizingView.setOnRecognizedListener(() -> showView(mariusHomeView, false));

        // C. Marius Home Navigation
        mariusHomeView.setOnNavigateListener(new MariusHomeView.OnNavigateListener() {
            @Override
            public void onOpenMusic() {
                statusBar.setActiveTab(SystemStatusBarView.TAB_MUSIC);
                showView(appleMusicView, true);
            }

            @Override
            public void onOpenHome() {
                statusBar.setActiveTab(SystemStatusBarView.TAB_HOME);
                showView(homeDashboard, true);
            }

            @Override
            public void onOpenRecipe() {
                showView(recipeView, false);
            }

            @Override
            public void onTriggerSiri() {
                showView(siriView, false);
                siriView.startSiriFlow();
            }

            @Override
            public void onResetToGallery() {
                statusBar.setActiveTab(SystemStatusBarView.TAB_FACES);
                showView(watchFaceGallery, false);
            }
        });

        // D. Siri & Recipe Dismiss back to Marius Home
        siriView.setOnDismissListener(() -> showView(mariusHomeView, false));
        recipeView.setOnDismissListener(() -> showView(mariusHomeView, false));

        homeDashboard.setOnOpenMusicListener(() -> {
            statusBar.setActiveTab(SystemStatusBarView.TAB_MUSIC);
            showView(appleMusicView, true);
        });

        // Status Bar Tab Switching
        statusBar.setOnTabChangeListener(tabIndex -> {
            switch (tabIndex) {
                case SystemStatusBarView.TAB_HOME:
                    showView(homeDashboard, true);
                    break;
                case SystemStatusBarView.TAB_FACES:
                    showView(watchFaceGallery, false);
                    break;
                case SystemStatusBarView.TAB_MUSIC:
                    showView(appleMusicView, true);
                    break;
                case SystemStatusBarView.TAB_CONTROL:
                    if (cupBoardDrawer != null) {
                        cupBoardDrawer.toggle();
                    }
                    break;
            }
        });

        // 3. CupBoard Control Center Drawer
        cupBoardDrawer = new CupBoardDrawer(this);
        FrameLayout.LayoutParams lpDrawer = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        rootContainer.addView(cupBoardDrawer, lpDrawer);

        setContentView(rootContainer);

        // Default screen: Watch Face Gallery (starting on Dad & Son)
        showView(watchFaceGallery, false);

        // Connect Home Assistant
        HassClient.getInstance().start();
    }

    private void showView(View target, boolean showStatusBar) {
        contentContainer.removeAllViews();
        target.setAlpha(0f);

        // For views that use status bar, adjust top margin
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        if (showStatusBar) {
            lp.topMargin = Theme.dp(56, density);
            statusBar.setVisibility(View.VISIBLE);
        } else {
            lp.topMargin = 0;
            statusBar.setVisibility(View.GONE);
        }

        contentContainer.addView(target, lp);
        target.animate().alpha(1f).setDuration(220).start();
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
        if (cupBoardDrawer != null && cupBoardDrawer.isOpen()) {
            cupBoardDrawer.close();
            return;
        }
        // Return to Marius Home or Watch Face Gallery on Back press
        if (contentContainer.getChildCount() > 0) {
            View current = contentContainer.getChildAt(0);
            if (current == siriView || current == recipeView || current == homeDashboard || current == appleMusicView) {
                showView(mariusHomeView, false);
                return;
            } else if (current == mariusHomeView || current == recognizingView) {
                showView(watchFaceGallery, false);
                return;
            }
        }
    }
}
