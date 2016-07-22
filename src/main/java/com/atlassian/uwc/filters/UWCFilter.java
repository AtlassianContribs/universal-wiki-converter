package com.atlassian.uwc.filters;

import java.util.Properties;

public interface UWCFilter {

	public void setProperties(Properties properties);
	public Properties getProperties();
}
