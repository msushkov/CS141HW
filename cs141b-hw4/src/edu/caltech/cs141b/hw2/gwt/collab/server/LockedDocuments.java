package edu.caltech.cs141b.hw2.gwt.collab.server;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

// Datastore "JDO interface" class for documents.
@PersistenceCapable
public class LockedDocuments {
	
	// Only need to store key
	@Persistent
	Set<String> lockedDocs;
	
	
	@PrimaryKey
	String key = "lockedDocuments";
	
	public LockedDocuments() {
		lockedDocs = new HashSet();
	}
	
	public void addDocument(String key) {
		// Add doc key if it doesn't exist
		if (!lockedDocs.contains(key)) {
			lockedDocs.add(key);
		}
	}
	
	public Set<String> getLockedDocs() {
		return lockedDocs;
	}
	
	public void removeDocument(String key) {
		// Remove dockey if it exists
		lockedDocs.remove(key);
	}
}