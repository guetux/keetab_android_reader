package com.keetab.library;

import java.io.File;
import java.io.IOException;

import ch.bazaruto.Bazaruto.GET;
import ch.bazaruto.Bazaruto.Route;
import ch.bazaruto.NanoHTTPD;
import ch.bazaruto.Request;
import ch.bazaruto.Response;
import ch.bazaruto.storage.FileStorage;

import com.keetab.AppContext;

@Route("^/library")
public class LibraryController {

	Library library = AppContext.library;
	
	@GET("/([\\w-]+)\\.json")
	public Response pubJSON(Request req, String fileName) {
		String epub = fileName + ".epub";
		Publication pub = library.findByFilename(epub);
		if (pub  != null) {	
			try {
				return new Response(
					pub.getBookData().toJSONString(),
					NanoHTTPD.HTTP_OK, 
					"application/json"
				);
			} catch(IOException e) {
				return new Response(e.getMessage(), 
						NanoHTTPD.HTTP_INTERNALERROR);
			}
			
    	} else {
    		return new Response("Epub not found", NanoHTTPD.HTTP_NOTFOUND);
    	}
	}
	
    @GET("/([\\w-\\.]+)/.*")
    public Response serve(Request req, String epub) {
    	Publication pub = library.findByFilename(epub);
    	if (pub != null) {
    		try {
    			File extractedDir = pub.extract();
    			FileStorage storage = new FileStorage(extractedDir);
    			req.path = req.path.replaceAll("^/" + epub + "/", "");
    			return NanoHTTPD.serveFile(req, storage, true);
    		} catch (IOException e) {
    			return new Response(e.getMessage(),
    					NanoHTTPD.HTTP_INTERNALERROR);
    		}
    	} else {
    		return new Response("Epub not found", NanoHTTPD.HTTP_NOTFOUND);
    	}
    }
}
