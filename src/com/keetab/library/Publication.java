package com.keetab.library;

import java.io.File;
import java.io.Serializable;

import org.json.simple.JSONObject;
import org.readium.sdk.android.Container;
import org.readium.sdk.android.EPub3;

import com.keetab.util.DirectoryManager;
import com.keetab.util.JSONStorage;


public class Publication implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String id;
	private String fileName;
	private transient JSONObject meta;
	
	public Publication(String id) {
		this.id = id;
		this.fileName = id + ".epub";
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public JSONObject getMeta() {
		if (meta == null) {
			JSONStorage storage = new JSONStorage();
			meta = storage.get("publications", id);
		}
		return meta;
	}
	
	
	public Container getContainer() {
		File libraryDir = DirectoryManager.getLibraryDir();
		File epub = new File(libraryDir, fileName);
		String path = epub.getAbsolutePath();
        Container container = EPub3.openBook(path);
        
        return container;
	}
}
