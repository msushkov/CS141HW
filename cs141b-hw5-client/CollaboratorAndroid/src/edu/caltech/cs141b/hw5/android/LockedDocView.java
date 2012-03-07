package edu.caltech.cs141b.hw5.android;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import edu.caltech.cs141b.hw5.android.data.LockedDocument;
import edu.caltech.cs141b.hw5.android.proto.CollabServiceWrapper;

public class LockedDocView extends ListActivity {

	// debugging
	private static String TAG = "LockedDocView";

	// initial title + contents of new doc
	private static String newDocTitle = "Enter the document title.";
	private static String newDocContents = "Enter the document contents.";

	// makes server calls
	CollabServiceWrapper service;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "created the locked doc view activity");
		service = new CollabServiceWrapper();
		displayLockedDoc(extractLockedDocKey());
	}
	
	/**
	 * Gets the locked doc key that was passed to this activity.
	 * @return
	 */
	private String extractLockedDocKey()
	{
		Log.i(TAG, "starting to extract locked doc");
		
		// get the doc key and make a datastore query to get this doc
		
		Bundle extras = getIntent().getExtras();
		String currDocKey = null;

		// extract the doc key
		if (extras != null)
			currDocKey = extras.getString(ActivityStarter.intentDataKey);
		
		return currDocKey;
	}

	/**
	 * Display the locked doc.
	 */
	public void displayLockedDoc(String currDocKey) {
		
		Log.i(TAG, "starting to display locked doc");
		
		// TODO

	}

	/**
	 * Create a menu when user presses the physical 'menu' button.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.listmenu, menu);
		return true;
	}

	/**
	 * Click handler for the menu buttons. Here the user has 4 options:
	 * create a new doc, refresh the doc list, get the lock, and refresh the doc.
	 * New doc and refresh list should be enabled. Get lockRefresh doc should
	 * be disabled.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// which button did the user press?
		switch (item.getItemId()) {

		// TODO
		
		// new doc is pressed
		case R.id.newDoc:
			// do this activity again with a new doc
			LockedDocument newDoc = new LockedDocument(null, null, null,
					newDocTitle, newDocContents);
			displayLockedDoc(newDoc.getKey());
			return true;

			/*
		case R.id.docList:
			startActivity(new Intent(this, UnlockedDocView.class));
			return true;
			 */

		default:
			return true;
		}

	}
}
