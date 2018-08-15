package org.multiot.em4so.protocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.multiot.em4so.behaviour.RankingComparator;
import org.multiot.em4so.behaviour.RolePlayerServices;
import org.multiot.em4so.model.Message;
import org.multiot.em4so.model.Player;


public class HierarchicalP2PProtocol extends SOProtocol{
	
	@Override
	public String query(String role) {
		// TODO Auto-generated method stub
		return null;
		
	}
	@Override
	public String query(String role, Collection<String> excludedPlayers) {
		// TODO Auto-generated method stub
		return null;
	}
	


	
	
	@SuppressWarnings("unchecked")
	public void doPing(Message msg){
		List<Object> args = new ArrayList<Object>();
		List<String> newcomerRoles = (List<String>)msg.getArgs().get(1);
		Player rolePointer=null;
		Map<String, Map<String, Player>> substitutes=null; 
		Map<String, Map<String, Player>> complementaries=null;
		 Map<String, Player> complementaryPointer=null;
		Set<String> rolePointersSend=null;
		int pointerIndex = 0;
		boolean commonRole = false;
		boolean recordNewcomer = false;
		String compId = null;
		
		//check if we share roles and prepare to send all info I have of substitute players
		for(String itsRole:newcomerRoles){
			for(String myRole:sobject.getMyRoles()){
				if(itsRole.equals(myRole)){ //common role
					recordNewcomer = true;
					if(substitutes==null)substitutes = new HashMap<String, Map<String, Player>>();
					commonRole = true;
					substitutes.put(itsRole,sobject.getKnownPlayers().get(itsRole));
					//Pick pointers of each common role to later send msgs with newcomer info
					rolePointer = getRolePointer((List<Player>)((Map<String,Player>)substitutes.values()).values(),pointerIndex);
					if(rolePointersSend==null) rolePointersSend = new TreeSet<String>();
					if (rolePointer!= null && rolePointer.getId()!=sobject.getAddress()){ // I am not the rolePointer
						rolePointersSend.add(String.valueOf(rolePointer.getId()));
					}else{ // If I am the rolePointer I have to send newcomer info to everyone in my substitute list
						for(Player substitute: ((Map<String,Player>)sobject.getKnownPlayers().get(itsRole).values()).values())
							rolePointersSend.add(String.valueOf(substitute.getId()));
					}
					rolePointer = null;
					break;
				} 
			}
			if(!commonRole){ //not common role
				complementaryPointer = sobject.getKnownPlayers().get(itsRole);
				
				if(complementaryPointer==null){//if I dont have pointer to that role, record newcomer as pointer
					recordNewcomer = true;
				}else{ 
					if(complementaries==null) complementaries = new HashMap<String, Map<String, Player>>();
					//to send pong with pointer info to newcomer, so it can register there
					complementaries.put(itsRole, complementaryPointer);
					//I have pointer to that role, so, update to new pointer if current pointer 
					//not in my substitutes list.
					compId = String.valueOf(((List<Player>)complementaryPointer.values()).get(0).getId());
					//Next line does not work because I haven't gone through all possible substitutes, 
					//I need to get the roles I play and to which I am pointer and on those list check this
					if(!substitutes.containsKey(compId)){
						recordNewcomer = true;
					}//If current pointer in my substitutes, discard newcomer, as for this role respects
				}
			}
			commonRole = false;
		}
		
		if(!substitutes.isEmpty()){
			//if pointer is not available, check who is second and send to them, if different to me
			//send ping to list of pointers
			args.add(substitutes);
		}
		
		//check roles newcomer, if there is anyone already handling them I sent its address per role,
		
		
		//Send pong back to ping sender
		
		
		
		
		 
		//I sent pointers of roles newcomer does not have
		//I store newcomer as pointer of roles it brings and I don't have, unless I have somebody
		//If I have somebody I send the newcomer address to every role pointer I have (for roles of the newcomer)
		
		
//		RolePlayerServices.updateKnownPlayersPlayer(sobject,((Integer)msg.getArgs().get(0)).intValue(),newcomerRoles);
		addNewKnownPeer(((Integer)msg.getArgs().get(0)).intValue());
//		System.out.println("(Sobject "+sobject.getAddress()+"): doPing updated: "+msg.getArgs().get(0)+" for roles: "+ (List<String>)msg.getArgs().get(1));
		network.sendMsg(Integer.parseInt(msg.getId()), sobject.getAddress(),msg.getSender(),PONG, args);
		
		
		
	}
	
	@SuppressWarnings("unchecked")
	public void doPong(Message msg){
		
	}
	
	public Player getRolePointer(List<Player> players, int pointerIndex){
		Collections.sort(players,new RankingComparator());
		return players.get(pointerIndex);
	}
	@Override
	public void doQuery(Message msg) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void doQueryHit(Message msg) {
		// TODO Auto-generated method stub
		
	}
}
