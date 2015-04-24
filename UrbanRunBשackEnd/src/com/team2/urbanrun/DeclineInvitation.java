package com.team2.urbanrun;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.servlet.http.*;

import java.sql.*;

import com.google.appengine.api.utils.SystemProperty;

@SuppressWarnings("serial")
public class DeclineInvitation extends HttpServlet {

	/***
	 *insert radios and center 
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		String inviteduser = req.getParameter("username");
		int gameID = Integer.parseInt(req.getParameter("gameID"));
		
		String url = null;

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

	      }
	    } catch (Exception e) {
	      e.printStackTrace();
	      return;
	    }

	    try {
	      Connection conn = DriverManager.getConnection(url);
	      try {
	          String statement = "update invitations SET status=\"declined\" where invited=\""+inviteduser+"\" AND gameID="+gameID;
	          PreparedStatement stmt = conn.prepareStatement(statement);
	          stmt.executeUpdate();

			  stmt.close();			
				
	      } finally {
	        conn.close();
	      }
	    } catch (SQLException e) {
	      e.printStackTrace();
	      log(e.toString());
	    }
	}
  }
	