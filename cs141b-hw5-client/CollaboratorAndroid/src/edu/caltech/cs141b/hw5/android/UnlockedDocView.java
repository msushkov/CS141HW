package edu.caltech.cs141b.hw5.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import edu.caltech.cs141b.hw5.android.data.InvalidRequest;
import edu.caltech.cs141b.hw5.android.data.LockUnavailable;
import edu.caltech.cs141b.hw5.android.data.LockedDocument;
import edu.caltech.cs141b.hw5.android.data.UnlockedDocument;
import edu.caltech.cs141b.hw5.android.proto.CollabServiceWrapper;

public class UnlockedDocView extends Activity {

	// debugging
	private static String TAG = "UnlockedDocView";

	// the key that identifies the doc that is passed between activities
	private static String intentDataKey = "doc";

	// initial title + contents of new doc
	private static String newDocTitle = "";
	private static String newDocContents = "";

	// makes server calls
	private CollabServiceWrapper service;

	// the key of the current unlocked doc we are dealing with
	private String currDocKey;

	// textboxes
	private EditText titleBox;
	private EditText contentBox;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "created the unlocked doc view activity");

		service = new CollabServiceWrapper();

		setContentView(R.layout.unlockeddocgui);

		titleBox = (EditText) findViewById(R.id.title);
		contentBox = (EditText) findViewById(R.id.content);

		// get the current doc's key (passed from another activity)
		extractUnlockedDocKey();

		// get the unlocked doc with the current key from the server
		getUnlockedDoc();
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

		// store the current doc's key
		this.currDocKey = key;
	}

	/**
	 * Display the unlocked doc.
	 */
	private void getUnlockedDoc() {
		Log.i(TAG, "starting to display unlocked doc");

		UnlockedDocument doc = null;

		// make a server request to get this doc
		try {
			doc = service.getDocument(currDocKey);
		} catch (InvalidRequest e) {
			Log.i(TAG, "Invalid request when getting doc.");

			// alert the user that the get doc operation failed
			Toast errorMsg = Toast.makeText(this,
					"Getting the doc failed - invalid request.",
					Toast.LENGTH_SHORT);
			errorMsg.show();

			// show the doc list on failure of the request
			startActivity(new Intent(this, DocListView.class));
		}
		
		Log.i(TAG, "got the unlocked doc from the server");

		// display the doc
		if (doc != null)
			displayUnlockedDoc(doc);
		else
			Log.i(TAG, "Server returned a null doc.");
	}

	/**
	 * Displays the unlocked doc.
	 * 
	 * @param doc
	 */
	private void displayUnlockedDoc(UnlockedDocument doc) {
		Log.i(TAG, "displaying the unlocked doc");

		if (doc != null) {
			titleBox.setText(doc.getTitle());
			contentBox.setText(doc.getContents());
		} else
			Log.i(TAG, "doc = null");
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
	public void lockDoc() {
		Log.i(TAG, "starting to lock the doc");
		
		LockedDocument doc = null;

		try {
			doc = service.lockDocument(currDocKey);
		} catch (LockUnavailable e) {
			Log.i(TAG, "Caught LockUnavailable when trying to lock the doc.");

			// alert the user that the lock doc operation failed
			Toast errorMsg = Toast.makeText(this,
					"Getting the lock failed - lock unavailable.",
					Toast.LENGTH_SHORT);
			errorMsg.show();

			// show the unlocked doc again
			getUnlockedDoc();
		} catch (InvalidRequest e) {
			Log.i(TAG, "Caught InvalidRequest when tyring to lock doc.");

			// alert the user that the lock doc operation failed
			Toast errorMsg = Toast.makeText(this,
					"Getting the lock failed - invalid request.",
					Toast.LENGTH_SHORT);
			errorMsg.show();

			// show the unlocked doc again
			getUnlockedDoc();
		}

		Log.i(TAG, "locked the doc");
		
		// now that we got the lock for this doc, switch to locked view
		startActivity(new Intent(this, LockedDocView.class).putExtra(
				intentDataKey, doc));
	}
}
