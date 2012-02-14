package edu.caltech.cs141b.hw2.gwt.collab.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

	
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		super.doGet(req, resp);
		System.out.println("lolz");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#getDocumentList
	 * ()
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#getDocument
	 * (java.lang.String)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#saveDocument
	 * (edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument)
	 */
	@Override
	public UnlockedDocument saveDocument(LockedDocument doc) throws LockExpired {
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

				// Get the IP Address
				String identity = getThreadLocalRequest().getRemoteAddr();
				// Check that the person trying to save has the lock and that
				// the lock hasn't expired
				if (lockedBy.equals(identity)
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
			if (t.isActive()) {
				t.rollback();
			}
			pm.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#releaseLock
	 * (edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument)
	 */
	@Override
	public void releaseLock(LockedDocument doc) throws LockExpired {
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

			// Get the IP Address
			String identity = getThreadLocalRequest().getRemoteAddr();

			// Make sure that the person unlocking is the person who locked the
			// doc.
			if (lockedBy.equals(identity)) {
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
			if (t.isActive()) {
				t.rollback();
			}
			pm.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#lockDocument
	 * (java.lang.String)
	 */
	@Override
	public LockedDocument lockDocument(String documentKey)
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
					throw new LockUnavailable("Document is locked for "
							+ (toSave.getLockedUntil().getTime() - System
									.currentTimeMillis()) / 1000L
							+ " more seconds.");
				}
			}

			// At this point, the doc should be unlocked
			// Get the IP Address of the user
			String identity = getThreadLocalRequest().getRemoteAddr();

			// Lock the document for 30 seconds
			toSave
					.lock(new Date(System.currentTimeMillis() + 30000L),
							identity);
			// Write this to the Datastore
			pm.makePersistent(toSave);

			// ...Ending transaction
			t.commit();

			// Return the locked document
			return toSave.getLockedDoc();
		} finally {
			// Do some cleanup
			if (t.isActive()) {
				t.rollback();
			}
			pm.close();
		}
	}
}
