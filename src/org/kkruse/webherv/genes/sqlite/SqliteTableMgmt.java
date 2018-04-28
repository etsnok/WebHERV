package org.kkruse.webherv.genes.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqliteTableMgmt {

	private Connection conn;
	private List<String> tables;
	
	public SqliteTableMgmt( Connection conn ){
		this.conn = conn;
		
		tables = getTables();
	}
	
	
	String createBEDtableQuery = "matches UNSIGNED INTEGER,"
+"misMatches UNSIGNED INTEGER,"
+"repMatches UNSIGNED INTEGER,"
+"nCount UNSIGNED INTEGER,"
+"qNumInsert UNSIGNED INTEGER,"
+"qBaseInsert UNSIGNED INTEGER,"
+"tNumInsert UNSIGNED INTEGER,"
+"tBaseInsert UNSIGNED INTEGER,"
+"strand	TEXT,"
+"qName	TEXT,"
+"qSize UNSIGNED INTEGER,"
+"qStart UNSIGNED INTEGER,"
+"qEnd UNSIGNED INTEGER,"
+"tName	TEXT,"
+"tSize UNSIGNED INTEGER,"
+"tStart UNSIGNED INTEGER,"
+"tEnd UNSIGNED INTEGER,"
+"blockCount UNSIGNED INTEGER,"
+"blockSizes	TEXT,"
+"qStarts	TEXT,"
+"tStarts	TEXT";
	
	
	public void newBedFormatTable( String tableName ){
		newBedFormatTable( tableName, false );
	}
	public void newBedFormatTable( String tableName, boolean _dropTableIfExists ){
	
		
		String sqlQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + createBEDtableQuery + ");";
		try {
			Statement state = conn.createStatement();
			state.executeUpdate( sqlQuery );

			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//
	public void insertBedTableLine( String _line ){
		
//		PreparedStatement state = conn.prepareStatement(null );
//		state.setString(1, x);
		
	}

	
	
	
	public List<String> getTables(){
		
		List<String> tables = new ArrayList<String>();
		try {
			Statement state = conn.createStatement();
			ResultSet results = state.executeQuery("SELECT name FROM sqlite_master WHERE type='table'");

			while( results.next() ){
				tables.add( results.getString("name") );
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tables;
	}
	
}
