package model.constraint.existence;

import java.util.ArrayList;

import miner.log.Log;
import miner.rule.Rule;
import model.Constraint;
import model.constraint.ExistenceConstraint;
import model.constraint.Negatable;
import model.data.Decision;
import model.expression.ActivityExpression;
import model.expression.ScheduleStatement;

public class ActivityAvailabilitySchedule extends ExistenceConstraint implements Negatable {

	private static final long serialVersionUID = 1679348278414714293L;

	private ArrayList<ScheduleStatement> schedule;
	private boolean isPositiveVersion;

	public ActivityAvailabilitySchedule(Decision activationDec, Decision deactivationDec,
			ActivityExpression activityExpression, ArrayList<ScheduleStatement> schedule,
			boolean isPositiveVersion, boolean isOptional) {
		super(activationDec, deactivationDec, activityExpression, isOptional);
		if(schedule == null || schedule.isEmpty())
			throw new IllegalArgumentException();
		this.schedule = schedule;
		this.isPositiveVersion = isPositiveVersion;
	}

	public ArrayList<ScheduleStatement> getSchedule() {
		return schedule;
	}

	public void setSchedule(ArrayList<ScheduleStatement> schedule) {
		this.schedule = schedule;
	}

	public boolean isPositiveVersion() {
		return isPositiveVersion;
	}

	@Override
	public void setIsPositiveVersion(boolean isPositiveVersion) {
		this.isPositiveVersion = isPositiveVersion;
	}

	@Override
	public Constraint getDecisionlessCopy() {
		return new ActivityAvailabilitySchedule(null, null, getActivityExpression(), getSchedule(),
				isPositiveVersion(), isOptional());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (isPositiveVersion ? 1231 : 1237);
		result = prime * result + ((schedule == null) ? 0 : schedule.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return equals(obj, true);
	}

	@Override
	public boolean equals(Object obj, boolean checkDecisions) {
		if (this == obj)
			return true;
		if (!super.equals(obj, checkDecisions))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActivityAvailabilitySchedule other = (ActivityAvailabilitySchedule) obj;
		if (isPositiveVersion != other.isPositiveVersion)
			return false;
		if (schedule == null) {
			if (other.schedule != null)
				return false;
		} else if (!schedule.equals(other.schedule))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String sched = "";
		for(ScheduleStatement s : getSchedule())
			sched += "/[" + s + "]";
		sched = sched.substring(1);
		String pre = "ActivityAvailabilitySchedule";
		if(!isPositiveVersion())
			pre = "ActivityUnavailabilitySchedule";
		return pre + "(" + getActivityExpression() + ", " + sched
				+ ")" + super.toString();
	}

	@Override
	protected String getTextualConstraint() {
		String sched = "";
		for(ScheduleStatement s : getSchedule())
			sched += "/[" + s + "]";
		sched = sched.substring(1);
		String verb;
		if(isPositiveVersion())
			verb = "only available";
		else
			verb = "unavailable";
		return getActivityExpression() + " is " + verb + " " + sched;
	}

	@Override
	public Rule evaluate(Log log, Rule seed) {
		// TODO Auto-generated method stub
		return null;
	}
}