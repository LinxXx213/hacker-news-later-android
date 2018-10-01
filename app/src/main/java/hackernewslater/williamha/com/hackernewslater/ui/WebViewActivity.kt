package hackernewslater.williamha.com.hackernewslater.ui

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import hackernewslater.williamha.com.hackernewslater.R

/**
 * Created by williamha on 8/6/18.
 */
class WebViewActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.webview_activity)

        val webview = findViewById<WebView>(R.id.webview)
        val url = intent.getStringExtra("url")
        url?.let {
            webview.loadUrl(it)
            webview.setBackgroundColor(Color.TRANSPARENT)
            webview.settings.javaScriptEnabled = true

            webview.webViewClient = object: WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return super.shouldOverrideUrlLoading(view, request)
                }
            }
        }
    }
}