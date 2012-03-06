package edu.caltech.cs141b.hw5.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import edu.caltech.cs141b.hw5.android.proto.CollabServiceWrapper;

public class CollaboratorAndroidActivity extends Activity {

	// debugging
	private static String TAG = "AndroidActivity";
	
	// makes server calls
	public CollabServiceWrapper service;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(TAG, "starting activity");
		
		service = new CollabServiceWrapper();

		Intent listIntent = new Intent(this, DocListView.class);
		//listIntent.putExtra("service", service);
		
		// start a new activity: in this case, the doc list view
		//startActivity();

		// Display a new document per default
		//createDoc();
		
		// can make it so that an activity returns some value - can use
		// this when selecting something in the doc list
	}

	/**
	 * Display the given doc
	 * 
	 * @param doc
	 */
	/*
	 * public void displayDoc(DocumentMetadata doc) { try { UnlockedDocument
	 * currDoc = service.getDocument(doc.getKey());
	 * 
	 * } catch (InvalidRequest e) { Log.i(TAG,
	 * "Caught 'invalid request' in displayDoc."); } }
	 */

	public void createDoc() {
		setContentView(R.layout.main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.collabmenu, menu);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		createDoc();

		switch (item.getItemId()) {
		case R.id.newDoc:
			return true;

		case R.id.docList:
			startActivity(new Intent(this, DocListView.class));
			return true;
		default:
			return true;
		}

	}
}