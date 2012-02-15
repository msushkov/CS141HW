package edu.caltech.cs141b.hw2.gwt.collab.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

	private static final String QUEUE_MAP = "queue_map";

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
	public UnlockedDocument saveDocument(String clientID, LockedDocument doc) throws LockExpired {
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
				String identity = clientID;//getThreadLocalRequest().getRemoteAddr();
				// Check that the person trying to save has the lock and that
				// the lock hasn't expired
				if (lockedBy.equals(identity)
						&& lockedUntil.after(new Date(System
								.currentTimeMillis()))) {
					// If both are fulfilled, update and unlock the doc
					toSave.update(doc);
					toSave.unlock();

					// the next client in line waiting on this 
					// document can now get the lock
					notifyNextObject(stringKey);
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

	private void notifyNextObject(String docKey) {
		// which client is next in line for this document?
		String clientID = pollNextClient(docKey);

		if (clientID != null) {
			// send a message to the next-in-line client that
			// he is now acquiring the lock
			getChannelService().sendMessage(
					new ChannelMessage(clientID, docKey));
			
			// TODO
			// Lock the document for this client
			// Start timer
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
	public void releaseLock(String clientID, LockedDocument doc)
			throws LockExpired {
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
			String identity = clientID;//getThreadLocalRequest().getRemoteAddr();

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

	@SuppressWarnings("unchecked")
	private void addToDocQueue(String clientID, String documentKey) {
		Map<String, List<String>> queueMap = (Map<String, List<String>>) getThreadLocalRequest()
				.getAttribute(QUEUE_MAP);

		if (queueMap == null)
			queueMap = new HashMap<String, List<String>>();

		// SUSHKOV IS CONFUSED
		
		// why are we checking for docKey here, arent we storing 
		// client id's as the keys?
		
		// or maybe you meant to store the docKeys as the keys, and the lists of
		// clientID's as values? 
				
		if (!queueMap.containsKey(documentKey)) {
			queueMap.put(clientID, Collections
					.synchronizedList(new LinkedList<String>()));
		}

		List<String> queue = queueMap.get(clientID);

		queue.add(clientID);

		queueMap.put(clientID, queue);
		getThreadLocalRequest().setAttribute(QUEUE_MAP, queueMap);
	}

	/**
	 * 
	 * @param documentKey
	 * @return a client ID of the next client waiting on this doc
	 */
	@SuppressWarnings("unchecked")
	private String pollNextClient(String documentKey) {
		Map<String, List<String>> queueMap = (Map<String, List<String>>) getThreadLocalRequest()
				.getAttribute(QUEUE_MAP);

		if (queueMap != null) {
			List<String> queue = queueMap.get(documentKey);
			if (queue != null && !queue.isEmpty()) {
				return queue.remove(0);
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private boolean removeClient(String clientID, String documentKey) {
		Map<String, List<String>> queueMap = (Map<String, List<String>>) getThreadLocalRequest()
				.getAttribute(QUEUE_MAP);

		if (queueMap != null) {
			List<String> queue = queueMap.get(documentKey);
			if (queue != null) {
				return queue.remove(clientID);
			}
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.caltech.cs141b.hw2.gwt.collab.client.CollaboratorService#lockDocument
	 * (java.lang.String)
	 */
	@Override
	public void lockDocument(String clientID, String documentKey) {
		addToDocQueue(clientID, documentKey);
	}

	@Override
	public String login(String clientID) {
		return getChannelService().createChannel(clientID);
	}

	private ChannelService getChannelService() {
		return ChannelServiceFactory.getChannelService();
	}

	@Override
	public LockedDocument getLockedDocument(String clientID, String documentKey)
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

			String identity = clientID;//getThreadLocalRequest().getRemoteAddr();

			// If the doc is locked and you own it...
			if (!toSave.isLocked()
					|| !toSave.getLockedBy().equals(identity)
					|| toSave.getLockedUntil().before(
							new Date(System.currentTimeMillis()))) {
				throw new LockUnavailable("This lock is not yours");
			}

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

	@Override
	public void leaveLockQueue(String clientID, String documentKey) {
		removeClient(clientID, documentKey);
	}
}
