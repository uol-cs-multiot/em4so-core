package org.multiot.em4so.config;

public final class GlobalParameters {
	
	/**
	 * Return configured Base TTL
	 * @return
	 */
	public static int getBaseTTL() {
		//TODO obtain from properties file
		return 3;
	}
	
	public static int getMsgLowPriority() {
		//TODO obtain from properties file
		return 3;
	}
	public static int getMsgMediumPriority() {
		//TODO obtain from properties file
		return 2;
	}
	public static int getMsgHighPriority() {
		//TODO obtain from properties file
		return 1;
	}
}
