package me.jxl.breadboard.ui.faces;

import android.content.Context;
import android.graphics.Color;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.List;

import me.jxl.breadboard.ui.Theme;

public class WatchFaceGalleryView extends FrameLayout {

    public interface OnOpenMusicListener {
        void onOpenMusic();
    }

    private final float density;
    private ViewFlipper flipper;
    private LinearLayout dotsContainer;
    private final List<View> dotViews = new ArrayList<>();
    private final List<WatchFaceBaseView> faces = new ArrayList<>();
    private GestureDetector gestureDetector;
    private OnOpenMusicListener musicListener;

    public WatchFaceGalleryView(Context context) {
        super(context);
        this.density = getResources().getDisplayMetrics().density;
        init(context);
    }

    public void setOnOpenMusicListener(OnOpenMusicListener listener) {
        this.musicListener = listener;
    }

    private void init(Context context) {
        setBackgroundColor(0xFF000000);

        flipper = new ViewFlipper(context);
        addView(flipper, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // 1. Modular Quad
        ModularFaceView modular = new ModularFaceView(context);
        modular.setOnMusicWidgetClickListener(() -> {
            if (musicListener != null) musicListener.onOpenMusic();
        });
        addFace(modular);

        // 2. Smart Home Indigo Platter
        addFace(new SmartHomeFaceView(context));

        // 3. Snoopy & Peanuts (Day & Night)
        addFace(new SnoopyFaceView(context));

        // 4. Calendar Matrix
        addFace(new CalendarFaceView(context));

        // 5. Typographic Sage
        addFace(new TypographicFaceView(context));

        // 6. Flow Jelly
        addFace(new FlowFaceView(context));

        // 7. StandBy Nightstand
        addFace(new StandByFaceView(context));

        // Navigation Chevron Chevrons (Left / Right)
        TextView prevBtn = createChevron(context, "‹", Gravity.START);
        prevBtn.setOnClickListener(v -> showPrevFace());
        addView(prevBtn);

        TextView nextBtn = createChevron(context, "›", Gravity.END);
        nextBtn.setOnClickListener(v -> showNextFace());
        addView(nextBtn);

        // Page Indicator Dots Container at Bottom
        dotsContainer = new LinearLayout(context);
        dotsContainer.setOrientation(LinearLayout.HORIZONTAL);
        dotsContainer.setGravity(Gravity.CENTER);
        dotsContainer.setPadding(Theme.dp(16, density), Theme.dp(6, density), Theme.dp(16, density), Theme.dp(6, density));
        dotsContainer.setBackground(Theme.makePill(0x33000000, 0x1AFFFFFF, density));

        LayoutParams lpDots = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpDots.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        lpDots.bottomMargin = Theme.dp(8, density);
        addView(dotsContainer, lpDots);

        createDots(context);
        updateDots();

        // Setup swipe gestures
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 80;
            private static final int SWIPE_VELOCITY_THRESHOLD = 80;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            showPrevFace();
                        } else {
                            showNextFace();
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void addFace(WatchFaceBaseView face) {
        faces.add(face);
        flipper.addView(face);
    }

    private TextView createChevron(Context context, String symbol, int gravity) {
        TextView chevron = new TextView(context);
        chevron.setText(symbol);
        chevron.setTextSize(32f);
        chevron.setTextColor(0x88FFFFFF);
        chevron.setGravity(Gravity.CENTER);
        chevron.setBackground(Theme.makePill(0x22FFFFFF, 0x1AFFFFFF, density));

        LayoutParams lp = new LayoutParams(Theme.dp(44, density), Theme.dp(64, density));
        lp.gravity = gravity | Gravity.CENTER_VERTICAL;
        if (gravity == Gravity.START) {
            lp.leftMargin = Theme.dp(12, density);
        } else {
            lp.rightMargin = Theme.dp(12, density);
        }
        chevron.setLayoutParams(lp);
        return chevron;
    }

    private void createDots(Context context) {
        dotsContainer.removeAllViews();
        dotViews.clear();
        for (int i = 0; i < faces.size(); i++) {
            View dot = new View(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Theme.dp(6, density), Theme.dp(6, density));
            lp.leftMargin = Theme.dp(4, density);
            lp.rightMargin = Theme.dp(4, density);
            dot.setLayoutParams(lp);
            dotViews.add(dot);
            dotsContainer.addView(dot);
        }
    }

    private void updateDots() {
        int currentIndex = flipper.getDisplayedChild();
        for (int i = 0; i < dotViews.size(); i++) {
            View dot = dotViews.get(i);
            if (i == currentIndex) {
                dot.setBackground(Theme.makePill(Color.WHITE, 0, density));
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) dot.getLayoutParams();
                lp.width = Theme.dp(16, density);
                dot.setLayoutParams(lp);
            } else {
                dot.setBackground(Theme.makePill(0x44FFFFFF, 0, density));
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) dot.getLayoutParams();
                lp.width = Theme.dp(6, density);
                dot.setLayoutParams(lp);
            }
        }
    }

    public void showNextFace() {
        flipper.setInAnimation(getContext(), android.R.anim.slide_in_left);
        flipper.setOutAnimation(getContext(), android.R.anim.slide_out_right);
        flipper.showNext();
        updateDots();
    }

    public void showPrevFace() {
        flipper.showPrevious();
        updateDots();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(ev);
        }
        return super.onInterceptTouchEvent(ev);
    }
}
