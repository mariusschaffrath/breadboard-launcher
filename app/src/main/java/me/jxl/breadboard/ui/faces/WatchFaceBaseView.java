package me.jxl.breadboard.ui.faces;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public abstract class WatchFaceBaseView extends View {
    protected final Handler timeHandler = new Handler(Looper.getMainLooper());
    protected final float density;

    protected Calendar calendar = Calendar.getInstance();
    protected SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.GERMANY);
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("EEE d. MMM", Locale.GERMANY);
    protected SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE", Locale.GERMANY);
    protected SimpleDateFormat dayNumFormat = new SimpleDateFormat("d", Locale.GERMANY);
    protected SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.GERMANY);

    private boolean isRunning = false;

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            calendar = Calendar.getInstance();
            onTimeTick();
            invalidate();
            if (isRunning) {
                timeHandler.postDelayed(this, 1000);
            }
        }
    };

    public WatchFaceBaseView(Context context) {
        super(context);
        this.density = getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isRunning = true;
        timeHandler.post(tickRunnable);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isRunning = false;
        timeHandler.removeCallbacks(tickRunnable);
    }

    protected void onTimeTick() {
        // Subclasses can override for custom tick logic
    }

    public abstract String getFaceName();
}
