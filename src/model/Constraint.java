package model;

import java.io.Serializable;
import java.util.List;

import miner.Config;
import miner.log.ActivityEvent;
import miner.log.Log;
import miner.rule.Rule;
import model.constraint.TimedConstraint;
import model.constraint.existence.AtLeast;
import model.constraint.existence.AtLeastChoice;
import model.constraint.existence.AtMost;
import model.constraint.existence.AtMostChoice;
import model.data.Decision;
import model.expression.ActivityExpression;
import model.expression.AtomicActivityExpression;
import model.expression.AtomicExistenceExpression;
import model.expression.ExistenceExpression;
import model.expression.LogicalOperator;
import model.expression.NonAtomicActivityExpression;
import model.expression.NonAtomicExistenceExpression;
import util.IndexResult;

public abstract class Constraint implements Serializable {

	private static final long serialVersionUID = -812539465001823321L;

	private Decision activationDecision;
	private Decision deactivationDecision;
	private boolean isOptional;

	public Constraint(Decision activationDecision, Decision deactivationDecision, boolean isOptional) {
		super();
		this.activationDecision = activationDecision;
		this.deactivationDecision = deactivationDecision;
		this.isOptional = isOptional;
	}

	public Decision getActivationDecision() {
		return activationDecision;
	}

	public void setActivationDecision(Decision activationDecision) {
		this.activationDecision = activationDecision;
	}

	public Decision getDeactivationDecision() {
		return deactivationDecision;
	}

	public void setDeactivationDecision(Decision deactivationDecision) {
		this.deactivationDecision = deactivationDecision;
	}

	public boolean isOptional() {
		return isOptional;
	}

	public void setOptional(boolean isOptional) {
		this.isOptional = isOptional;
	}

	//	//	protected ArrayList<ActivityInterval> activityIntervals(Instance instance) {//TODO: needed if data is allowed to change after initial value assigned
	//	protected ActivityInterval activityIntervals(Instance instance) {
	//		int start = -1;
	//		int end = -1;
	//		for(int i = 0; i < instance.getEvents().size(); i++) {
	//			if(start == -1) {
	//				boolean active = false || getActivationDecision().getRules().isEmpty();
	//				for(DecisionRule dr : getActivationDecision().getRules()) {
	//					boolean thisRuleActive = true;
	//					for(Expression e : dr.getTrues())
	//						if(instance.getData().get(e) != true
	//						&& instance.getEvents().get(i).getData().get(e) != true) {
	//							thisRuleActive = false;
	//							break;
	//						}
	//					if(thisRuleActive)
	//						for(Expression e : dr.getFalses())
	//							if(instance.getData().get(e) != false
	//							&& instance.getEvents().get(i).getData().get(e) != false) {
	//								thisRuleActive = false;
	//								break;
	//							}
	//					if(thisRuleActive) {
	//						active = thisRuleActive;
	//						break;
	//					}
	//				}
	//				if(active)
	//					start = i;
	//			}
	//			if(start != -1) {
	//				//TODO
	////				boolean deactive = false || getDeactivationDecision().getRules().isEmpty();
	////				for(DecisionRule dr : getDeactivationDecision().getRules()) {
	////					boolean thisRuleDeactive = true;
	////					for(Expression e : dr.getTrues())
	////						if(instance.getData().get(e) != true
	////						&& instance.getEvents().get(i).getData().get(e) != true) {
	////							thisRuleDeactive = false;
	////							break;
	////						}
	////					if(thisRuleDeactive)
	////						for(Expression e : dr.getFalses())
	////							if(instance.getData().get(e) != false
	////							&& instance.getEvents().get(i).getData().get(e) != false) {
	////								thisRuleDeactive = false;
	////								break;
	////							}
	////					if(thisRuleDeactive) {
	////						deactive = thisRuleDeactive;
	////						break;
	////					}
	////				}
	////				if(deactive) {
	////					end = i;
	////					break;
	////				}
	//			}
	//		}
	//		if(start != -1 && end == -1)
	//			end = instance.getEvents().size() - 1;
	//		return new ActivityInterval(start, end);
	//	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((activationDecision == null) ? 0 : activationDecision.hashCode());
		result = prime * result + ((deactivationDecision == null) ? 0 : deactivationDecision.hashCode());
		result = prime * result + (isOptional ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return equals(obj, true);
	}

	public boolean equals(Object obj, boolean checkDecisions) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		if(checkDecisions) {
			Constraint other = (Constraint) obj;
			if (activationDecision == null) {
				if (other.activationDecision != null)
					return false;
			} else if (!activationDecision.equals(other.activationDecision))
				return false;
			if (deactivationDecision == null) {
				if (other.deactivationDecision != null)
					return false;
			} else if (!deactivationDecision.equals(other.deactivationDecision))
				return false;
			if (isOptional != other.isOptional)
				return false;
		}
		return true;
	}

	public abstract Constraint getDecisionlessCopy();

	public abstract Rule evaluate(Log log, Rule ancestor);

	public static int count(ActivityExpression actExp, List<ActivityEvent> traceActs) {
		if(actExp instanceof AtomicActivityExpression)
			return count(((AtomicActivityExpression) actExp).getActivity(), traceActs);
		NonAtomicActivityExpression x = (NonAtomicActivityExpression) actExp;
		if(x.getOperator().equals(LogicalOperator.AND)) {//and
			int count = -1;
			for(ActivityExpression a : x.getExpressions()) {
				int c = count(a, traceActs);
				if(count == -1 || c < count)
					count = c;
			}
			return count;
		} else {//or
			int count = 0;
			for(ActivityExpression a : x.getExpressions())
				count += count(a, traceActs);
			return count;
		}
	}

	public static int count(Activity act, List<ActivityEvent> traceActs) {
		int count = 0;
		for(Activity a : traceActs)
			if(a.equals(act))
				count++;
		return count;
	}

	public static boolean contains(ActivityExpression actExp,
			List<ActivityEvent> trace) {
		IndexResult index = indexOf(actExp, trace, -1, -1);
		return index != null;
	}

	public static IndexResult indexOf(ActivityExpression actExp,
			List<ActivityEvent> trace) {
		return indexOf(actExp, trace, -1, -1);
	}

	public static IndexResult indexOf(ActivityExpression actExp,
			List<ActivityEvent> trace, int minIndex, long minTime) {
		if(actExp instanceof AtomicActivityExpression) {
			int sub = 0;
			int firstIndex = -1;
			while(sub < trace.size()) {
				int index = trace.subList(sub, trace.size()).indexOf(((AtomicActivityExpression) actExp).getActivity());
				if(index == -1)
					break;
				index = index + sub;
				if((minIndex == -1 || index >= minIndex)
						&& (firstIndex == -1 || firstIndex > index)
						&& (minTime == -1 || trace.get(index).getStart() >= minTime)) {
					firstIndex = index;
					break;
				}
				sub = index+1;
			}
			if(firstIndex == -1)
				return null;
			return new IndexResult(firstIndex, firstIndex);
		}
		IndexResult firstIndex = null;
		NonAtomicActivityExpression x = (NonAtomicActivityExpression) actExp;
		if(x.getOperator().equals(LogicalOperator.AND)) {//and
			for(ActivityExpression a : x.getExpressions()) {
				IndexResult i = indexOf(a, trace, minIndex, minTime);
				if(i == null)
					return null;
				if(firstIndex == null)
					firstIndex = i;
				else {
					firstIndex.setIndex_start(Math.min(firstIndex.getIndex_start(), i.getIndex_start()));
					firstIndex.setIndex_end(Math.max(firstIndex.getIndex_end(), i.getIndex_end()));
				}
			}
			return firstIndex;
		} else {//or
			for(ActivityExpression a : x.getExpressions()) {
				IndexResult i = indexOf(a, trace, minIndex, minTime);
				if(i != null)
					if(firstIndex == null
					|| i.getIndex_end() < firstIndex.getIndex_end()
					|| (i.getIndex_end() == firstIndex.getIndex_end()
					&& i.getIndex_start() < firstIndex.getIndex_start()))
						firstIndex = i;
			}
			return firstIndex;
		}
	}

	public static IndexResult lastIndexOf(ActivityExpression actExp,
			List<ActivityEvent> trace, int maxIndex, long maxTime) {
		if(actExp instanceof AtomicActivityExpression) {
			int sub = trace.size();
			int lastIndex = -1;
			while(sub > 0) {
				int index = trace.subList(0, sub).lastIndexOf(((AtomicActivityExpression) actExp).getActivity());
				if(index == -1)
					break;
				if((maxIndex == -1 || index <= maxIndex)
						&& (lastIndex == -1 || lastIndex > index)
						&& (maxTime == -1 || trace.get(index).getStart() <= maxTime)) {
					lastIndex = index;
					break;
				}
				sub = index;
			}
			if(lastIndex == -1)
				return null;
			return new IndexResult(lastIndex, lastIndex);
		}
		IndexResult lastIndex = null;
		NonAtomicActivityExpression x = (NonAtomicActivityExpression) actExp;
		if(x.getOperator().equals(LogicalOperator.AND)) {//and
			for(ActivityExpression a : x.getExpressions()) {
				IndexResult i = lastIndexOf(a, trace, maxIndex, maxTime);
				if(i == null)
					return null;
				if(lastIndex == null)
					lastIndex = i;
				else {
					lastIndex.setIndex_start(Math.min(lastIndex.getIndex_start(), i.getIndex_start()));
					lastIndex.setIndex_end(Math.max(lastIndex.getIndex_end(), i.getIndex_end()));
				}
			}
			return lastIndex;
		} else {//or
			for(ActivityExpression a : x.getExpressions()) {
				IndexResult i = lastIndexOf(a, trace, maxIndex, maxTime);
				if(i != null)
					if(lastIndex == null
					|| i.getIndex_start() > lastIndex.getIndex_start()
					|| (i.getIndex_start() == lastIndex.getIndex_start()
					&& i.getIndex_end() > lastIndex.getIndex_end()))
						lastIndex = i;
			}
			return lastIndex;
		}
	}

	public static boolean contains(ExistenceExpression exExp,
			List<ActivityEvent> trace) {
		return indexOf(exExp, trace, -1, -1) != null;
	}

	public static IndexResult indexOf(ExistenceExpression exExp,
			List<ActivityEvent> trace) {
		return indexOf(exExp, trace, -1, -1);
	}

	public static IndexResult indexOf(ExistenceExpression exExp,
			List<ActivityEvent> trace, int minIndex, long minTime) {
		if(exExp instanceof AtomicExistenceExpression) {
			if(((AtomicExistenceExpression) exExp).getExistenceConstraint() instanceof AtLeast) {
				AtLeast atL = (AtLeast) ((AtomicExistenceExpression) exExp).getExistenceConstraint();
				IndexResult index = null;
				int minIndex_tmp = minIndex;
				for(int i = 0; i < atL.getBound(); i++) {
					if(i == 0) {
						index = indexOf(atL.getActivityExpression(), trace, minIndex_tmp, minTime);
						if(index == null)
							return null;
						minIndex_tmp = index.getIndex_start()+1;
					} else {
						IndexResult index2 = indexOf(atL.getActivityExpression(), trace, minIndex_tmp, minTime);
						if(index2 == null)
							return null;
						minIndex_tmp = index2.getIndex_start()+1;
						index.setIndex_start(Math.min(index.getIndex_start(), index2.getIndex_start()));
						index.setIndex_end(Math.max(index.getIndex_end(), index2.getIndex_end()));
					}
				}
				return index;
			} else if(((AtomicExistenceExpression) exExp).getExistenceConstraint() instanceof AtMost) {
				throw new IllegalArgumentException("TODO");//TODO: other existence expressions
			} else if(((AtomicExistenceExpression) exExp).getExistenceConstraint() instanceof AtLeastChoice) {
				throw new IllegalArgumentException("TODO");
			} else if(((AtomicExistenceExpression) exExp).getExistenceConstraint() instanceof AtMostChoice) {
				throw new IllegalArgumentException("TODO");
			}
		}
		NonAtomicExistenceExpression x = (NonAtomicExistenceExpression) exExp;
		IndexResult firstIndex = null;
		if(x.getOperator().equals(LogicalOperator.AND)) {//and
			for(ExistenceExpression a : x.getExpressions()) {
				IndexResult i = indexOf(a, trace, minIndex, minTime);
				if(i == null)
					return null;
				if(firstIndex == null)
					firstIndex = i;
				else {
					firstIndex.setIndex_start(Math.min(firstIndex.getIndex_start(), i.getIndex_start()));
					firstIndex.setIndex_end(Math.max(firstIndex.getIndex_end(), i.getIndex_end()));
				}
			}
		} else {//or
			for(ExistenceExpression a : x.getExpressions()) {
				IndexResult i = indexOf(a, trace, minIndex, minTime);
				if(i != null)
					if(firstIndex == null
					|| i.getIndex_end() < firstIndex.getIndex_end()
					|| (i.getIndex_end() == firstIndex.getIndex_end())
					&& (i.getIndex_start() < firstIndex.getIndex_start()))
						firstIndex = i;
			}
		}
		return firstIndex;
	}

	@Override
	public String toString() {
		String act = "";
		if(getActivationDecision() != null
				&& !getActivationDecision().getRules().isEmpty())
			act = " activateIf" + getActivationDecision();
		else if(Config.ALWAYS_USE_SHOW_FULL_CONSTRAINT)
			act = " activateIf[True]";
		String deact = "";
		if(getDeactivationDecision() != null
				&& !getDeactivationDecision().getRules().isEmpty())
			deact = " deactivateIf" + getDeactivationDecision();
		else if(Config.ALWAYS_USE_SHOW_FULL_CONSTRAINT)
			deact = " deactivateIf[False]";
		return act + deact;
	}

	public String getTextRepresentation() {
		String optional = "";
		if(isOptional())
			optional += "OPTIONAL ";
		String deact = "";
		if(getDeactivationDecision() != null)
			deact = " unless " + getDeactivationDecision().getTextualVersion();
		if(getActivationDecision() == null
				|| getActivationDecision().getRules().isEmpty())
			return optional + getTextualConstraint() + deact;
		return optional + getActivationDecision().getTextualVersion()
				+ " then " + getTextualConstraint() + deact;
	}

	protected abstract String getTextualConstraint();

	protected static String getTimedText(TimedConstraint c) {
		String res = "";
		if(c.getTimeA() != 0)
			res += " after at least " + getTime(c.getTimeA());
		if(c.getTimeB() != -1) {
			if(res.length() != 0)
				res += " and";
			res += " within at most " + getTime(c.getTimeB());
		}
		return res;
	}

	public static String getTime(long inputSeconds) {
		if(inputSeconds < 60)
			return inputSeconds + " second" + (inputSeconds>1?"s":"");
		long minutes = inputSeconds / 60;
		long seconds = inputSeconds % 60;
		if(minutes < 60) {
			String res = minutes + " minute" + (minutes>1?"s":"");
			if(seconds != 0)
				res += " " + seconds + " second" + (seconds>1?"s":"");
			return res;
		}
		long hours = minutes / 60;
		minutes = minutes % 60;
		if(hours < 24) {
			String res = hours + " hour" + (hours>1?"s":"");
			if(minutes != 0)
				res += " " + minutes + " minute" + (minutes>1?"s":"");
			if(seconds != 0)
				res += " " + seconds + " second" + (seconds>1?"s":"");
			return res;
		}
		long days = hours / 24;
		hours = hours % 24;
		String res = days + " day" + (days>1?"s":"");
		if(hours != 0)
			res += " " + hours + " hour" + (hours>1?"s":"");
		if(minutes != 0)
			res += " " + minutes + " minute" + (minutes>1?"s":"");
		if(seconds != 0)
			res += " " + seconds + " second" + (seconds>1?"s":"");
		return res;
	}

	public boolean isRelatedTo(Constraint c) {
		if(c == null
				|| !this.getClass().equals(c.getClass()))
			return false;
		return true;
	}
}