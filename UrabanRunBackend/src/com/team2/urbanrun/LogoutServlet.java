package com.team2.urbanrun;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.*;

@SuppressWarnings("serial")
public class LogoutServlet extends HttpServlet {
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		PrintWriter out = resp.getWriter();
		HttpSession session = req.getSession(false);
		if (session == null) {
			log("session in null");
			return ;
		}
		session.invalidate();
		out.print("user logged out");
		log("closing session");
	}
}
