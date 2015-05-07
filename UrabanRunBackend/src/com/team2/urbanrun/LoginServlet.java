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

		String ID = req.getParameter("ID");

		try {
			Connection conn = DriverManager.getConnection(url);
			try {
				PreparedStatement stmt = conn.prepareStatement("select ID from Users where ID="+ID);
				ResultSet res = stmt.executeQuery();
				if (!res.next()) {
					String firstName = req.getParameter("FirstName");
					String lastName = req.getParameter("LastName");
					String imageURL = req.getParameter("imageURL");
					stmt=conn.prepareStatement("insert into Users values (?,?,?,?,0,0)");
					stmt.setString(1, ID);
					stmt.setString(2, firstName);
					stmt.setString(3, lastName);
					stmt.setString(4, imageURL);
					stmt.executeUpdate();
					log("user " + firstName+" "+ lastName + " added!");
					out.print("User " + firstName+" "+ lastName + " successfuly added to the database");
				}
				else{
					log("user already exsits!");
					out.print("Welcome Back ");
				}
				
				res.close();
				stmt.close();
				out.close();

			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log("fuck");
			out.print(e.toString());
			log(e.toString());
		}
	}
}
