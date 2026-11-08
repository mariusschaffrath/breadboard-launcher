package me.jxl.breadboard.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.jxl.breadboard.net.HassClient;

public class SystemStatusBarView extends LinearLayout implements HassClient.EntityListener {

    public interface OnTabChangeListener {
        void onTabChanged(int tabIndex);
    }

    public static final int TAB_HOME = 0;
    public static final int TAB_FACES = 1;
    public static final int TAB_MUSIC = 2;
    public static final int TAB_CONTROL = 3;

    private int activeTab = TAB_FACES; // Default to watch face
    private OnTabChangeListener tabListener;

    private TextView brandText;
    private TextView roomStatusPill;
    private TextView tabHome;
    private TextView tabFaces;
    private TextView tabMusic;
    private TextView tabControl;

    private float density;

    public SystemStatusBarView(Context context) {
        super(context);
        init(context);
    }

    public SystemStatusBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        density = getResources().getDisplayMetrics().density;
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(Theme.dp(16, density), Theme.dp(4, density), Theme.dp(16, density), Theme.dp(4, density));

        // Floating glass capsule background
        setBackground(Theme.makePill(0xD0121217, Theme.GLASS_BORDER, density));

        // Left: Apple Brand Indicator
        brandText = new TextView(context);
        brandText.setText(" HomeView 27.2");
        brandText.setTypeface(Theme.getDisplayBold(context));
        brandText.setTextSize(14f);
        brandText.setTextColor(Theme.TEXT_WHITE);
        LayoutParams lpBrand = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpBrand.rightMargin = Theme.dp(20, density);
        addView(brandText, lpBrand);

        // Center: Room & Climate Pill
        roomStatusPill = new TextView(context);
        roomStatusPill.setText("🛏️ Marius Zimmer • 22.0°C • Standby");
        roomStatusPill.setTypeface(Theme.getRoundedBold(context));
        roomStatusPill.setTextSize(13f);
        roomStatusPill.setTextColor(Theme.TEXT_SECONDARY);
        roomStatusPill.setPadding(Theme.dp(12, density), Theme.dp(4, density), Theme.dp(12, density), Theme.dp(4, density));
        roomStatusPill.setBackground(Theme.makePill(0x33FFFFFF, 0x1AFFFFFF, density));
        
        LayoutParams lpPill = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        lpPill.gravity = Gravity.CENTER_VERTICAL;
        addView(roomStatusPill, lpPill);

        // Right: Navigation Tabs
        LinearLayout tabContainer = new LinearLayout(context);
        tabContainer.setOrientation(HORIZONTAL);
        tabContainer.setGravity(Gravity.CENTER_VERTICAL);

        tabHome = createTab(context, "🏠 Home", TAB_HOME);
        tabFaces = createTab(context, "⏱️ Zifferblätter", TAB_FACES);
        tabMusic = createTab(context, "🎵 Musik", TAB_MUSIC);
        tabControl = createTab(context, "⚙️ Control", TAB_CONTROL);

        tabContainer.addView(tabHome);
        tabContainer.addView(tabFaces);
        tabContainer.addView(tabMusic);
        tabContainer.addView(tabControl);

        addView(tabContainer, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        updateTabStyles();

        // Listen for HA entity updates
        HassClient.getInstance().addEntityListener(this);
        updateRoomStatus();
    }

    private TextView createTab(Context context, String title, int tabIndex) {
        TextView tab = new TextView(context);
        tab.setText(title);
        tab.setTypeface(Theme.getRoundedBold(context));
        tab.setTextSize(13f);
        tab.setPadding(Theme.dp(10, density), Theme.dp(5, density), Theme.dp(10, density), Theme.dp(5, density));
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.leftMargin = Theme.dp(6, density);
        tab.setLayoutParams(lp);

        tab.setOnClickListener(v -> {
            if (tabIndex == TAB_CONTROL) {
                if (tabListener != null) {
                    tabListener.onTabChanged(TAB_CONTROL);
                }
            } else {
                setActiveTab(tabIndex);
            }
        });
        return tab;
    }

    public void setActiveTab(int tabIndex) {
        if (tabIndex == TAB_CONTROL) return;
        this.activeTab = tabIndex;
        updateTabStyles();
        if (tabListener != null) {
            tabListener.onTabChanged(tabIndex);
        }
    }

    public int getActiveTab() {
        return activeTab;
    }

    private void updateTabStyles() {
        applyTabStyle(tabHome, activeTab == TAB_HOME);
        applyTabStyle(tabFaces, activeTab == TAB_FACES);
        applyTabStyle(tabMusic, activeTab == TAB_MUSIC);
        applyTabStyle(tabControl, false);
    }

    private void applyTabStyle(TextView tab, boolean isSelected) {
        if (isSelected) {
            tab.setBackground(Theme.makePill(0xFFFFFFFF, 0, density));
            tab.setTextColor(Color.BLACK);
        } else {
            tab.setBackground(Theme.makePill(0x22FFFFFF, 0x1AFFFFFF, density));
            tab.setTextColor(Theme.TEXT_WHITE);
        }
    }

    public void setOnTabChangeListener(OnTabChangeListener listener) {
        this.tabListener = listener;
    }

    @Override
    public void onEntityUpdated(String entityId, HassClient.EntityState state) {
        if (entityId.contains("temperature") || entityId.contains("light.marius_zimmer") || entityId.contains("media_player")) {
            post(this::updateRoomStatus);
        }
    }

    private void updateRoomStatus() {
        HassClient client = HassClient.getInstance();
        HassClient.EntityState tempState = client.getState("sensor.0x00158d000548bad0_device_temperature");
        HassClient.EntityState lightState = client.getState("light.marius_zimmer");
        HassClient.EntityState mediaState = client.getState("media_player.marius_zimmer");

        double temp = (tempState != null) ? tempState.getTemperature() : 22.0;
        boolean lightsOn = (lightState != null && lightState.isOn());
        boolean musicPlaying = (mediaState != null && mediaState.isOn());

        StringBuilder sb = new StringBuilder();
        sb.append("🛏️ Marius Zimmer • ");
        sb.append(String.format("%.1f°C", temp));
        sb.append(" • ");
        if (musicPlaying) {
            sb.append("Spielt Musik");
        } else if (lightsOn) {
            sb.append("Licht an");
        } else {
            sb.append("Standby");
        }

        roomStatusPill.setText(sb.toString());
    }
}
