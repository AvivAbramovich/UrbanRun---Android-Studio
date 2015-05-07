package com.team2.urbanrun;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.servlet.http.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.appengine.api.utils.SystemProperty;
import com.team2.urbanrun.Classes.Player;
import com.team2.urbanrun.Classes.Score;

@SuppressWarnings("serial")
public class getUsers extends HttpServlet {

	/***
	 *insert radios and center 
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		String url = null;
		PrintWriter out = resp.getWriter();
	    try {
	        Class.forName("com.mysql.jdbc.GoogleDriver");
	        url = "jdbc:google:mysql://team2urban:team2db/team2db?user=root";
	    } catch (Exception e) {
	      e.printStackTrace();
	      return;
	    }
	    
	    String name = req.getParameter("name");	//not used right now, TODO: return only my friends
	    log(name);
	    
	    try {
	      Connection conn = DriverManager.getConnection(url);
	      try {
	    	  
	  		  List<Player> friends = new ArrayList<Player>();
	          String statement = "select * from Users";
	          PreparedStatement stmt = conn.prepareStatement(statement);
	          ResultSet res = stmt.executeQuery();
	          while (res.next()) {
	        	  
	        	  friends.add(new Player(res.getString("username"),res.getString("fullname") ,res.getString("firstname"), res.getString("image")));
	         }
	       
		  	res.close();
			stmt.close();
				
			Gson gson = new Gson();
			String userJsonResult = gson.toJson(friends);
			log("res: "+userJsonResult);
			out.print(userJsonResult);
			out.close();
				
	      } finally {
	        conn.close();
	      }
	    } catch (SQLException e) {
	      log("fuck");
	      e.printStackTrace();
	      log(e.toString());
	    }
	    resp.setHeader("Refresh", "3; url=/guestbook.jsp");
	}
  }
	