package com.keetab;

import org.readium.sdk.android.EPub3;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.keetab.library.Library;
import com.keetab.library.Publication;

public class LibraryActivity extends ListActivity {

	Library library = new Library();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		EPub3.setCachePath(getCacheDir().getAbsolutePath());
		
		setContentView(R.layout.activtity_library);

		PublicationAdapter adapter = new PublicationAdapter(library.asArray());
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Publication pub = library.findByIndex(position);
		Intent intent = new Intent(this, ReaderActivity.class);
		intent.putExtra("pub", pub);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.library, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.open_store) {
			Intent intent = new Intent(this, StoreActivity.class);
			startActivity(intent);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
}
