package com.team2.urbanrun;

public interface AppConstants {
//	public final double MIN_RADIUS=5;
	
	public final String SELECT_PLAYERS_INFO_IN_GAME="Select * from SingleGameTable?";
	public final String SELECT_PRIZE_LOCATIONS_IN_GAME="Select * from ElementsTable?";
	public final String GET_GAME_INFO="Select * from Games where GameID=?";
	public final String ADD_NEW_PRIZE="insert into ElementsTable? values (?,?,?,?,?)";
	public final double NUM_ELEMENTS_PER_PLAYER = 0.5;
	public final int Max_Elements_Per_User = 5;
	public final double PERCENT_FOR_NEW_ELEMENT = 3; // %(0-100)

	public final int NUM_OF_ELEMENT_TYPE = 8;
	public enum Element {
	//	name   	   type score   %  timeLife(1000*sec) radius 
		diamond		(0,	1000,	1,		1000*120,		5),		//"public"		//1%
		stink		(1,	 -10,	1,		1000*20,		50),	//"public"		//
		smoke		(2,	   0,	1,		1000*10,		50),					//
		gold		(3,  500,	10,		1000*20,		5),						//9%
		takeThorns	(4,	   0,	10,		1000*20,		5),  	//kochim		//
		takeMine	(5,	   0,	10,		1000*30,		5), 	//mokash		//
		silver		(6,	 250,	45,		1000*30,		5),						//35%					
		bronze		(7,	 100,	100,	1000*40,		5),						//55%
		//--------this is not element, this is HIT!-----------------------//
		
		hitThorns	(8,	-100,	-1,		1000*180,		15),
		hitMine		(9,	-100,	-1,		1000*180,		 6);
		
		private int score,prob,type;
		private long lifeTime;
		private double radius;
		Element(int T,int score,int prob,long time,double R){
			this.score = score;
			this.prob = prob;
			this.type = T;
			this.lifeTime=time;
			this.radius=R;
		}
		public int getScore(){
			return score;
		}
		public int getProb(){
			return prob;
		}
		public int getType(){
			return type;
		}
		public long getLifeTime(){
			return lifeTime;
		}
		public double getRadius(){
			return radius;
		}
	};	
	
	 
	
	








}
