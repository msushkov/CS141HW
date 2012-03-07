package edu.caltech.cs141b.hw5.android;

import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import edu.caltech.cs141b.hw5.android.data.DocumentMetadata;
import edu.caltech.cs141b.hw5.android.data.InvalidRequest;
import edu.caltech.cs141b.hw5.android.data.UnlockedDocument;
import edu.caltech.cs141b.hw5.android.proto.CollabServiceWrapper;

/**
 * Displays the doc list.
 * @author msushkov
 *
 */
public class DocListView extends ListActivity {
	// debugging
	private static String TAG = "AndroidActivity";

	// makes server calls
	private CollabServiceWrapper service;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		service = new CollabServiceWrapper();
		
		getDocList();
	}

	/**
	 * Get the document list from the server.
	 */
	public void getDocList() {
		// get the docs from the server
		List<DocumentMetadata> docs = service.getDocumentList();

		// reverse list as to get new content on top
		Collections.reverse(docs);

		// set the model for the list view, uses toString() method to get names.
		setListAdapter(new ArrayAdapter<DocumentMetadata>(this,
				android.R.layout.simple_list_item_1, docs));

		final ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		// define the action for when the user presses a doc in the list
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// get the currently-selected doc
				
				DocumentMetadata currDoc = (DocumentMetadata) lv
						.getItemAtPosition(position);

				// TODO
				// display the given doc - maybe open a new text view?
				// displayDoc(currDoc);
				
				Intent unlockedDocIntent = new Intent(DocListView.this, UnlockedDocView.class);
				Bundle b = new Bundle();
				
				//unlockedDocIntent.putExtra("doc", currDoc);
				
				//startActivity();
			}
		});

		// Try lock and unlocking a document
		// try {
		// LockedDocument ld = service.lockDocument(metas.get(0).getKey());
		// Log.i(TAG, "locked");
		//
		// // try modify and save the document
		// LockedDocument mld = new LockedDocument(ld.getLockedBy(),
		// ld.getLockedUntil(), ld.getKey(),
		// ld.getTitle() + " mod1", ld.getContents());
		// service.saveDocument(mld);
		// Log.i(TAG, "saved");
		//
		// // Should get lock expired here service.releaseLock(ld); Log.i(TAG,
		// "unlocked"); } catch (LockExpired e) { Log.i(TAG,
		// "lock expired when attemping release."); } catch (LockUnavailable e)
		// { Log.i(TAG, "Lock unavailable."); } catch (InvalidRequest e) {
		// Log.i(TAG, "Invalid request"); }

		/*
		 * ListView tv = new ListView(this); tv.ad(docsInfo);
		 * setContentView(tv);
		 */
	}

	/**
	 * Create a menu when user presses the physical 'menu' button.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.listmenu, menu);
		return true;
	}
	
	/**
	 * Click handler for the menu buttons.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// display the list menu
		//setContentView(R.menu.listmenu);
		
		// which button did the user press?
		switch (item.getItemId()) 
		{
			// new doc is pressed
			case R.id.newDoc:
				// start new doc view activity
				return true;

			// refresh is pressed
			case R.id.refreshList:
				getDocList();
				//startActivity(new Intent(this, DocListView.class));
				return true;
			
			default:
				return true;
		}
	}
}
