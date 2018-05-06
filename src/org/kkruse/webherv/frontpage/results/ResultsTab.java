package org.kkruse.webherv.frontpage.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.kkruse.webherv.genes.sqlite.GeneDBConnector.GeneEntry;

public class ResultsTab {

	private String id;
	private String label;
	private String file;
	private List<Gene> genes;
	private List<Gene> filteredGenes;
	private List<ColumnModel> columns;

	private boolean hideUnknownGenes;
	private boolean hideGenesWoResults;

	private Integer totalNumGenes;
	private Integer knownNumGenes;
	private List<Pair<Integer, Integer>> resultsNumGenes;


	public ResultsTab(String id, String label, String file, List<Gene> genes) {
		this.id = id;
		this.label = label;
		this.file = file;
		this.genes = genes;

		this.hideGenesWoResults = true;
		this.hideUnknownGenes   = true;

		totalNumGenes   = genes.size();
		resultsNumGenes = countGenesWithResults( genes );
		knownNumGenes   = countGenesKnown( genes );

		updateFilteredGenes();
		createDynamicColumns();
	}

	public void updateFilteredGenes(){
		if( !hideUnknownGenes && !hideGenesWoResults ){
			filteredGenes = genes;
			return;
		}
		filteredGenes = new ArrayList<>();
		for( Gene gene : genes ){
			if( hideGenesWoResults || hideUnknownGenes ){
				if( gene.offsetHervHits != null ){
					String off = String.valueOf( gene.offsets.get( gene.offsets.size() - 1 ) );
					OffsetHervHits hit = gene.offsetHervHits.get( off );
					if( !hideGenesWoResults || hit.hervHitsSize > 0 ){
						filteredGenes.add( gene );
					}
				} 
			} else {
				filteredGenes.add( gene );
			}
		}

	}


	private List<Pair<Integer, Integer>> countGenesWithResults( List<Gene> genes ){
		Map<Integer, Integer> hervCountsPerOffset = null;
		for( Gene gene : genes ){
			if( hervCountsPerOffset == null ){
				hervCountsPerOffset = new LinkedHashMap<>();
				for( Integer o : gene.offsets ){
					hervCountsPerOffset.put(o, 0);
				}
			}

			if( gene.offsetHervHits != null ){
				for( Integer off : gene.offsets ){
					OffsetHervHits hit = gene.offsetHervHits.get( String.valueOf( off ) );
					if( hit != null && hit.hervHitsSize > 0 ){
						Integer c = hervCountsPerOffset.get( off );
						hervCountsPerOffset.put( off, ++c );
					}
				}
			}
		}

		List<Pair<Integer, Integer>> countsList = new ArrayList<>();
		if( hervCountsPerOffset != null ){
			for( Integer k : hervCountsPerOffset.keySet() ){
				countsList.add( new ImmutablePair<Integer, Integer>( k, hervCountsPerOffset.get(k) ) );
			}
		}

		return countsList;
	}

	private int countGenesKnown( List<Gene> genes ){
		int count = 0;
		if(genes != null){
			for( Gene gene : genes ){
				if( gene.offsetHervHits != null ){
					count++;
				}
			}
		}
		return count;
	}

	private void createDynamicColumns() {

		columns = new ArrayList<ColumnModel>();   

		if( genes != null && genes.size() > 0 && genes.get(0) != null ){
			List<Integer> off = genes.get(0).offsets;
			if( off != null ){
				for( Integer columnKey : off ) {
					String key = String.valueOf( columnKey );
					columns.add( new ColumnModel( key, key ) );
				}
			}		
		}		
	}

	public void updateColumns() {
		//reset table state
		UIComponent table = FacesContext.getCurrentInstance().getViewRoot().findComponent(":form:cars");
		table.setValueExpression("sortBy", null);

		//update columns
		createDynamicColumns();
	}

	public List<ColumnModel> getColumns() {
		return columns;

	}

	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}


	public String getFile() {
		return file;
	}


	public void setFile(String file) {
		this.file = file;
	}


	public List<Gene> getGenes() {
		return genes;
	}


	public void setGenes(List<Gene> genes) {
		this.genes = genes;
	}

	public List<Gene> getFilteredGenes() {
		return filteredGenes;
	}

	public void setFilteredGenes(List<Gene> filteredGenes) {
		this.filteredGenes = filteredGenes;
	}

	public boolean isHideUnknownGenes() {
		return hideUnknownGenes;
	}

	public void setHideUnknownGenes(boolean hideUnknownGenes) {
		this.hideUnknownGenes = hideUnknownGenes;
	}

	public boolean isHideGenesWoResults() {
		return hideGenesWoResults;
	}

	public void setHideGenesWoResults(boolean hideGenesWoResults) {
		this.hideGenesWoResults = hideGenesWoResults;
		if( this.hideGenesWoResults ){
			this.hideUnknownGenes = true;
		}
	}

	public Integer getTotalNumGenes() {
		return totalNumGenes;
	}

	public void setTotalNumGenes(Integer totalNumGenes) {
		this.totalNumGenes = totalNumGenes;
	}

	public Integer getKnownNumGenes() {
		return knownNumGenes;
	}

	public void setKnownNumGenes(Integer knownNumGenes) {
		this.knownNumGenes = knownNumGenes;
	}

	public List<Pair<Integer, Integer>> getResultsNumGenes() {
		return resultsNumGenes;
	}

	//	public void setResultsNumGenes(Integer resultsNumGenes) {
	//		this.resultsNumGenes = resultsNumGenes;
	//	}

	static public class ColumnModel implements Serializable {

		private static final long serialVersionUID = -8894578229478663794L;
		private String header;
		private String property;

		public ColumnModel(String header, String property) {
			this.header = header;
			this.property = property;
		}

		public String getHeader() {
			return header;
		}

		public String getProperty() {
			return property;
		}
	}


	public static class Gene{

		private String id;
		private GeneEntry geneEntry;
		private Map<String, OffsetHervHits> offsetHervHits;
		private List<Integer> offsets;

		public Gene(String id, GeneEntry geneEntry, Map<String, OffsetHervHits> offsetHervHits, List<Integer> offsets ) {
			this.id = id;
			this.geneEntry = geneEntry;
			this.offsetHervHits = offsetHervHits;
			this.offsets = offsets;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public GeneEntry getGeneEntry() {
			return geneEntry;
		}

		public void setGeneEntry(GeneEntry geneEntry) {
			this.geneEntry = geneEntry;
		}

		public Map<String, OffsetHervHits> getOffsetHervHits() {
			return offsetHervHits;
		}

		public void setOffsetHervHits(Map<String, OffsetHervHits> offsetHervHits) {
			this.offsetHervHits = offsetHervHits;
		}

	}

	public static class OffsetHervHits{
		private Integer offset;
		private List<HervHit> hervHits;
		private int hervHitsSize;

		public OffsetHervHits(Integer offset, List<HervHit> hervHits) {
			this.offset = offset;
			this.hervHits = hervHits;
			setHervHitsSize(hervHits != null ? hervHits.size() : 0);
		}

		public Integer getOffset() {
			return offset;
		}

		public void setOffset(Integer offset) {
			this.offset = offset;
		}

		public List<HervHit> getHervHits() {
			return hervHits;
		}

		public void setHervHits(List<HervHit> hervHits) {
			this.hervHits = hervHits;
		}

		public int getHervHitsSize() {
			return hervHitsSize;
		}

		public void setHervHitsSize(int hervHitsSize) {
			this.hervHitsSize = hervHitsSize;
		}

	}


	public static class HervHit{

		private String id;
		private String chromosome; 
		private Integer startChromosome; 
		private Integer endChromosome; 
		private Integer startHerv; 
		private Integer endHerv; 
		private Double eValue; 
		private String chromStrand;

		public HervHit(String id, String chromosome, Integer startChromosome, Integer endChromosome, Integer startHerv,
				Integer endHerv, Double eValue, String chromStrand) {

			this.id = id;
			this.chromosome = chromosome;
			this.startChromosome = startChromosome;
			this.endChromosome = endChromosome;
			this.startHerv = startHerv;
			this.endHerv = endHerv;
			this.eValue = eValue;
			this.chromStrand = chromStrand;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getChromosome() {
			return chromosome;
		}

		public void setChromosome(String chromosome) {
			this.chromosome = chromosome;
		}

		public Integer getStartChromosome() {
			return startChromosome;
		}

		public void setStartChromosome(Integer startChromosome) {
			this.startChromosome = startChromosome;
		}

		public Integer getEndChromosome() {
			return endChromosome;
		}

		public void setEndChromosome(Integer endChromosome) {
			this.endChromosome = endChromosome;
		}

		public Integer getStartHerv() {
			return startHerv;
		}

		public void setStartHerv(Integer startHerv) {
			this.startHerv = startHerv;
		}

		public Integer getEndHerv() {
			return endHerv;
		}

		public void setEndHerv(Integer endHerv) {
			this.endHerv = endHerv;
		}

		public Double geteValue() {
			return eValue;
		}

		public void seteValue(Double eValue) {
			this.eValue = eValue;
		}

		public String getChromStrand() {
			return chromStrand;
		}

		public void setChromStrand(String chromStrand) {
			this.chromStrand = chromStrand;
		} 


	}
}
