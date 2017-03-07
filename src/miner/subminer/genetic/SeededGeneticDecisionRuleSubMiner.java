package miner.subminer.genetic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import miner.Config;
import miner.log.Log;
import miner.rule.BatchRule;
import miner.rule.Rule;
import miner.rule.RuleFitnessComparator;
import miner.rule.SingleRule;
import model.Constraint;
import model.data.BooleanDataAttribute;
import model.data.CategoricalDataAttribute;
import model.data.DataAttribute;
import model.data.Decision;
import model.data.DecisionRule;

//TODO: cache evaluations?
public class SeededGeneticDecisionRuleSubMiner {

	private final String name;//for identifying the subpopulations

	private final Log log;
	private final DataAttribute[] dataAttributes;//every data attribute once (=/= every value once!)
	private final Rule[] seeds;

	private final RulePopulationWithDiversityMap resultPop;
	private final int editPopSize;
	private RulePopulation editPop;
	private boolean hasConverged;
	private int iteration;

	private RuleFitnessComparator fitnessSorter;

	public SeededGeneticDecisionRuleSubMiner(String name, Log log,
			DataAttribute[] dataAttributes, Rule[] seeds, int editPopSize) {
		if(dataAttributes.length == 0)
			throw new IllegalArgumentException("No data elements to mine!");
		if(editPopSize < Config.MINIMAL_SUBPOPULATIONSIZE_EDIT)
			throw new IllegalArgumentException("Edit pop too small!");
		this.name = name;
		this.log = log;
		this.seeds = seeds;
		this.dataAttributes = dataAttributes;
		this.resultPop = new RulePopulationWithDiversityMap();
		this.editPop = null;
		this.hasConverged = false;
		this.iteration = 0;
		this.editPopSize = editPopSize;
	}

	public RulePopulation getResultPop() {
		return resultPop;
	}

	public String getName() {
		return name;
	}

	public Log getLog() {
		return log;
	}

	public DataAttribute[] getDataAttributes() {
		return dataAttributes;
	}

	public Rule[] getSeeds() {
		return seeds;
	}

	public boolean hasConverged() {
		return hasConverged;
	}

	public void setConvergence(boolean hasConverged) {
		this.hasConverged = hasConverged;
	}

	public int getEditPopSize() {
		return editPopSize;
	}

	public int getIteration() {
		return iteration;
	}

	public IntegrationReport doIteration(String spaces) {
		System.out.println(spaces + "Iteration " + iteration + " of " + getName() + " starting...");
		String spaces_sub = Config.LEVEL_SPACES + spaces;
		IntegrationReport report = new IntegrationReport();
		long t_start = System.currentTimeMillis();
		if(editPop == null || editPop.isEmpty()) {//init
			try {
				editPop = createInitialPop(spaces_sub);
			} catch (NoNewSeedFoundException e) {
				System.out.println("\n" + spaces_sub + "NO NEW SEEDS FOUND!");
				return report;
			}
			System.out.println(spaces_sub + "Sorting initial population...");
			report.add(sort(editPop, Config.LEVEL_SPACES + spaces_sub));
		} else {
			RulePopulation childPop = createChildPop(editPop, spaces_sub);
			System.out.println(spaces_sub + "Sorting child population...");
			report.add(sort(childPop, Config.LEVEL_SPACES + spaces_sub));
			setNewEditPop(childPop, spaces_sub);
			System.out.println(spaces_sub + "Sorting new edit population...");
			report.add(sort(editPop, Config.LEVEL_SPACES + spaces_sub));
		}
		long t_end = System.currentTimeMillis();
		System.out.println(spaces + "Iteration " + iteration + " of " + getName()
		+ " completed in " + (t_end-t_start) + "ms");
		System.out.println(spaces_sub + report.getSingleRulesAdded() + " new rules mined ("
				+ report.getBatchRulesAdded() + " aggregate rules)");
		System.out.println(spaces_sub + report.getSingleRulesRemoved() + " rules replaced by better new rule ("
				+ report.getBatchRulesRemoved() + " aggregate rules)");
		System.out.println(spaces_sub + "Result pop contains " + resultPop.size() + " aggregate rules");
		//		if(Config.DO_LOGGING)
		//			Logger.getInstance().logStat_p2(0, minedRules.size(), t_end-t_start);
		iteration++;
		return report;
	}

	private RulePopulation createInitialPop(String spaces) throws NoNewSeedFoundException {
		System.out.println(spaces + "Creating initial population...");
		System.out.print(spaces);
		RulePopulation initPop = new RulePopulation();
		for(int triesLeft = getEditPopSize() * Config.NEWSEED_GENERATION_RETRIES_PER_INDIVIDUAL;
				initPop.size() < getEditPopSize() && triesLeft > 0; triesLeft--) {
			Rule newSeed = getRandomSeedEvaluation();
			if(newSeed == null)
				System.out.print("X");
			else {
				System.out.print("I");
				initPop.add(newSeed);
			}
		}
		if(initPop.isEmpty())
			throw new NoNewSeedFoundException();
		if(initPop.size() < getEditPopSize()) {
			int size = initPop.size();
			while(initPop.size() < getEditPopSize()) {
				initPop.add(initPop.get(Config.RANDOM.nextInt(size)));
				System.out.print(".");
			}
		}
		System.out.println();
		return initPop;
	}

	private Rule getRandomSeedEvaluation() {
		Rule seed = seeds[Config.RANDOM.nextInt(seeds.length)];
		Constraint c = seed.getConstraint().getDecisionlessCopy();
		Decision d = new Decision();
		DecisionRule dr = generateRandomDecisionRule();
		d.addRule(dr);
		c.setActivationDecision(d);
		return c.evaluate(getLog(), seed);
	}

	private DecisionRule generateRandomDecisionRule() {
		int nrOfActiveDataAttrs = Config.RANDOM.nextInt(dataAttributes.length)+1;
		ArrayList<DataAttribute> availableAttributes = new ArrayList<>(Arrays.asList(dataAttributes));
		HashSet<DataAttribute> dataAttributes = new HashSet<>();
		for(int i = 0; i < nrOfActiveDataAttrs; i++) {
			int daIndex = Config.RANDOM.nextInt(availableAttributes.size());
			DataAttribute da = availableAttributes.get(daIndex);
			if(da instanceof BooleanDataAttribute) {
				BooleanDataAttribute bda = (BooleanDataAttribute) da;
				dataAttributes.add(new BooleanDataAttribute(bda.getName(), Config.RANDOM.nextBoolean(), bda.getParent()));
			} else {
				CategoricalDataAttribute cda = (CategoricalDataAttribute) da;
				int nrOfCategories = Config.RANDOM.nextInt(cda.getValues().size())+1;
				ArrayList<String> availCats = new ArrayList<>(cda.getValues());
				ArrayList<String> cats = new ArrayList<>();
				for(int k = 0; k < nrOfCategories; k++) {
					int catIndex = Config.RANDOM.nextInt(availCats.size());
					cats.add(availCats.get(catIndex));
					availCats.remove(catIndex);
				}
				for(String cat : cats)
					dataAttributes.add(new CategoricalDataAttribute(cda.getName(), cda.getValues(), cat, cda.getParent()));
			}
			availableAttributes.remove(daIndex);
		}
		return new DecisionRule(dataAttributes);
	}

	private RulePopulation createChildPop(RulePopulation editPop_sorted, String spaces) {
		if(editPop_sorted.size() < 2)
			throw new IllegalArgumentException();
		System.out.println(spaces + "Executing recombination...");
		RulePopulation childrenPop = new RulePopulation();
		System.out.println(Config.LEVEL_SPACES + spaces + "Adding children...");
		System.out.print(Config.LEVEL_SPACES + spaces);
		while(childrenPop.size() < getEditPopSize() * Config.POPULATIONSIZE_RECOMBINATION_MULTIPLIER) {
			int i1 = editPop_sorted.getWeightedRandomIndexBasedOnRank();
			int i2 = i1;
			while(i2 == i1)
				i2 = editPop_sorted.getWeightedRandomIndexBasedOnRank(i1);
			Rule parent1 = editPop_sorted.get(i1);
			Rule parent2 = editPop_sorted.get(i2);
			Rule child = recombine(parent1, parent2);
			if(child != null) {
				childrenPop.add(child);
				System.out.print("I");
			} else
				System.out.print("X");
		}
		System.out.println();
		return childrenPop;
	}

	private Rule recombine(Rule p1, Rule p2) {
		//half of each parents decision logic to child
		ArrayList<DataAttribute> l1 = new ArrayList<>(Arrays.asList(dataAttributes));
		HashSet<DataAttribute> l2 = new HashSet<>(dataAttributes.length/2);
		for(int i = 0; i < dataAttributes.length/2; i++) {
			int indexToMove = Config.RANDOM.nextInt(l1.size());
			l2.add(l1.get(indexToMove));
			l1.remove(indexToMove);
		}
		HashSet<DataAttribute> dataValues = new HashSet<>();
		for(DataAttribute da : dataAttributes) {
			ArrayList<DataAttribute> dvs;
			if(l2.contains(da))
				dvs = recombine_inner(p2, da);
			else
				dvs = recombine_inner(p1, da);
			if(Config.RANDOM.nextDouble() < Config.MUTATION_PROBABILITY) {
				if(dvs.isEmpty()) {
					//add with random value
					if(da instanceof BooleanDataAttribute)
						dvs.add(new BooleanDataAttribute(da.getName(), Config.RANDOM.nextBoolean(), da.getParent()));
					else {
						CategoricalDataAttribute cda = (CategoricalDataAttribute) da;
						int nrOfCategories = Config.RANDOM.nextInt(cda.getValues().size())+1;
						ArrayList<String> availCats = new ArrayList<>(cda.getValues());
						ArrayList<String> cats = new ArrayList<>();
						for(int k = 0; k < nrOfCategories; k++) {
							int catIndex = Config.RANDOM.nextInt(availCats.size());
							cats.add(availCats.get(catIndex));
							availCats.remove(catIndex);
						}
						for(String cat : cats)
							dvs.add(new CategoricalDataAttribute(cda.getName(), cda.getValues(), cat, cda.getParent()));
					}
				} else {//change value or remove datavalue altogether
					if(Config.RANDOM.nextBoolean())//remove
						dvs.clear();
					else if(da instanceof BooleanDataAttribute) {//switch value
						dvs.add(new BooleanDataAttribute(dvs.get(0).getName(),
								!((BooleanDataAttribute) dvs.get(0)).getValue(), dvs.get(0).getParent()));
						dvs.remove(0);
					} else {//change values (but keep same number of cats unless maxed out)
						if(dvs.size() == ((CategoricalDataAttribute) dvs.get(0)).getValues().size()) {//maxed out
							//remove 1 ad random
							dvs.remove(Config.RANDOM.nextInt(dvs.size()));
						} else {//change values (but keep same number of cats)
							CategoricalDataAttribute cda = (CategoricalDataAttribute) dvs.get(0);
							int nrOfChanges = Config.RANDOM.nextInt(dvs.size())+1;
							ArrayList<String> availCats = new ArrayList<>(cda.getValues());
							for(DataAttribute x : dvs)
								availCats.remove(((CategoricalDataAttribute) x).getValue());
							nrOfChanges = Math.min(nrOfChanges, availCats.size());
							ArrayList<String> cats = new ArrayList<>();
							for(int k = 0; k < nrOfChanges; k++) {
								int catIndex = Config.RANDOM.nextInt(availCats.size());
								cats.add(availCats.get(catIndex));
								availCats.remove(catIndex);
								dvs.remove(Config.RANDOM.nextInt(dvs.size()));//remove a random value
							}
							for(String cat : cats)//add new values
								dvs.add(new CategoricalDataAttribute(cda.getName(), cda.getValues(), cat, cda.getParent()));
						}
					}
				}
			}
			if(!dvs.isEmpty())
				dataValues.addAll(dvs);
		}
		if(dataValues.isEmpty())
			return null;
		//randomly determine which rule will be prototype (~gender)
		Constraint child;
		Rule seed;
		int generation;
		if(Config.RANDOM.nextBoolean()) {
			child = p1.getConstraint().getDecisionlessCopy();
			seed = p1.getSeed();
			generation = p1.getGeneration();
		} else {
			child = p2.getConstraint().getDecisionlessCopy();
			seed = p2.getSeed();
			generation = p2.getGeneration();
		}
		DecisionRule dr = new DecisionRule(dataValues);
		Decision d = new Decision();
		d.addRule(dr);
		child.setActivationDecision(d);
		Rule res = child.evaluate(getLog(), seed);
		if(res != null)
			res.setGeneration(generation+1);
		return res;
	}

	private ArrayList<DataAttribute> recombine_inner(Rule p, DataAttribute da) {
		ArrayList<DataAttribute> res = new ArrayList<>();
		for(DecisionRule dr : p.getConstraint().getActivationDecision().getRules())
			for(DataAttribute dv : dr.getDataValues())
				if(dv.getName().equals(da.getName()))
					res.add(dv);
		return res;
	}

	private void setNewEditPop(RulePopulation childPop_sorted, String spaces) {
		System.out.println(spaces + "Creating new edit population...");
		int nrToKeep = (int) Math.round((1d - Config.POPULATIONSIZE_NEWEDIT_SEEDS_PERCENTAGE) * getEditPopSize());
		if(nrToKeep == 0
				&& Config.POPULATIONSIZE_NEWEDIT_SEEDS_PERCENTAGE < 1d)
			nrToKeep = 1;
		editPop = childPop_sorted.subPopulation(0, nrToKeep);
		System.out.println(Config.LEVEL_SPACES + spaces + "Adding random seeds to new edit population...");
		System.out.print(Config.LEVEL_SPACES + spaces);
		ArrayList<Rule> newSeeds = new ArrayList<>();
		for(int triesLeft = (getEditPopSize() - editPop.size()) * Config.NEWSEED_GENERATION_RETRIES_PER_INDIVIDUAL;
				editPop.size() < getEditPopSize() && triesLeft > 0; triesLeft--) {
			Rule seed = getRandomSeedEvaluation();
			if(seed == null)
				System.out.print("X");
			else {
				System.out.print("I");
				editPop.add(seed);
				newSeeds.add(seed);
			}
		}

		if(editPop.isEmpty() && newSeeds.isEmpty()) {
			if(resultPop.isEmpty()) {//return empty pop

			} else {//add rules from resPop (~optimize them)
				while(editPop.size() < getEditPopSize()) {
					editPop.add(resultPop.get(Config.RANDOM.nextInt(resultPop.size())));
					System.out.print(":");
				}
			}
		} else if(newSeeds.isEmpty()) {//copy individuals already in population
			while(editPop.size() < getEditPopSize()) {
				editPop.add(editPop.get(Config.RANDOM.nextInt(editPop.size())));
				System.out.print(".");
			}
		} else if(editPop.size() < getEditPopSize()) {//copy the new seeds
			while(editPop.size() < getEditPopSize()) {
				editPop.add(newSeeds.get(Config.RANDOM.nextInt(newSeeds.size())));
				System.out.print(".");
			}
		}
		System.out.println();
		//		double avgGen = 0;TODO: test code
		//		int maxGen = 0;
		//		double avgFit = 0;
		//		double maxFit = 0;
		//		for(Rule r : editPop) {
		//			avgGen += r.getGeneration();
		//			if(r.getGeneration() > maxGen)
		//				maxGen = r.getGeneration();
		//			double fit = RuleFitnessComparator.getFitnessScore(r, editPop, resultPop, dataAttributes);
		//			avgFit += fit;
		//			if(fit > maxFit)
		//				maxFit = fit;
		//		}
		//		avgGen = avgGen/editPop.size();
		//		avgFit = avgFit/editPop.size();
		//		System.out.println(Config.LEVEL_SPACES + spaces + "Average generation in pop: " + avgGen + " (max=" + maxGen +")");
		//		System.out.println(Config.LEVEL_SPACES + spaces + "Average fitness: " + avgFit + " (max=" + maxFit +")");
	}

	private IntegrationReport sort(RulePopulation population, String spaces) {
		System.out.print(spaces + "Sorting population...");
		if(fitnessSorter == null)
			fitnessSorter = new RuleFitnessComparator(editPop, resultPop, dataAttributes);
		Collections.sort(population, fitnessSorter);
		System.out.println("Done");
		//add conforming mined rules to result pop
		IntegrationReport report = new IntegrationReport();
		for(Rule r : population) {
			if(r instanceof BatchRule) {
				for(SingleRule sr : r.rules())
					if(sr.getConformancePercentage_Traces() >= Config.MINIMAL_CONFORMANCE) {
						//					|| (Config.ALLOW_MINIMAL_CONFORMANCE_INTERMEDIATE && sr.getConformancePercentage() >= Config.MINIMAL_CONFORMANCE_INTERMEDIATE)) {
						IntegrationReport newRep = resultPop.integrate(r);
						if(newRep != null)
							report.add(newRep);
						break;
					}
			} else
				throw new RuntimeException("TODO?");
		}
		return report;
	}
}