package org.kkruse.webherv.genes.sqlite;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface GeneDBConnector extends Closeable{

	public static class GeneEntry{
		public String probeSet;
		public String chromosome;
		public int start;
		public int end;
		public String strand;
		public String getProbeSet() {
			return probeSet;
		}
		public void setProbeSet(String probeSet) {
			this.probeSet = probeSet;
		}
		public String getChromosome() {
			return chromosome;
		}
		public void setChromosome(String chromosome) {
			this.chromosome = chromosome;
		}
		public int getStart() {
			return start;
		}
		public void setStart(int start) {
			this.start = start;
		}
		public int getEnd() {
			return end;
		}
		public void setEnd(int end) {
			this.end = end;
		}
		public String getStrand() {
			return strand;
		}
		public void setStrand(String strand) {
			this.strand = strand;
		}
	}
	
	Connection connectDb() throws Exception;

	void closeConnection(Connection conn) throws Exception;

	Map<String, GeneEntry> queryGenesById(String _geneTable, List<String> _probeSets);

	int createTableStet(Connection conn2, String table, String query) throws SQLException;

	void insertString(Connection conn2, String insert);

	void insertData(Connection conn2, String[][] data, String table, int blockSize);

	void insertFile(Connection conn2, String filename, String table, int[] cols)
			throws FileNotFoundException, IOException, ClassNotFoundException;

	ArrayList<String[]> sqlQuery(String q) throws SQLException;

	//==============================================================
	String getPathToDb();

	void setPathToDb(String p);

	String getDbName();

	void setDbName(String n);

}