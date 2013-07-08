package com.keetab.reader.library;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.content.res.AssetManager;
import android.util.Log;

import com.keetab.reader.ReaderContext;

public class Library {

	public static final String LIBRARY = "library";
	public static final String TAG = "Library";
	
	public List<Publication> publications = new LinkedList<Publication>();
	
	private AssetManager assets = ReaderContext.instance.getAssets();
	
	public Library() {
		loadLibrary();
	}
	
	
	private void loadLibrary() {
		try {
			String[] files = assets.list(LIBRARY);
			for (String fileName : files) {
				if (fileName.endsWith(".epub")) {
					Publication pub = new Publication(fileName);
					publications.add(pub);
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
			if (pub.getFileName().equals(fileName)) {
				return pub;
			}
		}
		return null;
	}
	
	public String[] getTitles() {
		String[] titles = new String[publications.size()];
		try {
			for (int i=0; i < publications.size(); i++) {
				titles[i] = publications.get(i).getEpubInfo().getTitle();
			}
		} catch (IOException e) {}
		return titles;
	}
	
}
