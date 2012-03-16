package edu.caltech.cs141b.hw2.gwt.collab.server;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService;
import edu.caltech.cs141b.hw2.gwt.collab.shared.DocumentMetadata;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockExpired;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockUnavailable;
import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class CollaboratorServiceImpl extends RemoteServiceServlet implements
		CollaboratorService {

	// This is the time in seconds that clients can lock a document
	private static final int LOCK_TIME = 30;

	// This is the key for the locked document list
	private static final Key lockListKey = KeyFactory.createKey(
			LockedDocuments.class.getSimpleName(), "lockedDocuments");

	// This is a bit of a hack that takes advantage of the fact all the fields
	// are static. We need this static field to allow ClearLockServlet to access
	// some of the non static functions of this class.
	private static CollaboratorServiceImpl server = new CollaboratorServiceImpl();

	private final int TRANSACTION_ATTEMPTS = 1000;

	/**
	 * Cleans lock for an individual document
	 * 
	 * @param docKey
	 *            The dockey for the doc we want to clean the locks of.
	 */
	public void cleanLock(String docKey) {

		// Get the PM
		PersistenceManager pm = PMF.get().getPersistenceManager();

		String lockedBy = null;

		// boolean marking whether the doc is to be returned to the server
		Key key = KeyFactory.stringToKey(docKey);

		// Retrieve the document for the given key
		Document doc = pm.getObjectById(Document.class, key);

		// Unlock if lock expired
		Date lockedTil = doc.getLockedUntil();
		if (lockedTil != null
				&& lockedTil.before(new Date(System.currentTimeMillis()))) {
			lockedBy = doc.getLockedBy();

			// If the document lock was expired return the given document to
			// the server
			receiveToken(lockedBy, docKey);

		}

		pm.close();

	}

	/**
	 * Cleans locks for all currently locked documents, called by the cronjob
	 */
	public static void cleanLocks() {
		// Get the PM
		PersistenceManager pm = PMF.get().getPersistenceManager();

		LockedDocuments lockedDocsObj = pm.getObjectById(LockedDocuments.class,
				lockListKey);

		// Set of all locked document keys
		Set<String> lockedDocKeys = lockedDocsObj.getLockedDocs();

		for (String docKey : lockedDocKeys) {
			String lockedBy = null;
			Key key = KeyFactory.stringToKey(docKey);
			Document doc = pm.getObjectById(Document.class, key);

			// Unlock if lock expired
			if (doc.getLockedBy() != null
					&& doc.getLockedUntil().before(
							new Date(System.currentTimeMillis()))) {

				lockedBy = doc.getLockedBy();
				// set the document to be returned to the server
				server.receiveToken(lockedBy, docKey);

			}

		}
		// Finally close the PM
		pm.close();
	}

	/**
	 * Get the list of the documents in the datastore.
	 * 
	 * @see edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#getDocumentList()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<DocumentMetadata> getDocumentList() {

		// Get the PM
		PersistenceManager pm = PMF.get().getPersistenceManager();

		// Make the Document query
		Query query = pm.newQuery(Document.class);

		ArrayList<DocumentMetadata> docList = new ArrayList<DocumentMetadata>();

		try {
			// Query the Datastore and iterate through all the Documents in the
			// Datastore
			for (Document doc : (List<Document>) query.execute()) {
				// Get the document metadata from the Documents
				DocumentMetadata metaDoc = new DocumentMetadata(doc.getKey(),
						doc.getTitle());
				docList.add(metaDoc);
			}

			// Return the list of docs
			return docList;
		} finally {
			// Do cleanup
			query.closeAll();
			pm.close();
		}

	}

	/**
	 * Retrieves the given doc from the datastore.
	 * 
	 * @see edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#getDocument
	 *      (java.lang.String)
	 */
	@Override
	public UnlockedDocument getDocument(String documentKey) {
		// Get the PM
		PersistenceManager pm = PMF.get().getPersistenceManager();

		try {
			// Create the key for this document
			Key key = KeyFactory.stringToKey(documentKey);
			// Use the key to retrieve the document
			Document doc = pm.getObjectById(Document.class, key);

			// Return the unlocked document, will get cleaned up as finally
			// trumps return
			return doc.getUnlockedDoc();
		} finally {
			// Do cleanup
			pm.close();
		}
	}

	/**
	 * Saves the given document in the datastore.
	 * 
	 * @see edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#saveDocument
	 *      (edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument)
	 */
	@Override
	public UnlockedDocument saveDocument(String clientID, LockedDocument doc)
			throws LockExpired {
		// Number of times to retry before throwing a concurrent exception error
		int retries = TRANSACTION_ATTEMPTS;

		// Get the PM
		PersistenceManager pm = PMF.get().getPersistenceManager();

		Document toSave;
		// Get the document's key
		String stringKey = doc.getKey();

		pm.close();

		while (true) {
			pm = PMF.get().getPersistenceManager();
			Transaction t = pm.currentTransaction();
			try {
				// Starting transaction...
				t.begin();

				// If the doc has no key, it's a new document, so create a new
				// document
				if (stringKey == null) {
					// First unlock it
					toSave = new Document(doc.unlock());
				} else {

					// Create the key
					Key key = KeyFactory.stringToKey(stringKey);

					// Get the document corresponding to the key
					toSave = pm.getObjectById(Document.class, key);

					// Get the lock information - saveDocument can only be
					// called if
					// there is a lock or if it's a new document
					String lockedBy = toSave.getLockedBy();
					Date lockedUntil = toSave.getLockedUntil();

					// Get the client's ID
					String identity = clientID;

					// Check that the person trying to save has the lock and
					// that
					// the lock hasn't expired
					if (lockedBy != null
							&& lockedBy.equals(identity)
							&& lockedUntil.after(new Date(System
									.currentTimeMillis()))) {
						// If both are fulfilled, update and unlock the doc
						toSave.update(doc);
						// toSave.unlock();

					} else {
						// Set to null to prevent the token from being returned
						stringKey = null;

						// Otherwise, throw an exception
						throw new LockExpired();
					}
				}

				// Now write the results to Datastore
				pm.makePersistent(toSave);

				// ...Ending transaction
				t.commit();

				// Return the unlocked document (also breaks)
				return toSave.getUnlockedDoc();
			} catch (ConcurrentModificationException e) {
				// Don't throw exception until failed for x number of times.
				if (retries == 0) {
					throw e;
				}
				retries--;
			} finally {
				// Do some cleanup
				if (t.isActive()) {
					t.rollback();
				}

				pm.close();

				// Now, take the token back - even if there was a concurrent
				// modification exception or some other exception besides
				// LockExpired.
				if (stringKey != null) {
					receiveToken(clientID, stringKey);
				}
			}
		}
	}

	/**
	 * This fetches the locked document list and adds the specified document to
	 * it
	 * 
	 * @param docKey
	 *            The doc to add
	 */
	private void addLockedDoc(String docKey) {
		// Number of times to retry before throwing a concurrent exception error
		int retries = TRANSACTION_ATTEMPTS;

		while (true) {
			PersistenceManager pm = PMF.get().getPersistenceManager();
			Transaction t = pm.currentTransaction();

			try {
				t.begin();
				LockedDocuments lockedDocs;
				try {
					// Try to retrieve the locked document collection
					lockedDocs = pm.getObjectById(LockedDocuments.class,
							lockListKey);
				} catch (JDOObjectNotFoundException ex) {
					// If you can't find it, make a new one
					lockedDocs = new LockedDocuments();
				}
				// Add the documen to the queue
				lockedDocs.addDocument(docKey);
				pm.makePersistent(lockedDocs);
				t.commit();

				// if got here it means transaction succeeded, breaking the
				// loop.
				break;
			} catch (ConcurrentModificationException e) {
				if (retries == 0) {
					throw e;
				}
				retries--;
			} finally {
				if (t.isActive()) {
					t.rollback();
				}

				pm.close();
			}
		}
	}

	/**
	 * This fetches the locked document list and removes the specified document
	 * from it
	 * 
	 * @param docKey
	 *            The doc to remove
	 */
	private void rmLockedDoc(String docKey) {
		// Number of times to retry before throwing a concurrent exception error
		int retries = TRANSACTION_ATTEMPTS;

		while (true) {
			PersistenceManager pm = PMF.get().getPersistenceManager();
			Transaction t = pm.currentTransaction();
			try {
				t.begin();
				LockedDocuments lockedDocs;
				try {
					// Get the locked document collection
					lockedDocs = pm.getObjectById(LockedDocuments.class,
							lockListKey);
				} catch (JDOObjectNotFoundException ex) {
					// If none exists, create a new one
					lockedDocs = new LockedDocuments();
				}
				// Remove the document from this queue
				lockedDocs.removeDocument(docKey);
				pm.makePersistent(lockedDocs);
				t.commit();
				// if got here it means transaction succeeded, breaking the
				// loop.
				break;
			} catch (ConcurrentModificationException e) {
				if (retries == 0) {
					throw e;
				}
				retries--;
			}

			finally {
				if (t.isActive()) {
					t.rollback();
				}

				pm.close();
			}
		}
	}

	/**
	 * Called whenever a client returns a token for an item. This function will
	 * update the token map and then send a token if possible.
	 * 
	 * @param clientID
	 *            The ID of the client sending the message
	 * @param docKey
	 *            The key of the document whose token we are receiving
	 */
	private void receiveToken(String clientID, String docKey) {

		// Number of times to retry before throwing a concurrent exception error
		int retries = TRANSACTION_ATTEMPTS;

		Document toSave;
		String newClientID = null;

		while (true) {
			PersistenceManager pm = PMF.get().getPersistenceManager();
			Transaction t = pm.currentTransaction();

			try {
				// Starting transaction...
				t.begin();
				// Create the key
				Key key = KeyFactory.stringToKey(docKey);

				// Get the document corresponding to the key
				toSave = pm.getObjectById(Document.class, key);

				// Set next client if there's a queue
				newClientID = toSave.pollNextClient();

				toSave.unlock();

				pm.makePersistent(toSave);

				// Add the dockey to the client as well
				Client client = pm.getObjectById(Client.class, clientID);
				client.rmDoc(docKey);
				pm.makePersistent(client);

				t.commit();
				// if got here it means transaction succeeded, breaking the
				// loop.
				break;
			} catch (ConcurrentModificationException e) {
				if (retries == 0) {
					throw e;
				}
				retries--;
			}

			finally {
				// Do some cleanup
				if (t.isActive()) {
					t.rollback();
				}

				pm.close();

				// Now, try to send a token if we can
				if (newClientID != null) {
					sendToken(newClientID, docKey);
				}

				// Otherwise remove the document from locked documents.
				else {
					rmLockedDoc(docKey);
				}
			}
		}
	}

	/**
	 * Send a token for the docKey out to the specified client
	 * 
	 * @param clientID
	 *            The ID of the client sending the message
	 * @param docKey
	 *            The key of the document whose token we are receiving
	 */
	private void sendToken(String clientID, String docKey) {

		// Add to list of locked documents if not already in there

		Document toSave;
		Date endTime;

		// Number of times to retry before throwing a concurrent exception error
		int retries = TRANSACTION_ATTEMPTS;
		while (true) {
			PersistenceManager pm = PMF.get().getPersistenceManager();
			// Create the transaction
			Transaction t = pm.currentTransaction();
			try {
				// Starting transaction...
				t.begin();

				// Create the key
				Key key = KeyFactory.stringToKey(docKey);

				// Get the document corresponding to the key
				toSave = pm.getObjectById(Document.class, key);
				endTime = new Date(System.currentTimeMillis() + LOCK_TIME
						* 1000);
				toSave.lock(endTime, clientID);

				// Add key to locked documents (will only get added if it isn't
				// already there)
				pm.makePersistent(toSave);

				// End the transaction
				t.commit();

				// Finally, inform the client that the doc is locked and ready
				// for
				// them
				getChannelService().sendMessage(
						new ChannelMessage(clientID, docKey));

				// if got here it means transaction succeeded, breaking the
				// loop.
				break;

			} catch (ConcurrentModificationException e) {
				if (retries == 0) {
					throw e;
				}
				retries--;
			} finally {
				// Do some cleanup
				if (t.isActive()) {
					t.rollback();
				}
				pm.close();
			}
		}
	}

	/**
	 * Release the lock of the given document.
	 * 
	 * @see edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#releaseLock
	 *      (edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument)
	 */
	@Override
	public void releaseLock(String clientID, LockedDocument doc)
			throws LockExpired {

		// Number of times to retry before throwing a concurrent exception error
		int retries = TRANSACTION_ATTEMPTS;

		while (true) {
			// Get the PM
			PersistenceManager pm = PMF.get().getPersistenceManager();
			Transaction t = pm.currentTransaction();
			try {
				// Starting transaction...
				t.begin();

				// Get the doc's key
				Key key = KeyFactory.stringToKey(doc.getKey());

				// Use the key to retrieve the doc
				Document toSave = pm.getObjectById(Document.class, key);

				// Get the lock information - saveDocument can only be called if
				// there is a lock or if it's a new document
				String lockedBy = toSave.getLockedBy();

				// Get the client's identity
				String identity = clientID;

				// Make sure that the person unlocking is the person who locked
				// the
				// doc.
				if (lockedBy != null && lockedBy.equals(identity)) {
					// Unlock it
					toSave.unlock();

					// And store it in the Datastore
					pm.makePersistent(toSave);

				} else {
					// Otherwise, throw an exception
					throw new LockExpired("You no longer have the lock");

				}

				// ...Ending transaction
				t.commit();

				// Indicate that the token has been returned
				receiveToken(clientID, doc.getKey());

				// if got here it means transaction succeeded, breaking the
				// loop.
				break;

			} catch (ConcurrentModificationException e) {
				if (retries == 0) {
					throw e;
				}
				retries--;
			} finally {
				// Do some cleanup
				if (t.isActive()) {
					t.rollback();
				}
				pm.close();
			}
		}
	}

	/**
	 * Locks the given document.
	 * 
	 * @see edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#lockDocument
	 *      (java.lang.String)
	 */
	@Override
	public void lockDocument(String clientID, String docKey) {
		// Handle the case where the token map doesn't have the docKey - no
		// client has tried to access it yet

		PersistenceManager pm = PMF.get().getPersistenceManager();
		Document toSave = null;
		Client client = pm.getObjectById(Client.class, clientID);

		pm.close();
		// Number of times to retry before throwing a concurrent exception error
		int retries = TRANSACTION_ATTEMPTS;
		while (true) {
			pm = PMF.get().getPersistenceManager();
			Transaction t = pm.currentTransaction();
			try {
				// Starting transaction...
				t.begin();
				// Create the key
				Key key = KeyFactory.stringToKey(docKey);

				// Get the document corresponding to the key
				toSave = pm.getObjectById(Document.class, key);
				if (toSave.isLocked()) {
					System.out.println("locked");
					toSave.addToWaitingList(clientID);
				}

				client.addDoc(docKey);
				addLockedDoc(docKey);

				pm.makePersistent(client);
				pm.makePersistent(toSave);

				t.commit();

				// if got here it means transaction succeeded, breaking the
				// loop.
				break;

			} catch (ConcurrentModificationException e) {
				if (retries == 0) {
					throw e;
				}
				retries--;
			}

			finally {
				// Do some cleanup
				if (t.isActive()) {
					t.rollback();
				}

				pm.close();

				// UGH I have to put this here b/c sendToken can't go in a
				// transaction >.> I'm sorry...
				if (!toSave.isLocked()) {
					sendToken(clientID, docKey);
				}
			}
		}
	}

	/**
	 * Called when the client first connects to the app.
	 * 
	 * @see edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#login(java
	 *      .lang.String)
	 */
	@Override
	public String login(String clientID) {
		// Create a datastore client with key = Client ID
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Client client = new Client(clientID);

		pm.makePersistent(client);

		pm.close();

		return getChannelService().createChannel(clientID);
	}

	/**
	 * Returns a channel service that can be used to do channel operations
	 * 
	 * @return A channel service that can be used to do channel operations
	 */
	private ChannelService getChannelService() {
		return ChannelServiceFactory.getChannelService();
	}

	/**
	 * Get the locked doc with the given key.
	 * 
	 * @see edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#
	 *      getLockedDocument(java.lang.String, java.lang.String)
	 */
	@Override
	public LockedDocument getLockedDocument(String clientID, String documentKey)
			throws LockUnavailable {

		// Number of times to retry before throwing a concurrent exception error
		int retries = TRANSACTION_ATTEMPTS;
		while (true) {
			// Get the PM
			PersistenceManager pm = PMF.get().getPersistenceManager();
			Transaction t = pm.currentTransaction();
			try {
				// Starting transaction...
				t.begin();

				// Create the key
				Key key = KeyFactory.stringToKey(documentKey);

				// Get the document from the Datastore
				Document toSave = pm.getObjectById(Document.class, key);

				String identity = clientID;

				// If the doc is locked and you own it...
				if (!toSave.isLocked()
						|| !toSave.getLockedBy().equals(identity)
						|| toSave.getLockedUntil().before(
								new Date(System.currentTimeMillis()))) {
					throw new LockUnavailable("This lock is not yours");
				}

				// ...Ending transaction
				t.commit();

				// Return the locked document and break the loop
				return toSave.getLockedDoc();
			} catch (ConcurrentModificationException e) {
				if (retries == 0) {
					throw e;
				}
				retries--;
			} finally {
				// Do some cleanup
				if (t.isActive()) {
					t.rollback();
				}
				pm.close();
			}
		}
	}

	/**
	 * Remove the given client from the given document queue. (This client is no
	 * longer waiting on a lock for that doc.) It also makes sure that the token
	 * gets passed on to the next document if the client was holding the token.
	 * 
	 * @see edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#leaveLockQueue
	 *      (java.lang.String, java.lang.String)
	 */
	@Override
	public void leaveLockQueue(String clientID, String documentKey) {
		Document toSave = null;

		// Number of times to retry before throwing a concurrent exception error
		int retries = TRANSACTION_ATTEMPTS;
		while (true) {
			PersistenceManager pm = PMF.get().getPersistenceManager();
			Transaction t = pm.currentTransaction();

			try {
				// Starting transaction...
				t.begin();
				// Create the key
				Key key = KeyFactory.stringToKey(documentKey);

				// Get the document corresponding to the key
				toSave = pm.getObjectById(Document.class, key);
				toSave.removeClient(clientID);

				pm.makePersistent(toSave);

				t.commit();

				// if got here it means transaction succeeded, breaking the
				// loop.
				break;

			} catch (Exception e) {
				if (retries == 0) {
					e.printStackTrace();
					System.out.println("lock queue error");
				}
				retries--;
			} finally {

				// Do some cleanup
				if (t.isActive()) {
					System.out.println("lock queue rollback");
					t.rollback();
				}

				pm.close();

				// If neccessary, return the token
				if (toSave != null && toSave.getLockedBy().equals(clientID)) {
					receiveToken(clientID, documentKey);
				}
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#logout(java
	 * .lang.String)
	 */
	@Override
	public void logout(String clientID) {
		System.out.println("logout");
		PersistenceManager pm = PMF.get().getPersistenceManager();

		// Read all document keys the client has not finished with (queue + use)
		List<String> docKeys = pm.getObjectById(Client.class, clientID)
				.getLockedDocs();
		pm.close();

		// Remove from all these entities
		for (String docKey : docKeys) {
			leaveLockQueue(clientID, docKey);
		}

		// Number of times to retry before throwing a concurrent exception error
		int retries = TRANSACTION_ATTEMPTS;
		while (true) {
			pm = PMF.get().getPersistenceManager();
			Transaction t = pm.currentTransaction();
			try {
				t.begin();

				// Get the client
				Client client = pm.getObjectById(Client.class, clientID);

				// Delete the client
				pm.deletePersistent(client);

				t.commit();

				// if got here it means transaction succeeded, breaking the
				// loop.
				break;
			} catch (ConcurrentModificationException e) {
				if (retries == 0) {
					throw e;
				}
				retries--;
			} finally {
				if (t.isActive()) {
					t.rollback();
				}

				pm.close();
			}
		}
	}
}
