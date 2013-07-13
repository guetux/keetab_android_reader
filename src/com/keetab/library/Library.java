package com.keetab.library;

import java.util.LinkedList;
import java.util.List;

import com.keetab.util.DirectoryManager;

public class Library {
	
	public List<Publication> publications = new LinkedList<Publication>();
	
	public Library() {
		loadLibrary();
	}
	
	private void loadLibrary() {
		String[] files = DirectoryManager.getLibraryDir().list();
		for (String fileName : files) {
			if (fileName.endsWith(".epub")) {
				String id = fileName.substring(0, fileName.indexOf(".epub"));
				Publication pub = new Publication(id);
				publications.add(pub);
			}
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
	
	public Publication[] asArray() {
		return publications.toArray(new Publication[publications.size()]);
	}
	
}
