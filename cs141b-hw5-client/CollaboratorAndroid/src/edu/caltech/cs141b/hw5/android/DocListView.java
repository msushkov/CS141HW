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
import android.widget.Toast;
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
	private static String intentDataKey = "doc";

	// the key that identifies the isStartup boolean that is passed
	// to the list view activity
	private static String boolKey = "isStartup";

	// initial title + contents of new doc
	private static String newDocTitle = "";
	private static String newDocContents = "";

	// makes server calls
	private CollabServiceWrapper service;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "created the doc list view activity");
		
		// need this to make server calls
		service = new CollabServiceWrapper();
		
		// get the doc list from the server
		getDocList();

		// if this is the very first time this activity gets run 
		// (on startup), then there will be no extras. however, if this
		// activity gets called by other activities then they will pass
		// it an extra. 
		Bundle extras = getIntent().getExtras();
		
		if (extras == null || extras.getBoolean(boolKey))
		{
			// display message to the user saying to press menu key 
			// (this is only done the very first time the app loads)
			Toast msg = Toast.makeText(this, "Press MENU for more features!",
					Toast.LENGTH_LONG);
			msg.show();
		}
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
		else
		{
			Log.i(TAG, "doc list is null");

			// inform the user that the doc list is null
			Toast msg = Toast.makeText(this, "Error - server returned a null document list.", 
					Toast.LENGTH_SHORT);
			msg.show();

			// try again
			getDocList();
			return;
		}

		final ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		// define the action for when the user presses a doc in the list
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.i(TAG, "starting the unlocked doc activity");

				// get the currently-selected doc
				DocumentMetadata currDoc = (DocumentMetadata) 
						lv.getItemAtPosition(position);

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
