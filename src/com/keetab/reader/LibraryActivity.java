package com.keetab.reader;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.keetab.reader.library.Library;
import com.keetab.reader.library.Publication;

public class LibraryActivity extends ListActivity {

	Library library = ReaderContext.library;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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

}
