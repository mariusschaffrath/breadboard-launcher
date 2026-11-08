package me.jxl.breadboard.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import me.jxl.breadboard.models.HomeViewFixtures;

public class RecognizingView extends View {

    public interface OnRecognizedListener {
        void onRecognized();
    }

    private final float density;
    private final Paint bgPaint = new Paint();
    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subtextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint avatarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float rotationAngle = 0f;
    private boolean isMariusLocked = false;
    private float mariusScale = 1.0f;
    private ValueAnimator rotationAnim;

    private OnRecognizedListener recognizedListener;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public RecognizingView(Context context) {
        super(context);
        this.density = getResources().getDisplayMetrics().density;
        init();
    }

    public void setOnRecognizedListener(OnRecognizedListener listener) {
        this.recognizedListener = listener;
    }

    private void init() {
        bgPaint.setColor(0xD9000000); // 85% Black overlay

        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(Theme.dp(5, density));
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        Typeface bold = Theme.getRoundedTypeface(getContext());
        textPaint.setTypeface(bold != null ? bold : Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(Theme.dp(24, density));

        subtextPaint.setColor(0xFF9CA3AF);
        subtextPaint.setTextAlign(Paint.Align.CENTER);
        subtextPaint.setTextSize(Theme.dp(14, density));

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(Theme.dp(2, density));

        // Start Arc Rotation Animator
        rotationAnim = ValueAnimator.ofFloat(0f, 360f);
        rotationAnim.setDuration(1400);
        rotationAnim.setRepeatCount(ValueAnimator.INFINITE);
        rotationAnim.setInterpolator(new android.view.animation.LinearInterpolator());
        rotationAnim.addUpdateListener(animation -> {
            rotationAngle = (float) animation.getAnimatedValue();
            invalidate();
        });
    }

    public void startRecognitionFlow() {
        isMariusLocked = false;
        mariusScale = 1.0f;
        if (rotationAnim != null && !rotationAnim.isRunning()) {
            rotationAnim.start();
        }

        // Lock onto Marius after 1.2s
        handler.postDelayed(() -> {
            isMariusLocked = true;
            mariusScale = 1.25f;
            invalidate();

            // Crossfade to Home after 0.6s
            handler.postDelayed(() -> {
                if (recognizedListener != null) {
                    recognizedListener.onRecognized();
                }
            }, 600);
        }, 1200);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startRecognitionFlow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (rotationAnim != null) rotationAnim.cancel();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        // Dimmed backdrop
        canvas.drawRect(0, 0, w, h, bgPaint);

        // 1. Top Row: 6 Memoji Avatars (64dp circles)
        float avatarDiameter = Theme.dp(60, density);
        float spacing = Theme.dp(18, density);
        float totalWidth = (6 * avatarDiameter) + (5 * spacing);
        float startX = (w - totalWidth) / 2f + (avatarDiameter / 2f);
        float avatarY = Theme.dp(56, density);

        for (int i = 0; i < HomeViewFixtures.MEMOJIS.size(); i++) {
            HomeViewFixtures.MemojiProfile profile = HomeViewFixtures.MEMOJIS.get(i);
            float cx = startX + i * (avatarDiameter + spacing);
            boolean isMarius = profile.name.equals("Marius");

            canvas.save();
            if (isMarius && isMariusLocked) {
                canvas.scale(mariusScale, mariusScale, cx, avatarY);
            }

            int alpha = (isMariusLocked && !isMarius) ? 70 : 255;

            // Gradient Circle
            LinearGradient grad = new LinearGradient(
                    cx - avatarDiameter / 2f, avatarY - avatarDiameter / 2f,
                    cx + avatarDiameter / 2f, avatarY + avatarDiameter / 2f,
                    profile.gradientColors, null, Shader.TileMode.CLAMP
            );
            avatarPaint.setShader(grad);
            avatarPaint.setAlpha(alpha);
            canvas.drawCircle(cx, avatarY, avatarDiameter / 2f, avatarPaint);

            // Ring
            if (isMarius && isMariusLocked) {
                ringPaint.setColor(0xFF34D399);
                ringPaint.setStrokeWidth(Theme.dp(3, density));
            } else {
                ringPaint.setColor(Color.WHITE);
                ringPaint.setAlpha((int) (alpha * 0.3f));
                ringPaint.setStrokeWidth(Theme.dp(1.5f, density));
            }
            canvas.drawCircle(cx, avatarY, avatarDiameter / 2f, ringPaint);

            // Initial Letter / Icon inside
            Paint letterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            letterPaint.setColor(Color.WHITE);
            letterPaint.setAlpha(alpha);
            letterPaint.setTextSize(Theme.dp(20, density));
            letterPaint.setTextAlign(Paint.Align.CENTER);
            letterPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText(profile.name.substring(0, 1), cx, avatarY + Theme.dp(7, density), letterPaint);

            // Profile Name underneath
            Paint namePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            namePaint.setColor(isMarius && isMariusLocked ? 0xFF34D399 : Color.WHITE);
            namePaint.setAlpha(alpha);
            namePaint.setTextSize(Theme.dp(12, density));
            namePaint.setTextAlign(Paint.Align.CENTER);
            namePaint.setTypeface(Typeface.create(Typeface.DEFAULT, isMarius ? Typeface.BOLD : Typeface.NORMAL));
            canvas.drawText(profile.name, cx, avatarY + avatarDiameter / 2f + Theme.dp(16, density), namePaint);

            canvas.restore();
        }

        // 2. Center Rotating Scan Ring & Large Memoji
        float centerCx = w / 2f;
        float centerCy = h * 0.58f;
        float centerRadius = Theme.dp(56, density);

        // Center Green Marius Glow Base
        LinearGradient mariusGrad = new LinearGradient(
                centerCx - centerRadius, centerCy - centerRadius,
                centerCx + centerRadius, centerCy + centerRadius,
                new int[]{0xFF34D399, 0xFF059669}, null, Shader.TileMode.CLAMP
        );
        Paint centerMemojiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerMemojiPaint.setShader(mariusGrad);
        canvas.drawCircle(centerCx, centerCy, centerRadius, centerMemojiPaint);

        // Center Initial 'M'
        Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(Theme.dp(44, density));
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("M", centerCx, centerCy + Theme.dp(15, density), mPaint);

        // Rotating Gradient Arc
        canvas.save();
        canvas.rotate(rotationAngle, centerCx, centerCy);
        float arcRadius = centerRadius + Theme.dp(16, density);
        RectF arcRect = new RectF(centerCx - arcRadius, centerCy - arcRadius, centerCx + arcRadius, centerCy + arcRadius);

        SweepGradient sweep = new SweepGradient(
                centerCx, centerCy,
                new int[]{0xFF34D399, 0xFF06B6D4, 0x0034D399},
                new float[]{0f, 0.7f, 1f}
        );
        arcPaint.setShader(sweep);
        canvas.drawArc(arcRect, 0, 270, false, arcPaint);
        canvas.restore();

        // 3. Status Text
        if (isMariusLocked) {
            textPaint.setColor(0xFF34D399);
            canvas.drawText("Willkommen, Marius!", centerCx, centerCy + arcRadius + Theme.dp(40, density), textPaint);
            canvas.drawText("HomeView entsperrt", centerCx, centerCy + arcRadius + Theme.dp(62, density), subtextPaint);
        } else {
            textPaint.setColor(Color.WHITE);
            canvas.drawText("Erkennung...", centerCx, centerCy + arcRadius + Theme.dp(40, density), textPaint);
            canvas.drawText("Bitte in den Bildschirm schauen.", centerCx, centerCy + arcRadius + Theme.dp(62, density), subtextPaint);
        }
    }
}
