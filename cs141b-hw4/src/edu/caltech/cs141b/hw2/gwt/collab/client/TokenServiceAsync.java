package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;

/**
 * The async counterpart of <code>CollaboratorService</code>.
 */
public interface TokenServiceAsync {
	void login(AsyncCallback<String> callback);

	void lockDocument(String documentKey,
			AsyncCallback<LockedDocument> callback);

	void releaseLock(LockedDocument doc, AsyncCallback<Void> callback);

}

