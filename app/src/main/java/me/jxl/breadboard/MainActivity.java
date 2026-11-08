package me.jxl.breadboard;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {
    private static final String TAG = "HomeView";
    private static final String DASHBOARD_URL = "http://192.168.178.131:8123/apple-homeview/home?kiosk";

    private WebView webView;
    private String userAgent;
    private String polyfillScript;
    private String polyfillRawJs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen on 24/7 for smart display
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // Hide Android status bar and navigation bar
        hideSystemUI();

        // Load polyfill scripts from assets
        loadPolyfills();

        FrameLayout layout = new FrameLayout(this);
        layout.setBackgroundColor(Color.BLACK);

        webView = new WebView(this);
        layout.addView(webView);
        setContentView(layout);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setAllowFileAccess(true);
        s.setLoadsImagesAutomatically(true);
        s.setBlockNetworkImage(false);
        s.setBlockNetworkLoads(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setDisplayZoomControls(false);
        s.setBuiltInZoomControls(false);
        s.setSupportZoom(false);

        // Cache user agent string safely on UI thread
        userAgent = s.getUserAgentString();

        webView.setBackgroundColor(Color.BLACK);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.i("HomeViewJS", "[" + cm.messageLevel() + "] " + cm.message()
                    + " -- Line " + cm.lineNumber() + " (" + cm.sourceId() + ")");
                return true;
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                hideSystemUI();
                if (polyfillRawJs != null) {
                    view.evaluateJavascript(polyfillRawJs, null);
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.w(TAG, "WebView error: " + description + " (" + errorCode + "), retrying in 5s...");
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.loadUrl(DASHBOARD_URL);
                    }
                }, 5000);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (request != null && request.isForMainFrame() && "GET".equalsIgnoreCase(request.getMethod())) {
                    String url = request.getUrl().toString();
                    if (isDashboardHtmlUrl(url)) {
                        WebResourceResponse res = injectPolyfills(url);
                        if (res != null) return res;
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    if (isDashboardHtmlUrl(url)) {
                        WebResourceResponse res = injectPolyfills(url);
                        if (res != null) return res;
                    }
                }
                return super.shouldInterceptRequest(view, url);
            }

            private boolean isDashboardHtmlUrl(String url) {
                if (url == null) return false;
                return url.startsWith("http://192.168.178.131:8123/apple-homeview")
                    || url.equals("http://192.168.178.131:8123/")
                    || url.equals(DASHBOARD_URL);
            }

            private WebResourceResponse injectPolyfills(String url) {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setRequestMethod("GET");
                    if (userAgent != null) {
                        conn.setRequestProperty("User-Agent", userAgent);
                    }
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(8000);
                    int code = conn.getResponseCode();
                    if (code != 200) {
                        return null;
                    }

                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    reader.close();
                    String html = sb.toString();

                    // Neutralize dynamic import calls in inline scripts that crash Chrome 59 parser
                    html = html.replace("import(\"/browser_mod.js", "// import(\"/browser_mod.js");
                    html = html.replace("import(\"/hacsfiles/iconset.js", "// import(\"/hacsfiles/iconset.js");

                    if (polyfillScript != null) {
                        if (html.contains("<head>")) {
                            html = html.replaceFirst("<head>", "<head>" + polyfillScript);
                        } else {
                            html = polyfillScript + html;
                        }
                    }

                    byte[] bytes = html.getBytes("UTF-8");
                    return new WebResourceResponse("text/html", "UTF-8", new ByteArrayInputStream(bytes));
                } catch (Exception e) {
                    Log.e(TAG, "Error in injectPolyfills: " + e.getMessage(), e);
                    return null;
                }
            }
        });

        webView.loadUrl(DASHBOARD_URL);
    }

    private void loadPolyfills() {
        try {
            InputStream is = getAssets().open("polyfills.js");
            BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append("\n");
            }
            r.close();
            polyfillRawJs = sb.toString();
            polyfillScript = "<script>\n" + polyfillRawJs + "\n</script>";
            Log.i(TAG, "Loaded polyfills.js (" + polyfillRawJs.length() + " chars)");
        } catch (Exception e) {
            Log.e(TAG, "Failed to load polyfills.js from assets: " + e.getMessage(), e);
        }
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    public void onBackPressed() {
        // Prevent accidental exit of home launcher
    }
}
