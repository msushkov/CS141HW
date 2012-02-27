package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Used to remove a client form the document queue
 */
public class DocLogout implements AsyncCallback<Void> {

	private Collaborator collaborator;

	/**
	 * Constructor for DocCloser. Note that there is no internal state and
	 * thus no static constructor method is necessary.
	 * 
	 * @param collaborator
	 */
	public DocLogout(Collaborator collaborator) {
		this.collaborator = collaborator;
	}

	
	public void logout() {
		// remove this doc from our request queue
		collaborator.collabService.logout(collaborator.clientID, this);
	}

	@Override
	public void onFailure(Throwable caught) {
		collaborator.statusUpdate("Error removing logging out"
				+ "; caught exception " + caught.getClass() + " with message: "
				+ caught.getMessage());
		GWT.log("Error logging out.", caught);
	}

	@Override
	public void onSuccess(Void result) {
		// Do nothing...
	}

}
