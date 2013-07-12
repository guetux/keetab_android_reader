package com.keetab.reader;

import java.util.List;

import org.json.simple.JSONObject;

import com.keetab.reader.api.StoreAPI;

import android.os.Bundle;
import android.app.ListActivity;

public class StoreActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_store);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		Thread loader = new Thread(new Runnable() {
			public void run() {
				loadProducts();
			}
		});
		loader.start();

	}
	
	private void loadProducts() {
		List<JSONObject> purchasables = StoreAPI.getPurchasable();
		final StoreItemAdapter adapter = new StoreItemAdapter(purchasables);
		
		runOnUiThread(new Runnable() {
			public void run() {
				setListAdapter(adapter);
			}
		});
	}
	

}
