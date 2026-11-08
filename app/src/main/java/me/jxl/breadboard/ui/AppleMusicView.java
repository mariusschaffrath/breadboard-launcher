package me.jxl.breadboard.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import me.jxl.breadboard.net.HassClient;
import me.jxl.breadboard.net.ImageLoader;

public class AppleMusicView extends FrameLayout implements HassClient.EntityListener {

    private final float density;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private String targetEntityId = "media_player.marius_zimmer";

    private AmbientGlowView ambientGlow;
    private ImageView albumArtView;
    private TextView trackTitleView;
    private TextView artistView;
    private TextView airplayRoutePill;

    private SeekBar progressBar;
    private TextView timeElapsedView;
    private TextView timeRemainingView;

    private TextView btnShuffle;
    private TextView btnPrev;
    private TextView btnPlayPause;
    private TextView btnNext;
    private TextView btnRepeat;

    private SeekBar volumeBar;

    private boolean isPlaying = false;
    private int currentProgressSec = 95;
    private int totalDurationSec = 224;

    public AppleMusicView(Context context) {
        super(context);
        this.density = getResources().getDisplayMetrics().density;
        init(context);
    }

    private void init(Context context) {
        setBackgroundColor(0xFF000000);

        // Ambient blurred glow background
        ambientGlow = new AmbientGlowView(context);
        addView(ambientGlow, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // Main Container: Horizontal split (Artwork on Left, Controls & Scrubber on Right)
        LinearLayout mainContent = new LinearLayout(context);
        mainContent.setOrientation(LinearLayout.HORIZONTAL);
        mainContent.setGravity(Gravity.CENTER_VERTICAL);
        mainContent.setPadding(Theme.dp(48, density), Theme.dp(16, density), Theme.dp(48, density), Theme.dp(20, density));
        addView(mainContent, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // Left: Album Art with 22dp corner radius and glow
        FrameLayout artCard = new FrameLayout(context);
        int artSize = Theme.dp(240, density);
        LayoutParams lpArt = new LayoutParams(artSize, artSize);
        artCard.setLayoutParams(lpArt);
        artCard.setBackground(Theme.makeGlass(0x22FFFFFF, Theme.GLASS_SELECTED_BORDER, 22f, density));
        artCard.setPadding(Theme.dp(2, density), Theme.dp(2, density), Theme.dp(2, density), Theme.dp(2, density));

        albumArtView = new ImageView(context);
        albumArtView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        albumArtView.setImageBitmap(ImageLoader.createMusicPlaceholder(artSize, artSize));
        artCard.addView(albumArtView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        mainContent.addView(artCard);

        // Right: Metadata, Scrubber, Transport & Volume
        LinearLayout rightPane = new LinearLayout(context);
        rightPane.setOrientation(LinearLayout.VERTICAL);
        rightPane.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lpRight = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lpRight.leftMargin = Theme.dp(40, density);
        rightPane.setLayoutParams(lpRight);

        // AirPlay Pill (Tap to switch between HomePod and Apple TV)
        airplayRoutePill = new TextView(context);
        airplayRoutePill.setText(" AirPlay • Marius Zimmer (HomePod mini)");
        airplayRoutePill.setTypeface(Theme.getRoundedBold(context));
        airplayRoutePill.setTextSize(13f);
        airplayRoutePill.setTextColor(Theme.ACCENT_PINK);
        airplayRoutePill.setPadding(Theme.dp(12, density), Theme.dp(4, density), Theme.dp(12, density), Theme.dp(4, density));
        airplayRoutePill.setBackground(Theme.makePill(0x33FA2D48, 0x55FA2D48, density));
        LinearLayout.LayoutParams lpAirplay = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpAirplay.bottomMargin = Theme.dp(12, density);
        airplayRoutePill.setLayoutParams(lpAirplay);
        airplayRoutePill.setOnClickListener(v -> toggleTargetEntity());
        rightPane.addView(airplayRoutePill);

        // Track Title
        trackTitleView = new TextView(context);
        trackTitleView.setText("FEVER DREAM");
        trackTitleView.setTypeface(Theme.getDisplayBold(context));
        trackTitleView.setTextSize(26f);
        trackTitleView.setTextColor(Theme.TEXT_WHITE);
        trackTitleView.setSingleLine(true);
        rightPane.addView(trackTitleView);

        // Artist Name
        artistView = new TextView(context);
        artistView.setText("Alex Warren");
        artistView.setTypeface(Theme.getRoundedBold(context));
        artistView.setTextSize(17f);
        artistView.setTextColor(Theme.TEXT_SECONDARY);
        artistView.setSingleLine(true);
        LinearLayout.LayoutParams lpArtist = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpArtist.topMargin = Theme.dp(4, density);
        lpArtist.bottomMargin = Theme.dp(16, density);
        artistView.setLayoutParams(lpArtist);
        rightPane.addView(artistView);

        // Scrubber / Progress Bar
        progressBar = new SeekBar(context);
        progressBar.setMax(totalDurationSec);
        progressBar.setProgress(currentProgressSec);
        rightPane.addView(progressBar);

        // Time labels (Elapsed / Remaining)
        LinearLayout timeRow = new LinearLayout(context);
        timeRow.setOrientation(LinearLayout.HORIZONTAL);
        timeElapsedView = new TextView(context);
        timeElapsedView.setText("01:35");
        timeElapsedView.setTypeface(Theme.getRoundedBold(context));
        timeElapsedView.setTextSize(12f);
        timeElapsedView.setTextColor(Theme.TEXT_MUTED);

        timeRemainingView = new TextView(context);
        timeRemainingView.setText("-02:09");
        timeRemainingView.setTypeface(Theme.getRoundedBold(context));
        timeRemainingView.setTextSize(12f);
        timeRemainingView.setTextColor(Theme.TEXT_MUTED);
        timeRemainingView.setGravity(Gravity.END);

        timeRow.addView(timeElapsedView, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        timeRow.addView(timeRemainingView, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        LinearLayout.LayoutParams lpTime = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpTime.topMargin = Theme.dp(2, density);
        lpTime.bottomMargin = Theme.dp(16, density);
        timeRow.setLayoutParams(lpTime);
        rightPane.addView(timeRow);

        // Transport Controls Platter
        LinearLayout transportBar = new LinearLayout(context);
        transportBar.setOrientation(LinearLayout.HORIZONTAL);
        transportBar.setGravity(Gravity.CENTER_VERTICAL);

        btnShuffle = createControlButton(context, "🔀", 18f, Theme.dp(44, density));
        btnPrev = createControlButton(context, "⏮", 22f, Theme.dp(48, density));
        btnPlayPause = createControlButton(context, "▶", 28f, Theme.dp(64, density));
        btnNext = createControlButton(context, "⏭", 22f, Theme.dp(48, density));
        btnRepeat = createControlButton(context, "🔁", 18f, Theme.dp(44, density));

        // Special highlight for Play/Pause circle
        btnPlayPause.setBackground(Theme.makePill(0xFFFFFFFF, 0, density));
        btnPlayPause.setTextColor(Color.BLACK);

        btnShuffle.setOnClickListener(v -> {});
        btnPrev.setOnClickListener(v -> HassClient.getInstance().mediaPrev(targetEntityId));
        btnPlayPause.setOnClickListener(v -> HassClient.getInstance().mediaPlayPause(targetEntityId));
        btnNext.setOnClickListener(v -> HassClient.getInstance().mediaNext(targetEntityId));
        btnRepeat.setOnClickListener(v -> {});

        transportBar.addView(btnShuffle);
        transportBar.addView(btnPrev);
        transportBar.addView(btnPlayPause);
        transportBar.addView(btnNext);
        transportBar.addView(btnRepeat);

        LinearLayout.LayoutParams lpTransport = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpTransport.bottomMargin = Theme.dp(16, density);
        transportBar.setLayoutParams(lpTransport);
        rightPane.addView(transportBar);

        // Volume Slider Capsule
        LinearLayout volumeRow = new LinearLayout(context);
        volumeRow.setOrientation(LinearLayout.HORIZONTAL);
        volumeRow.setGravity(Gravity.CENTER_VERTICAL);
        volumeRow.setPadding(Theme.dp(12, density), Theme.dp(6, density), Theme.dp(12, density), Theme.dp(6, density));
        volumeRow.setBackground(Theme.makePill(0x22FFFFFF, 0x1AFFFFFF, density));

        TextView volMin = new TextView(context);
        volMin.setText("🔈");
        volMin.setTextSize(14f);
        volumeRow.addView(volMin);

        volumeBar = new SeekBar(context);
        volumeBar.setMax(100);
        volumeBar.setProgress(50);
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    HassClient.getInstance().setVolume(targetEntityId, progress / 100f);
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        LinearLayout.LayoutParams lpVol = new LinearLayout.LayoutParams(Theme.dp(240, density), LinearLayout.LayoutParams.WRAP_CONTENT);
        lpVol.leftMargin = Theme.dp(8, density);
        lpVol.rightMargin = Theme.dp(8, density);
        volumeBar.setLayoutParams(lpVol);
        volumeRow.addView(volumeBar);

        TextView volMax = new TextView(context);
        volMax.setText("🔊");
        volMax.setTextSize(14f);
        volumeRow.addView(volMax);

        rightPane.addView(volumeRow, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        mainContent.addView(rightPane);

        // Listen for HA events
        HassClient.getInstance().addEntityListener(this);
        updateState();
    }

    private TextView createControlButton(Context context, String symbol, float textSizeSp, int sizeDp) {
        TextView btn = new TextView(context);
        btn.setText(symbol);
        btn.setTextSize(textSizeSp);
        btn.setTextColor(Theme.TEXT_WHITE);
        btn.setGravity(Gravity.CENTER);
        btn.setBackground(Theme.makePill(0x22FFFFFF, Theme.GLASS_BORDER, density));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sizeDp, sizeDp);
        lp.rightMargin = Theme.dp(12, density);
        btn.setLayoutParams(lp);

        btn.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.92f).scaleY(0.92f).setDuration(80).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                    break;
            }
            return false;
        });

        return btn;
    }

    private void toggleTargetEntity() {
        if ("media_player.marius_zimmer".equalsIgnoreCase(targetEntityId)) {
            targetEntityId = "media_player.marius_zimmer_tv";
            airplayRoutePill.setText(" AirPlay • Apple TV (Marius Zimmer)");
        } else {
            targetEntityId = "media_player.marius_zimmer";
            airplayRoutePill.setText(" AirPlay • Marius Zimmer (HomePod mini)");
        }
        updateState();
    }

    @Override
    public void onEntityUpdated(String entityId, HassClient.EntityState state) {
        if (targetEntityId.equalsIgnoreCase(entityId)) {
            post(this::updateState);
        }
    }

    private void updateState() {
        HassClient.EntityState s = HassClient.getInstance().getState(targetEntityId);
        if (s != null) {
            isPlaying = "playing".equalsIgnoreCase(s.state);
            btnPlayPause.setText(isPlaying ? "⏸" : "▶");

            String title = s.getMediaTitle();
            String artist = s.getMediaArtist();
            trackTitleView.setText(title.isEmpty() ? "Apple Music" : title);
            artistView.setText(artist.isEmpty() ? "Marius Zimmer" : artist);

            double vol = s.getVolumeLevel();
            volumeBar.setProgress((int) Math.round(vol * 100));

            String pic = s.getEntityPicture();
            if (!pic.isEmpty()) {
                ImageLoader.getInstance().load(pic, Theme.dp(240, density), Theme.dp(240, density), bitmap -> {
                    albumArtView.setImageBitmap(bitmap);
                    ambientGlow.setCoverBitmap(bitmap);
                });
            }
        }
    }

    // Ambient background drawing with dynamic blur/radial glow
    private static class AmbientGlowView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int dominantColor = 0xFF8A1E35;

        public AmbientGlowView(Context context) {
            super(context);
        }

        public void setCoverBitmap(Bitmap bmp) {
            if (bmp != null && !bmp.isRecycled()) {
                // Sample center pixel for dominant glow
                dominantColor = bmp.getPixel(bmp.getWidth() / 2, bmp.getHeight() / 2);
                invalidate();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();

            canvas.drawColor(0xFF000000);

            // Left Radial Glow behind album cover
            RadialGradient glow = new RadialGradient(
                    w * 0.28f, h * 0.5f, w * 0.45f,
                    new int[]{0x66000000 | (dominantColor & 0x00FFFFFF), 0x22000000 | (dominantColor & 0x00FFFFFF), 0x00000000},
                    null, Shader.TileMode.CLAMP);
            paint.setShader(glow);
            canvas.drawRect(0, 0, w, h, paint);
        }
    }
}
