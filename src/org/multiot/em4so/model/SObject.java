package org.multiot.em4so.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import org.multiot.em4so.behaviour.ObjectExecutor;



public class SObject {
	
	private ObjectExecutor executor;
	
	/**
	 *My active roles 
	 */
	private List<String> myRoles;
	
	/**
	 * inactive roles 
	 */
	private List<String> inactiveRoles;
	
	/**
	 * inactive capabilities 
	 */
	private List<String> inactiveCapabilities;
	
	/**
	 * My capabilities 
	 */
	private List<String> myCapabilities;
	/**
	 * Known players by role 
	 */
	private Map<String,Map<String,Player>> knownPlayers;
	
	private Location myLocation;
		
	private List sensors;
		
	private List actuators;
		
	private int owner;
	
	private Set<String> knownPlayersSet;
		
		/**
		 * Work I have done since joined 
		 */
	private int contribution;
		
		/**
		 * Number of active connections 
		 */
	private int myConnections;
	
	private int address;
	
	private boolean addNewPeer;
	
	private Map<String,Plan> pendingPlansTrigger;
	/**
	 * Players already started not committed yet to a triggered plan activity. Table where key=planId & value= {Table where key=playerId, value = ActivePlayer} 
	 */
	private Map<String,Map<String,ActivePlayer>> pendingPlansStarted;
	
	/**
	 * Players that have not sent executed confirmation yet 
	 */
	private Map<String,Map<String,ActivePlayer>> pendingPlansExecuted;
	
	private Map<String,Integer> attemptedPlans;
	
	/**
	 * Roles which I do not known any player and I am waiting query hits for them 
	 */
	private List<String> queryingRoles;
	
	
	/**
	 * committed pending activities
	 */
	private Queue<Plan> executionQueue;
	
	private Set<String> relevantRoles;
	
	
	private Deque<Integer> knownPeers;
	
	private int timeJoined;
	
	private int maxKnownPeers;
	
	
	public ObjectExecutor getExecutor() {
		return executor;
	}


	public void setExecutor(ObjectExecutor executor) {
		this.executor = executor;
	}


	public boolean isAddNewPeer() {
		return addNewPeer;
	}


	public void setAddNewPeer(boolean addNewPeer) {
		this.addNewPeer = addNewPeer;
	}


	public int getTimeJoined() {
		return timeJoined;
	}


	public void setTimeJoined(int timeJoined) {
		this.timeJoined = timeJoined;
	}


	public Deque<Integer> getKnownPeers() {
		return knownPeers;
	}


	public void setKnownPeers(Deque<Integer> knownPeers) {
		this.knownPeers = knownPeers;
	}


	public Set<String> getRelevantRoles() {
		return relevantRoles;
	}


	public void setRelevantRoles(Set<String> relevantRoles) {
		this.relevantRoles = relevantRoles;
	}


	public Location getMyLocation() {
		return myLocation;
	}


	public void setMyLocation(Location myLocation) {
		this.myLocation = myLocation;
	}


	public Queue<Plan> getExecutionQueue() {
		return executionQueue;
	}


	public void setExecutionQueue(Queue<Plan> executionQueue) {
		this.executionQueue = executionQueue;
	}


	public Map<String, Plan> getPendingPlansTrigger() {
		return pendingPlansTrigger;
	}


	public void setPendingPlansTrigger(Map<String, Plan> pendingPlansTrigger) {
		this.pendingPlansTrigger = pendingPlansTrigger;
	}


	public Map<String, Map<String, ActivePlayer>> getPendingPlansStarted() {
		return pendingPlansStarted;
	}


	public void setPendingPlansStarted(Map<String, Map<String, ActivePlayer>> pendingPlansStarted) {
		this.pendingPlansStarted = pendingPlansStarted;
	}


	public Map<String, Map<String, ActivePlayer>> getPendingPlansExecuted() {
		return pendingPlansExecuted;
	}


	public void setPendingPlansExecuted(Map<String, Map<String, ActivePlayer>> pendingPlansExecuted) {
		this.pendingPlansExecuted = pendingPlansExecuted;
	}


	public Map<String, Integer> getAttemptedPlans() {
		return attemptedPlans;
	}


	public void setAttemptedPlans(Map<String, Integer> attemptedPlans) {
		this.attemptedPlans = attemptedPlans;
	}


	public List<String> getQueryingRoles() {
		return queryingRoles;
	}


	public void setQueryingRoles(List<String> queryingRoles) {
		this.queryingRoles = queryingRoles;
	}
	
	


	public List<String> getInactiveRoles() {
		return inactiveRoles;
	}


	public void setInactiveRoles(List<String> inactiveRoles) {
		this.inactiveRoles = inactiveRoles;
	}


	public List<String> getInactiveCapabilities() {
		return inactiveCapabilities;
	}


	public void setInactiveCapabilities(List<String> inactiveCapabilities) {
		this.inactiveCapabilities = inactiveCapabilities;
	}

	

	public int getMaxKnownPeers() {
		return maxKnownPeers;
	}


	public void setMaxKnownPeers(int maxKnownPeers) {
		this.maxKnownPeers = maxKnownPeers;
	}


	/**
	 * TODO define??
	 */
//	private def discoveryPolicies

		
	
	public SObject(int address,List<String> myRoles, List<String> myCapabilites,ObjectExecutor executor, int maxPeers, String lon, String lat){
		this.myCapabilities = myCapabilites;
		this.myRoles = myRoles;
		this.address = address;
		initializeCollections(maxPeers);
		executionQueue = new LinkedBlockingQueue<Plan>();
		this.myLocation = new Location(lon,lat);
		this.relevantRoles = new HashSet<String>();
		if (address%2==0) this.addNewPeer = true;
		else this.addNewPeer = false;
		this.maxKnownPeers=maxPeers;
		this.executor = executor;
	}
	
	
	public void initializeCollections(int maxPeers){
		knownPlayers = Collections.synchronizedMap(new HashMap<String,Map<String,Player>>());
		this.knownPeers = new LinkedBlockingDeque<Integer>(maxPeers);
		queryingRoles = new ArrayList<String>();
		pendingPlansTrigger = new HashMap<String,Plan>();
		pendingPlansStarted = new HashMap<String,Map<String,ActivePlayer>>();
		pendingPlansExecuted = new HashMap<String,Map<String,ActivePlayer>>();
		attemptedPlans = new HashMap<String,Integer>();
		knownPlayersSet = new HashSet<String>();
	}
	
	
	

	public List<String> getMyRoles() {
		return myRoles;
	}

	public void setMyRoles(List<String> myRoles) {
		this.myRoles = myRoles;
	}

	public List<String> getMyCapabilities() {
		return myCapabilities;
	}

	public void setMyCapabilities(List<String> myCapabilities) {
		this.myCapabilities = myCapabilities;
	}

	public Map<String, Map<String, Player>> getKnownPlayers() {
		return knownPlayers;
	}

	public void setKnownPlayers(Map<String, Map<String, Player>> knownPlayers) {
		this.knownPlayers = knownPlayers;
	}

	public int getContribution() {
		return contribution;
	}

	public void setContribution(int contribution) {
		this.contribution = contribution;
	}

	public int getMyConnections() {
		return myConnections;
	}

	public void setMyConnections(int myConnections) {
		this.myConnections = myConnections;
	}


	public int getAddress() {
		return address;
	}


	public void setAddress(int address) {
		this.address = address;
	}
	
	public boolean doesTriggerNow(String role){
		if(queryingRoles.contains(role.trim()))
			return false;
		else
			return true;
	}
	
	public String toString(){
		return "("+address+"): {r:"+myRoles+"-"+" qR: "+queryingRoles+"}";
	}
	
	public int countKnownPlayers(){
		return getKnownPlayersSet().size();
	}
	
	
	public Map<String,Player> getSubstitutePlayers(){
		Map<String,Player> substitutes=new HashMap<String,Player>();
		
		for(Entry<String,Map<String,Player>> entry : knownPlayers.entrySet() ){
			if(myRoles.contains(entry.getKey())){
				substitutes.putAll(entry.getValue());
			}
		}
		
		return substitutes;
	}
	
	public Map<String,Player> getComplementaryPlayers(){
		Map<String,Player> complementaries=new HashMap<String,Player>();
		
		for(Entry<String,Map<String,Player>> entry : knownPlayers.entrySet() ){
			if(!myRoles.contains(entry.getKey())){
				complementaries.putAll(entry.getValue());
			}
		}
		
		return complementaries;
	}
	
	
	public Set<String> getKnownPlayersSet(){
		knownPlayersSet.clear();
		for(Entry<String,Map<String,Player>> entry : knownPlayers.entrySet() ){
			for (Entry<String,Player> entryPlayer : entry.getValue().entrySet() ){
				knownPlayersSet.add(String.valueOf(entryPlayer.getValue().getId()));
			}
		}
		return knownPlayersSet;
	}

	
	public int calculateKnownPlayers(){
		int knownRolePlayers=0;
		for(Entry<String,Map<String,Player>> entry : knownPlayers.entrySet() ){
			for (Entry<String,Player> entryPlayer : entry.getValue().entrySet() ){
				if(!entryPlayer.getKey().equals(String.valueOf(this.getAddress())))
					knownRolePlayers++;
			}
		}
		
		return knownRolePlayers;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + address;
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
		SObject other = (SObject) obj;
		if (address != other.address)
			return false;
		return true;
	}
	
	

	
}
