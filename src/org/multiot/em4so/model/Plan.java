package org.multiot.em4so.model;

import java.util.List;


public class Plan {
private String id;
private List<String> steps;
private int nextStep;
private int owner;
private int previousResponsible;
private String scenarioId;
private int queryTime;
private int lastExecutedTime;


public int getLastExecutedTime() {
	return lastExecutedTime;
}
public void setLastExecutedTime(int lastExecutedTime) {
	this.lastExecutedTime = lastExecutedTime;
}
public int getQueryTime() {
	return queryTime;
}
public void setQueryTime(int queryTime) {
	this.queryTime = queryTime;
}
public String getScenarioId() {
	return scenarioId;
}
public void setScenarioId(String scenarioId) {
	this.scenarioId = scenarioId;
}
public int getOwner() {
	return owner;
}
public void setOwner(int owner) {
	this.owner = owner;
}
public int getPreviousResponsible() {
	return previousResponsible;
}
public void setPreviousResponsible(int previousResponsible) {
	this.previousResponsible = previousResponsible;
}
public List<String> getSteps() {
	return steps;
}
public String getId() {
	return id;
}
public void setId(String id) {
	this.id = id;
}
public void setSteps(List<String> steps) {
	this.steps = steps;
}

public Plan(Plan plan){
this.id = plan.id;
this.steps = plan.steps;
this.nextStep = plan.nextStep;
this.owner = plan.owner;
this.previousResponsible = plan.previousResponsible;
this.scenarioId = plan.scenarioId;
this.queryTime=plan.queryTime;
this.lastExecutedTime = plan.lastExecutedTime;
}
public Plan(String id, List<String> steps, int owner, String scenarioId){
	this.id =id;
	this.steps = steps;
	this.nextStep = 0;
	this.owner = owner;
	this.previousResponsible = owner;
	this.scenarioId = scenarioId;
	this.queryTime=0;
	this.lastExecutedTime=0;
}
public int getNextStep() {
	return nextStep;
}
public void setNextStep(int nextStep) {
	this.nextStep = nextStep;
}

/**
 * Returns current activity to execute
 * @return
 */
public String getActivity(String from){
	return nextStep!=-1?steps.get(nextStep):null;
}

public String toString(){
	String strPlan = "Plan "+id+":{["; 
	for(String s:steps){
		strPlan += s+", ";
	}
	strPlan += "] -next: "+nextStep
			+"owner: " + owner +" "
			+"previous: "+ previousResponsible +" "
			+"}";
	return strPlan;		
}
@Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((id == null) ? 0 : id.hashCode());
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
	Plan other = (Plan) obj;
	if (id == null) {
		if (other.id != null)
			return false;
	} else if (!id.equals(other.id))
		return false;
	return true;
}



}
