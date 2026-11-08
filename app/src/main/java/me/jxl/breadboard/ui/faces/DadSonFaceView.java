package me.jxl.breadboard.ui.faces;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;

import me.jxl.breadboard.ui.Theme;

public class DadSonFaceView extends WatchFaceBaseView {

    public interface OnRecognizeTriggerListener {
        void onTriggerRecognize();
    }

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cloudPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint datePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private OnRecognizeTriggerListener triggerListener;

    public DadSonFaceView(Context context) {
        super(context);
        init();
    }

    public void setOnRecognizeTriggerListener(OnRecognizeTriggerListener listener) {
        this.triggerListener = listener;
    }

    @Override
    public String getFaceName() {
        return "Dad & Son";
    }

    private void init() {
        setWillNotDraw(false);
        setClickable(true);

        cloudPaint.setColor(Color.WHITE);
        cloudPaint.setAlpha(80);

        Typeface rounded = Theme.getRoundedTypeface(getContext());
        timePaint.setTypeface(rounded != null ? rounded : Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        timePaint.setColor(Color.WHITE);
        timePaint.setAlpha(150);
        timePaint.setTextAlign(Paint.Align.CENTER);

        dadPaint.setColor(0xFF2E6F40); // Sage olive sweater
        sonPaint.setColor(0xFFB45309); // Warm terracotta shirt

        datePaint.setColor(Color.WHITE);
        datePaint.setTextSize(Theme.dp(16, density));
        datePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        datePaint.setTextAlign(Paint.Align.LEFT);

        setOnClickListener(v -> {
            if (triggerListener != null) {
                triggerListener.onTriggerRecognize();
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        // 1. Blue Sunny Sky Background
        LinearGradient skyGradient = new LinearGradient(
                0, 0, 0, h,
                new int[]{0xFF38BDF8, 0xFF7DD3FC, 0xFFBAE6FD},
                new float[]{0f, 0.6f, 1f},
                Shader.TileMode.CLAMP
        );
        bgPaint.setShader(skyGradient);
        canvas.drawRect(0, 0, w, h, bgPaint);

        // 2. Fluffy Cloud Shapes
        canvas.drawCircle(w * 0.22f, h * 0.28f, h * 0.22f, cloudPaint);
        canvas.drawCircle(w * 0.35f, h * 0.22f, h * 0.26f, cloudPaint);
        canvas.drawCircle(w * 0.78f, h * 0.32f, h * 0.24f, cloudPaint);
        canvas.drawCircle(w * 0.88f, h * 0.26f, h * 0.20f, cloudPaint);

        // 3. Giant Translucent Frosted Glass "10:09" Numerals (Full Height, Condensed)
        float textSize = h * 0.84f;
        timePaint.setTextSize(textSize);

        canvas.save();
        // Scale horizontally to give condensed Apple typographic look
        canvas.scale(0.75f, 1.15f, w / 2f, h / 2f);
        canvas.drawText("10:09", (w / 2f) / 0.75f, (h * 0.78f) / 1.15f, timePaint);
        canvas.restore();

        // 4. Dad & Son Foreground Silhouette
        float centerX = w * 0.50f;
        float bottomY = h;

        // Dad's shoulders & body (Olive sweater)
        RectF dadShoulders = new RectF(centerX - w * 0.20f, bottomY - h * 0.32f, centerX + w * 0.20f, bottomY + h * 0.20f);
        canvas.drawRoundRect(dadShoulders, Theme.dp(40, density), Theme.dp(40, density), dadPaint);

        // Dad's neck and head
        Paint skinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        skinPaint.setColor(0xFFFBBF24);
        canvas.drawCircle(centerX, bottomY - h * 0.36f, h * 0.11f, skinPaint);

        // Dad's hair
        Paint hairPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hairPaint.setColor(0xFF78350F);
        RectF dadHair = new RectF(centerX - h * 0.11f, bottomY - h * 0.47f, centerX + h * 0.11f, bottomY - h * 0.35f);
        canvas.drawRoundRect(dadHair, Theme.dp(20, density), Theme.dp(20, density), hairPaint);

        // Son riding on shoulders
        // Son's body (Terracotta)
        RectF sonBody = new RectF(centerX - w * 0.11f, bottomY - h * 0.62f, centerX + w * 0.11f, bottomY - h * 0.38f);
        canvas.drawRoundRect(sonBody, Theme.dp(30, density), Theme.dp(30, density), sonPaint);

        // Son's head
        canvas.drawCircle(centerX, bottomY - h * 0.68f, h * 0.085f, skinPaint);

        // Son's blond/brown curly hair
        Paint sonHair = new Paint(Paint.ANTI_ALIAS_FLAG);
        sonHair.setColor(0xFFD97706);
        canvas.drawCircle(centerX - h * 0.04f, bottomY - h * 0.74f, h * 0.05f, sonHair);
        canvas.drawCircle(centerX + h * 0.04f, bottomY - h * 0.74f, h * 0.05f, sonHair);
        canvas.drawCircle(centerX, bottomY - h * 0.76f, h * 0.055f, sonHair);

        // 5. Date badge in bottom left
        canvas.drawText("MITTWOCH, 1. APRIL", Theme.dp(32, density), h - Theme.dp(24, density), datePaint);

        // Tap hint badge in bottom right
        Paint hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hintPaint.setColor(Color.WHITE);
        hintPaint.setAlpha(180);
        hintPaint.setTextSize(Theme.dp(12, density));
        hintPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("Tippen zur Erkennung ›", w - Theme.dp(32, density), h - Theme.dp(24, density), hintPaint);
    }
}
