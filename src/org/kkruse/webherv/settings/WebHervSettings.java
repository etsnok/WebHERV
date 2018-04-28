/**
 * 
 */
package org.kkruse.webherv.settings;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.kkruse.webherv.frontpage.util.MessagesUtils;

/**
 * Class that reads the WebHERV configurations from a properties file.
 * 
 * @author Konstantin Kruse
 */
@ManagedBean( name="webHervSettings" )
@ApplicationScoped
public class WebHervSettings {

	/** relative path to the configurations file */
	private static final String WEB_INF_PORTAL_CONFIG_PROPERTIES = "/WEB-INF/portal-config.properties";

	// parameter constants:
	private static final String DRUMS_DIRECTORY_PATH       = "drums.directory.path";
	private static final String DRUMS_DATABASES_HERVS_HG18 = "drums.databases.hervs.hg18";
	private static final String DRUMS_DATABASES_HERVS_HG19 = "drums.databases.hervs.hg19";
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
	private List<String> databaseParameters = Arrays.asList( DRUMS_DATABASES_HERVS_HG19, DRUMS_DATABASES_HERVS_HG18 );
	private String platformDb;
	
	/**
	 *  constructor 
	 */
	public WebHervSettings(){}
	
	@PostConstruct
	public void init(){
		// read the properties file into the configurations
		try {
			config = initConfiguration();
			loadSettings( config );
		} catch (IOException e) {
			MessagesUtils.showFatalMsg( null , "Failed to init program configurations!");
		} catch (ConfigurationException e) {
			MessagesUtils.showFatalMsg( null , "Failed to init program configurations!");
		}
		
	}

//	// ------------------------------------------------------------------------
//	/**
//	 * static access to singleton object instance.
//	 * @return the web herv settings instance.
//	 * @throws IOException in case reading the properties fails.
//	 * @throws ConfigurationException in case loading the configurations fails.
//	 */
//	public static WebHervSettings getInstance() throws IOException, ConfigurationException{
//		if( webHervSettings == null ){
//			webHervSettings = new WebHervSettings();
//		}
//		return webHervSettings;
//	}

	// ------------------------------------------------------------------------
	/**
	 * loads the settings from the properties file.
	 * @throws IOException in case reading the properties fails.
	 * @throws ConfigurationException in case loading the configurations fails.
	 */
	private void loadSettings( Configuration config ) throws IOException, ConfigurationException{

		// map that holds the drums db configs:
		drumTablePropertiesIdMap = new HashMap<>();
		
		// read the drums db configurations:
		for( String genome : databaseParameters ){

			DrumsTableProbs tableProbs = new DrumsTableProbs();
			Configuration databasesConf = config.subset( genome );
			
			Iterator<String> keys = databasesConf.getKeys();
			while( keys.hasNext() ){
				String key = keys.next();
				switch (key) {
				case DRUMS_DB_ID: 
					tableProbs.id = databasesConf.getString( key );
					break;
				case DRUMS_DB_DIR:
					tableProbs.dir = databasesConf.getString( key );
					break;
				case DRUMS_DB_GENOME:
					tableProbs.hg = databasesConf.getString( key );
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
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		try( InputStream is = ec.getResourceAsStream( WEB_INF_PORTAL_CONFIG_PROPERTIES ) ){
			fh.load(is);	
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
