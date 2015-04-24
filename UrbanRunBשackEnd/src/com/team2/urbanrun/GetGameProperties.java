package com.team2.urbanrun;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.servlet.http.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.utils.SystemProperty;
import com.google.gson.Gson;
import com.team2.urbanrun.Classes.Player;
import com.team2.urbanrun.Classes.Score;

@SuppressWarnings("serial")
public class GetGameProperties extends HttpServlet {

	/***
	 *insert radios and center 
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		PrintWriter out = resp.getWriter();		
		
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
	    	  String GameID = req.getParameter("GameID");
	    	  log("GameID: "+GameID);
	    	  int radius;
	    	  double centerLat, centerLng;
	    	  List<Player> players = new ArrayList<Player>();
	    	  
	    	  // arena specs
	          String statement = "select radius, centerLat, centerLng from Games where GameID="+GameID;
	          PreparedStatement stmt = conn.prepareStatement(statement);
	          ResultSet res = stmt.executeQuery();
	          if(res.next())
	          {
	        	  radius = res.getInt("radius");
	        	  centerLat = res.getDouble("centerLat");
	        	  centerLng = res.getDouble("centerLng");
	          }
	          else
	          {
	        	  out.print("ERROR, cannot find arena specs");
	        	  return;
	          }
	          //players name and images
	          stmt = conn.prepareStatement("select Users.username, Users.FirstName, Users.image from Users, invitations "
	          		+ "where GameID="+GameID+" and (Users.username=invitations.invitor or Users.username=invitations.invited)");
	          res = stmt.executeQuery();
	          while(res.next())
	          {
	        	  players.add(new Player(res.getString(1), res.getString(2), res.getString(3)));
	          }
	          Gson gson = new Gson();
	          String playersJsonResult = gson.toJson(players);
	          out.print("{\"Players\":"+playersJsonResult+", \"Radius\":"+radius+", \"centerLat\":"+centerLat+", \"centerLng\":"+centerLng+"}"); 
	          stmt.close();			
				
	      } finally {
	    	log(out.toString());
	        conn.close();
	      }
	    } catch (SQLException e) {
	      e.printStackTrace();
	      log(e.toString());
	    }
	}
  }
