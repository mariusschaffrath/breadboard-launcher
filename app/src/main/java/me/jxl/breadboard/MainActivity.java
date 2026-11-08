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
import me.jxl.breadboard.ui.SystemStatusBarView;
import me.jxl.breadboard.ui.Theme;
import me.jxl.breadboard.ui.faces.WatchFaceGalleryView;

public class MainActivity extends Activity {
    private static final String TAG = "HomeViewOS";

    private FrameLayout rootContainer;
    private FrameLayout contentContainer;

    private SystemStatusBarView statusBar;
    private HomeDashboardView homeDashboard;
    private WatchFaceGalleryView watchFaceGallery;
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

        // 1. Content Container (Home, Watchfaces, Music)
        contentContainer = new FrameLayout(this);
        FrameLayout.LayoutParams lpContent = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        // Add top padding for the floating status bar
        contentContainer.setPadding(0, Theme.dp(56, density), 0, 0);
        rootContainer.addView(contentContainer, lpContent);

        // Initialize Views
        homeDashboard = new HomeDashboardView(this);
        watchFaceGallery = new WatchFaceGalleryView(this);
        appleMusicView = new AppleMusicView(this);

        // Connect Mini Music Widget in WatchFace and Dashboard to Apple Music Tab
        watchFaceGallery.setOnOpenMusicListener(() -> statusBar.setActiveTab(SystemStatusBarView.TAB_MUSIC));
        homeDashboard.setOnOpenMusicListener(() -> statusBar.setActiveTab(SystemStatusBarView.TAB_MUSIC));

        // Default to Watch Face Gallery
        showView(watchFaceGallery);

        // 2. Floating System Status Bar at Top
        statusBar = new SystemStatusBarView(this);
        FrameLayout.LayoutParams lpStatus = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        lpStatus.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        lpStatus.topMargin = Theme.dp(10, density);
        lpStatus.leftMargin = Theme.dp(20, density);
        lpStatus.rightMargin = Theme.dp(20, density);
        rootContainer.addView(statusBar, lpStatus);

        // Handle Status Bar Tab Switching
        statusBar.setOnTabChangeListener(tabIndex -> {
            switch (tabIndex) {
                case SystemStatusBarView.TAB_HOME:
                    showView(homeDashboard);
                    break;
                case SystemStatusBarView.TAB_FACES:
                    showView(watchFaceGallery);
                    break;
                case SystemStatusBarView.TAB_MUSIC:
                    showView(appleMusicView);
                    break;
                case SystemStatusBarView.TAB_CONTROL:
                    if (cupBoardDrawer != null) {
                        cupBoardDrawer.toggle();
                    }
                    break;
            }
        });

        // 3. CupBoard Control Center Overlay
        cupBoardDrawer = new CupBoardDrawer(this);
        FrameLayout.LayoutParams lpDrawer = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        rootContainer.addView(cupBoardDrawer, lpDrawer);

        setContentView(rootContainer);

        // Connect to Home Assistant WebSocket
        HassClient.getInstance().start();
    }

    private void showView(View target) {
        contentContainer.removeAllViews();
        target.setAlpha(0f);
        contentContainer.addView(target, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
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
        // If CupBoard is open, close it; otherwise prevent accidental launcher exit
        if (cupBoardDrawer != null && cupBoardDrawer.isOpen()) {
            cupBoardDrawer.close();
        }
    }
}
