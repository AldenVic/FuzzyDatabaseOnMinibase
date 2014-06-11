package tests;

import iterator.IoBuf;

import java.util.Map;

public class TopRankJoinState {

	public Map<String, boolean[]> _counter;
	public int _completedTupleCount;
	public IoBuf _buffer;
	public int _nextTable;
	public boolean[] _finishedItCount;

	public TopRankJoinState() {	}

}
