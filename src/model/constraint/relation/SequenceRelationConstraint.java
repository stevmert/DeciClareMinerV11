package model.constraint.relation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import miner.Config;
import miner.log.ActivityEvent;
import miner.log.DecisionActivation;
import miner.log.Log;
import miner.log.Trace;
import miner.rule.BatchRule;
import miner.rule.Rule;
import miner.rule.SingleRule;
import miner.rule.SingleRuleWithTimings;
import miner.subminer.MineRelationProfile;
import model.Constraint;
import model.constraint.RelationConstraint;
import model.constraint.existence.AtLeast;
import model.constraint.existence.AtMost;
import model.data.Decision;
import model.data.DecisionRule;
import model.expression.ActivityExpression;
import model.expression.AtomicActivityExpression;
import model.expression.AtomicExistenceExpression;
import model.expression.ExistenceExpression;
import model.expression.LogicalOperator;
import model.expression.NonAtomicActivityExpression;
import util.IndexResult;

public class SequenceRelationConstraint extends Constraint {

	private static final long serialVersionUID = -4594945752570329809L;

	private ActivityExpression conditionExpression;
	private ActivityExpression consequenceExpression;
	private MineRelationProfile mineRelationProfile;

	public SequenceRelationConstraint(Decision activationDecision, Decision deactivationDec,
			ActivityExpression conditionExpression, ActivityExpression consequenceExpression,
			boolean isOptional, MineRelationProfile mineRelationProfile) {
		super(activationDecision, deactivationDec, isOptional);
		this.conditionExpression = conditionExpression;
		this.consequenceExpression = consequenceExpression;
		this.mineRelationProfile = mineRelationProfile;
	}

	public ActivityExpression getConditionExpression() {
		return conditionExpression;
	}

	public void setConditionExpression(ActivityExpression conditionExpression) {
		this.conditionExpression = conditionExpression;
	}

	public ActivityExpression getConsequenceExpression() {
		return consequenceExpression;
	}

	public void setConsequenceExpression(ActivityExpression consequenceExpression) {
		this.consequenceExpression = consequenceExpression;
	}

	public MineRelationProfile getMineRelationProfile() {
		return mineRelationProfile;
	}

	public void setMineRelationProfile(MineRelationProfile mineRelationProfile) {
		this.mineRelationProfile = mineRelationProfile;
	}public static boolean isSearchable(ActivityExpression cond, ActivityExpression conseq) {
		return isSearchableCondition(cond) && isSearchableConsequence(conseq);
	}

	public static boolean isSearchableCondition(ActivityExpression cond) {
		if(cond instanceof AtomicActivityExpression)
			return true;
		NonAtomicActivityExpression tmp = (NonAtomicActivityExpression) cond;
		for(ActivityExpression a : tmp.getExpressions())
			if(a instanceof NonAtomicActivityExpression)
				return false;
		return tmp.getOperator().equals(LogicalOperator.AND);
	}

	public static boolean isSearchableConsequence(ActivityExpression conseq) {
		if(conseq instanceof AtomicActivityExpression)
			return true;
		NonAtomicActivityExpression tmp = (NonAtomicActivityExpression) conseq;
		for(ActivityExpression a : tmp.getExpressions())
			if(a instanceof NonAtomicActivityExpression)
				return false;
		return true;
	}

	protected static boolean doPositiveTemplates(ActivityExpression conseq) {
		if(conseq instanceof AtomicActivityExpression)
			return true;
		NonAtomicActivityExpression tmp = (NonAtomicActivityExpression) conseq;
		return tmp.getOperator().equals(LogicalOperator.OR);
	}

	protected static boolean doNegativeTemplates(ActivityExpression conseq) {
		if(conseq instanceof AtomicActivityExpression)
			return true;
		NonAtomicActivityExpression tmp = (NonAtomicActivityExpression) conseq;
		return tmp.getOperator().equals(LogicalOperator.AND);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((conditionExpression == null) ? 0 : conditionExpression.hashCode());
		result = prime * result + ((consequenceExpression == null) ? 0 : consequenceExpression.hashCode());
		result = prime * result + ((mineRelationProfile == null) ? 0 : mineRelationProfile.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return equals(obj, true);
	}

	public boolean equals(Object obj, boolean checkDecisions) {
		if (this == obj)
			return true;
		if (!super.equals(obj, checkDecisions))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SequenceRelationConstraint other = (SequenceRelationConstraint) obj;
		if (conditionExpression == null) {
			if (other.conditionExpression != null)
				return false;
		} else if (!conditionExpression.equals(other.conditionExpression))
			return false;
		if (consequenceExpression == null) {
			if (other.consequenceExpression != null)
				return false;
		} else if (!consequenceExpression.equals(other.consequenceExpression))
			return false;
		if (mineRelationProfile == null) {
			if (other.mineRelationProfile != null)
				return false;
		} else if (!mineRelationProfile.equals(other.mineRelationProfile))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + getConditionExpression()
		+ ", " + getConsequenceExpression() + ")" + super.toString();
	}

	@Override
	protected String getTextualConstraint() {
		return getConditionExpression() + " sequence relation to "
				+ getConsequenceExpression();
	}

	@Override
	public BatchRule evaluate(Log log, Rule ancestor) {
		return mineSequenceRelationConstraint(this.getActivationDecision(), this.getConditionExpression(),
				this.getConsequenceExpression(), this.getMineRelationProfile(), log, ancestor);
	}

	public static BatchRule mineSequenceRelationConstraint(Decision activationDecision,
			ActivityExpression conditionExpression, ActivityExpression consequenceExpression,
			MineRelationProfile mrp, Log log, Rule ancestor) {
		if(mrp.getFrom().isAtMost()
				|| (mrp.getTo().isAtMost() && mrp.getTo().getAtMost_UpperBound() > 0)
				|| (mrp.getFrom().isAtLeast() && mrp.getFrom().getAtLeast_UpperBound() > 1)
				|| (mrp.getTo().isAtLeast() && mrp.getTo().getAtLeast_UpperBound() > 1))
			throw new IllegalArgumentException("Unsupported MineRelationProfile!");
		if(!isSearchable(conditionExpression, consequenceExpression))
			throw new IllegalArgumentException("Unsupported mineSequenceRelationConstraint parameters!");

		boolean doPos = doPositiveTemplates(consequenceExpression);
		boolean doNeg = doNegativeTemplates(consequenceExpression);

		boolean ancestor_doChainPrecedence_pos = (ancestor == null);
		boolean ancestor_doChainPrecedence_neg = (ancestor == null);
		boolean ancestor_doPrecedence_pos = (ancestor == null);
		boolean ancestor_doPrecedence_neg = (ancestor == null);
		boolean ancestor_doRespondedPres_pos = (ancestor == null);
		boolean ancestor_doRespondedPres_neg = (ancestor == null);
		boolean ancestor_doResponse_pos = (ancestor == null);
		boolean ancestor_doResponse_neg = (ancestor == null);
		boolean ancestor_doChainResponse_pos = (ancestor == null);
		boolean ancestor_doChainResponse_neg = (ancestor == null);
		if(ancestor != null)
			for(SingleRule sr : ancestor.rules()) {
				if(sr.getConstraint() instanceof RespondedPresence) {
					if(((RelationConstraint) sr.getConstraint()).hasAtMostConsequence())
						ancestor_doRespondedPres_neg = true;
					else
						ancestor_doRespondedPres_pos = true;
				} else if(sr.getConstraint() instanceof Response) {
					if(((RelationConstraint) sr.getConstraint()).hasAtMostConsequence())
						ancestor_doResponse_neg = true;
					else
						ancestor_doResponse_pos = true;
				} else if(sr.getConstraint() instanceof ChainResponse) {
					if(((RelationConstraint) sr.getConstraint()).hasAtMostConsequence())
						ancestor_doChainResponse_neg = true;
					else
						ancestor_doChainResponse_pos = true;
				} else if(sr.getConstraint() instanceof Precedence) {
					if(((RelationConstraint) sr.getConstraint()).hasAtMostConsequence())
						ancestor_doPrecedence_neg = true;
					else
						ancestor_doPrecedence_pos = true;
				} else if(sr.getConstraint() instanceof ChainPrecedence) {
					if(((RelationConstraint) sr.getConstraint()).hasAtMostConsequence())
						ancestor_doChainPrecedence_neg = true;
					else
						ancestor_doChainPrecedence_pos = true;
				}
			}

		int countNrOfConfirmations_pres = 0;
		int countNrOfViolations_pres = 0;

		int countNrOfConfirmations_Traces_resp = 0;
		int countNrOfViolations_Traces_resp = 0;
		int countNrOfOtherViolations_Traces_resp = 0;
		int countNrOfViolations_resp = 0;
		ArrayList<Long> times_resp = new ArrayList<>();

		int countNrOfConfirmations_Traces_chainResp = 0;
		int countNrOfViolations_Traces_chainResp = 0;
		int countNrOfOtherViolations_Traces_chainResp = 0;
		int countNrOfViolations_chainResp = 0;
		ArrayList<Long> times_chainResp = new ArrayList<>();

		int countNrOfConfirmations_Traces_prec = 0;
		int countNrOfViolations_Traces_prec = 0;
		int countNrOfOtherViolations_Traces_prec = 0;
		int countNrOfViolations_prec = 0;
		ArrayList<Long> times_prec = new ArrayList<>();

		int countNrOfConfirmations_Traces_chainPrec = 0;
		int countNrOfViolations_Traces_chainPrec = 0;
		int countNrOfOtherViolations_Traces_chainPrec = 0;
		int countNrOfViolations_chainPrec = 0;
		ArrayList<Long> times_chainPrec = new ArrayList<>();

		HashSet<DecisionRule> usedDecisions = new HashSet<>();

		for(int i = 0; i < log.size(); i++) {
			Trace t = log.get(i);
			long activationTime = 0;
			if(activationDecision != null) {
				DecisionActivation decisionActivation = t.getDecisionActivation(activationDecision);
				if(decisionActivation != null) {
					activationTime = decisionActivation.getTime();
					usedDecisions.add(decisionActivation.getDecisionRule());
				} else
					activationTime = -1;
			}
			if(activationTime != -1) {
				List<ActivityEvent> acts = t.getRemainingActivityList(activationTime);
				//resPres
				if((Config.MINE_RESPONDEDPRESENCE && doPos && ancestor_doRespondedPres_pos)
						|| (Config.MINE_NOTRESPONDEDPRESENCE && doNeg && ancestor_doRespondedPres_neg)) {
					boolean containsCond = contains(conditionExpression, acts);
					boolean containsConseq = contains(consequenceExpression, acts);
					if(containsCond) {
						if(containsConseq)
							countNrOfConfirmations_pres++;
						else
							countNrOfViolations_pres++;
					}
				}
				if((((Config.MINE_RESPONSE && ancestor_doResponse_pos)  || (Config.MINE_CHAINRESPONSE && ancestor_doChainResponse_pos)) && doPos)
						|| ((Config.MINE_NOTRESPONSE && ancestor_doResponse_neg) || (Config.MINE_NOTCHAINRESPONSE && ancestor_doChainResponse_neg)) && doNeg) {
					//other 2 forwards
					//TODO: support alternate response
					IndexResult indexCond = indexOf(conditionExpression, acts, -1, -1);
					if(indexCond != null) {
						boolean restIsViolating_resp = false;
						boolean hasConformed_resp = false;
						boolean restIsViolating_chainResp = false;
						boolean hasConformed_chainResp = false;
						boolean hasViolated_chainResp = false;
						while(true) {
							IndexResult indexConseq = indexOf(consequenceExpression, acts
									.subList(indexCond.getIndex_end() + 1, acts.size()), -1,
									acts.get(indexCond.getIndex_end()).getEnd());
							//response
							if((Config.MINE_RESPONSE && doPos && ancestor_doResponse_pos)
									|| (Config.MINE_NOTRESPONSE && doNeg && ancestor_doResponse_neg)) {
								if(restIsViolating_resp
										|| indexConseq == null) {
									restIsViolating_resp = true;
									countNrOfViolations_resp++;
								} else {
									hasConformed_resp = true;
									int indexToStart = indexConseq.getIndex_start() + indexCond.getIndex_end() + 1;
									long countDiff = acts.get(indexToStart).getStart()
											- acts.get(indexCond.getIndex_end()).getEnd();
									if(countDiff < 0)
										throw new IllegalArgumentException("countDiff < 0???");
									else
										times_resp.add(countDiff);
								}
							}
							//chain response
							if((Config.MINE_CHAINRESPONSE && doPos && ancestor_doChainResponse_pos)
									|| (Config.MINE_NOTCHAINRESPONSE && doNeg && ancestor_doChainResponse_neg)) {
								if(restIsViolating_chainResp
										|| indexConseq == null
										|| indexConseq.getIndex_start() == -1
										|| indexConseq.getIndex_end() == -1) {
									restIsViolating_chainResp = true;
									hasViolated_chainResp = true;
									countNrOfViolations_chainResp++;
								} else {
									long firstTimingAfter = -1;
									for(int j = indexCond.getIndex_end()+1; j < acts.size(); j++) {
										if(acts.get(j).getStart()
												>= acts.get(indexCond.getIndex_end()).getEnd()) {
											firstTimingAfter = acts.get(j).getStart();
											break;
										}
									}
									if(firstTimingAfter == -1) {
										restIsViolating_chainResp = true;
										hasViolated_chainResp = true;
										countNrOfViolations_chainResp++;
									} else {
										int indexToStart = indexConseq.getIndex_start() + indexCond.getIndex_end() + 1;
										if(acts.get(indexToStart).getStart() == firstTimingAfter) {
											long countDiff = acts.get(indexToStart).getStart()
													- acts.get(indexCond.getIndex_end()).getEnd();
											if(countDiff < 0)
												throw new IllegalArgumentException("countDiff < 0???");
											else {
												hasConformed_chainResp = true;
												times_chainResp.add(countDiff);
											}
										} else {
											hasViolated_chainResp = true;
											countNrOfViolations_chainResp++;
										}
									}
								}
							}

							indexCond = indexOf(conditionExpression, acts, indexCond.getIndex_start()+1, -1);
							if(indexCond == null)
								break;
						}
						//response
						if((Config.MINE_RESPONSE && doPos && ancestor_doResponse_pos)
								|| (Config.MINE_NOTRESPONSE && doNeg && ancestor_doResponse_neg)) {
							if(!restIsViolating_resp && hasConformed_resp)
								countNrOfConfirmations_Traces_resp++;
							else if(restIsViolating_resp && !hasConformed_resp)
								countNrOfViolations_Traces_resp++;
							else
								countNrOfOtherViolations_Traces_resp++;
						}
						//chain response
						if((Config.MINE_CHAINRESPONSE && doPos && ancestor_doChainResponse_pos)
								|| (Config.MINE_NOTCHAINRESPONSE && doNeg && ancestor_doChainResponse_neg)) {
							if(!hasViolated_chainResp && hasConformed_chainResp)
								countNrOfConfirmations_Traces_chainResp++;
							else if(hasViolated_chainResp && !hasConformed_chainResp)
								countNrOfViolations_Traces_chainResp++;
							else
								countNrOfOtherViolations_Traces_chainResp++;
						}
					}
				}
				if((((Config.MINE_PRECEDENCE && ancestor_doPrecedence_pos) || (Config.MINE_CHAINPRECEDENCE && ancestor_doChainPrecedence_pos)) && doPos)
						|| (((Config.MINE_NOTPRECEDENCE && ancestor_doPrecedence_neg) || (Config.MINE_NOTCHAINPRECEDENCE && ancestor_doChainPrecedence_neg)) && doNeg)) {
					//other 2 backwards
					//TODO: support alternate precedence
					IndexResult indexCond = lastIndexOf(conditionExpression, acts, -1, -1);
					if(indexCond != null) {
						boolean restIsViolating_prec = false;
						boolean hasConformed_prec = false;
						boolean restIsViolating_chainPrec = false;
						boolean hasConformed_chainPrec = false;
						boolean hasViolated_chainPrec = false;
						while(true) {
							IndexResult indexConseq = lastIndexOf(consequenceExpression, acts
									.subList(0, indexCond.getIndex_start()), -1,
									acts.get(indexCond.getIndex_start()).getStart());
							//precedence
							if((Config.MINE_PRECEDENCE && doPos && ancestor_doPrecedence_pos)
									|| (Config.MINE_NOTPRECEDENCE && doNeg && ancestor_doPrecedence_neg)) {
								if(restIsViolating_prec
										|| indexConseq == null) {
									restIsViolating_prec = true;
									countNrOfViolations_prec++;
								} else {
									hasConformed_prec = true;
									long countDiff = acts.get(indexCond.getIndex_start()).getStart()
											- acts.get(indexConseq.getIndex_end()).getEnd();
									if(countDiff < 0)
										throw new IllegalArgumentException("countDiff < 0???");
									else
										times_prec.add(countDiff);
								}
							}
							//chain precedence
							if((Config.MINE_CHAINPRECEDENCE && doPos && ancestor_doChainPrecedence_pos)
									|| (Config.MINE_NOTCHAINPRECEDENCE && doNeg && ancestor_doChainPrecedence_neg)) {
								if(restIsViolating_chainPrec
										|| indexConseq == null
										|| indexConseq.getIndex_start() == -1
										|| indexConseq.getIndex_end() == -1) {
									restIsViolating_chainPrec = true;
									hasViolated_chainPrec = true;
									countNrOfViolations_chainPrec++;
								} else {
									long firstTimingBefore = -1;
									for(int j = 0; j < indexCond.getIndex_start(); j++) {
										if(acts.get(j).getEnd()
												<= acts.get(indexCond.getIndex_start()).getStart())
											firstTimingBefore = acts.get(j).getEnd();
									}
									if(firstTimingBefore == -1) {
										restIsViolating_chainPrec = true;
										hasViolated_chainPrec = true;
										countNrOfViolations_chainPrec++;
									} else {
										if(acts.get(indexConseq.getIndex_end()).getEnd() == firstTimingBefore) {
											long countDiff = acts.get(indexCond.getIndex_start()).getStart()
													- acts.get(indexConseq.getIndex_end()).getEnd();
											if(countDiff < 0)
												throw new IllegalArgumentException("countDiff < 0???");
											else {
												hasConformed_chainPrec = true;
												times_chainPrec.add(countDiff);
											}
										} else {
											hasViolated_chainPrec= true;
											countNrOfViolations_chainPrec++;
										}
									}
								}
							}

							if(indexCond.getIndex_end() == 0)
								break;
							indexCond = lastIndexOf(conditionExpression, acts, indexCond.getIndex_end()-1, -1);
							if(indexCond == null)
								break;
						}
						//precedence
						if((Config.MINE_PRECEDENCE && doPos && ancestor_doPrecedence_pos)
								|| (Config.MINE_NOTPRECEDENCE && doNeg && ancestor_doPrecedence_neg)) {
							if(!restIsViolating_prec && hasConformed_prec)
								countNrOfConfirmations_Traces_prec++;
							else if(restIsViolating_prec && !hasConformed_prec)
								countNrOfViolations_Traces_prec++;
							else
								countNrOfOtherViolations_Traces_prec++;
						}
						//chain precedence
						if((Config.MINE_CHAINPRECEDENCE && doPos && ancestor_doChainPrecedence_pos)
								|| (Config.MINE_NOTCHAINPRECEDENCE && doNeg && ancestor_doChainPrecedence_neg)) {
							if(!hasViolated_chainPrec && hasConformed_chainPrec)
								countNrOfConfirmations_Traces_chainPrec++;
							else if(hasViolated_chainPrec && !hasConformed_chainPrec)
								countNrOfViolations_Traces_chainPrec++;
							else
								countNrOfOtherViolations_Traces_chainPrec++;
						}
					}
				}
			}
		}
		//response timings
		double avgTime_resp = 0;
		double stdev_resp = 0;
		long minTime_resp = -1;
		long maxTime_resp = -1;
		if(!times_resp.isEmpty()) {
			for(long t : times_resp) {
				avgTime_resp += t;
				if(minTime_resp == -1 || minTime_resp > t)
					minTime_resp = t;
				if(maxTime_resp == -1 || maxTime_resp < t)
					maxTime_resp = t;
			}
			avgTime_resp = avgTime_resp/times_resp.size();
			for(long t : times_resp)
				stdev_resp += Math.pow(t-avgTime_resp, 2);
			stdev_resp = Math.sqrt(stdev_resp/times_resp.size());
		}
		//chain response timings
		double avgTime_chainResp = 0;
		double stdev_chainResp = 0;
		long minTime_chainResp = -1;
		long maxTime_chainResp = -1;
		if(!times_chainResp.isEmpty()) {
			for(long t : times_chainResp) {
				avgTime_chainResp += t;
				if(minTime_chainResp == -1 || minTime_chainResp > t)
					minTime_chainResp = t;
				if(maxTime_chainResp == -1 || maxTime_chainResp < t)
					maxTime_chainResp = t;
			}
			avgTime_chainResp = avgTime_chainResp/times_chainResp.size();
			for(long t : times_chainResp)
				stdev_chainResp += Math.pow(t-avgTime_chainResp, 2);
			stdev_chainResp = Math.sqrt(stdev_chainResp/times_chainResp.size());
		}
		//precedence timings
		double avgTime_prec = 0;
		double stdev_prec = 0;
		long minTime_prec = -1;
		long maxTime_prec = -1;
		if(!times_prec.isEmpty()) {
			for(long t : times_prec) {
				avgTime_prec += t;
				if(minTime_prec == -1 || minTime_prec > t)
					minTime_prec = t;
				if(maxTime_prec == -1 || maxTime_prec < t)
					maxTime_prec = t;
			}
			avgTime_prec = avgTime_prec/times_prec.size();
			for(long t : times_prec)
				stdev_prec += Math.pow(t-avgTime_prec, 2);
			stdev_prec = Math.sqrt(stdev_prec/times_prec.size());
		}
		//chain response timings
		double avgTime_chainPrec = 0;
		double stdev_chainPrec = 0;
		long minTime_chainPrec = -1;
		long maxTime_chainPrec = -1;
		if(!times_chainPrec.isEmpty()) {
			for(long t : times_chainPrec) {
				avgTime_chainPrec += t;
				if(minTime_chainPrec == -1 || minTime_chainPrec > t)
					minTime_chainPrec = t;
				if(maxTime_chainPrec == -1 || maxTime_chainPrec < t)
					maxTime_chainPrec = t;
			}
			avgTime_chainPrec = avgTime_chainPrec/times_chainPrec.size();
			for(long t : times_chainPrec)
				stdev_chainPrec += Math.pow(t-avgTime_chainPrec, 2);
			stdev_chainPrec = Math.sqrt(stdev_chainPrec/times_chainPrec.size());
		}
		//remove redundant decision rules, as these are not used anyway...
		if(countNrOfConfirmations_pres+countNrOfViolations_pres
				+times_resp.size()+countNrOfViolations_resp
				+times_chainResp.size()+countNrOfViolations_chainResp
				+times_prec.size()+countNrOfViolations_prec
				+times_chainPrec.size()+countNrOfViolations_chainPrec > 0
				&& activationDecision != null
				&& usedDecisions.size() != activationDecision.getRules().size())
			activationDecision.setRules(usedDecisions);
		ArrayList<Rule> result = new ArrayList<>();
		ExistenceExpression conditionEx_L1 = new AtomicExistenceExpression(
				new AtLeast(null, null, conditionExpression, 1, 0, -1, false));
		ExistenceExpression consequenceEx_L1 = new AtomicExistenceExpression(
				new AtLeast(null, null, consequenceExpression, 1, 0, -1, false));
		ExistenceExpression consequenceEx_M0 = new AtomicExistenceExpression(
				new AtMost(null, null, consequenceExpression, 0, 0, -1, false));
		if(Config.MINE_RESPONDEDPRESENCE && doPos && ancestor_doRespondedPres_pos
				&& countNrOfConfirmations_pres > 0)
			result.add(new SingleRule(new RespondedPresence(activationDecision, null, conditionEx_L1,
					consequenceEx_L1, false),
					countNrOfConfirmations_pres, countNrOfViolations_pres,
					countNrOfConfirmations_pres, countNrOfViolations_pres,
					log.size()));
		if(Config.MINE_NOTRESPONDEDPRESENCE && doNeg && ancestor_doRespondedPres_neg
				&& countNrOfViolations_pres > 0)
			result.add(new SingleRule(new RespondedPresence(activationDecision, null, conditionEx_L1,
					consequenceEx_M0, false),
					countNrOfViolations_pres, countNrOfConfirmations_pres,
					countNrOfViolations_pres, countNrOfConfirmations_pres,
					log.size()));
		if(Config.MINE_RESPONSE && doPos && ancestor_doResponse_pos
				&& times_resp.size() > 0)
			result.add(new SingleRuleWithTimings(new Response(activationDecision, null, conditionEx_L1,
					consequenceEx_L1, 0, -1, false),
					times_resp.size(), countNrOfViolations_resp,
					countNrOfConfirmations_Traces_resp,
					countNrOfViolations_Traces_resp + countNrOfOtherViolations_Traces_resp,
					log.size(), avgTime_resp, stdev_resp, minTime_resp, maxTime_resp));
		if(Config.MINE_NOTRESPONSE && doNeg && ancestor_doResponse_neg
				&& countNrOfViolations_resp > 0)
			result.add(new SingleRule(new Response(activationDecision, null, conditionEx_L1,
					consequenceEx_M0, 0, -1, false),
					countNrOfViolations_resp, times_resp.size(),
					countNrOfViolations_Traces_resp,
					countNrOfConfirmations_Traces_resp + countNrOfOtherViolations_Traces_resp,
					log.size()));
		if(Config.MINE_CHAINRESPONSE && doPos && ancestor_doChainResponse_pos
				&& times_chainResp.size() > 0)
			result.add(new SingleRuleWithTimings(new ChainResponse(activationDecision, null, conditionEx_L1,
					consequenceEx_L1, 0, -1, false),
					times_chainResp.size(), countNrOfViolations_chainResp,
					countNrOfConfirmations_Traces_chainResp,
					countNrOfViolations_Traces_chainResp + countNrOfOtherViolations_Traces_chainResp,
					log.size(), avgTime_chainResp, stdev_chainResp, minTime_chainResp, maxTime_chainResp));
		if(Config.MINE_NOTCHAINRESPONSE && doNeg && ancestor_doChainResponse_neg
				&& countNrOfViolations_chainResp > 0)
			result.add(new SingleRule(new ChainResponse(activationDecision, null, conditionEx_L1,
					consequenceEx_M0, 0, -1, false),
					countNrOfViolations_chainResp, times_chainResp.size(),
					countNrOfViolations_Traces_chainResp,
					countNrOfConfirmations_Traces_chainResp + countNrOfOtherViolations_Traces_chainResp,
					log.size()));
		if(Config.MINE_PRECEDENCE && doPos && ancestor_doPrecedence_pos
				&& times_prec.size() > 0)
			result.add(new SingleRuleWithTimings(new Precedence(activationDecision, null,
					consequenceEx_L1, conditionEx_L1, minTime_prec, maxTime_prec, false),
					times_prec.size(), countNrOfViolations_prec,
					countNrOfConfirmations_Traces_prec,
					countNrOfViolations_Traces_prec + countNrOfOtherViolations_Traces_prec,
					log.size(), avgTime_prec, stdev_prec, minTime_prec, maxTime_prec));
		if(Config.MINE_NOTPRECEDENCE && doNeg && ancestor_doPrecedence_neg
				&& countNrOfViolations_prec > 0)
			result.add(new SingleRule(new Precedence(activationDecision, null,
					consequenceEx_M0, conditionEx_L1, 0, -1, false),
					countNrOfViolations_prec, times_prec.size(),
					countNrOfViolations_Traces_prec,
					countNrOfConfirmations_Traces_prec + countNrOfOtherViolations_Traces_prec,
					log.size()));
		if(Config.MINE_CHAINPRECEDENCE && doPos && ancestor_doChainPrecedence_pos
				&& times_chainPrec.size() > 0)
			result.add(new SingleRuleWithTimings(new ChainPrecedence(activationDecision, null,
					consequenceEx_L1, conditionEx_L1, minTime_chainPrec, maxTime_chainPrec, false),
					times_chainPrec.size(), countNrOfViolations_chainPrec,
					countNrOfConfirmations_Traces_chainPrec,
					countNrOfViolations_Traces_chainPrec + countNrOfOtherViolations_Traces_chainPrec,
					log.size(), avgTime_chainPrec, stdev_chainPrec, minTime_chainPrec, maxTime_chainPrec));
		if(Config.MINE_NOTCHAINPRECEDENCE && doNeg && ancestor_doChainPrecedence_neg
				&& countNrOfViolations_chainPrec > 0)
			result.add(new SingleRule(new ChainPrecedence(activationDecision, null,
					consequenceEx_M0, conditionEx_L1, 0, -1, false),
					countNrOfViolations_chainPrec, times_chainPrec.size(),
					countNrOfViolations_Traces_chainPrec,
					countNrOfConfirmations_Traces_chainPrec + countNrOfOtherViolations_Traces_chainPrec,
					log.size()));
		if(result.isEmpty())
			return null;
		else
			return new BatchRule(new SequenceRelationConstraint(activationDecision, null,
					conditionExpression, consequenceExpression, false, mrp), result.toArray(new Rule[result.size()]), ancestor);
	}

	public SequenceRelationConstraint getShallowCopy(ActivityExpression conditionExpression,
			ActivityExpression consequenceExpression) {
		return new SequenceRelationConstraint(null, null, conditionExpression, consequenceExpression,
				isOptional(), getMineRelationProfile());
	}

	@Override
	public Constraint getDecisionlessCopy() {
		return new SequenceRelationConstraint(null, null, getConditionExpression(), getConsequenceExpression(),
				isOptional(), getMineRelationProfile());
	}
}