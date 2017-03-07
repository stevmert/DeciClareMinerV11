package miner.rule;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import miner.Config;
import miner.subminer.genetic.RulePopulation;
import miner.subminer.genetic.RulePopulationWithDiversityMap;
import miner.subminer.genetic.SubRuleGeneralityComparator;
import model.Constraint;
import model.constraint.BoundedConstraint;
import model.constraint.RelationConstraint;
import model.constraint.existence.AtLeast;
import model.constraint.existence.OccurrenceConstraint;
import model.constraint.existence.extra.ExtremitiesConstraint;
import model.constraint.relation.SequenceRelationConstraint;
import model.data.DataAttribute;
import model.data.DecisionRule;

public class RuleFitnessComparator implements Comparator<Rule> {

	private static final SubRuleGeneralityComparator subRuleGeneralityComparator = new SubRuleGeneralityComparator();

	private final RulePopulation intermediatePop;
	private final RulePopulationWithDiversityMap resultPop;
	private final DataAttribute[] dataAttributes;

	public RuleFitnessComparator(RulePopulation intermediatePop, RulePopulationWithDiversityMap resultPop,
			DataAttribute[] dataAttributes) {
		super();
		this.intermediatePop = intermediatePop;
		this.resultPop = resultPop;
		this.dataAttributes = dataAttributes;
	}

	public static double getFitnessScore_confidence(Rule r) {
		if(r instanceof BatchRule) {
			BatchRule br = (BatchRule) r;
			BatchRule seed = (BatchRule) r.getSeed();
			if(br.getConstraint() instanceof OccurrenceConstraint) {
				//AtMost and AtLeast separately each with same logic as SequenceRelationConstraint
				//Combine AtMost and AtLeast by taking max score
				List<SingleRule> list = br.rules();
				List<SingleRule> seedList = seed.rules();
				Collections.sort(list, subRuleGeneralityComparator);
				Collections.sort(seedList, subRuleGeneralityComparator);
				int weightSum_most = 0;
				double score_most = 0;
				int i_most = 0;
				int weightSum_least = 0;
				double score_least = 0;
				int i_least = 0;
				for(SingleRule s_this : list) {
					SingleRule s_seed = getSeedRule(s_this, seedList);
					double s_this_getConformancePercentage_Traces = s_this.getConformancePercentage_Traces();
					double s_this_getConformancePercentage = s_this.getConformancePercentage();
					double s_seed_getConformancePercentage_Traces = 0;
					double s_seed_getConformancePercentage = 0;
					boolean doCalc = true;
					if(s_seed == null) {
						//in dit geval ofwel fout in programma in eerdere stap, ofwel van situatie:
						//decision-independent seed:	AtL3==100%
						//decision-dependent r:			AtM2<100% of AtM1<100% of AtM0<100%
						//	==> wel nuttig!!! (maar enkel bij AtMost?)
						if(s_this.getConstraint() instanceof AtLeast)
							throw new RuntimeException("This subrule should not have been mined: " + s_this
									+ " (not contained by seed " +br.rules() + ")");
					}
					if(doCalc) {
						double tmp = ((Config.FITNESS_CONF_TRACES_WEIGHT*s_this_getConformancePercentage_Traces
								+Config.FITNESS_CONF_GEN_WEIGHT*s_this_getConformancePercentage)
								-(Config.FITNESS_CONF_TRACES_WEIGHT*s_seed_getConformancePercentage_Traces
										+Config.FITNESS_CONF_GEN_WEIGHT*s_seed_getConformancePercentage))
								/(100-(Config.FITNESS_CONF_TRACES_WEIGHT*s_seed_getConformancePercentage_Traces
										+Config.FITNESS_CONF_GEN_WEIGHT*s_seed_getConformancePercentage));
						if(s_this.getConstraint() instanceof AtLeast) {
							int w = 1+i_least*Config.FITNESS_CONF_GENERALIY_WEIGHTINCREASE;
							score_least += tmp*w;
							weightSum_least += w;
							i_least++;
						} else {
							int w = 1+i_most*Config.FITNESS_CONF_GENERALIY_WEIGHTINCREASE;
							score_most += tmp*w;
							weightSum_most += w;
							i_most++;
						}
					}
				}
				if(weightSum_least == 0)
					return score_most/weightSum_most;
				if(weightSum_most == 0)
					return score_least/weightSum_least;
				return Math.max(score_least/weightSum_least, score_most/weightSum_most);
			} else if(br.getConstraint() instanceof ExtremitiesConstraint) {
				double score = -100;
				for(SingleRule s_this : br.rules()) {
					SingleRule s_seed = getSeedRule(s_this, seed.rules());
					double tmp = ((Config.FITNESS_CONF_TRACES_WEIGHT*s_this.getConformancePercentage_Traces()
							+Config.FITNESS_CONF_GEN_WEIGHT*s_this.getConformancePercentage())
							-(Config.FITNESS_CONF_TRACES_WEIGHT*s_seed.getConformancePercentage_Traces()
									+Config.FITNESS_CONF_GEN_WEIGHT*s_seed.getConformancePercentage()))
							/(100-Config.FITNESS_CONF_TRACES_WEIGHT*s_seed.getConformancePercentage_Traces()
									+Config.FITNESS_CONF_GEN_WEIGHT*s_seed.getConformancePercentage());
					score = Math.max(score, tmp);
				}
				return score;
			} else if(br.getConstraint() instanceof SequenceRelationConstraint) {
				List<SingleRule> list = br.rules();
				List<SingleRule> seedList = seed.rules();
				Collections.sort(list, subRuleGeneralityComparator);
				Collections.sort(seedList, subRuleGeneralityComparator);
				int weightSum_pos = 0;
				double score_pos = 0;
				int i_pos = 0;
				int weightSum_neg = 0;
				double score_neg = 0;
				int i_neg = 0;
				for(SingleRule s_this : list) {
					SingleRule s_seed = getSeedRule(s_this, seedList);
					if(s_seed == null)
						throw new RuntimeException("This subrule should not have been mined: " + s_this
								+ " (not contained by seed " +br.rules() + ")");
					double tmp = ((Config.FITNESS_CONF_TRACES_WEIGHT*s_this.getConformancePercentage_Traces()
							+Config.FITNESS_CONF_GEN_WEIGHT*s_this.getConformancePercentage())
							-(Config.FITNESS_CONF_TRACES_WEIGHT*s_seed.getConformancePercentage_Traces()
									+Config.FITNESS_CONF_GEN_WEIGHT*s_seed.getConformancePercentage()))
							/(100-Config.FITNESS_CONF_TRACES_WEIGHT*s_seed.getConformancePercentage_Traces()
									+Config.FITNESS_CONF_GEN_WEIGHT*s_seed.getConformancePercentage());
					if(((RelationConstraint) s_this.getConstraint()).hasAtMostConsequence()) {
						int w = 1+i_neg*Config.FITNESS_CONF_GENERALIY_WEIGHTINCREASE;
						score_neg += tmp*w;
						weightSum_neg += w;
						i_neg++;
					} else {
						int w = 1+i_pos*Config.FITNESS_CONF_GENERALIY_WEIGHTINCREASE;
						score_pos += tmp*w;
						weightSum_pos += w;
						i_pos++;
					}
				}
				if(weightSum_pos == 0)
					return score_neg/weightSum_neg;
				if(weightSum_neg == 0)
					return score_pos/weightSum_pos;
				return Math.max(score_pos/weightSum_pos, score_neg/weightSum_neg);
			} else
				throw new RuntimeException("TODO?");
		} else
			throw new RuntimeException("TODO?");
	}

	private static SingleRule getSeedRule(SingleRule s, List<SingleRule> seedList) {
		//closest related for bounded!
		if(s.getConstraint() instanceof BoundedConstraint) {
			SingleRule closestRelated = null;
			for(SingleRule s2 : seedList)
				if(s.getConstraint().isRelatedTo(s2.getConstraint())
						&& (closestRelated == null
						|| s2.getConstraint().isRelatedTo(closestRelated.getConstraint()))) {
					closestRelated = s2;
					if(((BoundedConstraint) s.getConstraint()).getBound() == ((BoundedConstraint) closestRelated.getConstraint()).getBound())
						break;
				}
			return closestRelated;
		} else {
			for(SingleRule s2 : seedList)
				if(s.getConstraint().isRelatedTo(s2.getConstraint()))
					return s2;
			return null;
		}
	}

//	public static double getFitnessScore_diversity_classOnly(Rule r, RulePopulation pop) {
//		int nrSame = pop.get(r.getConstraint().getClass()).size();
//		boolean isConstainedByPop = false;
//		for(Rule tmp : pop.get(r.getConstraint().getClass()))
//			if(tmp == r) {
//				isConstainedByPop = true;
//				break;
//			}
//		double x = pop.size()-nrSame;
//		int y = pop.size();
//		if(isConstainedByPop)
//			y--;
//		return x/y;
//	}

	public static double getFitnessScore_diversity_decisionless_intermediatePop(Rule r, RulePopulation intermediatePop) {
		int nrSame = 0;
		boolean isConstainedByPop = false;
		for(Rule tmp : intermediatePop) {
			if(tmp == r) {
				nrSame++;
				isConstainedByPop = true;
			} else if(tmp.getConstraint().equals(r.getConstraint(), false))
				nrSame++;
		}
		double x = intermediatePop.size()-nrSame;
		double y = intermediatePop.size();
		if(isConstainedByPop)
			y--;
		return x/y;
	}

	public static double getFitnessScore_diversity_decisionless_resultPop(Rule r,
			RulePopulationWithDiversityMap resultPop) {
		if(resultPop.isEmpty())
			return 1;
		Constraint decisionless = r.getConstraint().getDecisionlessCopy();
		return resultPop.getDiversity(decisionless);
	}

	/**
	 * Lower score for higher generations (exponential punishments). Keeps population dynamic...
	 */
	public static double getFitnessScore_popDominance(Rule r) {
		Double score = generationScoreCache.get(r.getGeneration());
		if(score == null) {
			score = (101 - Math.exp(((double) r.getGeneration()) / Config.GENERATION_DIVERSITY_FACTOR))/100;
			generationScoreCache.put(r.getGeneration(), score);
		}
		return score;
	}
	private static HashMap<Integer, Double> generationScoreCache = new HashMap<>();

	public static double getFitnessScore_decisionGenerality(Rule r, DataAttribute[] dataAttributes) {
		DecisionRule dr_this = null;
		if(r.getConstraint().getActivationDecision().getRules().size() != 1)
			throw new RuntimeException("TODO?");
		for(DecisionRule dr : r.getConstraint().getActivationDecision().getRules())//should be max 1 rule
			dr_this = dr;
		int count = 0;
		for(DataAttribute da1 : dataAttributes)
			for(DataAttribute da2 : dr_this.getDataValues())
				if(da1.getName().equals(da2.getName())) {
					count++;
					break;
				}
		//		1 == 100%
		//		dataAttributes.length == 0
		return (dataAttributes.length - count)
				/(dataAttributes.length - 1);
	}

	@Override
	public int compare(Rule r1, Rule r2) {
		double score1 = getFitnessScore(r1, intermediatePop, resultPop, dataAttributes);
		double score2 = getFitnessScore(r2, intermediatePop, resultPop, dataAttributes);
		if(score1 > score2)
			return -1;
		if(score1 < score2)
			return 1;
		return 0;
	}

	public static double getFitnessScore(Rule r, RulePopulation intermediatePop,
			RulePopulationWithDiversityMap resultPop, DataAttribute[] dataAttributes) {
		return (
				getFitnessScore_confidence(r)*Config.FITNESS_CONF_WEIGHT
//				+ getFitnessScore_diversity_class(r, resultPop)*Config.FITNESS_DIV_WEIGHT
//				+ getFitnessScore_diversity_decisionless_intermediatePop(r, intermediatePop)*Config.FITNESS_DIV_WEIGHT
				+ getFitnessScore_diversity_decisionless_resultPop(r, resultPop)*Config.FITNESS_DIV_WEIGHT
				+ getFitnessScore_popDominance(r)*Config.FITNESS_DOM_WEIGHT
				+ getFitnessScore_decisionGenerality(r, dataAttributes)*Config.FITNESS_DECGEN_WEIGHT
				)
				/
				(Config.FITNESS_CONF_WEIGHT
						+Config.FITNESS_DIV_WEIGHT
						+Config.FITNESS_DOM_WEIGHT
						+Config.FITNESS_DECGEN_WEIGHT);
	}
}