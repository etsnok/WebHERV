package org.kkruse.webherv.genes.sqlite;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.kkruse.webherv.upload.FileUploader;

public class SqliteHg19DbConnector implements GeneDBConnector {

	//    SQLiteDatabase db = new SQLiteDatabase("jdbc:sqlite:test.db");
	//    Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db"); 

	//    private String pathToDB ="test.db";
//	private static String pathToDB ="WebContent/resources/db/hervDB.db";
	
	private static String pathToDB ="/Users/Konstantin/Documents/workspace/HervSever/WebContent/resources/db/hervDB.db";
	private String dbName;
	private Connection conn;
	
	private static final Logger LOG = Logger.getLogger( SqliteHg19DbConnector.class.getName() );

	public SqliteHg19DbConnector() throws ClassNotFoundException, SQLException {
		this( pathToDB );
	};

	public SqliteHg19DbConnector( String path ) throws ClassNotFoundException, SQLException{
		pathToDB = path;
	};



	/* (non-Javadoc)
	 * @see databases.sqlite.GeneDBConnector#connectDb()
	 */
	@Override
	public Connection connectDb() throws ClassNotFoundException, SQLException{
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:"+pathToDB);
		System.out.println("path:"+pathToDB);
		return conn;
	}
	
	/* (non-Javadoc)
	 * @see databases.sqlite.GeneDBConnector#closeConnection(java.sql.Connection)
	 */
	@Override
	public void closeConnection(Connection conn) throws Exception{
		conn.close();  
	}

	@Override
	public void close() throws IOException {
		try {
			conn.close();
		} catch (SQLException e) {
			LOG.warning( "Failed to close SQLite connection:" + e.getLocalizedMessage() );
		}
	}

	/* (non-Javadoc)
	 * @see databases.sqlite.GeneDBConnector#queryGenesById(java.lang.String, java.util.List)
	 */
	@Override
	public Map<String, GeneEntry> queryGenesById( String _geneTable, List<String> _probeSets ){

		if( _probeSets != null ){

			Map<String, GeneEntry> genes = new LinkedHashMap<>();

			int addedInsIndex = 1;
			int addedCount = 0;
			int BATCH_SIZE = 50;

			PreparedStatement stat = null;
			try{

				for( String probe : _probeSets ){

					if( addedInsIndex == 1 ){		
						// prepare the statement string:
						BATCH_SIZE = Math.min(50,  _probeSets.size() - addedCount );    			
						String prepedSqlQuery =  "SELECT probesetid, start, stop, strand, chrom FROM "+_geneTable+" WHERE probesetid IN (  " + createInClauseString( BATCH_SIZE ) + "  )";
						stat = conn.prepareStatement( prepedSqlQuery );		
					}	

					stat.setString( addedInsIndex, probe );
//					stat.addBatch();
					addedInsIndex++;
					addedCount++;

					if( addedInsIndex >= BATCH_SIZE ){
						try( ResultSet result = stat.executeQuery() ){    						
							while( result.next() ){
								GeneEntry newEntry = new GeneEntry();
								newEntry.probeSet   = result.getString( 1 );
								newEntry.start      = result.getInt( 2 );
								newEntry.end        = result.getInt( 3 );
								newEntry.strand     = Strand.getStrand(result.getString( 4 ));
								newEntry.chromosome = result.getString( 5 );
								genes.put( newEntry.probeSet, newEntry );
							}
						} catch( SQLException e ){
							LOG.warning( "SQLlite Execution failed:" + e.getLocalizedMessage() );
						} finally{
							addedInsIndex = 1;
						}
					}
				}
				
			} catch( SQLException se ){
				LOG.warning( "SQLlite statement preparation failed:" + se.getLocalizedMessage() );
			}
			
			return genes;

		}
		return null;
	}


	private String createInClauseString( int _size ){
		StringBuilder inClause = new StringBuilder();
		boolean firstValue = true;
		for (int i=0; i < _size; i++) {
			if ( firstValue ) {
				inClause.append('?');
				firstValue = false;
			} else {
				inClause.append(',').append('?');
			}
		}
		return inClause.toString();
	}


//	public static class HervEntry {
//		public String id;
//		public int start;
//		public int stop;
//		public int distance;
//		public String chromosome;
//	}
//
//	// load the HERVs:
//	public HervEntry queryNearestHervToCenter( int _geneStart, int _geneStop, String _strand, String _chromosome, String _hervTable ){
//
//		int geneCenter = _geneStart + (_geneStop - _geneStart + 1)/2; 
//		String sqlQuery = "SELECT *, MIN( ABS( "+geneCenter+" - ( start + ((stop-start +1)/2) )) )as 'distance' FROM hg18_rawHERV where chrom='"+ _chromosome + "'";
//
//		HervEntry entry = null;
//		try( Statement state = conn.createStatement() ){
//
//			ResultSet results = state.executeQuery(sqlQuery);
//
//			while( results.next() ){
//				entry = new HervEntry();
//				entry.id = results.getString("hervid") ;
//				entry.start = results.getInt("start") ;
//				entry.stop = results.getInt("stop") ;
//				entry.distance = results.getInt("distance") ;
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return entry;
//	}
//	
//
//	public List<HervEntry> queryHervsInRange( int _offset, String _range, int _geneStart, int _geneStop, String _strand, String _chromosome, String _hervTable ){
//		
//        String q=null;
//        
//        if( "+/-".equals( _range ) ){
//            q = "SELECT * FROM "+_hervTable+" WHERE chrom='"+_chromosome+"' AND (start BETWEEN ("+_geneStart+"-"+_offset+") "
//                + "AND ("+_geneStop+"+"+_offset+") OR stop between ("+_geneStart+"-"+_offset+") and ("+_geneStop+"+"+_offset+"));";
//        }else if( "-".equals( _range ) ){
//            //dependes on the strand of the probeset!!!
//            if( _strand.equals( "+" ) ){
//                q = "select * from (select * from "+_hervTable+" h where "
//                    + " (h.stop between ("+_geneStart+"-"+_offset+") and ("+_geneStart+"))"
//                    + " ) X "
//                    + " where chrom=\""+_chromosome+"\"; ";
//            }else{
//                q = "select * from (select * from "+_hervTable+" h where "
//                    + " (h.start between ("+_geneStop+") and ("+_geneStop+"+"+_offset+"))"
//                    + " ) X "
//                    + " where chrom=\""+_chromosome+"\"; ";
//            }
//            
//        }else{
//            //dependes on the strand of the probeset!!!
//            if( "+".equals( _range ) ){
//                q = "select * from (select * from "+_hervTable+" h where "
//                    + " (h.start between ("+_geneStop+") and ("+_geneStop+"+"+_offset+"))"
//                    + " ) X "
//                    + " where chrom=\""+_chromosome+"\"; ";
//            }else{
//                q = "select * from (select * from "+_hervTable+" h where "
//                    + " (h.stop between ("+_geneStart+"-"+_offset+") and ("+_geneStart+"))"
//                    + " ) X "
//                    + " where chrom=\""+_chromosome+"\"; ";
//            } 
//        }
//		
//        List<HervEntry> resultHervs = new ArrayList<HervEntry>();
//        
//    	try( Statement state = conn.createStatement() ){
//
//			ResultSet results = state.executeQuery( q );
//
//			while( results.next() ){
//				HervEntry entry = new HervEntry();
//				entry.id = results.getString("hervid");
//				entry.start = results.getInt("start");
//				entry.stop = results.getInt("stop");
//				entry.chromosome = results.getString("chrom");
//				
//				resultHervs.add(entry);
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return resultHervs;
//	}
	


	/* (non-Javadoc)
	 * @see databases.sqlite.GeneDBConnector#createTableStet(java.sql.Connection, java.lang.String, java.lang.String)
	 */
	@Override
	public int createTableStet(Connection conn2, String table, String query)throws SQLException{

		Statement stat = conn.createStatement();
		stat.executeUpdate("drop table if exists "+table+";");
		return stat.executeUpdate(query);
	}


	/* (non-Javadoc)
	 * @see databases.sqlite.GeneDBConnector#insertString(java.sql.Connection, java.lang.String)
	 */
	@Override
	public void insertString(Connection conn2, String insert ){
		try{
			PreparedStatement prep = conn.prepareStatement(insert);

			/*  prep.setString(1, "Gandhi");
           prep.setString(2, "politics");
               //prep.setString(3, "100"); 
            prep.setString(3, "2,99");*/
			prep.addBatch();
			conn.setAutoCommit(false);
			prep.executeBatch();
			conn.setAutoCommit(true);

		}catch(SQLException e){
			System.err.println(e);      
		}

	}

	/* (non-Javadoc)
	 * @see databases.sqlite.GeneDBConnector#insertData(java.sql.Connection, java.lang.String[][], java.lang.String, int)
	 */
	@Override
	public void insertData(Connection conn2, String[][] data, String table, int blockSize){

		try{
			Statement stat = conn.createStatement();
			stat.executeUpdate("drop table if exists t1;");
			//String q = "insert into "+table+" values ";
			conn.setAutoCommit(false);

			String q = "insert into "+table+" values ";

			for(int i= 0; i < data.length; i++){

				// stat = conn.createStatement();
				// String q = "insert into "+table+" values ";
				q += "(\""+data[i][0];

				for(int j= 1;j < data[i].length; j++){
					q += "\",\""+data[i][j];
				}

				q += "\")";
				if(i != data.length-1){
					q += ",";
				}else{
					q += ";";
				}
				//  System.out.println(q);
				// PreparedStatement prep = conn.prepareStatement(q);
				// prep.executeBatch();

			}
			System.out.println(q);
			// stat.execute(q);

			conn.setAutoCommit(true);
			//System.out.println("dfljk");
			//Statement stat = conn.createStatement();
			//ResultSet rs = stat.executeQuery("select * from t1;");
			// System.out.println("hmm");
		}catch(SQLException e){
			System.err.println(e);      
		}

	}


	public void insertFile2(Connection conn2, String filename, String table, int blockSize) throws FileNotFoundException, IOException{

		try{
			Statement stat = conn.createStatement();
			stat.executeUpdate("drop table if exists t1;");
			//String q = "insert into "+table+" values ";

			FileInputStream fstream = new FileInputStream(filename);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			int count=0;
			int currIndex=0;

			conn.setAutoCommit(false);
			String q = "insert into "+table+" values ";            
			while (((strLine = br.readLine()) != null)){

				String[] spl = strLine.split("\t");
				q += "(\'"+spl[0];

				for(int j= 1;j < spl.length; j++){
					q += "\',\'"+spl[j];
				}

				q += "\')";
				if(currIndex < 10-1){
					q += ",\n";
					count++;
				}else{
					q += ";";

					System.out.println(q);
					PreparedStatement prep = conn.prepareStatement(q);
					// prep.addBatch();

					// prep.executeBatch();

					prep.executeUpdate();

					//prep.executeQuery();
					// prep.executeQuery(q);
					currIndex= 0;
					q = "insert into "+table+" values ";
				}


				currIndex++;


			}

			if(currIndex < 10){
				q.replaceAll(",$", ";");

				///           db.insertData(conn, s, "t1", 10);
				System.out.println(q);
				//PreparedStatement prep = conn.//prepareStatement(q);
				// prep.executeQuery();

				//     Statement stat = conn.createStatement();
				//  stat.executeUpdate(q);
			}
			in.close(); 

			conn.setAutoCommit(true);
			//System.out.println("dfljk");
			//Statement stat = conn.createStatement();
			//ResultSet rs = stat.executeQuery("select * from t1;");
			// System.out.println("hmm");
		}catch(SQLException e){
			System.err.println(e);      
		}

	}


	/* (non-Javadoc)
	 * @see databases.sqlite.GeneDBConnector#insertFile(java.sql.Connection, java.lang.String, java.lang.String, int[])
	 */
	@Override
	public void insertFile(Connection conn2, String filename, String table, int[] cols) throws FileNotFoundException, IOException, ClassNotFoundException{
		
		try{

			//                Class.forName("org.sqlite.JDBC");
			//   Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db"); 

			Statement stat = conn.createStatement();
			// stat.executeUpdate("drop table if exists t1;");
			//  stat.executeUpdate("create table t1( s varchar , e varchar, r varchar );");


			//    stat = conn.createStatement();
			//stat.executeUpdate("drop table if exists t1;");
			//String q = "insert into "+table+" values ";

			FileInputStream fstream = new FileInputStream(filename);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			int count=0;
			int currIndex=0;

			//   conn.setAutoCommit(false);
			PreparedStatement prep;
			prep = conn.prepareStatement("BEGIN IMMEDIATE TRANSACTION");
			//prep.execute("BEGIN IMMEDIATE TRANSACTION");
			String[] spl;
			String q;
			//long startTime = System.currentTimeMillis();
			while (((strLine = br.readLine()) != null)){
				//System.out.println(strLine.charAt(1));
				if(!strLine.startsWith("#") && !strLine.startsWith("\"probe")){
					spl = strLine.split(",");
					q = "insert into "+table+" values "; 
					spl[cols[0]-1].replaceAll("\"", "");
					q += "(\'"+spl[cols[0]-1];

					for(int j= cols[0]-1;j < cols.length-1; j++){
						//  System.out.println("j: "+j+" "+cols[j]+" "+spl.length);
						spl[cols[j]-1].replaceAll("\"", "");
						q += "\',\'"+spl[cols[j]-1];
					}

					q += "\')";
					q += ";";

					//  System.out.println(q);
					prep = conn.prepareStatement(q);
					// prep.addBatch();
					if(count % 10000 == 0){
						//  conn.setAutoCommit(true);
						//  prep = conn.prepareStatement("BEGIN IMMEDIATE TRANSACTION");
						//  prep.execute("COMMIT TRANSACTION");
						System.out.println("insert!");
						//     conn.setAutoCommit(false);
					}
					count++;
					// prep.executeBatch();


					prep.executeUpdate();
				}
			}
			in.close(); 
			prep.execute("COMMIT TRANSACTION");
			//  conn.setAutoCommit(true);
			//System.out.println("dfljk");
			//Statement stat = conn.createStatement();
			//ResultSet rs = stat.executeQuery("select * from t1;");
			// System.out.println("hmm");
		}catch(SQLException e){
			System.err.println(e);      
		}

	}


	/* (non-Javadoc)
	 * @see databases.sqlite.GeneDBConnector#sqlQuery(java.lang.String)
	 */
	@Override
	public ArrayList<String[]> sqlQuery( String q) throws SQLException {

		Statement stat = conn.createStatement();
		ArrayList<String[]> arr = new ArrayList<String[]>();

		/*  for (int i = 0; i < 500; i++) {
            arr.add(null);   
        }
		 */
		try {

			ResultSet rs = stat.executeQuery(q);
			ResultSetMetaData rsMetaData = rs.getMetaData();
			/*System.out.println("bmm");
            stat.setFetchDirection(ResultSet.FETCH_UNKNOWN);
            rs.last();

            int matrix_len = rs.getRow();
            //rs.first();
            System.out.println(rs.getFetchSize());
            tmp = new String[matrix_len][rs.getFetchSize()];
            //rs.beforeFirst();
            int i= 0;
			 * 
			 */
			//   int idx=0;
			while (rs.next()){

				String[] str = new String[rsMetaData.getColumnCount()];
				for(int j = 1; j <= rsMetaData.getColumnCount(); j++){
					str[j-1] = rs.getString(j);
				} 
				arr.add(str);
				//       arr.set(idx, str);
				//     idx++;
			}
		}catch(SQLException e){
			System.err.println(e);
		}

		return arr;
	};



	//==============================================================
	/* (non-Javadoc)
	 * @see databases.sqlite.GeneDBConnector#getPathToDb()
	 */
	@Override
	public String getPathToDb(){ return pathToDB; };
	/* (non-Javadoc)
	 * @see databases.sqlite.GeneDBConnector#setPathToDb(java.lang.String)
	 */
	@Override
	public void setPathToDb(String p){pathToDB = p;};
	/* (non-Javadoc)
	 * @see databases.sqlite.GeneDBConnector#getDbName()
	 */
	@Override
	public String getDbName(){ return dbName; };
	/* (non-Javadoc)
	 * @see databases.sqlite.GeneDBConnector#setDbName(java.lang.String)
	 */
	@Override
	public void setDbName(String n){dbName = n;};



	//==============================================================
	public static void main(String[] args) throws Exception {

		GeneDBConnector connect = new SqliteHg19DbConnector();

		List<String> probes = Arrays.asList( new String[]{ "4028411" , "4028412", "4028415", "4028416", "4028437", "4028419", "4028421", "4028422", "4028439", "4028427", "4028429", "4028430", "4028431", "4028493", "4028495", "4028497", "4028465", "4028477", "4028502" } );

		Map<String, GeneEntry> results = connect.queryGenesById( "hg19_HuEx_1_0_st_v2_na32_probeset", probes );
//		for( GeneEntry e : results ){
//			System.out.println( e.probeSet + ", " + e.chromosome );
//			
//			List<HervEntry> hs = connect.queryHervsInRange(7000, "-", e.start, e.end, e.strand, e.chromosome, "hg19_rawHERV" );
//			for( HervEntry h : hs ){
//				System.out.println( h.id + ", " + h.start );
//			}
//		}

	}



}
