package com.keetab.api;

import java.io.File;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import com.keetab.AppContext;
import com.keetab.util.DirectoryManager;
import com.keetab.util.JSONStorage;

public class DownloadPublication implements Runnable {
    static AppContext ctx = AppContext.instance;
    //static StatisticEventLog eventLog = new StatisticEventLog();
    
    // The purchase this download is started with
    private JSONObject purchase;
    private String id;
    private String title;
    private File epub;
    
    // These values are used to add product to library after download
    private JSONObject product;
    
    // The id under which this download can be tracked
    private long downloadId;

    public DownloadPublication(JSONObject purchase) {
        this.purchase = purchase;
    }

    @Override
    public void run() {
        if (purchase.containsKey("product")) {
            product = (JSONObject)purchase.get("product");
            id = product.get("id").toString();
            title = product.get("title").toString();
            downloadPublication(purchase);
        }
    }

    public void downloadPublication(JSONObject purchase) {
        String epubUrl = getAssetUrlByType(purchase, "epub");
        
        File libraryDir = DirectoryManager.getLibraryDir();
        epub = new File(libraryDir, id + ".epub");
        if (epub.exists())
            epub.delete();

        downloadWithManager(epubUrl, epub);
    }
	
	private void downloadWithManager(String url, File toFile) {
        DownloadManager.Request request = 
                new DownloadManager.Request(Uri.parse(url));
        request.setTitle("Downloading " + title);
        request.setMimeType("application/epub+zip");
        request.setVisibleInDownloadsUi(false);
        request.setAllowedOverRoaming(false);
        request.setDestinationUri(Uri.fromFile(toFile));

        DownloadManager manager = 
                (DownloadManager)ctx.getSystemService(Context.DOWNLOAD_SERVICE);
        
        ctx.registerReceiver(new DownloadCompleteReceiver(), 
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        
        downloadId = manager.enqueue(request);
    }
	
	private final class DownloadCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            if (id == downloadId) {
                addProductToLibrary(product, epub);
            }
        }
    }
	
    public void addProductToLibrary(JSONObject product, File epub) {
        JSONStorage storage = new JSONStorage();
        storage.add("publications", product);
    }

    private String getAssetUrlByType(JSONObject product, String type) {
        String url = null;
        JSONArray assets = (JSONArray)product.get("assets");
        for (Object object : assets) {
            if (object instanceof JSONObject) {
                JSONObject asset = (JSONObject) object;
                if (asset.containsKey("type") && 
                		asset.get("type").toString().equals(type)) {
                    url = asset.get("__uri__").toString();
                }
            }
        }
        return url;
    }
}
