package edu.caltech.cs141b.hw5.android;

import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import edu.caltech.cs141b.hw5.android.data.DocumentMetadata;
import edu.caltech.cs141b.hw5.android.data.InvalidRequest;
import edu.caltech.cs141b.hw5.android.data.UnlockedDocument;
import edu.caltech.cs141b.hw5.android.proto.CollabServiceWrapper;

public class DocListView extends ListActivity {
	// debugging
	private static String TAG = "AndroidActivity";

	// makes server calls
	CollabServiceWrapper service;

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
				DocumentMetadata selectedDoc = (DocumentMetadata) lv
						.getItemAtPosition(position);

				try {
					UnlockedDocument doc = service.getDocument(selectedDoc
							.getKey());

				} catch (InvalidRequest e) {
					Log.d(TAG, "Invalid request");
					e.printStackTrace();
				}

				// TODO
				// display the given doc - maybe open a new text view?
				// displayDoc(currDoc);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.listmenu, menu);
		return true;

	}
}
