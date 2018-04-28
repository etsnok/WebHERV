package org.kkruse.webherv.drums;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.unister.semweb.drums.api.DRUMSException;

/**
 * Creates a connection to a drums database folder containing
 * multiple drums databases. Each database must be in a separate
 * sub-folder. The sub-folder name will be the database id. 
 * With {@link #getDrumsDatabasesServices()} the map of {@link HervService}
 * can be accessed.
 * 
 * @author Konstantin Kruse
 */
public class DrumsConnector {

	private final Path drumsDatabasesFolder;
	private Map<String, HervService> drumsDatabasesServices;
	private Integer maxHervLength;
	
	private static final Logger LOG = Logger.getLogger( DrumsConnector.class.getName() );

	// ------------------------------------------------------------------------
	/**
	 * Constructor
	 * @param drumsDatabasesFolder folder that contains the drums databases as subfolders.
	 * @throws DRUMSException 
	 * @throws IOException 
	 */
	public DrumsConnector( Path drumsDatabasesFolder, Integer maxHervLength ) throws DRUMSException, IOException{
		this.drumsDatabasesFolder = drumsDatabasesFolder;
		this.maxHervLength = maxHervLength;
		
		drumsDatabasesServices = new HashMap<>();

		if( this.drumsDatabasesFolder != null ){
			loadDatabasesFromFolder();
		} else {
			throw new DRUMSException( "DrumsDatabaseFolder is null!" );
		}
	}

	
	// ------------------------------------------------------------------------
	/**
	 * loads the databases inside the {@link #drumsDatabasesFolder}.
	 * @throws DRUMSException 
	 * @throws IOException 
	 */
	private void loadDatabasesFromFolder( ) throws DRUMSException, IOException{

		if( LOG.isLoggable( Level.FINE ) ){
			LOG.fine( "Loading drums database folders..." );
		}

		try( DirectoryStream<Path> drumsFolders = Files.newDirectoryStream( this.drumsDatabasesFolder ) ){
			for( Path drums : drumsFolders ){

				if( LOG.isLoggable( Level.FINER ) ){
					LOG.finer( "Loading service from:" + drums.toString() );
				}

				if( Files.isDirectory( drums ) ){
					String dbName = drums.getFileName().toString();
					drumsDatabasesServices.put(dbName, new DrumsHervService( drums, maxHervLength ) );
					if( LOG.isLoggable( Level.FINER ) ){
						LOG.finer( "Added DrumsHervService:'" + dbName + "' from:'"+ drums.toString() +"'!" );
					}
				}
			}
		}
		
		if( LOG.isLoggable( Level.FINER ) ){
			LOG.finer( "Finished loading drums databases:"+ drumsDatabasesServices.keySet() +", from:" + this.drumsDatabasesFolder );
		}
	}

	// ------------------------------------------------------------------------
	/**
	 * Get the map of loaded {@link HervService}s. 
	 * The drums folder names are the map keys.
	 * @return the map of loaded drums databases.
	 */
	public Map<String, HervService> getDrumsDatabasesServices(){
		return drumsDatabasesServices;
	}
}
