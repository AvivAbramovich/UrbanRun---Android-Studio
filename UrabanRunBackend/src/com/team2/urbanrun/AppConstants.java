package com.team2.urbanrun;

import java.lang.reflect.Type;
import java.util.Collection;

public interface AppConstants {
//	public final double MIN_RADIUS=5;
	
	public final String SELECT_PLAYERS_INFO_IN_GAME="Select * from SingleGameTable?";
	public final String SELECT_PRIZE_LOCATIONS_IN_GAME="Select * from ElementsTable?";
	public final String GET_GAME_INFO="Select * from Games where GameID=?";
	public final String ADD_NEW_PRIZE="insert into ElementsTable? values (?,?,?,?,?)";
	public final double NUM_ELEMENTS_PER_PLAYER = 0.5;
//	public final long ELEMENT_LIFETIME = 1000*30; //30 seconds
	public final double PERCENT_FOR_NEW_ELEMENT = 1; // %(0-100)

	public final int NUM_OF_ELEMENT_TYPE = 8;
	public enum Element {
	//	name   	   type score   %  timeLife(1000*sec) radius 
		diamond		(0,	1000,	2,		1000*120,		5),		//"public"		//2%
		stink		(1,	 -10,	7,		1000*20,		200),	//"public"		//5%
		smoke		(2,	   0,	16,		1000*10,		100),					//9%
		gold		(3,  500,	26,		1000*20,		5),						//10%
		takeThorns	(4,	   0,	36,		1000*20,		5),  	//kochim		//10%
		takeMine	(5,	   0,	50,		1000*30,		5), 	//mokash		//14%
		silver		(6,	 250,	70,		1000*30,		5),						//20%					
		bronze		(7,	 100,	100,	1000*40,		5),						//30%
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
