package com.keetab.reader;

import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.keetab.reader.library.Publication;

public class PublicationAdapter extends BaseAdapter{

	private ReaderContext ctx = ReaderContext.instance;
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
	public View getView(int i, View convertView, ViewGroup parent) {
		TextView view;
		Publication pub = values[i];
		if (convertView == null) {
			view = (TextView)inflater.inflate(R.layout.archive_item, parent, false);
		} else {
			view = (TextView)convertView;
		}
		
		try {
			view.setText(pub.getEpubInfo().getTitle());
		} catch (IOException e) {
			view.setText("Unknown");
		}
		
		try {
			Bitmap cover = pub.getThumbnail(40, 40);
			BitmapDrawable bd = new BitmapDrawable(ctx.getResources(), cover);
			view.setCompoundDrawablesWithIntrinsicBounds(bd, null, null, null);
		} catch (IOException e) {
			view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.archive_item, 0, 0, 0);
		}
		
		return view;
		
	}


}
