package com.keetab;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.readium.sdk.android.Container;
import org.readium.sdk.android.Package;
import org.readium.sdk.android.components.navigation.NavigationElement;
import org.readium.sdk.android.components.navigation.NavigationPoint;
import org.readium.sdk.android.components.navigation.NavigationTable;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.keetab.api.Cover;
import com.keetab.library.Publication;
import com.keetab.model.Page;
import com.keetab.model.PaginationInfo;
import com.keetab.model.ViewerSettings;
import com.keetab.util.JSONStorage;
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
	SlidingMenu menus;
	ListView tocList;
	ListView bookmarksList;
	
	JSONStorage storage;
	
	Publication publication;
	Container container;
	Package pckg;
	String openPageRequest;
	ViewerSettings viewerSettings;
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
	
		storage = new JSONStorage();
		
	    publication = (Publication)getIntent().getSerializableExtra("pub"); 
	    container = publication.getContainer();
	    pckg = container.getPackages().get(0);
	    
	    JSONObject meta = publication.getMeta();
	    setTitle(meta.get("title").toString());
	    
		if (meta.containsKey("position")) {
			openPageRequest = meta.get("position").toString();
		}
	    
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
	    
        initSlidingMenus();
        
		webview.loadUrl(READER_SKELETON);
		viewerSettings = new ViewerSettings(false, 100, 20);
	}
	
	@Override
	public void onBackPressed() {
		loadJS("window.LauncherUI.getCurrentPage(ReadiumSDK.reader.bookmarkCurrentPage());");
		finish();
	}
	
	
	private void initSlidingMenus() {
        menus = new SlidingMenu(this);
        menus.setMode(SlidingMenu.LEFT_RIGHT);
        menus.setTouchmodeMarginThreshold(40);
        menus.setBehindOffset(300);
        menus.setFadeDegree(0.35f);
        menus.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menus.setMenu(R.layout.toc_list);
        menus.setSecondaryMenu(R.layout.bookmarks);
        
        tocList = (ListView)menus.findViewById(R.id.tocList);
        bookmarksList = (ListView)menus.findViewById(R.id.bookmarksList);
        
        initTocMenu();
        initBookmarksMenu();
	}

	
	private void initBookmarksMenu() {
		List<String> titles = getBookmarkTitles();
		final StringListAdapter bookmarkAdapter = 
				new StringListAdapter(this, titles);
		bookmarksList.setAdapter(bookmarkAdapter);
		
		final Context context = this;
		
		bookmarksList.setOnItemClickListener(
				new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int index, long arg3) {
						openBookmark(index);
					}
		});
		
		bookmarksList.setLongClickable(true);
		bookmarksList.setOnItemLongClickListener(
			new AdapterView.OnItemLongClickListener() {
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						final int index, long arg3) {
					
					String bookmark = bookmarkAdapter.getItem(index).toString();
					
					new AlertDialog.Builder(context)
					.setMessage("Delete bookmark " + bookmark + "?")
					.setPositiveButton(android.R.string.yes, 
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								removeBookmark(index);
								bookmarkAdapter.notifyDataSetChanged();
							}
						}
					)
					.setNegativeButton(android.R.string.no, null)
					.show();
					return true;
				}
		});
	}
	
	private JSONArray getBookmarks() {
		JSONObject meta = publication.getMeta();
		String bookmarksData = meta.get("bookmarks").toString();
		try {
			return (JSONArray)new JSONParser().parse(bookmarksData);
		} catch (ParseException pe) {
			return new JSONArray();
		}
	}
	
	private List<String> getBookmarkTitles() {
		JSONArray bookmarks = getBookmarks();
		List<String> titles = new LinkedList<String>();
		for (Object o : bookmarks) {
			JSONObject bookmark = (JSONObject)o;
			String title = (String)bookmark.get("title");
			if (title != null) {
				titles.add(title);
			}
		}
		return titles;
	}
	
	private void openBookmark(int index) {
		JSONArray bookmarks = getBookmarks();
		try {
			JSONObject bookmark = (JSONObject)bookmarks.get(index);
			String idRef = (String)bookmark.get("idref");
			String elementCfi = (String)bookmark.get("contentCFI");
			openSpineItemElementCfi(idRef, elementCfi);
			menus.showContent();
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}
	
	private void removeBookmark(int index) {
		JSONArray bookmarks = getBookmarks();
		bookmarks.remove(index);
		JSONObject meta = publication.getMeta();
		meta.put("bookmarks", bookmarks);
		storage.update(meta);
		updateBookmarks();
	}
	

	
	private void initTocMenu() {
		final NavigationTable navigationTable = pckg.getTableOfContents();
        List<String> list = flatNavigationTable(navigationTable, new ArrayList<String>(), "");
        final List<NavigationElement> navigationElements = flatNavigationTable(navigationTable, new ArrayList<NavigationElement>());
        StringListAdapter bookListAdapter = new StringListAdapter(this, list);
        tocList.setAdapter(bookListAdapter);
        tocList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                NavigationElement navigation = navigationElements.get(arg2);
                if (navigation instanceof NavigationPoint) {
                    NavigationPoint point = (NavigationPoint) navigation;
                    String content = point.getContent();
                    String sourceHref = navigationTable.getSourceHref();
                    openContentUrl(content, sourceHref);
                    menus.showContent();
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
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       getMenuInflater().inflate(R.menu.reader, menu);
       return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.bookmark) {
        	bookmarkCurrentPage();
        	return true;
        } else if (id == R.id.show_settings) {
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
		
		public void onSwipeDown() {
		    actionBar.show();
		}
		
		public void onSwipeUp() {
		    actionBar.hide();
		}
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
        		if (openPageRequest != null) {
        			openBook(pckg.toJSON(), openPageRequest);
        		} else {
        			openBook(pckg.toJSON());
        		}
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
    
    public void saveCurrentLocation(String currentPage) {
    	JSONObject meta = publication.getMeta();
    	meta.put("position", currentPage);
    	storage.update(meta);
    }
    
	public class EpubInterface {
		@JavascriptInterface
		public void onPaginationChanged(String currentPagesInfo) {
			try {
				PaginationInfo paginationInfo = PaginationInfo.fromJson(currentPagesInfo);
				List<Page> openPages = paginationInfo.getOpenPages();
				// Thank you but we're not interested
			} catch (JSONException e) {
				Log.e(TAG, ""+e.getMessage(), e);
			}
		}
		
		@JavascriptInterface
		public void getCurrentPage(final String currentPage) {
			saveCurrentLocation(currentPage);
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
						saveBookmark(title, bookmarkData);
					}
				}
			});
	        builder.setNegativeButton(android.R.string.cancel, null);
	        builder.show();
		}
		
		public void saveBookmark(String title, String bookmarkData) {
			try {
				JSONParser p = new JSONParser();
				JSONObject bookmark = (JSONObject)p.parse(bookmarkData);
				bookmark.put("title", title);
				
				JSONObject meta = publication.getMeta();
				JSONArray bookmarks;
				if (meta.containsKey("bookmarks")) {
					String bookmarksData = meta.get("bookmarks").toString();
					bookmarks = (JSONArray)p.parse(bookmarksData);
				} else {
					bookmarks = new JSONArray();
				}
				
				bookmarks.add(bookmark);
				
				meta.put("bookmarks", bookmarks.toJSONString());
				storage.update(meta);
				updateBookmarks();
			} catch (ParseException pe) {
				pe.printStackTrace();
			}
		}
	}
	
	public void updateBookmarks() {
		ListView list = (ListView)menus.findViewById(R.id.bookmarksList);
		List<String> titles = getBookmarkTitles();
		StringListAdapter adapter = (StringListAdapter)list.getAdapter();
		adapter.changeData(titles);
	}

}
