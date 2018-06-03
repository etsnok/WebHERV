package org.kkruse.webherv.drums;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kkruse.webherv.genes.sqlite.GeneDBConnector.GeneEntry;

import com.unister.semweb.biodrums.herv.HERV;
import com.unister.semweb.drums.DRUMSParameterSet;
import com.unister.semweb.drums.api.DRUMS;
import com.unister.semweb.drums.api.DRUMSInstantiator;
import com.unister.semweb.drums.api.DRUMSReader;
import com.unister.semweb.drums.file.FileLockException;
import com.unister.semweb.drums.api.DRUMS.AccessMode;
import com.unister.semweb.drums.api.DRUMSException;

/**
 * @author Konstantin Kruse
 *
 */
public class DrumsHervService implements HervService {

	private static final int MAX_GET_READER_TRIALS = 20;   // maximim number of trials
	private static final int WAIT_BETWEEN_TRIAL_MS = 2000; // wait time between trails

	/** The parameter defining one drums database */
	private DRUMSParameterSet<HERV> globalParameters;
	/** The loaded drums database */
	private DRUMS<HERV> drums;
	/**Path to the drums database */
	private Path sdrumDatabase;

	private Integer maxHervLength;

	private static final Logger LOG = Logger.getLogger( DrumsHervService.class.getName() );

	// ------------------------------------------------------------------------
	/**
	 * @param sdrumDatabase
	 * @throws DRUMSException
	 * @throws IOException
	 */
	public DrumsHervService( Path sdrumDatabase, Integer maxHervLength ) throws DRUMSException, IOException{
		this.sdrumDatabase = sdrumDatabase;
		this.maxHervLength = maxHervLength;
		globalParameters = new DRUMSParameterSet<HERV>( this.sdrumDatabase.toFile() );

	}

	// ------------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see org.kkruse.webherv.drums.HervService#openConnection()
	 */
	@Override
	public void openConnection() throws IOException {
		drums = DRUMSInstantiator.openTable( AccessMode.READ_ONLY, globalParameters );
	}

	// ------------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		try {
			if( drums != null )
				drums.close();
		} catch (InterruptedException e) {
			throw new IOException( e );
		}
	}


	// ------------------------------------------------------------------------
	/**
	 * Create the {@link HERVRangeDefinition} upstream of the gene.
	 * 
	 * @param chromosome the chromosome of the HERVs.
	 * @param geneStart  the start position of the HERV.
	 * @param geneEnd    the end position of the HERV.
	 * @param geneStrand the stand of the HERV
	 * @param rangeSize  the range size or offset.
	 * @return the {@link HERVRangeDefinition} upstream of the gene.
	 */
	public HERVRangeDefinition selectHervsInRangeUpstream( GeneEntry geneEntry, int rangeSize, int longestHerv ){

		Integer chromInt = chromosomeToInt( geneEntry.chromosome );

		if( chromInt == null ){
			LOG.warning( "GeneEntry chromosome not found:" + geneEntry.chromosome );
			return null;
		}

		int rangeStart;
		int rangeEnd;

		if( "+".equals( geneEntry.strand ) ){
			rangeStart =  geneEntry.start - rangeSize - longestHerv;
			rangeEnd   = geneEntry.end + longestHerv;
		} else if( "-".equals( geneEntry.strand ) ){
			rangeStart = geneEntry.start - longestHerv;
			rangeEnd   = geneEntry.end + rangeSize + longestHerv;
		} else {
			throw new IllegalStateException( "Ivalide stand gene type:'" + geneEntry.strand + "'" );
		}

		return createHERVRangeDefinition( geneEntry, chromInt, rangeStart, rangeEnd);

	}

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	/**
	 * Create the {@link HERVRangeDefinition} downstream of the gene.
	 * 
	 * @param geneEntry the chromosome of the HERVs.
	 * @param geneStart  the start position of the HERV.
	 * @param geneEnd    the end position of the HERV.
	 * @param geneStrand the stand of the HERV
	 * @param rangeSize  the range size or offset.
	 * @return the {@link HERVRangeDefinition} downstream of the gene.
	 */
	public HERVRangeDefinition selectHervsInRangeDownstream( GeneEntry geneEntry, int rangeSize, int longestHerv ){

		Integer chromInt = chromosomeToInt( geneEntry.chromosome );

		if( chromInt == null ){
			LOG.warning( "GeneEntry chromosome not found:" + geneEntry.chromosome );
			return null;
		}

		int rangeStart; // start position of the query range
		int rangeEnd;   // end position of the query range

		if( "+".equals( geneEntry.strand ) ){
			rangeStart = geneEntry.start - longestHerv;
			rangeEnd   = geneEntry.end + rangeSize + longestHerv;
		} else if( "-".equals( geneEntry.strand ) ){
			rangeStart = geneEntry.start - rangeSize - longestHerv;
			rangeEnd   = geneEntry.end + longestHerv;
		} else {
			throw new IllegalStateException( "Ivalide stand gene type:'" + geneEntry.strand + "'" );
		}

		return createHERVRangeDefinition( geneEntry, chromInt, rangeStart, rangeEnd);
	}


	// ------------------------------------------------------------------------
	/**
	 * Create the {@link HERVRangeDefinition} overlapping of the gene.
	 * 
	 * @param chromosome the chromosome of the HERVs.
	 * @param geneStart  the start position of the HERV.
	 * @param geneEnd    the end position of the HERV.
	 * @param geneStrand the stand of the HERV
	 * @param rangeSize  the range size or offset.
	 * @return the {@link HERVRangeDefinition} overlapping of the gene.
	 */
	public HERVRangeDefinition selectHervsInRangeOverlap( GeneEntry geneEntry, int rangeSize, int longestHerv ){

		Integer chromInt = chromosomeToInt( geneEntry.chromosome );

		if( chromInt == null ){
			LOG.warning( "GeneEntry chromosome not found:" + geneEntry.chromosome );
			return null;
		}

		int rangeStart = geneEntry.start - rangeSize - longestHerv;
		int rangeEnd   = geneEntry.end + rangeSize + longestHerv;

		return createHERVRangeDefinition( geneEntry, chromInt, rangeStart, rangeEnd );
	}



	@Override
	public Map<GeneEntry, Map<Integer, List<HERV>>> selectHervsInRange( HervInputSettings inputSettings, String geneFile,  Map<String, List<GeneEntry>> geneEntryTable ) throws HervServiceException{

		if( globalParameters == null ){
			try {
				globalParameters = new DRUMSParameterSet<HERV>( sdrumDatabase.toFile() );
			} catch (DRUMSException | IOException e) {
				throw new HervServiceException( e );
			}
		}

		Map<GeneEntry, Map<Integer, List<HERV>>> probesetHervsMap = new LinkedHashMap<>();

		DRUMSReader<HERV> reader = null;
		try{
			reader = tryToGetDrumsReader();

			if( reader == null ){
				return probesetHervsMap;
			}
			
			// get the list of genes/probesets
			List<GeneEntry> geneEntries = geneEntryTable.get( geneFile );
			int count = 0;
			for( GeneEntry geneEntry : geneEntries ){

				if( LOG.isLoggable( Level.FINE ) ){
					LOG.fine( "SelectHervInRangeDownstream - chrom:" + geneEntry.chromosome + ", geneStart:" + geneEntry.start 
							+ ", geneEnd:" + geneEntry.end + ", geneStrand:" + geneEntry.strand + ", renage:" + inputSettings.selectedRange
							+ ", minLen:"+   inputSettings.minimalLength + ", maxE:" + inputSettings.maxEvalue );
				}

				HERVRangeDefinition rangeDef = null;

				if( inputSettings.selectedRange.equals( "+" ) ){
					rangeDef = selectHervsInRangeDownstream( geneEntry, inputSettings.offset.get( inputSettings.offset.size() - 1 ), maxHervLength );

				} else if( inputSettings.selectedRange.equals( "-" ) ){
					rangeDef = selectHervsInRangeUpstream( geneEntry, inputSettings.offset.get( inputSettings.offset.size() - 1 ), maxHervLength );

				} else if(  inputSettings.selectedRange.equals( "+/-" ) ){
					rangeDef = selectHervsInRangeOverlap( geneEntry, inputSettings.offset.get( inputSettings.offset.size() - 1 ), maxHervLength );
				
				} else {
					throw new IllegalStateException( "Invalide range type:'" + inputSettings.selectedRange + "'." );
				}

				try {
					Map<Integer, List<HERV>> filteredRange = null;
					if( rangeDef != null && rangeDef.lowerKey != null && rangeDef.upperKey != null ){
						// read the hervs inside the range:
						List<HERV> range = readHERVsInRange( reader, rangeDef.lowerKey, rangeDef.upperKey );

						// filter data
						filteredRange = filterRange( inputSettings.selectedRange, rangeDef.geneEntry, range, inputSettings.minimalLength, inputSettings.maxEvalue, inputSettings.offset );
						probesetHervsMap.put( geneEntry, filteredRange );
					} else {
						LOG.warning("HERVRangeDefinition is noz proper set:" + rangeDef );
					}

				} catch ( IOException e ) {
					LOG.log( Level.WARNING, "Failed to read HERVs in range", e );
				}

				count++;
				if( count % 500 == 0 && count > 0 ){
					LOG.info( "Finished:" + count +" of:" + geneEntries.size() );
				}

			}

		} catch ( IOException | InterruptedException e ) {
			LOG.warning( "Failed to open reader exception:" + e.getLocalizedMessage() );  
		} finally{
			if( reader != null ){
				reader.closeFiles();
			}
		}

		return probesetHervsMap;
	}



	private DRUMSReader<HERV> tryToGetDrumsReader() throws IOException, InterruptedException{

		DRUMSReader<HERV> reader = null;
		int count_trials = 0;

		while( reader == null && count_trials <= MAX_GET_READER_TRIALS ){
			try {
				reader = drums.getReader();
			} catch (FileLockException e) {
				// Wait a bit before trying again to get a reader:
				Thread.sleep( WAIT_BETWEEN_TRIAL_MS );
			} 

			count_trials++;
		}
		
		if( reader == null ){
			LOG.severe( "Couldn't get a DRUMS reder!" );
		}

		return reader;
	}

	// ------------------------------------------------------------------------
	/**
	 * @param lowerKey
	 * @param upperKey
	 * @return
	 * @throws IOException
	 */
	private List<HERV> readHERVsInRange( DRUMSReader<HERV> reader, HERV lowerKey, HERV upperKey ) throws IOException{
		List<HERV> range = null;
		try{
			range = reader.getRange(lowerKey.getKey(), upperKey.getKey());
		} catch( IOException e ){
			LOG.warning( "Faile to read HERV in range:" + e.getLocalizedMessage() );
		} 

		return range;
	}



	// ------------------------------------------------------------------------
	/**
	 * Filter the {@link HERV}s in the range by their length and eValue.
	 * 
	 * @param range1 the list to filter.
	 * @param minLength the minimal length of a {@link HERV}.
	 * @param maxEval the maximal eValue of a {@link HERV}.
	 * @return the filtered list of HERVs.
	 */
	private Map<Integer, List<HERV>> filterRange( String range, GeneEntry geneEntry, List<HERV> range1, int minLength, double maxEval, List<Integer> offsetList ){

		Map<Integer, List<HERV>> filteredRange = new LinkedHashMap<Integer, List<HERV>>();

		for( Integer offset : offsetList ){
			filteredRange.put( offset, new ArrayList<HERV>() );
		}

		if( range1 != null ){
			//			Iterator<HERV> rangeIterator = range1.iterator();
			//			while( rangeIterator.hasNext() ) {
			//				HERV herv = rangeIterator.next();
			for( HERV herv : range1 ){

				int hervLength = herv.getEndPositionChromosome() - herv.getStartPositionChromosome() + 1;
				if( herv.getEValue() <= maxEval && hervLength >= minLength ) {

					for( Integer offset : filteredRange.keySet() ){

						if( range.equals( "+/-" ) && herv.getEndPositionChromosome() >= ( geneEntry.start - offset ) && herv.getStartPositionChromosome() <= ( geneEntry.start + offset ) ){
							filteredRange.get( offset ).add( herv );
						} else if( range.equals( "+" ) ){
							if( geneEntry.strand.equals( "+" ) && herv.getStartPositionChromosome() <= ( geneEntry.getEnd() + offset ) && herv.getEndPositionChromosome() >= geneEntry.getStart() ){
								filteredRange.get( offset ).add( herv );
							} else if( geneEntry.strand.equals( "-" ) && herv.getEndPositionChromosome() >= ( geneEntry.getStart() - offset ) && herv.getStartPositionChromosome() <= geneEntry.getEnd() ){
								filteredRange.get( offset ).add( herv );
							}
						} else if( range.equals( "-" ) ){
							if( geneEntry.strand.equals( "+" ) && herv.getEndPositionChromosome() >= ( geneEntry.getStart() - offset ) && herv.getStartPositionChromosome() <= geneEntry.getStart() ){
								filteredRange.get( offset ).add( herv );
							} else if ( geneEntry.strand.equals( "-" ) && herv.getStartPositionChromosome() <= ( geneEntry.getEnd() + offset ) && herv.getEndPositionChromosome() >= geneEntry.getEnd() ){
								filteredRange.get( offset ).add( herv );
							}
						}

					}

					//					if( range.equals( "+/-" ) ){
					//						filteredRange.add( herv );
					//					} else if( range.equals( "+" ) ){
					//						if( geneEntry.strand.equals( "+" ) && herv.getEndPositionChromosome() > geneEntry.end ){ 
					//							filteredRange.add( herv );
					//						} else if( geneEntry.strand.equals( "-" ) && herv.getStartPositionChromosome() < geneEntry.start ){
					//							filteredRange.add( herv );
					//						}
					//					} else if( range.equals( "-" ) ){
					//						if( geneEntry.strand.equals( "+" ) && herv.getStartPositionChromosome() < geneEntry.start ){ 
					//							filteredRange.add( herv );
					//						} else if( geneEntry.strand.equals( "-" ) && herv.getEndPositionChromosome() > geneEntry.end ){
					//							filteredRange.add( herv );
					//						}
					//					}
				}
			}
		}

		return filteredRange;
	}

	// ------------------------------------------------------------------------
	/** Defines the lower and upper range of the query HERV. */
	private static class HERVRangeDefinition{
		public GeneEntry geneEntry;
		public HERV lowerKey;
		public HERV upperKey;
		
		@Override
		public String toString() {
			return "HERVRangeDefinition [geneEntry=" + geneEntry + ", lowerKey=" + lowerKey + ", upperKey=" + upperKey
					+ "]";
		}
	}

	// ------------------------------------------------------------------------
	/** 
	 * Create the {@link HERVRangeDefinition} for the chromosome where gene is located,
	 * the start- and end-position of the query range.
	 * @param chromosome the chromosome
	 * @param rangeStart the start-position
	 * @param rangeEnd   the end-position
	 * @return
	 */
	private HERVRangeDefinition createHERVRangeDefinition( GeneEntry geneEntry, int chromosome, int rangeStart, int rangeEnd ){
		HERVRangeDefinition rangeDef = new HERVRangeDefinition();

		rangeDef.geneEntry = geneEntry;
		rangeDef.lowerKey = new HERV( (byte) chromosome, rangeStart, 0, (char) 0, (char) 0, (char) 0 );
		rangeDef.upperKey = new HERV( (byte) chromosome, rangeEnd, 0, (char) 0, (char) 0, (char) 0 );

		return rangeDef;
	}

	// ------------------------------------------------------------------------
	private static final Map<String, Integer> CHROMOSOME_MAP = new HashMap<>();
	static{
		CHROMOSOME_MAP.put( "chr1", 1 );
		CHROMOSOME_MAP.put( "chr2", 2 );
		CHROMOSOME_MAP.put( "chr3", 3 );
		CHROMOSOME_MAP.put( "chr4", 4 );
		CHROMOSOME_MAP.put( "chr5", 5 );
		CHROMOSOME_MAP.put( "chr6", 6 );
		CHROMOSOME_MAP.put( "chr7", 7 );
		CHROMOSOME_MAP.put( "chr8", 8 );
		CHROMOSOME_MAP.put( "chr9", 9 );
		CHROMOSOME_MAP.put( "chr10", 10 );
		CHROMOSOME_MAP.put( "chr11", 11 );
		CHROMOSOME_MAP.put( "chr12", 12 );
		CHROMOSOME_MAP.put( "chr13", 13 );
		CHROMOSOME_MAP.put( "chr14", 14 );
		CHROMOSOME_MAP.put( "chr15", 15 );
		CHROMOSOME_MAP.put( "chr16", 16 );
		CHROMOSOME_MAP.put( "chr17", 17 );
		CHROMOSOME_MAP.put( "chr18", 18 );
		CHROMOSOME_MAP.put( "chr19", 19 );
		CHROMOSOME_MAP.put( "chr20", 20 );
		CHROMOSOME_MAP.put( "chr21", 21 );
		CHROMOSOME_MAP.put( "chr22", 22 );
		CHROMOSOME_MAP.put( "chr23", 23 );
		CHROMOSOME_MAP.put( "chr24", 24 );
		CHROMOSOME_MAP.put( "chrX", 25 );
		CHROMOSOME_MAP.put( "chrY", 26 );

	}

	// ------------------------------------------------------------------------
	public Integer chromosomeToInt( String chromosome ){
		return CHROMOSOME_MAP.get( chromosome );
	}




}
