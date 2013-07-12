package com.keetab.reader.api;

import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.keetab.reader.util.JSONStorage;

public class StoreAPI {

	static ApiClient client = new ApiClient();
	static JSONStorage storage = new JSONStorage();
	
	public static List<JSONObject> getPurchasable() {
		JSONObject products = client.getJSON("product/");
	    JSONArray object = (JSONArray)products.get("objects");
	    return filterLibraryIDs(object);
	}
	
    private static List<JSONObject> filterLibraryIDs (JSONArray objects) {
        List<JSONObject> filtered = new LinkedList<JSONObject>();
        for (Object object : objects) {
            JSONObject o = (JSONObject)object;
            if (o.containsKey("id"))  {
	            String id = o.get("id").toString();
	            if (storage.get("library", id) == null)
	                filtered.add(o);
            }
        }
        return filtered;
    }
}
