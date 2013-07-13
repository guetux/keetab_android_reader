package com.keetab.api;

public class Cover {

	static String API_URL = ApiClient.API_URL;
	
	public static String getCoverURL(String id, int width, int height) {
		return API_URL + "/cover/"+id+"/"+width+"x"+height+".png";
	}
	
}
