package com.keetab;

import org.json.simple.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.keetab.api.Cover;
import com.keetab.library.Publication;
import com.keetab.reader.R;
import com.nostra13.universalimageloader.core.ImageLoader;

public class PublicationAdapter extends BaseAdapter {

	private AppContext ctx = AppContext.instance;
	private Publication[] values;
	private LayoutInflater inflater;
	private ImageLoader imageLoader = ImageLoader.getInstance();
		
	
	public PublicationAdapter(Publication[] values) {
	    this.values = values;
	    this.inflater = (LayoutInflater)
	    		ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return values.length;
	}

	@Override
	public Object getItem(int i) {
		return values[i];
	}

	@Override
	public long getItemId(int i) {
		return i;
	}
	
	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	@Override
	public View getView(int i, View convertView, ViewGroup parent) {
		View vi = convertView;
		Publication pub = values[i];

		if (convertView == null)
			vi = inflater.inflate(R.layout.archive_item, parent, false);
		
		ImageView cover = (ImageView)vi.findViewById(R.id.cover);
		TextView title = (TextView)vi.findViewById(R.id.title);
		TextView description = (TextView)vi.findViewById(R.id.description);
		
		JSONObject meta = pub.getMeta();
		
		if (meta != null ) {
			title.setText(meta.get("title").toString());
			description.setText(meta.get("description").toString());
			
			String id = meta.get("id").toString();
			String coverURL = Cover.getCoverURL(id, 50, 50);
			imageLoader.displayImage(coverURL, cover);
		}
		
		return vi;
		
	}


}
