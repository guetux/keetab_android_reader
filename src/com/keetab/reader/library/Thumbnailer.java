package com.keetab.reader.library;

import java.io.IOException;
import java.io.InputStream;

import com.keetab.reader.ReaderContext;

import nl.siegmann.epublib.domain.Resource;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

public class Thumbnailer {
	
	static ReaderContext ctx = ReaderContext.instance;

	
	public static Bitmap load(Publication pub, int width, int height) 
			throws IOException {
		Resource cover = pub.getEpubInfo().getCoverImage();
		if (cover == null)
			return null;

		BitmapFactory.Options options = getImageDimensions(cover);
		
		options.inSampleSize = calculateInSampleSize(options, width, height);
		options.inJustDecodeBounds = false;
		
		InputStream is = cover.getInputStream();
		Bitmap cB = BitmapFactory.decodeStream(is, null, options);
		is.close();
		
		return cB;
	}



	private static BitmapFactory.Options getImageDimensions(Resource cover)
			throws IOException {
		InputStream is = cover.getInputStream();
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(is, null, options);
		is.close();
		return options;
	}
	
	
	
	private static int calculateInSampleSize(
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
