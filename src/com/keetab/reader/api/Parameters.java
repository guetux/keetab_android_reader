package com.keetab.reader.api;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

public class Parameters extends HashMap<String, String>{
	private static final long serialVersionUID = -9208325936763338720L;
	
	public static Parameters fromProperties(Properties properties) {
		Parameters result = new Parameters();
		for (Entry<Object, Object> e : properties.entrySet()) {
			result.put(e.getKey().toString(), e.getValue().toString());
		}
		return result;
	}
	
	public List<NameValuePair> toNameValuePairs() {
		List<NameValuePair> nvp = new LinkedList<NameValuePair>();
		for (Entry<String, String> e : this.entrySet()) {
			nvp.add(new BasicNameValuePair(e.getKey(), e.getValue()));
		}
		return nvp;
	}
	
	public HttpEntity toHttpEntity() {
		try {
			return new UrlEncodedFormEntity(toNameValuePairs(), HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
};
