package me.jxl.breadboard.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.jxl.breadboard.models.HomeViewFixtures;

public class RecipeView extends FrameLayout {

    public interface OnDismissListener {
        void onDismiss();
    }

    private final float density;
    private OnDismissListener dismissListener;

    private LinearLayout recipeInfoBox;
    private FrameLayout ingredientsSheet;
    private FrameLayout walkthroughCard;

    private int currentStepIndex = 0;
    private TextView tvStepHeader;
    private TextView tvStepText;
    private View progressBar;

    public RecipeView(Context context) {
        super(context);
        this.density = getResources().getDisplayMetrics().density;
        init(context);
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.dismissListener = listener;
    }

    private void init(Context context) {
        // 1. Culinary Vector Pumpkin Soup Background
        View soupBg = new PumpkinSoupCanvasView(context);
        addView(soupBg, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // 2. Foreground Layout
        FrameLayout content = new FrameLayout(context);
        content.setPadding(Theme.dp(32, density), Theme.dp(24, density), Theme.dp(32, density), Theme.dp(28, density));
        addView(content, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // Top Navigation Bar (Close "✕" on Left, "10:09" on Right)
        FrameLayout topBar = new FrameLayout(context);
        LayoutParams lpTop = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lpTop.gravity = Gravity.TOP;
        content.addView(topBar, lpTop);

        // Close "✕" Button
        FrameLayout btnClose = new FrameLayout(context);
        int closeSize = Theme.dp(44, density);
        LayoutParams lpClose = new LayoutParams(closeSize, closeSize);
        lpClose.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        btnClose.setLayoutParams(lpClose);

        GradientDrawable closeBg = new GradientDrawable();
        closeBg.setShape(GradientDrawable.OVAL);
        closeBg.setColor(0x66000000);
        closeBg.setStroke(Theme.dp(1, density), 0x44FFFFFF);
        btnClose.setBackground(closeBg);

        TextView tvClose = new TextView(context);
        tvClose.setText("✕");
        tvClose.setTextColor(Color.WHITE);
        tvClose.setTextSize(18);
        tvClose.setTypeface(Theme.getBoldTypeface(context));
        tvClose.setGravity(Gravity.CENTER);
        btnClose.addView(tvClose, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        btnClose.setOnClickListener(v -> {
            if (dismissListener != null) dismissListener.onDismiss();
        });
        topBar.addView(btnClose);

        // Clock Pill on Right
        LinearLayout clockPill = new LinearLayout(context);
        LayoutParams lpClock = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpClock.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        clockPill.setLayoutParams(lpClock);
        clockPill.setOrientation(LinearLayout.HORIZONTAL);
        clockPill.setGravity(Gravity.CENTER_VERTICAL);
        clockPill.setPadding(Theme.dp(16, density), Theme.dp(8, density), Theme.dp(16, density), Theme.dp(8, density));

        GradientDrawable pillBg = new GradientDrawable();
        pillBg.setCornerRadius(Theme.dp(20, density));
        pillBg.setColor(0x66000000);
        pillBg.setStroke(Theme.dp(1, density), 0x44FFFFFF);
        clockPill.setBackground(pillBg);

        TextView tvClock = new TextView(context);
        tvClock.setText("🍴  10:09");
        tvClock.setTextColor(Color.WHITE);
        tvClock.setTextSize(14);
        tvClock.setTypeface(Theme.getBoldTypeface(context));
        clockPill.addView(tvClock);
        topBar.addView(clockPill);

        // 3. Bottom Recipe Showcase Card
        recipeInfoBox = new LinearLayout(context);
        recipeInfoBox.setOrientation(LinearLayout.VERTICAL);
        LayoutParams lpInfo = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lpInfo.gravity = Gravity.BOTTOM;
        content.addView(recipeInfoBox, lpInfo);

        TextView tvCookbook = new TextView(context);
        tvCookbook.setText("HOME COOKBOOK");
        tvCookbook.setTextColor(0xFFFBBF24);
        tvCookbook.setTextSize(12);
        tvCookbook.setTypeface(Theme.getBoldTypeface(context));
        tvCookbook.setLetterSpacing(0.1f);
        recipeInfoBox.addView(tvCookbook);

        TextView tvTitle = new TextView(context);
        tvTitle.setText(HomeViewFixtures.RECIPE_TITLE);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(34);
        tvTitle.setTypeface(Theme.getRoundedTypeface(context));
        recipeInfoBox.addView(tvTitle);

        TextView tvMeta = new TextView(context);
        tvMeta.setText(HomeViewFixtures.RECIPE_SUBTITLE + " • Einfach");
        tvMeta.setTextColor(0xFFE5E7EB);
        tvMeta.setTextSize(14);
        tvMeta.setPadding(0, Theme.dp(4, density), 0, Theme.dp(16, density));
        recipeInfoBox.addView(tvMeta);

        // Action Buttons Row (Zutaten & Zubereitung starten)
        LinearLayout actionsRow = new LinearLayout(context);
        actionsRow.setOrientation(LinearLayout.HORIZONTAL);
        recipeInfoBox.addView(actionsRow);

        // Button "Zutaten"
        FrameLayout btnIngredients = new FrameLayout(context);
        btnIngredients.setPadding(Theme.dp(22, density), Theme.dp(12, density), Theme.dp(22, density), Theme.dp(12, density));
        GradientDrawable ingBg = new GradientDrawable();
        ingBg.setCornerRadius(Theme.dp(24, density));
        ingBg.setColor(0x55000000);
        ingBg.setStroke(Theme.dp(1, density), 0x55FFFFFF);
        btnIngredients.setBackground(ingBg);

        TextView tvIng = new TextView(context);
        tvIng.setText("📋  Zutaten");
        tvIng.setTextColor(Color.WHITE);
        tvIng.setTextSize(15);
        tvIng.setTypeface(Theme.getBoldTypeface(context));
        btnIngredients.addView(tvIng);
        btnIngredients.setOnClickListener(v -> showIngredients(true));
        actionsRow.addView(btnIngredients);

        View gap = new View(context);
        actionsRow.addView(gap, new LinearLayout.LayoutParams(Theme.dp(16, density), 1));

        // Button "Zubereitung starten"
        FrameLayout btnStart = new FrameLayout(context);
        btnStart.setPadding(Theme.dp(24, density), Theme.dp(12, density), Theme.dp(24, density), Theme.dp(12, density));
        GradientDrawable startBg = new GradientDrawable();
        startBg.setCornerRadius(Theme.dp(24, density));
        startBg.setColor(0xFFF59E0B); // Amber Orange
        btnStart.setBackground(startBg);

        TextView tvStart = new TextView(context);
        tvStart.setText("▶  Zubereitung starten");
        tvStart.setTextColor(Color.BLACK);
        tvStart.setTextSize(15);
        tvStart.setTypeface(Theme.getBoldTypeface(context));
        btnStart.addView(tvStart);
        btnStart.setOnClickListener(v -> startWalkthrough());
        actionsRow.addView(btnStart);

        // 4. Ingredients Modal Sheet
        initIngredientsSheet(context);

        // 5. Walkthrough Card
        initWalkthroughCard(context);
    }

    private void initIngredientsSheet(Context context) {
        ingredientsSheet = new FrameLayout(context);
        LayoutParams lpSheet = new LayoutParams(Theme.dp(440, density), Theme.dp(380, density));
        lpSheet.gravity = Gravity.CENTER;
        ingredientsSheet.setLayoutParams(lpSheet);

        GradientDrawable sheetBg = new GradientDrawable();
        sheetBg.setColor(0xF01C1917);
        sheetBg.setCornerRadius(Theme.dp(28, density));
        sheetBg.setStroke(Theme.dp(1, density), 0x44FFFFFF);
        ingredientsSheet.setBackground(sheetBg);
        ingredientsSheet.setPadding(Theme.dp(24, density), Theme.dp(20, density), Theme.dp(24, density), Theme.dp(20, density));
        ingredientsSheet.setVisibility(View.GONE);

        LinearLayout sheetContent = new LinearLayout(context);
        sheetContent.setOrientation(LinearLayout.VERTICAL);
        ingredientsSheet.addView(sheetContent, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Sheet Header
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        sheetContent.addView(header, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView tvHeader = new TextView(context);
        tvHeader.setText("ZUTATENLISTE");
        tvHeader.setTextColor(0xFFF59E0B);
        tvHeader.setTextSize(12);
        tvHeader.setTypeface(Theme.getBoldTypeface(context));
        tvHeader.setLetterSpacing(0.08f);
        header.addView(tvHeader, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView tvCloseSheet = new TextView(context);
        tvCloseSheet.setText("✕");
        tvCloseSheet.setTextColor(Color.WHITE);
        tvCloseSheet.setTextSize(18);
        tvCloseSheet.setOnClickListener(v -> showIngredients(false));
        header.addView(tvCloseSheet);

        TextView tvSubtitle = new TextView(context);
        tvSubtitle.setText("Kürbis-Ingwer-Suppe (4 Portionen)");
        tvSubtitle.setTextColor(Color.WHITE);
        tvSubtitle.setTextSize(18);
        tvSubtitle.setTypeface(Theme.getBoldTypeface(context));
        tvSubtitle.setPadding(0, Theme.dp(4, density), 0, Theme.dp(14, density));
        sheetContent.addView(tvSubtitle);

        // List of Ingredients
        for (String ingredient : HomeViewFixtures.RECIPE_INGREDIENTS) {
            TextView tvItem = new TextView(context);
            tvItem.setText("✓  " + ingredient);
            tvItem.setTextColor(0xFFE5E7EB);
            tvItem.setTextSize(13);
            tvItem.setPadding(0, Theme.dp(3, density), 0, Theme.dp(3, density));
            sheetContent.addView(tvItem);
        }

        addView(ingredientsSheet);
    }

    private void initWalkthroughCard(Context context) {
        walkthroughCard = new FrameLayout(context);
        LayoutParams lpWalk = new LayoutParams(Theme.dp(620, density), Theme.dp(240, density));
        lpWalk.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        lpWalk.bottomMargin = Theme.dp(28, density);
        walkthroughCard.setLayoutParams(lpWalk);

        GradientDrawable walkBg = new GradientDrawable();
        walkBg.setColor(0xE61C1917);
        walkBg.setCornerRadius(Theme.dp(28, density));
        walkBg.setStroke(Theme.dp(1, density), 0x44FFFFFF);
        walkthroughCard.setBackground(walkBg);
        walkthroughCard.setPadding(Theme.dp(26, density), Theme.dp(20, density), Theme.dp(26, density), Theme.dp(20, density));
        walkthroughCard.setVisibility(View.GONE);

        LinearLayout cardContent = new LinearLayout(context);
        cardContent.setOrientation(LinearLayout.VERTICAL);
        walkthroughCard.addView(cardContent, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Header (Step Index & Close)
        LinearLayout walkHeader = new LinearLayout(context);
        walkHeader.setOrientation(LinearLayout.HORIZONTAL);
        cardContent.addView(walkHeader);

        tvStepHeader = new TextView(context);
        tvStepHeader.setTextColor(0xFFFBBF24);
        tvStepHeader.setTextSize(12);
        tvStepHeader.setTypeface(Theme.getBoldTypeface(context));
        tvStepHeader.setLetterSpacing(0.08f);
        walkHeader.addView(tvStepHeader, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView tvCloseWalk = new TextView(context);
        tvCloseWalk.setText("✕");
        tvCloseWalk.setTextColor(Color.WHITE);
        tvCloseWalk.setTextSize(18);
        tvCloseWalk.setOnClickListener(v -> stopWalkthrough());
        walkHeader.addView(tvCloseWalk);

        // Progress Bar Line
        FrameLayout progressTrack = new FrameLayout(context);
        progressTrack.setBackgroundColor(0x33FFFFFF);
        LinearLayout.LayoutParams lpTrack = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Theme.dp(5, density));
        lpTrack.topMargin = Theme.dp(8, density);
        lpTrack.bottomMargin = Theme.dp(14, density);
        cardContent.addView(progressTrack, lpTrack);

        progressBar = new View(context);
        progressBar.setBackgroundColor(0xFFF59E0B);
        progressTrack.addView(progressBar, new FrameLayout.LayoutParams(Theme.dp(100, density), FrameLayout.LayoutParams.MATCH_PARENT));

        // Instruction Text
        tvStepText = new TextView(context);
        tvStepText.setTextColor(Color.WHITE);
        tvStepText.setTextSize(17);
        tvStepText.setTypeface(Theme.getRoundedTypeface(context));
        tvStepText.setLineSpacing(Theme.dp(4, density), 1.0f);
        cardContent.addView(tvStepText, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));

        // Navigation Chevrons Row (Zurück / Weiter)
        LinearLayout navRow = new LinearLayout(context);
        navRow.setOrientation(LinearLayout.HORIZONTAL);
        cardContent.addView(navRow);

        TextView btnPrev = new TextView(context);
        btnPrev.setText("‹ Zurück");
        btnPrev.setTextColor(Color.WHITE);
        btnPrev.setTextSize(14);
        btnPrev.setTypeface(Theme.getBoldTypeface(context));
        btnPrev.setPadding(Theme.dp(16, density), Theme.dp(8, density), Theme.dp(16, density), Theme.dp(8, density));
        btnPrev.setOnClickListener(v -> {
            if (currentStepIndex > 0) {
                currentStepIndex--;
                updateStep();
            }
        });
        navRow.addView(btnPrev, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView btnNext = new TextView(context);
        btnNext.setText("Weiter ›");
        btnNext.setTextColor(0xFFF59E0B);
        btnNext.setTextSize(14);
        btnNext.setTypeface(Theme.getBoldTypeface(context));
        btnNext.setGravity(Gravity.END);
        btnNext.setPadding(Theme.dp(16, density), Theme.dp(8, density), Theme.dp(16, density), Theme.dp(8, density));
        btnNext.setOnClickListener(v -> {
            if (currentStepIndex < HomeViewFixtures.RECIPE_STEPS.size() - 1) {
                currentStepIndex++;
                updateStep();
            } else {
                stopWalkthrough();
            }
        });
        navRow.addView(btnNext);

        addView(walkthroughCard);
    }

    private void showIngredients(boolean show) {
        ingredientsSheet.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void startWalkthrough() {
        recipeInfoBox.setVisibility(View.GONE);
        walkthroughCard.setVisibility(View.VISIBLE);
        currentStepIndex = 0;
        updateStep();
    }

    private void stopWalkthrough() {
        walkthroughCard.setVisibility(View.GONE);
        recipeInfoBox.setVisibility(View.VISIBLE);
    }

    private void updateStep() {
        tvStepHeader.setText("SCHRITT " + (currentStepIndex + 1) + " VON " + HomeViewFixtures.RECIPE_STEPS.size());
        tvStepText.setText(HomeViewFixtures.RECIPE_STEPS.get(currentStepIndex));

        // Update progress bar width
        float pct = (float) (currentStepIndex + 1) / (float) HomeViewFixtures.RECIPE_STEPS.size();
        int totalWidth = Theme.dp(560, density);
        progressBar.setLayoutParams(new FrameLayout.LayoutParams((int) (totalWidth * pct), FrameLayout.LayoutParams.MATCH_PARENT));
    }

    // MARK: - Artistic Pumpkin Soup Vector Shaded Canvas View
    private class PumpkinSoupCanvasView extends View {
        private final Paint basePaint = new Paint();
        private final Paint bowlRimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint creamPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint sageLeafPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public PumpkinSoupCanvasView(Context context) {
            super(context);
            initCanvas();
        }

        private void initCanvas() {
            bowlRimPaint.setColor(0xFF451A03);
            bowlRimPaint.setStyle(Paint.Style.STROKE);
            bowlRimPaint.setStrokeWidth(Theme.dp(24, density));

            creamPaint.setColor(0xCCFEF3C7);
            creamPaint.setStyle(Paint.Style.STROKE);
            creamPaint.setStrokeWidth(Theme.dp(8, density));
            creamPaint.setStrokeCap(Paint.Cap.ROUND);

            sageLeafPaint.setColor(0xFF4D7C0F); // Olive Sage Green
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();
            if (w == 0 || h == 0) return;

            // 1. Dark linen base
            canvas.drawColor(0xFF181412);

            // 2. Warm radial glowing amber soup
            RadialGradient radial = new RadialGradient(
                    w / 2f, h / 2f, h * 0.7f,
                    new int[]{0xFFD97706, 0xFFB45309, 0xFF78350F, 0xFF181412},
                    new float[]{0.1f, 0.45f, 0.75f, 1.0f},
                    Shader.TileMode.CLAMP
            );
            basePaint.setShader(radial);
            canvas.drawRect(0, 0, w, h, basePaint);

            // 3. Ceramic bowl rim
            canvas.drawCircle(w / 2f, h / 2f, h * 0.46f, bowlRimPaint);

            // 4. Cream swirl
            canvas.save();
            canvas.rotate(25, w / 2f, h / 2f);
            canvas.drawArc(w / 2f - h * 0.25f, h / 2f - h * 0.25f, w / 2f + h * 0.25f, h / 2f + h * 0.25f, 30, 140, false, creamPaint);
            canvas.restore();

            // 5. Fresh sage leaf in center
            canvas.drawCircle(w / 2f - Theme.dp(12, density), h / 2f - Theme.dp(12, density), Theme.dp(14, density), sageLeafPaint);
            canvas.drawCircle(w / 2f + Theme.dp(6, density), h / 2f - Theme.dp(6, density), Theme.dp(12, density), sageLeafPaint);
        }
    }
}
