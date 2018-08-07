package org.kkruse.webherv.frontpage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

import org.kkruse.webherv.drums.HervService.HervInputSettings;
import org.kkruse.webherv.settings.WebHervSettings;

@ManagedBean( name="userInput" )
@SessionScoped
public class UserInput {

	private static final Logger LOG = Logger.getLogger( UserInput.class.getName() );

//	private InputService inputService;

	@ManagedProperty( value="#{webHervSettings}" )
	private WebHervSettings webHervSettings;

	// Fields
	private String  offset;	
	private String  offsetListRegex;
	private String  offsetListSep;
	private String  selectedRange;
	private String  selectedPlatform;
	private String  selectedDrums;
	private boolean  selectedByGenome;
	private Boolean  uploadByGenomeCoordinates;
	private String  selectedVariant;
	private Integer minimalLength;
	private Integer minimalLengthMin;
	private Integer minimalLengthMax;
	private Integer maxEvalueExp;
	private Integer maxEvalueExpMin;
	private Integer maxEvalueExpMax;

	@PostConstruct
	public void init(){

		//inputService     = new InputServiceImpl();
//		inputService     = new PropertiesInputService();
		selectedRange    = webHervSettings.getDefaultRange();
		selectedPlatform = webHervSettings.getDefaultPlatform();
		selectedDrums    = webHervSettings.getPlatformDrums().get(selectedPlatform);
		selectedVariant  = webHervSettings.getDefaultVariant();

		offset          = webHervSettings.getOffsetListDefault();
		offsetListRegex = webHervSettings.getOffsetListRegex();
		offsetListSep   = webHervSettings.getOffsetListSeparator();

		minimalLength    = webHervSettings.getHervLengthDef();
		minimalLengthMin = webHervSettings.getHervLengthMin();
		minimalLengthMax = webHervSettings.getHervLengthMax();
		maxEvalueExp    = webHervSettings.getEvalueExpDefult();
		maxEvalueExpMin = webHervSettings.getEvalueExpMin();
		maxEvalueExpMax = webHervSettings.getEvalueExpMax();

		selectedByGenome = false;
		uploadByGenomeCoordinates = null;
	}

	//=========================================================================
	public void setWebHervSettings(WebHervSettings webHervSettings) {
		this.webHervSettings = webHervSettings;
	}
	//=========================================================================

	public HervInputSettings currentHervInputSettings(){

		return new HervInputSettings( 
				toOffsetList( getOffset() ),
				getSelectedRange(),
				getSelectedPlatform(),
				getSelectedDrums(),
				getSelectedVariant(), 
				getMinimalLength(),
				toMaxEvalue( getMaxEvalueExp() ) );
	}

	private List<Integer> toOffsetList( String offsetsStr ){

		offsetsStr = offsetsStr.replaceAll(" ", "");
		String[] offsetArr = offsetsStr.split( offsetListSep );

		List<Integer> offsetList = new ArrayList<>();
		for( String offsetStr : offsetArr ){
			try{
				offsetList.add( Integer.parseInt( offsetStr ) );
			} catch( NumberFormatException e ){
				LOG.warning( "Failed to parse int from:'" + offsetStr + "'." );
			}
		}
		Collections.sort( offsetList );
		return offsetList;
	}

	private Double toMaxEvalue( int maxEvalExp ){
		return 1 * Math.pow(10, -maxEvalExp);
	}

	
	public void logTest(){
		if(LOG.isLoggable(Level.FINEST)) LOG.finest("LOG TEST!");
		if(LOG.isLoggable(Level.FINE)) LOG.fine("LOG TEST!");
		if(LOG.isLoggable(Level.INFO)) LOG.info("LOG TEST!");
		if(LOG.isLoggable(Level.WARNING)) LOG.warning("LOG TEST!");
		System.out.println("LOG TEST!");
	}
	
	
	// Lists Getter -----------------------------------------------------------
	public List<SelectItem> getRanges(){
		if(LOG.isLoggable(Level.FINE)) LOG.fine("GET ranges:" + webHervSettings.getRanges() );
		return webHervSettings.getRanges();
	}

	public List<SelectItem> getPlatforms(){
		if(LOG.isLoggable(Level.FINE)) LOG.fine("GET platformaes:" + webHervSettings.getPlatforms() );
		return webHervSettings.getPlatforms();
	}

	public List<SelectItem> getDrumsDirs(){
		if(LOG.isLoggable(Level.FINE)) LOG.fine("GET drumDirs:" + webHervSettings.getDrumsDirs() );
		return webHervSettings.getDrumsDirs();
	}

	public List<SelectItem> getVariants(){
		if(LOG.isLoggable(Level.FINE)) LOG.fine("GET variants:" + webHervSettings.getVariants() );		
		return webHervSettings.getVariants();
	}	

	// Selected Values Getter and Setter --------------------------------------
	public String getOffset() {
		if(LOG.isLoggable(Level.FINE)) LOG.fine("GET offset:" + offset );				
		return offset;
	}

	public void setOffset(String offset) {
		if(LOG.isLoggable(Level.FINE)) LOG.fine("SET offset:" + offset );				
		this.offset = offset;
	}


	public String getSelectedRange() {
		if(LOG.isLoggable(Level.FINE)) LOG.fine("GET selectedRange:" + selectedRange );				
		return selectedRange;
	}

	public void setSelectedRange(String selectedRange) {
		if(LOG.isLoggable(Level.FINE)) LOG.fine("SET selectedRange:" + selectedRange );				
		this.selectedRange = selectedRange;
	}

	public String getSelectedPlatform() {
		if(LOG.isLoggable(Level.FINE)) LOG.fine("GET selectedPlatform:" + selectedPlatform);	
		return selectedPlatform;
	}

	public void setSelectedPlatform(String selectedPlatform) {
		if(LOG.isLoggable(Level.FINE)) LOG.fine("SET selectedPlatform:" + selectedPlatform);
		this.selectedPlatform = selectedPlatform;
		//this.selectedDrums = webHervSettings.getPlatformDrums().get( this.selectedPlatform );
	}

	public String getSelectedDrums() {
		if(LOG.isLoggable(Level.FINE)) LOG.fine("GET selectedDrums:" + selectedDrums);		
		return selectedDrums;
	}

	public void setSelectedDrums(String selectedDrums) {
		if(LOG.isLoggable(Level.FINE)) LOG.fine("SET selectedDrums:" + selectedDrums);	
		this.selectedDrums = selectedDrums;
	}

	public boolean isSelectedByGenome() {
		return selectedByGenome;
	}

	public void setSelectedByGenome(boolean selectedByGenome) {
		this.selectedByGenome = selectedByGenome;
	}

	public boolean isUploadByGenomeCoordinates() {
		return uploadByGenomeCoordinates;
	}

	public void setUploadByGenomeCoordinates(boolean uploadByGenomeCoordinates) {
		this.uploadByGenomeCoordinates = uploadByGenomeCoordinates;
	}

	public String getSelectedVariant() {
		return selectedVariant;
	}

	public void setSelectedVariant(String selectedVariant) {
		this.selectedVariant = selectedVariant;
	}

	public Integer getMinimalLength() {
		if(LOG.isLoggable(Level.FINE)) LOG.fine("GET minimalLength:" + minimalLength );				
		return minimalLength;
	}

	public void setMinimalLength(Integer minimalLength) {
		if(LOG.isLoggable(Level.FINE)) LOG.fine("SET minimalLength:" + minimalLength );				
		this.minimalLength = minimalLength;
	}

	public Integer getMinimalLengthMin() {
		return minimalLengthMin;
	}
	public Integer getMinimalLengthMax() {
		return minimalLengthMax;
	}


	// Evalue ------------------------
	public Integer getMaxEvalueExp() {
		return maxEvalueExp;
	}

	public void setMaxEvalueExp( Integer maxEvalueExp ) {
		this.maxEvalueExp = maxEvalueExp;
	}

	public Integer getMaxEvalueExpMin() {
		return maxEvalueExpMin;
	}

	public Integer getMaxEvalueExpMax() {
		return maxEvalueExpMax;
	}

	public String getOffsetListRegex(){
		return offsetListRegex;
	}

	public String getOffsetListSeparator() {
		return offsetListSep;
	}
}
