package com.keetab.reader;

import java.util.List;

import org.json.simple.JSONObject;

import com.keetab.reader.api.StoreAPI;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.app.ListActivity;
import android.content.Intent;

public class StoreActivity extends ListActivity {

	List<JSONObject> purchasables;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_store);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		new Thread(new Runnable() {
			public void run() {
				loadProducts();
			}
		}).start();

	}
	
	private void loadProducts() {
		purchasables = StoreAPI.getPurchasable();
		final StoreItemAdapter adapter = new StoreItemAdapter(purchasables);
		
		runOnUiThread(new Runnable() {
			public void run() {
				setListAdapter(adapter);
			}
		});
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int i, long id) {
		Intent intent = new Intent(this, PurchaseActivity.class);
		JSONObject publication = purchasables.get(i);
		intent.putExtra("publication", publication);
		startActivity(intent);
	}
	

}
