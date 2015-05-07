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

import org.mortbay.util.ajax.JSON;

import java.sql.*;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.labs.repackaged.com.google.common.primitives.UnsignedInteger;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class InitZoneServlet extends HttpServlet {
	/***
	 *insert radios and center 
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
	
		String url = null;
		try {
			Class.forName("com.mysql.jdbc.GoogleDriver");
			url = "jdbc:google:mysql://team2urban:team2db/team2db?user=root";
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
		
		int radius = Integer.parseInt(req.getParameter("Radius"));
		Double centerLat = Double.parseDouble(req.getParameter("CenterLat"));
		Double centerLng = Double.parseDouble(req.getParameter("CenterLng"));
		int time = Integer.parseInt(req.getParameter("Time"));
		String creator = req.getParameter("Creator");
		JSONArray players = new JSONArray(req.getParameter("Players"));
		
		PreparedStatement stmt;
		ResultSet rs;
	    
	      Connection conn = DriverManager.getConnection(url);
	      try {
	    	  int gameID;
	    	  while(true){
		    	  gameID = (int)(Math.random()*10000);
		    	  stmt = conn.prepareStatement("select GameID from Games where GameID=?");
		    	  stmt.setInt(0, gameID);
		    	  rs = stmt.executeQuery();
		    	  if(!rs.next())	//ID is free
		    		  break;
		    	  
		    	  //if get here. the ID is taken, generate new one
	    	  }
	    	  
	    	  String createSingleGameTable = "CREATE TABLE SingleGameTable? ("
						
	    			  	+ "ID varchar(30) PRIMARY KEY references Users(ID),"
						+ "Lat DOUBLE,"
						+ "Lng DOUBLE,"
						+ "score INT,"
						+ "inventory INT)";
						
	    	  stmt = conn.prepareStatement(createSingleGameTable);
	    	  stmt.setInt(1, gameID);

	          stmt.executeUpdate();
	          
	          stmt = conn.prepareStatement("insert into SingleGameTable? values (?,0,0,0,0)");
		      stmt.setInt(1, gameID);
	          stmt.setString(2,creator);
		      stmt.executeUpdate(); 
	          
		      for(int i=0; i< players.length(); i++)
		      {
		    	  String user = (String)players.get(i);
		    	  
			      stmt = conn.prepareStatement("insert into SingleGameTable? values (?,0,0,0,0)");
			      stmt.setInt(1, gameID);
		          stmt.setString(2, user);
			      stmt.executeUpdate(); 

			      stmt = conn.prepareStatement("insert into invitations values (?,?,?,?)");
		          stmt.setString(1,creator);
		          stmt.setString(2,user);
		          stmt.setInt(3,gameID);
		          stmt.setString(4, "wait");
		      }
		      
		      
	          
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
	         stmt.setInt(3,time);
	         stmt.setInt(4,radius);
	         stmt.setDouble(5,centerLat);
	         stmt.setDouble(6,centerLng);
	         
	         stmt.executeUpdate();

	         resp.getWriter().println(gameID);   
	         log(Integer.toString(gameID));
	         
	      } finally {
	        conn.close();
	      }
	    } catch (Exception e) {
	      e.printStackTrace();
	      log("fuck");
	      log(e.toString());
	    }
	}
}
