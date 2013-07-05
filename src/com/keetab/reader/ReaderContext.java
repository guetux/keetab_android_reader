package com.keetab.reader;

import java.io.File;

import android.app.Application;
import ch.bazaruto.Bazaruto;
import ch.bazaruto.storage.FileStorage;

import com.keetab.reader.library.Library;
import com.keetab.reader.library.LibraryController;
import com.keetab.reader.util.DirectoryManager;

public class ReaderContext  extends Application {

	public static ReaderContext instance;
	
	public static Library library;
	public static Bazaruto server;
	
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        library = new Library(this);
        startServer();
    }
    
    private void startServer() {
    	server = new Bazaruto();
    	server.addController(LibraryController.class);
    	File readerDir = DirectoryManager.getReaderDir();
    	server.addStaticPath("^/reader/", new FileStorage(readerDir));
    	server.enableRequestLogging();
    	server.start(9090);
    }
}
