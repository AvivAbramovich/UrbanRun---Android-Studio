package com.team2.urbanrun;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.servlet.http.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.google.appengine.api.utils.SystemProperty;
import com.team2.urbanrun.Classes.Player;
import com.team2.urbanrun.Classes.Score;

@SuppressWarnings("serial")
public class EndScores extends HttpServlet {

	/***
	 *insert radios and center 
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		String gameID = req.getParameter("GameID");
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
	      }
	    } catch (Exception e) {
	      e.printStackTrace();
	      return;
	    }

	    try {
	      Connection conn = DriverManager.getConnection(url);
	      try {
	    	  
	  		  List<Player> userscores = new ArrayList<Player>();
	          String statement = "select U.username, U.fullname, U.image, S.score from SingleGameTable"+gameID+" S, Users U"
	          		+ " where U.username=S.username order by S.score DESC";
	          PreparedStatement stmt = conn.prepareStatement(statement);
	          ResultSet res = stmt.executeQuery();
	          while (res.next()) {
	        	  
	        	  userscores.add(new Player(res.getString(1),res.getString(2), res.getString(3), res.getInt(4)));
	         }
	       
		  		res.close();
				stmt.close();
				
				Gson gson = new Gson();
				String userJsonResult = gson.toJson(userscores);
				out.print(userJsonResult);
				out.close();
				
	      } finally {
	        conn.close();
	      }
	    } catch (SQLException e) {
	      e.printStackTrace();
	      log("fuck");
	      log(e.toString());
	    }
	    resp.setHeader("Refresh", "3; url=/guestbook.jsp");
	}
  }
	