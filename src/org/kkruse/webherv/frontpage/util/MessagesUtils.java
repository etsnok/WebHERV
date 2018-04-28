package org.kkruse.webherv.frontpage.util;

import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;


public class MessagesUtils {

	public static void showInfoMsg( String msgId, String msg ){
		 FacesContext.getCurrentInstance().addMessage( msgId , new FacesMessage( FacesMessage.SEVERITY_INFO, msg, null) );
	}
	
	public static void showWarnMsg( String msgId, String msg ){
		 FacesContext.getCurrentInstance().addMessage( msgId , new FacesMessage( FacesMessage.SEVERITY_WARN, msg, null) );
	}

	public static void showFatalMsg( String msgId, String msg ){
		FacesContext.getCurrentInstance().addMessage( msgId , new FacesMessage( FacesMessage.SEVERITY_FATAL, msg, null) );
	}

	public static void showErrorMsg( String msgId, String msg ){
		FacesContext.getCurrentInstance().addMessage( msgId , new FacesMessage( FacesMessage.SEVERITY_ERROR, msg, null) );
	}
	
	
}
