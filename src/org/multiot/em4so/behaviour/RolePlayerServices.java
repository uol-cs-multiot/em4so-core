package org.multiot.em4so.behaviour;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.multiot.em4so.model.ActivePlayer;
import org.multiot.em4so.model.Plan;
import org.multiot.em4so.model.Player;
import org.multiot.em4so.model.SObject;
import org.multiot.em4so.utils.EntityUtils;
import org.multiot.em4so.utils.TimeUtils;;

public class RolePlayerServices {
	private BehaviourImplementator implementator;
	
	private static final int limitTimePlayerStarted = 30; // max time allowed to
															// wait for a task
															// completion
															// notification
															// after the
															// timelimit
															// specified when
															// task started
															// increased to 30 to give more realistic tolerance considering low resource devices 

	public static synchronized void updateKnownPlayersRole(SObject sobject, String role, List<Player> players) {
		boolean toAdd = false;
		Player player = null, receivedPlayer =null, preExistingPlayer = null;
		Iterator<Player> itp = players.iterator();
		Map<String,Map<String,Player>> knownPlayersSoFar;
		Map<String,Player> playersForRole;
		Player minPlayer;
		String keyPlayer=null;
		
		while (itp.hasNext()) 
				{
			receivedPlayer = itp.next();
			knownPlayersSoFar = sobject.getKnownPlayers();
			if(knownPlayersSoFar.containsKey(role) 
					&& knownPlayersSoFar.get(role).containsKey(String.valueOf(receivedPlayer.getId()))
					){
				//Discard if I already have the player
				toAdd = false;
			}else{
				if(sobject.calculateKnownPlayers()<(Integer)sobject.getMaxKnownPeers()){ //simplify to just one value max peers
					toAdd = true;
				}else{
					//Add always the coming player as the SO is needing it, EXCEPT, the received player is already in cache
					minPlayer = getMinScorePlayer(sobject,knownPlayersSoFar);
					if(minPlayer!=null)
						if(!minPlayer.getRole().equals(role)){
							removeFromKnownPlayersPlayer(sobject, minPlayer.getId());
							toAdd = true;
						}else{
							//Discarded if the min has the same role, because it is knwon for longer
							toAdd = false;
						}
					else
						toAdd=true;
				}
			}

			if (toAdd) {
				if (!knownPlayersSoFar.containsKey(role)) {
					knownPlayersSoFar.put(role, Collections.synchronizedMap(new HashMap<String, Player>()));
				}
				//Brand new player to avoid references to others own players
				playersForRole = knownPlayersSoFar.get(role);
				player = new Player(receivedPlayer.getId(),role,receivedPlayer.getTimeJoined(),TimeUtils.getInstance().getTime());
				//Every SO has its own count...although it could reuse count from others
				
				preExistingPlayer = getCommonPlayerDifferentRole(sobject,receivedPlayer);
				
				if(preExistingPlayer!=null){
					player.setTimeRecorded(preExistingPlayer.getTimeRecorded());
					player.setTimesWorked(preExistingPlayer.getTimesWorked());
				}else{
					player.setTimesWorked(0);
				}
					
				
				playersForRole.put(String.valueOf(player.getId()), player);
				knownPlayersSoFar.put(role, playersForRole);
				sobject.setKnownPlayers(knownPlayersSoFar);
				
				//TODO Review: It shouldn't be necessary as this is for simulated connection
				//sobject.getSimSobject().getNetwork().addConnection(player.getId());
				
				
				
				//Add player as peer, if peer queue is not in its max capacity
				
				if(!sobject.getKnownPeers().contains(player.getId())){
					sobject.getExecutor().getSoProtocol().addNewKnownPeer(player.getId());
				}else{
				//System.out.println("[updateKnownPlayersRole](Sobject "+sobject.getAddress()+") ["+TimeUtils.getInstance().getTime()+"] "+(sobject.getKnownPlayersSet().size()* sobject.getKnownPlayers().keySet().size())+" - Player: "+player+" was discarded");
				}
				
				
				//System.out.println("[updateKnownPlayersRole](Sobject "+sobject.getAddress()+") ["+TimeUtils.getInstance().getTime()+"] Player added: "+player); //debug-key
			}
			
			
			
		}
		
	}
	
	public static int getPlayersPerRole(Map<String,Map<String,Player>> knownPlayers, String role){
		return knownPlayers.containsKey(role)?knownPlayers.get(role).keySet().size():0;
	}
	
	public static Player getMinScorePlayer(SObject sobject, Map <String,Map<String,Player>> players){
		Player min=null;
		int maxPlayersRole=0;
		String roleMostPlayers=null;
		int i=0;
		
		
		for(Entry<String,Map<String,Player>> entry : players.entrySet() ){
				
			if (entry.getValue().keySet().size() >= maxPlayersRole ){
				maxPlayersRole = entry.getValue().keySet().size();
				roleMostPlayers = entry.getKey();
			}
		}
		//System.out.println("[getMinScorePlayer]() ["+TimeUtils.getInstance().getTime()+"] Role most players: "+roleMostPlayers); //debug-key
		List<Player> sortedPlayers = new ArrayList<Player>(players.get(roleMostPlayers).values()); 
		Collections.sort(sortedPlayers,new RankingComparator());
		while(i < sortedPlayers.size() && min==null){
			if(sortedPlayers.get(i).getId()!=sobject.getAddress())
				min = sortedPlayers.get(i);
			i++;
		}
		return min;
	}
	
	/**
	 * check if I have the player for another role and if this player has worked so I can register new player with pre existing values
	 * @param sobject
	 * @param newPlayer
	 * @return
	 */
	public static synchronized Player getCommonPlayerDifferentRole(SObject sobject, Player newPlayer){
		Player player = null;
		
		for(Entry<String,Map<String,Player>> entry : sobject.getKnownPlayers().entrySet() ){
			if (entry.getValue().containsKey(String.valueOf(newPlayer.getId())) ){
				player = entry.getValue().get(newPlayer.getId()); 
				break;
			}
		}
		
		
		
		return player;
	}


	public static synchronized void removeFromKnownPlayersPlayer(SObject sobject, int address) {
		Map<String, Player> updatedPlayersRole;
		Map<String, Map<String, Player>> updatedKnownPlayers;

		updatedKnownPlayers = sobject.getKnownPlayers();

		for (Entry<String, Map<String, Player>> eRoles : sobject.getKnownPlayers().entrySet()) {
			if (eRoles.getValue().containsKey(String.valueOf(address))) {
				updatedPlayersRole = updatedKnownPlayers.get(eRoles.getKey());
				updatedPlayersRole.remove(String.valueOf(address));
				updatedKnownPlayers.put(eRoles.getKey(), updatedPlayersRole);
			}
		}
		//TODO: Review
		//implementator.completeRemoveFromKnownPlayersPlayer(sobject,address);
		sobject.setKnownPlayers(updatedKnownPlayers);
	}

	/**
	 * Search for known players for give role
	 * 
	 * @param role
	 * @param excludedPlayers
	 * @return
	 */
	public static synchronized List<Player> queryPlayers(SObject sobject, String role, List<String> excludedPlayers) {
		List<Player> availablePlayers = new ArrayList<Player>();
		boolean addPlayer;

		if (sobject.getKnownPlayers().get(role) != null && !sobject.getKnownPlayers().get(role).isEmpty()) {
			for (Player player : sobject.getKnownPlayers().get(role).values()) {
				addPlayer = true;
				if (excludedPlayers != null && !excludedPlayers.isEmpty())
					for (String excludePlayer : excludedPlayers) {
						if (player.getId() == Integer.parseInt(excludePlayer)) {
							addPlayer = false;
							break;
						}
					}
				if (addPlayer)
					availablePlayers.add(player);
			}
		}
		return availablePlayers;
	}

	public static synchronized void doCommitted(SObject sobject, String playerId, Plan plan) {
		ActivePlayer playerCommitted;
		Map<String, ActivePlayer> startedPlayers = null;
		Map<String,Map<String,ActivePlayer>> pendingPlansExecuted = null;
		// If it is local call (e.g. first simulated trigger)
		if (sobject.getPendingPlansStarted().containsKey(plan.getId())

		) {
			try {
				startedPlayers = sobject.getPendingPlansStarted().remove(plan.getId());
				playerCommitted = startedPlayers.get(playerId);
				playerCommitted.setCommitted(true);
				startedPlayers.put(playerId, playerCommitted);
				
				pendingPlansExecuted = sobject.getPendingPlansExecuted();
				pendingPlansExecuted.put(plan.getId(), startedPlayers);
				
				sobject.setPendingPlansExecuted(pendingPlansExecuted);
				
			} catch (NullPointerException e) {
				System.out.println("[" + TimeUtils.getInstance().getTime() + "] ERROR: SO (" + sobject.getAddress()
						+ ") received msg from SO (" + playerId + ") committing to plan: {" + plan.getId()
						+ "} but does not have player as pending started");
				e.printStackTrace();
			}

		}

	}

	public static synchronized void doSuccessful(SObject sobject, Integer playerId, Plan plan) {
		ActivePlayer playerSucceed=null;
		Map<String, ActivePlayer> startedPlayers=null;
		
		
		if(sobject.getPendingPlansExecuted()!=null && sobject.getPendingPlansExecuted().containsKey(plan.getId())){
			startedPlayers = sobject.getPendingPlansExecuted().remove(plan.getId());
			playerSucceed = startedPlayers.get(String.valueOf(playerId));
		}else{ //Try triggered players
			if(sobject.getPendingPlansStarted()!=null && sobject.getPendingPlansStarted().containsKey(plan.getId())){
				startedPlayers = sobject.getPendingPlansStarted().remove(plan.getId());
				playerSucceed = startedPlayers.get(String.valueOf(playerId));
			}
		}

	
		if(playerSucceed!=null){
			playerSucceed.setSuccessful(true);
			updatePlayerSuccessful(sobject,playerSucceed.getPlayer());
			sobject.getPendingPlansExecuted().put(plan.getId(), startedPlayers);
		}else{
			System.out.println("(Sobject "+sobject.getAddress()+"): ["+TimeUtils.getInstance().getTime()+"] Error in Do successful: Player ("+playerId+") pending to execute not found -> "+startedPlayers);
		}
		
		// TODO increase ranking of player
	}

	public static synchronized void updatePlayerSuccessful(SObject sobject, Player playerSucceed){
		Map<String,Map<String,Player>> knownPlayers=null;
		Map<String,Player> playersRole = null;
		Set<Player> playersUpdate =null;
		knownPlayers = sobject.getKnownPlayers();
		
		for(Entry<String,Map<String,Player>> entry:knownPlayers.entrySet()){
			
			if(entry.getValue().containsKey(String.valueOf(playerSucceed.getId()))){
				playerSucceed = entry.getValue().get(String.valueOf(playerSucceed.getId()));
				playerSucceed.setTimesWorked(playerSucceed.getTimesWorked()+1);
				if(playersUpdate==null)
					playersUpdate = new HashSet<Player>();
				playersUpdate.add(playerSucceed);
			}
		}
		
		if(playersUpdate!=null && playersUpdate.size()>0){
			for(Player pToUpdate:playersUpdate){
				playersRole = knownPlayers.get(pToUpdate.getRole());
				playersRole.put(String.valueOf(pToUpdate.getId()), pToUpdate);
				knownPlayers.put(pToUpdate.getRole(), playersRole);
			}
			sobject.setKnownPlayers(knownPlayers);
		}
		
	}
	
	
	/**
	 * Decides if execute the activity and if so, schedules it
	 * 
	 * @param sobject
	 * @param requester
	 * @param plan
	 * @return
	 */
	public static synchronized boolean doExecute(SObject sobject, String requester, Plan plan, int deadline) {

		//TODO define mechanism to check deadline that works in RW and Sim
		if (deadline > 0 && // I am on time
				true // Sim
																													// reasoning
																													// to
																													// execute
		) {
			sobject.getExecutionQueue().add(plan);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Execute activity
	 * 
	 * @param sobject
	 * @param plan
	 * @return
	 */
	public static synchronized void execute(SObject sobject, Plan plan) { // ;
																			// TO
																			// INCLUDE
																			// ALGORTITHM

		Map<String, Plan> updatedPendingPlans;
		boolean startScenario = false;
		boolean endScenario = false;
		int existingPrevious;
		
		if(plan.getNextStep()==0) startScenario = true;
		
		String currentActivity = plan.getActivity("exc:" + sobject.getAddress());
		plan = setNextActivity(plan); // Plan with next activity
		existingPrevious = plan.getPreviousResponsible();
		plan.setPreviousResponsible(sobject.getAddress());

		if (plan.getNextStep() > 0) {
			sobject.getPendingPlansTrigger().put(plan.getId(), plan);
		} else {
			updatedPendingPlans = sobject.getPendingPlansTrigger();
			updatedPendingPlans.remove(plan.getId());
			sobject.setPendingPlansTrigger(updatedPendingPlans);
			endScenario = true;
		}
		
		sobject.getExecutor().simExecuteActivity(currentActivity, startScenario, endScenario, plan.getId(), plan.getScenarioId(),plan.getQueryTime());
		
		
		if(existingPrevious!=sobject.getAddress())
			sobject.getExecutor().getSoProtocol().ackExecuted(plan, currentActivity,existingPrevious);
		else{
			//create dummy player just to pass id and role so it can be retrieved from knownPlayers
			updatePlayerSuccessful(sobject,new Player(sobject.getAddress(),sobject.getExecutor().lookUpRole(currentActivity),-5,-5));
		}
			
			
		
	}

	public static synchronized Plan setNextActivity(Plan plan) {
		if (plan.getNextStep() < plan.getSteps().size() - 1) {
			plan.setNextStep(plan.getNextStep() + 1);
		} else {
			plan.setNextStep(-1);
			// stage = 1; //Start new scenario
		}
		return plan;
	}

	/**
	 * Check players that have been started and have not completed task yet. If
	 * timeout is over (current tick greater than limit by more than the global
	 * limitTick (usually 1), it will look for another player
	 * 
	 * @return
	 */
	public static synchronized void checkStartedPlayers(SObject sobject) {

		List<String> toRemove = new ArrayList<String>();
		Map<String, Map<String, ActivePlayer>> updateStartedPlayers = new HashMap<String, Map<String, ActivePlayer>>();
		Collection<ActivePlayer> listStarted;
		ActivePlayer mostRecentActivePlayer;
		boolean doSearch;

		for (Entry<String, Map<String, ActivePlayer>> entry : sobject.getPendingPlansStarted().entrySet()) {
			String planId = entry.getKey();
			Map<String, ActivePlayer> pendingPlayers = entry.getValue();

			listStarted = (Collection<ActivePlayer>) pendingPlayers.values();

			mostRecentActivePlayer = Collections.max(listStarted);
			if ((TimeUtils.getInstance().getTime() - mostRecentActivePlayer.getLimit()) > limitTimePlayerStarted ) {
				toRemove.add(planId);
				sobject.getPendingPlansTrigger().put(planId, mostRecentActivePlayer.getPlan());
			}
		}

		for (String removeId : toRemove) {
			sobject.getPendingPlansStarted().remove(removeId);
		}
		toRemove.clear();
		toRemove = null;
		sobject.getExecutor().setPendingCheck(true);
	}

	public static String getNextActivity(Plan plan) {
		return plan.getSteps().get(plan.getNextStep());
	}

	/**
	 * Executes committed activities. Executes everything in each call. TODO Set
	 * to execute a max number of activities in each call
	 * 
	 * @param sobject
	 */
	public static synchronized void executePendingActivities(SObject sobject) {
		Plan executingPlan;
		Queue<Plan> pendingQueue = sobject.getExecutionQueue();
		List<Object> args;
		
		while (!pendingQueue.isEmpty()) {
			executingPlan = pendingQueue.remove();
			args=new ArrayList<Object>();
			args.add(executingPlan);// Brand new object with activity to execute
			sobject.getExecutor().simulateTask("" + getNextActivity(executingPlan),args ); 
		}

		sobject.setExecutionQueue(pendingQueue);

	}

	public static synchronized void triggerNextPlayer(SObject sobject) { // Pending
																			// to
																			// include
																			// plan
		String planId;
		Plan plan;
		Player nextPlayer;
		Map<String, ActivePlayer> startedPlayers;
		Collection<Player> availablePlayers;
		String reqRole;
		String activity = null;
		int attempts = 0;
		int i;
		boolean triggerPlan = false;
		List<String> toRemove = new ArrayList<String>();
		boolean showLogs = false;
		String msgId;

			if(showLogs) System.out.println("(Sobject " + sobject.getAddress() + "): Pending plans: " + sobject.getPendingPlansTrigger());
		for (Entry<String, Plan> entry : sobject.getPendingPlansTrigger().entrySet()) {
			planId = entry.getKey();
			plan = entry.getValue();
			nextPlayer = null;
			msgId = null;
			activity = plan.getActivity("E:" + sobject.getAddress());
			if (activity == null) {
				toRemove.add(planId);
				continue;
			}

			reqRole = sobject.getExecutor().lookUpRole(activity);
			
			//This role becomes relevant for the SO
			sobject.getRelevantRoles().add(reqRole);

			triggerPlan = sobject.doesTriggerNow(reqRole); // If I am not
															// querying for this
															// role yet

			if (sobject.getAttemptedPlans().get(planId) != null) {
				attempts = sobject.getAttemptedPlans().get(planId).intValue();
			}else{
				attempts = 0;
			}
			attempts++;
			sobject.getAttemptedPlans().put(planId, Integer.valueOf(attempts));

			 if(showLogs) System.out.println("["+TimeUtils.getInstance().getTime()+"](Sobject" + sobject.getAddress() + "): TRigger plan " + triggerPlan);
			if (triggerPlan) {

				i = 0;

				if (sobject.getKnownPlayers().get(reqRole) != null && !sobject.getKnownPlayers().get(reqRole).isEmpty()) {
					availablePlayers = sobject.getKnownPlayers().get(reqRole).values();
					if (availablePlayers != null && !availablePlayers.isEmpty()) {
						if (!sobject.getPendingPlansStarted().containsKey(planId)) { // I have not started this plan before
							startedPlayers = new HashMap<String, ActivePlayer>();
							sobject.getPendingPlansStarted().put(planId, startedPlayers);
						}

						startedPlayers = sobject.getPendingPlansStarted().get(planId);
						while (i < availablePlayers.size()) {
							nextPlayer = sobject.getExecutor().selectPlayer(availablePlayers, i);
							if ((startedPlayers.containsKey(String.valueOf(nextPlayer.getId()))
									&& startedPlayers.get(String.valueOf(nextPlayer.getId())).getPlan()
											.getNextStep() == plan.getNextStep())
									|| !sobject.getExecutor().isConnected(nextPlayer.getId())
							// The player might have been unavailable for a
							// previous step but not to this
							) {
								i++;
								nextPlayer = null;
							} else {
								i = availablePlayers.size();
							}
						}
						if (nextPlayer != null) {
							if(showLogs)System.out.println("A (Sobject " + sobject.getAddress() + "): not null: " + nextPlayer);//debug-key
							
							
							ActivePlayer nextAPlayer = new ActivePlayer(nextPlayer, new Plan(plan),
									TimeUtils.getInstance().getTime() + TimeUtils.getInstance().getParamTimeLimit());

							if (nextPlayer.getId() != sobject.getAddress()) {
								sobject.getExecutor().getSoProtocol().requestExecute(nextAPlayer);
								// Following tables are only for control of
								// responses, so not required in local calls
								startedPlayers.put(String.valueOf(nextPlayer.getId()), nextAPlayer);
								sobject.getPendingPlansStarted().put(planId, startedPlayers);
								if(showLogs)System.out.println("(Sobject "+sobject.getAddress()+"): ["+TimeUtils.getInstance().getTime()+"] Triggered Player: just added:"+  nextAPlayer + " pendingStarted: "+sobject.getPendingPlansStarted());
							} else { // I am the selected player
								// By default it is committed to self activities
								sobject.getExecutionQueue().add(plan); 

							}
							toRemove.add(planId);
						} else {
							if(showLogs) System.out.println("B (Sobject " + sobject.getAddress() + ")["+TimeUtils.getInstance().getTime()+"]: player not found will query"); //debug-key
							if (attempts > TimeUtils.getInstance().getParamMaxTimeout()) {
								toRemove.add(planId);
								sobject.getExecutor().doUnachievableScenario(
										"After " + attempts + "_ still_don't know any player for plan: " + planId); // Unable to get a player for this role after n attempts
								sobject.getQueryingRoles().remove(reqRole); //Enable future queries of the rolefor other scenarios/plans
								sobject.getAttemptedPlans().put(planId, 0);
							} else {

								if (!sobject.getQueryingRoles().contains(reqRole)) { // I
																						// am
																						// not
																						// currently
																						// querying
																						// this
																						// role

									Set<String> excludedPlayers = null;
									if (startedPlayers != null && !startedPlayers.isEmpty())
										excludedPlayers = startedPlayers.keySet();
									msgId = sobject.getExecutor().getSoProtocol().query(reqRole, excludedPlayers);
									startQueryTime(sobject,plan,TimeUtils.getInstance().getTime(), msgId );
								}

							}
						}
						if (startedPlayers.isEmpty())
							sobject.getPendingPlansStarted().remove(planId);

					}
				} else {
					
					if(showLogs) {
						System.out.println("(Sobject " + sobject.getAddress() + ")["+TimeUtils.getInstance().getTime()+"]: Plan: "+plan);
						System.out.println("(Sobject " + sobject.getAddress() + ")["+TimeUtils.getInstance().getTime()+"]: Attempts: "+attempts+" timeout:"+TimeUtils.getInstance().getParamMaxTimeout());
					}
					if (attempts > TimeUtils.getInstance().getParamMaxTimeout()) {
						toRemove.add(planId);
						sobject.getExecutor().doUnachievableScenario("* I didn't get any player for plan: " + planId); // Unable
						// to
						// get
						// a
						// player
						// for
						// the
						// role
						// after
						// n
						// attempts
						sobject.getQueryingRoles().remove(reqRole); // Enable
																	// future
																	// queries
																	// of the
																	// role for
																	// other
																	// scenarios
						sobject.getAttemptedPlans().put(planId, 0);
					} else {
						if (!sobject.getQueryingRoles().contains(reqRole)) { // I am not currently querying this role, redundant with triggerPlan!
							msgId = sobject.getExecutor().getSoProtocol().query(reqRole);
							startQueryTime(sobject,plan,TimeUtils.getInstance().getTime(), msgId );

						}

					}
				}
			} else {
				if (attempts > TimeUtils.getInstance().getParamMaxTimeout()) {
					toRemove.add(planId);
					sobject.getExecutor().doUnachievableScenario(
							"** I didn't get any player for role:" + reqRole + " for plan:" + planId); // Unable
					// to
					// get
					// a
					// player
					// for
					// the
					// role
					// after
					// n
					// attempts
					sobject.getQueryingRoles().remove(reqRole.trim()); // Enable future
																// queries of
																// the role for
																// other
																// scenarios
					sobject.getAttemptedPlans().put(planId, 0);
				}
			}

		}

		for (String keyRemove : toRemove) {
			sobject.getPendingPlansTrigger().remove(keyRemove);
		}

		sobject.getExecutor().setPendingTrigger(true);
	}
	
	
	public static synchronized void startQueryTime(SObject sobject, Plan plan,int timeStart, String msgId ){
		boolean showLogs = false;
		
		
		if(showLogs)
		System.out.println("(Sobject " + sobject.getAddress() + ")["+TimeUtils.getInstance().getTime()+"]: Started Query Time Plan: "+plan+" - timeStart: "+timeStart+" - MsgId:"+msgId);
		
		Map<String, Plan> qp = sobject.getExecutor().getQueriesPlans();
		if(qp==null)
			qp = new HashMap<String,Plan>();
		qp.put(msgId, plan);
		sobject.getExecutor().setQueriesPlans(qp);
	}

	/**
	 * 
	 * @param sobject
	 * @param rolesToRemove
	 */
	public static synchronized void setRolesInactive(SObject sobject, List<String> rolesToRemove) {
		boolean removed;
		for (String roleToRemove : rolesToRemove) {
			removed = false;
			removed = sobject.getMyRoles().remove(roleToRemove);
			if (removed)
				sobject.getInactiveRoles().add(roleToRemove);
		}

	}

	public static synchronized void setCapabilitiesInactive(SObject sobject, List<String> capabilitiesToRemove) {
		boolean removed;

		for (String capabilityToRemove : capabilitiesToRemove) {
			removed = false;
			removed = sobject.getMyCapabilities().remove(capabilityToRemove);
			if (removed) {
				sobject.getInactiveCapabilities().add(capabilityToRemove);
			}
		}

		updateSORoles(sobject);

	}

	public static synchronized void setCapabilitiesActive(SObject sobject, List<String> capabilitiesToAdd) {
		boolean removed;
		for (String capabilityToAdd : capabilitiesToAdd) {
			removed = false;
			removed = sobject.getInactiveCapabilities().remove(capabilityToAdd);
			if (removed) {
				sobject.getMyCapabilities().add(capabilityToAdd);
			}
		}

		updateSORoles(sobject);
	}

	public static synchronized void setAllCapabilitiesActive(SObject sobject, List<String> capabilitiesToAdd) {
		setCapabilitiesActive(sobject, sobject.getInactiveCapabilities());
	}

	public static synchronized void setAllCapabilitiesInactive(SObject sobject, List<String> capabilitiesToRemove) {
		setCapabilitiesActive(sobject, sobject.getMyCapabilities());
	}

	@SuppressWarnings("unchecked")
	public static synchronized void updateSORoles(SObject sobject) {
		boolean playable;
		for (Entry<String, List<String>> entryRole : (EntityUtils.getInstance().getParamRoles())
				.entrySet()) {
			playable = true;
			for (String responsability : entryRole.getValue()) {
				if (!sobject.getMyCapabilities().contains(responsability)) {
					playable = false;
					break;
				}
			}
			if (playable)
				sobject.getMyRoles().add(entryRole.getKey());
		}

	}

}
