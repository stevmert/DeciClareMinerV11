package model.constraint.existence.extra;

import miner.log.Log;
import miner.rule.Rule;
import model.Constraint;
import model.constraint.ExistenceConstraint;
import model.data.Decision;
import model.expression.ActivityExpression;

//= starts as last (does not need to end at same time or after all others!)
public class Last extends ExistenceConstraint {

	private static final long serialVersionUID = 1968190760603471733L;

	public Last(Decision activationDec, Decision deactivationDec,
			ActivityExpression activityExpression, boolean isOptional) {
		super(activationDec, deactivationDec, activityExpression, isOptional);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + getActivityExpression() + ")" + super.toString();
	}

	@Override
	protected String getTextualConstraint() {
		if(getActivityExpression().getNrOfElements() == 1)
			return getActivityExpression() + " has to be performed as the last activity";
		return "one of the following activities has to be performed as the last activity: " + getActivityExpression();
	}

	@Override
	public Constraint getDecisionlessCopy() {
		return new Last(null, null, getActivityExpression(), isOptional());
	}

	@Override
	public Rule evaluate(Log log, Rule seed) {
		return mineLast(this.getActivationDecision(), this.getActivityExpression(), log);
	}

	public static Rule mineLast(Decision activationDecision,
			ActivityExpression activityExpression, Log log) {
		throw new IllegalArgumentException("TODO: still used?");//TODO: still used?
		//		Rule r = ExtremitiesConstraint.mineFirstAndLast(activationDecision, activityExpression, log);
		//		if(r == null)
		//			return null;
		//		List<SingleRule> res = r.rules();
		//		for(SingleRule sr : new ArrayList<>(res))
		//			if(!(sr.getConstraint() instanceof Last))
		//				res.remove(sr);
		//		if(res.isEmpty())
		//			return null;
		//		else if(res.size() == 1)
		//			return res.get(0);
		//		return new BatchRule(new ExtremitiesConstraint(activationDecision, activationDecision,
		//				activityExpression, false), res.toArray(new Rule[res.size()]));
	}
}