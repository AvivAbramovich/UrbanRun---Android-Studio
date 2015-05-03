package com.team2.urbanrun;

import java.io.*;

import javax.servlet.http.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.utils.SystemProperty;
import com.google.gson.Gson;
import com.team2.urbanrun.AppConstants;
import com.team2.urbanrun.Classes.MyElements;
import com.team2.urbanrun.Classes.Opponent;
import com.team2.urbanrun.Classes.Player;

@SuppressWarnings("serial")
public class SendLocation extends HttpServlet {
	
	private double plat, plng;
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String url = null;
		double centerLat=0, centerLng=0;
		int radius=0, sound=0;
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
				int myScore=0;
				String username = req.getParameter("username");
				String gameid = req.getParameter("GameID");
				double myLat = Double.parseDouble(req.getParameter("mylat"));
				double myLng = Double.parseDouble(req.getParameter("mylng"));
				Calendar calendar = Calendar.getInstance();
				Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
				
				
				PreparedStatement pstmt = conn.prepareStatement("Select * from SingleGameTable"+gameid);
				ResultSet rs=pstmt.executeQuery();
				List<Opponent> playersList = new ArrayList<Opponent>();
				while(rs.next())
				{
						playersList.add(new Opponent(rs.getString("username"),rs.getDouble("Lat"),rs.getDouble("Lng"),rs.getInt("score")));
						if(rs.getString("username").equals(username))
							myScore = rs.getInt("score");
				}
				
				
				pstmt = conn.prepareStatement(AppConstants.GET_GAME_INFO);
				pstmt.setString(1, gameid);
				rs=pstmt.executeQuery();
				if(rs.next()){
					centerLat=rs.getDouble("centerLat");
					centerLng=rs.getDouble("centerLng");
					radius=rs.getInt("radius");
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
				int countElements = 0;
				List<MyElements> elementsList = new ArrayList<MyElements>();
				while(rs.next())
				{
					if((currentTimestamp.getTime())>rs.getLong("terminates")){	//remove the item
						pstmt = conn.prepareStatement("delete from ElementsTable"+gameid+" where ID=?");
						pstmt.setInt(1, rs.getInt("ID"));
						pstmt.executeUpdate();
					}
					else
					{
						//first, check if i too close to it and got it
						double lat=rs.getDouble("Lat"),lng=rs.getDouble("Lng");
						int type = rs.getInt("type");
						if(diff_in_meters_between_two_points(lat, lng, myLat, myLng)<AppConstants.MIN_RADIUS){
							if(type==0)
								myScore+=50;
							if(type==1)
								myScore+=200;
							if(type==2)
								myScore+=1000;
							pstmt = conn.prepareStatement("delete from ElementsTable"+gameid+" where ID=?");
							pstmt.setInt(1, rs.getInt("ID"));
							pstmt.executeUpdate();
						}
						else	//if not, add to the list off elements
						{
							elementsList.add(new MyElements(lat,lng, type));
							countElements++;
						}
					}
				}
				if(countElements<AppConstants.NUM_ELEMENTS)	
					//add 1 item if there's less than should be
					//if need to add more than 1, adding it in the next servlet so it be faster response
				{
					pstmt = conn.prepareStatement("insert into ElementsTable"+gameid+" values(?,?,?,?,?)");
					pstmt.setInt(1,(int)(Math.random()*100000));
					generateNewElement(radius, centerLat, centerLng);
					pstmt.setDouble(2, plat);
					pstmt.setDouble(3, plng);
					int rnd = (int)(Math.random()*20), type;
					if(rnd<=10)
						type=0;	//bronzeCoin (0-10)
					else{
						if(rnd<=17) //silver coin (11-17)
							type=1;
						else
							type=2; //goldCoin (18-19)
					}
					pstmt.setInt(4, type);
					pstmt.setLong(5, currentTimestamp.getTime()+AppConstants.ELEMENT_LIFETIME);
					pstmt.executeUpdate();
					elementsList.add(new MyElements(plat, plng, type));
				}
				
				//updating the table (my location and score)
				pstmt = conn.prepareStatement("update SingleGameTable"+gameid+" set score=?, Lat=?, Lng=? where username=?");
				pstmt.setInt(1, myScore);
				pstmt.setDouble(2, myLat);
				pstmt.setDouble(3, myLng);
				pstmt.setString(4, username);
				pstmt.executeUpdate();
				
				Gson gson = new Gson();
				String ElementsListJson = gson.toJson(elementsList);
				String PlayersListJson = gson.toJson(playersList);
				
				//return these values
				out.print("{\"Players\":"+PlayersListJson+ ", \"Elements\": "+ElementsListJson+", \"timeLeft\": "+timeLeft+", \"sound\":"+sound+"}"); 
				
				
			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			log("fuck");
			e.printStackTrace();
			log(e.toString());
		}
	}
	
	void generateNewElement(int radius, double centerLat, double centerLng)
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
