package edu.caltech.cs141b.hw5.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import edu.caltech.cs141b.hw5.android.data.InvalidRequest;
import edu.caltech.cs141b.hw5.android.data.LockExpired;
import edu.caltech.cs141b.hw5.android.data.LockedDocument;
import edu.caltech.cs141b.hw5.android.data.UnlockedDocument;
import edu.caltech.cs141b.hw5.android.proto.CollabServiceWrapper;

public class LockedDocView extends Activity {

	// debugging
	private static String TAG = "LockedDocView";

	// the key that identifies the doc that is passed between activities
	public static String intentDataKey = "doc";

	// initial title + contents of new doc
	private static String newDocTitle = "Enter the document title.";
	private static String newDocContents = "Enter the document contents.";

	// makes server calls
	CollabServiceWrapper service;

	// the current locked doc we are dealing with
	private LockedDocument currDoc;

	// textboxes
	EditText titleBox;
	EditText contentBox;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(TAG, "created the locked doc view activity");

		service = new CollabServiceWrapper();

		setContentView(R.layout.lockeddocgui);
		titleBox = (EditText) findViewById(R.id.title);
		contentBox = (EditText) findViewById(R.id.content);

		extractLockedDoc();

		displayLockedDoc();

	}

	/**
	 * Gets the locked doc that was passed to this activity.
	 * 
	 * @return
	 */
	private void extractLockedDoc() {
		Log.i(TAG, "starting to extract locked doc");

		Bundle extras = getIntent().getExtras();
		LockedDocument doc = null;

		// extract the doc
		if (extras != null)
			doc = (LockedDocument) extras.get(intentDataKey);

		this.currDoc = doc;

		Log.i(TAG, "extracted locked doc");
	}

	/**
	 * Display the locked doc.
	 */
	public void displayLockedDoc() {

		Log.i(TAG, "starting to display locked doc");
		if (currDoc != null) {
			titleBox.setText(currDoc.getTitle());
			contentBox.setText(currDoc.getContents());
			Log.i(TAG, "currdoc != null");
			Log.i(TAG, currDoc.getTitle());
		} else {
			Log.i(TAG, "currdoc = null");
		}
		// TODO: display title and contents and let the user edit it

	}

	/**
	 * Create a menu when user presses the physical 'menu' button.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.lockedmenu, menu);
		return true;
	}

	/**
	 * Click handler for the menu buttons. Here the user has 3 options: create a
	 * new doc, save the doc, and refresh the doc list. New doc and refresh list
	 * should be enabled.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// which button did the user press?
		switch (item.getItemId()) {

		// create a new doc
		case R.id.newDoc:
			LockedDocument newDoc = new LockedDocument(null, null, null,
					newDocTitle, newDocContents);
			this.currDoc = newDoc;

			// do this activity again with a new doc
			displayLockedDoc();

			return true;

			// refresh the doc list
		case R.id.docList:
			startActivity(new Intent(this, DocListView.class));
			return true;

			// save this doc
		case R.id.saveDoc:
			saveDoc();
			return true;

		default:
			return true;
		}
	}

	/**
	 * Saves the current doc.
	 */
	public void saveDoc() {
		UnlockedDocument doc = null;

		try {
			doc = service.saveDocument(currDoc);
		} catch (LockExpired e) {
			Log.i(TAG, "Caught LockExpired when trying to save doc.");
			displayLockedDoc();
		} catch (InvalidRequest e) {
			Log.i(TAG, "Caught InvalidRequest when trying to save doc.");
			displayLockedDoc();
		}

		// start a new unlockedDocView activity
		startActivity(new Intent(this, UnlockedDocView.class).putExtra(
				intentDataKey, doc.getKey()));
	}
}
