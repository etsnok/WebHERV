/**
 * 
 */
package org.kkruse.webherv.frontpage.util;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 * @author Konstantin Kruse
 */
@ManagedBean( name="fatalErrorDialogView" )
@ViewScoped
public class FatalErrorDialogView {

	private String errorHeader;
	private String errorMsg;
	
	public String getErrorHeader() {
		return errorHeader;
	}
	public void setErrorHeader(String errorHeader) {
		this.errorHeader = errorHeader;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
}
