package com.team2.urbanrun.Classes;

public class Opponent {
	private String username;
	private double Lat;
	private double Lng;
	private int score;
	
	public Opponent(String name, double lat, double lng, int scr)
	{
		username = name;
		Lat=lat;
		Lng=lng;
		score=scr;
	}
}
