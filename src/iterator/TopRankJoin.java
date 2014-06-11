package iterator;

import global.AttrOperator;
import global.AttrType;
import global.GlobalConst;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tests.TopRankJoinState;
import btree.BTFileScan;
import btree.BTreeFile;
import btree.IntegerKey;
import btree.KeyClass;
import btree.KeyDataEntry;
import btree.LeafData;
import btree.StringKey;
import bufmgr.PageNotReadException;

public class TopRankJoin extends Iterator {

	public static final String PREFIX = "table";
	private static final int P = 5;
	private final String _outputHeapfileName;
	private boolean _firstRun;
	private int _memorySize;
	private int _returnedCount;
	private Heapfile _outputHeapfile;
	private byte[][] _bufs;
	private CondExpr[] _outFilter;
	private int _getOneIndex;
	private String[] _heapFiles;
	public int _iterationCount;

	private List<Integer> _visited;

	@Override
	public Tuple get_next() throws IOException, JoinsException, IndexException,
			InvalidTupleSizeException, InvalidTypeException,
			PageNotReadException, TupleUtilsException, PredEvalException,
			SortException, LowMemException, UnknowAttrType,
			UnknownKeyTypeException, Exception {
		// we have to sort the output tuples that we got from sorted/random access phases
		// we have to call the sort iterator here.
//		if (_firstRun) {
//			_firstRun = false;
//			runFAAlgorithm(_topN);
//			// create a sort iterator on _buffer
//		    FileScan outputTuples = null;
//			try {
//				FldSpec[] projection = new FldSpec[_outputAttrTypes.length];
//				for (int i = 0; i < projection.length; i++) {
//					projection[i] = new FldSpec(new RelSpec(RelSpec.outer), i + 1);
//				}
//				outputTuples = new FileScan(_outputHeapfileName, _outputAttrTypes, _outputStrSizes,
//						(short)_outputAttrTypes.length, (short)_outputAttrTypes.length, projection, null);
//			} catch (Exception e) {
//				System.err.println("error creating file scan iterator for output tuples");
//				e.printStackTrace();
//			}
//			AttrType[] extendedOutputTypes = new AttrType[_outputAttrTypes.length + 1];
//			for (int j = 0; j < _outputAttrTypes.length; j++) {
//				extendedOutputTypes[j] = _outputAttrTypes[j];
//			}
//			extendedOutputTypes[_outputAttrTypes.length] = new AttrType(AttrType.attrReal);
//			_outputIt = new Sort(extendedOutputTypes, (short) extendedOutputTypes.length, _outputStrSizes, outputTuples,
//					extendedOutputTypes.length, new TupleOrder(TupleOrder.Descending) , 4, _memorySize, true);
//		}
//		_returnedCount++;
//		if (_returnedCount > _topN) {
//			return null;
//		}
		if (_getOneIndex >= _topN) {
			return null;
		}
		return getOne();
	}

	@Override
	public void close() throws IOException, JoinsException, SortException, IndexException {
		for (Sort inputTable: _inputTables) {
			inputTable.close();
		}
	}

	public TopRankJoin(int numTables, AttrType[][] in, int[] len_in, short[][] s_sizes, int[] join_col_in,
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
		_counter = new HashMap<String, boolean[]>();
		_completedTupleCount = 0;
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
		_memorySize = amt_of_mem;
		_outputHeapfileName = new Double(Math.random()).toString() + ".in";
		_returnedCount = 0;
		_buffer = new IoBuf();
		try {
			_outputHeapfile = new Heapfile(_outputHeapfileName);
		} catch (Exception e) {
			System.err.println("error creating buffer heapfile for output tuples");
			e.printStackTrace();
		}
		_bufs = new byte[amt_of_mem][GlobalConst.MINIBASE_PAGESIZE];
		_outFilter = outFilter;
		_memBuffer = new ArrayList<Tuple>();
		_getOneIndex = 0;
		_heapFiles = heapFiles;
		_rankedTuples = new List[_tableCount];
		for (int i = 0; i < _rankedTuples.length; i++) {
			_rankedTuples[i] = new ArrayList<Float>();
		}
		_useIndicator = useIndicator;
		_iterationCount = 0;
	}

	private Map<String, boolean[]> _counter;
	private int _completedTupleCount;
	private int _topN;
	private int _tableCount;

	private IoBuf _buffer;
	private final List<Tuple> _memBuffer;
	private int _nextTable;
	private final List<Sort> _inputTables;
	private String[] _inputIndices;
	private final int[] _joinColIndices;
	private final AttrType[][] _tableTypes;
	private final short[][] _tableStrSizes;
	private final FldSpec[] _projList;
	private boolean _bufferInitialized = false;

	private boolean[] _finishedItCount;
	private Sort _outputIt;

	private short[] _outputStrSizes;
	private AttrType[] _outputAttrTypes;
	private List<Float>[] _rankedTuples;
	private boolean _useIndicator = true;
	private String[] _secondaryIndexFiles;
	private String[] _secondaryHeapFiles;

	private void runFAAlgorithm(int topK) throws SortException, UnknowAttrType, LowMemException, JoinsException,
		IOException, Exception {
		// scan through the sort iterators for each table one record at a time
		// update counters; check for termination
			// check the counter for number of objects which are read numTables
			// times, (_completedTupleCount)
			// if it is equal to _topN then return true else false
			// do validations for null iterators etc.
		// set other table scores as null
		while (!isFATerminated(topK) && !tablesExhausted()) {
			Sort currentTable = _inputTables.get(_nextTable);
			Tuple currentTuple = currentTable.get_next();
			if (currentTuple == null) {
				// end of current table; move to the next
				if (!_useIndicator || !arePItemsRead()) {
					_nextTable++;
					_nextTable %= _tableCount;
				} else {
					_nextTable = getNextTable(P);
				}
				// mark this table finished
				_finishedItCount[_nextTable] = true;
				continue;
			}
			_iterationCount++;
			// check if the entry exists; if it doesn't do random access for
			// this tuple from other tables
			// join these tuples; avg the score and store in the storage
			// if it does update counts
			int fieldIndex = _joinColIndices[_nextTable]; // starts from 0
			AttrType currentFieldType = currentTuple.getTypes()[fieldIndex];
			int currentHash = currentTuple.hashCode();
			String tupleKey = getKeyFromTuple(fieldIndex, currentTuple);

			_rankedTuples[_nextTable].add(currentTuple.getScore());

			if (_counter.containsKey(tupleKey)) {
				boolean[] currentCount = _counter.get(tupleKey);
				// check for tuple that was ingored because of the outputFilter
				if (currentCount != null) {
					if (currentCount[_nextTable]) {
						// skip; don't do anything
						_iterationCount--;
						// This was a useless read
					} else {
						currentCount[_nextTable] = true;
						//_counter.put(tupleKey, ++currentCount);
						boolean isCompleted = true;
						for (int i = 0; i < currentCount.length; i++) {
							isCompleted = isCompleted && currentCount[i];
							if (!isCompleted) {
								break;
							}
						}
						//if (currentCount == _tableCount) {
						if (isCompleted) {
							_completedTupleCount++;
						}
					}
				} 
			} else {
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
							Heapfile hf = new Heapfile(_heapFiles[i]);
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
//					tuples[i] = iscan.get_next();
//					if (tuples[i] == null) {
//						randomAccessMiss = true;
//						break;
//					}
				}
				if (randomAccessMiss) {
					// move the table pointer to the next one cyclicly
					if (!_useIndicator || !arePItemsRead()) {
						_nextTable++;
						_nextTable %= _tableCount;
					} else {
						_nextTable = getNextTable(P);
					}
					continue;
				}
				// join all the tuples in tuples array using the _tableTypes to generate the outputTuple
				// check for the filter condition first
				if (PredEval.Eval(_outFilter, tuples, _tableTypes)) {
					Tuple outputTuple = new Tuple();
					AttrType[] outputAttrTypes = new AttrType[_projList.length];
					_outputStrSizes = TupleUtils.setup_op_tuple(outputTuple, outputAttrTypes,
							_tableTypes, _tableStrSizes, _projList);
					_outputAttrTypes = outputAttrTypes;
					Projection.MultiJoin(tuples, _tableTypes, outputTuple, _projList);

					if (!_bufferInitialized) {
						_bufferInitialized = true;
						_buffer.init(_bufs, _memorySize, outputTuple.getLength(), _outputHeapfile);
					}
					_buffer.Put(outputTuple);
					sortedInsert(outputTuple);
					boolean[] countList = new boolean[_tableCount];
					countList[_nextTable] = true;
					_counter.put(tupleKey, countList);
				} else {
					// mark the tuple as not needed
					_counter.put(tupleKey, null);
				}
			}
			// move the table pointer to the next one cyclicly
			if (!_useIndicator || !arePItemsRead()) {
				_nextTable++;
				_nextTable %= _tableCount;
			} else {
				_nextTable = getNextTable(P);
			}
		}
		_buffer.flush();
	}

	private boolean isFATerminated(int topK) {
		if (_useIndicator && !arePItemsRead()) {
			return false;
		}
		return _completedTupleCount == topK;
	}

	public Tuple getOne() {
		try {
			runFAAlgorithm(_getOneIndex + 1);
			if (_getOneIndex < _memBuffer.size()) {
				return _memBuffer.get(_getOneIndex++);
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}

	public void sortedInsert(Tuple tuple) throws FieldNumberOutOfBoundException, IOException {
		if (_memBuffer.isEmpty()) {
			_memBuffer.add(tuple);
		} else {
			for (int i = 0; i <= _memBuffer.size(); i++) {
				if (i == _memBuffer.size()) {
					_memBuffer.add(tuple);
					break;
				} else if (_memBuffer.get(i).getScore() < tuple.getScore()) {
					_memBuffer.add(i, tuple);
					break;
				}
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

	private int getNextTable(int p) throws FieldNumberOutOfBoundException, IOException {
		// uses indicator
		float maxIndicator = 0;
		int maxTable = -1;
		for (int i = 0; i < _tableCount; i++) {
			if (_finishedItCount[i]) {
				continue;
			}
			float currentIndicator = 0;
			float zScore = _rankedTuples[i].get(_rankedTuples[i].size() - 1);
			float zpScore = _rankedTuples[i].get(_rankedTuples[i].size() - p);
			currentIndicator = (float) (0.5 * (zpScore - zScore));
			int count = 0;
			for (Tuple tuple : _memBuffer) {
				boolean[] bs = _counter.get(getKeyFromTuple(0, tuple));
				if (!bs[i]) {
					count++;
				}
			}
			currentIndicator *= count;
			if (currentIndicator >= maxIndicator) {
				maxIndicator = currentIndicator;
				maxTable = i;
			}
		}
		return maxTable;
	}

	public void setSecondaryIndexFiles(String[] indexFiles, String[] heapFiles) {
		_secondaryIndexFiles = indexFiles;
		_secondaryHeapFiles = heapFiles;
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

}