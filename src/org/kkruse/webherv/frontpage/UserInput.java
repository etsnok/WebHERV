package org.kkruse.webherv.frontpage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

	// Lists Getter -----------------------------------------------------------
	public List<SelectItem> getRanges(){
		return webHervSettings.getRanges();
	}

	public List<SelectItem> getPlatforms(){
		return webHervSettings.getPlatforms();
	}

	public List<SelectItem> getDrumsDirs(){
		return webHervSettings.getDrumsDirs();
	}

	public List<SelectItem> getVariants(){
		return webHervSettings.getVariants();
	}	

	// Selected Values Getter and Setter --------------------------------------
	public String getOffset() {
		return offset;
	}

	public void setOffset(String offset) {
		this.offset = offset;
	}


	public String getSelectedRange() {
		return selectedRange;
	}

	public void setSelectedRange(String selectedRange) {
		this.selectedRange = selectedRange;
	}

	public String getSelectedPlatform() {
		return selectedPlatform;
	}

	public void setSelectedPlatform(String selectedPlatform) {
		this.selectedPlatform = selectedPlatform;
		//this.selectedDrums = webHervSettings.getPlatformDrums().get( this.selectedPlatform );
	}

	public String getSelectedDrums() {
		return selectedDrums;
	}

	public void setSelectedDrums(String selectedDrums) {
		this.selectedDrums = selectedDrums;
	}

	public boolean isSelectedByGenome() {
		return selectedByGenome;
	}

	public void setSelectedByGenome(boolean selectedByGenome) {
		this.selectedByGenome = selectedByGenome;
	}

	public String getSelectedVariant() {
		return selectedVariant;
	}

	public void setSelectedVariant(String selectedVariant) {
		this.selectedVariant = selectedVariant;
	}

	public Integer getMinimalLength() {
		return minimalLength;
	}

	public void setMinimalLength(Integer minimalLength) {
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
