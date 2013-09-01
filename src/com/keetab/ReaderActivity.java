package com.keetab;

import java.io.ByteArrayInputStream;

import org.json.JSONException;
import org.readium.sdk.android.Container;
import org.readium.sdk.android.Package;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import com.keetab.library.Publication;
import com.keetab.model.ViewerSettings;
import com.keetab.util.TouchListener;


public class ReaderActivity extends FragmentActivity implements ViewerSettingsDialog.OnViewerSettingsChange {
	private static final String TAG = "ReaderActivity";
	private static final String ASSET_PREFIX = "file:///android_asset/readium-shared-js/";
	private static final String READER_SKELETON = "file:///android_asset/readium-shared-js/reader.html";

	WebView webview;
	ImageButton settingsButton;
	
	Package pckg;
	ViewerSettings viewerSettings;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_reader);

		webview = (WebView)findViewById(R.id.webview);
		initWebView();
	
	    Publication pub = (Publication)getIntent().getSerializableExtra("pub"); 
	    Container container = pub.getContainer();
	    pckg = container.getPackages().get(0);
	    
		webview.loadUrl(READER_SKELETON);
		viewerSettings = new ViewerSettings(false, 100, 20);
	}
	
	public void showSettings() {
	    FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        DialogFragment dialog = new ViewerSettingsDialog(this, viewerSettings);
        dialog.show(fm, "dialog");
        fragmentTransaction.commit();
	}
	
    @Override
    public void onViewerSettingsChange(ViewerSettings viewerSettings) {
        updateSettings(viewerSettings);
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       getMenuInflater().inflate(R.menu.reader, menu);
       return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.show_settings) {
            showSettings();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
   
   
	@SuppressLint("SetJavaScriptEnabled")
	private void initWebView() {
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setAllowUniversalAccessFromFileURLs(true);

		webview.getSettings().setLightTouchEnabled(true);
		webview.getSettings().setPluginState(WebSettings.PluginState.ON);
		webview.setWebViewClient(new EpubWebViewClient());
		webview.setWebChromeClient(new EpubWebChromeClient());
		webview.setOnTouchListener(new SwipeActions(this));
		//webview.addJavascriptInterface(new EpubInterface(), "LauncherUI");
	}
	
	
	public class SwipeActions extends TouchListener {
		
		public SwipeActions(Context ctx) {
			super(ctx);
		}

		public void onSwipeRight() {
			openPageRight();
		}
		
		public void rightTap() {
			openPageRight();
		}
		
		public void onSwipeLeft() {
			openPageLeft();
		}
		
		public void leftTap() {
			openPageLeft();
		}
		
		public void onSwipeDown() {}
		
		public void onSwipeUp() {}
	};
	
	private void bookmarkCurrentPage() {
		loadJS("window.LauncherUI.getBookmarkData(ReadiumSDK.reader.bookmarkCurrentPage());");
	}
	
	private void openPageLeft() {
		loadJS("ReadiumSDK.reader.openPageLeft();");
	}
	
	private void openPageRight() {
		loadJS("ReadiumSDK.reader.openPageRight();");
	}
	
	private void openBook(String packageData) {
		Log.i(TAG, "packageData: "+packageData);
		loadJSOnReady("ReadiumSDK.reader.openBook("+packageData+");");
	}
	
	private void openBook(String packageData, String openPageRequest) {
		Log.i(TAG, "packageData: "+packageData);
		loadJSOnReady("ReadiumSDK.reader.openBook("+packageData+", "+openPageRequest+");");
	}
	
    private void updateSettings(ViewerSettings viewerSettings) {
        Log.i(TAG, "viewerSettings: "+viewerSettings);
        this.viewerSettings = viewerSettings;
        try {
            loadJSOnReady("ReadiumSDK.reader.updateSettings("+viewerSettings.toJSON().toString()+");");
        } catch (JSONException e) {
            Log.e(TAG, ""+e.getMessage(), e);
        }
    }
	
	private void openContentUrl(String href, String baseUrl) {
		loadJSOnReady("ReadiumSDK.reader.openContentUrl(\""+href+"\", \""+baseUrl+"\");");
	}
	
	private void openSpineItemPage(String idRef, int page) {
		loadJSOnReady("ReadiumSDK.reader.openSpineItemPage(\""+idRef+"\", "+page+");");
	}

	private void openSpineItemElementCfi(String idRef, String elementCfi) {
		loadJSOnReady("ReadiumSDK.reader.openSpineItemElementCfi(\""+idRef+"\",\""+elementCfi+"\");");
	}

    private void loadJSOnReady(String jScript) {
        loadJS("$(document).ready(function () {" + jScript + "});");
    }

    private void loadJS(String jScript) {
        webview.loadUrl("javascript:(function(){" + jScript + "})()");
    }
    
    public final class EpubWebViewClient extends WebViewClient {
    	
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
        	Log.i(TAG, "onPageStarted: "+url);
        }
        
        @Override
        public void onPageFinished(WebView view, String url) {
        	Log.i(TAG, "onPageFinished: "+url);
        	if (url.equals(READER_SKELETON)) {
        	    openBook(pckg.toJSON());
        	    updateSettings(viewerSettings);
        	}
        }
        
        @Override
        public void onLoadResource(WebView view, String url) {
        	Log.i(TAG, "onLoadResource: "+url);
        	byte[] data = pckg.getContent(cleanResourceUrl(url));
            if (data.length > 0) {
            	Log.i(TAG, "Load : "+url);
                // TODO Pass the correct mimetype
            	webview.loadData(new String(data), null, "utf-8");
            }
        }
        
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
        	Log.i(TAG, "shouldOverrideUrlLoading: "+url);
    		return false;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        	Log.i(TAG, "shouldInterceptRequest ? "+url);

            byte[] data = pckg.getContent(cleanResourceUrl(url));
        	//Log.i(TAG, "data : "+new String(data));
            // TODO Pass the correct mimetype
        	return new WebResourceResponse(null, "utf-8", new ByteArrayInputStream(data));
        }
        
        private String cleanResourceUrl(String url) {
        	String cleanUrl = url.replace(ASSET_PREFIX, "");
        	return (cleanUrl.startsWith(pckg.getBasePath())) ? 
        			cleanUrl.replaceFirst(pckg.getBasePath(), "") : cleanUrl;
        }
    }
    
    public class EpubWebChromeClient extends WebChromeClient {

	}
    
	public class EpubInterface {
		/*
		@JavascriptInterface
		public void onPaginationChanged(String currentPagesInfo) {
			try {
				PaginationInfo paginationInfo = PaginationInfo.fromJson(currentPagesInfo);
				List<Page> openPages = paginationInfo.getOpenPages();
				if (!openPages.isEmpty()) {
					final Page page = openPages.get(0);
					runOnUiThread(new Runnable() {
						public void run() {
							pageInfo.setText(getString(R.string.page_x_of_y,
									page.getSpineItemPageIndex() + 1,
									page.getSpineItemPageCount()));
						}
					});
				}
			} catch (JSONException e) {
				Log.e(TAG, ""+e.getMessage(), e);
			}
		}
		
		@JavascriptInterface
		public void getBookmarkData(final String bookmarkData) {
			AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this).
					setTitle(R.string.add_bookmark);
	        
	        final EditText editText = new EditText(WebViewActivity.this);
	        editText.setId(android.R.id.edit);
	        editText.setHint(R.string.title);
	        builder.setView(editText);
	        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == DialogInterface.BUTTON_POSITIVE) {
						String title = editText.getText().toString();
						try {
							JSONObject bookmarkJson = new JSONObject(bookmarkData);
							BookmarkDatabase.getInstance().addBookmark(container.getName(), title,
									bookmarkJson.getString("idref"), bookmarkJson.getString("contentCFI"));
						} catch (JSONException e) {
							Log.e(TAG, ""+e.getMessage(), e);
						}
					}
				}
			});
	        builder.setNegativeButton(android.R.string.cancel, null);
	        builder.show();
		}
		*/
	}

}
