package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Allow a client to logout
 */
public class DocLogout implements AsyncCallback<Void> {

	private Collaborator collaborator;

	/**
	 * Constructor for DocLogout. Note that there is no internal state and
	 * thus no static constructor method is necessary.
	 * 
	 * @param collaborator
	 */
	public DocLogout(Collaborator collaborator) {
		this.collaborator = collaborator;
	}

	
	public void logout() {
		// remove this doc from all the request queues and return any tokens
		System.out.println("logging out");
		collaborator.collabService.logout(collaborator.clientID, this);
		System.out.println("logged out");
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
