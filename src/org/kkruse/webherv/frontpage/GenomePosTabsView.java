package org.kkruse.webherv.frontpage;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.kkruse.webherv.drums.HervService.GeneEntryTables;
import org.kkruse.webherv.genes.sqlite.GeneDBConnector.GeneEntry;
import org.primefaces.event.TabCloseEvent;

@ManagedBean( name="genomePosTabsView" )
@ViewScoped
public class GenomePosTabsView {

	private List<GenomePosTab> genomePosTabs;

	@ManagedProperty( value="#{inputController}" )
	private InputController inputController;

	private int tabIndex;

	@PostConstruct
	public void init(){
		GeneEntryTables tables = inputController.getTables();
		if( tables != null ){
			genomePosTabs = new ArrayList<>();

			for ( String filename : tables.getGeneEntryTables().keySet() ){
				//				GenomePosTab
				GenomePosTab tab = new GenomePosTab();
				genomePosTabs.add(tab);
				tab.setFilename(filename);
				List<GeneEntry> genes = tables.getGeneEntryTables().get(filename);
				tab.setGenomePos(genes);
			}		
		}
	}



	public List<GenomePosTab> getGenomePosTabs() {
		return genomePosTabs;
	}

	public void delteGenomePosTab( ){
		System.out.println("Delete a tab ..." + tabIndex);
	}

	public void onTabClose(TabCloseEvent event) {
		System.out.println("Delete a tab ..." + event.getTab().getTitle());
		
		inputController.deleteTableByName(event.getTab().getTitle());
	}

	//	public void delteGenomePosTab( GenomePosTab _tabToDelete ){
	//		if( _tabToDelete != null && genomePosTabs != null ){
	//			genomePosTabs.remove(_tabToDelete);
	//		}
	//	}

	public void setGenomePosTabs(List<GenomePosTab> genomePosTabs) {
		this.genomePosTabs = genomePosTabs;
	}

	public void setInputController( InputController inputController ){
		this.inputController = inputController;
	}


	public int getTabIndex() {
		return tabIndex;
	}



	public void setTabIndex(int tabIndex) {
		this.tabIndex = tabIndex;
	}


	public static class GenomePosTab {

		private String filename;
		private List<GeneEntry> genomePos;

		public List<GeneEntry> getGenomePos() {
			return genomePos;
		}

		public void setGenomePos(List<GeneEntry> genomePos) {
			this.genomePos = genomePos;
		}

		public String getFilename() {
			return filename;
		}

		public void setFilename(String filename) {
			this.filename = filename;
		}

	}

}


