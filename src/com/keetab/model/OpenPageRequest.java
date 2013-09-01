package com.keetab.model;

import org.json.JSONException;
import org.json.JSONObject;

public class OpenPageRequest {

	private final String idref;
	private final Integer spineItemPageIndex;
	private final String elementCfi;
	private final String contentRefUrl;
	private final String sourceFileHref;
	
	public static OpenPageRequest fromIdref(String idref) {
		return new OpenPageRequest(idref, 0, null, null, null);
	}
	
	public static OpenPageRequest fromIdrefAndIndex(String idref, int spineItemPageIndex) {
		return new OpenPageRequest(idref, spineItemPageIndex, null, null, null);
	}
	
	public static OpenPageRequest fromIdrefAndCfi(String idref, String elementCfi) {
		return new OpenPageRequest(idref, null, elementCfi, null, null);
	}
	
	public static OpenPageRequest fromContentUrl(String contentRefUrl, String sourceFileHref) {
		return new OpenPageRequest(null, null, null, contentRefUrl, sourceFileHref);
	}
	
	private OpenPageRequest(String idref, Integer spineItemPageIndex,
			String elementCfi, String contentRefUrl, String sourceFileHref) {
		this.idref = idref;
		this.spineItemPageIndex = spineItemPageIndex;
		this.elementCfi = elementCfi;
		this.contentRefUrl = contentRefUrl;
		this.sourceFileHref = sourceFileHref;
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("idref", idref);
		json.put("spineItemPageIndex", spineItemPageIndex);
		json.put("elementCfi", elementCfi);
		json.put("contentRefUrl", contentRefUrl);
		json.put("sourceFileHref", sourceFileHref);
		return json;
	}
}
