package com.keetab.reader.api;

import java.io.IOException;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.keetab.reader.AppContext;
import com.keetab.reader.R;

public class ApiClient extends DefaultHttpClient {
	
	private static AppContext ctx = AppContext.instance;
	public static String API_URL = ctx.getString(R.string.API_URL);
	public static String API_KEY = ctx.getString(R.string.API_KEY);
	
	JSONParser parser = new JSONParser();
	
    public ApiClient() {}
    
    public HttpResponse get(String url) {
        HttpGet getRequest = new HttpGet(getRequestUrl(url));
        setRequestHeaders(getRequest);
        return executeRequest(getRequest);
    }
    
    public JSONObject getJSON(String url) {
        return parseJsonResponse(get(url));
    }

    public HttpResponse post(String url, Parameters params) {
        HttpPost postRequest = new HttpPost(getRequestUrl(url));
        setRequestHeaders(postRequest);
        postRequest.setEntity(params.toHttpEntity());
        return executeRequest(postRequest);
    }
    
    public JSONObject postJSON(String url, Parameters params) {
        return parseJsonResponse(post(url, params));
    }

    private HttpResponse executeRequest(HttpUriRequest request) {
        try {
            return this.execute(request);
        } catch (IOException e) {
        	e.printStackTrace();
            HttpResponseFactory factory = new DefaultHttpResponseFactory();
            HttpResponse badGateway = factory.newHttpResponse(
        		new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_BAD_GATEWAY, null), null
        	);
            return badGateway;
        }
    }
    
    private JSONObject parseJsonResponse(HttpResponse response) {
        JSONObject result = new JSONObject();
        try {
            int statusCode = response.getStatusLine().getStatusCode();
            String responseContent = EntityUtils.toString(response.getEntity());

            if (statusCode < 400) {
                result = (JSONObject) parser.parse(responseContent);
            } else {
                result.put("error", responseContent);
                result.put("status", statusCode);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            result.put("error", "Malformed JSON");
        } catch (ClassCastException e) {
            e.printStackTrace();
            result.put("error", "Response is not a JSON Object");
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            result.put("error", "Client protocoll not supported");
        } catch (IOException e) {
            e.printStackTrace();
            result.put("error", "Network error");
        }
        
        return result;
    }

    private void setRequestHeaders(HttpRequest request) {
        request.addHeader("X-API-KEY", API_KEY);
        request.addHeader("Accept", "application/json");
    }

    private String getRequestUrl(String url) {
        if (API_URL.endsWith("/")) {
            API_URL = API_URL.substring(0, API_URL.length() - 1);
        }
        if (url.startsWith("/")) {
            url = url.substring(1, url.length());
        }
        return API_URL + "/" + url;
    }
}
