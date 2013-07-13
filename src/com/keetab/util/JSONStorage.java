package com.keetab.util;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.keetab.AppContext;

/**
 * A persistent storage for JSON Objects
 * 
 * All stored objects have a type. The type is used to seperate 
 * different kind of objects like books and authors.
 * 
 * All object are given a random id and can be retrieved by that id
 * later.
 * 
 * Object may provide their own id by having an 'id' property:
 * 
 *  { "id": 1, "name": "Custom id object" }
 * 
 * If the id is already used, the object will not be added!
 * However, it will be updated by with the given values
 * 
 * @author sr
 *
 */
public class JSONStorage {

    // Overrideable Context for testing
    public static Context ctx = AppContext.instance;
    
	private static final String TAG = "JSONStorage";
	
    private static final int DB_VERSION = 2;
    public static final String DB_NAME = "jsonstorage.db";
    private static final String TABLE_NAME = "objects";
    private static final String C_ID = "id";
    private static final String C_TYPE = "type";
    private static final String C_JSON = "json";
    private static final String[] C = {C_ID, C_TYPE, C_JSON};
    private static final String TABLE_CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                C_ID + " TEXT," +
                C_TYPE + " TEXT, " +
                C_JSON + " TEXT," +
                "UNIQUE(" + C_ID + ", " + C_TYPE + "));";

	
    private DBHelper dbhelper;
    private JSONParser parser;
    
    public static class DBHelper extends SQLiteOpenHelper {
    	
    	public DBHelper(Context ctx) {
    		super(ctx, DB_NAME, null, DB_VERSION);
    	}
	
	    @Override
	    public void onCreate(SQLiteDatabase db) {
	        db.execSQL(TABLE_CREATE);
	    }
	
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
           Log.w(TAG, "Upgrading database from version " + oldVersion 
                 + " to " + newVersion + ", which will destroy all objects");
           db.execSQL("DROP TABLE IF EXISTS" + TABLE_NAME);
           onCreate(db);
		}
    }
    
    public JSONStorage(Context ctx) {
    	dbhelper = new DBHelper(ctx);
    	parser = new JSONParser();
    }
    
    public JSONStorage() {
        this(ctx);
    }
    
    /**
     * @param type of the object
     * @param id of the object
     * @return null, if the object does not exist 
     */
    public JSONObject get(String type, String id) {
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        try {
        	Cursor cursor = db.query(TABLE_NAME, C, C_ID+"=? and "+C_TYPE+"=?", 
        			new String[] { id, type }, null, null, null);
        	
        	if (!cursor.moveToFirst()) {
        	    return null;
        	} else {
        	    return getSingleObject(cursor);
        	}
        } finally {
            db.close();
        }
    }
    
    /**
     * @param type of the object, something like "book"
     * @param object
     * @return null, if the provided id was already used
     */
    public JSONObject add(String type, JSONObject object) {
        String id;
        if (object.containsKey("id")) {
            id = object.get("id").toString();
            if (get(type, id) != null) {
                return null;
            }
        } else {
            id = UUID.randomUUID().toString();
            while(get(type, id) != null) {
                id = UUID.randomUUID().toString();
            }
        }
        
    	SQLiteDatabase db = dbhelper.getWritableDatabase();
    	try {
        	
        	ContentValues values = new ContentValues();
        	values.put(C_ID, id);
        	values.put(C_TYPE, type);
        	values.put(C_JSON, object.toString());
        	
        	db.insert(TABLE_NAME, null, values);
    	} finally {
    	    db.close();
    	}
    	
    	return get(type, id);
    }
    
    /**
     * @param object must have and attribute "id"
     * @return true if the object was updated
     */
    public boolean update(JSONObject object) {
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        try {
        	if (!object.containsKey("id")) return false;
        	
			String id = object.get("id").toString();
	    	ContentValues values = new ContentValues();
	    	values.put(C_JSON, object.toString());

	    	return db.update(TABLE_NAME, values, C_ID+"=?", 
	    	        new String[] {id}) == 1;
        } finally {
            db.close();
        }
    }
    
    /**
     * @param object must have and attribute "id"
     * @param type new type of the object
     * @return true if the object was updated
     */
    public boolean update(JSONObject object, String type) {
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        try {
        	if (!object.containsKey("id")) return false;

			String id = object.get("id").toString();    	
	    	ContentValues values = new ContentValues();
	    	values.put(C_TYPE, type);
	    	values.put(C_JSON, object.toString());

	    	return db.update(TABLE_NAME, values, C_ID+"=?", 
	    			new String[] {id}) == 1;
        } finally {
            db.close();
        }
    }
    
    /**
     * @param object must have and attribute "id"
     * @return true if the object was deleted
     */
    public boolean delete(JSONObject object) {
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        try {
        	if (!object.containsKey("id")) return false;  
        	
	        String id = object.get("id").toString();
	        
	        return db.delete(TABLE_NAME, C_ID+"=?", new String[] {id}) == 1;
    	} finally {
            db.close();
        }
    }
    
    /**
     * @param type of objects to be deleted
     * @return the number of objects deleted
     */
    public int deleteAllByType(String type) {
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        try {
            return db.delete(TABLE_NAME, C_TYPE+"=?", new String[] {type});
        } finally {
            db.close();
        }
    }
    
    /**
     * Get all objects of a type
     * @param type
     */
    public JSONArray list(String type) {
    	SQLiteDatabase db = dbhelper.getReadableDatabase();
    	try {
        	Cursor cursor = db.query(TABLE_NAME, C, C_TYPE+"=?", 
        			new String[] { type }, null, null, null);
        	return parseJSON(cursor);
    	} finally {
    	    db.close();
    	}
    }
   
    /**
     * A list of all objects with an additional type attribute
     * 
     * [{"id":1, "title": "clean code", "type": "book"}]
     * 
     * @return all objects with an additional type property:
     */
    public JSONArray listAll() {
    	SQLiteDatabase db = dbhelper.getReadableDatabase();
    	try {
        	String query = "SELECT * FROM " + TABLE_NAME;
        	Cursor cursor = db.rawQuery(query, null);
        	return parseJSON(cursor, true);
    	} finally {
    	    db.close();
    	}
    }
    
    /**
     * @return a list of types saved in the storage
     */
    public List<String> listTypes() {
    	SQLiteDatabase db = dbhelper.getReadableDatabase();
    	try {
    	    String query = "SELECT " + C_TYPE + " FROM " + TABLE_NAME;
    	    Cursor cursor = db.rawQuery(query, null);
    	    return getTypesList(cursor);
    	} finally {
    	    db.close();
    	}
    }
    
    public long count() {
    	SQLiteDatabase db = dbhelper.getReadableDatabase();
    	try {
    	    return DatabaseUtils.queryNumEntries(db, TABLE_NAME);
    	} finally {
    	    db.close();
    	}
    }
    
    private JSONArray parseJSON(Cursor cursor) {
    	return parseJSON(cursor, false);
    }
    
    private JSONArray parseJSON(Cursor cursor, boolean addType) {
        try {
        	JSONArray result = new JSONArray();
        	if (!cursor.moveToFirst()) {
        		return result;
        	}
        	
        	while (!cursor.isAfterLast()) {
        		JSONObject object = getSingleObject(cursor, addType);
        		result.add(object);
        		cursor.moveToNext();
        	}
        	
        	return result;
        } finally {
            cursor.close();
        }
    }
    
    private JSONObject getSingleObject(Cursor cursor) {
    	return getSingleObject(cursor, false);
    }
    
    private JSONObject getSingleObject(Cursor cursor, boolean addType) {
		try {
			String json = cursor.getString(2);
			JSONObject object = (JSONObject)parser.parse(json);
			object.put("id", cursor.getString(0));
			return object;
		} catch (ParseException e) {
			return null;
		}
    }
    
    private List<String> getTypesList(Cursor cursor) {
        try {
        	List<String> result = new LinkedList<String>();
        	cursor.moveToFirst();
        	
        	while (!cursor.isAfterLast()) {
    			result.add(cursor.getString(1));
        		cursor.moveToNext();
        	}
        	
        	return result;
        } finally {
            cursor.close();
        }
    }
}
