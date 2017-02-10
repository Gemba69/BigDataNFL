package de.fhm.bigdata.projekt.hbase.model;

import java.util.Comparator;

public class NFLTeam {

	private String teamName;
	private Integer rank;
	private int id;
	private String division;
	private String forecast;
	private String[] synonyms;
	
	
	public NFLTeam(String teamName, int rank, int id, String division, String forecast, String[] synonyms) {
		this.teamName = teamName;
		this.rank = rank;
		this.id = id;
		this.division = division;
		this.forecast = forecast;
		this.synonyms = synonyms;
	}


	public String getTeamName() {
		return teamName;
	}


	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}


	public int getRank() {
		return rank;
	}


	public void setRank(int rank) {
		this.rank = rank;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}


	public String getDivision() {
		return division;
	}


	public void setDivision(String division) {
		this.division = division;
	}


	public String getForecast() {
		return forecast;
	}


	public void setForecast(String forecast) {
		this.forecast = forecast;
	}


	public String[] getSynonyms() {
		return synonyms;
	}


	public void setSynonyms(String[] synonyms) {
		this.synonyms = synonyms;
	}
	
	public static Comparator<NFLTeam> getNFLTeamByRank()
	{   
	 Comparator<NFLTeam> comp = new Comparator<NFLTeam>(){
	     @Override
	     public int compare(NFLTeam t1, NFLTeam t2)
	     {
	         return t1.rank.compareTo(t2.rank);
	     }        
	 };
	 return comp;
	}  
	
	
}
