package edu.caltech.cs141b.hw5.android;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
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
import edu.caltech.cs141b.hw5.android.proto.CollabServiceWrapper;

public class UnlockedDocView extends ListActivity {
	// makes server calls
	CollabServiceWrapper service;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		service = new CollabServiceWrapper();

		Bundle extras = getIntent().getExtras();
		
		displayUnlockedDoc();
	}

	/**
	 * Display the 
	 */
	public void displayUnlockedDoc() {
		

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
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.listmenu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// display the list menu
		setContentView(R.menu.listmenu);
		
		// which button did the user press?
		switch (item.getItemId()) 
		{
			// new doc is pressed
			case R.id.newDoc:
				
				return true;

			case R.id.docList:
				startActivity(new Intent(this, UnlockedDocView.class));
				return true;
			
			default:
				return true;
		}

	}
}

