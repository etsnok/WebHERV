/**
 * 
 */
package org.kkruse.webherv.frontpage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.kkruse.webherv.frontpage.util.MessagesUtils;
import org.kkruse.webherv.settings.WebHervSettings.DrumsTableProbs;

/**
 * @author Konstantin
 *
 */
@ManagedBean( name="InputService" )
@ApplicationScoped
public class PropertiesInputService implements InputService {

	/** relative path to the configurations file */
	private static final String WEB_INF_PORTAL_CONFIG_PROPERTIES = "/WEB-INF/portal-config.properties";


	// parameter constants:
	//private static final String GENOME_IDS = "genome.ids";
	private static final String DRUMS_DB_IDS = "drums.databases.ids";
	private static final String DRUMS_DBS    = "drums.databases";
	private static final String DRUMS_DIRECTORY_PATH  = "drums.directory.path";
	
	private static final String DRUMS_DB_GENOME   = "genome";
	private static final String DRUMS_DB_DIR      = "dir";
	private static final String DRUMS_DB_ID       = "id";
	private static final String PLATFORM_DB_PROB  = "platform.exon_array.sqlite.db";
	private static final String USER_OFFSET_DEF   = "user.offset.list.default";
	private static final String USER_OFFSET_SEP   = "user.offset.list.separator";
	private static final String USER_OFFSET_REGEX = "user.offset.list.input_regex";
	private static final String USER_OFFSET_MIN   = "user.offset.list.min";
	private static final String USER_OFFSET_MAX   = "user.offset.list.max";
	private static final String USER_EVALUE_EXP_DEF  = "user.eval.exp.default";
	private static final String USER_EVALUE_EXP_MIN  = "user.eval.exp.min";
	private static final String USER_EVALUE_EXP_MAX  = "user.eval.exp.max";
	private static final String USER_HERV_LENGTH_DEF = "user.herv_length.default";
	private static final String USER_HERV_LENGTH_MIN = "user.herv_length.min";
	private static final String USER_HERV_LENGTH_MAX = "user.herv_length.max";
	private static final String BLAST_VERSION        = "blast.version";
	
	private static Configuration config;
	
	private Path drumsDatabasesFolder;
	private Map<String, DrumsTableProbs> drumTablePropertiesIdMap;
//	private List<String> databaseParameters = Arrays.asList( DRUMS_DATABASES_HERVS_HG19, DRUMS_DATABASES_HERVS_HG18 );
	private String platformDb;
	
	
	private List<SelectItem> variants;
	private List<SelectItem> ranges;
	private List<SelectItem> platforms;
	private List<SelectItem> genomes;

	private String defaultVariant;
	private String defaultRange;
	private String defaultPlatform;

	private Map<String, String> platformGenomes;
	
	
	public PropertiesInputService(){
		
		// read the properties file into the configurations
		try {
			config = initConfiguration();
			loadSettings( config );
		} catch (IOException e) {
			MessagesUtils.showFatalMsg( null , "Failed to init program configurations!");
		} catch (ConfigurationException e) {
			MessagesUtils.showFatalMsg( null , "Failed to init program configurations!");
		}
		
		platforms = new ArrayList<>();
		platforms.add( new SelectItem( "hg19_HuEx_1_0_st_v2_na32_probeset", "Hg19 Affy Human Exon 1.0v2" ) );
		platforms.add( new SelectItem( "hg18_HuEx_1_0_st_v2_na29_probeset", "Hg18 Affy Human Exon 1.0v2") );

		platformGenomes = new HashMap<>();
		platformGenomes.put( "hg19_HuEx_1_0_st_v2_na32_probeset" , "hervs_hg19");
		platformGenomes.put( "hg18_HuEx_1_0_st_v2_na29_probeset" , "hervs_hg18");

		variants = new ArrayList<>();
		variants.add( new SelectItem( "var2", "All Hervs inside the offset and range." ) );
		variants.add( new SelectItem( "var1", "Nearest Herv to gene center." ) );

		ranges = new ArrayList<>();
		ranges.add( new SelectItem( "+/-", "+/- (overlap)", "Range '+/-'  (overlap), the program will only look for sequences in front and behind the probeset." ) );
		ranges.add( new SelectItem( "+",  "+ (downstream)", "Range '+' (downstream), the program will only look for sequences behind the probeset.") ); 
		ranges.add( new SelectItem(  "-",  "- (upstream)", "Range '-' (upstream), the program will only look for sequences in front of the probeset." ) );

		defaultPlatform = (String) platforms.get(0).getValue();
		defaultVariant  = (String) variants.get(0).getValue();
		defaultRange    = (String) ranges.get(0).getValue();
	}

	
	// ------------------------------------------------------------------------
	/**
	 * loads the settings from the properties file.
	 * @throws IOException in case reading the properties fails.
	 * @throws ConfigurationException in case loading the configurations fails.
	 */
	private void loadSettings( Configuration config ) throws IOException, ConfigurationException{

		genomes = new ArrayList<>();
		
		// map that holds the drums db configs:
		drumTablePropertiesIdMap = new HashMap<>();
		
		String[] db_ids = config.getStringArray( DRUMS_DB_IDS );
		
		for( String id : db_ids ){
			System.out.println(id);

			DrumsTableProbs tableProbs = new DrumsTableProbs();
			tableProbs.id = id;
			Configuration databasesConf = config.subset( DRUMS_DBS+"."+id );
		
			Iterator<String> keys = databasesConf.getKeys();
			while( keys.hasNext() ){
				String key = keys.next();
				switch (key) {
				case DRUMS_DB_DIR:
					tableProbs.dir = databasesConf.getString( key );
					break;
				case DRUMS_DB_GENOME:
					tableProbs.hg = databasesConf.getString( key );
					SelectItem item = new SelectItem( tableProbs.hg,tableProbs.hg );
					if( ! genomes.contains(item) ){
						genomes.add( item );
					}
					break;
				default:
					break;
				}
			}
			drumTablePropertiesIdMap.put( tableProbs.id, tableProbs );
		}
		
		// ---------
		// read the drum database directory path:
		String drumDirName = config.getString( DRUMS_DIRECTORY_PATH );
		drumsDatabasesFolder = Paths.get( drumDirName );

		// ---------
		// read the platform/array database path:
		platformDb = config.getString( PLATFORM_DB_PROB, null );
		
	}

	//-------------------------------------------------------------------------
	/**
	 * Reads the configurations file.
	 * @return the read configurations
	 * @throws IOException in case the reading of the file into the properties fails.
	 * @throws ConfigurationException in case the creating the properties object fails.
	 */
	private Configuration initConfiguration() throws IOException, ConfigurationException{

		PropertiesConfiguration config = new BasicConfigurationBuilder<>(PropertiesConfiguration.class).configure(new Parameters().properties() ).getConfiguration();
		FileHandler fh = new FileHandler( config );
		if( FacesContext.getCurrentInstance() != null ){
			ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
			try( InputStream is = ec.getResourceAsStream( WEB_INF_PORTAL_CONFIG_PROPERTIES ) ){
				fh.load(is);	
			}
		} 
		return config;
	}
	
	//=========================================================================
	/** Container for drum table properties */
	public static class DrumsTableProbs{
		public String id;
		public String dir;
		public String hg;
	}

	
	// Getter -----------------------------------------------------------------	
	/* (non-Javadoc)
	 * @see org.kkruse.webherv.frontpage.InputService#getVariants()
	 */
	@Override
	public List<SelectItem> getVariants() {
		return variants;
	}

	/* (non-Javadoc)
	 * @see org.kkruse.webherv.frontpage.InputService#getRanges()
	 */
	@Override
	public List<SelectItem> getRanges() {
		return ranges;
	}

	/* (non-Javadoc)
	 * @see org.kkruse.webherv.frontpage.InputService#getPlatforms()
	 */
	@Override
	public List<SelectItem> getPlatforms() {
		return platforms;
	}

	/* (non-Javadoc)
	 * @see org.kkruse.webherv.frontpage.InputService#getGenomes()
	 */
	@Override
	public List<SelectItem> getGenomes() {
		return genomes;
	}

	/* (non-Javadoc)
	 * @see org.kkruse.webherv.frontpage.InputService#getPlatformGenomes()
	 */
	@Override
	public Map<String, String> getPlatformGenomes() {
		return platformGenomes;
	}

	/* (non-Javadoc)
	 * @see org.kkruse.webherv.frontpage.InputService#getDefaultRange()
	 */
	@Override
	public String getDefaultRange() {
		return defaultRange;
	}

	/* (non-Javadoc)
	 * @see org.kkruse.webherv.frontpage.InputService#getDefaultVariant()
	 */
	@Override
	public String getDefaultVariant() {
		return defaultVariant;
	}

	/* (non-Javadoc)
	 * @see org.kkruse.webherv.frontpage.InputService#setDefaultVariant(java.lang.String)
	 */
	@Override
	public void setDefaultVariant(String defaultVariant) {
		this.defaultVariant = defaultVariant;
	}

	/* (non-Javadoc)
	 * @see org.kkruse.webherv.frontpage.InputService#setDefaultRange(java.lang.String)
	 */
	@Override
	public void setDefaultRange(String defaultRange) {
		this.defaultRange = defaultRange;
	}

	/* (non-Javadoc)
	 * @see org.kkruse.webherv.frontpage.InputService#getDefaultPlatform()
	 */
	@Override
	public String getDefaultPlatform() {
		return defaultPlatform;
	}

	/* (non-Javadoc)
	 * @see org.kkruse.webherv.frontpage.InputService#setDefaultPlatform(java.lang.String)
	 */
	@Override
	public void setDefaultPlatform(String defaultPlatform) {
		this.defaultPlatform = defaultPlatform;
	}

	// Getter and Setter ------------------------------------------------------
	public Path getDrumsDatabasesFolder() {
		return drumsDatabasesFolder;
	}

	public Map<String, DrumsTableProbs> getDrumTablePropertiesIdMap() {
		return drumTablePropertiesIdMap;
	}

	public String getPlatformDb() {
		return platformDb;
	}
	
	public String getOffsetListDefault(){
		return config.getString( USER_OFFSET_DEF, "1000" );
	}

	public String getOffsetListSeparator(){
		return config.getString( USER_OFFSET_SEP );
	}

	public String getOffsetListRegex(){
		return config.getString( USER_OFFSET_REGEX );
	}

	public Integer getOffsetListMin(){
		return config.getInteger( USER_OFFSET_MIN, 0 );
	}
	
	public Integer getOffsetListMax(){
		return config.getInteger( USER_OFFSET_MAX, 1000000 );
	}
	
	public Integer getEvalueExpDefult(){
		return config.getInteger( USER_EVALUE_EXP_DEF, 1 );
	}

	public Integer getEvalueExpMin(){
		return config.getInteger( USER_EVALUE_EXP_MIN, 0 );
	}
	
	public Integer getEvalueExpMax(){
		return config.getInteger( USER_EVALUE_EXP_MAX, 1000 );
	}
	
	public Integer getHervLengthDef(){
		return config.getInteger( USER_HERV_LENGTH_DEF, 1 );
	}

	public Integer getHervLengthMin(){
		return config.getInteger( USER_HERV_LENGTH_MIN, 1 );
	}

	public Integer getHervLengthMax(){
		return config.getInteger( USER_HERV_LENGTH_MAX, null );
	}

	public String getBlastVersion(){
		return config.getString( BLAST_VERSION, "" );
	}
}
