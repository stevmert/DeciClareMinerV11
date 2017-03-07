package miner.subminer.genetic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Spliterator;

import miner.Config;
import miner.rule.BatchRule;
import miner.rule.Rule;
import miner.rule.SingleRule;
import model.data.DecisionRule;

public class RulePopulation implements List<Rule>, Set<Rule>, Serializable {

	private static final long serialVersionUID = -803101863690127416L;

	private List<Rule> list;
	private HashSet<Rule> set;

	protected RulePopulation(List<Rule> list, HashSet<Rule> set) {
		super();
		this.list = list;
		this.set = set;
	}

	public RulePopulation() {
		this(new ArrayList<>(), new HashSet<>());
	}

	public RulePopulation(List<Rule> list) {
		this(list, new HashSet<>(list));
	}

	public RulePopulation(HashSet<Rule> set) {
		this(new ArrayList<>(set), set);
	}

	public RulePopulation(RulePopulation rpop) {
		this(new ArrayList<>(rpop), new HashSet<>(rpop));
	}

	public RulePopulation subPopulation(int fromIndex, int toIndex) {
		return new RulePopulation(list.subList(fromIndex, toIndex));
	}

	protected static int getWeightedRandomIndexBasedOnRank(List<?> list) {
		int completeWeight = 0;
		for(int i = 1; i < list.size()-1; i++)
			completeWeight += i;
		double r = Config.RANDOM.nextDouble() * completeWeight;
		int countWeight = 0;
		for(int i = 0; i < list.size(); i++) {
			countWeight += list.size()-i;
			if (countWeight >= r)
				return i;
		}
		throw new RuntimeException("TODO?");
	}

	public int getWeightedRandomIndexBasedOnRank() {
		return getWeightedRandomIndexBasedOnRank(this);
	}

	public int getWeightedRandomIndexBasedOnRank(int indexBachelor) {
		//TODO: sorteer rest van populatie voor beste kandidaten om mee te paren.
		//sorteer enkel die van zelfde constraint class (in hashmap!), voeg rest achteraan toe...
		//sorteer gelijkaardige obv gelijkaardigheid van input activityexpression(s)
		//cache: gesorteerde lijsten!!! (of toch slechts een kleine berekening?)
		//		List<Rule> sortedList = new ArrayList<>(this.get(this.get(indexBachelor).getConstraint().getClass()));
		//		Collections.sort(sortedList, new RuleFitnessComparator(this, dataAttributes));
		//TODO: veel complexiteit, weinig toegevoegde waarde?

		//Easiest way (=KISS!)
		return getWeightedRandomIndexBasedOnRank();
	}

	public IntegrationReport integrate(Rule newR) {
		//only addable rules in BatchRule r
		//create new rule, since original rule might still be used in pops
		IntegrationReport res = new IntegrationReport();
		BatchRule newBR = (BatchRule) newR;
		{
			ArrayList<SingleRule> list = new ArrayList<>();
			boolean add = false;
			for(SingleRule sr : newR.rules()) {
				if(sr.getConformancePercentage_Traces() >= Config.MINIMAL_CONFORMANCE)
					list.add(sr);
				else
					add = true;
			}
			if(list.isEmpty())
				return null;
			if(add)
				newBR = new BatchRule(newR.getConstraint(), list.toArray(new Rule[list.size()]), newR.getSeed());
			res.addSingleRulesAdded(list.size());
			res.addBatchRulesAdded();
		}
		for(Rule alreadyFound : new ArrayList<>(this)) {
			if(newBR.equals(alreadyFound))//exactly same rule
				return null;
			if(newBR.getConstraint().equals(alreadyFound.getConstraint(), false)) {
				//check if there is already a rule that is contained by given rule, or vice versa
				for(DecisionRule x_new : newBR.getConstraint().getActivationDecision().getRules())
					for(DecisionRule x_alreadyFound : alreadyFound.getConstraint().getActivationDecision().getRules()) {
						//check if have same subrules
						ArrayList<SingleRule> newList_alF = new ArrayList<>();
						ArrayList<SingleRule> newList_new = new ArrayList<>();
						boolean contains1same = false;
						for(SingleRule sr_older : alreadyFound.rules()) {
							boolean found = false;
							for(SingleRule sr_new : newBR.rules())
								if(sr_new.getConstraint().equals(sr_older.getConstraint(), false)) {
									found = true;
									contains1same = true;
									break;
								}
							if(!found)
								newList_alF.add(sr_older);
						}
						for(SingleRule sr_new : newBR.rules()) {
							boolean found = false;
							for(SingleRule sr_older : alreadyFound.rules())
								if(sr_new.getConstraint().equals(sr_older.getConstraint(), false)) {
									found = true;
									break;
								}
							if(!found)
								newList_new.add(sr_new);
						}
						//check if same/more general/more specific decision
						if(x_alreadyFound.equals(x_new)) {//same
							if(newList_alF.isEmpty() && newList_new.isEmpty())//same as already found
								return null;
							else if(newList_alF.isEmpty()) {
								if(newBR.getRules().length != newList_new.size())
									newBR.setRules(newList_new.toArray(new Rule[newList_new.size()]));
							} else {
								//TODO: should not occur, but in to be sure a message is printed... (occurred during development because of a bug)
								System.out.println("THE UNTHINKABLE HAPPEND!!!");
								System.out.println("alreadyFound: " + alreadyFound);
								System.out.println("newBR: " + newBR);
								if(newBR.getRules().length != newList_new.size())
									newBR.setRules(newList_new.toArray(new Rule[newList_new.size()]));
							}
						} else if(x_alreadyFound.getDataValues().containsAll(x_new.getDataValues())) {//candidate for more general version
							if(newList_alF.isEmpty()) {//better than already found, contains at least same subrules (possibly more)
								res.addBatchRulesRemoved();
								res.addSingleRulesRemoved(((BatchRule) alreadyFound).getRules().length);
								this.remove(alreadyFound);
							} else if(contains1same) {//better than already found, but just for a couple of subrules
								res.addSingleRulesRemoved(((BatchRule) alreadyFound).getRules().length - newList_alF.size());
								((BatchRule) alreadyFound).setRules(newList_alF.toArray(new Rule[newList_alF.size()]));
							}
						} else if(x_new.getDataValues().containsAll(x_alreadyFound.getDataValues())) {//candidate more specific
							if(newList_alF.isEmpty() && newList_new.isEmpty())//same as already found, but more specific
								return null;
							if(newBR.getRules().length != newList_new.size())
								newBR.setRules(newList_new.toArray(new Rule[newList_new.size()]));
						}
					}
				if(newBR.getRules().length == 0)
					return null;//all subrules eliminated...
			}
		}
		add(newBR);
		doAdditionalIntegrationActions(newBR);
		return res;
	}

	protected void doAdditionalIntegrationActions(BatchRule newBR) {}

	@Override
	public String toString() {
		return "Size=" + list.size();
	}

	//LIST AND SET METHODS
	//---------------------------------------------------------------------------------------------
	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return set.contains(o);
	}

	@Override
	public Iterator<Rule> iterator() {
		return list.iterator();
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}

	/**
	 * Always added to list, if not already present also to set and map
	 * @return boolean set.add (=false if same rule already in set)
	 */
	@Override
	public boolean add(Rule r) {
		list.add(r);
		return set.add(r);
	}

	@Override
	public boolean remove(Object o) {
		boolean didRemove = list.remove(o);
		if(didRemove && !list.contains(o)) {//if not a double in list
			set.remove(o);
		}
		return didRemove;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return set.containsAll(c);
	}

	/**
	 * Always added to list, if not already present also to set and map
	 * @return boolean set.addAll (=false if same rule already in set)
	 */
	@Override
	public boolean addAll(Collection<? extends Rule> c) {
		boolean res = true;
		for(Rule r : c)
			res = res && add(r);
		return res;
	}

	/**
	 * Unsupported!
	 */
	@Override
	public boolean addAll(int index, Collection<? extends Rule> c) {
		throw new IllegalArgumentException("Unsupported");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean res = true;
		for(Object o : c)
			res = res && remove(o);
		return res;
	}

	/**
	 * Unsupported!
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		throw new IllegalArgumentException("Unsupported");
	}

	@Override
	public void clear() {
		list.clear();
		set.clear();
	}

	@Override
	public Rule get(int index) {
		return list.get(index);
	}

	@Override
	public Rule set(int index, Rule r) {
		Rule oldRule = list.set(index, r);
		if(!list.contains(oldRule))//if not a double in list
			set.remove(oldRule);
		set.add(r);
		return oldRule;
	}

	@Override
	public void add(int index, Rule r) {
		list.add(index, r);
		set.add(r);
	}

	@Override
	public Rule remove(int index) {
		Rule removedRule = list.remove(index);
		if(!list.contains(removedRule))//if not a double in list
			set.remove(removedRule);
		return removedRule;
	}

	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}

	@Override
	public ListIterator<Rule> listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator<Rule> listIterator(int index) {
		return list.listIterator(index);
	}

	@Override
	public List<Rule> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}

	/**
	 * Unsupported!
	 */
	@Override
	public Spliterator<Rule> spliterator() {
		//		return List.super.spliterator();
		throw new IllegalArgumentException("Unsupported");
	}
}