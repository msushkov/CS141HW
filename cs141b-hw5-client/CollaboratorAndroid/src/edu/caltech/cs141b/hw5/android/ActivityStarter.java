package edu.caltech.cs141b.hw5.android;

import java.io.Serializable;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import edu.caltech.cs141b.hw5.android.data.LockedDocument;
import edu.caltech.cs141b.hw5.android.data.UnlockedDocument;

public class ActivityStarter 
{
	// the key that identifies the doc that is passed between activities
	public static String intentDataKey = "doc";
	
	/**
	 * Start a new activity based on the input parameters.
	 * @param c The context (the enclosing listview object).
	 * @param cls The class to run.
	 */
	public static void startDocViewActivity(Context c, Class<?> cls, Object doc)
	{
		// intent to start the activity
		Intent i = new Intent(c, cls);
		
		// store the current unlocked or locked doc
		Bundle b = new Bundle();

		// put the correct doc in the bundle
		if (doc instanceof LockedDocument)
			b.putSerializable(intentDataKey, (Serializable) ((LockedDocument) doc));
		else if (doc instanceof UnlockedDocument)
			b.putSerializable(intentDataKey, (Serializable) ((UnlockedDocument) doc));
		i.putExtra(intentDataKey, b);
		
		// begin the activity
		c.startActivity(i);
	}

}
