package de.fhm.bigdata.projekt.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import com.google.protobuf.ServiceException;

import de.fhm.bigdata.projekt.hbase.model.*;

public class HBaseConnectionManager {

	private static Configuration config;
	
	private static String TABLE_TEAMS = "teams";
	private static String TABLE_HASHTAGS = "hashtags";
	
	private static final byte[] COLUMN_FAMILY_HASHTAGS = Bytes.toBytes("hashtag_family");
	private static final byte[] COLUMN_HASHTAGS_NAME = Bytes.toBytes("hashtag");
	private static final byte[] COLUMN_HASHTAGS_COUNTER = Bytes.toBytes("counter");
	private static final byte[] COLUMN_HASHTAGS_TIMESTAMP = Bytes.toBytes("timestamp");
	
	private static final byte[] COLUMN_FAMILY_TEAMS = Bytes.toBytes("team_family");
	private static final byte[] COLUMN_TEAMS_NAME = Bytes.toBytes("name");
	private static final byte[] COLUMN_TEAMS_RANK = Bytes.toBytes("rank");
	private static final byte[] COLUMN_TEAMS_ID = Bytes.toBytes("id");
	private static final byte[] COLUMN_TEAMS_DIVISION = Bytes.toBytes("division");
	private static final byte[] COLUMN_TEAMS_FORECAST = Bytes.toBytes("forecast");
	private static final byte[] COLUMN_TEAMS_SYNONYM = Bytes.toBytes("synonym");
	
	public HBaseConnectionManager() {
        try {
            config = HBaseConfiguration.create();
            
            /*config.clear();
            config.set("hbase.zookeeper.quorum", "172.17.0.2");
            config.set("hbase.zookeeper.property.clientPort","2181");
            config.set("hbase.master", "172.17.0.2:60000");
            */
            //HBaseConfiguration config = HBaseConfiguration.create();
            //config.set("hbase.zookeeper.quorum", "localhost");  // Here we are running zookeeper locally
            HBaseAdmin.checkHBaseAvailable(config);


            System.out.println("HBase is running!");
        //  createTable(config);    
            //creating a new table
        } catch (MasterNotRunningException e) {
            System.out.println("HBase is not running!");
        } catch (ZooKeeperConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<NFLTeam> getTeamRating() {
		ArrayList<NFLTeam> resultList = new ArrayList<NFLTeam>();
		ResultScanner rs = null;
		Result res = null;
		String hashtag = null;
		int rank;
		int id;
		String division;
		String forecast;
		String[] synonym;
		NFLTeam result = null;
		try {
			HTable table = new HTable(config, TABLE_TEAMS);
			Scan scan = new Scan();
			scan.addColumn(COLUMN_FAMILY_TEAMS, COLUMN_TEAMS_NAME);
			scan.addColumn(COLUMN_FAMILY_TEAMS, COLUMN_TEAMS_RANK);
			scan.addColumn(COLUMN_FAMILY_TEAMS, COLUMN_TEAMS_ID);
			scan.addColumn(COLUMN_FAMILY_TEAMS, COLUMN_TEAMS_DIVISION);
			scan.addColumn(COLUMN_FAMILY_TEAMS, COLUMN_TEAMS_FORECAST);
			scan.addColumn(COLUMN_FAMILY_TEAMS, COLUMN_TEAMS_SYNONYM);
			rs = table.getScanner(scan);
			while ((res = rs.next()) != null) {

				byte[] rohHashtag = res.getValue(COLUMN_FAMILY_TEAMS, COLUMN_TEAMS_NAME);
				byte[] rohRank = res.getValue(COLUMN_FAMILY_TEAMS, COLUMN_TEAMS_RANK);
				byte[] rohId = res.getValue(COLUMN_FAMILY_TEAMS, COLUMN_TEAMS_ID);
				byte[] rohDivision = res.getValue(COLUMN_FAMILY_TEAMS, COLUMN_TEAMS_DIVISION);
				byte[] rohForecast = res.getValue(COLUMN_FAMILY_TEAMS, COLUMN_TEAMS_FORECAST);
				byte[] rohSynonym = res.getValue(COLUMN_FAMILY_TEAMS, COLUMN_TEAMS_SYNONYM);

				hashtag = Bytes.toString(rohHashtag);
				rank = Bytes.toInt(rohRank);
				id = Bytes.toInt(rohId);
				division = Bytes.toString(rohDivision);
				forecast = Bytes.toString(rohForecast);
				synonym = Bytes.toString(rohSynonym).split(";");
				
				result = new NFLTeam (hashtag, rank, id, division, forecast, synonym);
				if (!resultList.contains(result)) {
					resultList.add(result);
				}
			}
		} catch (IOException e) {	
			System.out.println("Exception occured in retrieving data");
		} finally {
			rs.close();
		}
		Collections.sort(resultList, NFLTeam.getNFLTeamByRank());
		return resultList;
	}
	
	public ArrayList<Hashtag> getTopHashtags() {
		ArrayList<Hashtag> resultList = new ArrayList<Hashtag>();
		ResultScanner rs = null;
		Result res = null;
		String hashtag = null;
		String timestamp = null;
		int counter;
		Hashtag result = null;
		try {
			HTable table = new HTable(config, TABLE_HASHTAGS);
			Scan scan = new Scan();
			scan.addColumn(COLUMN_FAMILY_HASHTAGS, COLUMN_HASHTAGS_NAME);
			scan.addColumn(COLUMN_FAMILY_HASHTAGS, COLUMN_HASHTAGS_COUNTER);
			scan.addColumn(COLUMN_FAMILY_HASHTAGS, COLUMN_HASHTAGS_TIMESTAMP);
			rs = table.getScanner(scan);
			while ((res = rs.next()) != null) {

				byte[] rohHashtag = res.getValue(COLUMN_FAMILY_HASHTAGS, COLUMN_HASHTAGS_NAME);
				byte[] rohCounter = res.getValue(COLUMN_FAMILY_HASHTAGS, COLUMN_HASHTAGS_COUNTER);
				byte[] rohTimestamp = res.getValue(COLUMN_FAMILY_HASHTAGS, COLUMN_HASHTAGS_TIMESTAMP);

				hashtag = Bytes.toString(rohHashtag);
				counter = Bytes.toInt(rohCounter);
				timestamp = Bytes.toString(rohTimestamp);
				result = new Hashtag (hashtag, counter, timestamp);
				if (!resultList.contains(result) && !hashtag.isEmpty()) {
					resultList.add(result);
				}
			}
		} catch (IOException e) {	
			System.out.println("Exception occured in retrieving data");
		} finally {
			rs.close();
		}
		Collections.sort(resultList, Hashtag.getHashtagByCounter());
		return resultList;
	}

	
}