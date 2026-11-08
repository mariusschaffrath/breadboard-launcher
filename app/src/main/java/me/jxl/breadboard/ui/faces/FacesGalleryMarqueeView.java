package me.jxl.breadboard.ui.faces;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import me.jxl.breadboard.ui.Theme;

/**
 * 1:1 Apple HomeView Faces Gallery
 * - Background #F5F5F7
 * - Three rows of face previews drifting sideways in endless loops:
 *   Row 1 moves left, Row 2 moves right, Row 3 moves left (~25dp/s)
 * - Grid scales from 1.0 to 1.08 over 3s on appear
 * - Tapping any preview triggers selection callback to zoom to full screen
 */
public class FacesGalleryMarqueeView extends FrameLayout {

    public interface OnFaceSelectedListener {
        void onFaceSelected(FaceThumbnailView.FaceKind kind);
    }

    private final float density;
    private OnFaceSelectedListener listener;

    private LinearLayout gridContainer;
    private LinearLayout row1Layout;
    private LinearLayout row2Layout;
    private LinearLayout row3Layout;

    private ValueAnimator animRow1;
    private ValueAnimator animRow2;
    private ValueAnimator animRow3;
    private ValueAnimator animGridScale;

    private final int tileWidthDp = 175;
    private final int tileHeightDp = 160;
    private final int gapDp = 16;
    private int periodPixels;

    public FacesGalleryMarqueeView(Context context) {
        super(context);
        this.density = getResources().getDisplayMetrics().density;
        init(context);
    }

    public void setOnFaceSelectedListener(OnFaceSelectedListener listener) {
        this.listener = listener;
    }

    private void init(Context context) {
        setBackgroundColor(0xFFF5F5F7);

        // Center Grid Container (Scales 1.0 -> 1.08 on appear)
        gridContainer = new LinearLayout(context);
        gridContainer.setOrientation(LinearLayout.VERTICAL);
        gridContainer.setGravity(Gravity.CENTER);

        FrameLayout.LayoutParams lpGrid = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        lpGrid.gravity = Gravity.CENTER;
        addView(gridContainer, lpGrid);

        // Calculate periodic repeat width (6 tiles * (175 + 16) dp)
        periodPixels = (int) ((tileWidthDp + gapDp) * 6 * density);

        // Define Row items (Repeated twice for seamless loop)
        FaceThumbnailView.FaceKind[] row1Items = new FaceThumbnailView.FaceKind[]{
                FaceThumbnailView.FaceKind.DAD_SON,
                FaceThumbnailView.FaceKind.MODULAR_QUAD,
                FaceThumbnailView.FaceKind.SMART_HOME_INDIGO,
                FaceThumbnailView.FaceKind.SNOOPY,
                FaceThumbnailView.FaceKind.FLOW_JELLY,
                FaceThumbnailView.FaceKind.TYPOGRAPHIC_SAGE
        };

        FaceThumbnailView.FaceKind[] row2Items = new FaceThumbnailView.FaceKind[]{
                FaceThumbnailView.FaceKind.CALENDAR_MATRIX,
                FaceThumbnailView.FaceKind.STANDBY,
                FaceThumbnailView.FaceKind.DAD_SON,
                FaceThumbnailView.FaceKind.MODULAR_QUAD,
                FaceThumbnailView.FaceKind.TYPOGRAPHIC_SAGE,
                FaceThumbnailView.FaceKind.SNOOPY
        };

        FaceThumbnailView.FaceKind[] row3Items = new FaceThumbnailView.FaceKind[]{
                FaceThumbnailView.FaceKind.FLOW_JELLY,
                FaceThumbnailView.FaceKind.SMART_HOME_INDIGO,
                FaceThumbnailView.FaceKind.CALENDAR_MATRIX,
                FaceThumbnailView.FaceKind.DAD_SON,
                FaceThumbnailView.FaceKind.STANDBY,
                FaceThumbnailView.FaceKind.MODULAR_QUAD
        };

        // Row 1
        row1Layout = createRow(context, row1Items);
        gridContainer.addView(row1Layout);

        // Spacer
        addVerticalSpacer(context, gridContainer, gapDp);

        // Row 2
        row2Layout = createRow(context, row2Items);
        gridContainer.addView(row2Layout);

        // Spacer
        addVerticalSpacer(context, gridContainer, gapDp);

        // Row 3
        row3Layout = createRow(context, row3Items);
        gridContainer.addView(row3Layout);

        setupDriftAnimations();
    }

    private LinearLayout createRow(Context context, FaceThumbnailView.FaceKind[] items) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, Theme.dp(tileHeightDp, density)));

        // Repeat the 6 items 3 times for a wide seamless buffer
        for (int rep = 0; rep < 3; rep++) {
            for (FaceThumbnailView.FaceKind kind : items) {
                FaceThumbnailView thumb = new FaceThumbnailView(context, kind);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        Theme.dp(tileWidthDp, density), Theme.dp(tileHeightDp, density));
                lp.rightMargin = Theme.dp(gapDp, density);
                thumb.setLayoutParams(lp);

                thumb.setOnClickListener(v -> {
                    thumb.animate().scaleX(0.92f).scaleY(0.92f).setDuration(120).withEndAction(() -> {
                        thumb.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start();
                        if (listener != null) listener.onFaceSelected(kind);
                    }).start();
                });

                row.addView(thumb);
            }
        }
        return row;
    }

    private void addVerticalSpacer(Context context, LinearLayout parent, int dp) {
        View spacer = new View(context);
        parent.addView(spacer, new LinearLayout.LayoutParams(1, Theme.dp(dp, density)));
    }

    private void setupDriftAnimations() {
        // Row 1: Leftward drift
        animRow1 = ValueAnimator.ofFloat(0, -periodPixels);
        animRow1.setDuration(46000);
        animRow1.setRepeatCount(ValueAnimator.INFINITE);
        animRow1.setInterpolator(new LinearInterpolator());
        animRow1.addUpdateListener(animation -> {
            float val = (float) animation.getAnimatedValue();
            row1Layout.setTranslationX(val);
        });

        // Row 2: Rightward drift
        animRow2 = ValueAnimator.ofFloat(-periodPixels, 0);
        animRow2.setDuration(46000);
        animRow2.setRepeatCount(ValueAnimator.INFINITE);
        animRow2.setInterpolator(new LinearInterpolator());
        animRow2.addUpdateListener(animation -> {
            float val = (float) animation.getAnimatedValue();
            row2Layout.setTranslationX(val);
        });

        // Row 3: Leftward drift
        animRow3 = ValueAnimator.ofFloat(0, -periodPixels);
        animRow3.setDuration(46000);
        animRow3.setRepeatCount(ValueAnimator.INFINITE);
        animRow3.setInterpolator(new LinearInterpolator());
        animRow3.addUpdateListener(animation -> {
            float val = (float) animation.getAnimatedValue();
            row3Layout.setTranslationX(val);
        });

        // Grid Scale on appear: 1.0 -> 1.08 over 3s
        animGridScale = ValueAnimator.ofFloat(1.0f, 1.08f);
        animGridScale.setDuration(3000);
        animGridScale.setInterpolator(new AccelerateDecelerateInterpolator());
        animGridScale.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            gridContainer.setScaleX(scale);
            gridContainer.setScaleY(scale);
        });
    }

    public void startAnimations() {
        if (animRow1 != null && !animRow1.isRunning()) animRow1.start();
        if (animRow2 != null && !animRow2.isRunning()) animRow2.start();
        if (animRow3 != null && !animRow3.isRunning()) animRow3.start();
        if (animGridScale != null && !animGridScale.isRunning()) animGridScale.start();
    }

    public void stopAnimations() {
        if (animRow1 != null) animRow1.cancel();
        if (animRow2 != null) animRow2.cancel();
        if (animRow3 != null) animRow3.cancel();
        if (animGridScale != null) animGridScale.cancel();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimations();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimations();
    }
}
