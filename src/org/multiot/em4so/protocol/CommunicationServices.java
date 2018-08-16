package org.multiot.em4so.protocol;

import java.util.List;

import org.multiot.em4so.config.GlobalParameters;
import org.multiot.em4so.utils.EntityUtils;

public interface CommunicationServices {
	public String sendMsg(int id, int sender, int receiver, String type, List<?> args, int TTL, int priority);
	
	public default String sendMsg(int id, int sender, int receiver, String type){
		return this.sendMsg(id,sender,receiver,type,null,GlobalParameters.getBaseTTL(),3);
	}

	public default String sendMsg(int id, int sender, int receiver, String type, List<?> args){
		return this.sendMsg(id,sender,receiver,type,args,GlobalParameters.getBaseTTL(),3);
	}
	
	public void completeDoPong(int address);
	
}
