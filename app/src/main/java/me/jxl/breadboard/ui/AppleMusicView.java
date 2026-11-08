package me.jxl.breadboard.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

import me.jxl.breadboard.models.HomeViewFixtures;
import me.jxl.breadboard.net.HassClient;
import me.jxl.breadboard.net.ImageLoader;

public class AppleMusicView extends FrameLayout implements HassClient.EntityListener {

    public interface OnCloseListener {
        void onClose();
    }

    private final float density;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private OnCloseListener closeListener;

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
    private TextView btnLyrics;

    private SeekBar volumeBar;

    private boolean isPlaying = true;
    private boolean showLyrics = false;
    private int currentProgressSec = 123;
    private int totalDurationSec = 244;

    private LinearLayout lyricsContainer;
    private ScrollView lyricsScrollView;
    private int activeLyricIndex = 0;
    private final Runnable lyricsAdvancer = new Runnable() {
        @Override
        public void run() {
            if (showLyrics) {
                activeLyricIndex = (activeLyricIndex + 1) % HomeViewFixtures.SONG_LYRICS.size();
                updateLyricsHighlight();
                handler.postDelayed(this, 2500);
            }
        }
    };

    public AppleMusicView(Context context) {
        super(context);
        this.density = getResources().getDisplayMetrics().density;
        init(context);
    }

    public void setOnCloseListener(OnCloseListener listener) {
        this.closeListener = listener;
    }

    private void init(Context context) {
        setBackgroundColor(0xFF000000);

        // 1. Ambient blurred glow background
        ambientGlow = new AmbientGlowView(context);
        addView(ambientGlow, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // 2. Main Content Container
        LinearLayout mainContent = new LinearLayout(context);
        mainContent.setOrientation(LinearLayout.VERTICAL);
        mainContent.setPadding(Theme.dp(36, density), Theme.dp(16, density), Theme.dp(36, density), Theme.dp(16, density));
        addView(mainContent, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // 3. Top Bar: Close "✕" on Left, AirPlay Pill in Center, Lyrics Toggle on Right
        LinearLayout topBar = new LinearLayout(context);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        mainContent.addView(topBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Theme.dp(44, density)));

        // Close Button
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

        // Center: AirPlay Pill
        airplayRoutePill = new TextView(context);
        airplayRoutePill.setText(" AirPlay • Marius Zimmer HomePod");
        airplayRoutePill.setTypeface(Theme.getRoundedBold(context));
        airplayRoutePill.setTextSize(13f);
        airplayRoutePill.setTextColor(Theme.ACCENT_PINK);
        airplayRoutePill.setPadding(Theme.dp(14, density), Theme.dp(6, density), Theme.dp(14, density), Theme.dp(6, density));
        airplayRoutePill.setBackground(Theme.makePill(0x33FA2D48, 0x55FA2D48, density));
        airplayRoutePill.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lpAirplay = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lpAirplay.leftMargin = Theme.dp(16, density);
        lpAirplay.rightMargin = Theme.dp(16, density);
        airplayRoutePill.setLayoutParams(lpAirplay);
        airplayRoutePill.setOnClickListener(v -> toggleTargetEntity());
        topBar.addView(airplayRoutePill);

        // Lyrics Toggle Button
        btnLyrics = new TextView(context);
        btnLyrics.setText("❝");
        btnLyrics.setTextColor(Color.WHITE);
        btnLyrics.setTextSize(20);
        btnLyrics.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        btnLyrics.setGravity(Gravity.CENTER);
        btnLyrics.setBackground(Theme.makeGlass(0x33FFFFFF, 0x4DFFFFFF, btnSize / 2f, density));
        LinearLayout.LayoutParams lpLyr = new LinearLayout.LayoutParams(btnSize, btnSize);
        btnLyrics.setLayoutParams(lpLyr);
        btnLyrics.setOnClickListener(v -> toggleLyricsMode());
        topBar.addView(btnLyrics);

        // 4. Middle Pane: Split Artwork + Controls OR Lyrics
        FrameLayout middleContainer = new FrameLayout(context);
        LinearLayout.LayoutParams lpMiddle = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        lpMiddle.topMargin = Theme.dp(12, density);
        middleContainer.setLayoutParams(lpMiddle);
        mainContent.addView(middleContainer);

        // 4a. Player Layout (Album Art Left, Controls Right)
        LinearLayout playerRow = new LinearLayout(context);
        playerRow.setOrientation(LinearLayout.HORIZONTAL);
        playerRow.setGravity(Gravity.CENTER_VERTICAL);
        middleContainer.addView(playerRow, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Album Art
        FrameLayout artCard = new FrameLayout(context);
        int artSize = Theme.dp(250, density);
        LinearLayout.LayoutParams lpArt = new LinearLayout.LayoutParams(artSize, artSize);
        artCard.setLayoutParams(lpArt);
        artCard.setBackground(Theme.makeGlass(0x22FFFFFF, Theme.GLASS_SELECTED_BORDER, 24f, density));
        artCard.setPadding(Theme.dp(2, density), Theme.dp(2, density), Theme.dp(2, density), Theme.dp(2, density));

        albumArtView = new ImageView(context);
        albumArtView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        albumArtView.setImageBitmap(ImageLoader.createMusicPlaceholder(artSize, artSize));
        artCard.addView(albumArtView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        playerRow.addView(artCard);

        // Controls Right
        LinearLayout rightPane = new LinearLayout(context);
        rightPane.setOrientation(LinearLayout.VERTICAL);
        rightPane.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lpRight = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lpRight.leftMargin = Theme.dp(36, density);
        rightPane.setLayoutParams(lpRight);

        // Title & Artist
        trackTitleView = new TextView(context);
        trackTitleView.setText(HomeViewFixtures.SONG_TITLE);
        trackTitleView.setTypeface(Theme.getDisplayBold(context));
        trackTitleView.setTextSize(26f);
        trackTitleView.setTextColor(Theme.TEXT_WHITE);
        trackTitleView.setSingleLine(true);
        rightPane.addView(trackTitleView);

        artistView = new TextView(context);
        artistView.setText(HomeViewFixtures.SONG_ARTIST + " • " + HomeViewFixtures.SONG_ALBUM);
        artistView.setTypeface(Theme.getRoundedBold(context));
        artistView.setTextSize(17f);
        artistView.setTextColor(Theme.TEXT_SECONDARY);
        artistView.setSingleLine(true);
        LinearLayout.LayoutParams lpArtist = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpArtist.topMargin = Theme.dp(4, density);
        lpArtist.bottomMargin = Theme.dp(16, density);
        artistView.setLayoutParams(lpArtist);
        rightPane.addView(artistView);

        // Progress Bar
        progressBar = new SeekBar(context);
        progressBar.setMax(totalDurationSec);
        progressBar.setProgress(currentProgressSec);
        rightPane.addView(progressBar);

        // Time labels
        LinearLayout timeRow = new LinearLayout(context);
        timeRow.setOrientation(LinearLayout.HORIZONTAL);
        timeElapsedView = new TextView(context);
        timeElapsedView.setText("2:03");
        timeElapsedView.setTypeface(Theme.getRoundedBold(context));
        timeElapsedView.setTextSize(12f);
        timeElapsedView.setTextColor(Theme.TEXT_MUTED);

        timeRemainingView = new TextView(context);
        timeRemainingView.setText("-0:47");
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

        // Transport Controls
        LinearLayout transportBar = new LinearLayout(context);
        transportBar.setOrientation(LinearLayout.HORIZONTAL);
        transportBar.setGravity(Gravity.CENTER_VERTICAL);

        btnShuffle = createControlButton(context, "🔀", 18f, Theme.dp(44, density));
        btnPrev = createControlButton(context, "⏮", 22f, Theme.dp(48, density));
        btnPlayPause = createControlButton(context, "⏸", 28f, Theme.dp(64, density));
        btnNext = createControlButton(context, "⏭", 22f, Theme.dp(48, density));
        btnRepeat = createControlButton(context, "🔁", 18f, Theme.dp(44, density));

        btnPlayPause.setBackground(Theme.makePill(0xFFFFFFFF, 0, density));
        btnPlayPause.setTextColor(Color.BLACK);

        btnPrev.setOnClickListener(v -> HassClient.getInstance().mediaPrev(targetEntityId));
        btnPlayPause.setOnClickListener(v -> {
            isPlaying = !isPlaying;
            btnPlayPause.setText(isPlaying ? "⏸" : "▶");
            HassClient.getInstance().mediaPlayPause(targetEntityId);
        });
        btnNext.setOnClickListener(v -> HassClient.getInstance().mediaNext(targetEntityId));

        transportBar.addView(btnShuffle);
        transportBar.addView(btnPrev);
        transportBar.addView(btnPlayPause);
        transportBar.addView(btnNext);
        transportBar.addView(btnRepeat);

        LinearLayout.LayoutParams lpTransport = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpTransport.bottomMargin = Theme.dp(14, density);
        transportBar.setLayoutParams(lpTransport);
        rightPane.addView(transportBar);

        // Volume Bar
        LinearLayout volumeRow = new LinearLayout(context);
        volumeRow.setOrientation(LinearLayout.HORIZONTAL);
        volumeRow.setGravity(Gravity.CENTER_VERTICAL);
        volumeRow.setPadding(Theme.dp(12, density), Theme.dp(4, density), Theme.dp(12, density), Theme.dp(4, density));
        volumeRow.setBackground(Theme.makePill(0x22FFFFFF, 0x1AFFFFFF, density));

        TextView volMin = new TextView(context);
        volMin.setText("🔈");
        volMin.setTextSize(14f);
        volumeRow.addView(volMin);

        volumeBar = new SeekBar(context);
        volumeBar.setMax(100);
        volumeBar.setProgress(65);
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
        playerRow.addView(rightPane);

        // 4b. Synced Lyrics Overlay View
        lyricsScrollView = new ScrollView(context);
        lyricsScrollView.setVisibility(View.GONE);
        lyricsScrollView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        lyricsContainer = new LinearLayout(context);
        lyricsContainer.setOrientation(LinearLayout.VERTICAL);
        lyricsContainer.setPadding(Theme.dp(32, density), Theme.dp(24, density), Theme.dp(32, density), Theme.dp(24, density));
        lyricsScrollView.addView(lyricsContainer);

        buildLyricsLines(context);
        middleContainer.addView(lyricsScrollView);

        HassClient.getInstance().addEntityListener(this);
        updateState();
    }

    private void buildLyricsLines(Context context) {
        lyricsContainer.removeAllViews();
        List<String> lyrics = HomeViewFixtures.SONG_LYRICS;
        for (int i = 0; i < lyrics.size(); i++) {
            TextView tvLine = new TextView(context);
            tvLine.setText(lyrics.get(i));
            tvLine.setTextSize(26f);
            tvLine.setTypeface(Theme.getBoldTypeface(context));
            tvLine.setPadding(0, Theme.dp(8, density), 0, Theme.dp(8, density));
            lyricsContainer.addView(tvLine);
        }
        updateLyricsHighlight();
    }

    private void updateLyricsHighlight() {
        for (int i = 0; i < lyricsContainer.getChildCount(); i++) {
            View child = lyricsContainer.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                if (i == activeLyricIndex) {
                    tv.setTextColor(Color.WHITE);
                    tv.setScaleX(1.05f);
                    tv.setScaleY(1.05f);
                } else if (i < activeLyricIndex) {
                    tv.setTextColor(0x55FFFFFF);
                    tv.setScaleX(1.0f);
                    tv.setScaleY(1.0f);
                } else {
                    tv.setTextColor(0x88FFFFFF);
                    tv.setScaleX(1.0f);
                    tv.setScaleY(1.0f);
                }
            }
        }
        if (lyricsScrollView != null && lyricsContainer.getChildCount() > activeLyricIndex) {
            View target = lyricsContainer.getChildAt(activeLyricIndex);
            lyricsScrollView.smoothScrollTo(0, Math.max(0, target.getTop() - Theme.dp(80, density)));
        }
    }

    private void toggleLyricsMode() {
        showLyrics = !showLyrics;
        if (showLyrics) {
            btnLyrics.setBackground(Theme.makePill(0x55FA2D48, 0xFFFA2D48, density));
            lyricsScrollView.setVisibility(View.VISIBLE);
            lyricsScrollView.setAlpha(0f);
            lyricsScrollView.animate().alpha(1f).setDuration(250).start();
            handler.post(lyricsAdvancer);
        } else {
            btnLyrics.setBackground(Theme.makeGlass(0x33FFFFFF, 0x4DFFFFFF, Theme.dp(40, density) / 2f, density));
            lyricsScrollView.animate().alpha(0f).setDuration(200).withEndAction(() -> {
                lyricsScrollView.setVisibility(View.GONE);
            }).start();
            handler.removeCallbacks(lyricsAdvancer);
        }
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
            airplayRoutePill.setText(" AirPlay • Marius Zimmer HomePod");
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
            trackTitleView.setText(title.isEmpty() ? HomeViewFixtures.SONG_TITLE : title);
            artistView.setText(artist.isEmpty() ? (HomeViewFixtures.SONG_ARTIST + " • " + HomeViewFixtures.SONG_ALBUM) : artist);

            double vol = s.getVolumeLevel();
            volumeBar.setProgress((int) Math.round(vol * 100));

            String pic = s.getEntityPicture();
            if (!pic.isEmpty()) {
                ImageLoader.getInstance().load(pic, Theme.dp(250, density), Theme.dp(250, density), bitmap -> {
                    albumArtView.setImageBitmap(bitmap);
                    ambientGlow.setCoverBitmap(bitmap);
                });
            }
        }
    }

    private static class AmbientGlowView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int dominantColor = 0xFF8A1E35;

        public AmbientGlowView(Context context) {
            super(context);
        }

        public void setCoverBitmap(Bitmap bmp) {
            if (bmp != null && !bmp.isRecycled()) {
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

            RadialGradient glow = new RadialGradient(
                    w * 0.28f, h * 0.5f, w * 0.5f,
                    new int[]{0x66000000 | (dominantColor & 0x00FFFFFF), 0x22000000 | (dominantColor & 0x00FFFFFF), 0x00000000},
                    null, Shader.TileMode.CLAMP);
            paint.setShader(glow);
            canvas.drawRect(0, 0, w, h, paint);
        }
    }
}
