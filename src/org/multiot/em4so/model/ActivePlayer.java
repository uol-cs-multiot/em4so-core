package org.multiot.em4so.model;

public class ActivePlayer implements Comparable<ActivePlayer>{

	private Player player;
	private Plan plan;
	private int limit;
	
	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	private boolean successful;
	private boolean committed;
	
	public boolean isCommitted() {
		return committed;
	}

	public void setCommitted(boolean committed) {
		this.committed = committed;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public Plan getPlan() {
		return plan;
	}

	public void setPlan(Plan plan) {
		this.plan = plan;
	}

	public ActivePlayer(Player player, Plan plan, int limit){
		this.player = player;
		this.plan = plan;
		this.committed = false;
		this.successful = false;
		this.limit = limit;
	}
	
	public String toString(){
		return player.toString() +" - "+plan.toString()+", limit: "+limit+" c: "+committed+", s:"+successful;
	}

	@Override
	public int compareTo(ActivePlayer arg0) {
		return this.limit - arg0.limit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((plan == null) ? 0 : plan.hashCode());
		result = prime * result + ((player == null) ? 0 : player.hashCode());
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
		ActivePlayer other = (ActivePlayer) obj;
		if (plan == null) {
			if (other.plan != null)
				return false;
		} else if (!plan.equals(other.plan))
			return false;
		if (player == null) {
			if (other.player != null)
				return false;
		} else if (!player.equals(other.player))
			return false;
		return true;
	}
	
	
}
