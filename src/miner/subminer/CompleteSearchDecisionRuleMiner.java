package miner.subminer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;

import miner.Config;
import miner.Miner;
import miner.kb.KnowledgeBase;
import miner.log.Log;
import miner.rule.BatchRule;
import miner.rule.Rule;
import miner.rule.SingleRule;
import miner.subminer.genetic.IntegrationReport;
import miner.subminer.genetic.RulePopulation;
import model.Constraint;
import model.data.BooleanDataAttribute;
import model.data.CategoricalDataAttribute;
import model.data.DataAttribute;
import model.data.Decision;
import model.data.DecisionRule;

public class CompleteSearchDecisionRuleMiner extends Miner {

	private final DataAttribute[] dataAttributes;//every data attribute once (=/= every value once!)
	private final Rule[] seeds;
	private final Integer seedToCheck;//null= check all, otherwise = index of only seed to check

	public CompleteSearchDecisionRuleMiner(Log log, KnowledgeBase kb, Rule[] seeds, Integer seedToCheck) {
		super(log);
		if(seedToCheck != null
				&& (seedToCheck < 0 || seedToCheck >= seeds.length))
			throw new IllegalArgumentException();
		this.seeds = seeds;
		this.seedToCheck = seedToCheck;
		this.dataAttributes = kb.getDataElements().toArray(new DataAttribute[kb.getDataElements().size()]);
	}

	public CompleteSearchDecisionRuleMiner(Log log, KnowledgeBase kb, Rule[] seeds) {
		this(log, kb, seeds, null);
	}

	@Override
	public List<Rule> execute() throws Exception {
		System.out.println("Phase 2 " + this.getClass().getSimpleName() + " mining...");
		if(dataAttributes.length == 0) {
			System.out.println("No data elements to mine!");
			return new ArrayList<>();
		}
		if(seeds.length == 0) {
			System.out.println("No seeds!");
			return new ArrayList<>();
		}
		List<HashSet<DataAttribute>> allDataCombinations = getAllCombinations();

		//		if(Config.DO_LOGGING)
		//			Logger.getInstance().logP2();

		RulePopulation resultPop = new RulePopulation();
		long t_start_total = System.currentTimeMillis();
		for(int i1 = 0; i1 < seeds.length; i1++) {
			if(seedToCheck != null)
				i1 = seedToCheck;
			IntegrationReport report = new IntegrationReport();
			System.out.println(Config.LEVEL_SPACES + "Mining seed " + (i1+1) + "/" + seeds.length + " starting...");
			File preloadedResultsSeedX = new File(Config.PRELOADING_COMPLETE_RULESSEED + i1 + "_"
					+ Config.MINIMAL_CONFORMANCE + "_" + Config.MINIMAL_SUPPORT + "_" + Config.MAX_BRANCHING_LEVEL
					+ Config.PRELOADING_EXT);
			long t_start = System.currentTimeMillis();
			if(preloadedResultsSeedX.exists()) {//use preloaded results...
				ObjectInputStream input = null;
				try {
					input = new ObjectInputStream(new FileInputStream(preloadedResultsSeedX));
					RulePopulation subPop = (RulePopulation) input.readObject();
					input.close();
					for(Rule r : subPop) {
						IntegrationReport newRep = resultPop.integrate(r);
						if(newRep != null)
							report.add(newRep);
					}
				} catch (Exception ex) {
					throw new RuntimeException("An error occurred when loading the results for the complete search seed "
							+ seedToCheck + "!", ex);
				}
			} else {//calculate results
				int nextPercentageToShow = 1;
				for(int i2 = 0; i2 < allDataCombinations.size(); i2++) {
					if((100d*(i2+1))/allDataCombinations.size() >= nextPercentageToShow) {
						System.out.println(Config.LEVEL_SPACES + Config.LEVEL_SPACES
								+ "Mining seed " + (i1+1) + "/" + seeds.length
								+ " with decision " + (i2+1) + "/" + allDataCombinations.size()
								+ " (" + nextPercentageToShow + "% in " + (System.currentTimeMillis() - t_start) + "ms)...");
						nextPercentageToShow++;
					}
					DecisionRule dr = new DecisionRule(allDataCombinations.get(i2));
					Decision d = new Decision();
					d.addRule(dr);
					Constraint c = seeds[i1].getConstraint().getDecisionlessCopy();
					c.setActivationDecision(d);
					Rule r = c.evaluate(getLog(), seeds[i1]);
					if(r != null)
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
			}
			long t_end = System.currentTimeMillis();
			System.out.println(Config.LEVEL_SPACES + "Mining seed " + (i1+1) + "/" + seeds.length + " finished in "
					+ (t_end-t_start) + "ms");
			System.out.println(Config.LEVEL_SPACES + report.getSingleRulesAdded() + " new rules mined ("
					+ report.getBatchRulesAdded() + " aggregate rules)");
			System.out.println(Config.LEVEL_SPACES + report.getSingleRulesRemoved() + " rules replaced by better new rule ("
					+ report.getBatchRulesRemoved() + " aggregate rules)");
			System.out.println(Config.LEVEL_SPACES + "Result pop contains " + resultPop.size() + " aggregate rules");
			if(seedToCheck != null)
				break;
		}
		long t_end_total = System.currentTimeMillis();
		System.out.println("Phase 2 mining completed in " + (t_end_total-t_start_total) + "ms");

		if(seedToCheck != null) {//to make parallel computation possible
			System.out.print("Exporting ResPop...");
			ObjectOutputStream out = null;
			try {
				File preloadedResultsSeedX = new File(Config.PRELOADING_COMPLETE_RULESSEED + seedToCheck + "_"
						+ Config.MINIMAL_CONFORMANCE + "_" + Config.MINIMAL_SUPPORT + "_" + Config.MAX_BRANCHING_LEVEL
						+ Config.PRELOADING_EXT);
				out = new ObjectOutputStream(new FileOutputStream(preloadedResultsSeedX));
				out.writeObject(resultPop);
				out.close();
			} catch (Exception ex) {
				throw new RuntimeException("An error occurred when creating the results for the complete search seed "
						+ seedToCheck + "!", ex);
			}
			System.out.println("Done");
		}

		return resultPop;
	}

	private List<HashSet<DataAttribute>> getAllCombinations() {
		List<HashSet<DataAttribute>> res = new ArrayList<>();
		BitSet bs = new BitSet();
		while (bs.length() <= dataAttributes.length) {
			if(bs.length() > 0)
				res.addAll(getAllCombinations(bs)); 
			//Inc by 1
			int pos = bs.nextClearBit(0);
			bs.flip(0, pos + 1);
		}
		return res;
	}

	private List<HashSet<DataAttribute>> getAllCombinations(BitSet bs) {
		List<HashSet<DataAttribute>> allData = new ArrayList<>();
		for(int i = 0; i < bs.length(); i++)
			if(bs.get(i)) {
				DataAttribute da = dataAttributes[i];
				if(da instanceof BooleanDataAttribute) {
					if(allData.isEmpty()) {
						HashSet<DataAttribute> tmp1 = new HashSet<>();
						tmp1.add(new BooleanDataAttribute(da.getName(), true, da.getParent()));
						allData.add(tmp1);
						HashSet<DataAttribute> tmp2 = new HashSet<>();
						tmp2.add(new BooleanDataAttribute(da.getName(), false, da.getParent()));
						allData.add(tmp2);
					} else {
						for(HashSet<DataAttribute> tmp1 : new ArrayList<>(allData)) {
							HashSet<DataAttribute> tmp2 = new HashSet<>(tmp1);
							tmp1.add(new BooleanDataAttribute(da.getName(), true, da.getParent()));
							tmp2.add(new BooleanDataAttribute(da.getName(), false, da.getParent()));
							allData.add(tmp2);
						}
					}
				} else {
					//same logic repeated (= all combinations of categorical data attribute with BitSet)...
					CategoricalDataAttribute cda = (CategoricalDataAttribute) da;
					ArrayList<String> values = new ArrayList<>(cda.getValues());
					List<HashSet<DataAttribute>> catCombinations = new ArrayList<>();
					BitSet bs_cat = new BitSet();
					while (bs_cat.length() <= cda.getValues().size()) {
						if(bs_cat.length() > 0)
							catCombinations.add(getAllCombinations_cat(bs_cat, values, cda)); 
						//Inc by 1
						int pos = bs_cat.nextClearBit(0);
						bs_cat.flip(0, pos + 1);
					}
					//add all combinations...
					if(allData.isEmpty())
						allData.addAll(catCombinations);
					else {
						for(HashSet<DataAttribute> tmp1 : new ArrayList<>(allData)) {
							allData.remove(tmp1);
							for(HashSet<DataAttribute> ccomb : catCombinations) {
								HashSet<DataAttribute> tmp2 = new HashSet<>(tmp1);
								tmp2.addAll(ccomb);
								allData.add(tmp2);
							}
						}
					}
				}
			}
		return allData;
	}

	private HashSet<DataAttribute> getAllCombinations_cat(BitSet bs_cat, ArrayList<String> values,
			CategoricalDataAttribute cda) {
		HashSet<DataAttribute> tmp = new HashSet<>();
		for(int i = 0; i < bs_cat.length(); i++)
			if(bs_cat.get(i)) {
				String v = values.get(i);
				tmp.add(new CategoricalDataAttribute(cda.getName(), cda.getValues(), v, cda.getParent()));
			}
		return tmp;
	}
}