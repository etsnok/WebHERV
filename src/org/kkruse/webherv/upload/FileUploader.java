package org.kkruse.webherv.upload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.kkruse.webherv.drums.HervService.GeneEntryTables;
import org.kkruse.webherv.drums.HervService.HervInputSettings;
import org.kkruse.webherv.frontpage.util.MessagesUtils;
import org.kkruse.webherv.genes.sqlite.GeneDBConnector;
import org.kkruse.webherv.genes.sqlite.GeneDBConnector.GeneEntry;
import org.kkruse.webherv.genes.sqlite.SqliteHg19DbConnector;
import org.kkruse.webherv.settings.WebHervSettings;
import org.primefaces.model.UploadedFile;

/**
 * @author Konstantin Kruse
 *
 */
@ManagedBean( name="fileUploader" )
@SessionScoped
public class FileUploader {

	private static final String CSV_DELIMITER_RGX = "\t|,|;";

	private GeneDBConnector geneDBConnector;
	private boolean isDbConnectionOpen;
	
	private Map<String, List<String[]>> uploadedGeneLists;

	@ManagedProperty( value="#{webHervSettings}" )
	private WebHervSettings webHervSettings;
	
	private static final Logger LOG = Logger.getLogger( FileUploader.class.getName() );

	//=========================================================================
	@PostConstruct
	public void init(){
		try {
			geneDBConnector = new SqliteHg19DbConnector( webHervSettings.getPlatformDb() );
		} catch ( ClassNotFoundException | SQLException e) {
			LOG.log( Level.SEVERE, "Failed to init Sqlite Db!", e );
			MessagesUtils.showFatalMsg(null, "Failed to init platform database!");
		}
		isDbConnectionOpen = false;
		initNewGeneLists();
	}
	
	//=========================================================================
	public void setWebHervSettings(WebHervSettings webHervSettings) {
		this.webHervSettings = webHervSettings;
	}
	//=========================================================================
	
	public void initNewGeneLists(){
		LOG.info( "Init new UploadedGenesList!" );
		uploadedGeneLists = new HashMap<>();
	}

	public boolean isDbConnectionOpen() {
		return isDbConnectionOpen;
	}


	/**
	 * Reads the {@link UploadedFile} and adds the content to the 
	 * uploaded gene list but only if not a simillar named file already exists.
	 * @param fileName the name if the uploaded file.
	 * @param file the uploaded file.
	 */
	public void addToGeneList( String fileName,  UploadedFile file ){

		if( uploadedGeneLists.containsKey( fileName ) ){
			// file with same name exists already
			MessagesUtils.showWarnMsg( null,  "A file with name:'"+fileName+"' was already uploaded." );
			return;
		}

		List<String[]> geneList = readUploadedGeneList( file );
		uploadedGeneLists.put( fileName, geneList );

	}


	public void addToGeneList( String listName, List<String[]> geneList ){
		uploadedGeneLists.put( listName, geneList );
	}

	public Map<String, List<String[]>> getUploadedGeneLists() {
		return uploadedGeneLists;
	}

	public void setUploadedGeneLists(Map<String, List<String[]>> uploadedGeneLists) {
		this.uploadedGeneLists = uploadedGeneLists;
	}

	public List<String[]> readUploadedGeneList( UploadedFile _file ){

		List<String[]> geneTable = new LinkedList<String[]>();
		try (BufferedReader br = new BufferedReader( new InputStreamReader( _file.getInputstream(), "UTF-8" ) );){
			String line;
			while( ( line = br.readLine() ) != null ){
				geneTable.add( line.split( CSV_DELIMITER_RGX ) );
			}

		} catch (IOException e) {
			String fileName =  _file!=null?_file.getFileName():null;
			LOG.log(Level.WARNING,  "Failed to read file:" + fileName, e);
		}
		return geneTable;
	}

	
	
	public GeneEntryTables loadGenesLists( HervInputSettings hervInputSettings ) throws SQLException, Exception{

		if( uploadedGeneLists == null || uploadedGeneLists.size() == 0 ){
			LOG.warning( "No gene lists to load!" );
			MessagesUtils.showWarnMsg( "fileUploadMsg" , "No files to upload!");
		}

		String geneTableName = hervInputSettings.selectedPlatform;
		GeneEntryTables tables = new GeneEntryTables( geneTableName );

		try( Connection connection = geneDBConnector.connectDb() ){
			isDbConnectionOpen = true;
			
			LOG.info( "Loading Gene Lists..." );

			for( String geneListName : uploadedGeneLists.keySet() ){

				List<String[]> geneList = uploadedGeneLists.get( geneListName );
				List<String> geneIDs = new ArrayList<>();

				for( String[] gene : geneList ){
					geneIDs.add( gene[0].replace( "\"", "" ) );
				}

				Map<String, GeneEntry> geneEntriesMap = geneDBConnector.queryGenesById( geneTableName, geneIDs );
				
				// add probe-sets the were not found in the DB:
				for( String id : geneIDs ){
					if( !geneEntriesMap.containsKey(id) ){
						 GeneEntry e = new GeneEntry();
						 e.setProbeSet( id );
						geneEntriesMap.put(id, e );
					}
				}
				
				// convert the map to a list:
				List<GeneEntry> geneEntries = new ArrayList<>();
				for( String key : geneEntriesMap.keySet() ){
					geneEntries.add( geneEntriesMap.get(key) );
				}
				
				tables.addGeneEntryTables(geneListName, geneEntries );
			}

			isDbConnectionOpen = false;
		}
		
		return tables;
	}

	
	public GeneEntryTables convertGenesLists( HervInputSettings hervInputSettings ) throws SQLException, Exception{

		if( uploadedGeneLists == null || uploadedGeneLists.size() == 0 ){
			LOG.warning( "No gene lists to load!" );
			MessagesUtils.showWarnMsg( "fileUploadMsg" , "No files to upload!");
		}

		String geneTableName = hervInputSettings.selectedPlatform;
		GeneEntryTables tables = new GeneEntryTables( geneTableName );
		
		Exception ex = null;
		
		for( String geneListName : uploadedGeneLists.keySet() ){
			
			List<String[]> geneList = uploadedGeneLists.get( geneListName );
			List<GeneEntry> genes = new ArrayList<>();
			
			for( String[] gene : geneList ){
				GeneEntry entry = new GeneEntry();
				entry.setChromosome(gene[0]);
				try{
					entry.setStart(Integer.parseInt(gene[1]));
					entry.setEnd(Integer.parseInt(gene[2]));
					entry.setStrand(gene[3]);
					genes.add(entry);
				} catch ( Exception e){
					ex = e;
				}
			}
			tables.addGeneEntryTables(geneListName, genes);
		}
		
		if( ex != null ){
			LOG.warning( "Exception parsing genome coordinates:" + ex.getLocalizedMessage() );
			MessagesUtils.showWarnMsg( "fileUploadMsg" , "Exception parsing genome coordinates!");
		}
		
		return tables;
	}	
	
	
	
	
}

