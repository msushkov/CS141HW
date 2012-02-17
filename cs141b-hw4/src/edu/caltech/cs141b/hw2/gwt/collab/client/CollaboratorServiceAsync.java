package edu.caltech.cs141b.hw2.gwt.collab.client;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.caltech.cs141b.hw2.gwt.collab.shared.DocumentMetadata;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;

/**
 * The async counterpart of <code>CollaboratorService</code>.
 */
public interface CollaboratorServiceAsync {

	
	void login(String clientID, AsyncCallback<String> callback);
	
	void getDocumentList(AsyncCallback<List<DocumentMetadata>> callback);

	void lockDocument(String clientID, String documentKey,
			AsyncCallback<Void> callback);

	void getDocument(String documentKey,
			AsyncCallback<UnlockedDocument> callback);

	void saveDocument(String clientID, LockedDocument doc,
			AsyncCallback<UnlockedDocument> callback);

	void releaseLock(String clientID, LockedDocument doc, AsyncCallback<Void> callback);

	void getLockedDocument(String clientID, String documentKey,
			AsyncCallback<LockedDocument> callback);

	void leaveLockQueue(String clientID, String documentKey,
			AsyncCallback<Void> callback);
	
	void cleanLock(String documentKey,
			AsyncCallback<Void> callback);

}

