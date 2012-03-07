package edu.caltech.cs141b.hw5.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import edu.caltech.cs141b.hw5.android.data.LockedDocument;
import edu.caltech.cs141b.hw5.android.proto.CollabServiceWrapper;

public class UnlockedDocView extends Activity {

	// debugging
	private static String TAG = "UnlockedDocView";

	// the key that identifies the doc that is passed between activities
	public static String intentDataKey = "doc";

	// initial title + contents of new doc
	private static String newDocTitle = "Enter the document title.";
	private static String newDocContents = "Enter the document contents.";

	// makes server calls
	CollabServiceWrapper service;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "created the unlocked doc view activity");
		service = new CollabServiceWrapper();
		displayLockedDoc(extractUnlockedDocKey());
		setContentView(R.layout.unlockeddocgui);

	}

	/**
	 * Gets the unlocked doc key that was passed to this activity.
	 * 
	 * @return
	 */
	private String extractUnlockedDocKey() {
		Log.i(TAG, "starting to extract unlocked doc");

		String currDocKey = null;
		Bundle extras = getIntent().getExtras();

		// extract the doc key
		if (extras != null)
			currDocKey = extras.getString(intentDataKey);

		return currDocKey;
	}

	/**
	 * Display the locked doc.
	 */
	public void displayLockedDoc(String currDocKey) {

		Log.i(TAG, "starting to display unlocked doc");

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
	 * Click handler for the menu buttons. Here the user has 4 options: create a
	 * new doc, refresh the doc list, get the lock, and refresh the doc. New
	 * doc, refresh list, and get lock should be enabled. Refresh doc should be
	 * disabled.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// which button did the user press?
		switch (item.getItemId()) {

		// TODO

		// new doc is pressed
		case R.id.newDoc:
			Log.i(TAG, "starting the locked doc activity");

			LockedDocument newDoc = new LockedDocument(null, null, null,
					newDocTitle, newDocContents);

			// start new locked doc view activity with new doc key as arg
			startActivity(new Intent(this, LockedDocView.class).putExtra(
					intentDataKey, newDoc.getKey()));
			return true;

			// get doc list is pressed
			/*
			 * case R.id.docList: startActivity(new Intent(this,
			 * UnlockedDocView.class)); return true;
			 */

		default:
			return true;
		}

	}
}
