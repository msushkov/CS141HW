package edu.caltech.cs141b.hw5.gwt.collab.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.caltech.cs141b.hw5.gwt.collab.client.CollaboratorService;
import edu.caltech.cs141b.hw5.gwt.collab.shared.DocumentMetadata;
import edu.caltech.cs141b.hw5.gwt.collab.shared.LockExpired;
import edu.caltech.cs141b.hw5.gwt.collab.shared.LockUnavailable;
import edu.caltech.cs141b.hw5.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw5.gwt.collab.shared.UnlockedDocument;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class CollaboratorServiceImpl extends RemoteServiceServlet implements
		CollaboratorService {

	private static final Logger log = Logger
			.getLogger(CollaboratorServiceImpl.class.toString());

	/**
	 * Gets the doc list.
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
	 * Wrapper for lockDocument() - gets the IP of the user.
	 */
	@Override
	public LockedDocument lockDocument(String documentKey)
			throws LockUnavailable {
		return lockDocument(documentKey, getThreadLocalRequest()
				.getRemoteAddr());
	}

	/**
	 * Locks the doc with the given key for the given user.
	 * 
	 * @param documentKey
	 * @param ip
	 * @return The doc that was locked.
	 * @throws LockUnavailable
	 */
	public LockedDocument lockDocument(String documentKey, String ip)
			throws LockUnavailable {
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

			// If the doc is locked...
			if (toSave.isLocked()) {
				// But it should be unlocked...
				// System.out.println(toSave.getLockedUntil());
				if (toSave.getLockedUntil().before(
						new Date(System.currentTimeMillis()))) {
					// Unlock it
					toSave.unlock();
				} else {
					// Otherwise notify the client that it lock is unavailable
					throw new LockUnavailable(
							"\'"
									+ toSave.getTitle()
									+ "\' is locked for "
									+ (toSave.getLockedUntil().getTime() - System
											.currentTimeMillis()) / 1000L
									+ " more seconds.");
				}
			}

			// Lock the document for 120 seconds for the user whose IP was
			// provided
			toSave.lock(new Date(System.currentTimeMillis() + 120000L), ip);

			// Write this to the Datastore
			pm.makePersistent(toSave);

			// ...Ending transaction
			t.commit();

			// Return the locked document
			return toSave.getLockedDoc();
		} finally {
			// Do some cleanup
			if (t.isActive())
				t.rollback();

			pm.close();
		}
	}

	/**
	 * Retrieve the doc with the given key.
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

			// Return the unlocked document
			return doc.getUnlockedDoc();
		} finally {
			// Do cleanup
			pm.close();
		}
	}

	/**
	 * Wrapper for saveDocument() - gets the user's IP.
	 */
	@Override
	public UnlockedDocument saveDocument(LockedDocument doc) throws LockExpired {
		return saveDocument(doc, getThreadLocalRequest().getRemoteAddr());
	}

	/**
	 * Save the given doc.
	 * 
	 * @param doc
	 * @param ip
	 * @return
	 * @throws LockExpired
	 */
	public UnlockedDocument saveDocument(LockedDocument doc, String ip)
			throws LockExpired {
		// Get the PM
		PersistenceManager pm = PMF.get().getPersistenceManager();

		Document toSave;

		// Get the document's key
		String stringKey = doc.getKey();

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

				// Get the lock information - saveDocument can only be called if
				// there is a lock or if it's a new document
				String lockedBy = toSave.getLockedBy();
				Date lockedUntil = toSave.getLockedUntil();

				// Check that the person trying to save has the lock and that
				// the lock hasn't expired
				if (lockedBy.equals(ip)
						&& lockedUntil.after(new Date(System
								.currentTimeMillis()))) {
					// If both are fulfilled, update and unlock the doc
					toSave.update(doc);
					toSave.unlock();
				} else {
					// Otherwise, throw an exception
					throw new LockExpired();
				}
			}

			// Now write the results to Datastore
			pm.makePersistent(toSave);

			// ...Ending transaction
			t.commit();

			// Return the unlocked document
			return toSave.getUnlockedDoc();
		} finally {
			// Do some cleanup
			if (t.isActive())
				t.rollback();

			pm.close();
		}
	}

	/**
	 * Wrapper for releaseLock() - gets the user's IP.
	 */
	@Override
	public void releaseLock(LockedDocument doc) throws LockExpired {
		releaseLock(doc, getThreadLocalRequest().getRemoteAddr());
	}

	/**
	 * Releases the lock for doc.
	 * 
	 * @param doc
	 * @param ip
	 * @throws LockExpired
	 */
	public void releaseLock(LockedDocument doc, String ip) throws LockExpired {
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

			// Make sure that the person unlocking is the person who locked the
			// doc.
			if (lockedBy.equals(ip)) {
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
		} finally {
			// Do some cleanup
			if (t.isActive())
				t.rollback();

			pm.close();
		}
	}
}
