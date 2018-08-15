package org.multiot.em4so.protocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.multiot.em4so.behaviour.RolePlayerServices;
import org.multiot.em4so.config.GlobalParameters;
import org.multiot.em4so.model.ActivePlayer;
import org.multiot.em4so.model.Message;
import org.multiot.em4so.model.Plan;
import org.multiot.em4so.model.SObject;
import org.multiot.em4so.utils.EntityUtils;
import org.multiot.em4so.utils.TimeUtils;



public abstract class SOProtocol {
	protected final static String COMMITTED = "committed";
	protected final static String EXECUTE = "execute";
	protected final static String SUCCEED = "succeed";
	protected final static String PONG = "pong";
	protected final static String PING = "ping";
	protected final static String QUERY = "query";
	protected final static String QUERYHIT = "queryHit";
	protected SObject sobject;
	protected CommunicationServices network;

	protected Map<String, Message> pendingMsgExecuted;

	/**
	 * Message processed from me for each msg-id
	 */
	protected Map<String, Message> processedMsgs;

	
	
	public Map<String, Message> getPendingMsgExecuted() {
		return pendingMsgExecuted;
	}

	public void setPendingMsgExecuted(Map<String, Message> pendingMsgExecuted) {
		this.pendingMsgExecuted = pendingMsgExecuted;
	}

	public Map<String, Message> getProcessedMsgs() {
		return processedMsgs;
	}

	public void setProcessedMsgs(Map<String, Message> processedMsgs) {
		this.processedMsgs = processedMsgs;
	}

	/**
	 * Register/Update in the network with played roles
	 */
	public final void register(int addressRegistry) {
		
		ping(addressRegistry, GlobalParameters.getBaseTTL());

	}
	
	public final void ping(int address, int ttl) {
		List<Object> args = new ArrayList<Object>();

		args.add(sobject.getAddress());
		args.add(sobject.getMyRoles());
		args.add(sobject.getMyLocation());

		int result = Integer.parseInt(
				network.sendMsg(0, sobject.getAddress(), address, PING, args, ttl, 3)
			);
		
			//System.out.println("result of ping> from: "+sobject.getAddress()+" to "+address+" => "+result);
			if(result		< 0){
				RolePlayerServices.removeFromKnownPlayersPlayer(sobject, address);
				removeKnownPeer(address);
			}
		

	}

	/**
	 * Sobject queries for players of role
	 * 
	 * @param role
	 */
	public abstract String query(String role);

	/**
	 * query avoiding excluded list
	 * 
	 * @param role
	 * @param excludedPlayers
	 */
	public abstract String query(String role, Collection<String> excludedPlayers);

	/**
	 * Process a received message on the input sobject and returns it after
	 * processing
	 * 
	 * @param sobject
	 * @param msg
	 */
	public final synchronized void processMsg(Message msg) {
		
		//if(sobject.getAddress()==36 && SimUtils.getTime()>220)System.out.println("(Sobject "+sobject.getAddress()+"): ["+SimUtils.getTime()+"] ProcessingMsg: Before knownPlayers->"+sobject.getKnownPlayers());
		
		
		if (sobject.getExecutor().isConnected(sobject.getAddress())) {
			
			
			String processedKey = null;

			if (msg.getArgs() == null) {
				processedKey = msg.getId() + msg.getMsg();
			} else {
				processedKey = msg.getId() + "-" + msg.getMsg() + "-" + msg.getArgs().get(0);
			}

			if (!processedMsgs.containsKey(processedKey)) { // Avoid processing
															// same msg again
															// coming from other
															// peer
				processedMsgs.put(processedKey, msg);
		
//				if((msg.getMsg().equals(QUERYHIT)
//					||msg.getMsg().equals(QUERY)
//					||msg.getMsg().equals(SUCCEED)
//					||msg.getMsg().equals(COMMITTED)
//					||msg.getMsg().equals(EXECUTE)
//					)
//						){
//						 System.out.println("(Sobject "+sobject.getAddress()+"): ["+SimUtils.getTime()+"] ProcessingMsg:"+msg);//debug-key
//					 }

				switch (msg.getMsg()) {
				case PING:
					doPing(msg);
					break;
				case PONG:
					doPong(msg);
					break;
				case QUERY:
					doQuery(msg);
					break;
				case QUERYHIT:
					doQueryHit(msg);
					break;
				case EXECUTE:
					doExecute(msg);
					break;
				case COMMITTED:
					doCommitted(msg);
					break;
				case SUCCEED:
					doSuccessful(msg);
					break;
				default:
					doProcessSpecificMessage(msg);
					break;
				}
			}
		}
		
//		if(sobject.getAddress()==91 && SimUtils.getTime()>167)System.out.println("(Sobject "+sobject.getAddress()+"): ["+SimUtils.getTime()+"] ProcessingMsg: After knownPlayers->"+sobject.getKnownPlayers());
	}

	public abstract  void doPing(Message msg);

	public abstract void doPong(Message msg);

	public abstract void doQuery(Message msg);

	public abstract void doQueryHit(Message msg);

	public synchronized void doProcessSpecificMessage(Message msg) {
		System.out.println("Message Not Supported");
	}

	public synchronized void requestExecute(ActivePlayer activePlayer) {
		List<Object> args = new ArrayList<Object>();

		args.add(sobject.getAddress()); // Who
		args.add(activePlayer.getPlan()); // What
		args.add(activePlayer.getLimit()); // When

		if(	Integer.parseInt(
								network.sendMsg(0, sobject.getAddress(), activePlayer.getPlayer().getId(), EXECUTE, args)
							)	< 0){
			RolePlayerServices.removeFromKnownPlayersPlayer(sobject, activePlayer.getPlayer().getId());
		}
	}

	public synchronized void doExecute(Message msg) {
		List<Object> args;
		boolean toExecute = false;
		Plan plan = (Plan) msg.getArgs().get(1);
		String requester = String.valueOf(msg.getArgs().get(0));
		String keyPendingExecutionMsg = null;
		boolean showLog = false;
		if(this.sobject.getAddress()==11||this.sobject.getAddress()==12||this.sobject.getAddress()==13||this.sobject.getAddress()==15) showLog = true;
		
		toExecute = RolePlayerServices.doExecute(sobject, requester, plan, ((Integer) msg.getArgs().get(2)).intValue());
		if (toExecute && msg.getSender() != sobject.getAddress()) {
			args = new ArrayList<Object>();
			args.add(sobject.getAddress());
			args.add(plan);
			keyPendingExecutionMsg = plan.getId() + "." + plan.getActivity("B:"+sobject.getAddress()) + "." + plan.getNextStep() + "."	+ plan.getPreviousResponsible();
			pendingMsgExecuted.put(keyPendingExecutionMsg, msg);
			//System.out.println("(Sobject "+sobject.getAddress()+"): ["+SimUtils.getTime()+"]: pending key in doExecute:->"+keyPendingExecutionMsg+"<-");//debug-key
			
			if(		Integer.parseInt(					
						network.sendMsg(Integer.parseInt(msg.getId()), sobject.getAddress(), msg.getSender(), COMMITTED, args, 1, 3)
					) < 0){
				
				RolePlayerServices.removeFromKnownPlayersPlayer(sobject, msg.getSender());
			
			}else{
				//Add requester as peer
				
				addNewKnownPeer(Integer.parseInt(requester));
			}
			
			
			
		}
	}

	public synchronized void doCommitted(Message msg) {
		RolePlayerServices.doCommitted(sobject, String.valueOf(msg.getArgs().get(0)), (Plan) msg.getArgs().get(1));
	}

	public synchronized void ackExecuted(Plan plan, String currentActivity, int previous) {
		List<Object> args = new ArrayList<Object>();
		Message originalMsg;
		String keyPendingExecutionMsg = null;
		
		if(plan.getNextStep()>0){
			keyPendingExecutionMsg = plan.getId() + "." +  currentActivity + "."+(plan.getNextStep()-1) +"."	+ previous;
		}else{
			keyPendingExecutionMsg = plan.getId() + "." +  currentActivity + "."+(plan.getSteps().size()-1) +"."	+ previous;
		}
			

		args.add(sobject.getAddress()); // Who
		args.add(plan); // What
		args.add(TimeUtils.getInstance().getTime()); // When
		
//		System.out.println("(Sobject "+sobject.getAddress()+"): ["+SimUtils.getTime()+"]: pending key in ack:->"+keyPendingExecutionMsg+"<-");
//		System.out.println("(Sobject "+sobject.getAddress()+"): ["+SimUtils.getTime()+"]: pending executed in ack:"+pendingMsgExecuted);
		originalMsg = pendingMsgExecuted.get(keyPendingExecutionMsg);
		
		if(		Integer.parseInt(
				network.sendMsg(Integer.parseInt(originalMsg.getId()), sobject.getAddress(), originalMsg.getSender(), SUCCEED, args,1,4)
				) < 0
		){
			RolePlayerServices.removeFromKnownPlayersPlayer(sobject, Integer.valueOf(plan.getPreviousResponsible()));
		}
		// TODO send back notification of scenario done to initiator
	}

	public void doSuccessful(Message msg) {
		RolePlayerServices.doSuccessful(sobject, (Integer) msg.getArgs().get(0), (Plan) msg.getArgs().get(1));
	}

	public void addNewKnownPeer(int newPeer){
		Deque<Integer> knownPeers = sobject.getKnownPeers();
		Iterator<Integer> it =null;
		int i =0, peer, peerToRemove = -1;
		boolean showLog = false;
//		if(this.sobject.getAddress()==11||this.sobject.getAddress()==12||this.sobject.getAddress()==13||this.sobject.getAddress()==15) showLog = true;
		
		if(!sobject.getKnownPeers().contains(newPeer)){
		
			if(knownPeers.size()<(Integer)this.sobject.getMaxKnownPeers()){
				knownPeers.addLast(newPeer);
				if(showLog)System.out.println("(Sobject "+sobject.getAddress()+"): ["+TimeUtils.getInstance().getTime()+"]: peer:"+newPeer+" added");
			}else{
				it = knownPeers.iterator();
				if(sobject.isAddNewPeer()){
					while(it.hasNext()){
						peer = it.next();
						//Removes the first peer in the list of peers if this is not in the player cache
						if (i != 0 && !sobject.getKnownPlayersSet().contains(String.valueOf(peer)) ){
							peerToRemove = peer;
							break;
						}
					i++;
					}
				}
				sobject.setAddNewPeer(!sobject.isAddNewPeer());
				if(peerToRemove!=-1){ 
					knownPeers.remove(peerToRemove);
					knownPeers.addLast(newPeer);
					if(showLog)System.out.println("(Sobject "+sobject.getAddress()+"): ["+TimeUtils.getInstance().getTime()+"]: players: "+sobject.getKnownPlayersSet()+" peer:"+peerToRemove+" removed");
					if(showLog)System.out.println("(Sobject "+sobject.getAddress()+"): ["+TimeUtils.getInstance().getTime()+"]: peer:"+newPeer+" added");
				}else{
					if(showLog)System.out.println("(Sobject "+sobject.getAddress()+"): ["+TimeUtils.getInstance().getTime()+"]: peer:"+newPeer+" NO added");
					//else peer is discarded to keep as peers the ones it cooperates with
				}
			}
			sobject.setKnownPeers(knownPeers);
		}
		
	}

	public void removeKnownPeer(int peerToRemove){
		Deque<Integer> knownPeers = sobject.getKnownPeers();
		knownPeers.remove(peerToRemove);
		sobject.setKnownPeers(knownPeers);
	}

	
}
