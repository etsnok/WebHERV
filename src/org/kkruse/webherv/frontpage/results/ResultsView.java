package org.kkruse.webherv.frontpage.results;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.TabCloseEvent;

/**
 * 
 * @author Konstantin Kruse
 */
@ManagedBean(name="resultsView")
@SessionScoped
public class ResultsView {

	List<ResultsTab> resultsTabs;

	@PostConstruct
	public void init() {
	}


	public void onTabChange(TabChangeEvent event) {
		FacesMessage msg = new FacesMessage("Tab Changed", "Active Tab: " + event.getTab().getTitle());
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	public void onTabClose(TabCloseEvent event) {
		FacesMessage msg = new FacesMessage("Tab Closed", "Closed tab: " + event.getTab().getTitle());
		FacesContext.getCurrentInstance().addMessage(null, msg);
	}

	public void setResultsTabs( List<ResultsTab> resultsTabs ) {
		this.resultsTabs = resultsTabs;

	}

	public List<ResultsTab> getResultsTabs(){
		return this.resultsTabs;
	}


}
