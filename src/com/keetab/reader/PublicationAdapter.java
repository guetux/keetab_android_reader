package com.keetab.reader;

import java.io.IOException;
import java.io.InputStream;

import nl.siegmann.epublib.domain.Resource;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.keetab.reader.library.Publication;

public class PublicationAdapter extends BaseAdapter{

	private Context ctx;
	private Publication[] values;
	private LayoutInflater inflater;
		
	
	public PublicationAdapter(Context context, Publication[] values) {
		this.ctx = context;
	    this.values = values;
	    this.inflater = (LayoutInflater)
	    		context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			Resource cover = pub.getEpubInfo().getCoverImage();
			if (cover != null) {
				InputStream is = cover.getInputStream();
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(is, null, options);
				is.close();
				
				options.inSampleSize = calculateInSampleSize(options, 40, 40);
				options.inJustDecodeBounds = false;
				
				is = cover.getInputStream();
				Bitmap cB = BitmapFactory.decodeStream(is, null, options);
				
				BitmapDrawable bd = new BitmapDrawable(ctx.getResources(), cB);
				view.setCompoundDrawablePadding(60-options.outWidth);
				view.setCompoundDrawablesWithIntrinsicBounds(bd, null, null, null);
				
			} else {
				view.setCompoundDrawables(null, null, null, null);
			}
		} catch (IOException e) {
			view.setCompoundDrawables(null, null, null, null);
		}
		
		return view;
		
	}

	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        // Calculate ratios of height and width to requested height and width
	        final int heightRatio = Math.round((float) height / (float) reqHeight);
	        final int widthRatio = Math.round((float) width / (float) reqWidth);
	
	        // Choose the smallest ratio as inSampleSize value, this will guarantee
	        // a final image with both dimensions larger than or equal to the
	        // requested height and width.
	        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	    }
	
	    return inSampleSize;
}
}
