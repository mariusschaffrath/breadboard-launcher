package me.jxl.breadboard;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
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
    private WebView webView;
    private static final String DASHBOARD_URL = "http://192.168.178.131:8123/apple-homeview/home?kiosk";
    private static final String HA_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJkNzk0MWVhOWQ5OWU0YzhjOTc0YjU5NjE4N2Q1YjI0OSIsImlhdCI6MTc4NzkzMDIzNSwiZXhwIjoyMTAzMjkwMjM1fQ.Hx4GbbBbAPN9ToA2opU7wpyIZ5GV1bmgDNHNLswLG9M";

    private static final String POLYFILL_SCRIPT = "<script>"
        + "try {"
        + "  if (!window.localStorage.getItem('hassTokens')) {"
        + "    window.localStorage.setItem('hassTokens', JSON.stringify({"
        + "      access_token: '" + HA_TOKEN + "',"
        + "      token_type: 'Bearer',"
        + "      expires_in: 315360000,"
        + "      hassUrl: 'http://192.168.178.131:8123'"
        + "    }));"
        + "  }"
        + "} catch(e) {}"
        + "if (typeof globalThis === 'undefined') { window.globalThis = window; }"
        + "if (typeof queueMicrotask === 'undefined') { window.queueMicrotask = function(cb) { Promise.resolve().then(cb); }; }"
        + "if (window.HTMLSlotElement && !HTMLSlotElement.prototype.assignedElements) {"
        + "  HTMLSlotElement.prototype.assignedElements = function(opts) {"
        + "    return this.assignedNodes(opts).filter(function(n) { return n.nodeType === 1; });"
        + "  };"
        + "}"
        + "var _origScroll = window.scrollTo;"
        + "window.scrollTo = function(x, y) {"
        + "  if (typeof x === 'object' && x !== null) { _origScroll.call(this, x.left || 0, x.top || 0); }"
        + "  else { _origScroll.apply(this, arguments); }"
        + "};"
        + "</script>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen on 24/7 for smart display
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // Hide Android status bar and navigation bar
        hideSystemUI();

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

        webView.setBackgroundColor(Color.BLACK);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                hideSystemUI();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.loadUrl(DASHBOARD_URL);
                    }
                }, 5000);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (url.contains("/apple-homeview") || url.equals("http://192.168.178.131:8123/") || url.contains("/auth/token")) {
                    WebResourceResponse res = injectPolyfills(view, url);
                    if (res != null) return res;
                }
                return super.shouldInterceptRequest(view, url);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.contains("/apple-homeview") || url.equals("http://192.168.178.131:8123/")) {
                    WebResourceResponse res = injectPolyfills(view, url);
                    if (res != null) return res;
                }
                return super.shouldInterceptRequest(view, request);
            }

            private WebResourceResponse injectPolyfills(WebView view, String url) {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setRequestProperty("User-Agent", view.getSettings().getUserAgentString());
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(8000);
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    reader.close();
                    String html = sb.toString();
                    if (html.contains("<head>")) {
                        html = html.replaceFirst("<head>", "<head>" + POLYFILL_SCRIPT);
                    } else {
                        html = POLYFILL_SCRIPT + html;
                    }
                    byte[] bytes = html.getBytes("UTF-8");
                    return new WebResourceResponse("text/html", "UTF-8", new ByteArrayInputStream(bytes));
                } catch (Exception e) {
                    return null;
                }
            }
        });

        webView.loadUrl(DASHBOARD_URL);
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
