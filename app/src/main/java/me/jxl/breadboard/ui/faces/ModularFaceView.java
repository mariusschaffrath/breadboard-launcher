package me.jxl.breadboard.ui.faces;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import java.util.Calendar;

import me.jxl.breadboard.net.HassClient;
import me.jxl.breadboard.ui.Theme;

public class ModularFaceView extends WatchFaceBaseView {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    public interface OnMusicWidgetClickListener {
        void onMusicWidgetClicked();
    }
    private OnMusicWidgetClickListener musicClickListener;

    public ModularFaceView(Context context) {
        super(context);
    }

    public void setOnMusicWidgetClickListener(OnMusicWidgetClickListener listener) {
        this.musicClickListener = listener;
    }

    @Override
    public String getFaceName() {
        return "Modular Quad";
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX();
            float y = event.getY();
            int w = getWidth();
            int h = getHeight();
            // Check if tapped on Bottom-Left quadrant (Music widget)
            if (x < w * 0.5f && y > h * 0.5f) {
                if (musicClickListener != null) {
                    musicClickListener.onMusicWidgetClicked();
                    return true;
                }
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();

        // Center a 520x520dp rounded watchface card on screen
        float cardSize = Math.min(w * 0.88f, h * 0.94f);
        float left = (w - cardSize) / 2f;
        float top = (h - cardSize) / 2f;
        float right = left + cardSize;
        float bottom = top + cardSize;

        // Base Watchface Platter: Mint Ice Blue
        rect.set(left, top, right, bottom);
        paint.setColor(Theme.WF_MODULAR_BG);
        paint.setStyle(Paint.Style.FILL);
        float cornerRadius = 38f * density;
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);

        // Thin specular outline
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f * density);
        paint.setColor(0x33000000);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
        paint.setStyle(Paint.Style.FILL);

        float quadW = cardSize * 0.44f;
        float quadH = cardSize * 0.44f;
        float pad = cardSize * 0.04f;

        // 1. TOP-LEFT: Date Widget ("Wed Apr", large bold numeral "1")
        drawDateWidget(canvas, left + pad, top + pad, quadW, quadH);

        // 2. TOP-RIGHT: Analog Clock Dial
        drawAnalogDial(canvas, left + pad * 2 + quadW, top + pad, quadW, quadH);

        // 3. BOTTOM-LEFT: Now Playing Mini Card
        drawMusicWidget(canvas, left + pad, top + pad * 2 + quadH, quadW, quadH);

        // 4. BOTTOM-RIGHT: Weather Widget
        drawWeatherWidget(canvas, left + pad * 2 + quadW, top + pad * 2 + quadH, quadW, quadH);
    }

    private void drawDateWidget(Canvas canvas, float x, float y, float w, float h) {
        paint.setColor(Theme.TEXT_BLACK);
        paint.setTypeface(Theme.getDisplayBold(getContext()));

        // Date text "Wed Apr"
        paint.setTextSize(w * 0.14f);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(dateFormat.format(calendar.getTime()), x + (w * 0.12f), y + (h * 0.32f), paint);

        // Large bold day numeral "1"
        paint.setTypeface(Theme.getRoundedBold(getContext()));
        paint.setTextSize(w * 0.65f);
        canvas.drawText(dayNumFormat.format(calendar.getTime()), x + (w * 0.10f), y + (h * 0.88f), paint);
    }

    private void drawAnalogDial(Canvas canvas, float x, float y, float w, float h) {
        float cx = x + w * 0.5f;
        float cy = y + h * 0.5f;
        float r = Math.min(w, h) * 0.44f;

        // Dial base circle
        paint.setColor(0x18000000);
        canvas.drawCircle(cx, cy, r, paint);

        // Tick marks (12 hours)
        paint.setColor(Theme.TEXT_BLACK);
        paint.setStrokeWidth(2f * density);
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30);
            float innerR = (i % 3 == 0) ? r * 0.74f : r * 0.84f;
            float x1 = (float) (cx + innerR * Math.sin(angle));
            float y1 = (float) (cy - innerR * Math.cos(angle));
            float x2 = (float) (cx + r * 0.92f * Math.sin(angle));
            float y2 = (float) (cy - r * 0.92f * Math.cos(angle));
            canvas.drawLine(x1, y1, x2, y2, paint);
        }

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        // Hour Hand
        double hourAngle = Math.toRadians((hour + minute / 60.0) * 30);
        paint.setStrokeWidth(4.5f * density);
        paint.setColor(Theme.TEXT_BLACK);
        canvas.drawLine(cx, cy,
                (float) (cx + r * 0.52f * Math.sin(hourAngle)),
                (float) (cy - r * 0.52f * Math.cos(hourAngle)), paint);

        // Minute Hand
        double minAngle = Math.toRadians((minute + second / 60.0) * 6);
        paint.setStrokeWidth(3f * density);
        canvas.drawLine(cx, cy,
                (float) (cx + r * 0.76f * Math.sin(minAngle)),
                (float) (cy - r * 0.76f * Math.cos(minAngle)), paint);

        // Second Hand (Orange)
        double secAngle = Math.toRadians(second * 6);
        paint.setColor(Theme.ACCENT_AMBER);
        paint.setStrokeWidth(1.8f * density);
        canvas.drawLine(cx, cy,
                (float) (cx + r * 0.84f * Math.sin(secAngle)),
                (float) (cy - r * 0.84f * Math.cos(secAngle)), paint);

        // Center Pin
        paint.setColor(Theme.TEXT_BLACK);
        canvas.drawCircle(cx, cy, 4f * density, paint);
    }

    private void drawMusicWidget(Canvas canvas, float x, float y, float w, float h) {
        // Subtle background tile
        rect.set(x, y, x + w, y + h);
        paint.setColor(0x14000000);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(rect, 16f * density, 16f * density, paint);

        // Mini Artwork box
        float artSize = h * 0.45f;
        rect.set(x + 12f * density, y + 12f * density, x + 12f * density + artSize, y + 12f * density + artSize);
        paint.setColor(0xFFFA2D48);
        canvas.drawRoundRect(rect, 8f * density, 8f * density, paint);

        // Artwork Music Symbol
        paint.setColor(Color.WHITE);
        paint.setTypeface(Theme.getDisplayBold(getContext()));
        paint.setTextSize(artSize * 0.5f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("♫", rect.centerX(), rect.centerY() + (artSize * 0.18f), paint);

        // Titles
        HassClient.EntityState media = HassClient.getInstance().getState("media_player.marius_zimmer");
        String title = (media != null && !media.getMediaTitle().isEmpty()) ? media.getMediaTitle() : "FEVER DREAM";
        String artist = (media != null && !media.getMediaArtist().isEmpty()) ? media.getMediaArtist() : "Alex Warren";

        paint.setColor(Theme.TEXT_BLACK);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(w * 0.10f);
        canvas.drawText(title, x + 12f * density, y + h * 0.68f, paint);

        paint.setColor(Theme.TEXT_BLACK_SECONDARY);
        paint.setTextSize(w * 0.08f);
        canvas.drawText(artist, x + 12f * density, y + h * 0.82f, paint);

        // Play indicator
        paint.setColor(Theme.TEXT_BLACK);
        paint.setTextSize(w * 0.08f);
        canvas.drawText("▶ Play", x + 12f * density, y + h * 0.95f, paint);
    }

    private void drawWeatherWidget(Canvas canvas, float x, float y, float w, float h) {
        // Location text
        paint.setColor(Theme.TEXT_BLACK_SECONDARY);
        paint.setTypeface(Theme.getDisplayBold(getContext()));
        paint.setTextSize(w * 0.09f);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Marius Zimmer", x + (w * 0.08f), y + (h * 0.24f), paint);

        // Large Temperature (from HA or 22°)
        HassClient.EntityState tempState = HassClient.getInstance().getState("sensor.0x00158d000548bad0_device_temperature");
        String tempStr = (tempState != null) ? String.format("%.0f°", tempState.getTemperature()) : "22°";

        paint.setColor(Theme.TEXT_BLACK);
        paint.setTypeface(Theme.getRoundedBold(getContext()));
        paint.setTextSize(w * 0.36f);
        canvas.drawText(tempStr, x + (w * 0.08f), y + (h * 0.62f), paint);

        // Condition
        paint.setTypeface(Theme.getDisplayBold(getContext()));
        paint.setTextSize(w * 0.085f);
        paint.setColor(Theme.TEXT_BLACK);
        canvas.drawText("Sonnig & Mild", x + (w * 0.08f), y + (h * 0.80f), paint);

        paint.setColor(Theme.TEXT_BLACK_SECONDARY);
        paint.setTextSize(w * 0.075f);
        canvas.drawText("H:24° L:18°", x + (w * 0.08f), y + (h * 0.94f), paint);
    }
}
