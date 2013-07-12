package com.keetab.reader;

import java.io.IOException;
import nl.siegmann.epublib.domain.Book;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.keetab.reader.library.BookHelper;
import com.keetab.reader.library.Publication;

public class PublicationAdapter extends BaseAdapter {

	private AppContext ctx = AppContext.instance;
	private Publication[] values;
	private LayoutInflater inflater;
		
	
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
		
		try {
			Book info = pub.getEpubInfo();
			title.setText(info.getTitle());
			description.setText(BookHelper.getAuthors(info));
			
			Bitmap cov = pub.getThumbnail(40, 40);
			cover.setImageBitmap(cov);
		} catch (IOException e) {}
		
		return vi;
		
	}


}
