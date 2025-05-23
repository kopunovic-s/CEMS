package com.example.companyloginapp;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class PdfViewerActivity extends AppCompatActivity {

    public static final String EXTRA_PDF_URL = "pdf_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        String pdfUrl = getIntent().getStringExtra(EXTRA_PDF_URL);
        webView.loadUrl("https://docs.google.com/gview?embedded=true&url=" + pdfUrl);
    }
}

