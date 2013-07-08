package com.keetab.reader;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.keetab.reader.library.Library;
import com.keetab.reader.library.Publication;

public class LibraryActivity extends ListActivity {

	Library library = ReaderContext.library;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		String[] titles = library.getTitles();
		
		ArrayAdapter<String> adapter =
				new ArrayAdapter<String>(this, R.layout.archive_item, titles);
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Publication pub = library.findByIndex(position);
		
		Intent intent = new Intent(this, ReaderActivity.class);
		
		String dataFile = pub.getFileName().replace(".epub", ".json");
		String dataPath = "/library/" + dataFile;
		intent.putExtra("data", dataPath);
		startActivity(intent);
	}

}
