package me.jxl.breadboard.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import me.jxl.breadboard.net.HassClient;
import me.jxl.breadboard.net.ImageLoader;

public class HomeDashboardView extends LinearLayout implements HassClient.EntityListener {

    public interface OnOpenMusicListener {
        void onOpenMusic();
    }

    private final float density;
    private TextView roomTitle;
    private TextView roomSubtitle;

    // Mini Now Playing Banner
    private LinearLayout musicBanner;
    private ImageView miniAlbumArt;
    private TextView miniTrackTitle;
    private TextView miniArtist;
    private TextView miniPlayPauseBtn;

    private OnOpenMusicListener musicListener;

    public HomeDashboardView(Context context) {
        super(context);
        this.density = getResources().getDisplayMetrics().density;
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        setPadding(Theme.dp(24, density), Theme.dp(12, density), Theme.dp(24, density), Theme.dp(16, density));

        // Room Header Row
        LinearLayout headerRow = new LinearLayout(context);
        headerRow.setOrientation(HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        roomTitle = new TextView(context);
        roomTitle.setText("Marius Zimmer");
        roomTitle.setTypeface(Theme.getDisplayBold(context));
        roomTitle.setTextSize(26f);
        roomTitle.setTextColor(Theme.TEXT_WHITE);
        headerRow.addView(roomTitle);

        roomSubtitle = new TextView(context);
        roomSubtitle.setText("22.0°C • Alle Geräte bereit");
        roomSubtitle.setTypeface(Theme.getRoundedBold(context));
        roomSubtitle.setTextSize(13f);
        roomSubtitle.setTextColor(Theme.TEXT_SECONDARY);
        roomSubtitle.setPadding(Theme.dp(12, density), Theme.dp(4, density), Theme.dp(12, density), Theme.dp(4, density));
        roomSubtitle.setBackground(Theme.makePill(0x22FFFFFF, 0x1AFFFFFF, density));
        LayoutParams lpSub = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpSub.leftMargin = Theme.dp(16, density);
        headerRow.addView(roomSubtitle, lpSub);

        addView(headerRow);

        // Content Area: 2 Columns (Tiles Grid on Left, Now Playing & Scenes on Right)
        LinearLayout contentRow = new LinearLayout(context);
        contentRow.setOrientation(HORIZONTAL);
        LayoutParams lpContent = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f);
        lpContent.topMargin = Theme.dp(16, density);
        contentRow.setLayoutParams(lpContent);

        // Left: 3x2 Grid of PebbleTiles
        GridLayout grid = new GridLayout(context);
        grid.setColumnCount(3);
        grid.setRowCount(2);
        grid.setOrientation(GridLayout.HORIZONTAL);

        int tileWidth = Theme.dp(210, density);
        int tileHeight = Theme.dp(115, density);
        int margin = Theme.dp(10, density);

        // 1. Alle Lichter
        addGridTile(grid, new PebbleTileView(context, "light.marius_zimmer", "Alle Lichter", "💡", PebbleTileView.TileType.LIGHT),
                tileWidth, tileHeight, margin);

        // 2. Pixar Lampe
        addGridTile(grid, new PebbleTileView(context, "light.marius_zimmer_pixar", "Pixar Lampe", "🛋️", PebbleTileView.TileType.LIGHT),
                tileWidth, tileHeight, margin);

        // 3. Schreibtisch
        addGridTile(grid, new PebbleTileView(context, "light.schreibtisch", "Schreibtisch", "💻", PebbleTileView.TileType.LIGHT),
                tileWidth, tileHeight, margin);

        // 4. Dachfenster
        addGridTile(grid, new PebbleTileView(context, "cover.rolladenmarius", "Dachfenster", "🪟", PebbleTileView.TileType.COVER),
                tileWidth, tileHeight, margin);

        // 5. Kopffenster
        addGridTile(grid, new PebbleTileView(context, "cover.marius", "Kopffenster", "🪟", PebbleTileView.TileType.COVER),
                tileWidth, tileHeight, margin);

        // 6. Nachtlicht Szene
        addGridTile(grid, new PebbleTileView(context, "scene.marius_zimmer_nightlight", "Nachtlicht", "🌙", PebbleTileView.TileType.SCENE),
                tileWidth, tileHeight, margin);

        contentRow.addView(grid, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        // Right: Apple Music Now Playing Card & Environment
        LinearLayout rightColumn = new LinearLayout(context);
        rightColumn.setOrientation(VERTICAL);
        LayoutParams lpRight = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1f);
        lpRight.leftMargin = Theme.dp(14, density);
        rightColumn.setLayoutParams(lpRight);

        // Music Banner Card
        musicBanner = new LinearLayout(context);
        musicBanner.setOrientation(VERTICAL);
        musicBanner.setPadding(Theme.dp(16, density), Theme.dp(14, density), Theme.dp(16, density), Theme.dp(14, density));
        musicBanner.setBackground(Theme.makeGlass(Theme.GLASS_CARD, Theme.GLASS_BORDER, 20f, density));
        musicBanner.setOnClickListener(v -> {
            if (musicListener != null) musicListener.onOpenMusic();
        });

        LinearLayout bannerTop = new LinearLayout(context);
        bannerTop.setOrientation(HORIZONTAL);
        bannerTop.setGravity(Gravity.CENTER_VERTICAL);

        miniAlbumArt = new ImageView(context);
        miniAlbumArt.setScaleType(ImageView.ScaleType.CENTER_CROP);
        miniAlbumArt.setBackground(Theme.makeGlass(0x33FFFFFF, 0, 10f, density));
        miniAlbumArt.setImageBitmap(ImageLoader.createMusicPlaceholder(Theme.dp(56, density), Theme.dp(56, density)));
        bannerTop.addView(miniAlbumArt, new LayoutParams(Theme.dp(56, density), Theme.dp(56, density)));

        LinearLayout songInfo = new LinearLayout(context);
        songInfo.setOrientation(VERTICAL);
        LayoutParams lpSong = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        lpSong.leftMargin = Theme.dp(12, density);
        songInfo.setLayoutParams(lpSong);

        miniTrackTitle = new TextView(context);
        miniTrackTitle.setText(" Music");
        miniTrackTitle.setTypeface(Theme.getDisplayBold(context));
        miniTrackTitle.setTextSize(15f);
        miniTrackTitle.setTextColor(Theme.TEXT_WHITE);
        miniTrackTitle.setSingleLine(true);
        songInfo.addView(miniTrackTitle);

        miniArtist = new TextView(context);
        miniArtist.setText("Tippen zum Öffnen");
        miniArtist.setTypeface(Theme.getRoundedBold(context));
        miniArtist.setTextSize(12f);
        miniArtist.setTextColor(Theme.TEXT_SECONDARY);
        miniArtist.setSingleLine(true);
        songInfo.addView(miniArtist);

        bannerTop.addView(songInfo);

        miniPlayPauseBtn = new TextView(context);
        miniPlayPauseBtn.setText("▶");
        miniPlayPauseBtn.setTextSize(20f);
        miniPlayPauseBtn.setTextColor(Theme.TEXT_WHITE);
        miniPlayPauseBtn.setGravity(Gravity.CENTER);
        miniPlayPauseBtn.setBackground(Theme.makePill(0x33FFFFFF, Theme.GLASS_BORDER, density));
        miniPlayPauseBtn.setOnClickListener(v -> HassClient.getInstance().mediaPlayPause("media_player.marius_zimmer"));
        bannerTop.addView(miniPlayPauseBtn, new LayoutParams(Theme.dp(44, density), Theme.dp(44, density)));

        musicBanner.addView(bannerTop);

        // AirPlay Speaker Pill at bottom of banner
        TextView airplayPill = new TextView(context);
        airplayPill.setText(" AirPlay • Marius Zimmer (HomePod mini)");
        airplayPill.setTypeface(Theme.getRoundedBold(context));
        airplayPill.setTextSize(11f);
        airplayPill.setTextColor(Theme.ACCENT_PINK);
        airplayPill.setPadding(0, Theme.dp(10, density), 0, 0);
        musicBanner.addView(airplayPill);

        rightColumn.addView(musicBanner);

        // Climate Card below Music Banner
        LinearLayout climateCard = new LinearLayout(context);
        climateCard.setOrientation(VERTICAL);
        climateCard.setPadding(Theme.dp(16, density), Theme.dp(12, density), Theme.dp(16, density), Theme.dp(12, density));
        climateCard.setBackground(Theme.makeGlass(Theme.GLASS_CARD, Theme.GLASS_BORDER, 20f, density));
        LayoutParams lpClimate = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lpClimate.topMargin = Theme.dp(10, density);
        climateCard.setLayoutParams(lpClimate);

        TextView climateLabel = new TextView(context);
        climateLabel.setText("KLIMA & RAUMLUFT");
        climateLabel.setTypeface(Theme.getDisplayBold(context));
        climateLabel.setTextSize(11f);
        climateLabel.setTextColor(Theme.TEXT_MUTED);
        climateCard.addView(climateLabel);

        TextView climateVal = new TextView(context);
        climateVal.setText("22.0°C • Luftfeuchte 48%");
        climateVal.setTypeface(Theme.getRoundedBold(context));
        climateVal.setTextSize(14f);
        climateVal.setTextColor(Theme.TEXT_WHITE);
        climateVal.setPadding(0, Theme.dp(4, density), 0, 0);
        climateCard.addView(climateVal);

        rightColumn.addView(climateCard);

        contentRow.addView(rightColumn);
        addView(contentRow);

        HassClient.getInstance().addEntityListener(this);
        updateMusicState();
    }

    private void addGridTile(GridLayout grid, PebbleTileView tile, int width, int height, int margin) {
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = width;
        lp.height = height;
        lp.setMargins(0, 0, margin, margin);
        tile.setLayoutParams(lp);
        grid.addView(tile);
    }

    public void setOnOpenMusicListener(OnOpenMusicListener listener) {
        this.musicListener = listener;
    }

    @Override
    public void onEntityUpdated(String entityId, HassClient.EntityState state) {
        if (entityId.contains("media_player")) {
            post(this::updateMusicState);
        }
    }

    private void updateMusicState() {
        HassClient client = HassClient.getInstance();
        HassClient.EntityState media = client.getState("media_player.marius_zimmer");
        if (media == null) {
            media = client.getState("media_player.marius_zimmer_tv");
        }

        if (media != null && media.isOn()) {
            String title = media.getMediaTitle();
            String artist = media.getMediaArtist();
            miniTrackTitle.setText(title.isEmpty() ? "Apple Music" : title);
            miniArtist.setText(artist.isEmpty() ? "Marius Zimmer" : artist);
            miniPlayPauseBtn.setText("⏸");

            String pic = media.getEntityPicture();
            if (!pic.isEmpty()) {
                ImageLoader.getInstance().loadInto(pic, Theme.dp(56, density), Theme.dp(56, density), miniAlbumArt);
            }
        } else {
            miniTrackTitle.setText(" Music");
            miniArtist.setText("Tippen zum Öffnen");
            miniPlayPauseBtn.setText("▶");
        }
    }
}
