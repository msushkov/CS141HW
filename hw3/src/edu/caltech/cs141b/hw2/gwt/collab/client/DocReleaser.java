package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.caltech.cs141b.hw2.gwt.collab.shared.LockExpired;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;

/**
 * Used in conjunction with <code>CollaboratorService.releaseLock()</code>.
 */
public class DocReleaser implements AsyncCallback<Void> {
	
	private Collaborator collaborator;

	/**
	 * Constructor for DocReleaser.  Note that there is no internal state and
	 * thus no static constructor method is necessary.
	 * @param collaborator
	 */
	public DocReleaser(Collaborator collaborator) {
		this.collaborator = collaborator;
	}
	
	/**
	 * Releases the lock on the specified doc
	 * @param lockedDoc
	 */
	public void releaseLock(LockedDocument lockedDoc) {
		collaborator.collabService.releaseLock(lockedDoc, this);
	}

	@Override
	public void onFailure(Throwable caught) {
		if (caught instanceof LockExpired) {
			collaborator.statusUpdate("Lock had already expired; release failed.");
		} else {
			collaborator.statusUpdate("Error releasing document"
					+ "; caught exception " + caught.getClass()
					+ " with message: " + caught.getMessage());
			GWT.log("Error releasing document.", caught);
		}
	}

	@Override
	public void onSuccess(Void result) {
		// Do nothing...
	}
	
}

