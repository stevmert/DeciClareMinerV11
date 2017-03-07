package miner.rule;

import java.util.Comparator;

public class RuleLengthComparator implements Comparator<Rule> {

	@Override
	public int compare(Rule r1, Rule r2) {
		boolean isSingle1 = r1 instanceof SingleRule;
		boolean isSingle2 = r2 instanceof SingleRule;
		if(isSingle1 && isSingle2)
			return 0;
		if(isSingle1)
			return -1;
		if(isSingle2)
			return 1;
		//both batch
		int nr1 = ((BatchRule) r1).getRules().length;
		int nr2 = ((BatchRule) r2).getRules().length;
		if(nr1 < nr2)
			return -1;
		if(nr1 > nr2)
			return 1;
		return 0;
	}
}