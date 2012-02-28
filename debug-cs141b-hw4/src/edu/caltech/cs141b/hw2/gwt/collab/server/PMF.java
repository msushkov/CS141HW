package edu.caltech.cs141b.hw2.gwt.collab.server;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

/**
 * A factory class for generating one PersistenceManager for the entire program
 * 
 */
public final class PMF {
	private static final PersistenceManagerFactory pmfInstance = JDOHelper
			.getPersistenceManagerFactory("transactions-optional");
	
	private PMF() {}
	
	/**
	 * @return The PersistenceManagerFactory that we use to generate our PersistenceManagers
	 */
	public static PersistenceManagerFactory get() {
		return pmfInstance;
	}
}