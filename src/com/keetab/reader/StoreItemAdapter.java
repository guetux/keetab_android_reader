package com.keetab.reader;
import java.util.List;

import org.json.simple.JSONObject;

import com.keetab.reader.R;
import com.keetab.reader.api.ApiClient;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class StoreItemAdapter extends BaseAdapter {
	
	private static String API_URL = ApiClient.API_URL;
	
	private AppContext ctx = AppContext.instance;
	private List<JSONObject> storeItems;
	private LayoutInflater inflater;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	
	
	public StoreItemAdapter(List<JSONObject> storeItems) {
		this.storeItems = storeItems;
		this.inflater = (LayoutInflater)
	    		ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return storeItems.size();
	}

	public Object getItem(int index) {
		return storeItems.get(index);
	}

	public long getItemId(int index) {
		return index;
	}

	public View getView(int index, View convertView, ViewGroup parent) {
		View view = convertView;
		JSONObject item = storeItems.get(index);

		if (convertView == null)
			view = inflater.inflate(R.layout.store_item, parent, false);
		
		TextView title = (TextView)view.findViewById(R.id.title);
		TextView description = (TextView)view.findViewById(R.id.description);
		ImageView cover = (ImageView)view.findViewById(R.id.cover);

		title.setText(item.get("title").toString());
		description.setText(item.get("description").toString());
		
		String id = item.get("id").toString();
		String coverURL = API_URL + "/cover/" + id + "/50x50.png";
		imageLoader.displayImage(coverURL, cover);
		
		return view;
	}

}
