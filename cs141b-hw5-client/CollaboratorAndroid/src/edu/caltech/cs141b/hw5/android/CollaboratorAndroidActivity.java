package edu.caltech.cs141b.hw5.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import edu.caltech.cs141b.hw5.android.data.DocumentMetadata;
import edu.caltech.cs141b.hw5.android.data.InvalidRequest;
import edu.caltech.cs141b.hw5.android.data.UnlockedDocument;
import edu.caltech.cs141b.hw5.android.proto.CollabServiceWrapper;

public class CollaboratorAndroidActivity extends Activity {

	// debugging
	private static String TAG = "AndroidActivity";

	// makes server calls
	CollabServiceWrapper service;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(TAG, "starting activity");

		//service = new CollabServiceWrapper();  
		
		// start a new activity: in this case, the doc list view
		startActivity(new Intent(this, DocListView.class));
		
		// can make it so that an activity returns some value - can use
		// this when selecting something in the doc list
	}
	
	/**
	 * Display the given doc
	 * @param doc
	 */
	/*
	public void displayDoc(DocumentMetadata doc)
	{
		try {
			UnlockedDocument currDoc = service.getDocument(doc.getKey());
			
		} 
		catch (InvalidRequest e) {
			Log.i(TAG, "Caught 'invalid request' in displayDoc.");
		}
	}
	*/
}