package org.multiot.em4so.model;

public class Player {
	private int id;
	private Location myLocation;
	private int timeJoined;
	private int timeRecorded;
	private int timesWorked;
	
	private String role;

	public int getTimesWorked() {
		return timesWorked;
	}
	public void setTimesWorked(int timesWorked) {
		this.timesWorked = timesWorked;
	}
	
	public Player(int id, String role, int timeJoined, int timeRecorded){
		this.id = id;
		this.role = role;
		this.timeJoined = timeJoined;
		this.timeRecorded = timeRecorded;
		this.timesWorked = 0;
	}
	
	public Player(int id, String role, int timeJoined, int timeRecorded, Location location){
		this.id = id;
		this.role = role;
		this.timeJoined = timeJoined;
		this.timeRecorded = timeRecorded;
		this.timesWorked = 0;
		this.myLocation = location;
	}
	
	
	public Location getMyLocation() {
		return myLocation;
	}
	public void setMyLocation(Location myLocation) {
		this.myLocation = myLocation;
	}
	
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	
	public int getTimeJoined() {
		return timeJoined;
	}
	public void setTimeJoined(int timeJoined) {
		this.timeJoined = timeJoined;
	}
	public int getTimeRecorded() {
		return timeRecorded;
	}
	public void setTimeRecorded(int timeRecorded) {
		this.timeRecorded = timeRecorded;
	}
	public String toString(){
		return "Player_"+id+":{ role: "+role+", timeJoined:"+timeJoined+", timesWorked: "+timesWorked+", timeRecorded: "+timeRecorded+"}";
	}
	
	//TODO TBD 
	public double getDistanceTo(Location location){
		return 0;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
