package com.team2.urbanrun;

import java.io.*;

import javax.servlet.http.*;

import java.sql.*;
import java.util.Calendar;
import java.util.Date;

import com.google.appengine.api.utils.SystemProperty;
import com.team2.urbanrun.AppConstants;

@SuppressWarnings("serial")
public class SendLocation extends HttpServlet {
	
	private double plat, plng;
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String url = null;
		double oppLat=0, oppLng=0, prizeLat=0, prizeLng=0, centerLat=0, centerLng=0;
		int oppScore=0,myScore=0,radius=0, sound=0;
		long timeLeft=0;
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

				// Alternatively, connect to a Google Cloud SQL instance using:
				// jdbc:mysql://ip-address-of-google-cloud-sql-instance:3306/guestbook?user=root
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		try {
			Connection conn = DriverManager.getConnection(url);
			try {
				String username = req.getParameter("username");
				String gameid = req.getParameter("GameID");
				double myLat = Double.parseDouble(req.getParameter("mylat"));
				double myLng = Double.parseDouble(req.getParameter("mylng"));
				
				log(username+" game: "+gameid+"lat: "+myLat+", my Lng: "+myLng);
				
				PreparedStatement pstmt = conn.prepareStatement("Select * from SingleGameTable"+gameid);
				ResultSet rs=pstmt.executeQuery();
				while(rs.next()){
					if(!rs.getString("username").equals(username)){
						oppLat=rs.getDouble("Lat");
						oppLng=rs.getDouble("Lng");
						oppScore=rs.getInt("score");
					}
					else{
						myScore=rs.getInt("score");
					}
				}
				
				pstmt = conn.prepareStatement(AppConstants.GET_GAME_INFO);
				pstmt.setString(1, gameid);
				rs=pstmt.executeQuery();
				if(rs.next()){
					centerLat=rs.getDouble("centerLat");
					centerLng=rs.getDouble("centerLng");
					radius=rs.getInt("radius");
					Calendar calendar = Calendar.getInstance();
					Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
					timeLeft=(rs.getLong("gameLimit") - currentTimestamp.getTime())/1000;
					
					if(timeLeft<=0)
						sound=2; //time up sound
				}
				else{
					out.print("noGameFound");
					return;
				}
				
				pstmt = conn.prepareStatement("select * from ElementsTable"+gameid);
				rs=pstmt.executeQuery();
				//TODO: Convert to While loop for all prizes.
				if(rs.next()){
					prizeLat=rs.getDouble("Lat");
					prizeLng=rs.getDouble("Lng");
					double dist = diff_in_meters_between_two_points(prizeLat, prizeLng, myLat, myLng);
					log("distance from the apple is "+dist);
					if (dist<AppConstants.MIN_RADIUS){
						//update score 100
						int  prizeID=rs.getInt("ID");
						log("got_an_apple"+prizeID);
						myScore+=100; //score updating in the wnd of the servlet with the user's location
						
						//delete prize
						log("check1");
						pstmt = conn.prepareStatement("delete from ElementsTable"+gameid+" where ID=?");
						pstmt.setInt(1, prizeID); //get the original element ID
						pstmt.executeUpdate();
						log("check2");
						
						generateNewCoin(radius, centerLat, centerLng);
						
						//insert new prize
						pstmt = conn.prepareStatement("insert into ElementsTable"+gameid+" values(?,?,?,?,?)");
						pstmt.setInt(1,(int)(Math.random()*1000000));
						pstmt.setDouble(2, plat);
						pstmt.setDouble(3, plng);
						pstmt.setInt(4, 0); //temporary
						pstmt.setInt(5, 30);
						pstmt.executeUpdate();
						log("check3");
						sound=1; //get prize sound
					}
				}
				else{
					
					generateNewCoin(radius, centerLat, centerLng);
					prizeLat=plat;
					prizeLng=plng;
					log("check4");
					pstmt.close();
					pstmt = conn.prepareStatement("insert into ElementsTable"+gameid+" values (?,?,?,?,?)");
					pstmt.setInt(1, ((int)(Math.random()*100000)));
					pstmt.setDouble(2, plat);
					pstmt.setDouble(3, plng);
					pstmt.setInt(4, 0);	//TODO: generate constants for each type of element
					pstmt.setInt(5, 30);
					
					pstmt.executeUpdate();
					log("check5");
				}
				log("check6");
				//updating the table (my location and score)
				pstmt = conn.prepareStatement("update SingleGameTable"+gameid+" set score=?, Lat=?, Lng=? where username=?");
				pstmt.setInt(1, myScore);
				pstmt.setDouble(2, myLat);
				pstmt.setDouble(3, myLng);
				pstmt.setString(4, username);
				pstmt.executeUpdate();
				
				log("check7");
				//return these values
				out.print("{\"oppLat\":"+oppLat+", \"oppLng\":"+oppLng+", \"myScore\":"+myScore+", \"oppScore\": "+oppScore+""
						+ ", \"prizeLat\": "+prizeLat+", \"prizeLng\": "+prizeLng+", \"timeLeft\": "+timeLeft+", \"sound\":"+sound+"}"); 
				
				
			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			log("fuck");
			e.printStackTrace();
			log(e.toString());
		}
	}
	
	void generateNewCoin(int radius, double centerLat, double centerLng)
    {

        //PROBLEM: not mach between LatLng to meters, don't know how to do this conversation

        double r = Math.random()*radius; 	//generate a number between 0 to the radius
        double angle = Math.random()*360;			//generate a number between 0 to 360

        //little bit trigonometry...
        double a,b;
        a = Math.sin(angle)*r;
        b = Math.cos(angle)*r;

        //a and b are in meters and we need to change it into lat lng diffrences, so
        // the difference in meters between 2 LatLng points when Lng1==Lng2 and |Lat1-Lat2|== 1 is 111.23KM, so 1m = 1/111230 Lat
        // the difference in meters between 2 LatLng points when Lat1==Lat2 and |Lng1-Lng2|== 1 is 87.65KM, so 1m = 1/87650 Lng
        a/=111230;
        b/=87650;

        plat=centerLat+b;
        plng=centerLng+a;
    }
	
	double diff_in_meters_between_two_points(double pointlat1, double pointlng1, double pointlat2, double pointlng2)
    {
        double R = 6371000; //earth's radius meters
        double lat1 = Math.toRadians(pointlat1);
        double lat2 = Math.toRadians(pointlat2);
        double diff_lat = Math.toRadians((pointlat2-pointlat1));
        double diff_lng = Math.toRadians((pointlng2-pointlng1));

        double a = Math.pow(Math.sin(diff_lat/2),2)+Math.cos(lat1)*Math.cos(lat2)*Math.pow(Math.sin(diff_lng/2),2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return R * c;
    }
	
}
