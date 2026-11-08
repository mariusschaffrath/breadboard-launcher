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
import android.view.View;

import me.jxl.breadboard.ui.Theme;

/**
 * Miniature Apple HomeView Face Preview with:
 * - Thin light-grey outer rim (1.5dp, #E5E5EA)
 * - Thick black bezel (~10dp, #000000)
 * - Inner screen rendering live miniature faces
 * - Aspect ratio ~1.05:1 (175dp x 160dp)
 */
public class FaceThumbnailView extends View {

    public enum FaceKind {
        DAD_SON,
        MODULAR_QUAD,
        SMART_HOME_INDIGO,
        SNOOPY,
        CALENDAR_MATRIX,
        TYPOGRAPHIC_SAGE,
        FLOW_JELLY,
        STANDBY
    }

    private final float density;
    private final FaceKind kind;

    private final Paint rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bezelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path clipPath = new Path();

    public FaceThumbnailView(Context context, FaceKind kind) {
        super(context);
        this.density = getResources().getDisplayMetrics().density;
        this.kind = kind;
        init();
    }

    public FaceKind getKind() {
        return kind;
    }

    private void init() {
        setClickable(true);

        rimPaint.setStyle(Paint.Style.STROKE);
        rimPaint.setColor(0xFFE5E5EA);
        rimPaint.setStrokeWidth(Theme.dp(1.5f, density));

        bezelPaint.setStyle(Paint.Style.FILL);
        bezelPaint.setColor(0xFF000000);

        textPaint.setTextAlign(Paint.Align.CENTER);
        subTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        float outerCorner = Theme.dp(36, density);
        float bezelInset = Theme.dp(1.5f, density);
        float innerInset = Theme.dp(11, density);
        float innerCorner = Theme.dp(24, density);

        // 1. Thin Light-Grey Outer Rim
        RectF outerRect = new RectF(1, 1, w - 1, h - 1);
        canvas.drawRoundRect(outerRect, outerCorner, outerCorner, rimPaint);

        // 2. Thick Black Bezel
        RectF bezelRect = new RectF(bezelInset, bezelInset, w - bezelInset, h - bezelInset);
        canvas.drawRoundRect(bezelRect, outerCorner - 2, outerCorner - 2, bezelPaint);

        // 3. Inner Screen Area (Clipped rounded rect)
        RectF innerRect = new RectF(innerInset, innerInset, w - innerInset, h - innerInset);
        clipPath.reset();
        clipPath.addRoundRect(innerRect, innerCorner, innerCorner, Path.Direction.CW);

        canvas.save();
        canvas.clipPath(clipPath);

        drawMiniFace(canvas, innerRect);

        canvas.restore();
    }

    private void drawMiniFace(Canvas canvas, RectF r) {
        float cx = r.centerX();
        float cy = r.centerY();
        float rw = r.width();
        float rh = r.height();

        switch (kind) {
            case DAD_SON: {
                // Sky gradient
                LinearGradient grad = new LinearGradient(
                        r.left, r.top, r.left, r.bottom,
                        new int[]{0xFF38BDF8, 0xFF818CF8}, null, Shader.TileMode.CLAMP
                );
                innerPaint.setShader(grad);
                canvas.drawRect(r, innerPaint);

                // Giant 10:09 translucent
                textPaint.setShader(null);
                textPaint.setColor(0x66FFFFFF);
                textPaint.setTextSize(Theme.dp(44, density));
                textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                canvas.drawText("10:09", cx, cy + Theme.dp(14, density), textPaint);

                // Silhouette of Dad & Son at bottom
                innerPaint.setShader(null);
                innerPaint.setColor(0xCC000000);
                canvas.drawCircle(cx, r.bottom - Theme.dp(28, density), Theme.dp(18, density), innerPaint);
                canvas.drawCircle(cx, r.bottom - Theme.dp(52, density), Theme.dp(12, density), innerPaint);
                break;
            }

            case MODULAR_QUAD: {
                innerPaint.setShader(null);
                innerPaint.setColor(0xFFBDD2D6);
                canvas.drawRect(r, innerPaint);

                textPaint.setShader(null);
                textPaint.setColor(Color.BLACK);
                textPaint.setTextSize(Theme.dp(10, density));
                textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                canvas.drawText("Wed Apr", r.left + Theme.dp(24, density), r.top + Theme.dp(20, density), textPaint);

                textPaint.setTextSize(Theme.dp(28, density));
                canvas.drawText("1", r.left + Theme.dp(20, density), r.top + Theme.dp(46, density), textPaint);

                // Mini analog clock ring
                innerPaint.setStyle(Paint.Style.STROKE);
                innerPaint.setColor(Color.BLACK);
                innerPaint.setStrokeWidth(Theme.dp(2, density));
                canvas.drawCircle(r.right - Theme.dp(26, density), r.top + Theme.dp(26, density), Theme.dp(15, density), innerPaint);
                innerPaint.setStyle(Paint.Style.FILL);

                // Weather 14°
                textPaint.setTextSize(Theme.dp(20, density));
                canvas.drawText("14°", r.right - Theme.dp(26, density), r.bottom - Theme.dp(16, density), textPaint);
                break;
            }

            case SMART_HOME_INDIGO: {
                innerPaint.setShader(null);
                innerPaint.setColor(0xFF23305E);
                canvas.drawRect(r, innerPaint);

                textPaint.setShader(null);
                textPaint.setColor(Color.WHITE);
                textPaint.setTextSize(Theme.dp(28, density));
                textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                canvas.drawText("21°", r.left + Theme.dp(28, density), r.top + Theme.dp(36, density), textPaint);

                // Mini tiles at bottom
                innerPaint.setColor(0x40FFFFFF);
                float tileW = (rw - Theme.dp(24, density)) / 3f;
                for (int i = 0; i < 3; i++) {
                    float left = r.left + Theme.dp(8, density) + i * (tileW + Theme.dp(4, density));
                    RectF tr = new RectF(left, r.bottom - Theme.dp(28, density), left + tileW, r.bottom - Theme.dp(8, density));
                    if (i == 1) innerPaint.setColor(0xFF38BDF8);
                    else innerPaint.setColor(0x40FFFFFF);
                    canvas.drawRoundRect(tr, Theme.dp(4, density), Theme.dp(4, density), innerPaint);
                }
                break;
            }

            case SNOOPY: {
                innerPaint.setShader(null);
                innerPaint.setColor(0xFFEBEAE6);
                canvas.drawRect(r, innerPaint);

                textPaint.setShader(null);
                textPaint.setColor(Color.BLACK);
                textPaint.setTextSize(Theme.dp(9, density));
                textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                canvas.drawText("WEDNESDAY", cx, r.top + Theme.dp(22, density), textPaint);

                textPaint.setTextSize(Theme.dp(56, density));
                canvas.drawText("1", cx, cy + Theme.dp(18, density), textPaint);

                subTextPaint.setColor(0x99000000);
                subTextPaint.setTextSize(Theme.dp(11, density));
                canvas.drawText("10:09", cx, r.bottom - Theme.dp(14, density), subTextPaint);
                break;
            }

            case CALENDAR_MATRIX: {
                innerPaint.setShader(null);
                innerPaint.setColor(0xFFF2F2F6);
                canvas.drawRect(r, innerPaint);

                textPaint.setShader(null);
                textPaint.setColor(0xFFDC2626);
                textPaint.setTextSize(Theme.dp(10, density));
                textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                canvas.drawText("April 2026", r.left + Theme.dp(28, density), r.top + Theme.dp(20, density), textPaint);

                // Mini calendar grid dots
                innerPaint.setColor(0xFF3B82F6);
                float dw = rw / 8f;
                for (int col = 1; col <= 7; col++) {
                    canvas.drawCircle(r.left + col * dw, r.top + Theme.dp(36, density), Theme.dp(2, density), innerPaint);
                }

                // Red highlight day 1
                innerPaint.setColor(0xFFDC2626);
                RectF day1 = new RectF(cx - Theme.dp(12, density), cy - Theme.dp(6, density), cx + Theme.dp(12, density), cy + Theme.dp(20, density));
                canvas.drawRoundRect(day1, Theme.dp(6, density), Theme.dp(6, density), innerPaint);
                break;
            }

            case TYPOGRAPHIC_SAGE: {
                innerPaint.setShader(null);
                innerPaint.setColor(0xFF5B8A72);
                canvas.drawRect(r, innerPaint);

                textPaint.setShader(null);
                textPaint.setColor(Color.BLACK);
                textPaint.setTextSize(Theme.dp(32, density));
                textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

                canvas.drawText("12", cx, r.top + Theme.dp(36, density), textPaint);
                canvas.drawText("6", cx, r.bottom - Theme.dp(12, density), textPaint);
                canvas.drawText("9", r.left + Theme.dp(24, density), cy + Theme.dp(11, density), textPaint);
                canvas.drawText("3", r.right - Theme.dp(24, density), cy + Theme.dp(11, density), textPaint);
                break;
            }

            case FLOW_JELLY: {
                LinearGradient grad = new LinearGradient(
                        r.left, r.top, r.right, r.bottom,
                        new int[]{0xFFE08BBB, 0xFF9967C9, 0xFF4D88E3}, null, Shader.TileMode.CLAMP
                );
                innerPaint.setShader(grad);
                canvas.drawRect(r, innerPaint);

                textPaint.setShader(null);
                textPaint.setColor(Color.WHITE);
                textPaint.setTextSize(Theme.dp(36, density));
                textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                canvas.drawText("10", cx, cy - Theme.dp(4, density), textPaint);
                canvas.drawText("09", cx, cy + Theme.dp(28, density), textPaint);
                break;
            }

            case STANDBY:
            default: {
                LinearGradient grad = new LinearGradient(
                        r.left, r.top, r.right, r.bottom,
                        new int[]{0xFFFBBF24, 0xFFEA580C}, null, Shader.TileMode.CLAMP
                );
                innerPaint.setShader(grad);
                canvas.drawRect(r, innerPaint);

                textPaint.setShader(null);
                textPaint.setColor(Color.WHITE);
                textPaint.setTextSize(Theme.dp(26, density));
                textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                canvas.drawText("10:09", cx, cy + Theme.dp(9, density), textPaint);
                break;
            }
        }
    }
}
