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
import edu.caltech.cs141b.hw5.android.data.LockExpired;
import edu.caltech.cs141b.hw5.android.data.LockedDocument;
import edu.caltech.cs141b.hw5.android.data.UnlockedDocument;
import edu.caltech.cs141b.hw5.android.proto.CollabServiceWrapper;

public class LockedDocView extends Activity {
	// implements OnClickListener, OnFocusChangeListener {

	// debugging
	private static String TAG = "LockedDocView";

	// the key that identifies the doc that is passed between activities
	private static String intentDataKey = "doc";

	// initial title + contents of new doc
	private static String newDocTitle = "";
	private static String newDocContents = "";

	// makes server calls
	private CollabServiceWrapper service;

	// the current locked doc we are dealing with
	private LockedDocument currDoc;

	// textboxes
	private EditText titleBox;
	private EditText contentBox;

	boolean newDocument = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(TAG, "created the locked doc view activity");

		service = new CollabServiceWrapper();

		setContentView(R.layout.lockeddocgui);
		titleBox = (EditText) findViewById(R.id.title);
		contentBox = (EditText) findViewById(R.id.content);

		// titleBox.setOnClickListener(this);
		// contentBox.setOnClickListener(this);
		// titleBox.setOnFocusChangeListener(this);
		// contentBox.setOnFocusChangeListener(this);

		// sets the currDoc class variable
		extractLockedDoc();

		// displays currDoc
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
	private void displayLockedDoc() {

		Log.i(TAG, "starting to display locked doc");

		if (currDoc != null) {
			// display the title and contents
			titleBox.setText(currDoc.getTitle());
			contentBox.setText(currDoc.getContents());

			// set the cursor to the end of the title text box
			titleBox.setSelection(titleBox.getText().length());

			Log.i(TAG, "currdoc != null");
			Log.i(TAG, currDoc.getTitle());
		} else
			Log.i(TAG, "currdoc = null");
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
			// release the lock since we are closing the current doc for which
			// we likely hold the lock and are starting a new one

			// if we had a new doc open before, dont
			// release the lock on the first new doc. only release the lock
			// if the curr doc is not a new doc
			if (isNewDoc())
				releaseLock();

			LockedDocument newDoc = new LockedDocument(null, null, null,
					newDocTitle, newDocContents);
			this.currDoc = newDoc;

			// do this activity again with a new doc
			displayLockedDoc();
			return true;

			// refresh the doc list
		case R.id.docList:
			// release the lock since we are closing a doc for which
			// we likely hold the lock

			// if we had a new doc open before, dont
			// release the lock on the first new doc. only release the lock
			// if the curr doc is not a new doc
			if (isNewDoc())
				releaseLock();

			// once we release the lock, go to the list view since this is what
			// the user requested
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
	 * Checks if currDoc is a new doc.
	 * @return
	 */
	private boolean isNewDoc()
	{
		return (currDoc.getKey() != null && currDoc.getContents().equals(newDocTitle) &&
				currDoc.getTitle().equals(newDocContents));
	}

	/**
	 * Saves the current doc.
	 */
	private void saveDoc() {
		UnlockedDocument doc = null;

		// set the title and contents of the doc we are trying to save
		// to be what is in the text boxes at this moment (whatever the
		// user input)
		currDoc.setTitle(titleBox.getText().toString());
		currDoc.setContents(contentBox.getText().toString());

		try {
			doc = service.saveDocument(currDoc);
		} catch (LockExpired e) {
			// alert the user that the save failed
			Toast errorMsg = Toast.makeText(this,
					"Save failed - lock was expired.", Toast.LENGTH_SHORT);
			errorMsg.show();

			Log.i(TAG, "Caught LockExpired when trying to save doc.");
			displayLockedDoc();
		} catch (InvalidRequest e) {
			// alert the user that the save failed
			Toast errorMsg = Toast.makeText(this,
					"Save failed - invalid request.", Toast.LENGTH_SHORT);
			errorMsg.show();

			Log.i(TAG, "Caught InvalidRequest when trying to save doc.");
			displayLockedDoc();
		}

		// start a new unlockedDocView activity
		startActivity(new Intent(this, UnlockedDocView.class).putExtra(
				intentDataKey, doc.getKey()));
	}

	/**
	 * Release the lock for the current doc.
	 */
	private void releaseLock() {
		Log.i(TAG, "trying to release the lock");

		try {
			service.releaseLock(currDoc);
			Log.i(TAG, "released the lock");
		} catch (LockExpired e) {
			// alert the user that the release failed
			Toast errorMsg = Toast.makeText(this,
					"Lock release failed - lock expired.", Toast.LENGTH_SHORT);
			errorMsg.show();

			Log.i(TAG, "Caught LockExpired when trying to release lock.");
		} catch (InvalidRequest e) {
			// alert the user that the release failed
			Toast errorMsg = Toast.makeText(this,
					"Lock release failed - invalid request.",
					Toast.LENGTH_SHORT);
			errorMsg.show();

			Log.i(TAG, "Caught InvalidRequest when trying to release lock.");
		}
	}

	// @Override
	// public void onClick(View v) {
	//
	// switch (v.getId()) {
	// case R.id.title:
	// Log.i(TAG, "Title pressed");
	//
	// if (titleBox.getText().toString().equals(newDocTitle)) {
	// titleBox.setText("");
	//
	// }
	// break;
	// case R.id.content:
	// Log.i(TAG, "Content pressed");
	// if (contentBox.getText().toString().equals(newDocContents)) {
	// contentBox.setText("");
	// }
	// break;
	// default:
	// break;
	// }
	// }
	//
	// @Override
	// public void onFocusChange(View v, boolean hasFocus) {
	// // if (hasFocus) {
	// // Log.i(TAG, "focusChange");
	// // switch (v.getId()) {
	// // case R.id.title:
	// // Log.i(TAG, "Title pressed");
	// //
	// // if (titleBox.getText().toString().equals(newDocTitle)) {
	// // titleBox.setText("");
	// //
	// // }
	// // break;
	// // case R.id.content:
	// // Log.i(TAG, "Content pressed");
	// // if (contentBox.getText().toString().equals(newDocContents)) {
	// // contentBox.setText("");
	// // }
	// // break;
	// // default:
	// // break;
	// // }
	// // }
	// }

}
