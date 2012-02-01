package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.caltech.cs141b.hw2.gwt.collab.shared.LockUnavailable;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;

/**
 * Used in conjunction with <code>CollaboratorService.lockDocument()</code>.
 */
public class DocLocker implements AsyncCallback<LockedDocument> {
	
	private Collaborator collaborator;
	
	public DocLocker(Collaborator collaborator) {
		this.collaborator = collaborator;
	}
	
	public void lockDocument(String key) {
		collaborator.statusUpdate("Attempting to lock document.");
		collaborator.waitingKey = key;
		collaborator.collabService.lockDocument(key, this);
		collaborator.lockButton.setEnabled(false);
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
		collaborator.lockButton.setEnabled(true);
	}

	@Override
	public void onSuccess(LockedDocument result) {
		if (result.getKey().equals(collaborator.waitingKey)) {
			collaborator.statusUpdate("Lock retrieved for document.");
			gotDoc(result);
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
	protected void gotDoc(LockedDocument result) {
		// we got the lock for this document
		
		collaborator.readOnlyDoc = null;
		collaborator.lockedDoc = result;
		
		// enable the title and the contents widgets
		//collaborator.docContents.setEnabled(true);
		//collaborator.docTitle.setEnabled(true);
		collaborator.setContentsEnabled(true);
		collaborator.setTitleEnabled(true);
		
		//collaborator.docTitle.setValue(result.getTitle());
		//collaborator.docContents.setHTML(result.getContents());
		collaborator.docTitle = result.getTitle();
		collaborator.docContents = result.getContents();
		
		// set the document properties on the tabpanel
		collaborator.setTabPanelContents(result.getContents(), result.getTitle());
		
		collaborator.refreshDoc.setEnabled(false);
		collaborator.lockButton.setEnabled(false);
		collaborator.saveButton.setEnabled(true);
	}
	
}

