package com.example.ecommerce.payment;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.ecommerce.R;

public class VNPayActivity extends AppCompatActivity {

    private WebView webView;
    private String paymentUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vnpay);

        // Get payment URL from intent
        paymentUrl = getIntent().getStringExtra("PAYMENT_URL");
        if (paymentUrl == null || paymentUrl.isEmpty()) {
            finish();
            return;
        }

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("VNPAY Payment");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Setup WebView
        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        // Setup WebViewClient to handle redirects
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Check if the URL is our return URL
                if (url.startsWith(VNPayHelper.VNP_RETURN_URL)) {
                    handleVNPayCallback(url);
                    return true;
                }
                // Load the URL in WebView
                return false;
            }
        });

        // Load payment URL
        webView.loadUrl(paymentUrl);
    }

    private void handleVNPayCallback(String url) {
        // Parse the URL to get response parameters
        Intent resultIntent = new Intent();
        resultIntent.putExtra("VNPAY_RESPONSE", url);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}