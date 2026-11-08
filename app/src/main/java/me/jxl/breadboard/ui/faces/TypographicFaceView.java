package me.jxl.breadboard.ui.faces;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.Calendar;

import me.jxl.breadboard.ui.Theme;

public class TypographicFaceView extends WatchFaceBaseView {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    public TypographicFaceView(Context context) {
        super(context);
    }

    @Override
    public String getFaceName() {
        return "Typographic Sage";
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

        // Sage Green Dial
        rect.set(left, top, right, bottom);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Theme.WF_TYPOGRAPHIC_BG);
        float cornerRadius = 38f * density;
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);

        // Specular stroke
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f * density);
        paint.setColor(0x33000000);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
        paint.setStyle(Paint.Style.FILL);

        float cx = rect.centerX();
        float cy = rect.centerY();

        // Oversized Numerals 12, 3, 6, 9
        paint.setColor(Color.BLACK);
        paint.setTypeface(Theme.getDisplayBold(getContext()));
        paint.setTextSize(cardSize * 0.32f);
        paint.setTextAlign(Paint.Align.CENTER);

        // 12
        canvas.drawText("12", cx, top + (cardSize * 0.30f), paint);
        // 3
        canvas.drawText("3", right - (cardSize * 0.16f), cy + (cardSize * 0.11f), paint);
        // 6
        canvas.drawText("6", cx, bottom - (cardSize * 0.08f), paint);
        // 9
        canvas.drawText("9", left + (cardSize * 0.16f), cy + (cardSize * 0.11f), paint);

        // Date Indicator at Bottom Left: "WED 1"
        paint.setTypeface(Theme.getRoundedBold(getContext()));
        paint.setTextSize(cardSize * 0.045f);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(dateFormat.format(calendar.getTime()).toUpperCase(), left + (cardSize * 0.12f), bottom - (cardSize * 0.12f), paint);

        // Analog Hands
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        float dialRadius = cardSize * 0.44f;

        // Hour Hand (White bold)
        double hAngle = Math.toRadians((hour + minute / 60.0) * 30);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(6.5f * density);
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(cx, cy,
                (float) (cx + dialRadius * 0.52f * Math.sin(hAngle)),
                (float) (cy - dialRadius * 0.52f * Math.cos(hAngle)), paint);

        // Minute Hand (White long)
        double mAngle = Math.toRadians((minute + second / 60.0) * 6);
        paint.setStrokeWidth(4.5f * density);
        canvas.drawLine(cx, cy,
                (float) (cx + dialRadius * 0.78f * Math.sin(mAngle)),
                (float) (cy - dialRadius * 0.78f * Math.cos(mAngle)), paint);

        // Second Hand (White thin)
        double sAngle = Math.toRadians(second * 6);
        paint.setColor(0xEEFFFFFF);
        paint.setStrokeWidth(2f * density);
        canvas.drawLine(cx, cy,
                (float) (cx + dialRadius * 0.85f * Math.sin(sAngle)),
                (float) (cy - dialRadius * 0.85f * Math.cos(sAngle)), paint);

        // Center Pin
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        canvas.drawCircle(cx, cy, 5.5f * density, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(cx, cy, 2.5f * density, paint);
    }
}
