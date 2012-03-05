package edu.caltech.cs141b.hw2.gwt.collab.server;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;

import edu.caltech.cs141b.hw2.gwt.collab.shared.LockedDocument;
import edu.caltech.cs141b.hw2.gwt.collab.shared.UnlockedDocument;

// Datastore "JDO interface" class for documents.
@PersistenceCapable
public class Document {
	// The following are values we are writing to our Datastore
	@Persistent
	private String title;

	@Persistent
	private Text content;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key docKey; // Automatically assigned GAE key

	@Persistent
	private String lockedBy;

	@Persistent
	private Date lockedUntil;

	@Persistent
	private LinkedList<String> waitingClients;

	/**
	 * Constructor
	 * 
	 * @param doc
	 *            The UnlockedDocument we should create the java data object out
	 *            of
	 */
	public Document(UnlockedDocument doc) {
		title = doc.getTitle();
		content = new Text(doc.getContents());
		waitingClients = new LinkedList<String>();
	}

	/**
	 * Constructor
	 * 
	 * @param doc
	 *            The LockedDocument we should create the java data object out
	 *            of
	 */
	public Document(LockedDocument doc) {
		title = doc.getTitle();
		content = new Text(doc.getContents());

		lockedBy = doc.getLockedBy();
		lockedUntil = doc.getLockedUntil();
		waitingClients = new LinkedList<String>();
	}

	// Getters

	/**
	 * @return The title of the document
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return The person who locked the document
	 */
	public String getLockedBy() {
		return lockedBy;
	}

	/**
	 * @return The string form of the document's key
	 */
	public String getKey() {
		return KeyFactory.keyToString(docKey);
	}

	/**
	 * @return The date until when this is locked
	 */
	public Date getLockedUntil() {
		return lockedUntil;
	}

	public boolean removeClient(String clientID) {
		boolean inQueue = false;
		Iterator<String> it = waitingClients.iterator();
		while (it.hasNext()) {
			String client = it.next();
			if (client.equals(clientID)) {
				inQueue = true;
				it.remove();
			}
		}

		return inQueue;
	}

	public String pollNextClient() {
		if (waitingClients.isEmpty()) {
			return null;
		} else {
			return waitingClients.remove(0);
		}
	}
	
	public void addToWaitingList(String client) {
		waitingClients.add(client);
	}

	/*
	 * public LinkedList<String> getWaitingClients() { return waitingClients; }
	 */

	/**
	 * @return Whether the document is locked
	 */
	public boolean isLocked() {
		return lockedBy != null && lockedUntil != null;
	}

	/**
	 * Update the Document with new data
	 * 
	 * @param doc
	 *            The LockedDocument whose data we should update it with
	 */
	public void update(LockedDocument doc) {
		title = doc.getTitle();
		content = new Text(doc.getContents());
	}

	// Setters

	/**
	 * Locks the document
	 * 
	 * @param lockedTil
	 *            The document should stay locked until this time
	 * @param lockedBy
	 *            The document is locked by this person
	 */
	public void lock(Date lockedTil, String lockedBy) {
		this.lockedUntil = lockedTil;
		this.lockedBy = lockedBy;
	}

	/**
	 * Unlocks the document
	 */
	public void unlock() {
		this.lockedUntil = null;
		this.lockedBy = null;
	}

	
	public boolean hasQueue() {
		return !waitingClients.isEmpty();
	}
	
	// Document type converters

	/**
	 * @return Returns the unlocked form of this document
	 */
	public UnlockedDocument getUnlockedDoc() {
		String keyString = getKey();
		UnlockedDocument doc = new UnlockedDocument(keyString, title,
				content.getValue());
		return doc;
	}

	/**
	 * @return Returns the locked form of this document
	 */
	public LockedDocument getLockedDoc() {
		String keyString = getKey();
		LockedDocument doc = new LockedDocument(lockedBy, lockedUntil,
				keyString, title, content.getValue());
		return doc;
	}
	

}