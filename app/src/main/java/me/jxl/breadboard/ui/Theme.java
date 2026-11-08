package me.jxl.breadboard.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;

public class Theme {
    // Apple HomeView Color Tokens
    public static final int BG_COLOR = 0xFF000000;               // Deep Pitch OLED Black
    public static final int GLASS_CARD = 0xFF18181D;              // Clear Glass Inactive
    public static final int GLASS_BORDER = 0x2EFFFFFF;            // 18% White Outline
    public static final int GLASS_SELECTED_BORDER = 0x80FFFFFF;   // 50% White Outline

    // Apple Accent Colors
    public static final int ACCENT_BLUE = 0xFF0A84FF;             // Apple Electric Blue (Covers/Active)
    public static final int ACCENT_AMBER = 0xFFFF9F0A;            // Warm Amber (Lighting active)
    public static final int ACCENT_PURPLE = 0xFFBF5AF2;           // Vibrant Purple (Scenes/Nightlight)
    public static final int ACCENT_SAGE = 0xFF30D158;             // Apple Mint/Sage (Climate/Temp)
    public static final int ACCENT_PINK = 0xFFFA2D48;             // Apple Music Pink
    public static final int ACCENT_INDIGO = 0xFF5E5CE6;           // Smart Home Indigo
    public static final int ACCENT_RED = 0xFFFF453A;              // Apple Coral Red (Calendar/Night)

    // Watchface Color Tokens
    public static final int WF_SNOOPY_BG_DAY = 0xFFEBEAE6;        // Snoopy Day Dial (Light Grey/Cream)
    public static final int WF_SNOOPY_BG_NIGHT = 0xFF141416;      // Snoopy Dark Dial
    public static final int WF_MODULAR_BG = 0xFFBDD2D6;           // Modular Ice Mint Platter
    public static final int WF_SMART_HOME_BG = 0xFF23305E;        // Deep Indigo Glass
    public static final int WF_CALENDAR_BG = 0xFFF2F2F6;          // Apple Calendar Light Glass
    public static final int WF_TYPOGRAPHIC_BG = 0xFF5B8A72;       // Sage Green Dial
    public static final int WF_FLOW_BG = 0xFF2A163B;              // Flow Deep Violet Gradient

    // Typography Colors
    public static final int TEXT_WHITE = 0xFFFFFFFF;
    public static final int TEXT_SECONDARY = 0xB3FFFFFF;          // 70% White
    public static final int TEXT_MUTED = 0x66FFFFFF;              // 40% White
    public static final int TEXT_BLACK = 0xFF1C1C1E;
    public static final int TEXT_BLACK_SECONDARY = 0x991C1C1E;

    private static Typeface displayBold;
    private static Typeface roundedBold;

    public static Typeface getDisplayBold(Context context) {
        if (displayBold == null) {
            try {
                displayBold = Typeface.createFromAsset(context.getAssets(), "fonts/SF-Pro-Display-Bold.otf");
            } catch (Exception e) {
                displayBold = Typeface.create("sans-serif", Typeface.BOLD);
            }
        }
        return displayBold;
    }

    public static Typeface getRoundedBold(Context context) {
        if (roundedBold == null) {
            try {
                roundedBold = Typeface.createFromAsset(context.getAssets(), "fonts/SF-Pro-Rounded-Bold.otf");
            } catch (Exception e) {
                roundedBold = Typeface.create("sans-serif-medium", Typeface.BOLD);
            }
        }
        return roundedBold;
    }

    public static Typeface getRoundedTypeface(Context context) {
        return getRoundedBold(context);
    }

    public static Typeface getBoldTypeface(Context context) {
        return getDisplayBold(context);
    }

    public static GradientDrawable makeGlass(int bgColor, int borderColor, float cornerRadiusDp, float density) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bgColor);
        gd.setCornerRadius(cornerRadiusDp * density);
        if (borderColor != 0) {
            gd.setStroke(Math.max(1, (int)(1 * density)), borderColor);
        }
        return gd;
    }

    public static GradientDrawable makePill(int bgColor, int borderColor, float density) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bgColor);
        gd.setCornerRadius(100f * density);
        if (borderColor != 0) {
            gd.setStroke(Math.max(1, (int)(1 * density)), borderColor);
        }
        return gd;
    }

    public static int dp(float dpVal, float density) {
        return (int) (dpVal * density + 0.5f);
    }
}
