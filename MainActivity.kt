package com.motionstudio.app

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.webkit.WebViewAssetLoader

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private var pendingFiles: ValueCallback<Array<Uri>>? = null

    // Result of the native file picker (video / image / audio import)
    private val picker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        var uris: Array<Uri>? = null
        if (result.resultCode == RESULT_OK && data != null) {
            val clip = data.clipData
            val single = data.data
            uris = when {
                clip != null -> Array(clip.itemCount) { clip.getItemAt(it).uri }
                single != null -> arrayOf(single)
                else -> null
            }
        }
        pendingFiles?.onReceiveValue(uris)
        pendingFiles = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Serves the bundled index.html from a secure virtual origin, so IndexedDB,
        // localStorage and media playback all behave like a normal website.
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        webView = WebView(this)
        setContentView(webView)
        webView.setBackgroundColor(0xFF000000.toInt())

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = false
            allowContentAccess = false
            builtInZoomControls = false
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                return request?.url?.let { assetLoader.shouldInterceptRequest(it) }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                view: WebView?,
                callback: ValueCallback<Array<Uri>>?,
                params: WebChromeClient.FileChooserParams?
            ): Boolean {
                pendingFiles?.onReceiveValue(null)
                pendingFiles = callback
                return try {
                    val intent = params?.createIntent()
                        ?: Intent(Intent.ACTION_GET_CONTENT).setType("*/*")
                    picker.launch(intent)
                    true
                } catch (e: Exception) {
                    pendingFiles = null
                    callback?.onReceiveValue(null)
                    false
                }
            }
        }

        webView.addJavascriptInterface(DownloadBridge(), "AndroidBridge")
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html")

        // Back button: inside the editor go to the project list; on the list, exit the app.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                webView.evaluateJavascript(
                    "(function(){if(window.PID){goHome();return 1}return 0})()"
                ) { result ->
                    if (result != "1") {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })
    }

    // Saves exported videos to the phone's Downloads folder.
    inner class DownloadBridge {
        @JavascriptInterface
        fun saveFile(fileName: String, mimeType: String, base64Data: String) {
            runOnUiThread {
                try {
                    val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                    val mime = if (mimeType.isBlank()) "application/octet-stream" else mimeType
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, mime)
                        put(MediaStore.Downloads.IS_PENDING, 1)
                    }
                    val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                        ?: throw IllegalStateException("ফাইল তৈরি করা যায়নি")
                    contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                    values.clear()
                    values.put(MediaStore.Downloads.IS_PENDING, 0)
                    contentResolver.update(uri, values, null, null)
                    Toast.makeText(this@MainActivity, "Downloads-এ সেভ হয়েছে: $fileName", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "সেভ হয়নি: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
