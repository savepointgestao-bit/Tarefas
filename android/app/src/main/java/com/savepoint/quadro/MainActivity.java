package com.savepoint.quadro;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Casca do app Tarefas: uma WebView que abre o site (index.html) empacotado dentro do app.
 * Os dados ficam na nuvem (Supabase), como no site. A ponte "TarefasApp" liga o aviso de prazo
 * às notificações do celular, informa o tema do sistema e pinta a barra de status.
 */
public class MainActivity extends Activity {
    private WebView web;
    private static final String CHANNEL = "prazos";
    private static final int REQ_NOTIF = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setTextZoom(100);
        s.setSupportZoom(false);
        web.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("file:")) return false;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception ignored) { }
                return true;
            }
        });
        web.setWebChromeClient(new WebChromeClient());
        web.addJavascriptInterface(new Bridge(), "TarefasApp");
        int bg = Color.parseColor(isDarkMode() ? "#0d1715" : "#ebf7f4");
        web.setBackgroundColor(bg);
        applyBars(bg, isDarkMode());
        setContentView(web);
        web.loadUrl("file:///android_asset/index.html");
    }

    private boolean isDarkMode() {
        int mode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return mode == Configuration.UI_MODE_NIGHT_YES;
    }

    /** Pinta a barra de status e a de navegação com a cor do fundo do site e acerta a cor dos ícones. */
    private void applyBars(int color, boolean dark) {
        Window w = getWindow();
        w.getDecorView().setBackgroundColor(color);
        w.setStatusBarColor(color);
        int flags = w.getDecorView().getSystemUiVisibility();
        // SYSTEM_UI_FLAG_LIGHT_STATUS_BAR = 0x2000 (ícones escuros em barra clara)
        flags = dark ? (flags & ~0x2000) : (flags | 0x2000);
        if (Build.VERSION.SDK_INT >= 26) {
            w.setNavigationBarColor(color);
            // SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR = 0x10
            flags = dark ? (flags & ~0x10) : (flags | 0x10);
        }
        w.getDecorView().setSystemUiVisibility(flags);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int bg = Color.parseColor(isDarkMode() ? "#0d1715" : "#ebf7f4");
        web.setBackgroundColor(bg);
        applyBars(bg, isDarkMode());
        web.evaluateJavascript("window.applyPrefs && applyPrefs()", null);
    }

    @Override
    public void onBackPressed() {
        web.evaluateJavascript("window.onBack ? window.onBack() : false", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (!"true".equals(value)) MainActivity.super.onBackPressed();
            }
        });
    }

    private boolean notificationsAllowed() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= 24) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            return nm.areNotificationsEnabled();
        }
        return true;
    }

    private void answerPermission(final boolean granted) {
        runOnUiThread(new Runnable() {
            public void run() {
                web.evaluateJavascript("window.__notifRes && window.__notifRes('" + (granted ? "granted" : "denied") + "')", null);
            }
        });
    }

    class Bridge {
        @JavascriptInterface
        public boolean isDark() {
            return isDarkMode();
        }

        @JavascriptInterface
        public void setBars(final String color, final boolean dark) {
            runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        int c = Color.parseColor(color);
                        web.setBackgroundColor(c);
                        applyBars(c, dark);
                    } catch (Exception ignored) { }
                }
            });
        }

        @JavascriptInterface
        public boolean notificationsAllowed() {
            return MainActivity.this.notificationsAllowed();
        }

        @JavascriptInterface
        public void requestNotificationPermission() {
            if (MainActivity.this.notificationsAllowed()) {
                answerPermission(true);
            } else if (Build.VERSION.SDK_INT >= 33) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, REQ_NOTIF);
                    }
                });
            } else {
                // notificações desligadas nas configurações do sistema: não dá para pedir de dentro do app
                answerPermission(false);
            }
        }

        @JavascriptInterface
        public void notify(String title, String body) {
            if (!MainActivity.this.notificationsAllowed()) return;
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= 26) {
                nm.createNotificationChannel(new NotificationChannel(CHANNEL, "Avisos de prazo", NotificationManager.IMPORTANCE_DEFAULT));
            }
            Intent open = new Intent(MainActivity.this, MainActivity.class);
            open.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pi = PendingIntent.getActivity(MainActivity.this, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Notification.Builder b = Build.VERSION.SDK_INT >= 26
                    ? new Notification.Builder(MainActivity.this, CHANNEL)
                    : new Notification.Builder(MainActivity.this);
            b.setSmallIcon(R.drawable.ic_stat)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(new Notification.BigTextStyle().bigText(body))
                    .setContentIntent(pi)
                    .setAutoCancel(true);
            nm.notify(1, b.build());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQ_NOTIF) {
            answerPermission(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
        }
    }
}
