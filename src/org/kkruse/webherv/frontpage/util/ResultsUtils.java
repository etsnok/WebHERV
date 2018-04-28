package org.kkruse.webherv.frontpage.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.kkruse.webherv.frontpage.results.ResultsTab;
import org.kkruse.webherv.frontpage.results.ResultsTab.HervHit;
import org.kkruse.webherv.frontpage.results.ResultsTab.OffsetHervHits;

import com.unister.semweb.biodrums.herv.HERV;


/**
 * Util class for objects that are needed when showing the results.
 * 
 * @author Konstantin Kruse
 */
public class ResultsUtils {

	/** Maps the numeric strand (0,1) to the symbols (+,-) */
	static Map<Integer, String> STRAND_TO_STRING_MAP = new HashMap<>();
	static{
		STRAND_TO_STRING_MAP.put( 1, "+" );
		STRAND_TO_STRING_MAP.put( 0, "-" );
	}

	//-------------------------------------------------------------------------
	/**
	 * Convert a {@link HERV} result object into a {@link HervHit}.
	 * @param herv the {@link HERV} to convert. 
	 * @return the converted {@link HervHit}.
	 */
	public static HervHit toHervHit( HERV herv ){

		// convert:
		String id         = String.valueOf( ( int ) herv.getIdHERV()  );
		String chromosome = String.valueOf( ( int ) herv.getChromosome() );
		int startHerv     =  ( int ) herv.getStartHERV();
		int endHerv       =  ( int ) herv.getEndHERV();
		String strand     = String.valueOf( STRAND_TO_STRING_MAP.get( ( int ) herv.getStrandOnChromosome() ) );

		// init new HervHit:
		HervHit newHit = new HervHit(id, chromosome,
				herv.getStartPositionChromosome(), herv.getEndPositionChromosome(), startHerv, 
				endHerv, herv.getEValue(), strand );

		return newHit;
	}

	//-------------------------------------------------------------------------
	/**
	 *  Convert a list of {@link HERV }s into a list of {@link HervHit}s.
	 *  
	 * @param hervs the list of {@link HERV }s.
	 * @return the converted list of {@link HervHit}s.
	 */
	public static List<HervHit> toHervHits( List<HERV> hervs ){

		List<HervHit> hervHits = new ArrayList<>();

		// for each HERv convert it into HervHit:
		for( HERV herv : hervs ){
			hervHits.add( toHervHit(herv) );
		}

		return hervHits;
	}

	public static Map<String, OffsetHervHits> toOffsetHervHits( Map<Integer, List<HERV>> hervs ){
		Map<String, OffsetHervHits> offsetHervHits = new LinkedHashMap<>();
		for( Integer offset : hervs.keySet() ){
			offsetHervHits.put( String.valueOf( offset ), new OffsetHervHits( offset, toHervHits(  hervs.get( offset ) ) ) );
		}		
		return offsetHervHits;
	}

}
