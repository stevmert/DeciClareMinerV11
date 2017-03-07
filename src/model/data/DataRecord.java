package model.data;

import java.io.Serializable;
import java.util.ArrayList;

public class DataRecord implements DataStructure, Serializable {

	private static final long serialVersionUID = 2558014894569970791L;

	private String name;
	private ArrayList<DataAttribute> attributes;
	private ArrayList<DataRecord> subRecords;
	private DataRecord parent;

	public DataRecord(String name) {
		this(name, new ArrayList<>(), new ArrayList<>(), null);
	}

	public DataRecord(String name, DataRecord parent) {
		this(name, new ArrayList<>(), new ArrayList<>(), parent);
	}

	public DataRecord(String name, ArrayList<DataAttribute> attributes,
			ArrayList<DataRecord> subRecords, DataRecord parent) {
		super();
		this.name = name;
		this.attributes = attributes;
		this.subRecords = subRecords;
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<DataAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(ArrayList<DataAttribute> attributes) {
		this.attributes = attributes;
	}

	public ArrayList<DataRecord> getSubRecords() {
		return subRecords;
	}

	public void setSubRecords(ArrayList<DataRecord> subRecords) {
		this.subRecords = subRecords;
	}

	@Override
	public DataRecord getParent() {
		return parent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((subRecords == null) ? 0 : subRecords.hashCode());
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
		DataRecord other = (DataRecord) obj;
		if (attributes == null) {
			if (other.attributes != null)
				return false;
		} else if (!attributes.equals(other.attributes))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (subRecords == null) {
			if (other.subRecords != null)
				return false;
		} else if (!subRecords.equals(other.subRecords))
			return false;
		return true;
	}

	@Override
	public String toString() {
		if(getParent() != null)
			return getParent() + "->" + getName();
		return getName();
	}
}