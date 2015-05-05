package com.team2.urbanrun;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import javax.servlet.http.*;
import java.sql.*;
import com.google.appengine.api.utils.SystemProperty;


@SuppressWarnings("serial")
public class LoginServlet extends HttpServlet {

	/***
	 * insert radios and center
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		String url = null;
		PrintWriter out = resp.getWriter();
		try {
			if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
				// Load the class that provides the new "jdbc:google:mysql://"
				// prefix.
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

		String username = req.getParameter("username");
		String fullname = req.getParameter("fullname");
		String firstname = req.getParameter("firstname");
		String imageURL = req.getParameter("imageURL");
		log(username);

		try {
			Connection conn = DriverManager.getConnection(url);
			try {
				String statement = "select username from Users where username=?";
				PreparedStatement stmt = conn.prepareStatement(statement);
				stmt.setString(1, username);
				ResultSet res = stmt.executeQuery();
				if (!res.next()) {
					statement = "insert into Users values (?,?,?,?,0,0)";
					stmt=conn.prepareStatement(statement);
					stmt.setString(1, username);
					stmt.setString(2, fullname);
					stmt.setString(3, firstname);
					stmt.setString(4, imageURL);
					log("user " + username + " added!");
				}
				else{
					log("user " + username + " already exsits!");
				}
				
				HttpSession session=req.getSession();
				session.setAttribute("username",username);
				session.setAttribute("gameID",-1);
				session.setMaxInactiveInterval(60*120);
				
				out.print("user logged in");
				res.close();
				stmt.close();
				out.close();

			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log(e.toString());
		}
	}
}
