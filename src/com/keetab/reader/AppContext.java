package com.keetab.reader;

import android.app.Application;
import ch.bazaruto.Bazaruto;

import com.keetab.reader.library.Library;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class AppContext  extends Application {

	public static AppContext instance;
	
	public static Library library;
	public static Bazaruto server;
	
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        library = new Library();
        configureImageLoader();
    }
    
    private void configureImageLoader() {
        DisplayImageOptions defaultOptions = 
            	new DisplayImageOptions.Builder()
    				.cacheInMemory(true)
    				.cacheOnDisc(true)
    				.build();
        ImageLoaderConfiguration config = 
        	new ImageLoaderConfiguration.Builder(this)
	        	.defaultDisplayImageOptions(defaultOptions)
	        	.build();
        ImageLoader.getInstance().init(config);
    }
    

}
