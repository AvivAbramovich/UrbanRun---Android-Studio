package com.team2.urbanrun;
import java.io.IOException;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.*;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.FilterOperator;

import java.util.Date;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.utils.SystemProperty;

import java.io.*;

import javax.servlet.http.*;

import java.sql.*;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.labs.repackaged.com.google.common.primitives.UnsignedInteger;

@SuppressWarnings("serial")
public class InitZoneServlet extends HttpServlet {
	/***
	 *insert radios and center 
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
	
		String url = null;
		PrintWriter out = resp.getWriter();
	    try {
	      if (SystemProperty.environment.value() ==
	          SystemProperty.Environment.Value.Production) {
	        // Load the class that provides the new "jdbc:google:mysql://" prefix.
	        Class.forName("com.mysql.jdbc.GoogleDriver");
	        url = "jdbc:google:mysql://team2urban:team2db/team2db?user=root";
	      } else {
	        // Local MySQL instance to use during development.
	        Class.forName("com.mysql.jdbc.Driver");
	        url = "jdbc:mysql://127.0.0.1:3306/guestbook?user=root";

	        // Alternatively, connect to a Google Cloud SQL instance using:
	        // jdbc:mysql://ip-address-of-google-cloud-sql-instance:3306/guestbook?user=root
	      }
	    } catch (Exception e) {
	      e.printStackTrace();
	      return;
	    }
	    
	    int radius = Integer.parseInt(req.getParameter("radius")); 			//radius
		Double centerLat = Double.parseDouble(req.getParameter("centerLat"));	//centerLat
		Double centerLng = Double.parseDouble(req.getParameter("centerLng"));	//centerLng
		String player1 = req.getParameter("username");						//username
		String player2 = req.getParameter("oppName");						//oppName
		String type = req.getParameter("type");							//type
		int limit = Integer.parseInt(req.getParameter("limit"));			//limit (sec)
	   
		//TODO: more than 1 opp..
		
	    try {
	      Connection conn = DriverManager.getConnection(url);
	      try {
	    	  
	    	  int gameID = (int)(Math.random()*10000);
	    	  //TODO: check that the generated value not taken already...
	    	  String createSingleGameTable = "CREATE TABLE SingleGameTable? ("
						
	    			  	+ "username varchar(30) PRIMARY KEY references Users(username),"
						+ "Lat DOUBLE,"
						+ "Lng DOUBLE,"
						+ "score INT,"
						+ "inventory INT)";
						
	    	  PreparedStatement stmt = conn.prepareStatement(createSingleGameTable);
	    	  stmt.setInt(1, gameID);

	          stmt.executeUpdate();
	          
	          stmt = conn.prepareStatement("insert into SingleGameTable? values (?,0,0,0,0)");
		      stmt.setInt(1, gameID);
	          stmt.setString(2,player1);
		      stmt.executeUpdate(); 
	          
		      stmt = conn.prepareStatement("insert into SingleGameTable? values (?,0,0,0,0)");
		      stmt.setInt(1, gameID);
	          stmt.setString(2,player2);
		      stmt.executeUpdate(); 
		      
		      String insertToinvitations= "insert into invitations values (?,?,?,?)";
	          
		      stmt = conn.prepareStatement(insertToinvitations);
	          stmt.setString(1,player1);
	          stmt.setString(2,player2);
	          stmt.setInt(3,gameID);
	          stmt.setString(4, "wait");
	          
	          stmt.executeUpdate();
	           
	          String createElementsTable = "CREATE TABLE ElementsTable? ("
						
	    			  	+ "ID INT PRIMARY KEY,"
						+ "Lat DOUBLE,"
						+ "Lng DOUBLE,"
						+ "type INT,"
						+ "terminates LONG)";
						
	    	 stmt = conn.prepareStatement(createElementsTable );
	    	 stmt.setInt(1, gameID);
	    	 stmt.executeUpdate();
	          
	    	 String insertNewGame= "insert into Games values (?,?,?,?,?,?)";
	         
	    	 stmt = conn.prepareStatement(insertNewGame);
	         stmt.setInt(1,gameID);
	         stmt.setInt(2,0);
	         stmt.setInt(3,limit);
	         stmt.setInt(4,radius);
	         stmt.setDouble(5,centerLat);
	         stmt.setDouble(6,centerLng);
	         
	         stmt.executeUpdate();
	         
	         resp.setContentType("text/plain");
	         resp.getWriter().println(gameID);   
	         log(Integer.toString(gameID));
	         
	      } finally {
	        conn.close();
	      }
	    } catch (SQLException e) {
	      e.printStackTrace();
	      log(e.toString());
	    }
	    //resp.setHeader("Refresh", "3; url=/guestbook.jsp");
	}
}
