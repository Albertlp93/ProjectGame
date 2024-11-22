package com.projectgame.projectgame;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Habilita JavaScript si es necesario

        webView.setWebViewClient(new WebViewClient()); // Para que el contenido se cargue dentro de la app

        // Carga contenido local o remoto
        webView.loadUrl("file:///android_asset/help.html"); // Archivo local
        // webView.loadUrl("https://tu-sitio-de-ayuda.com"); // URL remota
    }

    @Override
    public void onBackPressed() {
        // Si se puede retroceder en el historial del WebView, hazlo
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed(); // De lo contrario, sal de la actividad
        }
    }
}
