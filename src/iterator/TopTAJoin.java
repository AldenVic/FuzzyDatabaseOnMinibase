package iterator;

import global.AttrOperator;
import global.AttrType;
import global.IndexType;
import global.RID;
import global.TupleOrder;
import heap.FieldNumberOutOfBoundException;
import heap.Heapfile;
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

import tests.MainDriver;

import btree.BTFileScan;
import btree.BTreeFile;
import btree.IntegerKey;
import btree.KeyClass;
import btree.KeyDataEntry;
import btree.LeafData;
import btree.StringKey;
import bufmgr.PageNotReadException;

public class TopTAJoin extends Iterator {

	private static final int P = MainDriver.P;

	@Override
	public Tuple get_next() throws IOException, JoinsException, IndexException,
			InvalidTupleSizeException, InvalidTypeException,
			PageNotReadException, TupleUtilsException, PredEvalException,
			SortException, LowMemException, UnknowAttrType,
			UnknownKeyTypeException, Exception {
		// we have to sort the output tuples that we got from sorted/random access phases
		// we have to call the sort iterator here.
		if (_firstRun) {
			_firstRun = false;
			_returnItem = _topN - 1;
			runTAAlgorithm();
			// sort the contents in _memBuffer and return them 1 by 1
			_sortedList = new ArrayList<Tuple>();
			for (String key : _sortedKeys) {
				_sortedList.add(_memBuffer.get(key));
			}
			_sortedList = sortOnScores();
			_currentItem = -1;
		}
		_currentItem++;
		if (_currentItem == _topN || _currentItem >= _sortedList.size()) {
			return null;
		}
		return _sortedList.get(_currentItem);
	}

	@Override
	public void close() throws IOException, JoinsException, SortException, IndexException {
		for (Sort inputTable: _inputTables) {
			inputTable.close();
		}
	}

	public TopTAJoin(int numTables, AttrType[][] in, int[] len_in, short[][] s_sizes, int[] join_col_in,
			Iterator[] am, IndexType[] index, String[] indNames, String[] heapFiles, int amt_of_mem, CondExpr[] outFilter,
			FldSpec[] proj_list, int n_out_flds, int num, boolean useIndicator) throws SortException, IOException {
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
		_inputIndices = indNames;
		_finishedItCount = new boolean[_tableCount];
		for (int i = 0; i < _finishedItCount.length; i++) {
			_finishedItCount[i] = false;
		}
		_firstRun = true;
		_outFilter = outFilter;
		_memBuffer = new HashMap<String, Tuple>();
		_tableThresholds = new float[_tableCount];
		for (int i = 0; i < _tableThresholds.length; i++) {
			_tableThresholds[i] = -1;
		}
		_returnItem = 0;
		_sortedKeys = new ArrayList<String>();
		_heapFileNames = heapFiles;
		_iterationCount = 0;
		_useIndicator = useIndicator;
		_rankedTuples = new List[_tableCount];
		for (int i = 0; i < _rankedTuples.length; i++) {
			_rankedTuples[i] = new ArrayList<Float>();
		}
	}

	private boolean _firstRun;
	private CondExpr[] _outFilter;

	// size k atmost
	private Map<String, Tuple> _memBuffer;
	private List<String> _sortedKeys;
	private float[] _tableThresholds;
	private List<Tuple> _sortedList;
	private int _currentItem;
	private int _returnItem;
	private String[] _heapFileNames;
	public int _iterationCount;
	private boolean _useIndicator;
	private int _topN;
	private int _tableCount;

	private int _nextTable;
	private final List<Sort> _inputTables;
	private String[] _inputIndices;
	private final int[] _joinColIndices;
	private final AttrType[][] _tableTypes;
	private final short[][] _tableStrSizes;
	private final FldSpec[] _projList;

	private boolean[] _finishedItCount;

	private String[] _secondaryIndexFiles;
	private String[] _secondaryHeapFiles;
	private List<Float>[] _rankedTuples;

	private void runTAAlgorithm() throws SortException, UnknowAttrType, LowMemException, JoinsException,
		IOException, Exception {
		// scan through the sort iterators for each table one record at a time
		// update counters; check for termination
			// check the counter for number of objects which are read numTables
			// times, (_completedTupleCount)
			// if it is equal to _topN then return true else false
			// do validations for null iterators etc.
		// set other table scores as null
		while (!isTATerminated() && !tablesExhausted()) {
			_iterationCount++;
			Sort currentTable = _inputTables.get(_nextTable);
			Tuple currentTuple = currentTable.get_next();
			if (currentTuple == null) {
				// mark this table finished
				_finishedItCount[_nextTable] = true;
				// end of current table; move to the next
				seekToNext();
				continue;
			}
			// update table threshold
			_tableThresholds[_nextTable] = currentTuple.getScore();

			// check if the entry exists; if it doesn't do random access for
			// this tuple from other tables
			// join these tuples; avg the score and store in the storage
			// if it does update counts
			int fieldIndex = _joinColIndices[_nextTable]; // starts from 0
			AttrType currentFieldType = currentTuple.getTypes()[fieldIndex];
			String tupleKey = getKeyFromTuple(fieldIndex, currentTuple);
			_rankedTuples[_nextTable].add(currentTuple.getScore());
			//int tupleKey = currentTuple.hashCode();
			if (!_memBuffer.containsKey(tupleKey)) {
				Tuple[] tuples = new Tuple[_tableCount];
				tuples[_nextTable] = currentTuple;
				boolean randomAccessMiss = false;
				for (int i = 0; i < _tableCount; i++) {
					if (i == _nextTable) {
						continue;
					}
					String currentIndexFile = _inputIndices[i];
					int currentFieldIndex = _joinColIndices[i];
//					IndexScan iscan = null;
					AttrType[] currentTypes = _tableTypes[i];
					short[] currentStrSizes = _tableStrSizes[i];
					try {
						// project the entire tuple
						FldSpec[] projlist = new FldSpec[currentTypes.length];
						for (int j = 0; j < projlist.length; j++) {
							projlist[j] = new FldSpec(new RelSpec(RelSpec.outer), j + 1);
						}
						CondExpr[] filter = new CondExpr[2];
						filter[0] = new CondExpr();
						filter[0].op = new AttrOperator(AttrOperator.aopEQ);
						filter[0].type1 = new AttrType(AttrType.attrSymbol);
						filter[0].type2 = currentFieldType;
						filter[0].operand1 = new Operand();
						filter[0].operand1.symbol = new FldSpec(new RelSpec(RelSpec.outer), currentFieldIndex + 1);
						filter[0].operand2 = new Operand();
						switch (currentFieldType.attrType) {
						case AttrType.attrString:
							filter[0].operand2.string = currentTuple.getStrFld(fieldIndex + 1);
						case AttrType.attrInteger:
							filter[0].operand2.integer = currentTuple.getIntFld(fieldIndex + 1);
						case AttrType.attrReal:
							filter[0].operand2.real = currentTuple.getFloFld(fieldIndex + 1);
						}
						
						filter[1] = null;

						KeyClass btreeKey = null;
						if (currentFieldType.attrType == AttrType.attrInteger) {
							btreeKey = new IntegerKey(currentTuple.getIntFld(currentFieldIndex + 1));
						} else if (currentFieldType.attrType == AttrType.attrString) {
							btreeKey = new StringKey(currentTuple.getStrFld(currentFieldIndex + 1));
						}

						BTreeFile btf = new BTreeFile(currentIndexFile);
						BTFileScan btfScan = btf.new_scan(btreeKey, btreeKey);
						KeyDataEntry keyDataEntry = btfScan.get_next();
						Tuple currentMaxRecord = null;
						if (keyDataEntry == null) {
							randomAccessMiss = true;
						} else {
							RID rid = ((LeafData)keyDataEntry.data).getData();
							Heapfile hf = new Heapfile(_heapFileNames[i]);
							currentMaxRecord = hf.getRecord(rid);
							currentMaxRecord.setHdr((short) _tableTypes[i].length, _tableTypes[i], _tableStrSizes[i]);
							keyDataEntry = btfScan.get_next();
							while (keyDataEntry != null) {
								rid = ((LeafData)keyDataEntry.data).getData();
								Tuple tempRecord = hf.getRecord(rid);
								tempRecord.setHdr((short) _tableTypes[i].length, _tableTypes[i], _tableStrSizes[i]);
								if (currentMaxRecord.getScore() <  tempRecord.getScore()) {
									currentMaxRecord = tempRecord;
								}
								keyDataEntry = btfScan.get_next();
							}
						}

						if (_secondaryIndexFiles != null && _secondaryIndexFiles[i] != null) {
							btf = new BTreeFile(_secondaryIndexFiles[i]);
							btfScan = btf.new_scan(btreeKey, btreeKey);
							keyDataEntry = btfScan.get_next();
							if (keyDataEntry != null) {

								randomAccessMiss = false;
	
								RID rid = ((LeafData)keyDataEntry.data).getData();
								Heapfile hf = new Heapfile(_secondaryHeapFiles[i]);
								Tuple currentRecord = hf.getRecord(rid);
								currentRecord.setHdr((short) _tableTypes[i].length, _tableTypes[i], _tableStrSizes[i]);
								if (currentMaxRecord != null) {
									if (currentMaxRecord.getScore() <  currentRecord.getScore()) {
										currentMaxRecord = currentRecord;
									}
								} else {
									currentMaxRecord = currentRecord;
								}
								keyDataEntry = btfScan.get_next();
								while (keyDataEntry != null) {
									rid = ((LeafData)keyDataEntry.data).getData();
									Tuple tempRecord = hf.getRecord(rid);
									tempRecord.setHdr((short) _tableTypes[i].length, _tableTypes[i], _tableStrSizes[i]);
									if (currentMaxRecord.getScore() <  tempRecord.getScore()) {
										currentMaxRecord = tempRecord;
									}
									keyDataEntry = btfScan.get_next();
								}
							}
						}
						if (randomAccessMiss) {
							break;
						}
						tuples[i] = currentMaxRecord;
					} catch (Exception e) {
						System.err.println("Error doing random access for tuple: ");
						e.printStackTrace();
						return;
					}
				}
				if (randomAccessMiss) {
					// move the table pointer to the next one cyclicly
					seekToNext();
					continue;
				}

				Tuple outputTuple = new Tuple();
				AttrType[] outputAttrTypes = new AttrType[_projList.length];
				TupleUtils.setup_op_tuple(outputTuple, outputAttrTypes, _tableTypes, _tableStrSizes, _projList);
				Projection.MultiJoin(tuples, _tableTypes, outputTuple, _projList);

				// compute minimum if its the first run
				// increment the counter for number of items encountered
				// if the buffer doesn't have k just insert and update minimum
				// else compare current score with the current minimum
				// if we have to replace update the minimum
				sortedInsert(outputTuple, fieldIndex);

			}
			// move the table pointer to the next one cyclicly
			seekToNext();
		}
	}

	public Tuple getOne() throws SortException, UnknowAttrType, LowMemException, JoinsException, IOException, Exception {
		runTAAlgorithm();
		if (_sortedKeys.size() <= _returnItem) {
			return null;
		}
		return _memBuffer.get(_sortedKeys.get(_returnItem++));
	}

	private void sortedInsert(Tuple tuple, int fieldIndex)
			throws FieldNumberOutOfBoundException, IOException {
		String tupleKey = getKeyFromTuple(fieldIndex, tuple);
		if (_sortedKeys.isEmpty()) {
			_sortedKeys.add(tupleKey);
			_memBuffer.put(tupleKey, tuple);
		} else {
			boolean inserted = false;
			for (int i = 0; i < _sortedKeys.size(); i++) {
				String currentKey = _sortedKeys.get(i);
				Tuple currentTuple = _memBuffer.get(currentKey);
				if (currentTuple.getScore() < tuple.getScore()) {
					_sortedKeys.add(i, tupleKey);
					_memBuffer.put(tupleKey, tuple);
					inserted = true;
					break;
				}
			}
			if (!inserted) {
				_sortedKeys.add(tupleKey);
				_memBuffer.put(tupleKey, tuple);
			}
		}
	}

	private boolean isTATerminated() throws FieldNumberOutOfBoundException, IOException {
		if (_sortedKeys.size() < _returnItem + 1) {
			return false;
		}
		float threshold = 0;
		for (int i = 0; i < _tableThresholds.length; i++) {
			if (_tableThresholds[i] == -1) {
				return false;
			}
			threshold += _tableThresholds[i];
		}
		threshold /= _tableThresholds.length;
		Tuple tuple = _memBuffer.get(_sortedKeys.get(_returnItem));
		return threshold < tuple.getScore();
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

	public void setSecondaryIndexFiles(String[] indexFiles, String[] heapFiles) {
		_secondaryIndexFiles = indexFiles;
		_secondaryHeapFiles = heapFiles;
	}

	private List<Tuple> sortOnScores() {
		Comparator<Tuple> tupleComparator = new Comparator<Tuple>() {
			@Override
			public int compare(Tuple o1, Tuple o2) {
				float score1 = 0;
				float score2 = 0;
				try {
					score1 = o1.getScore();
					score2 = o2.getScore();
				} catch (FieldNumberOutOfBoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				if(score1 > score2)
					return -1;
				else if(score1 < score2)
					return 1;
				else
					return 0;
			}
		};
		List<Tuple> sortedList = new ArrayList<Tuple>();
		for (Tuple tuple : _memBuffer.values()) {
			sortedList.add(tuple);
		}
		Collections.sort(sortedList, tupleComparator);
		return sortedList;
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

	public void seekToNext() throws FieldNumberOutOfBoundException, IOException {
		if (!_useIndicator || !arePItemsRead()) {
			_nextTable++;
			_nextTable %= _tableCount;
		} else {
			_nextTable = getNextTable(P);
		}
	}

	private int getNextTable(int p) throws FieldNumberOutOfBoundException, IOException {
		int maxTable = -1;
		float maxThreshold = -1;
		for (int i = 0; i < _tableCount; i++) {
			if (_finishedItCount[i]) {
				continue;
			}
			if (_tableThresholds[i] > maxThreshold) {
				maxTable = i;
				maxThreshold = _tableThresholds[i];
			}
		}
		return maxTable;
	}

}