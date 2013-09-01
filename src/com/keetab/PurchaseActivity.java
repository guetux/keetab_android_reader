package com.keetab;
import java.util.HashMap;

import org.json.simple.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.keetab.api.Cover;
import com.keetab.api.DownloadPublication;
import com.keetab.api.StoreAPI;
import com.nostra13.universalimageloader.core.ImageLoader;

@SuppressWarnings("rawtypes")
public class PurchaseActivity extends Activity {

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
		String coverURL = Cover.getCoverURL(id, 50, 50);
		ImageLoader.getInstance().displayImage(coverURL, cover);
		
	}
	
	private String capitalize(String line) {
	  return Character.toUpperCase(line.charAt(0)) + line.substring(1);
	}
	
	public void cancel(View view) {
		finish();
	}
	
	public void purchase(View view) {
		final String id = pub.get("id").toString();
		final PurchaseActivity ctx = this;
		
		AsyncTask<String, Void, JSONObject> task = 
			new AsyncTask<String, Void, JSONObject>() {
				@Override
				protected JSONObject doInBackground(String... params) {
					return StoreAPI.purchase(params[0]);
				}
				
				@Override
				protected void onPostExecute(JSONObject purchase) {
			        if (purchase.containsKey("error")) {
			        	if (((Integer)purchase.get("status")).equals(402)) { 
			        		Toast.makeText(ctx, "PAYMENT REQURED", Toast.LENGTH_LONG).show();
			        	} else {
			        		String error = purchase.get("error").toString();
			        		Toast.makeText(ctx, error, Toast.LENGTH_LONG).show();
			        	}
			        } else {
			            new DownloadPublication(purchase).run();
			            Toast.makeText(ctx, "Download started", Toast.LENGTH_LONG).show();
			        }
				}
			};
        
		task.execute(id);
			
        finish();
	}
}
