package edu.caltech.cs141b.hw5.android;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

	// Time in ms a lock has
	private final static int LOCK_TIME = 30000;

	// debugging
	private static String TAG = "LockedDocView";

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

	// the current locked doc we are dealing with
	private LockedDocument currDoc;

	// textboxes
	private EditText titleBox;
	private EditText contentBox;

	// timer for the lock
	private Timer lockedTimer = new Timer();

	// Boolean indicating if the button pressed was back
	private boolean srcBack = false;

	// Indicates it save doc operation failed because of lock expired.
	private boolean isLockExpired = false;

	// Is the current doc a new doc?
	private boolean isNewDoc;

	// Did we just save the doc?
	private boolean isSaved = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "created the locked doc view activity");

		// need this to make server calls
		service = new CollabServiceWrapper();

		// set the view to be the 'locked doc gui'
		setContentView(R.layout.lockeddocgui);

		// get the styles for the text boxes
		titleBox = (EditText) findViewById(R.id.title);
		contentBox = (EditText) findViewById(R.id.content);

		// sets the currDoc class variable
		extractLockedDoc();

		// display the current doc
		display();
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
		this.isNewDoc = false;

		Log.i(TAG, "extracted locked doc");
	}

	/**
	 * Display currDoc. Checks whether currDoc is null or not (if not null 
	 * then calls another helper method).
	 */
	private void display()
	{
		// displays currDoc
		if (currDoc != null) 
		{
			// displays a non-null doc
			displayLockedDoc();

			// do not start a timer if we have a new doc
			if (!isNewDoc)
			{
				// inform the user how long they have to edit
				Toast.makeText(this, "Can edit the doc for the next " + LOCK_TIME / 1000 + 
						" seconds.", Toast.LENGTH_SHORT).show();

				// schedule a client-side timer that informs the user when time is up
				lockedTimer.schedule(new TimerTask() {
					private Handler updateUI = new Handler() {
						/** Tells the user when time is up. */
						@Override
						public void dispatchMessage(Message msg) {
							super.dispatchMessage(msg);

							isLockExpired = true;
							displayExpiredMessage();
						}
					};

					public void run() {
						try {
							updateUI.sendEmptyMessage(0);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}, LOCK_TIME);
			}
		} 
		else 
		{
			Log.i(TAG, "Cannot display - doc is null.");

			// inform the user that something went wrong
			Toast.makeText(this,
					"Something went wrong - document is null.",
					Toast.LENGTH_SHORT).show();

			// go back to list view (pass it false to show that this is not
			// startup)
			startActivity((new Intent(this, DocListView.class)).
					putExtra(boolKey, false));
		}		
	}
	
	/**
	 * Display the locked doc. Called after making the check that currDoc 
	 * not null.
	 */
	private void displayLockedDoc() {
		// assumes currDoc is non-null

		Log.i(TAG, "starting to display locked doc");

		// display the title and contents
		titleBox.setText(currDoc.getTitle());
		contentBox.setText(currDoc.getContents());

		// set the cursor to the end of the title text box
		titleBox.setSelection(titleBox.getText().length());

		Log.i(TAG, currDoc.getTitle());
	}

	/**
	 * Inform the user that the lock had expired.
	 */
	private void displayExpiredMessage() {
		// expired toast
		Toast.makeText(LockedDocView.this, "Lock expired!",
				Toast.LENGTH_SHORT).show();
	}

	//==========================================================================
	// MENU
	
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
		Log.i(TAG, "doc key: " + currDoc.getKey());

		// which button did the user press?
		switch (item.getItemId()) {

		// create a new doc
		case R.id.newDoc:		
			// if we had a new doc open before, dont release the lock on it
			// since it hasnt been saved
			if (currDoc.getKey() != null) {
				// release lock since we are closing the current doc for which
				// we likely hold the lock and are starting a new one
				releaseLock();

				// cancel the timer
				lockedTimer.cancel();
			}

			// create a new document
			LockedDocument newDoc = new LockedDocument(null, null, null,
					newDocTitle, newDocContents);
			this.isNewDoc = true;
			this.currDoc = newDoc;

			// do this activity again with a new doc
			displayLockedDoc();
			return true;

			// refresh the doc list
		case R.id.docList:
			// once we release the lock, go to the list view since this is what
			// the user requested (pass it false to say that this is not startup)
			startActivity((new Intent(this, DocListView.class)).
					putExtra(boolKey, false));
			return true;

			// save this doc
		case R.id.saveDoc:
			saveDoc();
			return true;

		default:
			return true;
		}
	}

	// END MENU
	//==========================================================================

	/**
	 * Saves the current doc.
	 */
	private void saveDoc() {
		UnlockedDocument doc = null;

		// set the title and contents of the doc we are trying to save
		// to be what is in the text boxes at this moment (whatever the
		// user input)
		if (currDoc != null) {
			try {
				// if the user made no changes to the doc
				if (currDoc.getTitle().equals(titleBox.getText().toString())
						&& currDoc.getContents().equals(
								contentBox.getText().toString())) {
					Log.i(TAG, "no changes to the doc, so not saving");

					// alert the user that we are not saving
					Toast.makeText(this,
							"No document changes; not saving.",
							Toast.LENGTH_SHORT).show();
				} else {
					// set the to-be-saved doc to have the most
					// recent title and contents
					currDoc.setTitle(titleBox.getText().toString());
					currDoc.setContents(contentBox.getText().toString());

					// make the server call to save this doc
					doc = service.saveDocument(currDoc);

					// save if successful
					if (doc != null)
						saveComplete(doc);
					else {
						Log.i(TAG, "Error - save could not complete " +
								"(server returned null).");
						throw new LockExpired();
					}
				}
			} catch (LockExpired e) {
				Log.i(TAG, "Caught LockExpired when trying to save doc.");

				isLockExpired = true;

				// inform the user that the save failed
				Toast.makeText(this, "Save failed - lock expired.",
						Toast.LENGTH_LONG).show();

				// start a new unlockedDocView activity that will
				// display the doc that
				// we had before since the save operation failed
				startActivity(new Intent(this, UnlockedDocView.class).putExtra(
						intentDataKey, currDoc.getKey()));

			} catch (InvalidRequest e) {
				Log.i(TAG, "Caught InvalidRequest when trying to save doc.");

				// alert the user that the save failed
				Toast.makeText(this, "Save failed - invalid request.", 
						Toast.LENGTH_SHORT).show();

				// go back to displaying the doc the way it was before
				displayLockedDoc();
			}
		}
	}
	
	/**
	 * Called when the saveDoc operation completes successfully.
	 * @param doc
	 */
	private void saveComplete(UnlockedDocument doc) {
		Log.i(TAG, "saved the doc");

		isSaved = true;

		// inform the user that we saved the doc
		Toast.makeText(this, "Document saved", Toast.LENGTH_SHORT).show();

		// start a new unlockedDocView activity that will display the doc
		// that was saved
		startActivity(new Intent(this, UnlockedDocView.class).putExtra(
				intentDataKey, doc.getKey()));
	}

	/**
	 * Release the lock for the current doc.
	 */
	private void releaseLock() {
		Log.i(TAG, "trying to release the lock");

		if (currDoc != null) {
			try {
				service.releaseLock(currDoc);
				Log.i(TAG, "released the lock");

				// inform the user of the release 
				// (don't do this if lock expired so user doesn't get confused)
				if (!isLockExpired)
					Toast.makeText(this, "Lock released.", Toast.LENGTH_SHORT).show();
			} 
			catch (LockExpired e) {
				// alert the user that the release failed
				Toast.makeText(this, "Lock release failed - lock expired.",
						Toast.LENGTH_SHORT).show();

				Log.i(TAG, "Caught LockExpired when trying to release lock.");
			} catch (InvalidRequest e) {
				// alert the user that the release failed
				Toast.makeText(this, "Lock release failed - invalid request.",
						Toast.LENGTH_SHORT).show();

				Log.i(TAG, "Caught InvalidRequest when trying to release lock.");
			}
		}
	}

	//==========================================================================
	// ACTIVITY METHODS
	
	/**
	 * Called when the user exits this view.
	 */
	@Override
	public void finish() {
		Log.i(TAG, "quitting!");

		// Cancel the timer
		lockedTimer.cancel();

		// user is leaving this view, so release the lock
		// of the current doc if it is not a new doc
		if (currDoc.getKey() != null && !isSaved) 
			releaseLock();

		super.finish();
	}
	
	/**
	 * Called when the user leaves this activity 
	 * (triggered by menu and back buttons).
	 */
	@Override
	protected void onPause() {
		if (!srcBack) 
			finish();

		super.onPause();
	}

	/**
	 * Called when the back button is pressed.
	 */
	@Override
	public void onBackPressed() {
		srcBack = true;
		super.onBackPressed();
	}
}
