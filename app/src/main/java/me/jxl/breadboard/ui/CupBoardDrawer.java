package me.jxl.breadboard.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import me.jxl.breadboard.net.HassClient;

public class CupBoardDrawer extends FrameLayout {

    private final float density;
    private LinearLayout drawerContent;
    private SeekBar brightnessBar;
    private SeekBar volumeBar;
    private TextView brightnessValueText;
    private TextView volumeValueText;

    private boolean isOpen = false;

    public CupBoardDrawer(Context context) {
        super(context);
        this.density = getResources().getDisplayMetrics().density;
        init(context);
    }

    private static class SliderCardView extends LinearLayout {
        final SeekBar bar;
        final TextView val;

        SliderCardView(Context context, String label, String icon, int initialVal, float density) {
            super(context);
            setOrientation(VERTICAL);
            setPadding(Theme.dp(16, density), Theme.dp(14, density), Theme.dp(16, density), Theme.dp(14, density));
            setBackground(Theme.makeGlass(0x33FFFFFF, 0x1AFFFFFF, 18f, density));

            LinearLayout top = new LinearLayout(context);
            top.setOrientation(HORIZONTAL);

            TextView lbl = new TextView(context);
            lbl.setText(icon + " " + label);
            lbl.setTypeface(Theme.getDisplayBold(context));
            lbl.setTextSize(12f);
            lbl.setTextColor(Theme.TEXT_MUTED);
            top.addView(lbl, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

            val = new TextView(context);
            val.setText(initialVal + "%");
            val.setTypeface(Theme.getRoundedBold(context));
            val.setTextSize(13f);
            val.setTextColor(Theme.TEXT_WHITE);
            top.addView(val);

            addView(top);

            bar = new SeekBar(context);
            bar.setMax(100);
            bar.setProgress(initialVal);
            LayoutParams lpBar = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            lpBar.topMargin = Theme.dp(10, density);
            bar.setLayoutParams(lpBar);
            addView(bar);
        }
    }

    private void init(Context context) {
        setBackgroundColor(0x99000000); // Scrim background
        setVisibility(View.GONE);

        // Tap scrim to close
        setOnClickListener(v -> close());

        // Drawer Platter
        drawerContent = new LinearLayout(context);
        drawerContent.setOrientation(LinearLayout.VERTICAL);
        drawerContent.setPadding(Theme.dp(32, density), Theme.dp(24, density), Theme.dp(32, density), Theme.dp(28, density));
        drawerContent.setBackground(Theme.makeGlass(0xE61C1C24, Theme.GLASS_BORDER, 28f, density));
        drawerContent.setClickable(true); // Don't pass clicks through to scrim

        LayoutParams lpContent = new LayoutParams(Theme.dp(560, density), LayoutParams.WRAP_CONTENT);
        lpContent.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        lpContent.topMargin = Theme.dp(40, density);
        drawerContent.setLayoutParams(lpContent);

        // Header: Title & Close Button
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(context);
        title.setText("CupBoard • Kontrollzentrum");
        title.setTypeface(Theme.getDisplayBold(context));
        title.setTextSize(20f);
        title.setTextColor(Theme.TEXT_WHITE);
        header.addView(title, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        TextView closeBtn = new TextView(context);
        closeBtn.setText("✕");
        closeBtn.setTextSize(18f);
        closeBtn.setTextColor(Theme.TEXT_SECONDARY);
        closeBtn.setGravity(Gravity.CENTER);
        closeBtn.setBackground(Theme.makePill(0x22FFFFFF, 0, density));
        closeBtn.setOnClickListener(v -> close());
        header.addView(closeBtn, new LayoutParams(Theme.dp(36, density), Theme.dp(36, density)));

        drawerContent.addView(header);

        // Dual Sliders: Brightness & Volume
        LinearLayout slidersRow = new LinearLayout(context);
        slidersRow.setOrientation(HORIZONTAL);
        LayoutParams lpSliders = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lpSliders.topMargin = Theme.dp(24, density);
        slidersRow.setLayoutParams(lpSliders);

        // 1. Brightness Card
        SliderCardView briCard = new SliderCardView(context, "HELLIGKEIT", "💡", 100, density);
        brightnessBar = briCard.bar;
        brightnessValueText = briCard.val;
        brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightnessValueText.setText(progress + "%");
                if (fromUser) {
                    HassClient.getInstance().setBrightness("light.marius_zimmer", progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 2. Volume Card
        SliderCardView volCard = new SliderCardView(context, "LAUTSTÄRKE", "🔊", 50, density);
        volumeBar = volCard.bar;
        volumeValueText = volCard.val;
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volumeValueText.setText(progress + "%");
                if (fromUser) {
                    HassClient.getInstance().setVolume("media_player.marius_zimmer", progress / 100f);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        LayoutParams lpCard1 = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        lpCard1.rightMargin = Theme.dp(8, density);
        briCard.setLayoutParams(lpCard1);

        LayoutParams lpCard2 = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        lpCard2.leftMargin = Theme.dp(8, density);
        volCard.setLayoutParams(lpCard2);

        slidersRow.addView(briCard);
        slidersRow.addView(volCard);
        drawerContent.addView(slidersRow);

        // Quick Scene Toggles Row
        LinearLayout scenesRow = new LinearLayout(context);
        scenesRow.setOrientation(HORIZONTAL);
        LayoutParams lpScenes = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lpScenes.topMargin = Theme.dp(20, density);
        scenesRow.setLayoutParams(lpScenes);

        TextView btnNight = createSceneButton(context, "🌙 Gute Nacht", 0x33BF5AF2, Theme.ACCENT_PURPLE);
        btnNight.setOnClickListener(v -> HassClient.getInstance().triggerScene("scene.marius_zimmer_nightlight"));

        TextView btnOff = createSceneButton(context, "⭕ Alle Lichter Aus", 0x33FF453A, Theme.ACCENT_RED);
        btnOff.setOnClickListener(v -> {
            try {
                HassClient.getInstance().callService("light", "turn_off",
                        new org.json.JSONObject().put("entity_id", "light.marius_zimmer"));
            } catch (Exception e) {}
        });

        scenesRow.addView(btnNight, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        scenesRow.addView(btnOff, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        drawerContent.addView(scenesRow);

        addView(drawerContent);
    }

    private TextView createSceneButton(Context context, String text, int bg, int border) {
        TextView btn = new TextView(context);
        btn.setText(text);
        btn.setTypeface(Theme.getDisplayBold(context));
        btn.setTextSize(14f);
        btn.setTextColor(Theme.TEXT_WHITE);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(Theme.dp(12, density), Theme.dp(12, density), Theme.dp(12, density), Theme.dp(12, density));
        btn.setBackground(Theme.makeGlass(bg, border, 16f, density));
        LayoutParams lp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(Theme.dp(4, density), 0, Theme.dp(4, density), 0);
        btn.setLayoutParams(lp);
        return btn;
    }

    public void open() {
        isOpen = true;
        setVisibility(View.VISIBLE);
        setAlpha(0f);
        animate().alpha(1f).setDuration(200).start();

        drawerContent.setTranslationY(-Theme.dp(100, density));
        drawerContent.animate().translationY(0).setDuration(220).start();
    }

    public void close() {
        isOpen = false;
        animate().alpha(0f).setDuration(180).withEndAction(() -> setVisibility(View.GONE)).start();
    }

    public void toggle() {
        if (isOpen) close();
        else open();
    }

    public boolean isOpen() {
        return isOpen;
    }
}
