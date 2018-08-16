package org.multiot.em4so.protocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.multiot.em4so.behaviour.RolePlayerServices;
import org.multiot.em4so.config.GlobalParameters;
import org.multiot.em4so.model.Message;
import org.multiot.em4so.model.Player;
import org.multiot.em4so.model.SObject;
import org.multiot.em4so.utils.EntityUtils;
import org.multiot.em4so.utils.TimeUtils;





public class GnutellaSOProtocol extends SOProtocol {
	
public GnutellaSOProtocol(SObject sobject, CommunicationServices network){
	this.sobject = sobject;
//	recordSent = new ArrayList<String>();
//	sentQueries = new HashMap<String,Message>();
	processedMsgs = new HashMap<String,Message>();
//	pendingHitRoles = new HashMap<String,Integer>();
	pendingMsgExecuted = new HashMap<String,Message>();
	this.network = network;
}

public synchronized String query(String role, Collection<String> excluded) {
	List<Object> args = new ArrayList<Object>();
	sobject.getQueryingRoles().add(role.trim());
	args.add(sobject.getAddress());
	args.add(role);
	if(excluded!=null)
		args.add(excluded);
	
	return query("0",sobject.getAddress(),args,GlobalParameters.getBaseTTL()+1);
	
	
}

@Override
public synchronized String query(String role) {
	return query(role,null);
}
/**
 * Query if I have received the message
 * @param role
 * @param msg
 */
public synchronized String query(String msgId, int sender, List<Object> args, int TTL) {
	int newTTL = (TTL-1)>0?(TTL-1):0;
//	System.out.println("(Sobject "+sobject.getAddress()+"): ["+SimUtils.getTime()+"] msgId:"+msgId+"  newTTL:"+newTTL+" sender:"+sender+" - args:"+args);
	if ( newTTL > 0)
		msgId = sendMsgPeers(msgId,sender, QUERY, args, newTTL);
	return msgId;
	
}

/**
 * Send message to connected peers
 * @param msgId
 * @param sender
 * @param type
 * @param args
 * @param TTL
 */
public synchronized String sendMsgPeers(String msgId,int sender, String type, List<Object> args, int TTL){
//	if(type.equals(QUERY)){ //debug-key
//		System.out.println("(Sobject "+sobject.getAddress()+"): ["+SimUtils.getTime()+"] msgId:"+msgId+" sender:"+sender+" args: "+args+" queried with TTL:"+TTL+": connections:"+network.getConnections());
//		System.out.println("(Sobject "+sobject.getAddress()+"): ["+SimUtils.getTime()+"]: knownPlayers:"+sobject.getKnownPlayersSet());
//		System.out.println("(Sobject "+sobject.getAddress()+"): ["+SimUtils.getTime()+"]: knownPeers:"+sobject.getKnownPeers());
//	
//	}

	Integer peer;
	Iterator<Integer> it = sobject.getKnownPeers().iterator();
	int i=0;
	String commonMsgId = null;
	while (it.hasNext()){
	
		synchronized(this){
			peer = it.next();
			if(((Integer)args.get(0)).intValue() != peer.intValue() 
					&& sobject.getAddress() != peer.intValue() 
					&& sender!=peer.intValue()
					) //Do not send msg to originator nor itself!
			if(sobject.getExecutor().isConnected(peer.intValue())){
//				if(i==0 ){
					msgId = network.sendMsg(Integer.parseInt(msgId), sobject.getAddress(),peer.intValue(),type, args,TTL,GlobalParameters.getMsgMediumPriority());
//				}else {
//					network.sendMsg(msgId, sobject.getAddress(),peer.intValue(),type, args,TTL,NetworkSimulator.mediumPriority);
//				}
//				if(type.equals(QUERY)){
//					System.out.println("(Sobject "+sobject.getAddress()+"): ["+SimUtils.getTime()+"]: Message ("+msgId+") "+type+" Sent to: "+peer);
//				}
			}else{
				removeKnownPeer(peer);
				RolePlayerServices.removeFromKnownPlayersPlayer(sobject, peer);
				
			}
			i++;
		}
	
	}
	return msgId;
}




@SuppressWarnings("unchecked")
public synchronized void  doQueryHit (Message msg){
	
	Message originalQueryMsg=null;
	int newTTL;
	String searchKey;
	
	for(Entry<String,Message> entry:processedMsgs.entrySet()){
//		System.out.println("(Sobject "+sobject.getAddress()+"): ["+SimUtils.getTime()+"] processedMsg {"+entry.getKey()+"}:"+entry.getValue());
		searchKey = msg.getId()+"-"+QUERY+"-";
//		System.out.println("(Sobject "+sobject.getAddress()+"): ["+SimUtils.getTime()+"] searchKey: ->"+searchKey+"<-");
		if(entry.getKey().startsWith(searchKey)){
			originalQueryMsg = entry.getValue();
			break;
		}
	}
	
//	System.out.println("(Sobject "+sobject.getAddress()+"): ["+SimUtils.getTime()+"] original:"+originalQueryMsg+"--queryhit msg: "+msg);
	if(originalQueryMsg==null){ //Its my query
		// Save the players for me
//		def reqRole = sentQueries[msg.id]
		RolePlayerServices.updateKnownPlayersRole(sobject, ((String)msg.getArgs().get(1)).trim(),((List<Player>)msg.getArgs().get(2)));
		
		//System.out.println("removing: "+msg.getArgs().get(1)+"--from--"+sobject.getQueryingRoles()+"-->"+sobject.getKnownPlayers().get(((String)msg.getArgs().get(1)).trim()));
		sobject.getQueryingRoles().remove(((String)msg.getArgs().get(1)).trim());
	}else{ //I am intermediate node so need to send msg back
		newTTL = msg.getTTL()-1;
		if(newTTL>0)
			network.sendMsg(Integer.parseInt(msg.getId()), sobject.getAddress(),originalQueryMsg.getSender(),QUERYHIT, msg.getArgs(),newTTL,GlobalParameters.getMsgMediumPriority());
		
	}
	
}


@SuppressWarnings("unchecked")
public synchronized void  doQuery (Message msg){

	List<String> excludedList;
	HashMap<String,List<String>> argeList = null;
	List<Object> args;
	int newTTL ;
	
	if(msg.getArgs().size()>2){
		//argeList = (HashMap<String,List<String>>)msg.getArgs().get(2);
		excludedList = new ArrayList<String>();
		excludedList.addAll((Set<String>)msg.getArgs().get(2));
		System.out.println("(Sobject "+sobject.getAddress()+"): ["+TimeUtils.getInstance().getTime()+"] excludedList: "+excludedList);
	}else
		excludedList = null;
	
	List<Player> availablePlayers = RolePlayerServices.queryPlayers(sobject,(String)msg.getArgs().get(1), excludedList);
	
	//System.out.println("(Sobject "+sobject.getAddress()+"): ["+SimUtils.getTime()+"] available: "+availablePlayers);
	if(!availablePlayers.isEmpty()  
		){  //If I found players myself I don't ask others
		newTTL = msg.getTTL() - 1;
		newTTL = GlobalParameters.getBaseTTL() - newTTL; 
		args = new ArrayList<Object>();
		args.add(sobject.getAddress());
		args.add(msg.getArgs().get(1)); //Role
		args.add(availablePlayers);
		network.sendMsg(Integer.parseInt(msg.getId()), sobject.getAddress(),msg.getSender(),QUERYHIT,args,newTTL,GlobalParameters.getMsgMediumPriority());
	}else{
		query(msg.getId(),msg.getSender(),msg.getArgs(),msg.getTTL());
	}
}




/**
 * Send back update of current information to known peers
 * @param msg
 * @return
 */
@SuppressWarnings("unchecked")
public synchronized void doPong(Message msg){
	Message originalPingMsg=null;
	int newTTL;
	
	for(Entry<String,Message> entry:processedMsgs.entrySet()){
		if(entry.getKey().startsWith(msg.getId()+"-"+PING)){
			originalPingMsg = entry.getValue();
			break;
		}
	}
	
	newTTL = (msg.getTTL()-1)>0?(msg.getTTL()-1):0;
	
	//System.out.println("(Sobject "+sobject.getAddress()+"): ["+SimUtils.getTime()+"] Original ping msg: "+originalPingMsg);
	
	if(originalPingMsg!=null)//I am intermediate node
		network.sendMsg(Integer.parseInt(msg.getId()), sobject.getAddress(),originalPingMsg.getSender(),PONG, msg.getArgs(),newTTL,GlobalParameters.getMsgMediumPriority());
	else{//; I am target node

		if (!sobject.getKnownPeers().contains(((Integer)msg.getArgs().get(0)).intValue())){
			addNewKnownPeer(((Integer)msg.getArgs().get(0)).intValue());
			
			//TODO Review: It shouldn't be necessary as this is for simulated connection
			//network.addConnection(msg.getArgs().get(0)); // msg.args[0] : responder
			network.completeDoPong(((Integer)msg.getArgs().get(0)).intValue());
		}
	}
	
}

@SuppressWarnings("unchecked")
public synchronized void doPing(Message msg){
	List<Object> args = new ArrayList<Object>();
	int newTTL;
	boolean showLog = false;
//	if(this.sobject.getAddress()==11||this.sobject.getAddress()==12||this.sobject.getAddress()==13||this.sobject.getAddress()==15) showLog = true;
	//Send pong back to ping sender
	//Args includes also who as this msg will be passed on from node to node,varying the sender but keeping original pong replier
	
	args.add(sobject.getAddress());
	args.add(sobject.getMyRoles());
	
	newTTL = (msg.getTTL()-1)>0?(msg.getTTL()-1):0;
	
//	RolePlayerServices.updateKnownPlayersPlayer(sobject,((Integer)msg.getArgs().get(0)).intValue(),(List<String>)msg.getArgs().get(1));
	if(showLog)System.out.println("ping add peer:"+msg);
	addNewKnownPeer(((Integer)msg.getArgs().get(0)).intValue());
	
//	System.out.println("(Sobject "+sobject.getAddress()+"): doPing updated: "+msg.getArgs().get(0)+" for roles: "+ (List<String>)msg.getArgs().get(1));
	
	if(		Integer.valueOf(
				network.sendMsg(Integer.parseInt(msg.getId()), sobject.getAddress(),msg.getSender(),PONG, args,GlobalParameters.getBaseTTL() - newTTL,GlobalParameters.getMsgMediumPriority())
			) < 0
	){
		removeKnownPeer(msg.getSender());
		RolePlayerServices.removeFromKnownPlayersPlayer(sobject, msg.getSender());
	}
	
	
	if ( newTTL > 0)
		sendMsgPeers(msg.getId(),msg.getSender(), PING, msg.getArgs(), newTTL);	
}


}
