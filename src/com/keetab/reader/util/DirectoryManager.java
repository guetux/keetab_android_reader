package com.keetab.reader.util;

import java.io.File;

import com.keetab.reader.ReaderContext;

public class DirectoryManager {
	
	private static ReaderContext ctx = ReaderContext.instance;
	
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
