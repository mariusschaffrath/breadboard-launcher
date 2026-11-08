package me.jxl.breadboard.ui.faces;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.Calendar;

import me.jxl.breadboard.ui.Theme;

public class CalendarFaceView extends WatchFaceBaseView {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    public CalendarFaceView(Context context) {
        super(context);
    }

    @Override
    public String getFaceName() {
        return "Calendar Matrix";
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

        // Card Base
        rect.set(left, top, right, bottom);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Theme.WF_CALENDAR_BG);
        float cornerRadius = 38f * density;
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);

        // Thin border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f * density);
        paint.setColor(0x22000000);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
        paint.setStyle(Paint.Style.FILL);

        float pad = cardSize * 0.08f;

        // Header: Month & Year on Left (Red), Digital Time on Right (Red)
        paint.setTypeface(Theme.getDisplayBold(getContext()));
        paint.setTextSize(cardSize * 0.055f);
        paint.setColor(Theme.ACCENT_RED);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(monthYearFormat.format(calendar.getTime()), left + pad, top + pad + (cardSize * 0.06f), paint);

        paint.setTypeface(Theme.getRoundedBold(getContext()));
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(timeFormat.format(calendar.getTime()), right - pad, top + pad + (cardSize * 0.06f), paint);

        // Weekday Labels Row: S M T W T F S
        String[] daysOfWeek = {"S", "M", "D", "M", "D", "F", "S"};
        float gridTop = top + pad + (cardSize * 0.14f);
        float gridW = cardSize - (pad * 2);
        float colW = gridW / 7f;

        paint.setColor(Theme.ACCENT_BLUE);
        paint.setTextSize(cardSize * 0.045f);
        paint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < 7; i++) {
            canvas.drawText(daysOfWeek[i], left + pad + (i * colW) + (colW * 0.5f), gridTop, paint);
        }

        // Calendar Grid computation
        Calendar calCopy = (Calendar) calendar.clone();
        calCopy.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = calCopy.get(Calendar.DAY_OF_WEEK) - 1; // 0 for Sunday
        int maxDays = calCopy.getActualMaximum(Calendar.DAY_OF_MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        float cellStartY = gridTop + (cardSize * 0.08f);
        float rowH = (cardSize * 0.58f) / 6f;

        // Draw dates
        for (int day = 1; day <= maxDays; day++) {
            int slot = firstDayOfWeek + (day - 1);
            int col = slot % 7;
            int row = slot / 7;

            float cx = left + pad + (col * colW) + (colW * 0.5f);
            float cy = cellStartY + (row * rowH);

            if (day == currentDay) {
                // Signature Apple vertical rounded capsule highlight
                RectF highlight = new RectF(cx - (colW * 0.42f), cy - (rowH * 0.70f),
                        cx + (colW * 0.42f), cy + (rowH * 0.35f));
                paint.setColor(Theme.ACCENT_RED);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRoundRect(highlight, colW * 0.4f, colW * 0.4f, paint);

                paint.setColor(Color.WHITE);
                paint.setTypeface(Theme.getDisplayBold(getContext()));
            } else {
                paint.setColor(0xCC000000);
                paint.setTypeface(Theme.getRoundedBold(getContext()));
            }

            paint.setTextSize(cardSize * 0.045f);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(String.valueOf(day), cx, cy, paint);
        }
    }
}
