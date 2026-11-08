package me.jxl.breadboard.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.jxl.breadboard.models.HomeViewFixtures;

public class SiriView extends FrameLayout {

    public interface OnDismissListener {
        void onDismiss();
    }

    private final float density;
    private OnDismissListener dismissListener;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private FrameLayout promptContainer;
    private TextView tvTranscript;
    private FrameLayout answerCard;

    private final String[] transcriptWords = HomeViewFixtures.SIRI_QUERY.split(" ");
    private int wordsRevealed = 0;

    public SiriView(Context context) {
        super(context);
        this.density = getResources().getDisplayMetrics().density;
        init(context);
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.dismissListener = listener;
    }

    private void init(Context context) {
        // Dark translucent background with pulsing edge glow
        setBackgroundColor(0xCC000000);
        setClickable(true);
        setOnClickListener(v -> dismiss());

        // 1. Initial Prompt Container (Centered Siri Orb + Transcript)
        promptContainer = new FrameLayout(context);
        addView(promptContainer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        LinearLayout promptBox = new LinearLayout(context);
        promptBox.setOrientation(LinearLayout.VERTICAL);
        promptBox.setGravity(Gravity.CENTER);
        LayoutParams lpPrompt = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lpPrompt.gravity = Gravity.CENTER;
        promptContainer.addView(promptBox, lpPrompt);

        // Big Siri Orb
        View bigOrb = createSiriOrb(context, 100);
        promptBox.addView(bigOrb);

        View gap = new View(context);
        promptBox.addView(gap, new LinearLayout.LayoutParams(1, Theme.dp(24, density)));

        tvTranscript = new TextView(context);
        tvTranscript.setTextColor(Color.WHITE);
        tvTranscript.setTextSize(26);
        tvTranscript.setTypeface(Theme.getRoundedTypeface(context));
        tvTranscript.setGravity(Gravity.CENTER);
        promptBox.addView(tvTranscript);

        // 2. White Answer Card Overlay
        answerCard = new FrameLayout(context);
        LayoutParams lpCard = new LayoutParams(Theme.dp(580, density), Theme.dp(440, density));
        lpCard.gravity = Gravity.CENTER;
        answerCard.setLayoutParams(lpCard);

        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.WHITE);
        cardBg.setCornerRadius(Theme.dp(32, density));
        answerCard.setBackground(cardBg);
        answerCard.setPadding(Theme.dp(28, density), Theme.dp(24, density), Theme.dp(28, density), Theme.dp(24, density));
        answerCard.setVisibility(View.GONE);

        LinearLayout cardContent = new LinearLayout(context);
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setGravity(Gravity.CENTER_HORIZONTAL);
        answerCard.addView(cardContent, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Shrunk Siri Orb at top
        View miniOrb = createSiriOrb(context, 54);
        cardContent.addView(miniOrb);

        // Answer Headline
        TextView tvAnswer = new TextView(context);
        tvAnswer.setText(HomeViewFixtures.SIRI_ANSWER);
        tvAnswer.setTextColor(Color.BLACK);
        tvAnswer.setTextSize(18);
        tvAnswer.setTypeface(Theme.getBoldTypeface(context));
        tvAnswer.setGravity(Gravity.CENTER);
        tvAnswer.setPadding(0, Theme.dp(12, density), 0, Theme.dp(14, density));
        cardContent.addView(tvAnswer);

        // Vector Map View
        View mapView = new DattenfeldSiegMapView(context);
        LinearLayout.LayoutParams lpMap = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Theme.dp(150, density));
        cardContent.addView(mapView, lpMap);

        // Map Info Panel
        LinearLayout infoPanel = new LinearLayout(context);
        infoPanel.setOrientation(LinearLayout.HORIZONTAL);
        infoPanel.setGravity(Gravity.CENTER_VERTICAL);
        infoPanel.setPadding(Theme.dp(14, density), Theme.dp(10, density), Theme.dp(14, density), Theme.dp(10, density));
        GradientDrawable infoBg = new GradientDrawable();
        infoBg.setColor(0xFFF2F2F7);
        infoBg.setCornerRadius(Theme.dp(14, density));
        infoPanel.setBackground(infoBg);

        LinearLayout leftInfo = new LinearLayout(context);
        leftInfo.setOrientation(LinearLayout.VERTICAL);
        infoPanel.addView(leftInfo, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView tvDest = new TextView(context);
        tvDest.setText(HomeViewFixtures.SIRI_DESTINATION);
        tvDest.setTextColor(Color.BLACK);
        tvDest.setTextSize(14);
        tvDest.setTypeface(Theme.getBoldTypeface(context));
        leftInfo.addView(tvDest);

        TextView tvDist = new TextView(context);
        tvDist.setText(HomeViewFixtures.SIRI_DISTANCE);
        tvDist.setTextColor(0xFF6B7280);
        tvDist.setTextSize(12);
        leftInfo.addView(tvDist);

        TextView tvRoute = new TextView(context);
        tvRoute.setText(HomeViewFixtures.SIRI_ROUTE);
        tvRoute.setTextColor(0xFF34C759); // Apple Green
        tvRoute.setTextSize(13);
        tvRoute.setTypeface(Theme.getBoldTypeface(context));
        infoPanel.addView(tvRoute);

        LinearLayout.LayoutParams lpInfo = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpInfo.topMargin = Theme.dp(8, density);
        cardContent.addView(infoPanel, lpInfo);

        // Blue 7 Min. Capsule Button
        FrameLayout btnDrive = new FrameLayout(context);
        LinearLayout.LayoutParams lpBtn = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Theme.dp(44, density));
        lpBtn.topMargin = Theme.dp(12, density);
        btnDrive.setLayoutParams(lpBtn);

        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(0xFF1E88F5); // Apple Blue
        btnBg.setCornerRadius(Theme.dp(22, density));
        btnDrive.setBackground(btnBg);

        TextView tvDrive = new TextView(context);
        tvDrive.setText("🚗  " + HomeViewFixtures.SIRI_TRAVEL_TIME);
        tvDrive.setTextColor(Color.WHITE);
        tvDrive.setTextSize(15);
        tvDrive.setTypeface(Theme.getBoldTypeface(context));
        tvDrive.setGravity(Gravity.CENTER);
        btnDrive.addView(tvDrive, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        btnDrive.setOnClickListener(v -> dismiss());
        cardContent.addView(btnDrive);

        addView(answerCard);
    }

    public void startSiriFlow() {
        promptContainer.setVisibility(View.VISIBLE);
        answerCard.setVisibility(View.GONE);
        wordsRevealed = 0;
        tvTranscript.setText("");

        // Reveal words one by one
        for (int i = 0; i < transcriptWords.length; i++) {
            final int count = i + 1;
            handler.postDelayed(() -> {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < count; j++) {
                    sb.append(transcriptWords[j]).append(" ");
                }
                tvTranscript.setText(sb.toString().trim());
            }, 180L * (i + 1));
        }

        // Spring up answer card after 1.6s
        handler.postDelayed(() -> {
            promptContainer.setVisibility(View.GONE);
            answerCard.setVisibility(View.VISIBLE);
            answerCard.setScaleX(0.7f);
            answerCard.setScaleY(0.7f);
            answerCard.setAlpha(0f);

            ObjectAnimator animScaleX = ObjectAnimator.ofFloat(answerCard, "scaleX", 0.7f, 1.0f);
            ObjectAnimator animScaleY = ObjectAnimator.ofFloat(answerCard, "scaleY", 0.7f, 1.0f);
            ObjectAnimator animAlpha = ObjectAnimator.ofFloat(answerCard, "alpha", 0f, 1.0f);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(animScaleX, animScaleY, animAlpha);
            set.setDuration(450);
            set.setInterpolator(new OvershootInterpolator(1.2f));
            set.start();
        }, 1600);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startSiriFlow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
    }

    private void dismiss() {
        if (dismissListener != null) {
            dismissListener.onDismiss();
        }
    }

    private View createSiriOrb(Context context, int sizeDp) {
        FrameLayout orb = new FrameLayout(context);
        int size = Theme.dp(sizeDp, density);
        orb.setLayoutParams(new LinearLayout.LayoutParams(size, size));

        GradientDrawable orbBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF60A5FA, 0xFFA855F7, 0xFFEC4899}
        );
        orbBg.setShape(GradientDrawable.OVAL);
        orb.setBackground(orbBg);
        return orb;
    }

    // MARK: - Sieg Valley / Dattenfeld Stylized Vector Map View
    private class DattenfeldSiegMapView extends View {
        private final Paint roadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint riverPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint landPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint pinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint pinTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public DattenfeldSiegMapView(Context context) {
            super(context);
            initMap();
        }

        private void initMap() {
            landPaint.setColor(0xFFE5F3E7); // Meadow green base

            riverPaint.setColor(0xFF93C5FD); // Sieg River blue
            riverPaint.setStyle(Paint.Style.STROKE);
            riverPaint.setStrokeWidth(Theme.dp(16, density));
            riverPaint.setStrokeCap(Paint.Cap.ROUND);

            roadPaint.setColor(0xFFCBD5E1); // L333 Road grey
            roadPaint.setStyle(Paint.Style.STROKE);
            roadPaint.setStrokeWidth(Theme.dp(8, density));
            roadPaint.setStrokeCap(Paint.Cap.ROUND);

            pinPaint.setColor(0xFF22C55E); // Green Runner Pin

            pinTextPaint.setColor(Color.WHITE);
            pinTextPaint.setTextSize(Theme.dp(12, density));
            pinTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            pinTextPaint.setTextAlign(Paint.Align.CENTER);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();
            if (w == 0 || h == 0) return;

            // Meadow base
            RectF bounds = new RectF(0, 0, w, h);
            canvas.drawRoundRect(bounds, Theme.dp(18, density), Theme.dp(18, density), landPaint);

            // Curved Sieg river flowing through Dattenfeld
            Path river = new Path();
            river.moveTo(0, h * 0.70f);
            river.cubicTo(w * 0.35f, h * 0.85f, w * 0.65f, h * 0.35f, w, h * 0.50f);
            canvas.drawPath(river, riverPaint);

            // L333 highway curve
            Path road = new Path();
            road.moveTo(0, h * 0.40f);
            road.cubicTo(w * 0.30f, h * 0.50f, w * 0.70f, h * 0.20f, w, h * 0.25f);
            canvas.drawPath(road, roadPaint);

            // Sportpark Windeck Green Destination Pin
            float pinX = w * 0.62f;
            float pinY = h * 0.38f;
            canvas.drawCircle(pinX, pinY, Theme.dp(16, density), pinPaint);
            canvas.drawText("🏃", pinX, pinY + Theme.dp(5, density), pinTextPaint);
        }
    }
}
