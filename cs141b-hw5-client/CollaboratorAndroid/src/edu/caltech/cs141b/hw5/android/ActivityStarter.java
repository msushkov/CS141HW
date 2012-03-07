package edu.caltech.cs141b.hw5.android;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ActivityStarter 
{
	// debugging
	private static String TAG = "ActivityStarter";
	
	// the key that identifies the doc that is passed between activities
	public static String intentDataKey = "doc";
	
	/**
	 * Start a new activity based on the input parameters.
	 * @param c The context (the enclosing listview object).
	 * @param cls The class to run.
	 */
	public static void startDocViewActivity(Context c, Class<?> cls, String docKey)
	{
		Log.i(TAG, "starting activity starter");
		
		// intent to start the activity
		Intent i = new Intent(c, cls);
		
		// add the doc key as a parameter to the activity
		i.putExtra(intentDataKey, docKey);
		
		// begin the activity
		c.startActivity(i);
	}

}
