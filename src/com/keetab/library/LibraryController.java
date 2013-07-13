package com.keetab.library;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import org.json.simple.JSONArray;

import ch.bazaruto.Bazaruto.GET;
import ch.bazaruto.Bazaruto.Route;
import ch.bazaruto.JSONResponse;
import ch.bazaruto.NanoHTTPD;
import ch.bazaruto.Request;
import ch.bazaruto.Response;
import ch.bazaruto.storage.ZipStorage;

import com.keetab.util.DirectoryManager;
import com.keetab.util.JSONStorage;

@Route("^/library")
public class LibraryController {
    public static final String PUBLICATIONS = "publications";
    static final File libraryDir = DirectoryManager.getLibraryDir();
    
    JSONStorage storage = new JSONStorage();
    
    @GET("/")
    public Response list(Request req) {
        JSONArray result = storage.list(PUBLICATIONS);
        //Collections.sort(result, new PublicationComparator());
        return new JSONResponse(result.toJSONString());
    }
    
	@GET("/(\\d+)\\.json")
	public Response pubJSON(Request req, Object id) {
		String epub = id + ".epub";
		Library library = new Library();
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

    
    @GET("/(\\d+)/.*")
    public Response serveCorrect(Request req, Object id) {
        File epub = new File(libraryDir, id + ".epub");
        req.path = req.path.replaceAll("^/" + id + "/", "");
        return serveFromEpub(req, epub);
    }
    
    private Response serveFromEpub(Request req, File epub) {
        if (epub.exists()) {
            try {
            	ZipStorage storage = new ZipStorage(new ZipFile(epub));
                
                return NanoHTTPD.serveFile(req, storage, true);
            } catch (Exception e) {
                return new Response(e.getMessage());
            }

        } else {
            return new Response("Epub not found");
        }
    }
    
}