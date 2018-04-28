package org.kkruse.webherv.frontpage;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import org.kkruse.webherv.upload.FileUploader;

@ManagedBean( name="backendStatusView" )
public class BackendStatusView {
	
	GeneDbStatus geneDbStatus;
	HERVDbStatus hervDbStatus;
	String currentHervDbQueryFileName;
	Integer currentHervDbQueryFileNameIdx;
	
	@ManagedProperty( value="#{fileUploader}" )
	FileUploader fileUploader;
	
	@PostConstruct
	public void init(){
		geneDbStatus = null;
		hervDbStatus = null;
	}
	
	private void checkGeneDbStatus(){
		geneDbStatus = fileUploader.isDbConnectionOpen() ? GeneDbStatus.OPEN : null;
	}
	
	public GeneDbStatus getGeneDbStatus() {
		return geneDbStatus;
	}

	public void setGeneDbStatus(GeneDbStatus geneDbStatus) {
		this.geneDbStatus = geneDbStatus;
	}

	public HERVDbStatus getHervDbStatus() {
		return hervDbStatus;
	}

	public void setHervDbStatus(HERVDbStatus hervDbStatus) {
		this.hervDbStatus = hervDbStatus;
	}

	public String getCurrentHervDbQueryFileName() {
		return currentHervDbQueryFileName;
	}

	public void setCurrentHervDbQueryFileName(String currentHervDbQueryFileName) {
		this.currentHervDbQueryFileName = currentHervDbQueryFileName;
	}

	public Integer getCurrentHervDbQueryFileNameIdx() {
		return currentHervDbQueryFileNameIdx;
	}

	public void setCurrentHervDbQueryFileNameIdx(Integer currentHervDbQueryFileNameIdx) {
		this.currentHervDbQueryFileNameIdx = currentHervDbQueryFileNameIdx;
	}



	public enum HERVDbStatus{
		OPEN, LOADING, DONE_LOADING
	}

	public enum GeneDbStatus{
		OPEN, LOADING, DONE_LOADING
	}
}
