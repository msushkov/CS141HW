package edu.caltech.cs141b.hw5.android;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import edu.caltech.cs141b.hw5.android.data.DocumentMetadata;
import edu.caltech.cs141b.hw5.android.data.LockedDocument;
import edu.caltech.cs141b.hw5.android.data.UnlockedDocument;
import edu.caltech.cs141b.hw5.android.proto.CollabServiceWrapper;

/**
 * Displays the doc list.
 * 
 * @author msushkov
 * 
 */
public class DocListView extends ListActivity {

	// debugging
	private static String TAG = "DocListView";

	// initial title + contents of new doc
	private static String newDocTitle = "Enter the document title.";
	private static String newDocContents = "Enter the document contents.";

	// makes server calls
	private CollabServiceWrapper service;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		service = new CollabServiceWrapper();
		getDocList();
	}

	/**
	 * Get the document list from the server.
	 */
	public void getDocList() {
		// get the docs from the server
		List<DocumentMetadata> docs = service.getDocumentList();

		// reverse list as to get new content on top
		Collections.reverse(docs);

		// set the model for the list view, uses toString() method to get names.
		setListAdapter(new ArrayAdapter<DocumentMetadata>(this,
				android.R.layout.simple_list_item_1, docs));

		final ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		// define the action for when the user presses a doc in the list
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// get the currently-selected doc
				DocumentMetadata currDoc = (DocumentMetadata) lv
						.getItemAtPosition(position);

				ActivityStarter.startDocViewActivity(DocListView.this, UnlockedDocView.class, currDoc);
			}
		});

	}

	/**
	 * Create a menu when user presses the physical 'menu' button.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.listmenu, menu);
		return true;
	}

	/**
	 * Click handler for the menu buttons. Here the user has 2 options:
	 * create a new doc, or refresh the doc list.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// which button did the user press?
		switch (item.getItemId()) {

		// new doc is pressed
		case R.id.newDoc:
			// start new locked doc view activity with new doc as arg
			LockedDocument newDoc = new LockedDocument(null, null, null,
					newDocTitle, newDocContents);
			ActivityStarter.startDocViewActivity(this, LockedDocView.class, newDoc);
			return true;

			// refresh is pressed
		case R.id.refreshList:
			getDocList();
			return true;

		default:
			return true;
		}
	}
}
