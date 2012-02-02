package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.TextBox;

import edu.caltech.cs141b.hw2.gwt.collab.shared.LockUnavailable;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;

/**
 * Used in conjunction with <code>CollaboratorService.lockDocument()</code>.
 */
public class DocLocker implements AsyncCallback<LockedDocument> {
	
	private Collaborator collaborator;
	String side;
	int index;
	
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
		collaborator.collabService.lockDocument(key, this);
		//collaborator.lockButton.setEnabled(false);
	}

	@Override
	public void onFailure(Throwable caught) {
		if (caught instanceof LockUnavailable) {
			collaborator.statusUpdate("LockUnavailable: " + caught.getMessage());
		} else {
			collaborator.statusUpdate("Error retrieving lock"
					+ "; caught exception " + caught.getClass()
					+ " with message: " + caught.getMessage());
			GWT.log("Error getting document lock.", caught);
		}
		// enable lock button
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
	 * Generalized so that it can be used elsewhere.  In particular, when
	 * creating a new document, a locked document is simulated by calling this
	 * function with a new LockedDocument object without the lock primitives.
	 * 
	 * @param result
	 */
	protected void gotDoc(LockedDocument result, String side, int index) {
		if (side.equals("left")) {
			collaborator.documentsLeft.set(index, result);
			TextBox box = collaborator.titleL.get(index);
			box.setValue(result.getTitle());
			box.setEnabled(true);
			RichTextArea area = collaborator.contentsL.get(index);
			area.setHTML(result.getContents());
			area.setEnabled(true);
		} else if (side.equals("right")) {
			
		}
		
		collaborator.refreshDoc.setEnabled(false);
		//collaborator.lockButton.setEnabled(false);
		//collaborator.saveButton.setEnabled(true);
	}
	
}

