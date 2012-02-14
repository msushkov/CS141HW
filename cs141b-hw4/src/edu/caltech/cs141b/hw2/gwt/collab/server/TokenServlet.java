package edu.caltech.cs141b.hw2.gwt.collab.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;

public class TokenServlet extends HttpServlet {
	
	LinkedBlockingQueue<Integer> requestQueue = new LinkedBlockingQueue<Integer>();
	
//	@Override
//	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
//			throws ServletException, IOException {
//		System.out.print("I'm accessed!");
//		System.out.print(req);
//		System.out.print(resp);
//
//		super.doGet(req, resp);
//		
//	}

	// Requesting lock for document with key given as URL /token/<key>
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.out.println(req.getPathInfo());
	    ChannelService channelService = ChannelServiceFactory.getChannelService();
	    final Random random = new Random();
	    
		// Generate a random integer as channel id
	    String channelId = ((Integer) random.nextInt()).toString();

	    resp.setContentType("text/html");
	    resp.getWriter().write(channelId);
		super.doPost(req, resp);
	}
	
	
}
