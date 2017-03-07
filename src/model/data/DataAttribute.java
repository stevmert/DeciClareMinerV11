package model.data;

import java.io.Serializable;

import util.xml.AtomicNode;

public abstract class DataAttribute implements DataStructure, Serializable {

	private static final long serialVersionUID = -874307583255724006L;

	private String name;
	private DataRecord parent;

	public DataAttribute(String name, DataRecord parent) {
		super();
		if(name == null)
			throw new IllegalArgumentException();
		this.name = name;
		this.parent = parent;
		if(parent != null) {
			boolean found = false;
			boolean foundDifferentWithSameName = false;
			for(DataAttribute a : parent.getAttributes())
				if(a.getName().equals(this.name)) {
					foundDifferentWithSameName = foundDifferentWithSameName || !a.getClass().equals(this.getClass());
					found = found || a.getClass().equals(this.getClass());
				}
			if(foundDifferentWithSameName)
				throw new IllegalArgumentException();
			if(!found)
				parent.getAttributes().add(this);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public DataRecord getParent() {
		return parent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		DataAttribute other = (DataAttribute) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		if(getParent() != null)
			return getParent() + "->" + getName();
		return getName();
	}

	public abstract AtomicNode getXesNode();
}