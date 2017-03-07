package model.constraint.existence;

import miner.log.Log;
import miner.rule.Rule;
import model.Constraint;
import model.constraint.BoundedConstraint;
import model.constraint.ExistenceConstraint;
import model.constraint.TimedConstraint;
import model.data.Decision;
import model.expression.ActivityExpression;
import model.expression.AtomicActivityExpression;
import model.expression.LogicalOperator;
import model.expression.NonAtomicActivityExpression;

public class AtMostChoice extends ExistenceConstraint implements TimedConstraint, BoundedConstraint {

	private static final long serialVersionUID = 2364983328963407385L;

	private int atMost;
	private long timeA;
	private long timeB;

	public AtMostChoice(Decision activationDec, Decision deactivationDec,
			ActivityExpression activityExpression, int atMost,
			long timeA, long timeB, boolean isOptional) {
		super(activationDec, deactivationDec, activityExpression, isOptional);
		if(activityExpression instanceof AtomicActivityExpression
				|| ((NonAtomicActivityExpression) activityExpression).getOperator().equals(LogicalOperator.AND)
				|| atMost < 0
				|| ((NonAtomicActivityExpression) activityExpression).getExpressions().size() <= atMost)
			//TODO: + no OR inside child expressions
			throw new IllegalArgumentException();
		this.atMost = atMost;
		this.timeA = timeA;
		this.timeB = timeB;
	}

	@Override
	public int getBound() {
		return atMost;
	}

	@Override
	public void setBound(int atMost) {
		this.atMost = atMost;
	}

	public long getTimeA() {
		return timeA;
	}

	public void setTimeA(long timeA) {
		this.timeA = timeA;
	}

	public long getTimeB() {
		return timeB;
	}

	public void setTimeB(long timeB) {
		this.timeB = timeB;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + getActivityExpression() + ", " + getBound()
		+ ", " + getTimeA()
		+ ", " + getTimeB() + ")" + super.toString();
	}

	@Override
	protected String getTextualConstraint() {
		return "at most " + getBound() + " out of " + getActivityExpression().getNrOfElements()
				+ " of the following activities can to be performed: " + getActivityExpression() + getTimedText(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + atMost;
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
		AtMostChoice other = (AtMostChoice) obj;
		if (atMost != other.atMost)
			return false;
		return true;
	}

	@Override
	public Constraint getDecisionlessCopy() {
		return new AtMostChoice(null, null, getActivityExpression(),
				getBound(), getTimeA(), getTimeB(), isOptional());
	}

	@Override
	public Rule evaluate(Log log, Rule seed) {
		return mineAtMostChoice(this.getActivationDecision(), this.getActivityExpression(), log);
	}

	public static Rule mineAtMostChoice(Decision activationDecision,
			ActivityExpression activityExpression, Log log) {
		//TODO
		return null;
		//		ArrayList<Rule> res = mineAtLeastAndAtMostChoice(activationDecision, activityExpression, false, log);
		//		return res.toArray(new Rule[res.size()]);
	}

	//	public static ArrayList<Rule> mineAtLeastAndAtMostChoice(Decision activationDecision,
	//			ActivityExpression activityExpression, boolean allowMaxMost, Log log) {
	//		ArrayList<Rule> result = new ArrayList<>();
	//		HashMap<Integer, HashSet<Integer>> counts = new HashMap<>();
	//		//		long minTime = -1;
	//		//		long maxTime = -1;
	//		HashSet<DecisionRule> usedDecisions = new HashSet<>();
	//		for(int i = 0; i < log.size(); i++) {
	//			Trace t = log.get(i);
	//			long activationTime = 0;
	//			if(activationDecision != null) {
	//				DecisionActivation decisionActivation = t.getDecisionActivation(activationDecision);
	//				if(decisionActivation != null) {
	//					activationTime = decisionActivation.getTime();
	//					usedDecisions.add(decisionActivation.getDecisionRule());
	//				} else
	//					activationTime = -1;
	//			}
	//			if(activationTime != -1) {
	//				List<ActivityEvent> actsRem = t.getRemainingActivityList(activationTime);
	//				int nrInTrace = 0;
	//				for(ActivityExpression ae : ((NonAtomicActivityExpression) activityExpression).getExpressions())
	//					if(contains(ae, actsRem))
	//						nrInTrace++;
	//				//				if(nrInTrace > 0) {//TODO: time
	//				//					HashSet<HashSet<Activity>> setOfSets = new HashSet<>();
	//				//					for(Activity a : activities) {
	//				//						HashSet<Activity> s = new HashSet<>();
	//				//						s.add(a);
	//				//						setOfSets.add(s);
	//				//					}
	//				//					int index = indexOf(setOfSets, actsRem, -1, -1);
	//				//					if(minTime == -1 || actsRem.get(index).getStart() < minTime)
	//				//						minTime = actsRem.get(index).getStart();
	//				//					int lastIndex = lastIndexOf(setOfSets, actsRem, -1, -1);
	//				//					if(maxTime == -1 || actsRem.get(lastIndex).getStart() > maxTime)
	//				//						maxTime = actsRem.get(lastIndex).getStart();
	//				//				}
	//				if(counts.get(nrInTrace) == null)
	//					counts.put(nrInTrace, new HashSet<Integer>());
	//				counts.get(nrInTrace).add(i);
	//			}
	//		}
	//		//remove redundant decision rules, as these are not used anyway...
	//		if(!counts.isEmpty()
	//				&& activationDecision != null
	//				&& usedDecisions.size() != activationDecision.getRules().size())
	//			activationDecision.setRules(usedDecisions);
	//		if(counts.isEmpty()) {
	//			result.add(new SingleRule(new AtMostChoice(activationDecision, null, activityExpression, 0, 0, -1, false),
	//					0, 0, 0, 0, log.size()));
	//			result.add(new SingleRule(new AtLeastChoice(activationDecision, null, activityExpression, 0, 0, -1, false),
	//					0, 0, 0, 0, log.size()));
	//		}
	//		for(int i : counts.keySet())
	//			if(allowMaxMost || i < ((NonAtomicActivityExpression) activityExpression).getExpressions().size()) {
	//				HashSet<Integer> totalSupport_AtLeastChoice_Traces = new HashSet<>();
	//				HashSet<Integer> totalSupport_AtMostChoice_Traces = new HashSet<>();
	//				int totalSupport_AtLeastChoice_nrOfViolations = 0;
	//				int totalSupport_AtMostChoice_nrOfViolations = 0;
	//				int totalSupport_AtLeastChoice_nrOfViolations_Traces = 0;
	//				int totalSupport_AtMostChoice_nrOfViolations_Traces = 0;
	//				for(int k : counts.keySet()) {
	//					if(k >= i)
	//						totalSupport_AtLeastChoice_Traces.addAll(counts.get(k));
	//					if(k <= i)
	//						totalSupport_AtMostChoice_Traces.addAll(counts.get(k));
	//					if(k < i) {
	//						totalSupport_AtLeastChoice_nrOfViolations += (i - k) * counts.get(k).size();
	//						totalSupport_AtLeastChoice_nrOfViolations_Traces += counts.get(k).size();
	//					}
	//					if(k > i) {
	//						totalSupport_AtMostChoice_nrOfViolations += (k - i) * counts.get(k).size();
	//						totalSupport_AtMostChoice_nrOfViolations_Traces += counts.get(k).size();
	//					}
	//				}
	//				if(Config.MINE_ATLEASTCHOICE
	//						&& i > 0 && i < ((NonAtomicActivityExpression) activityExpression).getExpressions().size())
	//					result.add(new SingleRule(new AtLeastChoice(activationDecision, null, activityExpression, i, 0, -1, false),
	//							totalSupport_AtLeastChoice_Traces.size(), totalSupport_AtLeastChoice_nrOfViolations,
	//							totalSupport_AtLeastChoice_Traces.size(), totalSupport_AtLeastChoice_nrOfViolations_Traces,
	//							log.size()));
	//				if(Config.MINE_ATMOSTCHOICE)
	//					result.add(new SingleRule(new AtMostChoice(activationDecision, null, activityExpression, i, 0, -1, false),
	//							totalSupport_AtMostChoice_Traces.size(), totalSupport_AtMostChoice_nrOfViolations,
	//							totalSupport_AtMostChoice_Traces.size(), totalSupport_AtMostChoice_nrOfViolations_Traces,
	//							log.size()));
	//			}
	//		return result;
	//	}

	@Override
	public boolean isRelatedTo(Constraint c) {
		if(!super.isRelatedTo(c))
			return false;
		return this.getBound() == ((BoundedConstraint) c).getBound();
	}
}