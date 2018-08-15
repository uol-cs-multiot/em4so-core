package org.multiot.em4so.utils;

public abstract class TimeUtils {
	protected static TimeUtils timeUtils;
	
	public static TimeUtils getInstance() {
		return timeUtils;
	}
	
	
	public abstract int getTime();
	public abstract int getParamTimeLimit();
	public abstract int getParamMaxTimeout();

}
