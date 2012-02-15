package edu.caltech.cs141b.hw2.gwt.collab.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.caltech.cs141b.hw2.gwt.collab.shared.LockExpired;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockUnavailable;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("token")
public interface TokenService extends RemoteService {
	
	String login();
	
	/**
	 * Used to lock an existing document for editing.
	 * 
	 * @param documentKey the key of the document to lock
	 * @return a LockedDocument object containing the current document state
	 *         and the locking primites necessary to save the document
	 * @throws LockUnavailable if a lock cannot be obtained
	 */
	LockedDocument lockDocument(String documentKey) throws LockUnavailable;
	
	/**
	 * Used to release a lock that is no longer needed without saving.
	 * 
	 * @param doc the LockedDocument object returned by lockDocument(); any
	 *         modifications made to the document properties in this case are
	 *         ignored
	 * @throws LockExpired if the locking primitives in the supplied
	 *         LockedDocument object cannot be used to release the lock
	 */
	void releaseLock(LockedDocument doc) throws LockExpired;
	
}

