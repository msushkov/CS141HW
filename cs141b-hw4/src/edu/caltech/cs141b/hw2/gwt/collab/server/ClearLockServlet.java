package edu.caltech.cs141b.hw2.gwt.collab.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class ClearLockServlet extends HttpServlet {

	
	/**
	 * Cleans expired locks by calling cleanLocks() of the
	 * CollaboratorServiceImpl class. To be called by a cron job at
	 * regular intervals.
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
			CollaboratorServiceImpl.cleanLocks();
	}

}
