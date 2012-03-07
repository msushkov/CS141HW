package edu.caltech.cs141b.hw5.android;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import edu.caltech.cs141b.hw5.android.data.DocumentMetadata;
import edu.caltech.cs141b.hw5.android.data.LockedDocument;
import edu.caltech.cs141b.hw5.android.data.UnlockedDocument;
import edu.caltech.cs141b.hw5.android.proto.CollabServiceWrapper;

public class UnlockedDocView extends ListActivity {

	// debugging
	private static String TAG = "UnlockedDocView";

	// initial title + contents of new doc
	private static String newDocTitle = "Enter the document title.";
	private static String newDocContents = "Enter the document contents.";

	// makes server calls
	CollabServiceWrapper service;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		service = new CollabServiceWrapper();
		displayLockedDoc(extractUnlockedDoc());
	}
	
	/**
	 * Gets the unlocked doc that was passed to this activity.
	 * @return
	 */
	private UnlockedDocument extractUnlockedDoc()
	{
		Bundle extras = getIntent().getExtras();
		UnlockedDocument currDoc = null;
		Bundle b = null;

		// extract the bundle first (the bundle contains the doc)
		if (extras != null)
			b = (Bundle) extras.get(ActivityStarter.intentDataKey);

		// now extract the doc from the bundle
		if (b != null)
			currDoc = (UnlockedDocument) b.get(ActivityStarter.intentDataKey);
		
		return currDoc;
	}

	/**
	 * Display the locked doc.
	 */
	public void displayLockedDoc(UnlockedDocument currDoc) {
		
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
	 * New doc, refresh list, and get lock should be enabled. Refresh doc should
	 * be disabled.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {	
		// which button did the user press?
		switch (item.getItemId()) 
		{
		
		// TODO
		
		// new doc is pressed
		case R.id.newDoc:
			// start new locked doc view activity with new doc as arg
			LockedDocument newDoc = new LockedDocument(null, null, null,
					newDocTitle, newDocContents);
			ActivityStarter.startDocViewActivity(this, LockedDocView.class, newDoc);
			return true;

			// get doc list is pressed
			/*case R.id.docList:
			startActivity(new Intent(this, UnlockedDocView.class));
			return true;
			 */

		default:
			return true;
		}

	}
}

