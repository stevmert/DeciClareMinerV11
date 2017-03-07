package miner.log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class Log extends ArrayList<Trace> implements Serializable {

	private static final long serialVersionUID = 1292554470852560470L;

	private String name;

	public Log(String name, Collection<? extends Trace> c) {
		super(c);
		this.name = name;
	}

	public Log(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Log other = (Log) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}