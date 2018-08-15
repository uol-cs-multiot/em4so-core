package org.multiot.em4so.behaviour;

import org.multiot.em4so.model.SObject;

public interface BehaviourImplementator {
	public default void completeRemoveFromKnownPlayersPlayer(SObject sobject, int address) {
	};
}
