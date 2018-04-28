package org.kkruse.webherv.frontpage;

import javax.faces.bean.ManagedBean;

import org.kkruse.webherv.frontpage.results.ResultsTab.Gene;

@ManagedBean(name="geneSelectionView")
public class GeneSelectionView {

	private Gene selectedGene;
	private String selectedOffset;
	
	public Gene getSelectedGene() {
		return selectedGene;
	}

	public void setSelectedGene(Gene selectedGene) {
		this.selectedGene = selectedGene;
	}

	public String getSelectedOffset() {
		return selectedOffset;
	}

	public void setSelectedOffset(String selectedOffset) {
		this.selectedOffset = selectedOffset;
	}
	
	
}
