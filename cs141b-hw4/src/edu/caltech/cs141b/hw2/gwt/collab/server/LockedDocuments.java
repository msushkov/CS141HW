package edu.caltech.cs141b.hw2.gwt.collab.server;

import java.util.List;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

// Datastore "JDO interface" class for documents.
@PersistenceCapable
public class LockedDocuments {
	
	// Only need to store key
	@Persistent
	List<String> lockedDocs;
	
	
	@PrimaryKey
	String key = "lockedDocuments";
	
	public LockedDocuments() {
		// No need to do anything as of now
	}
	
	public void addDocument(String key) {
		// Add doc key if it doesn't exist
		if (!lockedDocs.contains(key)) {
			lockedDocs.add(key);
		}
	}
	
	public List<String> getLockedDocs() {
		return lockedDocs;
	}
	
	public void removeDocument(String key) {
		// Remove dockey if it exists
		lockedDocs.remove(key);
	}
}