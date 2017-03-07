package miner.subminer.genetic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import miner.rule.BatchRule;
import miner.rule.Rule;
import model.Constraint;

public class RulePopulationWithDiversityMap extends RulePopulation {

	private static final long serialVersionUID = -954123925729455183L;

	private HashMap<Constraint, Integer> map_diversity;
	private int totalDiversity;

	private RulePopulationWithDiversityMap(List<Rule> list, HashSet<Rule> set,
			HashMap<Constraint, Integer> map_diversity) {
		super(list, set);
		this.map_diversity = map_diversity;
		totalDiversity = 0;
		for(Integer i : map_diversity.values())
			if(i != null)
				totalDiversity += i;
	}

	public RulePopulationWithDiversityMap() {
		this(new ArrayList<>(), new HashSet<>(), new HashMap<>());
	}

	public RulePopulationWithDiversityMap(List<Rule> list) {
		this(list, new HashSet<>(list), getHashMap_div(list));
	}

	public RulePopulationWithDiversityMap(HashSet<Rule> set) {
		this(new ArrayList<>(set), set, getHashMap_div(set));
	}

	public RulePopulationWithDiversityMap(RulePopulationWithDiversityMap rpop) {
		this(new ArrayList<>(rpop), new HashSet<>(rpop), getHashMap_div(rpop));
	}

	private static HashMap<Constraint, Integer> getHashMap_div(Collection<Rule> collection) {
		HashMap<Constraint, Integer> res = new HashMap<>();
		for(Rule r : collection) {
			Constraint ruleDecisionless = r.getConstraint().getDecisionlessCopy();
			if(res.get(ruleDecisionless) == null)
				res.put(ruleDecisionless, 1);
			else
				res.put(ruleDecisionless, res.get(ruleDecisionless)+1);
		}
		return res;
	}

	public RulePopulationWithDiversityMap subPopulation(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void doAdditionalIntegrationActions(BatchRule newBR) {
		addToMapDiversity(newBR);
	}

	private void addToMapDiversity(Rule r) {
		Constraint ruleDecisionless = r.getConstraint().getDecisionlessCopy();
		Integer nr = map_diversity.get(ruleDecisionless);
		if(nr == null)
			map_diversity.put(ruleDecisionless, 1);
		else
			map_diversity.put(ruleDecisionless, nr+1);
		totalDiversity++;
	}

	@Override
	public String toString() {
		return super.toString() + " // diversity=" + map_diversity.size();
	}

	//MAP METHODS
	//----------------------------------------------------------------------------------------

	public Integer get(Constraint c) {
		return map_diversity.get(c);
	}

	public double getDiversity(Constraint c) {
		if(totalDiversity == 0)
			return 1;
		Integer div = map_diversity.get(c);
		if(div == null)
			return 1;
		return ((double) (totalDiversity-div))/totalDiversity;
	}
}