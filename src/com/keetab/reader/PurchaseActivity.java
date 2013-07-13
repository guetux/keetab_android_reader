package com.keetab.reader;
import java.util.HashMap;

import com.keetab.reader.R;
import com.keetab.reader.api.ApiClient;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("rawtypes")
public class PurchaseActivity extends Activity {

	private static String API_URL = ApiClient.API_URL;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	
	
	HashMap pub;
	
	final String[] keys = {"author", "publisher", "rights", "description"};
	
	ImageView cover;
	TableLayout table;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_purchase);
		
		cover = (ImageView)findViewById(R.id.cover);
		table = (TableLayout)findViewById(R.id.table);
		
		Intent intent = getIntent();
		pub = (HashMap)intent.getSerializableExtra("publication");
		
		setTitle(pub.get("title").toString());
		
		table.setColumnShrinkable(1, true);
		
		for (String key : keys) {
			if (!pub.containsKey(key) || pub.get(key).equals("")) continue;
			
			String value = pub.get(key).toString();
			TableRow row = new TableRow(this);
			
			TextView keyText = new TextView(this);
			keyText.setTypeface(null, Typeface.BOLD);
			keyText.setText(capitalize(key));
			row.addView(keyText);
			
			TextView valueText = new TextView(this);
			valueText.setText(value);
			valueText.setSingleLine(false);
			TableRow.LayoutParams p2 = new TableRow.LayoutParams();
			p2.setMargins(10, 0, 10, 5);
			valueText.setLayoutParams(p2);
			row.addView(valueText);
			table.addView(row);
		}
		
		String id = pub.get("id").toString();
		String coverURL = API_URL + "/cover/" + id + "/100x150.png";
		imageLoader.displayImage(coverURL, cover);
		
	}
	
	private String capitalize(String line) {
	  return Character.toUpperCase(line.charAt(0)) + line.substring(1);
	}
	
	public void cancel(View view) {
		finish();
	}
	
	public void purchase(View view) {
		Toast.makeText(this, "Kablam", Toast.LENGTH_LONG).show();
	}
}
