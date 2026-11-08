package me.jxl.breadboard.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.jxl.breadboard.net.HassClient;

public class PebbleTileView extends LinearLayout implements HassClient.EntityListener {

    public enum TileType {
        LIGHT,
        COVER,
        SCENE,
        MEDIA
    }

    private final String entityId;
    private final String defaultTitle;
    private final String defaultIcon;
    private final TileType type;
    private final float density;

    private TextView iconView;
    private TextView titleView;
    private TextView stateView;

    private boolean isTurnedOn = false;

    public PebbleTileView(Context context, String entityId, String defaultTitle, String defaultIcon, TileType type) {
        super(context);
        this.entityId = entityId;
        this.defaultTitle = defaultTitle;
        this.defaultIcon = defaultIcon;
        this.type = type;
        this.density = getResources().getDisplayMetrics().density;
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        setPadding(Theme.dp(16, density), Theme.dp(14, density), Theme.dp(16, density), Theme.dp(14, density));

        // Icon
        iconView = new TextView(context);
        iconView.setText(defaultIcon);
        iconView.setTextSize(24f);
        iconView.setGravity(Gravity.CENTER);
        LayoutParams lpIcon = new LayoutParams(Theme.dp(40, density), Theme.dp(40, density));
        lpIcon.bottomMargin = Theme.dp(10, density);
        iconView.setLayoutParams(lpIcon);
        addView(iconView);

        // Title
        titleView = new TextView(context);
        titleView.setText(defaultTitle);
        titleView.setTypeface(Theme.getDisplayBold(context));
        titleView.setTextSize(15f);
        titleView.setTextColor(Theme.TEXT_WHITE);
        titleView.setSingleLine(true);
        addView(titleView);

        // State Subtitle
        stateView = new TextView(context);
        stateView.setText("Wird geladen...");
        stateView.setTypeface(Theme.getRoundedBold(context));
        stateView.setTextSize(12f);
        stateView.setTextColor(Theme.TEXT_SECONDARY);
        stateView.setSingleLine(true);
        LayoutParams lpState = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpState.topMargin = Theme.dp(3, density);
        addView(stateView, lpState);

        // Click interaction
        setOnClickListener(v -> handleAction());

        // Spring tactile press effect
        setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
                    break;
            }
            return false;
        });

        updateVisualState();

        // Register listener with HassClient
        HassClient.getInstance().addEntityListener(this);
    }

    private void handleAction() {
        HassClient client = HassClient.getInstance();
        switch (type) {
            case LIGHT:
                client.toggleLight(entityId);
                break;
            case COVER:
                client.toggleCover(entityId);
                break;
            case SCENE:
                client.triggerScene(entityId);
                break;
            case MEDIA:
                client.mediaPlayPause(entityId);
                break;
        }
    }

    @Override
    public void onEntityUpdated(String updatedEntityId, HassClient.EntityState state) {
        if (entityId.equalsIgnoreCase(updatedEntityId)) {
            post(this::updateVisualState);
        }
    }

    public void updateVisualState() {
        HassClient.EntityState s = HassClient.getInstance().getState(entityId);
        if (s != null) {
            isTurnedOn = s.isOn();
            String rawState = s.state;
            if (type == TileType.LIGHT) {
                if (isTurnedOn) {
                    int bri = s.getBrightness();
                    int pct = (int) Math.round((bri / 255.0) * 100);
                    stateView.setText(pct > 0 ? "An • " + pct + "%" : "An");
                } else {
                    stateView.setText("Aus");
                }
            } else if (type == TileType.COVER) {
                stateView.setText("open".equalsIgnoreCase(rawState) ? "Geöffnet" : "Geschlossen");
            } else if (type == TileType.SCENE) {
                stateView.setText("Aktivieren");
            } else if (type == TileType.MEDIA) {
                stateView.setText(isTurnedOn ? "Wiedergabe" : "Pausiert");
            } else {
                stateView.setText(rawState);
            }
        } else {
            stateView.setText("Bereit");
        }

        // Apply dynamic Apple glass styling
        int bgColor;
        int borderColor;
        int iconBg;

        if (isTurnedOn) {
            switch (type) {
                case LIGHT:
                    bgColor = 0x33FF9F0A; // Amber glow
                    borderColor = Theme.ACCENT_AMBER;
                    iconBg = 0x55FF9F0A;
                    break;
                case COVER:
                    bgColor = 0x330A84FF; // Electric Blue glow
                    borderColor = Theme.ACCENT_BLUE;
                    iconBg = 0x550A84FF;
                    break;
                case SCENE:
                    bgColor = 0x33BF5AF2; // Purple glow
                    borderColor = Theme.ACCENT_PURPLE;
                    iconBg = 0x55BF5AF2;
                    break;
                default:
                    bgColor = 0x3330D158;
                    borderColor = Theme.ACCENT_SAGE;
                    iconBg = 0x5530D158;
                    break;
            }
            titleView.setTextColor(Theme.TEXT_WHITE);
            stateView.setTextColor(borderColor);
        } else {
            bgColor = Theme.GLASS_CARD;
            borderColor = Theme.GLASS_BORDER;
            iconBg = 0x22FFFFFF;
            titleView.setTextColor(Theme.TEXT_WHITE);
            stateView.setTextColor(Theme.TEXT_SECONDARY);
        }

        setBackground(Theme.makeGlass(bgColor, borderColor, 20f, density));
        iconView.setBackground(Theme.makePill(iconBg, 0, density));
    }
}
