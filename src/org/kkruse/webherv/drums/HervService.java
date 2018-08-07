package org.kkruse.webherv.drums;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kkruse.webherv.genes.sqlite.GeneDBConnector.GeneEntry;

import com.unister.semweb.biodrums.herv.HERV;


public interface HervService extends Closeable{

	
	public void openConnection() throws IOException;
		
//	public List<HERV> selectHervsInRange( int chromosome, int start, int end, int rangeSize, String range, int minLength, double maxEval ) throws DRUMSException, FileLockException, IOException;
	
//	public Map<String, Map<GeneEntry, List<HERV>>> selectHervsInRange( HervInputSettings inputSettings, GeneEntryTables geneEntryTables );
	
	public Map<GeneEntry, Map<Integer, List<HERV>>> selectHervsInRange( HervInputSettings inputSettings, String geneFile,  Map<String, List<GeneEntry>> geneEntryTable ) throws HervServiceException;

	public Map<GeneEntry, Map<Integer, List<HERV>>> selectHervsInRange( HervInputSettings inputSettings, String geneFile,  Map<String, List<GeneEntry>> geneEntryTable, HervServiceStatus status ) throws HervServiceException;

	
	public static class HervServiceStatus{
		private int uploadedFiles;
		private int processingFileCount;
		private int currCoordinatesTotal;
		private int currCoordinatesCount;
		
		public HervServiceStatus(){
			uploadedFiles = 0;
			processingFileCount = 0;
			currCoordinatesTotal = 0;
			currCoordinatesCount = 0;			
		}
		
		public int getUploadedFiles() {
			return uploadedFiles;
		}

		public void setUploadedFiles(int uploadedFiles) {
			this.uploadedFiles = uploadedFiles;
		}

		public int getProcessingFileCount() {
			return processingFileCount;
		}

		public void setProcessingFileCount(int processingFileCount) {
			this.processingFileCount = processingFileCount;
		}

		public int getCurrCoordinatesTotal() {
			return currCoordinatesTotal;
		}

		public void setCurrCoordinatesTotal(int currCoordinatesTotal) {
			this.currCoordinatesTotal = currCoordinatesTotal;
		}

		public int getCurrCoordinatesCount() {
			return currCoordinatesCount;
		}

		public void setCurrCoordinatesCount(int currCoordinatesCount) {
			this.currCoordinatesCount = currCoordinatesCount;
		}

	}
	
	
	public static class HervServiceException extends Exception{
		private static final long serialVersionUID = 1L;

		public HervServiceException( Exception e ){
			super( null, e );
		}
		public HervServiceException( String msg, Exception e ){
			super( msg, e );
		}
	}
	
	public static class HervInputSettings{
		public List<Integer> offset = null;
		public String  selectedRange = null;
		public String  selectedPlatform = null;
		public String  selectedGenome = null;
		public String  selectedVariant = null;
		public Integer minimalLength = null;	
		public Double  maxEvalue = null;	

		public HervInputSettings( 
				List<Integer> offset, 
				String  selectedRange, 
				String  selectedPlatform,
				String  selectedGenome,
				String  selectedVariant,
				Integer minimalLength,
				Double  maxEvalue
				){
			this.offset = offset;
			this.selectedRange = selectedRange;
			this.selectedPlatform = selectedPlatform;
			this.selectedGenome = selectedGenome;
			this.selectedVariant = selectedVariant;
			this.minimalLength = minimalLength;
			this.maxEvalue = maxEvalue;
		}

		@Override
		public String toString() {
			return "HervInputSettings [offset=" + offset + ", selectedRange=" + selectedRange + ", selectedPlatform="
					+ selectedPlatform + ", selectedGenome=" + selectedGenome + ", selectedVariant=" + selectedVariant
					+ ", minimalLength=" + minimalLength + ", maxEvalue=" + maxEvalue + "]";
		}
	}

	public static class GeneEntryTables{
		
		private String geneTabelName;
		private Map<String, List<GeneEntry>> geneEntryTables;
		
		public GeneEntryTables( String geneTableName ){
			this.setGeneTabelName(geneTableName);
		}

		
		public String getGeneTabelName() {
			return geneTabelName;
		}

		public void setGeneTabelName(String geneTabelName) {
			this.geneTabelName = geneTabelName;
		}

		public Map<String, List<GeneEntry>> getGeneEntryTables() {
			return geneEntryTables;
		}

		public void setGeneEntryTables(Map<String, List<GeneEntry>> geneEntryTables) {
			this.geneEntryTables = geneEntryTables;
		}

		public void addGeneEntryTables( String fileName, List<GeneEntry> geneEntries ) {
			if( this.geneEntryTables == null ){
				this.geneEntryTables = new HashMap<>();				
			}
			this.geneEntryTables.put( fileName, geneEntries );
		}		
		
	}


}

