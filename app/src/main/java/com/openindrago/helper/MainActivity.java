package com.openindrago.helper;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

    private static final String DRAGO_URL = "https://dragoplayer.online/";
    private static final Pattern HTTPS_URL = Pattern.compile("https://[^\\s<>\\\"]+");

    private FrameLayout root;
    private WebView webView;
    private ProgressBar progress;
    private String pendingDiskwalaUrl;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private WebChromeClient chromeClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
        configureWebView();
        handleIntent(getIntent());
        webView.loadUrl(DRAGO_URL);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
        webView.loadUrl(DRAGO_URL);
    }

    private void buildUi() {
        root = new FrameLayout(this);
        root.setBackgroundColor(0xFF000000);

        webView = new WebView(this);
        FrameLayout.LayoutParams webParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        root.addView(webView, webParams);

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(3)
        );
        progressParams.gravity = Gravity.TOP;
        root.addView(progress, progressParams);

        setContentView(root);
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progress.setVisibility(View.GONE);
                if (url != null && url.startsWith("https://dragoplayer.online")) {
                    injectSharedLinkWhenReady();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();
                if (scheme == null) return false;
                if (scheme.equals("http") || scheme.equals("https")) return false;
                openExternal(uri);
                return true;
            }
        });

        chromeClient = new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progress.setProgress(newProgress);
                progress.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                customView = view;
                customViewCallback = callback;
                webView.setVisibility(View.GONE);
                root.addView(view, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
            }

            @Override
            public void onHideCustomView() {
                if (customView == null) return;
                root.removeView(customView);
                customView = null;
                webView.setVisibility(View.VISIBLE);
                if (customViewCallback != null) {
                    customViewCallback.onCustomViewHidden();
                    customViewCallback = null;
                }
            }
        };
        webView.setWebChromeClient(chromeClient);

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimetype, long contentLength) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Could not open download link", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void handleIntent(Intent intent) {
        pendingDiskwalaUrl = null;
        if (intent == null) return;

        String candidate = null;
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            CharSequence shared = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
            if (shared != null) candidate = extractUrl(shared.toString());
        } else if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            candidate = intent.getData().toString();
        }

        if (candidate != null && isDiskwalaUrl(candidate)) {
            pendingDiskwalaUrl = candidate;
        } else if (Intent.ACTION_SEND.equals(intent.getAction()) || Intent.ACTION_VIEW.equals(intent.getAction())) {
            Toast.makeText(this, "No Diskwala link found in the shared text", Toast.LENGTH_LONG).show();
        }
    }

    private String extractUrl(String text) {
        Matcher matcher = HTTPS_URL.matcher(text);
        while (matcher.find()) {
            String url = trimTrailingPunctuation(matcher.group());
            if (isDiskwalaUrl(url)) return url;
        }
        return null;
    }

    private String trimTrailingPunctuation(String url) {
        while (url.endsWith(")") || url.endsWith("]") || url.endsWith("}") ||
                url.endsWith(",") || url.endsWith(".") || url.endsWith(";") || url.endsWith("!")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    private boolean isDiskwalaUrl(String value) {
        try {
            Uri uri = Uri.parse(value);
            String host = uri.getHost();
            if (host == null || !"https".equalsIgnoreCase(uri.getScheme())) return false;
            host = host.toLowerCase(Locale.ROOT);
            return host.equals("diskwala.com") || host.endsWith(".diskwala.com");
        } catch (Exception ignored) {
            return false;
        }
    }

    private void injectSharedLinkWhenReady() {
        if (pendingDiskwalaUrl == null) return;

        final String url = pendingDiskwalaUrl;
        pendingDiskwalaUrl = null; // Avoid submitting it again on every navigation.
        final String quoted = JSONObject.quote(url);

        String script =
                "(function(){" +
                "const shared=" + quoted + ";" +
                "let tries=0;" +
                "const t=setInterval(function(){" +
                "tries++;" +
                "const inputs=[...document.querySelectorAll('input')];" +
                "const input=inputs.find(function(i){" +
                " const p=(i.placeholder||'').toLowerCase();" +
                " const n=(i.name||'').toLowerCase();" +
                " return i.type==='url'||p.includes('video url')||p.includes('paste')||n.includes('url')||n.includes('link');" +
                "});" +
                "if(input){" +
                " try{" +
                "  const setter=Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value').set;" +
                "  setter.call(input,shared);" +
                " }catch(e){input.value=shared;}" +
                " input.dispatchEvent(new Event('input',{bubbles:true}));" +
                " input.dispatchEvent(new Event('change',{bubbles:true}));" +
                " input.focus();" +
                " const buttons=[...document.querySelectorAll('button,input[type=submit]')];" +
                " const watch=buttons.find(function(b){return ((b.innerText||b.value||'').trim().toLowerCase()==='watch');});" +
                " clearInterval(t);" +
                " setTimeout(function(){" +
                "   if(watch){watch.click();}" +
                "   else if(input.form){if(input.form.requestSubmit){input.form.requestSubmit();}else{input.form.submit();}}" +
                " },250);" +
                "}" +
                "if(tries>40){clearInterval(t);}" +
                "},250);" +
                "})();";

        webView.evaluateJavascript(script, null);
    }

    private void openExternal(Uri uri) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException ignored) {
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed() {
        if (customView != null) {
            chromeClient.onHideCustomView();
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }
}
