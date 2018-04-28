package org.kkruse.webherv.frontpage;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean( name="testBean" )
@SessionScoped
public class TestBean {

	String test = "hallo";

	public String getTest() {
		return "halleo";
	}

	public void setTest(String test) {
		this.test = test;
	}
	
}
