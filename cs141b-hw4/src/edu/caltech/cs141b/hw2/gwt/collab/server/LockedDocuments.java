package edu.caltech.cs141b.hw2.gwt.collab.server;

import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * A collection of all the locked documents
 * 
 */
@PersistenceCapable
public class LockedDocuments {

	// A set containing all the locked documents
	@Persistent
	Set<String> lockedDocs;

	// The key of this queue
	@PrimaryKey
	String key = "lockedDocuments";

	/**
	 * Initializes the locked document set
	 */
	public LockedDocuments() {
		lockedDocs = new HashSet<String>();
	}

	/**
	 * Adds a document to the set of locked documents
	 * 
	 * @param key
	 *            The key of the locked document
	 */
	public void addDocument(String key) {
		// Add doc key if it doesn't exist
		if (!lockedDocs.contains(key)) {
			lockedDocs.add(key);
		}
	}

	/**
	 * Gets the set of all the locked documents
	 * 
	 * @return The set of all the locked documents
	 */
	public Set<String> getLockedDocs() {
		return lockedDocs;
	}

	/**
	 * Allows you to remove a document from the locked document set
	 * 
	 * @param key
	 *            The document to remove
	 */
	public void removeDocument(String key) {
		// Remove dockey if it exists
		lockedDocs.remove(key);
	}
}