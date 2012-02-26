package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Used in conjunction with <code>CollaboratorService.lockDocument()</code>.
 */
public class DocLocker implements AsyncCallback<Void> {

	// Stores a reference to the collaborator object so we can communicate with 
	// the client.
	private Collaborator collaborator;

	/**
	 * This function creates a new DocLocker and calls it. This function
	 * basically allows for multiple requests to be sent at the same time with
	 * different internal state associated with them.
	 * 
	 * @param collaborator
	 * @param key
	 * @param side
	 * @param ind
	 * @return
	 */
	public static void lockDoc(Collaborator collab, String key) {
		DocLocker dl = new DocLocker(collab);
		dl.lockDocument(key);
	}

	public DocLocker(Collaborator collaborator) {
		this.collaborator = collaborator;
	}

	/**
	 * Sends a request to be added to the locked document queue.
	 * @param key
	 * @param side
	 * @param index
	 */
	public void lockDocument(String key) {
		// this calls the server's method addToDocQueue: essentially
		// asks the server to enqueue this request for the lock
		collaborator.collabService.lockDocument(collaborator.clientID, key, this);
	}

	@Override
	public void onFailure(Throwable caught) {
		collaborator.statusUpdate("Error entering lock queue");
	}

	@Override
	public void onSuccess(Void result) {
		collaborator.statusUpdate("In document queue.");
	}
}
