package org.multiot.em4so.behaviour;

import java.util.Comparator;

import org.multiot.em4so.model.Player;

public class RankingComparator implements Comparator<Player> {

	@Override
	public int compare(Player arg0, Player arg1) {

		//TODO change id for ranking, reputation, etc.
		int startComparison = compare(arg0.getTimesWorked(), arg1.getTimesWorked());
	    return startComparison != 0 ? startComparison
	    							//This ensure that always my capabilities/roles will be ranked higher to avoid remove my own capabilities
	                                : inverseCompare(arg0.getTimeRecorded(),arg1.getTimeRecorded())!=0?inverseCompare(arg0.getTimeRecorded(),arg1.getTimeRecorded()):
	                                 inverseCompare(arg0.getTimeJoined(),arg1.getTimeJoined())!=0?inverseCompare(arg0.getTimeJoined(),arg1.getTimeJoined()):1; 
	                                	// Arbitrary select one
	}

		  private static int compare(int a, int b) {
		    return a < b ? -1
		         : a > b ? 1
		         : 0;
		  }
		  
		  private static int inverseCompare(int a, int b) {
			    return a > b ? -1
			         : a < b ? 1
			         : 0;
			  }

}
