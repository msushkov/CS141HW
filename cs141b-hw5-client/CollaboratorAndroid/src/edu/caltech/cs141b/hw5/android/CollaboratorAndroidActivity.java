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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(TAG, "starting activity");
		
		// start a new activity: in this case, the doc list view
		startActivity(new Intent(this, DocListView.class));
	}
	
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.collabmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		setContentView(R.layout.main);

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
	*/
}