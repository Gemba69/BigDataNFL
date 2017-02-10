package de.fhm.bigdata.projekt.rest;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import de.fhm.bigdata.projekt.hbase.HBaseConnectionManager;
import de.fhm.bigdata.projekt.hbase.model.*;

@Path("nfl")
public class JerseyService {
	private final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"dd.MM.yyyy");

	@GET
	@Path("/getTeamstatistics")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public ArrayList<NFLTeam> getAllTeamStatistics() {

		ArrayList<NFLTeam> resultList = createTestTeams();
		Collections.sort(resultList, NFLTeam.getNFLTeamByRank());
		
		return resultList;
	}
	
	@GET
	@Path("/getTopHashtags")
	@Produces(MediaType.APPLICATION_JSON +";charset=utf-8")
	public ArrayList<Hashtag> getTopHashtags() {
		HBaseConnectionManager connMan = new HBaseConnectionManager();	
		return connMan.getTopHashtags();
		
	}
	
	private ArrayList<NFLTeam> createTestTeams() {
		ArrayList<NFLTeam> test = new ArrayList<NFLTeam>();
		test.add(new NFLTeam("Green Bay Packers", 1, 1, null, null, null));
		test.add(new NFLTeam("Arizona Cardinals", 2, 1, null, null, null));
		test.add(new NFLTeam("New England Patriots", 7, 13, null, null, null));
		test.add(new NFLTeam("Atlanta Falcons", 2, 8, null, null, null));
		
		return test;		
	}
}