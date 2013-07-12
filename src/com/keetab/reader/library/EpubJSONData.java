package com.keetab.reader.library;

import java.io.File;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Date;
import nl.siegmann.epublib.domain.Date.Event;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.domain.TOCReference;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

public class EpubJSONData {
	
	Book epub;
	String fileName;
	
	public EpubJSONData(Book epub, String fileName) {
		this.epub = epub;
		this.fileName = fileName;
	}
	
	private JSONObject getMetaData(Book epub) {
		JSONObject meta = new JSONObject();
		Metadata data = epub.getMetadata();
		meta.put("title", epub.getTitle());
		meta.put("creator", BookHelper.getAuthors(epub));
		for (Date d: data.getDates()) {
			if (d.getEvent() == Event.PUBLICATION) {
				meta.put("publication", d.getValue());
			} else if (d.getEvent() == Event.CREATION) {
				meta.put("creation", d.getValue());
			} else if (d.getEvent() == Event.MODIFICATION)  {
				meta.put("modification", d.getValue());
			}
		}
		return meta;
	}
	
	private JSONArray getComponents(Book epub) {		
		JSONArray spine = new JSONArray();
		for (SpineReference r: epub.getSpine().getSpineReferences()) {
			if (r.isLinear()) {
				spine.add(r.getResource().getHref());
			}
		}
		return spine;
	}
	
	private JSONArray getContents(Book epub) {
		JSONArray contents = new JSONArray();
		for (TOCReference ref : epub.getTableOfContents().getTocReferences()) {
			JSONObject entry = getTOCEnty(ref);
			if (entry != null)
				contents.add(entry);
		}
		return contents;
	}
	
	private JSONObject getTOCEnty(TOCReference ref) {
		try {
			JSONObject entry = new JSONObject();
			entry.put("title", ref.getTitle());
			entry.put("src", ref.getCompleteHref());
			if (!ref.getChildren().isEmpty()) {
				JSONArray children = new JSONArray();
				for (TOCReference child : ref.getChildren())
					children.add(getTOCEnty(child));
				entry.put("children", children);
			}
			return entry;
		} catch (NullPointerException e) {
			return null;
		}
		
	}

	public JSONAware parse() {
		String location = "/library/"+ fileName + "/";
		
		File opfPath = new File(epub.getOpfResource().getHref());
		File docPath = new File(location, opfPath.getParent());
		
		JSONObject data = new JSONObject();
		data.put("doc_path", docPath.toString());
		data.put("metadata", getMetaData(epub));
		data.put("components", getComponents(epub));
		data.put("contents", getContents(epub));

		return data;
	}
}
