package org.multiot.em4so.model;

import java.util.List;



public class Message {

	private String id;
	private int sender;
	private int receiver;
	private List<Object> args;
	private String msg;
	private int latency;
	private int priority;
	private int TTL;
	
	public Message (String id, int sender, int receiver, String msg, List<Object> args, int TTL, int priority, int latency ){
		this.id = id;
		this.sender = sender;
		this.receiver = receiver;
		this.msg = msg;
		this.args = args;
		this.TTL = TTL;
		this.priority = priority;
		this.latency = latency;
		
	}
	
	public Message (Message msg){
		this.id = msg.id;
		this.sender = msg.sender;
		this.receiver = msg.receiver;
		this.msg = msg.msg;
		this.args = msg.args;
		this.TTL = msg.TTL;
		this.priority = msg.priority;
		this.latency = msg.latency;
		
	}
	
	
	public int getTTL() {
		return TTL;
	}


	public void setTTL(int tTL) {
		TTL = tTL;
	}


	public int getPriority() {
		return priority;
	}


	public void setPriority(int priority) {
		this.priority = priority;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public int getSender() {
		return sender;
	}


	public void setSender(int sender) {
		this.sender = sender;
	}


	public int getReceiver() {
		return receiver;
	}


	public void setReceiver(int receiver) {
		this.receiver = receiver;
	}


	public List<Object> getArgs() {
		return args;
	}


	public void setArgs(List<Object> args) {
		this.args = args;
	}


	public String getMsg() {
		return msg;
	}


	public void setMsg(String msg) {
		this.msg = msg;
	}


	public int getLatency() {
		return latency;
	}


	public void setLatency(int latency) {
		this.latency = latency;
	}


	@Override
	public String toString() {
		return "{id: "+id+", sender: "+sender+", receiver: "+receiver+", args: "+args+", msg: "+msg+", TTL:"+TTL+", priority: "+priority+"}";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((args == null) ? 0 : args.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + receiver;
		result = prime * result + sender;
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
		Message other = (Message) obj;
		if (args == null) {
			if (other.args != null)
				return false;
		} else if (!args.equals(other.args))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (receiver != other.receiver)
			return false;
		if (sender != other.sender)
			return false;
		return true;
	}


	
	
	
}
