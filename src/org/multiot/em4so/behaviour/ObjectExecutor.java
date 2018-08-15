package org.multiot.em4so.behaviour;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.multiot.em4so.model.Plan;
import org.multiot.em4so.model.Player;
import org.multiot.em4so.protocol.SOProtocol;

public interface ObjectExecutor {
	public void simulateTask(String task, List<?> args);
	public void setPendingCheck(boolean pendingCheck);
	public void setPendingTrigger(boolean pendingTrigger);
	public void simExecuteActivity(String activity,boolean startScenario, boolean endScenario, String planId, String scenarioId, int queryTime);
	public SOProtocol getSoProtocol();
	public String lookUpRole ( String activityName );
	public Player selectPlayer (Collection<Player> availablePlayers, int index);
	public boolean isConnected(int soId);
	public void doUnachievableScenario(String reason);
	public Map<String, Plan> getQueriesPlans();
	public void setQueriesPlans(Map<String, Plan> queriesPlans);
}
