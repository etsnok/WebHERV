package org.kkruse.webherv.frontpage;

import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

public interface InputService {

	// Getter -----------------------------------------------------------------	
	List<SelectItem> getVariants();

	List<SelectItem> getRanges();

	List<SelectItem> getPlatforms();

	List<SelectItem> getGenomes();

	Map<String, String> getPlatformGenomes();

	String getDefaultRange();

	String getDefaultVariant();

	void setDefaultVariant(String defaultVariant);

	void setDefaultRange(String defaultRange);

	String getDefaultPlatform();

	void setDefaultPlatform(String defaultPlatform);

}