package edu.caltech.cs141b.hw2.gwt.collab.shared;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


@PersistenceCapable
public class Document {
	@Persistent
	private String title;
	
	@Persistent
	private String content;
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key docKey;
	
	@Persistent
	private boolean locked;
	
	@Persistent
	private String lockedBy;
	
	@Persistent
	private Date lockedUntil;
	
	public Document(UnlockedDocument doc) {
		title = doc.getTitle();
		content = doc.getContents();
	}
	
	public Document(LockedDocument doc) {
		title = doc.getTitle();
		content = doc.getContents();
	}
	
	public String getTitle() {
		return title;
	}
	
	public String GetKey() {
		return KeyFactory.keyToString(docKey);
	}
	
	public void update(UnlockedDocument doc) {
		title = doc.getTitle();
		content = doc.getContents();	
	}
	
	public void update(LockedDocument doc) {
		title = doc.getTitle();
		content = doc.getContents();	
	}
	
	public void lock(Date lockedTil, String lockedBy) {
		this.locked = true;
		this.lockedUntil = lockedTil;
		this.lockedBy = lockedBy;
	}
	
	public void unlock() {
		this.lockedUntil = null;
		this.lockedBy = null;
		this.locked = false;
	}
	
	public UnlockedDocument getUnlocked() {
		String keyString = KeyFactory.keyToString(docKey);
		UnlockedDocument doc = new UnlockedDocument(keyString, title, content);
		return doc;
	}
	
	public Date getLockedUntil() {
		return this.lockedUntil;
	}
	
	public LockedDocument getLocked() {
		String keyString = KeyFactory.keyToString(docKey);
		LockedDocument doc = new LockedDocument(lockedBy, lockedUntil, keyString, title, content);
		return doc;
	}
	
	public boolean isLocked() {
		return this.locked;
	}
	
	
}