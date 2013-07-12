package com.keetab.reader;

import android.app.Application;
import ch.bazaruto.Bazaruto;

import com.keetab.reader.library.Library;

public class AppContext  extends Application {

	public static AppContext instance;
	
	public static Library library;
	public static Bazaruto server;
	
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        library = new Library();
    }
    

}
