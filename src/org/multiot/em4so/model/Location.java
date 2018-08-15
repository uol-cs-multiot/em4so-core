package org.multiot.em4so.model;

public class Location {
/**
 *longitude 
 */
private String lon;
/**
 * latitude
 */
private String lat;

public Location(String lon, String lat){
	this.lon = lon;
	this.lat = lat;
}


public String getLon() {
	return lon;
}
public void setLon(String lon) {
	this.lon = lon;
}
public String getLat() {
	return lat;
}
public void setLat(String lat) {
	this.lat = lat;
}

public String toString(){
	return "lon: "+lon+", lat: "+lat;
}
}
