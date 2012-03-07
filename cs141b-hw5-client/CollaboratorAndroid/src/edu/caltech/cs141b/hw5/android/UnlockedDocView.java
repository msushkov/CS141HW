package edu.caltech.cs141b.hw5.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import edu.caltech.cs141b.hw5.android.data.InvalidRequest;
import edu.caltech.cs141b.hw5.android.data.LockUnavailable;
import edu.caltech.cs141b.hw5.android.data.LockedDocument;
import edu.caltech.cs141b.hw5.android.data.UnlockedDocument;
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
	
	// the key of the current unlocked doc we are dealing with
	private String currDocKey;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(TAG, "created the unlocked doc view activity");

		service = new CollabServiceWrapper();
		
		extractUnlockedDocKey();
		getUnlockedDoc();
		
		setContentView(R.layout.unlockeddocgui);
	}

	/**
	 * Gets the unlocked doc key that was passed to this activity.
	 */
	private void extractUnlockedDocKey() {
		Log.i(TAG, "starting to extract unlocked doc");

		String key = null;
		Bundle extras = getIntent().getExtras();

		// extract the doc key
		if (extras != null)
			key = extras.getString(intentDataKey);

		this.currDocKey = key;
	}

	/**
	 * Display the unlocked doc.
	 */
	public void getUnlockedDoc() {

		Log.i(TAG, "starting to display unlocked doc");

		UnlockedDocument doc = null;

		// make a server request to get this doc
		try {
			doc = service.getDocument(currDocKey);
		} 
		catch (InvalidRequest e) {
			Log.i(TAG, "Invalid request when getting doc.");
			
			// TODO
			// pop up error msg to user and show the doc list
			startActivity(new Intent(this, DocListView.class));
		}

		// display the doc
		if (doc != null)
			displayUnlockedDoc(doc);
	}
	
	/**
	 * Displays the unlocked doc.
	 * @param doc
	 */
	private void displayUnlockedDoc(UnlockedDocument doc)
	{
		// TODO: set the title and contents (without letting the user edit)
	}

	/**
	 * Create a menu when user presses the physical 'menu' button.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.unlockedmenu, menu);
		return true;
	}

	/**
	 * Click handler for the menu buttons. Here the user has 4 options: create a
	 * new doc, get the lock, refresh the doc list, and refresh the doc. New
	 * doc, refresh list, and get lock should be enabled. Refresh doc should be
	 * disabled.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// which button did the user press?
		switch (item.getItemId()) {
		
		// create a new doc
		case R.id.newDoc:
			Log.i(TAG, "starting the locked doc activity");

			LockedDocument newDoc = new LockedDocument(null, null, null,
					newDocTitle, newDocContents);

			// start new locked doc view activity with new doc key as arg
			startActivity(new Intent(this, LockedDocView.class).putExtra(
					intentDataKey, newDoc.getKey()));
			
			return true;

		// refresh the doc list
		case R.id.docList: 
			startActivity(new Intent(this, DocListView.class)); 
			return true;
		
		// lock this doc
		case R.id.lockDoc:
			lockDoc();
			return true;

		default:
			return true;
		}
	}
	
	/**
	 * Send the lock request to the server.
	 */
	public void lockDoc()
	{
		LockedDocument doc = null;
		
		try 
		{
			doc = service.lockDocument(currDocKey);
		} 
		catch (LockUnavailable e) {
			Log.i(TAG, "Caught LockUnavailable when trying to lock the doc.");
			
			// TODO
			// print error msg to user and show the unlocked doc again
			
		} 
		catch (InvalidRequest e) {
			Log.i(TAG, "Caught InvalidRequest when tyring to lock doc.");
			
			// TODO
			// print error msg to user and show the unlocked doc again
		}
		
		// now that we got the lock for this doc, switch to locked view
		startActivity(new Intent(this,
				LockedDocView.class).putExtra(intentDataKey, doc));
	}
}
