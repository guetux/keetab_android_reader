package com.keetab.reader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.keetab.reader.util.DirectoryManager;
import com.keetab.reader.util.MD5Sum;
import com.keetab.reader.util.OnSwipeTouchListener;
import com.keetab.reader.util.Unzipper;


public class ReaderActivity extends Activity {

	WebView webView;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reader);


		webView = (WebView)findViewById(R.id.webview);

		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setDomStorageEnabled(true);
        settings.setBuiltInZoomControls(false);
        settings.setRenderPriority(RenderPriority.HIGH);

		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onConsoleMessage(ConsoleMessage cM) {
				Log.i(cM.sourceId(), cM.message());
				return true;
			}
		});

		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				Log.e(failingUrl, description);
			}
		});

		OnSwipeTouchListener swipe = new OnSwipeTouchListener(this) {
			@Override
			public void onSwipeRight() {
				webView.loadUrl("javascript:swipeRight();");
			}

			@Override
			public void onSwipeLeft() {
				webView.loadUrl("javascript:swipeLeft();");
			}
		};

		webView.setOnTouchListener(swipe);

		checkReader();

		String data = (String)getIntent().getExtras().get("data");
		webView.loadUrl("http://127.0.0.1:9090/reader/index.html?data=" + data);
	}

	private void checkReader() {
		try {
			SharedPreferences prefs = getPreferences(MODE_PRIVATE);
			String now = new MD5Sum().getMD5Sum(getAssets().open("reader.zip"));
			String have = prefs.getString("reader_version", "");
			if (!now.equals(have)) {
				extractReader();
				Editor edit = prefs.edit();
				edit.putString("reader_version", now);
				edit.commit();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void extractReader() {
		try {
			File readerDir = DirectoryManager.getReaderDir();
			InputStream is = getAssets().open("reader.zip");
			ZipInputStream rdr = new ZipInputStream(is);
			Unzipper.unzipStream(rdr, readerDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
//	        webView.reload();
//	        return true;
//	    }
//	    return super.onKeyDown(keyCode, event);
//	}
}
