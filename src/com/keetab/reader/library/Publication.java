package com.keetab.reader.library;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

import org.json.simple.JSONAware;

import android.content.res.AssetManager;

import com.keetab.reader.ReaderContext;
import com.keetab.reader.util.DirectoryManager;
import com.keetab.reader.util.Unzipper;


public class Publication {
	
	private AssetManager assets = ReaderContext.instance.getAssets();
	private static final String LIBRARYDIR = "library";
	
	private String fileName;
	private Book epubInfo;
	private JSONAware bookData;
	
	public Publication(String fileName) {
		this.fileName = fileName;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public Book getEpubInfo() throws IOException {
		if (epubInfo == null) {
			EpubReader reader = new EpubReader();
			InputStream in = assets.open(LIBRARYDIR+"/"+fileName);
			epubInfo = reader.readEpub(in);
		}
		return epubInfo;
	}
	
	public JSONAware getBookData() throws IOException {
		if (bookData == null) {
			bookData = new EpubData(getEpubInfo(), fileName).parse();
		}
		return bookData;
	}
	
	public InputStream getInputStream() throws IOException {
		return assets.open(LIBRARYDIR+"/"+fileName);
	}
	
	public File extract() throws IOException {
		File cacheDir = DirectoryManager.getLibraryCacheDir();
		File epubDir = new File(cacheDir, fileName);
		if (!epubDir.exists()) {
			epubDir.mkdirs();
			ZipInputStream zis =  new ZipInputStream(getInputStream());	
			Unzipper.unzipStream(zis, epubDir);
		}
		return epubDir;
	}
}
