package com.team2.urbanrun;

import java.lang.reflect.Type;
import java.util.Collection;

public interface AppConstants {
	public final double MIN_RADIUS=5;
	
	public final String SELECT_PLAYERS_INFO_IN_GAME="Select * from SingleGameTable?";
	public final String SELECT_PRIZE_LOCATIONS_IN_GAME="Select * from ElementsTable?";
	public final String GET_GAME_INFO="Select * from Games where GameID=?";
	public final String ADD_NEW_PRIZE="insert into ElementsTable? values (?,?,?,?,?)";
}
