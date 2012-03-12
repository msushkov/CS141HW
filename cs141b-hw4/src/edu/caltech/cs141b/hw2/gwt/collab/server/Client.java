package edu.caltech.cs141b.hw2.gwt.collab.server;

import java.util.List;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * This class stores client information on the server
 * 
 */
@PersistenceCapable
public class Client {

	// The list of locked docs
	@Persistent
	List<String> myDocs;

	// An ID for the locked doc
	@PrimaryKey
	String id;

	/**
	 * Constructs a new client
	 * 
	 * @param clientID
	 *            ID of the client
	 */
	public Client(String clientID) {
		id = clientID;
	}

	/**
	 * Adds a document to the locked document list
	 * 
	 * @param key
	 *            The document key
	 */
	public void addDoc(String key) {
		// Add doc key if it doesn't exist
		if (!myDocs.contains(key)) {
			myDocs.add(key);
		}
	}

	/**
	 * Returns the document list
	 * 
	 * @return The document list
	 */
	public List<String> getLockedDocs() {
		return myDocs;
	}

	/**
	 * Removes a document from the locked doc list
	 * 
	 * @param key
	 *            The key of the document to remove
	 */
	public void rmDoc(String key) {
		// Remove dockey if it exists
		myDocs.remove(key);
	}
}