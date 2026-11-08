package me.jxl.breadboard.ui.faces;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import me.jxl.breadboard.net.HassClient;
import me.jxl.breadboard.ui.Theme;

public class StandByFaceView extends WatchFaceBaseView {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private boolean isNightVision = false;

    public StandByFaceView(Context context) {
        super(context);
    }

    @Override
    public String getFaceName() {
        return "StandBy Nightstand";
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            isNightVision = !isNightVision;
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

        // StandBy occupies the full display width and height
        canvas.drawColor(0xFF000000);

        int primaryColor = isNightVision ? Theme.ACCENT_RED : Color.WHITE;
        int secondaryColor = isNightVision ? 0x88FF453A : 0xB3FFFFFF;
        int glassBg = isNightVision ? 0x22FF453A : Theme.GLASS_CARD;
        int glassBorder = isNightVision ? 0x44FF453A : Theme.GLASS_BORDER;

        float pad = 40f * density;
        float colW = (w - (pad * 3)) / 2f;
        float cardH = h - (pad * 2);

        // LEFT COLUMN: Giant Digital Clock Card
        rect.set(pad, pad, pad + colW, pad + cardH);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(glassBg);
        canvas.drawRoundRect(rect, 28f * density, 28f * density, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f * density);
        paint.setColor(glassBorder);
        canvas.drawRoundRect(rect, 28f * density, 28f * density, paint);
        paint.setStyle(Paint.Style.FILL);

        // Giant Time
        paint.setColor(primaryColor);
        paint.setTypeface(Theme.getDisplayBold(getContext()));
        paint.setTextSize(cardH * 0.40f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(timeFormat.format(calendar.getTime()), rect.centerX(), rect.centerY() + (cardH * 0.12f), paint);

        // Secondary Seconds / AM/PM indicator below
        paint.setTypeface(Theme.getRoundedBold(getContext()));
        paint.setTextSize(cardH * 0.08f);
        paint.setColor(secondaryColor);
        canvas.drawText(isNightVision ? "● NACHTMODUS AKTIV" : "STANDBY UHR", rect.centerX(), rect.bottom - (cardH * 0.15f), paint);

        // RIGHT COLUMN: Date, Room & Next Event
        rect.set(pad * 2 + colW, pad, pad * 2 + colW * 2, pad + cardH);
        paint.setColor(glassBg);
        canvas.drawRoundRect(rect, 28f * density, 28f * density, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(glassBorder);
        canvas.drawRoundRect(rect, 28f * density, 28f * density, paint);
        paint.setStyle(Paint.Style.FILL);

        float rightCx = rect.centerX();

        // Date Header
        paint.setColor(isNightVision ? Theme.ACCENT_RED : Theme.ACCENT_AMBER);
        paint.setTypeface(Theme.getDisplayBold(getContext()));
        paint.setTextSize(cardH * 0.10f);
        canvas.drawText(dayOfWeekFormat.format(calendar.getTime()).toUpperCase(), rightCx, rect.top + (cardH * 0.22f), paint);

        paint.setColor(primaryColor);
        paint.setTypeface(Theme.getRoundedBold(getContext()));
        paint.setTextSize(cardH * 0.18f);
        canvas.drawText(dateFormat.format(calendar.getTime()), rightCx, rect.top + (cardH * 0.44f), paint);

        // Room Temperature
        HassClient.EntityState temp = HassClient.getInstance().getState("sensor.0x00158d000548bad0_device_temperature");
        String tempText = (temp != null) ? String.format("%.1f°C", temp.getTemperature()) : "22.0°C";

        paint.setColor(secondaryColor);
        paint.setTextSize(cardH * 0.085f);
        canvas.drawText("Marius Zimmer • " + tempText, rightCx, rect.top + (cardH * 0.64f), paint);

        // Light & Standby Status Pill
        HassClient.EntityState light = HassClient.getInstance().getState("light.marius_zimmer");
        boolean lightsOn = (light != null && light.isOn());
        paint.setColor(lightsOn ? Theme.ACCENT_AMBER : secondaryColor);
        paint.setTextSize(cardH * 0.075f);
        canvas.drawText(lightsOn ? "💡 Lichter im Zimmer aktiv" : "🌙 Schlafraum im Ruhezustand", rightCx, rect.bottom - (cardH * 0.15f), paint);
    }
}
