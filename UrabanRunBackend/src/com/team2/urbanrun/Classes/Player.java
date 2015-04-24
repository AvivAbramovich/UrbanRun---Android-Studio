package com.team2.urbanrun.Classes;

public class Player {
	private String Username;
	private String FullName;
	private String FirstName;
	private String ImageURL;
	int score;
	
	public Player(String username, String Full, String First, String img)
	{
		Username = username;
		ImageURL = img;
		FirstName = First;
		FullName = Full;
	}
	
	public Player(String username, String First, String img)
	{
		Username = username;
		FirstName = First;
		ImageURL = img;
	}
	
	public Player(String username, String Full, String img, int scr)
	{
		Username = username;
		FullName = Full;
		ImageURL = img;
		score = scr;
	}
}
