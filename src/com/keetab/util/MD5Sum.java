package com.keetab.util;

import java.io.InputStream;
import java.security.MessageDigest;

public class MD5Sum {
	
	public String getMD5Sum(InputStream in) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			
			byte[] buffer = new byte[1024];
			int read = 0;
			while ((read = in.read(buffer)) != -1) {
				md.update(buffer, 0, read);
			}
			
			byte[] mdbytes = md.digest();
			
			StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < mdbytes.length; i++) {
	        	String hex = Integer.toHexString(0xff & mdbytes[i]);
	   	     	if(hex.length()==1) sb.append('0');
	   	     	sb.append(hex);
	        }
	        
	        return sb.toString();
		} catch (Exception e) {
			return null;
		}
	}
}
