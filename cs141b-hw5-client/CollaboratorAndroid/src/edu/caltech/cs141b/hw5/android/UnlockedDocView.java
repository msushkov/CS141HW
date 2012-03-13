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

	// the key that identifies the isStartup boolean that is passed
	// to the list view activity
	private static String boolKey = "isStartup";

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

		// need this to make server calls
		service = new CollabServiceWrapper();

		setContentView(R.layout.unlockeddocgui);

		titleBox = (EditText) findViewById(R.id.title);
		contentBox = (EditText) findViewById(R.id.content);

		// get the current doc's key (passed from another activity)
		extractUnlockedDocKey();

		// get the unlocked doc with the current key from the server
		if (currDocKey != null)
			getUnlockedDoc();
		else 
		{
			Log.i(TAG, "doc key is null.");

			// inform the user that something went wrong
			Toast.makeText(this, "Error - document key is null.", 
					Toast.LENGTH_SHORT).show();

			// go back to list view (pass it false to show that this is not
			// startup)
			startActivity((new Intent(this, 
					DocListView.class)).putExtra(boolKey, false));
		}
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
			Log.i(TAG, "got the unlocked doc from the server");
		} catch (InvalidRequest e) {
			Log.i(TAG, "Invalid request when getting doc.");

			// alert the user that the get doc operation failed
			Toast.makeText(this, "Getting the doc failed - invalid request.",
					Toast.LENGTH_SHORT).show();

			// show the doc list on failure of the request
			// (pass it false to show that this is not startup)
			startActivity((new Intent(this, 
					DocListView.class)).putExtra(boolKey, false));
		}

		// display the doc
		if (doc != null)
			displayUnlockedDoc(doc);
		else 
		{
			Log.i(TAG, "Error - server returned a null doc.");

			// inform the user that something went wrong
			Toast.makeText(this,"Sserver returned a null doc.",
					Toast.LENGTH_SHORT).show();

			// go back to list view
			// (pass it false to show that this is not startup)
			startActivity((new Intent(this, 
					DocListView.class)).putExtra(boolKey, false));
		}
	}

	/**
	 * Displays the unlocked doc.
	 * 
	 * @param doc
	 */
	private void displayUnlockedDoc(UnlockedDocument doc) {
		Log.i(TAG, "displaying the unlocked doc");

		titleBox.setText(doc.getTitle());
		contentBox.setText(doc.getContents());
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
			startActivity(new Intent(this, 
					LockedDocView.class).putExtra(intentDataKey, newDoc));
			return true;

		// refresh the doc list
		case R.id.docList:
			// (pass it false to show that this is not startup)
			startActivity((new Intent(this, 
					DocListView.class)).putExtra(boolKey, false));
			return true;

		// lock this doc
		case R.id.lockDoc:
			lockDoc();
			return true;

		// refresh this doc
		case R.id.refresh:
			getUnlockedDoc();
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
			Log.i(TAG, "locked the doc");

			lockComplete(doc);
		} catch (LockUnavailable e) {
			Log.i(TAG, "Caught LockUnavailable when trying to lock the doc.");

			// alert the user that the lock doc operation failed
			Toast.makeText(this, "Getting the lock failed - lock unavailable.",
					Toast.LENGTH_SHORT).show();

			// show the unlocked doc again
			getUnlockedDoc();
		} catch (InvalidRequest e) {
			Log.i(TAG, "Caught InvalidRequest when tyring to lock doc.");

			// alert the user that the lock doc operation failed
			Toast.makeText(this, "Getting the lock failed - invalid request.",
					Toast.LENGTH_SHORT).show();

			// show the unlocked doc again
			getUnlockedDoc();
		}
	}

	/**
	 * Called when the lockDoc operation successfully finishes.
	 * 
	 * @param doc
	 */
	private void lockComplete(LockedDocument doc) {
		// inform the user that we locked the doc
		Toast.makeText(this, "Document locked", Toast.LENGTH_SHORT).show();

		// now that we got the lock for this doc, switch to locked view
		startActivity(new Intent(this, 
				LockedDocView.class).putExtra(intentDataKey, doc));
	}

	@Override
	public void onBackPressed() {
		// go back to list view
		// (pass it false to say that this is not startup)
		startActivity((new Intent(this, 
				DocListView.class)).putExtra(boolKey, false));
		
		super.onBackPressed();
	}
}
