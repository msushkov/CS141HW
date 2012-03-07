package edu.caltech.cs141b.hw5.android.data;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Used to contain the entire document along with the locking primitives
 * necessary to modify it.
 */
public class LockedDocument implements Parcelable {

	private String lockedBy = null;
	private Date lockedUntil = null;
	private String key = null;
	private String title = null;
	private String contents = null;
	
	// Required by GWT serialization.
	public LockedDocument(Parcel source) 
	{
		// reconstruct from the Parcel
		readFromParcel(source);
	}
	
	public LockedDocument(String lockedBy, Date lockedUntil, String key,
			String title, String contents) {
		this.lockedBy = lockedBy;
		this.lockedUntil = lockedUntil;
		this.key = key;
		this.title = title;
		this.contents = contents;
	}

	public String getKey() {
		return key;
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String documentTitle) {
		this.title = documentTitle;
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String documentContents) {
		this.contents = documentContents;
	}

	public String getLockedBy() {
		return lockedBy;
	}

	public Date getLockedUntil() {
		return lockedUntil;
	}
	
	public UnlockedDocument unlock() {
		return new UnlockedDocument(key, title, contents);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(lockedBy);
		dest.writeSerializable(lockedUntil);
		dest.writeString(key);
		dest.writeString(title);
		dest.writeString(contents);
	}
	
	public void readFromParcel(Parcel source)
	{
		lockedBy = source.readString();
		lockedUntil = (Date) source.readSerializable();
		key = source.readString();
		title = source.readString();
		contents = source.readString();
	}
	
    @SuppressWarnings("rawtypes")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
	      public LockedDocument createFromParcel(Parcel source) {
	            return new LockedDocument(source);
	      }
	      
	      public LockedDocument[] newArray(int size) {
	            return new LockedDocument[size];
	      }
	};
}

