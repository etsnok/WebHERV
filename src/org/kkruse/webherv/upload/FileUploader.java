package org.kkruse.webherv.upload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.security.auth.login.FailedLoginException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.kkruse.webherv.drums.HervService.GeneEntryTables;
import org.kkruse.webherv.drums.HervService.HervInputSettings;
import org.kkruse.webherv.frontpage.util.MessagesUtils;
import org.kkruse.webherv.genes.sqlite.GeneDBConnector;
import org.kkruse.webherv.genes.sqlite.GeneDBConnector.GeneEntry;
import org.kkruse.webherv.genes.sqlite.GeneDBConnector.Strand;
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

	private static final Pattern STRAND_RGX = Pattern.compile( "(-|\\+|1|-1|\\+1)" );

	private static final String CSV_DELIMITER_RGX = "\t|,|;";
//	private static final Pattern CHROMOSOME_RGX = Pattern.compile( "[cC][hH][rR][ -_]?(1[0-9]?|2[0-2]?|[xX]|[yY])" );

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

		if( uploadedGeneLists == null ){
			uploadedGeneLists = new HashMap<>();
		} else if( uploadedGeneLists.containsKey( fileName ) ){
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

		List<String[]> geneTable = null;
		try{
			geneTable = readUploadedGeneList( _file.getInputstream() );
		} catch (IOException e) {
			String fileName =  _file!=null?_file.getFileName():null;
			LOG.log(Level.WARNING,  "Failed to read file:" + fileName, e);
		}
		
		return geneTable;
//		List<String[]> geneTable = new LinkedList<String[]>();
//		try (BufferedReader br = new BufferedReader( new InputStreamReader( _file.getInputstream(), "UTF-8" ) );){
//			String line;
//			while( ( line = br.readLine() ) != null ){
//				geneTable.add( line.split( CSV_DELIMITER_RGX ) );
//			}
//
//		} catch (IOException e) {
//			String fileName =  _file!=null?_file.getFileName():null;
//			LOG.log(Level.WARNING,  "Failed to read file:" + fileName, e);
//		}
//		return geneTable;
	}

	public List<String[]> readUploadedGeneList( InputStream  _is ){

		List<String[]> geneTable = new LinkedList<String[]>();
		try (BufferedReader br = new BufferedReader( new InputStreamReader( _is, "UTF-8" ) );){
			String line;
			while( ( line = br.readLine() ) != null ){
				geneTable.add( line.split( CSV_DELIMITER_RGX ) );
			}

		} catch (IOException e) {
			LOG.log(Level.WARNING,  "Failed to read inputstream:", e);
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
					if(gene != null && gene.length>0){						
						geneIDs.add( gene[0].replace( "\"", "" ) );
					}
				}
				String tableName = webHervSettings.getPlatformTable().get(geneTableName );
				Map<String, GeneEntry> geneEntriesMap = geneDBConnector.queryGenesById( tableName, geneIDs );
				
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

	
	public void convertGenesLists( GeneEntryTables tables, HervInputSettings hervInputSettings ) throws SQLException, Exception{

		if( uploadedGeneLists == null || uploadedGeneLists.size() == 0 ){
			LOG.warning( "No gene lists to load!" );
			MessagesUtils.showWarnMsg( "fileUploadMsg" , "No files to upload!");
		}

//		String geneTableName = hervInputSettings.selectedPlatform;
		
		FacesContext context = FacesContext.getCurrentInstance();
		
		for( String geneListName : uploadedGeneLists.keySet() ){
			
			List<String[]> geneList = uploadedGeneLists.get( geneListName );
			List<GeneEntry> genes = new ArrayList<>();
			
			Map<String[],Exception> failedGeneList = new LinkedHashMap<>();
			
			int countFailed = 0; // number all failed line
			int lineIdx = 1;
			for( String[] gene : geneList ){
				if( gene != null && gene.length >= 4 && !gene[0].isEmpty() ){
					GeneEntry entry = new GeneEntry();
					entry.setChromosome(gene[0]);
					try{
						int st = Integer.parseInt(gene[1]);
						if( st >= 0 ){
							entry.setStart( st );
						} else{ throw new NumberFormatException("Start must be >= 0 :"+ st); } 
						int en = Integer.parseInt(gene[2]);
						if( en >= 0 ){
							entry.setEnd(en);
						} else{ throw new NumberFormatException("End must be >= 0 :"+ st); }

						if( STRAND_RGX.matcher( gene[3] ).matches() ){
							entry.setStrand(Strand.getStrand(gene[3]));
						} else { throw new InputMismatchException( "Strand must be one of (+,-,1,+1,-1) not:"+ gene[3] ); }
						
						if( st < en ){
							genes.add(entry);
						} else {  throw new InputMismatchException("Start position ("+st+") must be smaller than end ("+en+")"); }
						
					} catch ( Exception e){
						failedGeneList.put(gene, e);
						countFailed++;
					}
				} else {
					countFailed++;
					if( LOG.isLoggable( Level.FINE ) ){
						LOG.fine("Unable to parse uploaded gene entry line#:"+lineIdx+" file:'"+geneListName+"' '"+ StringUtils.join(gene, " ") + "'.");
					}
//				    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, geneListName,  "Error reading line:"+lineIdx ) );
				}
				lineIdx++;
			}
			tables.addGeneEntryTables(geneListName, genes);
			
		    
		    if( failedGeneList != null && failedGeneList.size() > 0 ){

		    	String failsMsgStr = "";
		    	for( String[] fg : failedGeneList.keySet() ){
		    		Exception e = failedGeneList.containsKey(fg)?failedGeneList.get(fg):null;
		    		
		    		failsMsgStr += StringUtils.join(fg, " ")+(e!=null?e.getMessage():"")+"\n";
		    	}
		    	
		    	context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, geneListName,  "Read :"+(lineIdx-countFailed)+" lines skipped:"+countFailed + "\n" +failsMsgStr ) );
		    	
		    }
		    
		}
		
		uploadedGeneLists.clear();
		
		
//		if( ex != null ){
//			LOG.warning( "Exception parsing genome coordinates:" + ex.getLocalizedMessage() );
//			MessagesUtils.showWarnMsg( "fileUploadMsg" , "Exception parsing genome coordinates!");
//		}
		
//		return tables;
	}	
	
	
	
	
}

