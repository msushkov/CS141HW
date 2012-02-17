package edu.caltech.cs141b.hw2.gwt.collab.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.caltech.cs141b.hw2.gwt.collab.shared.DocumentMetadata;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockExpired;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockUnavailable;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("collab")
public interface CollaboratorService extends RemoteService {

	String login(String clientID);

	/**
	 * Used to get a list of the currently available documents.
	 * 
	 * @return a list of the metadata of the currently available documents
	 */
	List<DocumentMetadata> getDocumentList();

	/**
	 * Used to enter the queue for a locked document.
	 * 
	 * @param documentKey
	 *            the key of the document to lock
	 */
	 void lockDocument(String clientID, String documentKey);

	/**
	 * Used to retrieve a document you were just notified that you have the lock
	 * for
	 * 
	 * @param clientID
	 * @param documentKey
	 * @return
	 * @throws LockUnavailable
	 */
	LockedDocument getLockedDocument(String clientID, String documentKey)
			throws LockUnavailable;

	/**
	 * Used to retrieve a document in read-only mode.
	 * 
	 * @param documentKey
	 *            the key of the documen to read
	 * @return an UnlockedDocument object which contains the entire document but
	 *         without any locking primitives
	 */
	UnlockedDocument getDocument(String documentKey);

	/**
	 * Used to save a currently locked document.
	 * 
	 * @param doc
	 *            the LockedDocument object returned by lockDocument(), with the
	 *            document properties (but not the locking primitives)
	 *            potentially modified
	 * @return the read-only version of the saved document
	 * @throws LockExpired
	 *             if the locking primitives in the supplied LockedDocument
	 *             object cannot be used to modify the document
	 */
	UnlockedDocument saveDocument(String clientID, LockedDocument doc) throws LockExpired;

	/**
	 * Used to release a lock that is no longer needed without saving.
	 * 
	 * @param doc
	 *            the LockedDocument object returned by lockDocument(); any
	 *            modifications made to the document properties in this case are
	 *            ignored
	 * @throws LockExpired
	 *             if the locking primitives in the supplied LockedDocument
	 *             object cannot be used to release the lock
	 */
	void releaseLock(String clientID, LockedDocument doc) throws LockExpired;
	
	/**
	 * Used to leave the document queue prematurely
	 * @param clientID
	 * @param documentKey
	 */
	void leaveLockQueue(String clientID, String documentKey);

	void cleanLock(String documentKey);

}
