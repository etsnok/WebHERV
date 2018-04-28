/**
 * 
 */
package org.kkruse.webherv.frontpage;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import org.kkruse.webherv.upload.FileUploader;

/**
 * @author Konstantin
 *
 */
@ManagedBean( name="fileUploadView" )
@ViewScoped
public class FileUploadView {

	private List<String> uploadedFileNames;
	
	@ManagedProperty( value="#{fileUploader}" )
	private FileUploader fileUploader;	
	
	public void setFileUploader( FileUploader fileUploader ){
		this.fileUploader = fileUploader;
	}
	
	@PostConstruct
	public void init(){
		reloadFileNames();
	}
	
	public void reloadFileNames(){
		uploadedFileNames = new ArrayList<>( fileUploader.getUploadedGeneLists().keySet() );
	}
	
	public List<String> getUploadedFileNames(){
		return new ArrayList<>( fileUploader.getUploadedGeneLists().keySet() );
	}
	
	public void deleteFileByName( String fileNameToDelete ){
		if( fileUploader.getUploadedGeneLists().containsKey(fileNameToDelete) ){
			fileUploader.getUploadedGeneLists().remove(fileNameToDelete);
		}
	}

	
}
