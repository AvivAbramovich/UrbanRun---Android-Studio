package com.team2.urbanrun.Classes;

public class Score {
	
	private String Username;
	private String fullName;
	private int Userscore;
	
	public Score(String username,String full, int userscore){
		fullName= full;
		setUsername(username);
		setUserscore(userscore);		
	}
	
	public Score(){
		Username = null;
		Userscore = 0;
	}

	public String getUsername() {
		return Username;
	}

	public void setUsername(String username) {
		Username = username;
	}

	public int getUserscore() {
		return Userscore;
	}

	public void setUserscore(int userscore) {
		Userscore = userscore;
	}
}
