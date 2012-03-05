package edu.caltech.cs141b.hw2.gwt.collab.server;

import java.util.List;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

// Datastore "JDO interface" class for documents.
@PersistenceCapable
public class Client {
	// Only need to store key
	@Persistent
	List<String> myDocs;
	
	
	@PrimaryKey
	String key;
	
	public Client(String clientID) {
		key = clientID;
	}
	
	public void addDocument(String key) {
		// Add doc key if it doesn't exist
		if (!myDocs.contains(key)) {
			myDocs.add(key);
		}
	}
	
	public List<String> getLockedDocs() {
		return myDocs;
	}
	
	public void removeDocument(String key) {
		// Remove dockey if it exists
		myDocs.remove(key);
	}
}