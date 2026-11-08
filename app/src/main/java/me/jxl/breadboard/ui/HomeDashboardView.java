package me.jxl.breadboard.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import me.jxl.breadboard.net.HassClient;

/**
 * 1:1 Apple HomeView Amber Thermostat Dashboard
 * Warm amber glossy gradient with soft specular light streaks,
 * 3x2 translucent frosted glass tile grid, interactive 21°C thermostat,
 * analog clock, Intercom, Guten Morgen, Türschloss, Licht tiles,
 * and swipeable sub-screens for Air Conditioner and Lamp Dimmer.
 */
public class HomeDashboardView extends FrameLayout {

    public interface OnCloseListener {
        void onClose();
    }

    private final float density;
    private OnCloseListener closeListener;

    private int activeTab = 0; // 0: Amber Grid, 1: Air Conditioner, 2: Lamp Dimmer
    private int temperature = 21;
    private boolean lockClosed = true;
    private boolean lightOn = true;
    private boolean sceneActive = true;
    private int dimmerPct = 46;

    private TextView tvTemp;
    private ViewFlipper tabFlipper;
    private LinearLayout dotsContainer;
    private final View[] dotViews = new View[3];
    private GestureDetector gestureDetector;

    public HomeDashboardView(Context context) {
        super(context);
        this.density = getResources().getDisplayMetrics().density;
        init(context);
    }

    public void setOnCloseListener(OnCloseListener listener) {
        this.closeListener = listener;
    }

    // Backwards compatibility if needed
    public void setOnOpenMusicListener(Runnable listener) {}

    private void init(Context context) {
        // 1. Warm Amber Glossy Gradient Canvas Background
        AmberGlossyBackground bgView = new AmberGlossyBackground(context);
        addView(bgView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // 2. Root Vertical Container
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Theme.dp(36, density), Theme.dp(16, density), Theme.dp(36, density), Theme.dp(16, density));
        addView(root, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // 3. Top Navigation Bar
        LinearLayout topBar = new LinearLayout(context);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(topBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Theme.dp(44, density)));

        // Close Button "✕" (Frosted Glass Circle)
        FrameLayout closeBtn = new FrameLayout(context);
        int btnSize = Theme.dp(40, density);
        LinearLayout.LayoutParams lpClose = new LinearLayout.LayoutParams(btnSize, btnSize);
        closeBtn.setLayoutParams(lpClose);
        closeBtn.setBackground(Theme.makeGlass(0x33FFFFFF, 0x4DFFFFFF, btnSize / 2f, density));

        TextView tvClose = new TextView(context);
        tvClose.setText("✕");
        tvClose.setTextColor(Color.WHITE);
        tvClose.setTextSize(16);
        tvClose.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        tvClose.setGravity(Gravity.CENTER);
        closeBtn.addView(tvClose, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        closeBtn.setOnClickListener(v -> {
            if (closeListener != null) closeListener.onClose();
        });
        topBar.addView(closeBtn);

        // Centered Title: "WOHNZIMMER STEUERUNG"
        TextView tvTitle = new TextView(context);
        tvTitle.setText("WOHNZIMMER STEUERUNG");
        tvTitle.setTextColor(0xCCFFFFFF);
        tvTitle.setTextSize(13);
        tvTitle.setTypeface(Theme.getBoldTypeface(context));
        tvTitle.setLetterSpacing(0.12f);
        tvTitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lpTitle = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        topBar.addView(tvTitle, lpTitle);

        // Tab Dots on Right (40dp width container)
        dotsContainer = new LinearLayout(context);
        dotsContainer.setOrientation(LinearLayout.HORIZONTAL);
        dotsContainer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lpDots = new LinearLayout.LayoutParams(Theme.dp(50, density), Theme.dp(40, density));
        dotsContainer.setLayoutParams(lpDots);

        for (int i = 0; i < 3; i++) {
            final int index = i;
            View dot = new View(context);
            int dotSize = Theme.dp(8, density);
            LinearLayout.LayoutParams lpDot = new LinearLayout.LayoutParams(dotSize, dotSize);
            if (i > 0) lpDot.leftMargin = Theme.dp(6, density);
            dot.setLayoutParams(lpDot);
            dot.setOnClickListener(v -> switchToTab(index));
            dotViews[i] = dot;
            dotsContainer.addView(dot);
        }
        updateDots();
        topBar.addView(dotsContainer);

        // 4. Tab Flipper (Screen 0: Amber Grid, Screen 1: AC, Screen 2: Dimmer)
        tabFlipper = new ViewFlipper(context);
        LinearLayout.LayoutParams lpFlipper = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        lpFlipper.topMargin = Theme.dp(12, density);
        root.addView(tabFlipper, lpFlipper);

        tabFlipper.addView(createMainAmberGrid(context));
        tabFlipper.addView(createAirConditionerScreen(context));
        tabFlipper.addView(createLampDimmerScreen(context));

        // 5. Swipe Gesture Detector
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > Theme.dp(60, density)) {
                    if (diffX < 0) {
                        // Swipe Left -> Next Tab
                        switchToTab((activeTab + 1) % 3);
                    } else {
                        // Swipe Right -> Prev Tab
                        switchToTab((activeTab + 2) % 3);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(ev);
        }
        return super.onInterceptTouchEvent(ev);
    }

    private void switchToTab(int tabIndex) {
        if (tabIndex == activeTab) return;
        boolean forward = tabIndex > activeTab;
        activeTab = tabIndex;
        updateDots();

        if (forward) {
            tabFlipper.setInAnimation(getContext(), android.R.anim.slide_in_left);
            tabFlipper.setOutAnimation(getContext(), android.R.anim.slide_out_right);
        } else {
            tabFlipper.setInAnimation(getContext(), android.R.anim.fade_in);
            tabFlipper.setOutAnimation(getContext(), android.R.anim.fade_out);
        }
        tabFlipper.setDisplayedChild(activeTab);
    }

    private void updateDots() {
        for (int i = 0; i < 3; i++) {
            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.OVAL);
            if (i == activeTab) {
                gd.setColor(Color.WHITE);
            } else {
                gd.setColor(0x55FFFFFF);
            }
            dotViews[i].setBackground(gd);
        }
    }

    // =========================================================================
    // SCREEN 0: Main 3x2 Amber Grid
    // =========================================================================
    private View createMainAmberGrid(Context context) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);

        // Row 1: Thermostat (2 Columns) & Analog Clock
        LinearLayout row1 = new LinearLayout(context);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lpRow1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Theme.dp(185, density));
        container.addView(row1, lpRow1);

        // Thermostat Card (Spans 2 columns)
        View thermostatCard = createThermostatCard(context);
        LinearLayout.LayoutParams lpThermo = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 2.5f);
        lpThermo.rightMargin = Theme.dp(12, density);
        row1.addView(thermostatCard, lpThermo);

        // Analog Clock Card (10:09)
        View clockCard = createAnalogClockCard(context);
        LinearLayout.LayoutParams lpClock = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        row1.addView(clockCard, lpClock);

        // Spacing between rows
        View rowSpacer = new View(context);
        container.addView(rowSpacer, new LinearLayout.LayoutParams(1, Theme.dp(12, density)));

        // Row 2: 4 Tiles (Intercom, Guten Morgen, Türschloss, Licht)
        LinearLayout row2 = new LinearLayout(context);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lpRow2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Theme.dp(140, density));
        container.addView(row2, lpRow2);

        // 1. Intercom Tile
        View tileIntercom = createDashboardTile(context, "ılılı", "Intercom", "Überall", false, Color.WHITE, () -> {
            // Intercom chime feedback
        });
        row2.addView(tileIntercom, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

        addHorizontalGap(context, row2, 12);

        // 2. Guten Morgen Tile
        View tileMorning = createDashboardTile(context, "☀", "Guten Morgen", "Szene", sceneActive, 0xFFFDE68A, null);
        tileMorning.setOnClickListener(v -> {
            sceneActive = !sceneActive;
            updateTileState(tileMorning, "☀", "Guten Morgen", sceneActive ? "Szene Aktiv" : "Bereit", sceneActive, 0xFFFDE68A);
        });
        row2.addView(tileMorning, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

        addHorizontalGap(context, row2, 12);

        // 3. Türschloss Tile
        View tileLock = createDashboardTile(context, lockClosed ? "🔒" : "🔓", "Türschloss", lockClosed ? "Geschlossen" : "Entriegelt", !lockClosed, Color.WHITE, null);
        tileLock.setOnClickListener(v -> {
            lockClosed = !lockClosed;
            updateTileState(tileLock, lockClosed ? "🔒" : "🔓", "Türschloss", lockClosed ? "Geschlossen" : "Entriegelt", !lockClosed, 0xFF34D399);
        });
        row2.addView(tileLock, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

        addHorizontalGap(context, row2, 12);

        // 4. Licht Flur / Eingang Tile
        View tileLight = createDashboardTile(context, "💡", "Licht", lightOn ? "Flur • 80%" : "Aus", lightOn, 0xFFFDE047, null);
        tileLight.setOnClickListener(v -> {
            lightOn = !lightOn;
            updateTileState(tileLight, "💡", "Licht", lightOn ? "Flur • 80%" : "Aus", lightOn, 0xFFFDE047);
        });
        row2.addView(tileLight, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

        return container;
    }

    // Thermostat Card with Interactive + / - Chevrons
    private View createThermostatCard(Context context) {
        FrameLayout card = new FrameLayout(context);
        card.setBackground(Theme.makeGlass(0x2BFFFFFF, 0x40FFFFFF, 28f, density));
        card.setPadding(Theme.dp(24, density), Theme.dp(16, density), Theme.dp(24, density), Theme.dp(16, density));

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(content, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Left: Label & Temperature
        LinearLayout leftBox = new LinearLayout(context);
        leftBox.setOrientation(LinearLayout.VERTICAL);
        leftBox.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lpLeft = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        content.addView(leftBox, lpLeft);

        TextView tvSub = new TextView(context);
        tvSub.setText("Thermostat");
        tvSub.setTextColor(0xCCFFFFFF);
        tvSub.setTextSize(14);
        tvSub.setTypeface(Theme.getBoldTypeface(context));
        leftBox.addView(tvSub);

        TextView tvRoom = new TextView(context);
        tvRoom.setText("Wohnzimmer");
        tvRoom.setTextColor(Color.WHITE);
        tvRoom.setTextSize(18);
        tvRoom.setTypeface(Theme.getBoldTypeface(context));
        leftBox.addView(tvRoom);

        LinearLayout tempRow = new LinearLayout(context);
        tempRow.setOrientation(LinearLayout.HORIZONTAL);
        tempRow.setGravity(Gravity.BOTTOM);
        leftBox.addView(tempRow);

        tvTemp = new TextView(context);
        tvTemp.setText(temperature + "°");
        tvTemp.setTextColor(Color.WHITE);
        tvTemp.setTextSize(68);
        tvTemp.setTypeface(Theme.getRoundedTypeface(context));
        tempRow.addView(tvTemp);

        TextView tvUnit = new TextView(context);
        tvUnit.setText("C");
        tvUnit.setTextColor(0xB3FFFFFF);
        tvUnit.setTextSize(26);
        tvUnit.setTypeface(Theme.getBoldTypeface(context));
        LinearLayout.LayoutParams lpUnit = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpUnit.bottomMargin = Theme.dp(12, density);
        lpUnit.leftMargin = Theme.dp(4, density);
        tempRow.addView(tvUnit, lpUnit);

        // Right: Chevrons (Up and Down circular frosted buttons)
        LinearLayout chevronsBox = new LinearLayout(context);
        chevronsBox.setOrientation(LinearLayout.VERTICAL);
        chevronsBox.setGravity(Gravity.CENTER);
        content.addView(chevronsBox, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        int chevSize = Theme.dp(48, density);

        // Chevron Up (▲ / +)
        FrameLayout btnUp = createChevronCircle(context, "▲", chevSize);
        btnUp.setOnClickListener(v -> {
            animateButtonBounce(btnUp);
            adjustTemperature(1);
        });
        chevronsBox.addView(btnUp);

        View chevSpacer = new View(context);
        chevronsBox.addView(chevSpacer, new LinearLayout.LayoutParams(1, Theme.dp(12, density)));

        // Chevron Down (▼ / -)
        FrameLayout btnDown = createChevronCircle(context, "▼", chevSize);
        btnDown.setOnClickListener(v -> {
            animateButtonBounce(btnDown);
            adjustTemperature(-1);
        });
        chevronsBox.addView(btnDown);

        // Interactive Drag on card
        card.setOnTouchListener(new OnTouchListener() {
            private float startY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = event.getY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        float deltaY = startY - event.getY();
                        if (deltaY > Theme.dp(30, density)) {
                            adjustTemperature(1);
                        } else if (deltaY < -Theme.dp(30, density)) {
                            adjustTemperature(-1);
                        }
                        return true;
                }
                return false;
            }
        });

        return card;
    }

    private void adjustTemperature(int delta) {
        temperature = Math.max(16, Math.min(30, temperature + delta));
        tvTemp.setText(temperature + "°");
        animateTempRoll();
    }

    private void animateButtonBounce(View v) {
        v.setScaleX(0.85f);
        v.setScaleY(0.85f);
        v.animate().scaleX(1f).scaleY(1f).setDuration(220).setInterpolator(new OvershootInterpolator(2.0f)).start();
    }

    private void animateTempRoll() {
        tvTemp.setScaleX(1.18f);
        tvTemp.setScaleY(1.18f);
        tvTemp.animate().scaleX(1.0f).scaleY(1.0f).setDuration(240).setInterpolator(new OvershootInterpolator(1.8f)).start();
    }

    private FrameLayout createChevronCircle(Context context, String text, int size) {
        FrameLayout btn = new FrameLayout(context);
        btn.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        btn.setBackground(Theme.makeGlass(0x33FFFFFF, 0x4DFFFFFF, size / 2f, density));

        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(16);
        tv.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        tv.setGravity(Gravity.CENTER);
        btn.addView(tv, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        return btn;
    }

    // Analog Clock Card (10:09)
    private View createAnalogClockCard(Context context) {
        FrameLayout card = new FrameLayout(context);
        card.setBackground(Theme.makeGlass(0x2BFFFFFF, 0x40FFFFFF, 28f, density));

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        card.addView(content, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Analog Dial Canvas
        View clockDial = new AnalogDialView(context);
        int dialSize = Theme.dp(90, density);
        content.addView(clockDial, new LinearLayout.LayoutParams(dialSize, dialSize));

        TextView tvDigital = new TextView(context);
        tvDigital.setText("10:09");
        tvDigital.setTextColor(Color.WHITE);
        tvDigital.setTextSize(15);
        tvDigital.setTypeface(Theme.getRoundedTypeface(context));
        LinearLayout.LayoutParams lpDig = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpDig.topMargin = Theme.dp(8, density);
        content.addView(tvDigital, lpDig);

        return card;
    }

    // Dashboard Tile (Intercom, Guten Morgen, Türschloss, Licht)
    private View createDashboardTile(Context context, String icon, String title, String subtitle, boolean active, int activeColor, Runnable action) {
        FrameLayout tile = new FrameLayout(context);
        tile.setTag(new TileData(icon, title, subtitle, active, activeColor));
        renderTileBackground(tile, active, activeColor);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        tile.addView(content, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        TextView tvIcon = new TextView(context);
        tvIcon.setId(View.generateViewId());
        tvIcon.setText(icon);
        tvIcon.setTextColor(active ? activeColor : Color.WHITE);
        tvIcon.setTextSize(24);
        tvIcon.setGravity(Gravity.CENTER);
        content.addView(tvIcon);

        TextView tvTitle = new TextView(context);
        tvTitle.setId(View.generateViewId());
        tvTitle.setText(title);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(13);
        tvTitle.setTypeface(Theme.getBoldTypeface(context));
        tvTitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lpT = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpT.topMargin = Theme.dp(4, density);
        content.addView(tvTitle, lpT);

        TextView tvSub = new TextView(context);
        tvSub.setId(View.generateViewId());
        tvSub.setText(subtitle);
        tvSub.setTextColor(active ? activeColor : 0xAAFFFFFF);
        tvSub.setTextSize(11);
        tvSub.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        tvSub.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lpS = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpS.topMargin = Theme.dp(2, density);
        content.addView(tvSub, lpS);

        tile.setOnClickListener(v -> {
            animateButtonBounce(tile);
            if (action != null) action.run();
        });

        return tile;
    }

    private void renderTileBackground(View tile, boolean active, int activeColor) {
        int fill = active ? ((activeColor & 0x00FFFFFF) | 0x48000000) : 0x1FFFFFFF;
        int stroke = active ? activeColor : 0x33FFFFFF;
        tile.setBackground(Theme.makeGlass(fill, stroke, 24f, density));
    }

    private void updateTileState(View tile, String icon, String title, String subtitle, boolean active, int activeColor) {
        renderTileBackground(tile, active, activeColor);
        if (tile instanceof FrameLayout) {
            FrameLayout fl = (FrameLayout) tile;
            if (fl.getChildCount() > 0 && fl.getChildAt(0) instanceof LinearLayout) {
                LinearLayout ll = (LinearLayout) fl.getChildAt(0);
                if (ll.getChildCount() >= 3) {
                    TextView tvIcon = (TextView) ll.getChildAt(0);
                    TextView tvTitle = (TextView) ll.getChildAt(1);
                    TextView tvSub = (TextView) ll.getChildAt(2);

                    tvIcon.setText(icon);
                    tvIcon.setTextColor(active ? activeColor : Color.WHITE);
                    tvTitle.setText(title);
                    tvSub.setText(subtitle);
                    tvSub.setTextColor(active ? activeColor : 0xAAFFFFFF);
                }
            }
        }
    }

    private static class TileData {
        final String icon, title, subtitle;
        final boolean active;
        final int activeColor;
        TileData(String icon, String title, String subtitle, boolean active, int activeColor) {
            this.icon = icon; this.title = title; this.subtitle = subtitle;
            this.active = active; this.activeColor = activeColor;
        }
    }

    // =========================================================================
    // SCREEN 1: Cooling / Air Conditioner
    // =========================================================================
    private View createAirConditionerScreen(Context context) {
        FrameLayout card = new FrameLayout(context);
        GradientDrawable acBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF0284C7, 0xFF0369A1}
        );
        acBg.setCornerRadius(Theme.dp(28, density));
        card.setBackground(acBg);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        card.addView(content, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        TextView tvSnow = new TextView(context);
        tvSnow.setText("❄");
        tvSnow.setTextColor(Color.WHITE);
        tvSnow.setTextSize(54);
        content.addView(tvSnow);

        TextView tvCool = new TextView(context);
        tvCool.setText("Kühlen auf 20°");
        tvCool.setTextColor(Color.WHITE);
        tvCool.setTextSize(36);
        tvCool.setTypeface(Theme.getRoundedTypeface(context));
        LinearLayout.LayoutParams lpC = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpC.topMargin = Theme.dp(10, density);
        content.addView(tvCool, lpC);

        TextView tvSub = new TextView(context);
        tvSub.setText("Klimaanlage aktiv • Raumklima kühlt ab");
        tvSub.setTextColor(0xD9FFFFFF);
        tvSub.setTextSize(15);
        tvSub.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        LinearLayout.LayoutParams lpS = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpS.topMargin = Theme.dp(6, density);
        content.addView(tvSub, lpS);

        return card;
    }

    // =========================================================================
    // SCREEN 2: Lamp Dimmer Screen
    // =========================================================================
    private View createLampDimmerScreen(Context context) {
        FrameLayout card = new FrameLayout(context);
        GradientDrawable dimBg = new GradientDrawable();
        dimBg.setColor(0xFF1E1E24);
        dimBg.setCornerRadius(Theme.dp(28, density));
        card.setBackground(dimBg);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        content.setPadding(Theme.dp(48, density), Theme.dp(24, density), Theme.dp(48, density), Theme.dp(24, density));
        card.addView(content, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Header Row: Room name & percentage
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        content.addView(row, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout titleBox = new LinearLayout(context);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        row.addView(titleBox, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvDimTag = new TextView(context);
        tvDimTag.setText("DIMMER STEUERUNG");
        tvDimTag.setTextColor(0xFF9CA3AF);
        tvDimTag.setTextSize(12);
        tvDimTag.setTypeface(Theme.getBoldTypeface(context));
        tvDimTag.setLetterSpacing(0.08f);
        titleBox.addView(tvDimTag);

        TextView tvRoom = new TextView(context);
        tvRoom.setText("Wohnzimmer Deckenlicht");
        tvRoom.setTextColor(Color.WHITE);
        tvRoom.setTextSize(22);
        tvRoom.setTypeface(Theme.getBoldTypeface(context));
        titleBox.addView(tvRoom);

        TextView tvPct = new TextView(context);
        tvPct.setText(dimmerPct + "%");
        tvPct.setTextColor(0xFFFDE047);
        tvPct.setTextSize(34);
        tvPct.setTypeface(Theme.getRoundedTypeface(context));
        row.addView(tvPct);

        // Slider Bar
        DimmerSliderView slider = new DimmerSliderView(context, dimmerPct, pct -> {
            dimmerPct = pct;
            tvPct.setText(dimmerPct + "%");
        });
        LinearLayout.LayoutParams lpSlide = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Theme.dp(42, density));
        lpSlide.topMargin = Theme.dp(28, density);
        content.addView(slider, lpSlide);

        return card;
    }

    private void addHorizontalGap(Context context, LinearLayout layout, int dp) {
        View gap = new View(context);
        layout.addView(gap, new LinearLayout.LayoutParams(Theme.dp(dp, density), 1));
    }

    // =========================================================================
    // Custom Views: Background Glossy Amber Canvas & Analog Clock Dial
    // =========================================================================
    private static class AmberGlossyBackground extends View {
        private final Paint bgPaint = new Paint();
        private final Paint streakPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        AmberGlossyBackground(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();
            if (w == 0 || h == 0) return;

            // Warm Amber Glossy Gradient
            LinearGradient grad = new LinearGradient(
                    0, 0, w, h,
                    new int[]{0xFFD97706, 0xFFB45309, 0xFF78350F},
                    new float[]{0f, 0.5f, 1f},
                    Shader.TileMode.CLAMP
            );
            bgPaint.setShader(grad);
            canvas.drawRect(0, 0, w, h, bgPaint);

            // Specular Soft Light Streaks
            streakPaint.setStyle(Paint.Style.FILL);
            RadialGradient radial = new RadialGradient(
                    w * 0.15f, h * 0.15f, w * 0.5f,
                    new int[]{0x3DFDE68A, 0x00FDE68A},
                    null, Shader.TileMode.CLAMP
            );
            streakPaint.setShader(radial);
            canvas.drawCircle(w * 0.15f, h * 0.15f, w * 0.5f, streakPaint);
        }
    }

    private class AnalogDialView extends View {
        private final Paint dialPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint handPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        AnalogDialView(Context context) {
            super(context);
            dialPaint.setStyle(Paint.Style.STROKE);
            dialPaint.setColor(0x66FFFFFF);
            dialPaint.setStrokeWidth(Theme.dp(2.5f, density));

            handPaint.setColor(Color.WHITE);
            handPaint.setStrokeCap(Paint.Cap.ROUND);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();
            if (w == 0 || h == 0) return;

            float cx = w / 2f;
            float cy = h / 2f;
            float radius = Math.min(cx, cy) - Theme.dp(4, density);

            // Outer dial ring
            canvas.drawCircle(cx, cy, radius, dialPaint);

            // 10:09 Hands
            // Hour hand pointing to ~10 (approx 305 degrees)
            canvas.save();
            canvas.rotate(305f, cx, cy);
            handPaint.setStrokeWidth(Theme.dp(3.5f, density));
            canvas.drawLine(cx, cy, cx, cy - radius * 0.55f, handPaint);
            canvas.restore();

            // Minute hand pointing to ~9 minutes (approx 54 degrees)
            canvas.save();
            canvas.rotate(54f, cx, cy);
            handPaint.setStrokeWidth(Theme.dp(2.5f, density));
            canvas.drawLine(cx, cy, cx, cy - radius * 0.8f, handPaint);
            canvas.restore();

            // Center Pin
            handPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx, cy, Theme.dp(3.5f, density), handPaint);
        }
    }

    private class DimmerSliderView extends View {
        public interface OnValueChangeListener {
            void onValueChanged(int pct);
        }

        private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int pct;
        private final OnValueChangeListener listener;

        DimmerSliderView(Context context, int initialPct, OnValueChangeListener listener) {
            super(context);
            this.pct = initialPct;
            this.listener = listener;

            trackPaint.setColor(0x33FFFFFF);
            fillPaint.setColor(0xFFFDE047);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();
            if (w == 0 || h == 0) return;

            float corner = h / 2f;
            RectF trackRect = new RectF(0, 0, w, h);
            canvas.drawRoundRect(trackRect, corner, corner, trackPaint);

            float fillW = w * (pct / 100f);
            if (fillW > 0) {
                RectF fillRect = new RectF(0, 0, fillW, h);
                canvas.drawRoundRect(fillRect, corner, corner, fillPaint);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                float x = Math.max(0, Math.min(getWidth(), event.getX()));
                pct = Math.round((x / (float) getWidth()) * 100f);
                invalidate();
                if (listener != null) listener.onValueChanged(pct);
                return true;
            }
            return super.onTouchEvent(event);
        }
    }
}
