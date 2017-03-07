package util;

public class IndexResult {

	private int index_start;
	private int index_end;
	
	public IndexResult(int index_start, int index_end) {
		super();
		this.index_start = index_start;
		this.index_end = index_end;
	}

	public int getIndex_start() {
		return index_start;
	}

	public void setIndex_start(int index_start) {
		this.index_start = index_start;
	}

	public int getIndex_end() {
		return index_end;
	}

	public void setIndex_end(int index_end) {
		this.index_end = index_end;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + index_end;
		result = prime * result + index_start;
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
		IndexResult other = (IndexResult) obj;
		if (index_end != other.index_end)
			return false;
		if (index_start != other.index_start)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[start=" + index_start + ", end=" + index_end + "]";
	}
}