/*
 * oxCore is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.xdi.service.custom.script;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.log.Log;
import org.xdi.model.custom.script.CustomScriptType;
import org.xdi.model.custom.script.conf.CustomScriptConfiguration;
import org.xdi.util.StringHelper;

/**
 * Provides factory methods needed to create external extension
 *
 * @author Yuriy Movchan Date: 01/08/2015
 */
public class ExternalScriptService implements Serializable {

	private static final long serialVersionUID = -1070021905117441202L;
	
	protected CustomScriptType customScriptType;

	protected Map<String, CustomScriptConfiguration> customScriptConfigurationsNameMap;
	protected List<CustomScriptConfiguration> customScriptConfigurations;
	protected CustomScriptConfiguration defaultExternalCustomScript;

	@Logger
	protected Log log;
	
	@In
	protected CustomScriptManager customScriptManager;
	
	public ExternalScriptService(CustomScriptType customScriptType) {
		this.customScriptType = customScriptType;
	}

	@Observer(CustomScriptManager.MODIFIED_EVENT_TYPE)
	public void reload() {
		// Get actual list of external configurations
		List<CustomScriptConfiguration> newCustomScriptConfigurations = customScriptManager.getCustomScriptConfigurationsByScriptType(customScriptType);
		addExternalConfigurations(newCustomScriptConfigurations);
		
		this.customScriptConfigurations = newCustomScriptConfigurations; 
		this.customScriptConfigurationsNameMap = buildExternalConfigurationsNameMap(customScriptConfigurations);

		// Determine default configuration
		this.defaultExternalCustomScript = determineDefaultCustomScriptConfiguration(this.customScriptConfigurations);
		
		// Allow to executu additional logic
		reloadExternal();
	}

	protected void addExternalConfigurations(List<CustomScriptConfiguration> newCustomScriptConfigurations) {
	}

	protected void reloadExternal() {
	}

	private Map<String, CustomScriptConfiguration> buildExternalConfigurationsNameMap(List<CustomScriptConfiguration> customScriptConfigurations) {
		Map<String, CustomScriptConfiguration> reloadedExternalConfigurations = new HashMap<String, CustomScriptConfiguration>(customScriptConfigurations.size());
		
		for (CustomScriptConfiguration customScriptConfiguration : customScriptConfigurations) {
			reloadedExternalConfigurations.put(StringHelper.toLowerCase(customScriptConfiguration.getName()), customScriptConfiguration);
		}

		return reloadedExternalConfigurations;
	}

	public CustomScriptConfiguration determineDefaultCustomScriptConfiguration(List<CustomScriptConfiguration> customScriptConfigurations) {
		CustomScriptConfiguration defaultExternalCustomScript = null;
		for (CustomScriptConfiguration customScriptConfiguration : this.customScriptConfigurations) {
			// Determine default script. It has lower level than others
			if ((defaultExternalCustomScript == null) ||
					(defaultExternalCustomScript.getLevel() >= customScriptConfiguration.getLevel())) {
				defaultExternalCustomScript = customScriptConfiguration;
			}
		}
		
		return defaultExternalCustomScript;
	}

	public boolean isEnabled() {
		if (this.customScriptConfigurations == null) {
			return false;
		}

		return this.customScriptConfigurations.size() > 0;
	}

	public CustomScriptConfiguration getCustomScriptConfigurationByName(String name) {
		return this.customScriptConfigurationsNameMap.get(StringHelper.toLowerCase(name));
	}

	public CustomScriptConfiguration getDefaultExternalCustomScript() {
		return defaultExternalCustomScript;
	}

	public List<CustomScriptConfiguration> getCustomScriptConfigurations() {
		return this.customScriptConfigurations;
	}

}
