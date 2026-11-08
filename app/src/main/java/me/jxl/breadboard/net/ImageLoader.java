package me.jxl.breadboard.net;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {
    private static final String TAG = "ImageLoader";
    private static ImageLoader instance;

    public interface ImageCallback {
        void onLoaded(Bitmap bitmap);
    }

    private final LruCache<String, Bitmap> memoryCache;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ImageLoader() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8; // Use 1/8th of memory for cache
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public static synchronized ImageLoader getInstance() {
        if (instance == null) {
            instance = new ImageLoader();
        }
        return instance;
    }

    public void load(String rawUrl, int targetWidth, int targetHeight, ImageCallback callback) {
        if (rawUrl == null || rawUrl.isEmpty()) {
            if (callback != null) {
                callback.onLoaded(createMusicPlaceholder(targetWidth, targetHeight));
            }
            return;
        }

        final String fullUrl;
        if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
            fullUrl = rawUrl;
        } else {
            fullUrl = "http://192.168.178.131:8123" + rawUrl;
        }

        Bitmap cached = memoryCache.get(fullUrl);
        if (cached != null && !cached.isRecycled()) {
            if (callback != null) callback.onLoaded(cached);
            return;
        }

        executor.execute(() -> {
            Bitmap bmp = null;
            try {
                URL url = new URL(fullUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("User-Agent", "HomeView/27.2 (FireOS; Ford)");
                if (fullUrl.contains("192.168.178.131")) {
                    conn.setRequestProperty("Authorization", "Bearer " + HassClient.AUTH_TOKEN);
                }
                conn.connect();

                int code = conn.getResponseCode();
                if (code == 200) {
                    InputStream is = conn.getInputStream();
                    bmp = BitmapFactory.decodeStream(is);
                    is.close();
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.w(TAG, "Failed to load image from " + fullUrl + ": " + e.getMessage());
            }

            final Bitmap resultBmp = (bmp != null) ? bmp : createMusicPlaceholder(targetWidth, targetHeight);
            if (bmp != null) {
                memoryCache.put(fullUrl, bmp);
            }

            mainHandler.post(() -> {
                if (callback != null) {
                    callback.onLoaded(resultBmp);
                }
            });
        });
    }

    public void loadInto(String url, int width, int height, ImageView imageView) {
        load(url, width, height, bitmap -> {
            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    public static Bitmap createMusicPlaceholder(int width, int height) {
        int w = Math.max(128, width);
        int h = Math.max(128, height);
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Shader shader = new LinearGradient(0, 0, w, h,
                new int[]{0xFFFA2D48, 0xFF7A1B38, 0xFF1C0D22},
                null, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        canvas.drawRect(0, 0, w, h, paint);

        // Subtle gloss circle
        paint.setShader(null);
        paint.setColor(0x22FFFFFF);
        canvas.drawCircle(w * 0.5f, h * 0.45f, w * 0.35f, paint);

        // Music note symbol
        paint.setColor(Color.WHITE);
        paint.setTextSize(w * 0.38f);
        paint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm = paint.getFontMetrics();
        float y = (h * 0.52f) - ((fm.ascent + fm.descent) / 2);
        canvas.drawText("♫", w * 0.5f, y, paint);

        return bitmap;
    }
}
