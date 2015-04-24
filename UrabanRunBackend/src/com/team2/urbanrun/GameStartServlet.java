package com.team2.urbanrun;
import java.io.IOException;
import java.io.IOException;
import java.util.Calendar;
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

import com.google.appengine.labs.repackaged.com.google.common.primitives.UnsignedInteger;

@SuppressWarnings("serial")
public class GameStartServlet extends HttpServlet {
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
	    
	    String username = req.getParameter("username");
		String GameID = req.getParameter("GameID");
	    PreparedStatement stmt;
	    boolean isCreator;
	    ResultSet rs;
	    
	    
	    try {
	      Connection conn = DriverManager.getConnection(url);
	      try {
			stmt = conn.prepareStatement("select * from invitations where GameID=?");
			stmt.setString(1, GameID);
			rs = stmt.executeQuery();
	    	if(rs.next()) {
	    		
	    		if(rs.getString(1).equals(username))	//the user is the invitor
	    			isCreator = true;
	    		else									//the user is the invited
	    			isCreator = false;
	    	}
	    	else{	//NOT SUPPOSED TO HAPPEN!!!
	    		out.print("ERROR!!!");
	    		log("ERROR");
				conn.close();
				return;
	    	}
	    	if(isCreator){	//the game creator
	    		if(rs.getString(4).equals("wait"))	// still waiting for the invited
				{
					out.print("Wait");
					conn.close();
					return;
				}
	    		if(rs.getString(4).equals("declined"))
	    		{
	    			log("Declined!");
	    			out.print("Declined");
					conn.close();
					return;
	    		}
				else{	//the invited is ready
					Calendar calendar = Calendar.getInstance();
					Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());

					long startTime = currentTimestamp.getTime(); //starting time
					long gameLimit  = startTime+3*60*1000; //3m	TODO: take it from the limit in the Games table	
					stmt = conn.prepareStatement("update Games set StartTime=?,gameLimit=? where GameID=?");
					stmt.setLong(1, startTime);
					stmt.setLong(2, gameLimit);
					stmt.setString(3,GameID);
					stmt.executeUpdate();
					out.print("Start");

					conn.close();
					return;	
				}
	    	}
	    	else{	//the invited	
	    		stmt.close();
				stmt = conn.prepareStatement("update invitations set status=? where GameID=?");
				stmt.setString(1, "ready");
				stmt.setString(2,GameID);
				stmt.executeUpdate();
				out.print("Start");
				conn.close();
				return;
			}
	      } finally {
	        conn.close();
	      }
	    } catch (SQLException e) {
	      e.printStackTrace();
	      log(e.toString());
	    }
	}
}
