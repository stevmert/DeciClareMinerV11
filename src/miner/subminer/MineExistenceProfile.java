package miner.subminer;

import java.io.Serializable;

public class MineExistenceProfile implements MineProfile, Serializable {

	private static final long serialVersionUID = 8837947306034967714L;

	private boolean atLeast;
	private int atLeast_LowerBound;
	private int atLeast_UpperBound;
	private boolean atMost;
	private int atMost_LowerBound;
	private int atMost_UpperBound;

	public MineExistenceProfile(boolean atLeast, int atLeast_LowerBound, int atLeast_UpperBound, boolean atMost,
			int atMost_LowerBound, int atMost_UpperBound) {
		super();
		if(!atLeast && !atMost)
			throw new IllegalArgumentException();
		this.atLeast = atLeast;
		this.atLeast_LowerBound = atLeast_LowerBound;
		this.atLeast_UpperBound = atLeast_UpperBound;
		this.atMost = atMost;
		this.atMost_LowerBound = atMost_LowerBound;
		this.atMost_UpperBound = atMost_UpperBound;
	}

	public MineExistenceProfile(boolean atLeast, boolean atMost,
			int lowerBound, int upperBound) {
		this(atLeast, lowerBound, upperBound, atMost, lowerBound, upperBound);
	}

	public MineExistenceProfile(boolean atLeast, boolean atMost) {
		this(atLeast, 1, -1, atMost, 0, -1);
	}

	public boolean isAtLeast() {
		return atLeast;
	}

	public void setAtLeast(boolean atLeast) {
		this.atLeast = atLeast;
	}

	public int getAtLeast_LowerBound() {
		return atLeast_LowerBound;
	}

	public void setAtLeast_LowerBound(int atLeast_LowerBound) {
		this.atLeast_LowerBound = atLeast_LowerBound;
	}

	public int getAtLeast_UpperBound() {
		return atLeast_UpperBound;
	}

	public void setAtLeast_UpperBound(int atLeast_UpperBound) {
		this.atLeast_UpperBound = atLeast_UpperBound;
	}

	public boolean isAtMost() {
		return atMost;
	}

	public void setAtMost(boolean atMost) {
		this.atMost = atMost;
	}

	public int getAtMost_LowerBound() {
		return atMost_LowerBound;
	}

	public void setAtMost_LowerBound(int atMost_LowerBound) {
		this.atMost_LowerBound = atMost_LowerBound;
	}

	public int getAtMost_UpperBound() {
		return atMost_UpperBound;
	}

	public void setAtMost_UpperBound(int atMost_UpperBound) {
		this.atMost_UpperBound = atMost_UpperBound;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (atLeast ? 1231 : 1237);
		result = prime * result + atLeast_LowerBound;
		result = prime * result + atLeast_UpperBound;
		result = prime * result + (atMost ? 1231 : 1237);
		result = prime * result + atMost_LowerBound;
		result = prime * result + atMost_UpperBound;
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
		MineExistenceProfile other = (MineExistenceProfile) obj;
		if (atLeast != other.atLeast)
			return false;
		if (atLeast_LowerBound != other.atLeast_LowerBound)
			return false;
		if (atLeast_UpperBound != other.atLeast_UpperBound)
			return false;
		if (atMost != other.atMost)
			return false;
		if (atMost_LowerBound != other.atMost_LowerBound)
			return false;
		if (atMost_UpperBound != other.atMost_UpperBound)
			return false;
		return true;
	}
}