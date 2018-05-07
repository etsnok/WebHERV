package org.kkruse.webherv.frontpage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.kkruse.webherv.drums.DrumsConnector;
import org.kkruse.webherv.drums.HervService;
import org.kkruse.webherv.drums.HervService.GeneEntryTables;
import org.kkruse.webherv.drums.HervService.HervInputSettings;
import org.kkruse.webherv.drums.HervService.HervServiceException;
import org.kkruse.webherv.frontpage.results.ResultsTab;
import org.kkruse.webherv.frontpage.results.ResultsView;
import org.kkruse.webherv.frontpage.results.ResultsTab.Gene;
import org.kkruse.webherv.frontpage.results.ResultsTab.OffsetHervHits;
import org.kkruse.webherv.frontpage.util.MessagesUtils;
import org.kkruse.webherv.frontpage.util.ResultsUtils;
import org.kkruse.webherv.genes.sqlite.GeneDBConnector.GeneEntry;
import org.kkruse.webherv.settings.WebHervSettings;
import org.kkruse.webherv.settings.WebHervSettings.DrumsTableProbs;
import org.kkruse.webherv.upload.FileUploader;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.UploadedFile;



import com.unister.semweb.biodrums.herv.HERV;
import com.unister.semweb.drums.api.DRUMSException;

/**
 * Controller for the HERV settings input page.
 * 
 * @author Konstantin Kruse
 */
@ManagedBean( name="inputController" )
@SessionScoped
public class InputController {

	private int uploadedFiles;
	private int finishedFiles;
	private int genesInCurrentFile;
	private int finishedGenes;
	
	private GeneEntryTables tables;
	
	private DrumsConnector drumsConnector;

	@ManagedProperty( value="#{fileUploader}" )
	private FileUploader fileUploader;

	@ManagedProperty( value="#{userInput}" )
	private UserInput userInput;

	@ManagedProperty( value="#{resultsView}" )
	private ResultsView resultsView;

	@ManagedProperty( value="#{webHervSettings}" )
	private WebHervSettings webHervSettings;

//	@ManagedProperty( value="#{backendStatusView}" )
//	private BackendStatusView backendStatusView;

	private static final Logger LOG = Logger.getLogger( InputController.class.getName() );

	// INIT ===================================================================
	public InputController(){}

	@PostConstruct
	public void init(){
		
		setUploadedFiles(0);
		setFinishedFiles(0);
		setGenesInCurrentFile(0);
		setFinishedGenes(0);
		
		try {
			initDbConnectors();
		} catch (DRUMSException | IOException e ) {
			LOG.log( Level.SEVERE, "Failed to init Drums Database connection.",  e );
			MessagesUtils.showFatalMsg(null, "Failed to init Drums Database connection:" + e.getLocalizedMessage());
			RequestContext requestContext = RequestContext.getCurrentInstance();
			requestContext.execute("PF('bui').show()");

			FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Error", "Failed to init Drums Database connection." );
			RequestContext.getCurrentInstance().showMessageInDialog(message);
		}
	}

	// ------------------------------------------------------------------------
	private void initDbConnectors() throws DRUMSException, IOException{
		FacesContext ctx = FacesContext.getCurrentInstance();
		String myConstantValue = ctx.getExternalContext().getInitParameter("drumsDatabasesDir");

		Path databasesPath = Paths.get( myConstantValue );
		drumsConnector = new DrumsConnector( databasesPath, webHervSettings.getHervLengthMax() );
	}

	// Setter =================================================================
	public void setUserInput( UserInput userInput ){
		this.userInput = userInput;
	}

	public void setFileUploader( FileUploader fileUploader ){
		this.fileUploader = fileUploader;
	}

	public void setResultsView(ResultsView resultsView) {
		this.resultsView = resultsView;
	}
	
	public void setWebHervSettings(WebHervSettings webHervSettings) {
		this.webHervSettings = webHervSettings;
	}

	// FILE UPLOAD ============================================================
	/**
	 * This method gets called when a {@link FileUploadEvent} was fired
	 * from the page. Each event is one uploaded gene file. The
	 * uploads must be sequential in order to work proper.
	 * @param event the {@link FileUploadEvent}
	 */
	public void handleFileUpload( FileUploadEvent event ) {

		if( LOG.isLoggable( Level.FINE ) ){
			LOG.fine( "handleFileUpload:" + event );
		}

		// the name of the file:
		String fileName = event.getFile().getFileName();
		// the uploaded file:
		UploadedFile uploadedFile = event.getFile();

		// add the file and with it's name to the gene list:
		fileUploader.addToGeneList( fileName, uploadedFile );	

		userInput.setSelectedByGenome(false);
		
		FacesMessage message = new FacesMessage("Succesful", fileName + " is uploaded.");
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	// ------------------------------------------------------------------------
	/**
	 * This method gets called after the files were uploaded and 
	 * and the program should query the overlapping hervs.
	 * @return page name to navigate to after submitting.
	 */
	public String submitUpload(){

		if( drumsConnector == null ){
			LOG.log(Level.WARNING, "DrumsConnectior is null!" );
			MessagesUtils.showErrorMsg(null, "DrumsConnectior is null!" );
			return "";
		}

		if( fileUploader == null ){
			LOG.log(Level.WARNING, "FileUploader is null!" );
			MessagesUtils.showErrorMsg(null, "FileUploader is null!" );
			return "";
		}

		if( LOG.isLoggable(Level.FINE) ){LOG.fine( "SubmitingUpload" );	}
		
		HervInputSettings userSettings = userInput.currentHervInputSettings();
		// Load the data for the gene lists:
//		GeneEntryTables tables;
		if( tables == null ){
			try {
				tables = fileUploader.loadGenesLists( userSettings );
			} catch (Exception e1) {
				LOG.log(Level.WARNING, "Exception while loading gene lists:", e1 );
				MessagesUtils.showErrorMsg(null, "Exception while loading gene lists:" + e1.getLocalizedMessage()  );
				return "";
			}
		}
		setUploadedFiles( tables.getGeneEntryTables().size() );
		
		Map<String, HervService> services = drumsConnector.getDrumsDatabasesServices();
		Map<String, Map<GeneEntry, Map<Integer, List<HERV>>>> genesFileIdHervsMap = new HashMap<>(); 
		
		DrumsTableProbs table = webHervSettings.getDrumTablePropertiesIdMap().get( userSettings.selectedGenome );
		String genomeDir = null;
		if( table != null ){
			genomeDir = table.dir;
		} else{
			LOG.severe( "Failed to get drums table dir for selected genome:" + userSettings.selectedGenome ); 
			MessagesUtils.showErrorMsg(null, "Failed to get drums table dir for selected genome:" + userSettings.selectedGenome );
			return "";
		}
		
		// get the HervService for the selected genome:
		HervService hervService = services.get( genomeDir );
		if( hervService != null ){
			try {
				hervService.openConnection();
				int count = 0;
				for( String fileName : tables.getGeneEntryTables().keySet() ){
					genesFileIdHervsMap.put(fileName, hervService.selectHervsInRange( userInput.currentHervInputSettings(), fileName, tables.getGeneEntryTables() ) );
					if( LOG.isLoggable(Level.FINE) ){LOG.fine("Finished uploaded file:" + fileName);}
					setFinishedFiles( ++count );
				}

			} catch (IOException | HervServiceException e) {
				LOG.log(Level.WARNING, "Failed to open connection!", e );
				MessagesUtils.showFatalMsg(null, "Failed to open connection to database:" + e.getLocalizedMessage() );
			} finally{
				try {
					hervService.close();
				} catch ( IOException e ) {
					LOG.log( Level.WARNING, "Failed to clode drum connection!", e );
				}
			}
		} else {
			LOG.warning( "No drum database was fond for genome:" + userInput.currentHervInputSettings().selectedGenome );
			MessagesUtils.showErrorMsg(null, "No database found for:" + userInput.currentHervInputSettings().selectedGenome );
		}

		// the resulttabs that are used to show the results page:
		List<ResultsTab> resultsTabs = createResultsTabs( genesFileIdHervsMap );
		resultsView.setResultsTabs( resultsTabs );

		// reset the gene list after submitting
		fileUploader.initNewGeneLists();

		return "results?faces-redirect=true"; // navigate to results page
	}


	// ------------------------------------------------------------------------
	/**
	 * @param genesFileIdHervsMap
	 * @return
	 */
	private List<ResultsTab> createResultsTabs( Map<String, Map<GeneEntry, Map<Integer, List<HERV>>>> genesFileIdHervsMap ){
		List<ResultsTab> resultsTabs = new ArrayList<>();

		for( String genesFileName : genesFileIdHervsMap.keySet() ){

			Map<GeneEntry, Map<Integer, List<HERV>>> geneHervs = genesFileIdHervsMap.get( genesFileName );

			List<Gene> genes = null;
			if( geneHervs != null ){
				genes = createListOfGenes( geneHervs );
			}

			ResultsTab resultsTab = new ResultsTab( genesFileName, genesFileName, genesFileName, genes );

			resultsTabs.add( resultsTab );
		}

		return resultsTabs;
	}

	// ------------------------------------------------------------------------
	/**
	 * @param geneHervs
	 * @return
	 */
	private List<Gene> createListOfGenes( Map<GeneEntry, Map<Integer, List<HERV>>> geneHervs ){

		List<Gene> genes = new ArrayList<>();

		for( GeneEntry geneEntry : geneHervs.keySet() ){
			Map<Integer, List<HERV>> hervs = geneHervs.get( geneEntry );
			Map<String, OffsetHervHits> hervHits = null;
			if( hervs != null && hervs.size() > 0 ){
				hervHits = ResultsUtils.toOffsetHervHits( hervs );
			}
			List<Integer> offsets = null;
			if( hervs != null ){
				offsets = new ArrayList<>( hervs.keySet() );
			}
			
			String id = geneEntry.probeSet;
			if( id == null ){
				id = geneEntry.chromosome+":"+geneEntry.start+":"+geneEntry.end;
			}
			
			Gene gene = new Gene(id, geneEntry, hervHits, offsets );
			genes.add( gene );
		}
		return genes;
	}

	
	
	public void handleCoordinatesFileUpload( FileUploadEvent event ) {

		if( LOG.isLoggable( Level.FINE ) ){
			LOG.fine( "handleCoordinatesFileUpload:" + event );
		}

		// the name of the file:
		String fileName = event.getFile().getFileName();
		// the uploaded file:
		UploadedFile uploadedFile = event.getFile();

		// add the file and with it's name to the gene list:
		fileUploader.addToGeneList( fileName, uploadedFile );	

		HervInputSettings userSettings = userInput.currentHervInputSettings();

		if( tables == null ){
			tables = new GeneEntryTables(userSettings.selectedGenome);
		} 
		
		
		try {
			fileUploader.convertGenesLists( tables, userSettings);
//			fileUploader.getUploadedGeneLists().remove(fileName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		userInput.setSelectedByGenome(true);
		
//		GeneEntry
//		FacesMessage message = new FacesMessage("Succesful", fileName + " is uploaded.");
//		FacesContext.getCurrentInstance().addMessage(null, message);
//		
		
	}
	
	public void deleteTableByName( String name ){
		if( tables != null && tables.getGeneEntryTables() != null
				&& tables.getGeneEntryTables().containsKey(name) ){
			tables.getGeneEntryTables().remove(name);
			fileUploader.getUploadedGeneLists().remove(name);
		}
	}
	
	public void onTabChange(TabChangeEvent event) {
		if( tables != null && tables.getGeneEntryTables() != null && tables.getGeneEntryTables().size() > 0 ){
			tables.getGeneEntryTables().clear();// = null;
		}
		fileUploader.initNewGeneLists();
	}
	
	
	// === Getter and Setter =========================================================
	public int getUploadedFiles() {
		return uploadedFiles;
	}

	public void setUploadedFiles(int uploadedFiles) {
		this.uploadedFiles = uploadedFiles;
	}

	public int getFinishedFiles() {
		return finishedFiles;
	}

	public void setFinishedFiles(int finishedFiles) {
		this.finishedFiles = finishedFiles;
	}

	public int getGenesInCurrentFile() {
		return genesInCurrentFile;
	}

	public void setGenesInCurrentFile(int genesInCurrentFile) {
		this.genesInCurrentFile = genesInCurrentFile;
	}

	public int getFinishedGenes() {
		return finishedGenes;
	}

	public void setFinishedGenes(int finishedGenes) {
		this.finishedGenes = finishedGenes;
	}

	public GeneEntryTables getTables() {
		return tables;
	}

	public void setTables(GeneEntryTables tables) {
		this.tables = tables;
	}
}
