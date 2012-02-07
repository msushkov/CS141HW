package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

import edu.caltech.cs141b.hw2.gwt.collab.shared.LockUnavailable;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;

/**
 * Used in conjunction with <code>CollaboratorService.lockDocument()</code>.
 */
public class DocLocker implements AsyncCallback<LockedDocument> {

	private Collaborator collaborator;
	String side;         // is the current doc on the left or the right?
	private int index;   // which tab is the current doc on?

	public static void lockDoc(Collaborator collab, String key, String side, int ind) {
		DocLocker dl = new DocLocker(collab);
		dl.lockDocument(key, side, ind);
	}

	public DocLocker(Collaborator collaborator) {
		this.collaborator = collaborator;
	}

	public void lockDocument(String key, String side, int index) {
		collaborator.statusUpdate("Attempting to lock document.");
		collaborator.waitingKey = key;
		this.side = side;
		this.index = index;
		
		collaborator.collabService.lockDocument(key, this);
	}

	@Override
	public void onFailure(Throwable caught) {
		if (caught instanceof LockUnavailable) 
			collaborator.statusUpdate("LockUnavailable: " + caught.getMessage());
		else 
		{
			collaborator.statusUpdate("Error retrieving lock"
					+ "; caught exception " + caught.getClass()
					+ " with message: " + caught.getMessage());
			GWT.log("Error getting document lock.", caught);
		}

		// set and enable/disable correct buttons
		lockFailed();
	}

	@Override
	public void onSuccess(LockedDocument result) {
		if (result.getKey().equals(collaborator.waitingKey)) {
			collaborator.statusUpdate("Lock retrieved for document.");
			gotDoc(result, side, index);
		} else {
			collaborator.statusUpdate("Got lock for document which is "
					+ "no longer active.  Releasing lock.");
			collaborator.releaser.releaseLock(result);
		}
	}
	
	/**
	 * Called by when docLocker failed to acquire lock.
	 * @param side
	 * @param index
	 */
	protected void lockFailed()
	{				
		if (side.equals("left"))
			collaborator.setGenericObjects(true);
		else
			collaborator.setGenericObjects(false);				
		
		TextBox box = collaborator.titleList.get(index);
		TextArea area = collaborator.contentsList.get(index);
		
		// the user cannot edit the title and the contents of this doc
		box.setEnabled(false);
		area.setEnabled(false);

		// we need lock, removeTab, and refresh buttons
		collaborator.hPanel.clear();
		collaborator.hPanel.add(collaborator.lockButton);
		collaborator.hPanel.add(collaborator.removeTabButton);
		collaborator.hPanel.add(collaborator.refresh);

		collaborator.lockButton.setEnabled(true);
		collaborator.removeTabButton.setEnabled(true);
		collaborator.refresh.setEnabled(true);
	}


	/**
	 * If we successfully acquired the locker - can now edit the doc.
	 * @param result
	 * @param side
	 * @param index
	 */
	protected void gotDoc(LockedDocument result, String side, int index) {
		if (side.equals("left"))
			collaborator.setGenericObjects(true);
		else
			collaborator.setGenericObjects(false);				
		
		TextBox box = collaborator.titleList.get(index);
		TextArea area = collaborator.contentsList.get(index);		

		collaborator.docList.set(index, result);
		
		// set the title and contents of this doc to be the current thing on the page
		box.setValue(result.getTitle());
		area.setText(result.getContents());
		
		// the user can now edit the title and the contents of this doc
		box.setEnabled(true);
		area.setEnabled(true);

		// we need save, removeTab, and refresh buttons
		collaborator.hPanel.clear();
		collaborator.hPanel.add(collaborator.saveDocButton);
		collaborator.hPanel.add(collaborator.removeTabButton);
		collaborator.hPanel.add(collaborator.refresh);

		collaborator.saveDocButton.setEnabled(true);
		collaborator.removeTabButton.setEnabled(true);
		collaborator.refresh.setEnabled(false);
	}
}

