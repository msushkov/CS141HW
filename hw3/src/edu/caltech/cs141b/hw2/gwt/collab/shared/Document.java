<<<<<<< HEAD
package edu.caltech.cs141b.hw2.gwt.collab.shared;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


// Datastore "JDO interface" class for documents.
@PersistenceCapable
public class Document {
	// The following are values we are writing to our Datastore
	@Persistent
	private String title;
	
	@Persistent
	private String content;
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)  
	private Key docKey; // Automatically assigned GAE key
	
	@Persistent
	private boolean locked;
	
	@Persistent
	private String lockedBy;
	
	@Persistent
	private Date lockedUntil;
	
	/**
	 * Constructor
	 * 
	 * @param doc The UnlockedDocument we should create the java data object out of
	 */
	public Document(UnlockedDocument doc) {
		title = doc.getTitle();
		content = doc.getContents();
		locked = false;
	}
	
	/**
	 * Constructor
	 * 
	 * @param doc The LockedDocument we should create the java data object out of
	 */
	public Document(LockedDocument doc) {
		title = doc.getTitle();
		content = doc.getContents();
		
		locked = true;
		lockedBy = doc.getLockedBy();
		lockedUntil = doc.getLockedUntil();
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
	
	
	/**
	 * @return Whether the document is locked
	 */
	public boolean isLocked() {
		return locked;
	}
	
	
	// Update Functions
	
	/**
	 * Updates the Document with new data
	 * 
	 * @param doc The UnlockedDocument whose data we should update it with
	 */
	public void update(UnlockedDocument doc) {
		title = doc.getTitle();
		content = doc.getContents();	
	}
	
	
	/**
	 * Update the Document with new data
	 * 
	 * @param doc The LockedDocument whose data we should update it with 
	 */
	public void update(LockedDocument doc) {
		title = doc.getTitle();
		content = doc.getContents();	
	}
	
	// Setters
	
	/**
	 * Locks the document
	 * 
	 * @param lockedTil The document should stay locked until this time
	 * @param lockedBy The document is locked by this person
	 */
	public void lock(Date lockedTil, String lockedBy) {
		this.locked = true;
		this.lockedUntil = lockedTil;
		this.lockedBy = lockedBy;
	}
	
	/**
	 * Unlocks the document
	 */
	public void unlock() {
		this.lockedUntil = null;
		this.lockedBy = null;
		this.locked = false;
	}
	
	
	// Document type converters 
	
	/**
	 * @return Returns the unlocked form of this document
	 */
	public UnlockedDocument getUnlockedDoc() {
		String keyString = getKey();
		UnlockedDocument doc = new UnlockedDocument(keyString, title, content);
		return doc;
	}

	/**
	 * @return Returns the locked form of this document
	 */
	public LockedDocument getLockedDoc() {
		String keyString = getKey();
		LockedDocument doc = new LockedDocument(lockedBy, lockedUntil, keyString, title, content);
		return doc;
	}
	

	
=======
package edu.caltech.cs141b.hw2.gwt.collab.shared;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


// Datastore "JDO interface" class for documents.
@PersistenceCapable
public class Document {
	// The following are values we are writing to our Datastore
	@Persistent
	private String title;
	
	@Persistent
	private String content;
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)  
	private Key docKey; // Automatically assigned GAE key
	
	@Persistent
	private boolean locked;
	
	@Persistent
	private String lockedBy;
	
	@Persistent
	private Date lockedUntil;
	
	/**
	 * Constructor
	 * 
	 * @param doc The UnlockedDocument we should create the java data object out of
	 */
	public Document(UnlockedDocument doc) {
		title = doc.getTitle();
		content = doc.getContents();
		locked = false;
	}
	
	/**
	 * Constructor
	 * 
	 * @param doc The LockedDocument we should create the java data object out of
	 */
	public Document(LockedDocument doc) {
		title = doc.getTitle();
		content = doc.getContents();
		
		locked = true;
		lockedBy = doc.getLockedBy();
		lockedUntil = doc.getLockedUntil();
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
	
	
	/**
	 * @return Whether the document is locked
	 */
	public boolean isLocked() {
		return locked;
	}
	
	
	// Update Functions
	
	/**
	 * Updates the Document with new data
	 * 
	 * @param doc The UnlockedDocument whose data we should update it with
	 */
	public void update(UnlockedDocument doc) {
		title = doc.getTitle();
		content = doc.getContents();	
	}
	
	
	/**
	 * Update the Document with new data
	 * 
	 * @param doc The LockedDocument whose data we should update it with 
	 */
	public void update(LockedDocument doc) {
		title = doc.getTitle();
		content = doc.getContents();	
	}
	
	// Setters
	
	/**
	 * Locks the document
	 * 
	 * @param lockedTil The document should stay locked until this time
	 * @param lockedBy The document is locked by this person
	 */
	public void lock(Date lockedTil, String lockedBy) {
		this.locked = true;
		this.lockedUntil = lockedTil;
		this.lockedBy = lockedBy;
	}
	
	/**
	 * Unlocks the document
	 */
	public void unlock() {
		this.lockedUntil = null;
		this.lockedBy = null;
		this.locked = false;
	}
	
	
	// Document type converters 
	
	/**
	 * @return Returns the unlocked form of this document
	 */
	public UnlockedDocument getUnlockedDoc() {
		String keyString = getKey();
		UnlockedDocument doc = new UnlockedDocument(keyString, title, content);
		return doc;
	}

	/**
	 * @return Returns the locked form of this document
	 */
	public LockedDocument getLockedDoc() {
		String keyString = getKey();
		LockedDocument doc = new LockedDocument(lockedBy, lockedUntil, keyString, title, content);
		return doc;
	}
	

	
>>>>>>> a3381fdbeb4d8716f27f45106becb999eecc9c01
}