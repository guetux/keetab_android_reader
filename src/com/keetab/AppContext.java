package com.keetab;

import android.app.Application;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class AppContext  extends Application {

	public static AppContext instance;
	
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
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
