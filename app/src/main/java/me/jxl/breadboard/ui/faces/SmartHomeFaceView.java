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

public class SmartHomeFaceView extends WatchFaceBaseView {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final RectF tileLights = new RectF();
    private final RectF tileCover = new RectF();
    private final RectF tileScene = new RectF();

    public SmartHomeFaceView(Context context) {
        super(context);
    }

    @Override
    public String getFaceName() {
        return "Smart Home";
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX();
            float y = event.getY();
            if (tileLights.contains(x, y)) {
                HassClient.getInstance().toggleLight("light.marius_zimmer");
                invalidate();
                return true;
            } else if (tileCover.contains(x, y)) {
                HassClient.getInstance().toggleCover("cover.rolladenmarius");
                invalidate();
                return true;
            } else if (tileScene.contains(x, y)) {
                HassClient.getInstance().triggerScene("scene.marius_zimmer_nightlight");
                invalidate();
                return true;
            }
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

        // Indigo Glass Dial
        rect.set(left, top, right, bottom);
        paint.setColor(Theme.WF_SMART_HOME_BG);
        paint.setStyle(Paint.Style.FILL);
        float cornerRadius = 38f * density;
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);

        // Specular border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f * density);
        paint.setColor(0x44FFFFFF);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
        paint.setStyle(Paint.Style.FILL);

        float pad = cardSize * 0.05f;

        // Top Section: Left = Climate, Right = Analog Clock
        float topSectionH = cardSize * 0.52f;

        // 1. TOP-LEFT: Climate (22°C or 68°F)
        HassClient.EntityState tempState = HassClient.getInstance().getState("sensor.0x00158d000548bad0_device_temperature");
        String tempVal = (tempState != null) ? String.format("%.0f°", tempState.getTemperature()) : "22°";

        paint.setColor(Color.WHITE);
        paint.setTypeface(Theme.getRoundedBold(getContext()));
        paint.setTextSize(cardSize * 0.20f);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(tempVal, left + pad, top + pad + (cardSize * 0.18f), paint);

        paint.setTypeface(Theme.getDisplayBold(getContext()));
        paint.setTextSize(cardSize * 0.045f);
        paint.setColor(0xB3FFFFFF);
        canvas.drawText("Marius Zimmer", left + pad, top + pad + (cardSize * 0.25f), paint);

        paint.setTextSize(cardSize * 0.038f);
        paint.setColor(Theme.ACCENT_SAGE);
        canvas.drawText("● Raumklima Optimal", left + pad, top + pad + (cardSize * 0.31f), paint);

        // 2. TOP-RIGHT: Compact Analog Clock Dial
        float dialCenterX = right - pad - (cardSize * 0.22f);
        float dialCenterY = top + pad + (cardSize * 0.20f);
        float dialRadius = cardSize * 0.18f;

        // Dial base
        paint.setColor(0x33000000);
        canvas.drawCircle(dialCenterX, dialCenterY, dialRadius, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0x33FFFFFF);
        paint.setStrokeWidth(1.5f * density);
        canvas.drawCircle(dialCenterX, dialCenterY, dialRadius, paint);
        paint.setStyle(Paint.Style.FILL);

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        // Hands
        double hAngle = Math.toRadians((hour + minute / 60.0) * 30);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3.5f * density);
        canvas.drawLine(dialCenterX, dialCenterY,
                (float) (dialCenterX + dialRadius * 0.55f * Math.sin(hAngle)),
                (float) (dialCenterY - dialRadius * 0.55f * Math.cos(hAngle)), paint);

        double mAngle = Math.toRadians((minute + second / 60.0) * 6);
        paint.setStrokeWidth(2.5f * density);
        canvas.drawLine(dialCenterX, dialCenterY,
                (float) (dialCenterX + dialRadius * 0.78f * Math.sin(mAngle)),
                (float) (dialCenterY - dialRadius * 0.78f * Math.cos(mAngle)), paint);

        paint.setColor(Theme.ACCENT_AMBER);
        paint.setStrokeWidth(1.5f * density);
        double sAngle = Math.toRadians(second * 6);
        canvas.drawLine(dialCenterX, dialCenterY,
                (float) (dialCenterX + dialRadius * 0.85f * Math.sin(sAngle)),
                (float) (dialCenterY - dialRadius * 0.85f * Math.cos(sAngle)), paint);

        paint.setColor(Color.WHITE);
        canvas.drawCircle(dialCenterX, dialCenterY, 3.5f * density, paint);

        // Label below dial
        paint.setColor(0xB3FFFFFF);
        paint.setTextSize(cardSize * 0.035f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Gute Nacht Szene", dialCenterX, dialCenterY + dialRadius + (cardSize * 0.06f), paint);

        // 3. BOTTOM ROW: 3 Smart Home Control Tiles
        float tileY = top + topSectionH + (cardSize * 0.04f);
        float tileH = cardSize * 0.32f;
        float totalTileW = cardSize - (pad * 2);
        float singleTileW = (totalTileW - (cardSize * 0.04f)) / 3f;

        // Tile 1: Lights
        tileLights.set(left + pad, tileY, left + pad + singleTileW, tileY + tileH);
        HassClient.EntityState lightState = HassClient.getInstance().getState("light.marius_zimmer");
        boolean lightOn = (lightState != null && lightState.isOn());
        drawControlPill(canvas, tileLights, "💡", "Licht", lightOn ? "An" : "Aus", lightOn, Theme.ACCENT_AMBER);

        // Tile 2: Cover (Rolladen)
        tileCover.set(left + pad + singleTileW + (cardSize * 0.02f), tileY,
                left + pad + singleTileW * 2 + (cardSize * 0.02f), tileY + tileH);
        HassClient.EntityState coverState = HassClient.getInstance().getState("cover.rolladenmarius");
        boolean coverOpen = (coverState != null && "open".equalsIgnoreCase(coverState.state));
        drawControlPill(canvas, tileCover, "🪟", "Rolladen", coverOpen ? "Offen" : "Zu", coverOpen, Theme.ACCENT_BLUE);

        // Tile 3: Scene (Nachtlicht)
        tileScene.set(left + pad + (singleTileW + (cardSize * 0.02f)) * 2, tileY,
                left + pad + singleTileW * 3 + (cardSize * 0.04f), tileY + tileH);
        drawControlPill(canvas, tileScene, "🌙", "Nachtlicht", "Szene", false, Theme.ACCENT_PURPLE);
    }

    private void drawControlPill(Canvas canvas, RectF r, String icon, String title, String sub, boolean active, int accentColor) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(active ? (0x55000000 | (accentColor & 0x00FFFFFF)) : 0x22FFFFFF);
        canvas.drawRoundRect(r, 18f * density, 18f * density, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.2f * density);
        paint.setColor(active ? accentColor : 0x22FFFFFF);
        canvas.drawRoundRect(r, 18f * density, 18f * density, paint);
        paint.setStyle(Paint.Style.FILL);

        // Icon
        paint.setColor(Color.WHITE);
        paint.setTextSize(r.height() * 0.28f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(icon, r.centerX(), r.top + (r.height() * 0.38f), paint);

        // Title
        paint.setTypeface(Theme.getDisplayBold(getContext()));
        paint.setTextSize(r.height() * 0.17f);
        canvas.drawText(title, r.centerX(), r.top + (r.height() * 0.65f), paint);

        // Subtitle
        paint.setTypeface(Theme.getRoundedBold(getContext()));
        paint.setTextSize(r.height() * 0.14f);
        paint.setColor(active ? accentColor : 0x80FFFFFF);
        canvas.drawText(sub, r.centerX(), r.top + (r.height() * 0.84f), paint);
    }
}
