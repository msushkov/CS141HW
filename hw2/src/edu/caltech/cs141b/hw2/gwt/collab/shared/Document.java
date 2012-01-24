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
	private Date lockedTil;
	
	public Document(UnlockedDocument doc) {
		title = doc.getTitle();
		content = doc.getContents();
	}
	
	public Document(LockedDocument doc) {
		title = doc.getTitle();
		content = doc.getContents();
	}
	
	
	public void Update(UnlockedDocument doc) {
		title = doc.getTitle();
		content = doc.getContents();	
	}
	
	public void Lock(Date lockedTil, String lockedBy) {
		this.locked = true;
		this.lockedTil = lockedTil;
		this.lockedBy = lockedBy;
	}
	
	public void Unlock() {
		lockedTil = null;
		lockedBy = null;
	}
	
	public UnlockedDocument GetUnlocked() {
		String keyString = KeyFactory.keyToString(docKey);
		UnlockedDocument doc = new UnlockedDocument(keyString, title, content);
		return doc;
	}
	
	public LockedDocument GetLocked() {
		String keyString = KeyFactory.keyToString(docKey);
		LockedDocument doc = new LockedDocument(lockedBy, lockedTil, keyString, title, content);
		return doc;
	}
	
	
}