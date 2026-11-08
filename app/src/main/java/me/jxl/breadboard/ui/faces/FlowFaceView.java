package me.jxl.breadboard.ui.faces;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import java.text.SimpleDateFormat;
import java.util.Locale;

import me.jxl.breadboard.ui.Theme;

public class FlowFaceView extends WatchFaceBaseView {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final SimpleDateFormat hourFormat = new SimpleDateFormat("HH", Locale.GERMANY);
    private final SimpleDateFormat minFormat = new SimpleDateFormat("mm", Locale.GERMANY);

    public FlowFaceView(Context context) {
        super(context);
    }

    @Override
    public String getFaceName() {
        return "Flow Jelly";
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

        // Base Card
        rect.set(left, top, right, bottom);
        float cornerRadius = 38f * density;

        // Fluid Multi-Stop Linear Gradient Background
        Shader bgShader = new LinearGradient(left, top, right, bottom,
                new int[]{0xFFE08BBB, 0xFF9967C9, 0xFF4D88E3, 0xFFE08BBB},
                null, Shader.TileMode.CLAMP);
        paint.setShader(bgShader);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
        paint.setShader(null);

        // Specular stroke
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f * density);
        paint.setColor(0x44FFFFFF);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
        paint.setStyle(Paint.Style.FILL);

        float cx = rect.centerX();
        float cy = rect.centerY();

        // Fluid Jelly Droplets Behind Typography
        paint.setColor(0x44FFFFFF);
        canvas.drawCircle(cx - (cardSize * 0.18f), cy - (cardSize * 0.14f), cardSize * 0.22f, paint);
        paint.setColor(0x33FF69B4);
        canvas.drawCircle(cx + (cardSize * 0.16f), cy + (cardSize * 0.14f), cardSize * 0.26f, paint);

        // Jelly Digits (Stacked: Hours on top, Minutes on bottom)
        String hh = hourFormat.format(calendar.getTime());
        String mm = minFormat.format(calendar.getTime());

        paint.setTypeface(Theme.getRoundedBold(getContext()));
        paint.setTextSize(cardSize * 0.38f);
        paint.setTextAlign(Paint.Align.CENTER);

        // Jelly Glow Layer
        paint.setColor(0x66FFFFFF);
        canvas.drawText(hh, cx + 2f * density, cy - (cardSize * 0.04f) + 2f * density, paint);
        canvas.drawText(mm, cx + 2f * density, cy + (cardSize * 0.30f) + 2f * density, paint);

        // Pure Glossy White Foreground
        paint.setColor(Color.WHITE);
        canvas.drawText(hh, cx, cy - (cardSize * 0.04f), paint);
        canvas.drawText(mm, cx, cy + (cardSize * 0.30f), paint);

        // Date pill at bottom
        paint.setTypeface(Theme.getDisplayBold(getContext()));
        paint.setTextSize(cardSize * 0.042f);
        paint.setColor(0xE6FFFFFF);
        canvas.drawText(dateFormat.format(calendar.getTime()), cx, bottom - (cardSize * 0.08f), paint);
    }
}
