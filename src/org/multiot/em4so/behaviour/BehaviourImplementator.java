package org.multiot.em4so.behaviour;

import org.multiot.em4so.model.SObject;

public interface BehaviourImplementator {
	public default void completeRemoveFromKnownPlayersPlayer(SObject sobject, int address) {
		System.out.println("Nothing to do from default 1");
	};
	public default void completeUpdateKnownPlayersRole(SObject sobject, int address) {
		System.out.println("Nothing to do from default 2");
	}
}
