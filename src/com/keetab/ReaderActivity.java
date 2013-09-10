package com.keetab;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.readium.sdk.android.Container;
import org.readium.sdk.android.ManifestItem;
import org.readium.sdk.android.Package;
import org.readium.sdk.android.SpineItem;
import org.readium.sdk.android.components.navigation.NavigationElement;
import org.readium.sdk.android.components.navigation.NavigationPoint;
import org.readium.sdk.android.components.navigation.NavigationTable;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.VideoView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.keetab.api.Cover;
import com.keetab.library.Publication;
import com.keetab.model.BookmarkDatabase;
import com.keetab.model.OpenPageRequest;
import com.keetab.model.ReadiumJSApi;
import com.keetab.model.ViewerSettings;
import com.keetab.util.StringListAdapter;
import com.keetab.util.TouchListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;


public class ReaderActivity extends ActionBarActivity implements ViewerSettingsDialog.OnViewerSettingsChange {
	private static final String TAG = "ReaderActivity";
	private static final String ASSET_PREFIX = "file:///android_asset/readium-shared-js/";
	private static final String READER_SKELETON = "file:///android_asset/readium-shared-js/reader.html";
	
	WebView webview;
	ImageButton settingsButton;
	SlidingMenu tocMenu;
	
	Container container;
	Package pckg;
	OpenPageRequest openPageRequest;
	ViewerSettings viewerSettings;
	ReadiumJSApi readiumJSApi;
	ActionBar actionBar;
	
	private Boolean inFullscreen = false;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.activity_reader);

		webview = (WebView)findViewById(R.id.webview);
		initWebView();
	
	    Publication pub = (Publication)getIntent().getSerializableExtra("pub"); 
	    container = pub.getContainer();
	    pckg = container.getDefaultPackage();
	    
	    // Open first page
	    SpineItem spineItem = pckg.getSpineItems().get(0);
	    openPageRequest = OpenPageRequest.fromIdref(spineItem.getIdRef());
	    
	    JSONObject meta = pub.getMeta();
	    setTitle(meta.get("title").toString());
	    
	    actionBar = getSupportActionBar();
	    String id = meta.get("id").toString();
        String coverURL = Cover.getCoverURL(id, 50, 50);
        ImageLoader.getInstance().loadImage(coverURL, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                BitmapDrawable drawable = new BitmapDrawable(getResources(), loadedImage);
                actionBar.setIcon(drawable);
            }
        });
	    
        
        tocMenu = new SlidingMenu(this);
        tocMenu.setMode(SlidingMenu.LEFT);
        tocMenu.setTouchmodeMarginThreshold(40);
//        menu.setShadowWidthRes(R.dimen.shadow_width);
//        menu.setShadowDrawable(R.drawable.shadow);
//        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        tocMenu.setBehindOffset(300);
        tocMenu.setFadeDegree(0.35f);
        tocMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        tocMenu.setMenu(R.layout.toc_list);
        
        ListView tocList = (ListView)tocMenu.findViewById(R.id.tocList);
        NavigationTable toc = pckg.getTableOfContents();
        setListViewContent(tocList, toc);

        
		webview.loadUrl(READER_SKELETON);
		viewerSettings = new ViewerSettings(false, 100, 20);
		readiumJSApi = new ReadiumJSApi(new ReadiumJSApi.JSLoader() {
			@Override
			public void loadJS(String javascript) {
				webview.loadUrl(javascript);
			}
		});
	}
	
	protected void setListViewContent(ListView view, final NavigationTable navigationTable) {
        List<String> list = flatNavigationTable(navigationTable, new ArrayList<String>(), "");
        final List<NavigationElement> navigationElements = flatNavigationTable(navigationTable, new ArrayList<NavigationElement>());
        StringListAdapter bookListAdapter = new StringListAdapter(this, list);
        view.setAdapter(bookListAdapter);
        view.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                NavigationElement navigation = navigationElements.get(arg2);
                if (navigation instanceof NavigationPoint) {
                    NavigationPoint point = (NavigationPoint) navigation;
                    String content = point.getContent();
                    String sourceHref = navigationTable.getSourceHref();
                    readiumJSApi.openContentUrl(content, sourceHref);
                    tocMenu.showContent();
                }
            }
        });
    }

    private List<String> flatNavigationTable(NavigationElement parent, List<String> list, String shift) {
        String newShift = shift + "   ";
        for (NavigationElement ne : parent.getChildren()) {
            list.add(shift + ne.getTitle()+" ("+ne.getChildren().size()+")");
            flatNavigationTable(ne, list, newShift);
        }
        return list;
    }

    private List<NavigationElement> flatNavigationTable(NavigationElement parent, List<NavigationElement> list) {
        for (NavigationElement ne : parent.getChildren()) {
            list.add(ne);
            flatNavigationTable(ne, list);
        }
        return list;
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

    private void updateSettings(ViewerSettings viewerSettings) {
        this.viewerSettings = viewerSettings;
        readiumJSApi.updateSettings(viewerSettings);
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
        } else if (id == R.id.fullscreen) {
            toggleFullscreen();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
   
    public void toggleFullscreen() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (inFullscreen) {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
            inFullscreen = false;
        } else {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            inFullscreen = true;
        }
        getWindow().setAttributes(attrs);
    }
   
	@SuppressWarnings("deprecation")
	@SuppressLint("SetJavaScriptEnabled")
	private void initWebView() {
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setAllowUniversalAccessFromFileURLs(true);
		webview.getSettings().setLightTouchEnabled(true);
		webview.getSettings().setPluginState(WebSettings.PluginState.ON);
		webview.setWebViewClient(new EpubWebViewClient());
		webview.setWebChromeClient(new EpubWebChromeClient());
		webview.setOnTouchListener(new SwipeActions(this));
		webview.addJavascriptInterface(new EpubInterface(), "LauncherUI");
	}
	
	
	public class SwipeActions extends TouchListener {
		
		public SwipeActions(Context ctx) {
			super(ctx);
		}

		public void onSwipeRight() {
			readiumJSApi.openPageRight();
		}
		
		public void rightTap() {
			readiumJSApi.openPageRight();
		}
		
		public void onSwipeLeft() {
			readiumJSApi.openPageLeft();
		}
		
		public void leftTap() {
			readiumJSApi.openPageLeft();
		}
		
		public void onSwipeDown() {
		    actionBar.show();
		}
		
		public void onSwipeUp() {
		    actionBar.hide();
		}
	};
    

    public final class EpubWebViewClient extends WebViewClient {
    	
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
        	Log.i(TAG, "onPageStarted: "+url);
        }
        
        @Override
        public void onPageFinished(WebView view, String url) {
        	Log.i(TAG, "onPageFinished: "+url);
        	if (url.equals(READER_SKELETON)) {
        	    readiumJSApi.openBook(pckg, viewerSettings, openPageRequest);
        	    readiumJSApi.loadJSOnReady("console.log('ready')");
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
        	String cleanedUrl = cleanResourceUrl(url);
            byte[] data = pckg.getContent(cleanedUrl);
        	return new WebResourceResponse(null, "utf-8", new ByteArrayInputStream(data));
        }
        
        private String cleanResourceUrl(String url) {
        	String cleanUrl = url.replace(ASSET_PREFIX, "");
        	return (cleanUrl.startsWith(pckg.getBasePath())) ? 
        			cleanUrl.replaceFirst(pckg.getBasePath(), "") : cleanUrl;
        }
    }
    
    public class EpubWebChromeClient extends WebChromeClient implements
    	MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    	public void onShowCustomView(View view, CustomViewCallback callback) {
			Log.d(TAG, "here in on ShowCustomView: " + view);
			super.onShowCustomView(view, callback);
			if (view instanceof FrameLayout) {
				FrameLayout frame = (FrameLayout) view;
				Log.d(TAG, "frame.getFocusedChild(): " + frame.getFocusedChild());
				if (frame.getFocusedChild() instanceof VideoView) {
					VideoView video = (VideoView) frame.getFocusedChild();
					// frame.removeView(video);
					// a.setContentView(video);
					video.setOnCompletionListener(this);
					video.setOnErrorListener(this);
					video.start();
				}
			}
		}
    	
    	public void onCompletion(MediaPlayer mp) {
			Log.d(TAG, "Video completed");
		}

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			Log.d(TAG, "MediaPlayer onError: " + what + ", " + extra);
			return false;
		}

	}
    
	public class EpubInterface {
		@JavascriptInterface
		public void onPaginationChanged(String currentPagesInfo) {
			// Thank you but we're not interested
		}
		
		@JavascriptInterface
		public void onSettingsApplied() {
			Log.d(TAG, "onSettingsApplied");
		}
		
		@JavascriptInterface
		public void getBookmarkData(final String bookmarkData) {
			AlertDialog.Builder builder = new AlertDialog.Builder(ReaderActivity.this).
					setTitle(R.string.add_bookmark);
	        
	        final EditText editText = new EditText(ReaderActivity.this);
	        editText.setId(android.R.id.edit);
	        editText.setHint(R.string.title);
	        builder.setView(editText);
	        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == DialogInterface.BUTTON_POSITIVE) {
						String title = editText.getText().toString();
						try {
							org.json.JSONObject bookmarkJson = new org.json.JSONObject(bookmarkData);
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
	}

}
