package edu.caltech.cs141b.hw5.android;

import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import edu.caltech.cs141b.hw5.android.data.DocumentMetadata;
import edu.caltech.cs141b.hw5.android.data.LockedDocument;
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

	// the key that identifies the doc that is passed between activities
	public static String intentDataKey = "doc";

	// initial title + contents of new doc
	private static String newDocTitle = "Enter the document title.";
	private static String newDocContents = "Enter the document contents.";

	// makes server calls
	private CollabServiceWrapper service;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "created the doc list view activity");
		service = new CollabServiceWrapper();
		getDocList();
	}

	/**
	 * Get the document list from the server.
	 */
	public void getDocList() {
		Log.i(TAG, "starting to get the doc list");

		// get the docs from the server
		List<DocumentMetadata> docs = service.getDocumentList();

		Log.i(TAG, "finished getting the doc list");

		if (docs != null) {
			// reverse list to get new content on top
			Collections.reverse(docs);

			// set the model for the list view, uses toString() method to get
			// names.
			setListAdapter(new ArrayAdapter<DocumentMetadata>(this,
					android.R.layout.simple_list_item_1, docs));
		}

		final ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		// define the action for when the user presses a doc in the list
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.i(TAG, "starting the unlocked doc activity");

				// get the currently-selected doc
				DocumentMetadata currDoc = (DocumentMetadata) lv
						.getItemAtPosition(position);

				// start the unlocked doc view activity to display
				// this doc (use its id to make a datastore request)
				startActivity(new Intent(DocListView.this,
						UnlockedDocView.class).putExtra(intentDataKey,
						currDoc.getKey()));
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
	 * Click handler for the menu buttons. Here the user has 2 options: create a
	 * new doc, or refresh the doc list.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// which button did the user press?
		switch (item.getItemId()) {

		// new doc is pressed
		case R.id.newDoc:
			Log.i(TAG, "starting the locked doc activity");

			LockedDocument newDoc = new LockedDocument(null, null, null,
					newDocTitle, newDocContents);

			// start new locked doc view activity with new doc as arg
			startActivity(new Intent(this, LockedDocView.class).putExtra(
					intentDataKey, newDoc));

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
