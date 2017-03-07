package miner.subminer;

import java.io.Serializable;

public class MineRelationProfile implements MineProfile, Serializable {

	private static final long serialVersionUID = -3227168856665789930L;

	private MineExistenceProfile from;
	private MineExistenceProfile to;

	public MineRelationProfile(MineExistenceProfile from, MineExistenceProfile to) {
		super();
		if(from == null
				|| to == null)
			throw new IllegalArgumentException("from or to cannot be null!");
		this.from = from;
		this.to = to;
	}

	public MineExistenceProfile getFrom() {
		return from;
	}

	public void setFrom(MineExistenceProfile from) {
		this.from = from;
	}

	public MineExistenceProfile getTo() {
		return to;
	}

	public void setTo(MineExistenceProfile to) {
		this.to = to;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
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
		MineRelationProfile other = (MineRelationProfile) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}
}