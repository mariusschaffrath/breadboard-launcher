package me.jxl.breadboard;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class MainActivity extends Activity {
    private WebView webView;
    private static final String DASHBOARD_URL = "http://192.168.178.131:8123/apple-homeview/home?kiosk";
    private static final String HA_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJkNzk0MWVhOWQ5OWU0YzhjOTc0YjU5NjE4N2Q1YjI0OSIsImlhdCI6MTc4NzkzMDIzNSwiZXhwIjoyMTAzMjkwMjM1fQ.Hx4GbbBbAPN9ToA2opU7wpyIZ5GV1bmgDNHNLswLG9M";

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
        s.setAppCacheEnabled(true);
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

                // Inject Home Assistant Long-Lived Access Token directly into localStorage
                // This eliminates the login screen completely!
                String js = "try {" +
                    "  var tokens = window.localStorage.getItem('hassTokens');" +
                    "  if (!tokens || tokens.indexOf('" + HA_TOKEN + "') === -1) {" +
                    "    window.localStorage.setItem('hassTokens', JSON.stringify({" +
                    "      access_token: '" + HA_TOKEN + "'," +
                    "      token_type: 'Bearer'," +
                    "      expires_in: 315360000," +
                    "      hassUrl: 'http://192.168.178.131:8123'" +
                    "    }));" +
                    "    window.location.reload();" +
                    "  }" +
                    "} catch(e) {}";
                view.evaluateJavascript(js, null);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // Auto-retry in 5 seconds if Home Assistant is restarting or WiFi dropped
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.loadUrl(DASHBOARD_URL);
                    }
                }, 5000);
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
        // Prevent accidental closing of launcher
    }
}
