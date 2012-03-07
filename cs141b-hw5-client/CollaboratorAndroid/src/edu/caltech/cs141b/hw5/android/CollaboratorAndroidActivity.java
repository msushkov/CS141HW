package edu.caltech.cs141b.hw5.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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
}


//Try lock and unlocking a document
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