package miner.subminer.genetic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import miner.Config;
import miner.DeciClareMinerV11;
import miner.Miner;
import miner.kb.KnowledgeBase;
import miner.log.Log;
import miner.rule.Rule;
import miner.rule.RulePrintComparator;
import miner.rule.SingleRule;
import model.constraint.existence.OccurrenceConstraint;
import model.constraint.relation.SequenceRelationConstraint;
import model.data.DataAttribute;
import util.Logger;

public class SeededGeneticDecisionRuleMiner extends Miner {

	private final DataAttribute[] dataAttributes;//every data attribute once (=/= every value once!)
	private final Rule[] seeds_bounded;
	private final Rule[] seeds_relation;
	private final Rule[] seeds_other;

	public SeededGeneticDecisionRuleMiner(Log log, KnowledgeBase kb, Rule[] seeds) {
		super(log);
		ArrayList<Rule> seeds_boundedL = new ArrayList<>();
		ArrayList<Rule> seeds_relationL = new ArrayList<>();
		ArrayList<Rule> seeds_otherL = new ArrayList<>();
		for(Rule r : seeds) {
			if(r.getConstraint() instanceof OccurrenceConstraint)
				seeds_boundedL.add(r);
			else if(r.getConstraint() instanceof SequenceRelationConstraint)
				seeds_relationL.add(r);
			else
				seeds_otherL.add(r);
		}
		this.seeds_bounded = seeds_boundedL.toArray(new Rule[seeds_boundedL.size()]);
		this.seeds_relation = seeds_relationL.toArray(new Rule[seeds_relationL.size()]);
		this.seeds_other = seeds_otherL.toArray(new Rule[seeds_otherL.size()]);
		this.dataAttributes = kb.getDataElements().toArray(new DataAttribute[kb.getDataElements().size()]);
	}

	@Override
	public List<Rule> execute() throws Exception {
		System.out.println("Phase 2 " + this.getClass().getSimpleName() + " mining...");
		if(dataAttributes.length == 0) {
			System.out.println("No data elements to mine!");
			return new ArrayList<>();
		}
		if(seeds_bounded.length + seeds_relation.length + seeds_other.length == 0) {
			System.out.println("No seeds!");
			return new ArrayList<>();
		}

		int popSize_other = 0;
		int popSize_bounded = 0;
		int popSize_relation = 0;
		if(seeds_other.length > 0)
			popSize_other = Math.max(Config.MINIMAL_SUBPOPULATIONSIZE_EDIT,
					(int) Math.round(((double) Config.POPULATIONSIZE_EDIT_TOTAL)
							* (((double) seeds_other.length)/(seeds_bounded.length+seeds_relation.length+seeds_other.length))));
		{
			int popSize_rest = Config.POPULATIONSIZE_EDIT_TOTAL - popSize_other;
			if(seeds_bounded.length > 0 && seeds_relation.length > 0) {
				popSize_bounded = popSize_rest/2;
				popSize_relation = popSize_rest-popSize_bounded;
			} else if(seeds_bounded.length > 0)
				popSize_bounded = popSize_rest;
			else if(seeds_relation.length > 0)
				popSize_relation = popSize_rest;
		}
		if(Config.DO_LOGGING)
			Logger.getInstance().logP2(popSize_bounded, seeds_bounded.length,
					popSize_relation, seeds_relation.length,
					popSize_other, seeds_other.length);

		SeededGeneticDecisionRuleSubMiner subMiner_bounded = null;
		if(seeds_bounded.length > 0) {
			subMiner_bounded = new SeededGeneticDecisionRuleSubMiner("BoundedSubMiner",
					getLog(), dataAttributes, seeds_bounded, popSize_bounded);
			Logger.getInstance().logP2_bounded();
		}
		SeededGeneticDecisionRuleSubMiner subMiner_relation = null;
		if(seeds_relation.length > 0) {
			subMiner_relation = new SeededGeneticDecisionRuleSubMiner("RelationSubMiner",
					getLog(), dataAttributes, seeds_relation, popSize_relation);
			Logger.getInstance().logP2_relation();
		}
		SeededGeneticDecisionRuleSubMiner subMiner_other = null;
		if(seeds_other.length > 0) {
			subMiner_other = new SeededGeneticDecisionRuleSubMiner("OtherSubMiner",
					getLog(), dataAttributes, seeds_other, popSize_other);
			Logger.getInstance().logP2_other();
		}

		ArrayList<IntegrationReport> pastReports_bounded = new ArrayList<>();
		ArrayList<IntegrationReport> pastReports_relation = new ArrayList<>();
		ArrayList<IntegrationReport> pastReports_other = new ArrayList<>();
		long t_start = System.currentTimeMillis();
		for(int i = 0; true; i++) {
			System.out.println("Step " + i + " starting...");
			if(subMiner_bounded != null
					&& !subMiner_bounded.hasConverged()) {
				long t_sub_start = System.currentTimeMillis();
				pastReports_bounded.add(subMiner_bounded.doIteration(Config.LEVEL_SPACES));
				long t_sub_end = System.currentTimeMillis();
				if(i != 0 && System.currentTimeMillis() - t_start > Config.MAX_SEARCHTIME_GENETIC)//ensure at least 1 iteration for each pop
					break;
				if(checkConvergence(subMiner_bounded, pastReports_bounded))
					System.out.println(subMiner_bounded.getName() + " has converged!!!");
				Logger.getInstance().logStat_p2_bounded(i,
						pastReports_bounded.get(pastReports_bounded.size()-1).getBatchRulesAdded(),
						pastReports_bounded.get(pastReports_bounded.size()-1).getBatchRulesRemoved(),
						pastReports_bounded.get(pastReports_bounded.size()-1).getSingleRulesAdded(),
						pastReports_bounded.get(pastReports_bounded.size()-1).getSingleRulesRemoved(),
						t_sub_end-t_sub_start);
			}
			if(subMiner_relation != null
					&& !subMiner_relation.hasConverged()) {
				long t_sub_start = System.currentTimeMillis();
				pastReports_relation.add(subMiner_relation.doIteration(Config.LEVEL_SPACES));
				long t_sub_end = System.currentTimeMillis();
				if(i != 0 && System.currentTimeMillis() - t_start > Config.MAX_SEARCHTIME_GENETIC)//ensure at least 1 iteration for each pop
					break;
				if(checkConvergence(subMiner_relation, pastReports_relation))
					System.out.println(subMiner_relation.getName() + " has converged!!!");
				Logger.getInstance().logStat_p2_relation(i,
						pastReports_relation.get(pastReports_relation.size()-1).getBatchRulesAdded(),
						pastReports_relation.get(pastReports_relation.size()-1).getBatchRulesRemoved(),
						pastReports_relation.get(pastReports_relation.size()-1).getSingleRulesAdded(),
						pastReports_relation.get(pastReports_relation.size()-1).getSingleRulesRemoved(),
						t_sub_end-t_sub_start);
			}
			if(subMiner_other != null
					&& !subMiner_other.hasConverged()) {
				long t_sub_start = System.currentTimeMillis();
				pastReports_other.add(subMiner_other.doIteration(Config.LEVEL_SPACES));
				long t_sub_end = System.currentTimeMillis();
				if(checkConvergence(subMiner_other, pastReports_other))
					System.out.println(subMiner_other.getName() + " has converged!!!");
				Logger.getInstance().logStat_p2_other(i,
						pastReports_other.get(pastReports_other.size()-1).getBatchRulesAdded(),
						pastReports_other.get(pastReports_other.size()-1).getBatchRulesRemoved(),
						pastReports_other.get(pastReports_other.size()-1).getSingleRulesAdded(),
						pastReports_other.get(pastReports_other.size()-1).getSingleRulesRemoved(),
						t_sub_end-t_sub_start);
			}
			
			//write out all rules found every iteration (ignore the time this took!)
			if(Config.PHASE2_STORE_INTERMEDIATE_RESPOPS) {
				long t_sub_start = System.currentTimeMillis();
				System.out.print("Creating intermediate ResPop file...");
				ArrayList<Rule> res = new ArrayList<>();
				if(subMiner_bounded != null)
					res.addAll(subMiner_bounded.getResultPop());
				if(subMiner_relation != null)
					res.addAll(subMiner_relation.getResultPop());
				if(subMiner_other != null)
					res.addAll(subMiner_other.getResultPop());
				//filter
				List<SingleRule> res_mergeAndFilter = DeciClareMinerV11.mergeAndFilter(res, getLog());
				Collections.sort(res_mergeAndFilter, new RulePrintComparator());
				//write out
				String text = Rule.getCSV_HEADERS();
				for(SingleRule r : res_mergeAndFilter) {
					if(Config.SHOW_RESULT_RULES)
						System.out.println(r);
					text += "\n" + ((SingleRule) r).getCSV();
				}
				Logger.getInstance().writeToFile(text.trim(), Config.FILENAME_INTERMEDIATE_P2 + i, Config.FILE_CSV);
				System.out.println("Done");
				t_start += System.currentTimeMillis()-t_sub_start
						+500;//compensation for delays due to write (mem)
			}

			if(System.currentTimeMillis() - t_start > Config.MAX_SEARCHTIME_GENETIC)
				break;

			System.out.println("Step " + i + " finished...");
		}
		System.out.println("Phase 2 mining completed");
		ArrayList<Rule> res = new ArrayList<>();
		if(subMiner_bounded != null)
			res.addAll(subMiner_bounded.getResultPop());
		if(subMiner_relation != null)
			res.addAll(subMiner_relation.getResultPop());
		if(subMiner_other != null)
			res.addAll(subMiner_other.getResultPop());
		return res;
	}

	private boolean checkConvergence(SeededGeneticDecisionRuleSubMiner subMiner,
			ArrayList<IntegrationReport> pastReports) {
		if(pastReports.size() < Config.CHECK_CHANGE_NR_OF_ITERATIONS)
			return false;
		int count = 0;
		for(IntegrationReport pastReport : pastReports)
			count += pastReport.getSingleRulesAdded();
		if(count == 0) {
			subMiner.setConvergence(true);
			return true;
		} else
			pastReports.remove(0);
		return false;
	}
}