package com.keetab.library;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import com.keetab.util.DirectoryManager;
import com.keetab.util.JSONStorage;


public class Publication implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String id;
	private String fileName;
	private transient Book epubInfo;
	private transient JSONObject meta;
	private transient JSONAware bookData;
	
	public Publication(String id) {
		this.id = id;
		this.fileName = id + ".epub";
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public Book getEpubInfo() throws IOException {
		if (epubInfo == null) {
			File libraryDir = DirectoryManager.getLibraryDir();
			File epub = new File(libraryDir, fileName);
			EpubReader reader = new EpubReader();
			epubInfo = reader.readEpubLazy(epub.toString(), "UTF-8");
		}
		return epubInfo;
	}
	
	public JSONObject getMeta() {
		if (meta == null) {
			JSONStorage storage = new JSONStorage();
			meta = storage.get("publications", id);
		}
		return meta;
	}
	
	public JSONAware getBookData() throws IOException {
		if (bookData == null) {
			bookData = new EpubJSONData(getEpubInfo(), id).parse();
		}
		return bookData;
	}
	
}
