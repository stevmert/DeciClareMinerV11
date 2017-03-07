package model.data;

import java.io.Serializable;
import java.util.HashSet;

public class DecisionRule implements Serializable {

	private static final long serialVersionUID = 7585050727146099565L;

	private HashSet<DataAttribute> dataValues;
	
	public DecisionRule(HashSet<DataAttribute> dataValues) {
		super();
		this.dataValues = dataValues;
	}

	public HashSet<DataAttribute> getDataValues() {
		return dataValues;
	}

	public void setDataValues(HashSet<DataAttribute> dataValues) {
		this.dataValues = dataValues;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataValues == null) ? 0 : dataValues.hashCode());
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
		DecisionRule other = (DecisionRule) obj;
		if (dataValues == null) {
			if (other.dataValues != null)
				return false;
		} else if (!dataValues.equals(other.dataValues))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return dataValues.toString();
	}

	public String getTextualVersion() {
		String res = null;
		for(DataAttribute de : getDataValues()) {
			if(res == null)
				res = "'" + de + "'";
			else
				res += " and '" + de + "'";
		}
		return "[" + res + "]";
	}
}