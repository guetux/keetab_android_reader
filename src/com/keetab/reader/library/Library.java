package com.keetab.reader.library;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipInputStream;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.keetab.reader.util.DirectoryManager;
import com.keetab.reader.util.Unzipper;

public class Library {

	public static final String LIBRARY = "library";
	public static final String TAG = "Library";
	
	public List<Publication> publications = new LinkedList<Publication>();
	
	private Context ctx;
	
	public Library(Context ctx) {
		this.ctx = ctx;
		loadLibrary();
	}
	
	
	private void loadLibrary() {
		AssetManager assets = ctx.getAssets();
		EpubReader reader = new EpubReader();
		try {
			String[] files = assets.list(LIBRARY);
			for (String fileName : files) {
				if (!fileName.endsWith(".epub")) continue;
				try {
					InputStream in = open(fileName);
					Book book = reader.readEpub(in);
					
					EpubData data = new EpubData(book, fileName);
					
					Publication pub = new Publication();
					pub.fileName = fileName;
					pub.epubInfo = book;
					pub.bookData = data.parse();
					
					publications.add(pub);
				} catch (IOException e) {
					Log.e(TAG, "Could not read file" + fileName);
				}
			}
		} catch (IOException e) {
			Log.e(TAG, "Could not list library");
		}
	}
	
	public Publication findByIndex(int index) {
		try {
			return publications.get(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	public Publication findByFilename(String fileName) {
		for (Publication pub : publications) {
			if (pub.fileName.equals(fileName)) {
				return pub;
			}
		}
		return null;
	}
	
	public File extract(Publication publication) throws IOException {
		String fileName = publication.fileName;
		File cacheDir = DirectoryManager.getLibraryCacheDir();
		File epubDir = new File(cacheDir, fileName);
		if (!epubDir.exists()) {
			epubDir.mkdirs();
			ZipInputStream zis =  new ZipInputStream(open(fileName));	
			Unzipper.unzipStream(zis, epubDir);
		}
		return epubDir;
	}
	
	public InputStream open(String fileName) throws IOException {
		return ctx.getAssets().open(LIBRARY+ "/" + fileName);
	}
	
	public String[] getTitles() {
		String[] titles = new String[publications.size()];
		for (int i=0; i < publications.size(); i++) {
			titles[i] = publications.get(i).epubInfo.getTitle();
		}
		return titles;
	}
	
}
