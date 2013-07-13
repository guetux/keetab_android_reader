package com.keetab.util;

import java.io.File;

import com.keetab.AppContext;

public class DirectoryManager {
	
	private static AppContext ctx = AppContext.instance;
	
	public static File getLibraryDir() {
		File libraryDir = new File(ctx.getExternalFilesDir(null), "library");
		if (!libraryDir.exists())
			libraryDir.mkdirs();
		return libraryDir;
	}
	
	public static File getLibraryCacheDir() {
		File libraryCache = new File(ctx.getExternalCacheDir(), "library");
		if (!libraryCache.exists())
			libraryCache.mkdirs();
		return libraryCache;
	}
	
	public static File getReaderDir() {
		File readerDir = new File(ctx.getExternalCacheDir(), "reader");
		if (!readerDir.exists())
			readerDir.mkdirs();
		return readerDir;
	}
}
