package org.kkruse.webherv.frontpage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

/**
 * @author Konstantin Kruse
 */
public class InputServiceImpl implements InputService{
	
	private List<SelectItem> variants;
	private List<SelectItem> ranges;
	private List<SelectItem> platforms;
	private List<SelectItem> genomes;

	private String defaultVariant;
	private String defaultRange;
	private String defaultPlatform;

	private Map<String, String> platformGenomes;
	
	//-------------------------------------------------------------------------
	public InputServiceImpl(){
		
		platforms = new ArrayList<>();
		platforms.add( new SelectItem( "hg19_HuEx_1_0_st_v2_na32_probeset", "Hg19 Affy Human Exon 1.0v2" ) );
		platforms.add( new SelectItem( "hg18_HuEx_1_0_st_v2_na29_probeset", "Hg18 Affy Human Exon 1.0v2") );

		platformGenomes = new HashMap<>();
		platformGenomes.put( "hg19_HuEx_1_0_st_v2_na32_probeset" , "hervs_hg19");
		platformGenomes.put( "hg18_HuEx_1_0_st_v2_na29_probeset" , "hervs_hg18");
		
		genomes = new ArrayList<>();
		genomes.add( new SelectItem( "hervs_hg19","hervs_hg19" ));
		genomes.add( new SelectItem( "hervs_hg18","hervs_hg18" ) );
		
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
	
}
