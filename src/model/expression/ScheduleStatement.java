package model.expression;

import java.io.Serializable;

//TODO: is nu sterk vereenvoudig!!!
//form ‘(mandatory) every [periodic point in time], (optional) between [two smaller periodic points in time]’
//(e.g., every Monday, between 8 am and 5 pm)
public class ScheduleStatement implements Serializable {

	private static final long serialVersionUID = -1991600583899551507L;

	private String periodicTimePoint;
	private String betweenStatement;

	public ScheduleStatement(String periodicTimePoint, String betweenStatement) {
		super();
		if(periodicTimePoint == null)
			throw new IllegalArgumentException();
		this.periodicTimePoint = periodicTimePoint;
		this.betweenStatement = betweenStatement;
	}

	public String getPeriodicTimePoint() {
		return periodicTimePoint;
	}

	public void setPeriodicTimePoint(String periodicTimePoint) {
		this.periodicTimePoint = periodicTimePoint;
	}

	public String getBetweenStatement() {
		return betweenStatement;
	}

	public void setBetweenStatement(String betweenStatement) {
		this.betweenStatement = betweenStatement;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((betweenStatement == null) ? 0 : betweenStatement.hashCode());
		result = prime * result + ((periodicTimePoint == null) ? 0 : periodicTimePoint.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScheduleStatement other = (ScheduleStatement) obj;
		if (betweenStatement == null) {
			if (other.betweenStatement != null)
				return false;
		} else if (!betweenStatement.equals(other.betweenStatement))
			return false;
		if (periodicTimePoint == null) {
			if (other.periodicTimePoint != null)
				return false;
		} else if (!periodicTimePoint.equals(other.periodicTimePoint))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String tmp = "";
		if(getBetweenStatement() != null)
			tmp = ", between " + getBetweenStatement();
		return "Every " + getPeriodicTimePoint() + tmp;
	}
}