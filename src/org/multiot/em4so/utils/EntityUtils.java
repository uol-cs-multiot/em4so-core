package org.multiot.em4so.utils;

import java.util.List;
import java.util.Map;

public abstract class EntityUtils {
	private static EntityUtils entityUtils;
	
	public void newInstance(EntityUtils entityUtils) {
		EntityUtils.entityUtils = entityUtils;
	}
	
	public static EntityUtils getInstance() {
		return EntityUtils.entityUtils;
	}
	
	public abstract Map<String, List<String>> getParamRoles();
}
