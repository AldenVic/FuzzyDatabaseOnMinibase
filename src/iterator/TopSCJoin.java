package iterator;

import global.AttrType;
import global.TupleOrder;
import heap.FieldNumberOutOfBoundException;
import heap.InvalidTupleSizeException;
import heap.InvalidTypeException;
import heap.Tuple;
import index.IndexException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bufmgr.PageNotReadException;

public class TopSCJoin extends Iterator {


	public static final String PREFIX = "table";
	private static final int P = 5;
	private final String _outputHeapfileName;
	private boolean _firstRun;
	private int _memorySize;
	private int _currentItem;
	private CondExpr[] _outFilter;
	private Map<String, List<ObjectData>> _memBuffer;
	private List<Float> _currentBests;
	private List<TupleAggregate> _topTuples;
	public int _iterationCount;
	private int _returnItem;
	private List<Float>[] _rankedTuples;
	private boolean _rr;

	@Override
	public Tuple get_next() throws IOException, JoinsException, IndexException,
			InvalidTupleSizeException, InvalidTypeException,
			PageNotReadException, TupleUtilsException, PredEvalException,
			SortException, LowMemException, UnknowAttrType,
			UnknownKeyTypeException, Exception {
		if (_returnItem == _topN) {
			return null;
		}
		Tuple tuple = getOne();
		return tuple;
	}

	@Override
	public void close() throws IOException, JoinsException, SortException, IndexException {
		for (Sort inputTable: _inputTables) {
			inputTable.close();
		}
	}

	public TopSCJoin(int numTables, AttrType[][] in, int[] len_in, short[][] s_sizes, int[] join_col_in,
			Iterator[] am, int amt_of_mem, CondExpr[] outFilter,
			FldSpec[] proj_list, int n_out_flds, int num, boolean rr) throws SortException, IOException {
		_nextTable = 0;
		_inputTables = new ArrayList<Sort>();
		for (int i = 0; i < am.length; i++) {
			AttrType[] extendedTypes = new AttrType[in[i].length + 1];
			for (int j = 0; j < in[i].length; j++) {
				extendedTypes[j] = in[i][j];
			}
			extendedTypes[in[i].length] = new AttrType(AttrType.attrReal);
			Sort sortIt = new Sort(extendedTypes, (short)extendedTypes.length, s_sizes[i], am[i], (short)(extendedTypes.length),
					new TupleOrder(TupleOrder.Descending), 4, amt_of_mem, true);
			_inputTables.add(sortIt);
			//MainDriver.printIterator(sortIt, in[i]);
		}

		_topN = num;
		_tableCount = am.length;
		_joinColIndices = join_col_in;
		_tableTypes = in;
		_tableStrSizes = s_sizes;
		_projList = proj_list;
		_finishedItCount = new boolean[_tableCount];
		for (int i = 0; i < _finishedItCount.length; i++) {
			_finishedItCount[i] = false;
		}
		_firstRun = true;
		_memorySize = amt_of_mem;
		_outputHeapfileName = new Double(Math.random()).toString() + ".in";
		_outFilter = outFilter;
		_memBuffer = new HashMap<String, List<ObjectData>>();
		_currentBests = new ArrayList<Float>();
		for (int i = 0; i < _tableCount; i++) {
			_currentBests.add((float) 1.0);
		}
		_blankTuples = new Tuple[_tableCount];
		_bufTuples = new HashMap<String, Tuple>();
		_currentItem = 0;
		_rankedTuples = new List[_tableCount];
		for (int i = 0; i < _rankedTuples.length; i++) {
			_rankedTuples[i] = new ArrayList<Float>();
		}
		_rr = rr;
	}

	private int _topN;
	private int _tableCount;

	private int _nextTable;
	private final List<Sort> _inputTables;
	private final int[] _joinColIndices;
	private final AttrType[][] _tableTypes;
	private final short[][] _tableStrSizes;
	private final FldSpec[] _projList;

	private boolean[] _finishedItCount;
	private Sort _outputIt;

	private short[] _outputStrSizes;
	private AttrType[] _outputAttrTypes;
	private Tuple[] _blankTuples;
	private Map<String, Tuple> _bufTuples;

	private void runSCAlgorithm() throws SortException, UnknowAttrType, LowMemException, JoinsException,
		IOException, Exception {
		for (int i = 0; i < _blankTuples.length; i++) {
			_blankTuples[i] = new Tuple();
			_blankTuples[i].setHdr((short) _tableTypes[i].length,
					_tableTypes[i], _tableStrSizes[i]);
		}
		// scan through the sort iterators for each table one record at a time
		// update counters; check for termination
			// check the counter for number of objects which are read numTables
			// times, (_completedTupleCount)
			// if it is equal to _topN then return true else false
			// do validations for null iterators etc.
		// set other table scores as null
		while (!isSCTerminated() && !tablesExhausted()) {
			Sort currentTable = _inputTables.get(_nextTable);
			Tuple currentTuple = currentTable.get_next();
			if (currentTuple == null) {
				// end of current table; move to the next
				if (_rr || !arePItemsRead()) {
					_nextTable++;
					_nextTable %= _tableCount;
				} else {
					_nextTable = getNextTable(P);
				}
				// mark this table finished
				_finishedItCount[_nextTable] = true;
				continue;
			}
			// check if the entry exists; if it doesn't do random access for
			// this tuple from other tables
			// join these tuples; avg the score and store in the storage
			// if it does update counts
			int fieldIndex = _joinColIndices[_nextTable]; // starts from 0
			String tupleKey = getKeyFromTuple(fieldIndex, currentTuple);
			// update the current bests
			// for the current object, check if entry exists,
			// if it doesn't, create new and set bests using the current bests and worsts as 0
			// if it does, update the values at the current table
			// update the bests for all the objects which are not flagged original for this table
			_currentBests.set(_nextTable, currentTuple.getScore());
			_rankedTuples[_nextTable].add(currentTuple.getScore());
			if (_memBuffer.containsKey(tupleKey)) {
				List<ObjectData> scores = _memBuffer.get(tupleKey);
				ObjectData data = scores.get(_nextTable);
				if (data.isActualValue()) {
					// we're seeing the value for this table again, must be duplicate; ignore
				} else {
					data.setScore(currentTuple.getScore());
					data.setActualValue(true);
					// update the blank join tuple
					Tuple blankJoinTuple = _bufTuples.get(tupleKey);
					int columnOffset = 0;
					for (int i = 0; i < _nextTable; i++) {
						columnOffset += _tableTypes[i].length;
					}
					for (int i = 0; i < _tableTypes[_nextTable].length; i++) {
						if (i != _joinColIndices[_nextTable]) {
							AttrType colType = _tableTypes[_nextTable][i];
							switch (colType.attrType) {
							case AttrType.attrInteger:
								blankJoinTuple.setIntFld(columnOffset + i + 1, currentTuple.getIntFld(i + 1));
								break;
							case AttrType.attrReal:
								blankJoinTuple.setFloFld(columnOffset + i + 1, currentTuple.getFloFld(i + 1));
								break;
							case AttrType.attrString:
								blankJoinTuple.setStrFld(columnOffset + i + 1, currentTuple.getStrFld(i + 1));
								break;
							default:
								throw new UnknowAttrType("Don't know how to handle attrSymbol, attrNull");
							}
						}
					}
					_iterationCount++;
				}
			} else {
				_iterationCount++;
				List<ObjectData> scores = new ArrayList<ObjectData>();
				for (int i = 0; i < _tableCount; i++) {
					if (i == _nextTable) {
						scores.add(new ObjectData(currentTuple.getScore(), true));
					} else {
						scores.add(new ObjectData(_currentBests.get(i), false));
					}
				}
				_memBuffer.put(tupleKey, scores);
				Tuple[] joinTuples = new Tuple[_blankTuples.length];
				for (int i = 0; i < joinTuples.length; i++) {
					if (i == _nextTable) {
						joinTuples[i] = currentTuple;
					} else {
						joinTuples[i] = _blankTuples[i];
					}
				}
				Tuple jTuple = new Tuple();
				AttrType[] outputAttrTypes = new AttrType[_projList.length];
				int totalFields = 0;
				for (int i = 0; i < _tableTypes.length; i++) {
					totalFields += _tableTypes[i].length;
					joinTuples[i].setStrFld(_joinColIndices[i] + 1, tupleKey);
				}
				FldSpec[] projList = new FldSpec[totalFields];
				int count = 0;
				for (int i = 0; i < _tableTypes.length; i++) {
					for (int j = 0; j < _tableTypes[i].length; j++) {
						projList[count++] = new FldSpec(i, j + 1);
					}
				}
				TupleUtils.setup_op_tuple(jTuple, outputAttrTypes, _tableTypes, _tableStrSizes, projList);
				Projection.MultiJoin(joinTuples, _tableTypes, jTuple , projList);
				_bufTuples.put(tupleKey, jTuple);
			}
			for (String objectKey : _memBuffer.keySet()) {
				List<ObjectData> scores = _memBuffer.get(objectKey);
				ObjectData data = scores.get(_nextTable);
				if (!data.isActualValue()) {
					data.setScore(_currentBests.get(_nextTable));
				}
			}
			// move the table pointer to the next one
			if (_rr || !arePItemsRead()) {
				_nextTable++;
				_nextTable %= _tableCount;
			} else {
				_nextTable = getNextTable(P);
			}
		}
	}

	private boolean tablesExhausted() {
		boolean exhausted = true;
		for (int i = 0; i < _finishedItCount.length; i++) {
			exhausted = exhausted && _finishedItCount[i];
		}
		return exhausted;
	}

	private String getKeyFromTuple(int fieldIndex, Tuple t) throws FieldNumberOutOfBoundException, IOException {
		AttrType fieldType = t.getTypes()[fieldIndex];
		switch (fieldType.attrType) {
		case AttrType.attrString:
			return t.getStrFld(fieldIndex + 1);
		case AttrType.attrInteger:
			return new Integer(t.getIntFld(fieldIndex + 1)).toString();
		case AttrType.attrReal:
			return new Float(t.getFloFld(fieldIndex + 1)).toString();
		default:
			return null;
		}
	}

	private class ObjectData {

		private float _score;
		private boolean _actualValue;

		public ObjectData(float score, boolean actualValue) {
			_score = score;
			_actualValue = actualValue;
		}

		public float getScore() {
			return _score;
		}

		public void setScore(float score) {
			_score = score;
		}

		public boolean isActualValue() {
			return _actualValue;
		}

		public void setActualValue(boolean actualValue) {
			_actualValue = actualValue;
		}

	}

	private class TupleAggregate {

		private String _key;
		private List<ObjectData> _scores;

		public TupleAggregate(String key, List<ObjectData> scores) {
			_key = key;
			_scores = scores;
		}

		public String getKey() {
			return _key;
		}

		public List<ObjectData> getScores() {
			return _scores;
		}

		public void setKey(String key) {
			_key = key;
		}

		public void setScores(List<ObjectData> scores) {
			_scores = scores;
		}

		public float getScore() {
			float score = 0;
			for (ObjectData data : _scores) {
				score += data.getScore();
			}
			return score/_scores.size();
		}

	}

	private void purgeIncomplete() {
		List<String> removeSet = new ArrayList<String>();
		for (String string : _memBuffer.keySet()) {
			TupleAggregate ta = new TupleAggregate(string, _memBuffer.get(string));
			boolean completed = true;
			for (ObjectData objectData : _memBuffer.get(string)) {
				completed = completed && objectData.isActualValue();
			}
			if (!completed) {
				removeSet.add(string);
			}
		}
		for (String string : removeSet) {
			_memBuffer.remove(string);
		}
	}

	private boolean isSCTerminated() {
		if (!_rr && !arePItemsRead()) {
			return false;
		}
		if (_memBuffer.size() <= _returnItem + 1 || _iterationCount < _tableCount) {
			return false;
		}
//		if (tablesExhausted()) {
//			purgeIncomplete();
//		}
		List<TupleAggregate> objectScores = new ArrayList<TupleAggregate>();
		for (String	objectKey : _memBuffer.keySet()) {
			objectScores.add(new TupleAggregate(objectKey, _memBuffer.get(objectKey)));
		}
		List<TupleAggregate> sortedTuples = sortOnScore(objectScores);
		String kthKey = sortedTuples.get(_returnItem).getKey();
		List<ObjectData> list = _memBuffer.get(kthKey);
		boolean completed = true;
		for (ObjectData objectData : list) {
			completed = completed && objectData.isActualValue();
			if (!completed) {
				return completed;
			}
		}
		_topTuples = sortedTuples.subList(0, _returnItem + 1);
		return completed;
	}

	public boolean arePItemsRead() {
		boolean read = true;
		for (int i = 0; i < _rankedTuples.length; i++) {
			read = read && (_finishedItCount[i] || _rankedTuples[i].size() >= P);
			if (!read) {
				return read;
			}
		}
		return read;
	}

	public Tuple getOne() throws SortException, UnknowAttrType, LowMemException, JoinsException, Exception {
		runSCAlgorithm();
		if (_returnItem >= _topTuples.size()) {
			return null;
		}
		TupleAggregate tupleAggregate = _topTuples.get(_returnItem++);
		_bufTuples.get(tupleAggregate.getKey()).setScore(tupleAggregate.getScore());
		return _bufTuples.get(tupleAggregate.getKey());
	}

	private List<TupleAggregate> sortOnScore(List<TupleAggregate> objectScores) {
		Comparator<TupleAggregate> worstScoreDescending = new Comparator<TupleAggregate>() {
			@Override
			public int compare(TupleAggregate o1, TupleAggregate o2) {
				float score1 = 0;
				float score2 = 0;
				score1 = o1.getScore();
				score2 = o2.getScore();

				if(score1 > score2)
					return -1;
				else if(score1 < score2)
					return 1;
				else
					return 0;
			}
		};
		Collections.sort(objectScores, worstScoreDescending);
		return objectScores;
	}

	private int getNextTable(int p) {
		// uses indicator
//		float maxIndicator = 0;
//		int maxTable = -1;
//		for (int i = 0; i < _tableCount; i++) {
//			if (_finishedItCount[i]) {
//				continue;
//			}
//			float currentIndicator = 0;
//			float zScore = _rankedTuples[i].get(_rankedTuples[i].size() - 1);
//			float zpScore = _rankedTuples[i].get(_rankedTuples[i].size() - p);
//			currentIndicator = (float) (0.5 * (zpScore - zScore));
//			int count = 0;
//			for (String key : _memBuffer.keySet()) {
//				if (!_memBuffer.get(key).get(i).isActualValue()) {
//					count++;
//				}
//			}
//			currentIndicator *= count;
//			if (currentIndicator >= maxIndicator) {
//				maxIndicator = currentIndicator;
//				maxTable = i;
//			}
//		}
//		return maxTable;
		_nextTable++;
		_nextTable %= _tableCount;
		return _nextTable;
	}

}
