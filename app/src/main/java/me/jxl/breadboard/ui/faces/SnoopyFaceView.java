package me.jxl.breadboard.ui.faces;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.MotionEvent;

import me.jxl.breadboard.ui.Theme;

public class SnoopyFaceView extends WatchFaceBaseView {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private boolean isNightMode = false;

    public SnoopyFaceView(Context context) {
        super(context);
    }

    @Override
    public String getFaceName() {
        return "Snoopy & Peanuts";
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            // Toggle between Day and Night variant
            isNightMode = !isNightMode;
            invalidate();
            return true;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();

        float cardSize = Math.min(w * 0.88f, h * 0.94f);
        float left = (w - cardSize) / 2f;
        float top = (h - cardSize) / 2f;
        float right = left + cardSize;
        float bottom = top + cardSize;

        // Background
        rect.set(left, top, right, bottom);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(isNightMode ? Theme.WF_SNOOPY_BG_NIGHT : Theme.WF_SNOOPY_BG_DAY);
        float cornerRadius = 38f * density;
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);

        // Specular stroke
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f * density);
        paint.setColor(isNightMode ? 0x33FFFFFF : 0x22000000);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
        paint.setStyle(Paint.Style.FILL);

        // 1. Day of Week Header: "WEDNESDAY" / "MITTWOCH" in sketchy comic style
        paint.setTypeface(Theme.getDisplayBold(getContext()));
        paint.setTextSize(cardSize * 0.055f);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(isNightMode ? Color.WHITE : Theme.TEXT_BLACK);
        canvas.drawText(dayOfWeekFormat.format(calendar.getTime()).toUpperCase(), rect.centerX(), top + (cardSize * 0.16f), paint);

        // 2. Giant Date Numeral (e.g. "1")
        paint.setTypeface(Theme.getRoundedBold(getContext()));
        paint.setTextSize(cardSize * 0.44f);
        float numeralX = rect.centerX() - (cardSize * 0.08f);
        float numeralY = top + (cardSize * 0.62f);
        canvas.drawText(dayNumFormat.format(calendar.getTime()), numeralX, numeralY, paint);

        // 3. Characters: Snoopy and Woodstock
        if (!isNightMode) {
            // Day Mode: Snoopy leaning against the numeral, Woodstock on a bag
            drawDaySnoopy(canvas, rect.centerX() + (cardSize * 0.06f), top + (cardSize * 0.42f), cardSize * 0.22f);
            drawWoodstock(canvas, rect.centerX() - (cardSize * 0.22f), top + (cardSize * 0.38f), cardSize * 0.10f);
        } else {
            // Night Mode: Snoopy with Baseball Glove and Pink Bubblegum bubble!
            drawBubblegumSnoopy(canvas, rect.centerX() + (cardSize * 0.08f), top + (cardSize * 0.52f), cardSize * 0.24f);
        }

        // Live Digital Time Pill at bottom
        paint.setTypeface(Theme.getRoundedBold(getContext()));
        paint.setTextSize(cardSize * 0.05f);
        paint.setColor(isNightMode ? 0xB3FFFFFF : 0x99000000);
        canvas.drawText(timeFormat.format(calendar.getTime()), rect.centerX(), bottom - (cardSize * 0.08f), paint);
    }

    private void drawDaySnoopy(Canvas canvas, float x, float y, float size) {
        // Snoopy Head
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawOval(new RectF(x, y, x + size * 0.7f, y + size * 0.55f), paint);

        // Outline
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f * density);
        canvas.drawOval(new RectF(x, y, x + size * 0.7f, y + size * 0.55f), paint);

        // Black Nose
        paint.setStyle(Paint.Style.FILL);
        canvas.drawOval(new RectF(x + size * 0.55f, y + size * 0.22f, x + size * 0.74f, y + size * 0.38f), paint);

        // Black Ear drooping
        canvas.drawOval(new RectF(x + size * 0.05f, y + size * 0.12f, x + size * 0.28f, y + size * 0.60f), paint);

        // Sleeping Eye curve
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.5f * density);
        canvas.drawArc(new RectF(x + size * 0.32f, y + size * 0.18f, x + size * 0.46f, y + size * 0.32f), 180, 180, false, paint);

        // Snoopy Body
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(new RectF(x + size * 0.15f, y + size * 0.45f, x + size * 0.55f, y + size * 0.95f), 12f * density, 12f * density, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(new RectF(x + size * 0.15f, y + size * 0.45f, x + size * 0.55f, y + size * 0.95f), 12f * density, 12f * density, paint);

        // Red Collar
        paint.setColor(Theme.ACCENT_RED);
        paint.setStrokeWidth(4f * density);
        canvas.drawLine(x + size * 0.20f, y + size * 0.48f, x + size * 0.45f, y + size * 0.48f, paint);
    }

    private void drawBubblegumSnoopy(Canvas canvas, float x, float y, float size) {
        // Baseball Glove
        paint.setColor(0xFF8B4513);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawOval(new RectF(x - size * 0.2f, y + size * 0.4f, x + size * 0.3f, y + size * 0.8f), paint);

        // Snoopy Head
        paint.setColor(Color.WHITE);
        canvas.drawOval(new RectF(x, y, x + size * 0.65f, y + size * 0.5f), paint);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f * density);
        canvas.drawOval(new RectF(x, y, x + size * 0.65f, y + size * 0.5f), paint);

        // Black Ear
        paint.setStyle(Paint.Style.FILL);
        canvas.drawOval(new RectF(x + size * 0.05f, y + size * 0.12f, x + size * 0.25f, y + size * 0.55f), paint);

        // GIANT PINK BUBBLEGUM BUBBLE!
        paint.setColor(0xFFFF69B4);
        paint.setStyle(Paint.Style.FILL);
        float bubbleCx = x + size * 0.62f;
        float bubbleCy = y + size * 0.25f;
        float bubbleR = size * 0.38f;
        canvas.drawCircle(bubbleCx, bubbleCy, bubbleR, paint);

        // Bubble Specular Shine
        paint.setColor(0x88FFFFFF);
        canvas.drawCircle(bubbleCx - (bubbleR * 0.35f), bubbleCy - (bubbleR * 0.35f), bubbleR * 0.22f, paint);

        // Woodstock sitting nearby
        drawWoodstock(canvas, x + size * 0.8f, y + size * 0.55f, size * 0.35f);
    }

    private void drawWoodstock(Canvas canvas, float x, float y, float size) {
        // Yellow Bird Body & Tuft
        paint.setColor(0xFFFFD700);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawOval(new RectF(x, y, x + size * 0.7f, y + size * 0.6f), paint);

        // Beak
        paint.setColor(0xFFFF8C00);
        canvas.drawOval(new RectF(x + size * 0.55f, y + size * 0.25f, x + size * 0.85f, y + size * 0.45f), paint);

        // Eye
        paint.setColor(Color.BLACK);
        canvas.drawCircle(x + size * 0.45f, y + size * 0.25f, 2f * density, paint);

        // Sticks (legs)
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f * density);
        canvas.drawLine(x + size * 0.3f, y + size * 0.6f, x + size * 0.3f, y + size * 0.85f, paint);
        canvas.drawLine(x + size * 0.5f, y + size * 0.6f, x + size * 0.5f, y + size * 0.85f, paint);
    }
}
