package iterator;

import heap.Tuple;

import java.util.Arrays;

class TupleInfo {

	private int[] _tupleHashes;
	private boolean[] _visited;

	public TupleInfo(Tuple[] tuples) {
		_tupleHashes = new int[tuples.length];
		for (int i = 0; i < tuples.length; i++) {
			_tupleHashes[i] = tuples[i].hashCode();
		}
		_visited = new boolean[_tupleHashes.length];
		for (int i = 0; i < _visited.length; i++) {
			_visited[i] = false;
		}
	}

	public int getHash(int i) {
		return _tupleHashes[i];
	}

	public TupleInfo(int[] tupleHashes) {
		_tupleHashes = tupleHashes;
		_visited = new boolean[_tupleHashes.length];
		for (int i = 0; i < _visited.length; i++) {
			_visited[i] = false;
		}
	}

	public boolean containsHash(int hashCode) {
		for (int i = 0; i < _tupleHashes.length; i++) {
			if (_tupleHashes[i] == hashCode) {
				return true;
			}
		}
		return false;
	}

	public boolean isVisited(int hashCode) {
		if (!containsHash(hashCode)) {
			return false;
		}
		for (int i = 0; i < _tupleHashes.length; i++) {
			if (_tupleHashes[i] == hashCode) {
				return _visited[i];
			}
		}
		return false;
	}

	public boolean isTableVisited(int tableIndex) {
		return _visited[tableIndex];
	}

	public void setVisited(int hashCode) {
		if (!containsHash(hashCode)) return;
		for (int i = 0; i < _tupleHashes.length; i++) {
			if (_tupleHashes[i] == hashCode) {
				_visited[i] = true;
			}
		}
	}

	public boolean isCompleted() {
		boolean completed = true;
		for (int i = 0; i < _visited.length; i++) {
			completed = completed && _visited[i];
		}
		return completed;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(_tupleHashes);
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
		TupleInfo other = (TupleInfo) obj;
		if (!Arrays.equals(_tupleHashes, other._tupleHashes))
			return false;
		return true;
	}

}