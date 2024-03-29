package com.keetab;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipInputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import ch.bazaruto.Bazaruto;
import ch.bazaruto.storage.FileStorage;

import com.keetab.api.Cover;
import com.keetab.library.LibraryController;
import com.keetab.library.Publication;
import com.keetab.reader.R;
import com.keetab.util.DirectoryManager;
import com.keetab.util.MD5Sum;
import com.keetab.util.OnSwipeTouchListener;
import com.keetab.util.Unzipper;
import com.nostra13.universalimageloader.core.ImageLoader;


public class ReaderActivity extends Activity {

	Bazaruto server;
	
	WebView webView;
	ImageView cover;
	ProgressBar progress;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_reader);


		webView = (WebView)findViewById(R.id.webview);
		cover = (ImageView)findViewById(R.id.cover);
		progress = (ProgressBar)findViewById(R.id.progress);

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

		final Activity that = this;
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				new Timer().schedule(new TimerTask() {
					public void run() {
						that.runOnUiThread(new Runnable() {
							public void run() {
								cover.setVisibility(View.INVISIBLE);
								progress.setVisibility(View.INVISIBLE);
							}
						});
					}
				}, 300);
			}
			
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
			
			@Override
			public void onSwipeDown() {
				webView.loadUrl("javascript:swipeDown();");
			}
			
			@Override
			public void onSwipeUp() {
				webView.loadUrl("javascript:swipeUp();");
			}
			
		};

		webView.setOnTouchListener(swipe);
		
		checkReader();
		startServer();

		Publication pub = (Publication)getIntent().getSerializableExtra("pub");
		
		String dataFile = pub.getFileName().replace(".epub", ".json");
		String dataPath = "/library/" + dataFile;
		
		int width = cover.getWidth();
		int height= cover.getHeight();
	
		String id = pub.getMeta().get("id").toString();
		String coverURL = Cover.getCoverURL(id, width, height);
		ImageLoader.getInstance().displayImage(coverURL, cover);
		
		webView.loadUrl("http://127.0.0.1:9090/reader/index.html?data=" + dataPath);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (server != null) {
			server.stop();
		}
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

    private void startServer() {
    	server = new Bazaruto();
    	server.addController(LibraryController.class);
    	File readerDir = DirectoryManager.getReaderDir();
    	server.addStaticPath("^/reader/", new FileStorage(readerDir));
    	server.enableRequestLogging();
    	server.start(9090);
    }
	
	private void extractReader() {
		try {
			File readerDir = DirectoryManager.getReaderDir();
			InputStream is = getAssets().open("reader.zip");
			ZipInputStream rdr = new ZipInputStream(is);
			Unzipper.unzipStream(rdr, readerDir);
			webView.clearCache(true);
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
