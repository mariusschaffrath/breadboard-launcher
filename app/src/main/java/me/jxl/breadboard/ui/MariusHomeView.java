package me.jxl.breadboard.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.jxl.breadboard.models.HomeViewFixtures;

public class MariusHomeView extends FrameLayout {

    public interface OnNavigateListener {
        void onOpenMusic();
        void onOpenHome();
        void onOpenRecipe();
        void onTriggerSiri();
        void onResetToGallery();
    }

    private final float density;
    private OnNavigateListener navigateListener;

    public MariusHomeView(Context context) {
        super(context);
        this.density = getResources().getDisplayMetrics().density;
        init(context);
    }

    public void setOnNavigateListener(OnNavigateListener listener) {
        this.navigateListener = listener;
    }

    private void init(Context context) {
        // Soft blurred dark green gradient background
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF064E3B, 0xFF065F46, 0xFF022C22}
        );
        setBackground(bg);

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(Theme.dp(44, density), Theme.dp(32, density), Theme.dp(44, density), Theme.dp(24, density));
        addView(root, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // 1. Header: Greeting on Left, Memoji on Right
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(header, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout greetingBox = new LinearLayout(context);
        greetingBox.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lpGreeting = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        header.addView(greetingBox, lpGreeting);

        TextView tvSub = new TextView(context);
        tvSub.setText("GUTEN MORGEN,");
        tvSub.setTextColor(0xFFD1D5DB);
        tvSub.setTextSize(22);
        tvSub.setTypeface(Theme.getBoldTypeface(context));
        tvSub.setLetterSpacing(0.08f);
        greetingBox.addView(tvSub);

        TextView tvName = new TextView(context);
        tvName.setText("MARIUS");
        tvName.setTextColor(Color.WHITE);
        tvName.setTextSize(26);
        tvName.setTypeface(Theme.getRoundedTypeface(context));
        tvName.setLetterSpacing(0.08f);
        greetingBox.addView(tvName);

        // Memoji Circle on Top Right (72dp)
        FrameLayout memojiBtn = new FrameLayout(context);
        int memojiSize = Theme.dp(64, density);
        LinearLayout.LayoutParams lpMemoji = new LinearLayout.LayoutParams(memojiSize, memojiSize);
        GradientDrawable memojiBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF34D399, 0xFF059669}
        );
        memojiBg.setShape(GradientDrawable.OVAL);
        memojiBtn.setBackground(memojiBg);

        TextView memojiIcon = new TextView(context);
        memojiIcon.setText("M");
        memojiIcon.setTextColor(Color.WHITE);
        memojiIcon.setTextSize(24);
        memojiIcon.setTypeface(Theme.getBoldTypeface(context));
        memojiIcon.setGravity(Gravity.CENTER);
        memojiBtn.addView(memojiIcon, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        memojiBtn.setOnClickListener(v -> {
            if (navigateListener != null) navigateListener.onResetToGallery();
        });
        header.addView(memojiBtn, lpMemoji);

        // Spacing
        View spacer1 = new View(context);
        root.addView(spacer1, new LinearLayout.LayoutParams(1, Theme.dp(24, density)));

        // 2. Row 1: Music, Podcasts, Weather Widget
        LinearLayout row1 = new LinearLayout(context);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(row1, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Theme.dp(120, density)));

        // Music Squircle
        View musicSquircle = createAppSquircle(context, "Musik", "♫", new int[]{0xFFFA2D48, 0xFFBE123C}, () -> {
            if (navigateListener != null) navigateListener.onOpenMusic();
        });
        row1.addView(musicSquircle);

        addHorizontalGap(context, row1, 16);

        // Podcasts Squircle
        View podcastsSquircle = createAppSquircle(context, "Podcasts", "🎙", new int[]{0xFFA855F7, 0xFF7E22CE}, () -> {});
        row1.addView(podcastsSquircle);

        addHorizontalGap(context, row1, 16);

        // Wide Weather Widget
        View weatherWidget = createWeatherWidget(context);
        row1.addView(weatherWidget, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));

        // Spacing
        View spacer2 = new View(context);
        root.addView(spacer2, new LinearLayout.LayoutParams(1, Theme.dp(18, density)));

        // 3. Row 2: Home, Cookbook, Siri, Calendar Widget
        LinearLayout row2 = new LinearLayout(context);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        root.addView(row2, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Theme.dp(120, density)));

        // Home Squircle
        View homeSquircle = createAppSquircle(context, "Home", "⌂", new int[]{0xFFF59E0B, 0xFFD97706}, () -> {
            if (navigateListener != null) navigateListener.onOpenHome();
        });
        row2.addView(homeSquircle);

        addHorizontalGap(context, row2, 16);

        // Cookbook Squircle
        View cookbookSquircle = createAppSquircle(context, "Rezepte", "🍴", new int[]{0xFF10B981, 0xFF047857}, () -> {
            if (navigateListener != null) navigateListener.onOpenRecipe();
        });
        row2.addView(cookbookSquircle);

        addHorizontalGap(context, row2, 16);

        // Siri Sphere Button
        View siriBtn = createSiriButton(context, () -> {
            if (navigateListener != null) navigateListener.onTriggerSiri();
        });
        row2.addView(siriBtn);

        addHorizontalGap(context, row2, 16);

        // Wide Calendar Widget
        View calendarWidget = createCalendarWidget(context);
        row2.addView(calendarWidget, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));
    }

    private View createAppSquircle(Context context, String title, String icon, int[] gradientColors, Runnable onClick) {
        int size = Theme.dp(120, density);
        FrameLayout card = new FrameLayout(context);
        card.setLayoutParams(new LinearLayout.LayoutParams(size, size));

        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR, gradientColors);
        bg.setCornerRadius(Theme.dp(26, density));
        bg.setStroke(Theme.dp(1, density), 0x44FFFFFF);
        card.setBackground(bg);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        card.addView(content, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        TextView tvIcon = new TextView(context);
        tvIcon.setText(icon);
        tvIcon.setTextColor(Color.WHITE);
        tvIcon.setTextSize(34);
        tvIcon.setGravity(Gravity.CENTER);
        content.addView(tvIcon);

        TextView tvTitle = new TextView(context);
        tvTitle.setText(title);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(13);
        tvTitle.setTypeface(Theme.getBoldTypeface(context));
        tvTitle.setGravity(Gravity.CENTER);
        content.addView(tvTitle);

        card.setOnClickListener(v -> onClick.run());
        return card;
    }

    private View createSiriButton(Context context, Runnable onClick) {
        int size = Theme.dp(120, density);
        FrameLayout card = new FrameLayout(context);
        card.setLayoutParams(new LinearLayout.LayoutParams(size, size));

        // Dark glass base with rounded corners
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF1F2937, 0xFF111827}
        );
        bg.setCornerRadius(Theme.dp(26, density));
        bg.setStroke(Theme.dp(2, density), 0xFF8B5CF6);
        card.setBackground(bg);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        card.addView(content, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Siri Orb Mini
        FrameLayout orb = new FrameLayout(context);
        int orbSize = Theme.dp(52, density);
        LinearLayout.LayoutParams lpOrb = new LinearLayout.LayoutParams(orbSize, orbSize);
        GradientDrawable orbBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF60A5FA, 0xFFA855F7, 0xFFEC4899}
        );
        orbBg.setShape(GradientDrawable.OVAL);
        orb.setBackground(orbBg);
        content.addView(orb, lpOrb);

        TextView tvTitle = new TextView(context);
        tvTitle.setText("Siri");
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(13);
        tvTitle.setTypeface(Theme.getBoldTypeface(context));
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setPadding(0, Theme.dp(6, density), 0, 0);
        content.addView(tvTitle);

        card.setOnClickListener(v -> onClick.run());
        return card;
    }

    private View createWeatherWidget(Context context) {
        FrameLayout card = new FrameLayout(context);
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF0284C7, 0xFF0369A1, 0xFF075985}
        );
        bg.setCornerRadius(Theme.dp(26, density));
        bg.setStroke(Theme.dp(1, density), 0x33FFFFFF);
        card.setBackground(bg);
        card.setPadding(Theme.dp(20, density), Theme.dp(16, density), Theme.dp(20, density), Theme.dp(16, density));

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(content, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        LinearLayout left = new LinearLayout(context);
        left.setOrientation(LinearLayout.VERTICAL);
        content.addView(left, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView tvLoc = new TextView(context);
        tvLoc.setText("📍 " + HomeViewFixtures.LOCATION);
        tvLoc.setTextColor(Color.WHITE);
        tvLoc.setTextSize(14);
        tvLoc.setTypeface(Theme.getBoldTypeface(context));
        left.addView(tvLoc);

        TextView tvDesc = new TextView(context);
        tvDesc.setText(HomeViewFixtures.WEATHER_CONDITION);
        tvDesc.setTextColor(0xFFE0F2FE);
        tvDesc.setTextSize(12);
        tvDesc.setPadding(0, Theme.dp(4, density), 0, 0);
        left.addView(tvDesc);

        TextView tvTemp = new TextView(context);
        tvTemp.setText(HomeViewFixtures.TEMPERATURE);
        tvTemp.setTextColor(Color.WHITE);
        tvTemp.setTextSize(44);
        tvTemp.setTypeface(Theme.getRoundedTypeface(context));
        content.addView(tvTemp);

        return card;
    }

    private View createCalendarWidget(Context context) {
        FrameLayout card = new FrameLayout(context);
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF1E293B, 0xFF0F172A}
        );
        bg.setCornerRadius(Theme.dp(26, density));
        bg.setStroke(Theme.dp(1, density), 0x33FFFFFF);
        card.setBackground(bg);
        card.setPadding(Theme.dp(20, density), Theme.dp(16, density), Theme.dp(20, density), Theme.dp(16, density));

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(content, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        // Date indicator
        LinearLayout dateBox = new LinearLayout(context);
        dateBox.setOrientation(LinearLayout.VERTICAL);
        dateBox.setGravity(Gravity.CENTER);
        dateBox.setPadding(0, 0, Theme.dp(16, density), 0);
        content.addView(dateBox);

        TextView tvMonth = new TextView(context);
        tvMonth.setText("APRIL");
        tvMonth.setTextColor(0xFFEF4444);
        tvMonth.setTextSize(11);
        tvMonth.setTypeface(Theme.getBoldTypeface(context));
        dateBox.addView(tvMonth);

        TextView tvDay = new TextView(context);
        tvDay.setText("1");
        tvDay.setTextColor(Color.WHITE);
        tvDay.setTextSize(30);
        tvDay.setTypeface(Theme.getRoundedTypeface(context));
        dateBox.addView(tvDay);

        // Schedule list
        LinearLayout schedule = new LinearLayout(context);
        schedule.setOrientation(LinearLayout.VERTICAL);
        content.addView(schedule, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView tvEvent1 = new TextView(context);
        tvEvent1.setText("Projekt-Sprint Review  •  11:00");
        tvEvent1.setTextColor(Color.WHITE);
        tvEvent1.setTextSize(13);
        tvEvent1.setTypeface(Theme.getBoldTypeface(context));
        schedule.addView(tvEvent1);

        TextView tvEvent2 = new TextView(context);
        tvEvent2.setText("Mittagessen im Siegtal  •  12:30");
        tvEvent2.setTextColor(0xFF94A3B8);
        tvEvent2.setTextSize(12);
        tvEvent2.setPadding(0, Theme.dp(4, density), 0, 0);
        schedule.addView(tvEvent2);

        return card;
    }

    private void addHorizontalGap(Context context, LinearLayout parent, int dp) {
        View gap = new View(context);
        parent.addView(gap, new LinearLayout.LayoutParams(Theme.dp(dp, density), 1));
    }
}
