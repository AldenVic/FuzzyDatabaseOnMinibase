package tests;

import global.AttrOperator;
import global.AttrType;
import global.RID;
import global.SystemDefs;
import global.TupleOrder;
import heap.FieldNumberOutOfBoundException;
import heap.HFBufMgrException;
import heap.HFDiskMgrException;
import heap.HFException;
import heap.Heapfile;
import heap.InvalidSlotNumberException;
import heap.InvalidTupleSizeException;
import heap.InvalidTypeException;
import heap.Scan;
import heap.SpaceNotAvailableException;
import heap.Tuple;
import index.IndexException;
import iterator.CondExpr;
import iterator.DuplElim;
import iterator.DuplElimException;
import iterator.FileScan;
import iterator.FldSpec;
import iterator.Iterator;
import iterator.JoinLowMemory;
import iterator.JoinNewFailed;
import iterator.JoinsException;
import iterator.LowMemException;
import iterator.NestedLoopException;
import iterator.Operand;
import iterator.PredEvalException;
import iterator.RelSpec;
import iterator.SortException;
import iterator.TopNRAJoin;
import iterator.TopNestedLoopsJoin;
import iterator.TopRankJoin;
import iterator.TopSCJoin;
import iterator.TopSortMergeJoin;
import iterator.TopTAJoin;
import iterator.TupleUtils;
import iterator.TupleUtilsException;
import iterator.UnknowAttrType;
import iterator.UnknownKeyTypeException;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import au.com.bytecode.opencsv.CSVReader;
import btree.BTreeFile;
import btree.IntegerKey;
import btree.StringKey;
import bufmgr.HashEntryNotFoundException;
import bufmgr.InvalidFrameNumberException;
import bufmgr.PageNotReadException;
import bufmgr.PageUnpinnedException;
import bufmgr.ReplacerException;
import diskmgr.PCounter;

class Parameters {

	public int numTables;
	public int[] joinColIndices = null;
	public AttrType[][] tableTypes = null;
	public short[][] tableStrSizes = null;
	public int amountOfMemory = 0;
	public int topK = 0;
	public String[] indexFiles;
	public Heapfile[] heapfiles;
	public Heapfile[] addHeapfiles;
	public Heapfile[] subHeapfiles;
	

	public Parameters(int count) {
		numTables = count;
		joinColIndices = new int[numTables];
		tableTypes = new AttrType[numTables][];
		tableStrSizes = new short[numTables][];
		indexFiles = new String[numTables];
		heapfiles = new Heapfile[numTables];
		addHeapfiles = new Heapfile[numTables];
		subHeapfiles = new Heapfile[numTables];
	}

}

public class MainDriver {

	public static final int P = 5;

	public static AttrType[][] _tableTypes;
	public static short[][] _tableStrSizes;
	public static short[][] _tableStrSizesIndexed;
	public static Heapfile[] _heapfiles;
	public static int _tableCount;
	public static int _amountOfMemory = 50;
	public static boolean _ignore_increments = true;
	public static boolean _useIndicator = false;
	public static int[] _recordCount;
	public static int _topK = 0;

	// below copy from main
	// prepare db; clean up previous instances mess
	public static String dbpath = "/tmp/" + System.getProperty("user.name")
			+ ".minibase.fuzzydbtest2";
	public static String logpath = "/tmp/" + System.getProperty("user.name")
			+ ".fuzzydblog2";

	/* 3072 - 0; */
	public static String remove_cmd = "/bin/rm -rf ";
	public static String remove_logcmd = remove_cmd + logpath;
	public static String remove_dbcmd = remove_cmd + dbpath;
	public static String remove_joincmd = remove_cmd + dbpath;

	public static SystemDefs sysdef;

	public static int[] _joinColIndices = { -1, -1, -1, -1, -1, -1, -1, -1, -1 };
	public static String[] _indexFiles = { "-1", "-1", "-1", "-1", "-1", "-1",
			"-1", "-1", "-1" };
	public static Parameters _ta_parameter;
	private static Map<String, List<Integer>> _indexFilesCounter = new HashMap<String, List<Integer>>();

	public MainDriver() {
		sysdef = new SystemDefs(dbpath, 10242880 /* 5GiB */, 5120/*20480*//* 5MiB */, "Clock");
		try {
			Runtime.getRuntime().exec(remove_logcmd);
			Runtime.getRuntime().exec(remove_dbcmd);
			Runtime.getRuntime().exec(remove_joincmd);
		} catch (IOException e) {
			System.err.println("" + e);
		}

	}

	public static Parameters extractParameters(int count) {
		Parameters params = new Parameters(count);
		params.amountOfMemory = _amountOfMemory;
		params.topK = _topK;
		int totalFiles = 0;
		for (int i = 0; i < Utilities._maxFiles; i++) {
			if (_joinColIndices[i] > -1) {
				params.joinColIndices[totalFiles] = _joinColIndices[i];
				params.tableTypes[totalFiles] = Utilities.p3_csvFiles[i]
						.getcType_db_algos();
				params.tableStrSizes[totalFiles] = Utilities.p3_csvFiles[i]
						.get_cSize_str_Tuple();
				params.heapfiles[totalFiles] = Utilities.p3_csvFiles[i]
						.getHF();
				if (Utilities.p3_csvFiles_add[i] != null) {
					params.addHeapfiles[totalFiles] = Utilities.p3_csvFiles_add[i].getHF();
				}
				if (Utilities.p3_csvFiles_sub[i] != null) {
					params.subHeapfiles[totalFiles] = Utilities.p3_csvFiles_sub[i].getHF();
				}
				totalFiles++;
			}
		}
		return params;
	}

	public static void runTA(int count, javax.swing.text.Style style,
			StyledDocument doc) throws JoinsException, IndexException,
			InvalidTupleSizeException, InvalidTypeException,
			PageNotReadException, TupleUtilsException, PredEvalException,
			LowMemException, UnknowAttrType, UnknownKeyTypeException, Exception {

		Parameters params = extractParameters(count);
		int addRemoveCount = 0;
		for(int i = 0; i < Utilities._maxFiles; i++) {
			if (Utilities._filesInDB_add[i] == 2 || Utilities._filesInDB_sub[i] == 2) {
				addRemoveCount++;
			}
		}
		if (!_ignore_increments && addRemoveCount > 0 ) {
			String[] addIndexFiles = new String[params.addHeapfiles.length];
			Iterator[] addTuples = new Iterator[params.addHeapfiles.length];
			String[] addHeapfiles = new String[params.addHeapfiles.length];
			for (int i = 0; i < params.addHeapfiles.length; i++) {
				if (params.addHeapfiles[i] != null) {
					String currentHeapFileName = params.addHeapfiles[i].getFileName();
					addHeapfiles[i] = params.addHeapfiles[i].getFileName();
					String indexFile = getIndexFile(currentHeapFileName,
							params.tableTypes[i], params.tableStrSizes[i],
							params.joinColIndices[i]);
					addIndexFiles[i] = indexFile;
					FldSpec[] projection = new FldSpec[params.tableTypes[i].length];
					for (int j = 0; j < projection.length; j++) {
						projection[j] = new FldSpec(new RelSpec(RelSpec.outer), j + 1);
					}
					addTuples[i] = new FileScan(params.addHeapfiles[i].getFileName(),
							params.tableTypes[i], params.tableStrSizes[i],
							(short) params.tableTypes[i].length,
							(short) params.tableTypes[i].length, projection, null);
				}
			}

			String[] removeIndexFiles = new String[params.subHeapfiles.length];
			String[] removeHeapfiles = new String[params.subHeapfiles.length];
			Iterator[] removeTuples = new Iterator[params.subHeapfiles.length];
			for (int i = 0; i < params.subHeapfiles.length; i++) {
				if (params.subHeapfiles[i] != null) {
					String currentHeapFileName = params.subHeapfiles[i].getFileName();
					String indexFile = getIndexFile(currentHeapFileName,
							params.tableTypes[i], params.tableStrSizes[i],
							params.joinColIndices[i]);
					removeIndexFiles[i] = indexFile;
					removeHeapfiles[i] = params.subHeapfiles[i].getFileName();
					FldSpec[] projection = new FldSpec[params.tableTypes[i].length];
					for (int j = 0; j < projection.length; j++) {
						projection[j] = new FldSpec(new RelSpec(RelSpec.outer), j + 1);
					}
					removeTuples[i] = new FileScan(params.subHeapfiles[i].getFileName(),
							params.tableTypes[i], params.tableStrSizes[i],
							(short) params.tableTypes[i].length,
							(short) params.tableTypes[i].length, projection, null);
				}
			}

			Iterator[] am = new Iterator[params.numTables];
			String[] heapFiles = new String[params.numTables];
			for (int j = 0; j < am.length; j++) {
				try {
					FldSpec[] projection = new FldSpec[params.tableTypes[j].length];
					for (int k = 0; k < projection.length; k++) {
						projection[k] = new FldSpec(new RelSpec(RelSpec.outer),
								k + 1);
					}
					heapFiles[j] = params.heapfiles[j].getFileName();
					am[j] = new FileScan(params.heapfiles[j].getFileName(),
							params.tableTypes[j], params.tableStrSizes[j],
							(short) params.tableTypes[j].length,
							(short) params.tableTypes[j].length, projection, null);
				} catch (Exception k) {
					System.err
							.println("error creating file scan iterator for table: "
									+ j);
				}
			}

			for (int i = 0; i < heapFiles.length; i++) {
				Heapfile actualHeapfile = new Heapfile(heapFiles[i]);
				if (addHeapfiles[i] != null) {
					// add the tuples from the add heapfile to this heapfile
					Heapfile addHeapfile = new Heapfile(addHeapfiles[i]);
					Scan openScan = addHeapfile.openScan();
					RID rid = new RID();
					Tuple tuple = openScan.getNext(rid);
					while (tuple != null) {
						actualHeapfile.insertRecord(tuple.getTupleByteArray());
						tuple = openScan.getNext(rid);
					}
				}
				if (removeHeapfiles[i] != null) {
					// remove the tuples from the remove heapfile to this heapfile
					Heapfile removeHeapfile = new Heapfile(removeHeapfiles[i]);
					Scan openScan = removeHeapfile.openScan();
					RID rid = new RID();
					Tuple tuple = openScan.getNext(rid);
					while (tuple != null) {
						tuple.setHdr((short) params.tableTypes[i].length, params.tableTypes[i], params.tableStrSizes[i]);
						Scan scan = actualHeapfile.openScan();
						Tuple t2 = scan.getNext(rid);
						while (t2 != null) {
							t2.setHdr((short) params.tableTypes[i].length, params.tableTypes[i], params.tableStrSizes[i]);
							if (TupleUtils.Equal(tuple, t2, params.tableTypes[i], params.tableTypes[i].length)) {
								actualHeapfile.deleteRecord(rid);
							}
							t2 = scan.getNext(rid);
						}
						tuple = openScan.getNext(rid);
					}
				}
			}

			String[] indexFiles = new String[params.heapfiles.length];
			for (int k = 0; k < params.heapfiles.length; k++) {
				String currentHeapFileName = params.heapfiles[k].getFileName();
				String indexFile = getIndexFile(currentHeapFileName,
						params.tableTypes[k], params.tableStrSizes[k],
						params.joinColIndices[k]);
				indexFiles[k] = indexFile;
			}

			int[] tableSizes = new int[params.numTables];
			int totalSize = 0;
			for (int j = 0; j < tableSizes.length; j++) {
				tableSizes[j] = params.tableTypes[j].length;
				totalSize += tableSizes[j];
			}
			FldSpec[] projList = new FldSpec[totalSize];
			int count1 = 0;
			for (int j = 0; j < params.numTables; j++) {
				for (int k = 0; k < params.tableTypes[j].length; k++) {
					projList[count1++] = new FldSpec(j, k + 1);
				}
			}

			PCounter.initialize();
			TopTAJoin topTuplesIt = new TopTAJoin(tableSizes.length, params.tableTypes, tableSizes,
					params.tableStrSizes, params.joinColIndices, am, null, indexFiles, heapFiles, params.amountOfMemory,
					null, projList, projList.length, params.topK, _useIndicator);

//			List<Tuple> updateTopRankJoin = UpdateJoins.updateTopRankJoin(addTuples, removeTuples, addHeapfiles, removeHeapfiles,
//					params.tableTypes, params.tableStrSizes, params.topK, addIndexFiles, removeIndexFiles, params.amountOfMemory,
//					params.joinColIndices, topTuplesIt, heapFiles);
//			JTextArea textArea = printInWindow("FA Join (Incremental)");
//			int counter = 0;
//			for (Tuple tuple : updateTopRankJoin) {
//				textArea.append(++counter + " " + tuple.toString() + "\n");
//			}

			JTextArea textArea = printInWindow("TA Join (Incremental)");
			int counter = 0;
			Tuple topTuple = topTuplesIt.get_next();
			while (topTuple != null) {
				//if (!UpdateJoins.checkFromDeleteSet(removeIndexFiles, topTuple.getStrFld(1))) {
					String tupleString = topTuple.toString();
					textArea.append(++counter + " " + tupleString + "\n");
				//}
				topTuple = topTuplesIt.get_next();
			}

			//debug_Util("\nIteration count: " + topTuplesIt._iterationCount, style, doc);
			debug_Util("\nPage access count: " + PCounter.counter, style, doc);
		} else {
			String[] indexFiles = new String[params.heapfiles.length];
			String[] heapfileNames = new String[params.heapfiles.length];
			for (int k = 0; k < params.heapfiles.length; k++) {
				String currentHeapFileName = params.heapfiles[k].getFileName();
				heapfileNames[k] = currentHeapFileName;
				String indexFile = getIndexFile(currentHeapFileName,
						params.tableTypes[k], params.tableStrSizes[k],
						params.joinColIndices[k]);
				indexFiles[k] = indexFile;
			}
			int[] tableSizes = new int[params.numTables];
			int totalSize = 0;
			for (int j = 0; j < tableSizes.length; j++) {
				tableSizes[j] = params.tableTypes[j].length;
				totalSize += tableSizes[j];
			}
			FldSpec[] projList = new FldSpec[totalSize];
			int count1 = 0;
			for (int j = 0; j < params.numTables; j++) {
				for (int k = 0; k < params.tableTypes[j].length; k++) {
					projList[count1++] = new FldSpec(j, k + 1);
				}
			}
	
			Iterator[] am = new Iterator[params.numTables];
			for (int j = 0; j < am.length; j++) {
				try {
					FldSpec[] projection = new FldSpec[params.tableTypes[j].length];
					for (int k = 0; k < projection.length; k++) {
						projection[k] = new FldSpec(new RelSpec(RelSpec.outer),
								k + 1);
					}
					am[j] = new FileScan(params.heapfiles[j].getFileName(),
							params.tableTypes[j], params.tableStrSizes[j],
							(short) params.tableTypes[j].length,
							(short) params.tableTypes[j].length, projection, null);
				} catch (Exception k) {
					System.err
							.println("error creating file scan iterator for table: "
									+ j);
				}
			}

			PCounter.initialize();
			TopTAJoin topTAJoin = new TopTAJoin(params.numTables,
					params.tableTypes, tableSizes, params.tableStrSizes,
					params.joinColIndices, am, null, indexFiles, heapfileNames,
					params.amountOfMemory, null, projList, projList.length,
					params.topK, MainDriver._useIndicator);
	
			// print both
			JTextArea textArea = printInWindow("TA Join");
			int counter = 0;
			Tuple topTuple = topTAJoin.get_next();
			while (topTuple != null) {
				String tupleString = topTuple.toString();
				topTuple = topTAJoin.get_next();
				textArea.append(++counter + " " + tupleString + "\n");
			}
			//debug_Util("\nIteration count: " + topTAJoin._iterationCount, style, doc);
			debug_Util("\nPage access count: " + PCounter.counter, style, doc);
		}
	}

	public static void runNRA(int count, javax.swing.text.Style style,
			StyledDocument doc) throws JoinsException, IndexException,
			InvalidTupleSizeException, InvalidTypeException,
			PageNotReadException, TupleUtilsException, PredEvalException,
			LowMemException, UnknowAttrType, UnknownKeyTypeException, Exception {
		Parameters params = extractParameters(count);
		int addRemoveCount = 0;
		for(int i = 0; i < Utilities._maxFiles; i++) {
			if (Utilities._filesInDB_add[i] == 2 || Utilities._filesInDB_sub[i] == 2) {
				addRemoveCount++;
			}
		}

		if (!_ignore_increments && addRemoveCount > 0) {
			String[] addIndexFiles = new String[params.addHeapfiles.length];
			Iterator[] addTuples = new Iterator[params.addHeapfiles.length];
			String[] addHeapfiles = new String[params.addHeapfiles.length];
			for (int i = 0; i < params.addHeapfiles.length; i++) {
				if (params.addHeapfiles[i] != null) {
					String currentHeapFileName = params.addHeapfiles[i].getFileName();
					addHeapfiles[i] = params.addHeapfiles[i].getFileName();
					String indexFile = getIndexFile(currentHeapFileName,
							params.tableTypes[i], params.tableStrSizes[i],
							params.joinColIndices[i]);
					addIndexFiles[i] = indexFile;
					FldSpec[] projection = new FldSpec[params.tableTypes[i].length];
					for (int j = 0; j < projection.length; j++) {
						projection[j] = new FldSpec(new RelSpec(RelSpec.outer), j + 1);
					}
					addTuples[i] = new FileScan(params.addHeapfiles[i].getFileName(),
							params.tableTypes[i], params.tableStrSizes[i],
							(short) params.tableTypes[i].length,
							(short) params.tableTypes[i].length, projection, null);
				}
			}

			String[] removeIndexFiles = new String[params.subHeapfiles.length];
			String[] removeHeapfiles = new String[params.subHeapfiles.length];
			Iterator[] removeTuples = new Iterator[params.subHeapfiles.length];
			for (int i = 0; i < params.subHeapfiles.length; i++) {
				if (params.subHeapfiles[i] != null) {
					String currentHeapFileName = params.subHeapfiles[i].getFileName();
					String indexFile = getIndexFile(currentHeapFileName,
							params.tableTypes[i], params.tableStrSizes[i],
							params.joinColIndices[i]);
					removeIndexFiles[i] = indexFile;
					removeHeapfiles[i] = params.subHeapfiles[i].getFileName();
					FldSpec[] projection = new FldSpec[params.tableTypes[i].length];
					for (int j = 0; j < projection.length; j++) {
						projection[j] = new FldSpec(new RelSpec(RelSpec.outer), j + 1);
					}
					removeTuples[i] = new FileScan(params.subHeapfiles[i].getFileName(),
							params.tableTypes[i], params.tableStrSizes[i],
							(short) params.tableTypes[i].length,
							(short) params.tableTypes[i].length, projection, null);
				}
			}

			Iterator[] am = new Iterator[params.numTables];
			String[] heapFiles = new String[params.numTables];
			for (int j = 0; j < am.length; j++) {
				try {
					FldSpec[] projection = new FldSpec[params.tableTypes[j].length];
					for (int k = 0; k < projection.length; k++) {
						projection[k] = new FldSpec(new RelSpec(RelSpec.outer),
								k + 1);
					}
					heapFiles[j] = params.heapfiles[j].getFileName();
					am[j] = new FileScan(params.heapfiles[j].getFileName(),
							params.tableTypes[j], params.tableStrSizes[j],
							(short) params.tableTypes[j].length,
							(short) params.tableTypes[j].length, projection, null);
				} catch (Exception k) {
					System.err
							.println("error creating file scan iterator for table: "
									+ j);
				}
			}

			for (int i = 0; i < heapFiles.length; i++) {
				Heapfile actualHeapfile = new Heapfile(heapFiles[i]);
				if (addHeapfiles[i] != null) {
					// add the tuples from the add heapfile to this heapfile
					Heapfile addHeapfile = new Heapfile(addHeapfiles[i]);
					Scan openScan = addHeapfile.openScan();
					RID rid = new RID();
					Tuple tuple = openScan.getNext(rid);
					while (tuple != null) {
						actualHeapfile.insertRecord(tuple.getTupleByteArray());
						tuple = openScan.getNext(rid);
					}
				}
				if (removeHeapfiles[i] != null) {
					// remove the tuples from the remove heapfile to this heapfile
					Heapfile removeHeapfile = new Heapfile(removeHeapfiles[i]);
					Scan openScan = removeHeapfile.openScan();
					RID rid = new RID();
					Tuple tuple = openScan.getNext(rid);
					while (tuple != null) {
						tuple.setHdr((short) params.tableTypes[i].length, params.tableTypes[i], params.tableStrSizes[i]);
						Scan scan = actualHeapfile.openScan();
						Tuple t2 = scan.getNext(rid);
						while (t2 != null) {
							t2.setHdr((short) params.tableTypes[i].length, params.tableTypes[i], params.tableStrSizes[i]);
							if (TupleUtils.Equal(tuple, t2, params.tableTypes[i], params.tableTypes[i].length)) {
								actualHeapfile.deleteRecord(rid);
							}
							t2 = scan.getNext(rid);
						}
						tuple = openScan.getNext(rid);
					}
				}
			}


			int[] tableSizes = new int[params.numTables];
			int totalSize = 0;
			for (int j = 0; j < tableSizes.length; j++) {
				tableSizes[j] = params.tableTypes[j].length;
				totalSize += tableSizes[j];
			}
			FldSpec[] projList = new FldSpec[totalSize];
			int count1 = 0;
			for (int j = 0; j < params.numTables; j++) {
				for (int k = 0; k < params.tableTypes[j].length; k++) {
					projList[count1++] = new FldSpec(j, k + 1);
				}
			}

			PCounter.initialize();
			TopNRAJoin topTuplesIt = new TopNRAJoin(tableSizes.length, params.tableTypes, tableSizes,
					params.tableStrSizes, params.joinColIndices, am, params.amountOfMemory,
					null, projList, projList.length, params.topK, _useIndicator);

//			List<Tuple> updateTopRankJoin = UpdateJoins.updateTopRankJoin(addTuples, removeTuples, addHeapfiles, removeHeapfiles,
//					params.tableTypes, params.tableStrSizes, params.topK, addIndexFiles, removeIndexFiles, params.amountOfMemory,
//					params.joinColIndices, topTuplesIt, heapFiles);
//			JTextArea textArea = printInWindow("FA Join (Incremental)");
//			int counter = 0;
//			for (Tuple tuple : updateTopRankJoin) {
//				textArea.append(++counter + " " + tuple.toString() + "\n");
//			}

			JTextArea textArea = printInWindow("NRA Join (Incremental)");
			int counter = 0;
			Tuple topTuple = topTuplesIt.get_next();
			while (topTuple != null) {
				//if (!UpdateJoins.checkFromDeleteSet(removeIndexFiles, topTuple.getStrFld(1))) {
					String tupleString = topTuple.toString();
					textArea.append(++counter + " " + tupleString + "\n");
				//}
				topTuple = topTuplesIt.get_next();
			}

			//debug_Util("\nIteration count: " + topTuplesIt._iterationCount, style, doc);
			debug_Util("\nPage access count: " + PCounter.counter, style, doc);
		} else {
			int[] tableSizes = new int[params.numTables];
			int totalSize = 0;
			for (int j = 0; j < tableSizes.length; j++) {
				tableSizes[j] = params.tableTypes[j].length;
				totalSize += tableSizes[j];
			}
			FldSpec[] projList = new FldSpec[totalSize];
			int count1 = 0;
			for (int j = 0; j < params.numTables; j++) {
				for (int k = 0; k < params.tableTypes[j].length; k++) {
					projList[count1++] = new FldSpec(j, k + 1);
				}
			}
	
			Iterator[] am = new Iterator[params.numTables];
			for (int j = 0; j < am.length; j++) {
				try {
					FldSpec[] projection = new FldSpec[params.tableTypes[j].length];
					for (int k = 0; k < projection.length; k++) {
						projection[k] = new FldSpec(new RelSpec(RelSpec.outer),
								k + 1);
					}
					am[j] = new FileScan(params.heapfiles[j].getFileName(),
							params.tableTypes[j], params.tableStrSizes[j],
							(short) params.tableTypes[j].length,
							(short) params.tableTypes[j].length, projection, null);
				} catch (Exception k) {
					System.err
							.println("error creating file scan iterator for table: "
									+ j);
				}
			}
			PCounter.initialize();
			TopNRAJoin topNRAJoin = new TopNRAJoin(params.numTables,
					params.tableTypes, tableSizes, params.tableStrSizes,
					params.joinColIndices, am, params.amountOfMemory, null,
					projList, projList.length, params.topK, _useIndicator);
			// print both
			JTextArea textArea = printInWindow("NRA Join");
			int counter = 0;
			Tuple topTuple = topNRAJoin.get_next();
			while (topTuple != null) {
				String tupleString = topTuple.toString();
				topTuple = topNRAJoin.get_next();
				textArea.append(++counter + " " + tupleString + "\n");
			}
			//debug_Util("\nIteration count: " + topNRAJoin._iterationCount, style, doc);
			debug_Util("\nPage access count: " + PCounter.counter, style, doc);
		}
	}


	private static String getIndexFile(String heapfile, AttrType[] types,
			short[] strSizes, int joinColIndex) {
		// indexfile manager,
		// checks for one time creation of indexfiles
		String indexFileName = heapfile + "_" + joinColIndex + ".inx";
		if (!_indexFilesCounter.containsKey(heapfile)
				|| !_indexFilesCounter.get(heapfile).contains(joinColIndex)) {
			try {
				createIndexFile(heapfile, types, strSizes, indexFileName,
						joinColIndex);
				if (_indexFilesCounter.get(heapfile) != null) {
					_indexFilesCounter.get(heapfile).add(joinColIndex);
				} else {
					ArrayList<Integer> joinCols = new ArrayList<Integer>();
					joinCols.add(joinColIndex);
					_indexFilesCounter.put(heapfile, joinCols);
				}
			} catch (Exception e) {
				System.err.println("Error creating indexfile for heapfile: "
						+ heapfile);
				return null;
			}
		}
		return indexFileName;
	}

	private static void createIndexFile(String heapFileName, AttrType[] types,
			short[] strSizes, String indexFileName, int joinColIndex)
			throws HFException, HFBufMgrException, HFDiskMgrException,
			IOException, InvalidTupleSizeException, InvalidTypeException,
			FieldNumberOutOfBoundException, PageUnpinnedException,
			InvalidFrameNumberException, HashEntryNotFoundException,
			ReplacerException {
		short[] strSizesIndexed = new short[types.length];
		int count = 0;
		for (int j = 0; j < strSizesIndexed.length; j++) {
			if (types[j].attrType == AttrType.attrString) {
				strSizesIndexed[j] = strSizes[count++];
			} else {
				strSizesIndexed[j] = -1;
			}
		}
		Heapfile heapfile = new Heapfile(heapFileName);
		Scan scan = heapfile.openScan();
		BTreeFile btf = null;
		try {
			if (types[joinColIndex].attrType == AttrType.attrInteger) {
				btf = new BTreeFile(indexFileName, AttrType.attrInteger, 4, 1);
			} else if (types[joinColIndex].attrType == AttrType.attrString) {
				btf = new BTreeFile(indexFileName, AttrType.attrString,
						strSizesIndexed[joinColIndex], 1);
			} else {
				// unsupported index type
				throw new IllegalStateException(
						"Unsupported column type for BtreeIndex");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error creating BtreeIndex for heapfile: "
					+ heapFileName);
			Runtime.getRuntime().exit(1);
		}
		RID rid = new RID();
		Tuple temp = null;
		Tuple t = new Tuple();
		t.setHdr((short) types.length, types, strSizes);
		try {
			temp = scan.getNext(rid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		while (temp != null) {
			t.tupleCopy(temp);
			try {
				if (types[joinColIndex].attrType == AttrType.attrInteger) {
					int key = t.getIntFld(joinColIndex + 1);
					btf.insert(new IntegerKey(key), rid);
				} else if (types[joinColIndex].attrType == AttrType.attrString) {
					String key = t.getStrFld(joinColIndex + 1);
					btf.insert(new StringKey(key), rid);
				} else {
					// unsupported index type
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.err
						.println("Error inserting record into BTreeIndex for heapfile: "
								+ heapFileName);
			}

			try {
				temp = scan.getNext(rid);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		scan.closescan();
		btf.close();
	}

	private static CondExpr[] readCondExpr() {
		CondExpr[] filter = null;
		System.out.print("Enter no. of conditions: ");
		int filterCount = readInt();
		if (filterCount > 0) {
			filter = new CondExpr[filterCount + 1];
			filter[filterCount] = null;
		}
		for (int i = 0; i < filterCount; i++) {
			System.out.println("Values for condition " + (i + 1) + ":");
			System.out.print("\tEnter LHS table index (0-" + (_tableCount - 1)
					+ "): ");
			int outerTable = readInt();
			System.out.print("\tEnter LHS table column offset (1-"
					+ _tableTypes[outerTable].length + "): ");
			int outerOffset = readInt();
			System.out
					.print("\tEnter condition (0-EQ, 1-LT, 2-GT, 3-NE, 4-LE, 5-GE): ");
			int operator = readInt();
			System.out
					.print("\tEnter RHS type (0-Table column, 1-String Value, 2-Int Value, 3-Real Value): ");
			int innerType = readInt();
			filter[i] = new CondExpr();
			filter[i].type1 = new AttrType(AttrType.attrSymbol);
			filter[i].op = new AttrOperator(operator);
			filter[i].operand1 = new Operand();
			filter[i].operand1.symbol = new FldSpec(outerTable, outerOffset);
			filter[i].operand2 = new Operand();
			switch (innerType) {
			case 0:
				System.out.print("\tEnter RHS table index (0-"
						+ (_tableCount - 1) + "): ");
				int innerTable = readInt();
				System.out.print("\tEnter RHS table column offset (1-"
						+ _tableTypes[innerTable].length + "): ");
				int innerOffset = readInt();
				filter[i].type2 = new AttrType(AttrType.attrSymbol);
				filter[i].operand2.symbol = new FldSpec(innerTable, innerOffset);
				break;
			case 1:
				System.out.print("\tEnter RHS string value: ");
				String stringValue = readString();
				filter[i].type2 = new AttrType(AttrType.attrString);
				filter[i].operand2.string = stringValue;
				break;
			case 2:
				System.out.print("\tEnter RHS int value: ");
				int intValue = readInt();
				filter[i].type2 = new AttrType(AttrType.attrInteger);
				filter[i].operand2.integer = intValue;
				break;
			case 3:
				System.out.print("\tEnter RHS float value: ");
				float floatValue = readFloat();
				filter[i].type2 = new AttrType(AttrType.attrReal);
				filter[i].operand2.real = floatValue;
				break;
			default:
				break;
			}
		}
		return filter;
	}

	private static void runDuplicateElimination() throws DuplElimException,
			IOException {
		System.out.print("Choose table index [0-" + (_tableCount - 1) + "]: ");
		int tableIndex = readInt();
		FileScan fs = null;
		try {
			FldSpec[] projection = new FldSpec[_tableTypes[tableIndex].length];
			for (int i = 0; i < projection.length; i++) {
				projection[i] = new FldSpec(new RelSpec(RelSpec.outer), i + 1);
			}
			fs = new FileScan(_heapfiles[tableIndex].getFileName(),
					_tableTypes[tableIndex], _tableStrSizes[tableIndex],
					(short) _tableTypes[tableIndex].length,
					(short) _tableTypes[tableIndex].length, projection, null);
		} catch (Exception e) {
			System.err.println("error creating file scan iterator for table: "
					+ tableIndex);
			e.printStackTrace();
			return;
		}
		DuplElim deIt = new DuplElim(_tableTypes[tableIndex],
				(short) _tableTypes[tableIndex].length,
				_tableStrSizes[tableIndex], fs, _amountOfMemory, false);
		System.out.print("Print table (1/0)? ");
		int input = readInt();
		if (input == 0) {
			// print count
			int recordCount = countIterator(deIt, _tableTypes[tableIndex]);
			System.out.println("Old record count: " + _recordCount[tableIndex]);
			System.out.println("New record count: " + recordCount);
		} else {
			// print both
			int recordCount = printIterator(deIt, _tableTypes[tableIndex]);
			System.out.println("Old record count: " + _recordCount[tableIndex]);
			System.out.println("New record count: " + recordCount);
		}
	}

	private static void runTopNestedLoopsJoin() throws DuplElimException,
			IOException, NestedLoopException, TupleUtilsException,
			SortException {
		System.out.println("Note 1: Duplicate Eliminaion is implicitly done.\n"
				+ "Note 2: Equi-join is assumed.");
		System.out.print("Choose outer table index [0-" + (_tableCount - 1)
				+ "]: ");
		int table1Index = readInt();
		System.out.print("Choose inner table index [0-" + (_tableCount - 1)
				+ "]: ");
		int table2Index = readInt();
		System.out.print("Enter join column offset for outer table(1-"
				+ _tableTypes[table1Index].length + "): ");
		int table1Offset = readInt();
		System.out.print("Enter join column offset for inner table(1-"
				+ _tableTypes[table2Index].length + "): ");
		int table2Offset = readInt();
		// System.out.print("Enter value for N: ");
		// int readCount = readInt();

		FileScan fs1 = null;
		try {
			FldSpec[] projection = new FldSpec[_tableTypes[table1Index].length];
			for (int i = 0; i < projection.length; i++) {
				projection[i] = new FldSpec(new RelSpec(RelSpec.outer), i + 1);
			}
			fs1 = new FileScan(_heapfiles[table1Index].getFileName(),
					_tableTypes[table1Index], _tableStrSizes[table1Index],
					(short) _tableTypes[table1Index].length,
					(short) _tableTypes[table1Index].length, projection, null);
		} catch (Exception e) {
			System.err.println("error creating file scan iterator for table: "
					+ table1Index);
			e.printStackTrace();
			return;
		}
		DuplElim deIt1 = new DuplElim(_tableTypes[table1Index],
				(short) _tableTypes[table1Index].length,
				_tableStrSizes[table1Index], fs1, _amountOfMemory, false);
		FileScan fs2 = null;
		try {
			FldSpec[] projection = new FldSpec[_tableTypes[table2Index].length];
			for (int i = 0; i < projection.length; i++) {
				projection[i] = new FldSpec(new RelSpec(RelSpec.outer), i + 1);
			}
			fs2 = new FileScan(_heapfiles[table2Index].getFileName(),
					_tableTypes[table2Index], _tableStrSizes[table2Index],
					(short) _tableTypes[table2Index].length,
					(short) _tableTypes[table2Index].length, projection, null);
		} catch (Exception e) {
			System.err.println("error creating file scan iterator for table: "
					+ table2Index);
			e.printStackTrace();
			return;
		}
		DuplElim deIt2 = new DuplElim(_tableTypes[table2Index],
				(short) _tableTypes[table2Index].length,
				_tableStrSizes[table2Index], fs2, _amountOfMemory, false);
		Heapfile f = null;
		try {
			f = new Heapfile(_heapfiles[table2Index].getFileName() + ".dehf");
		} catch (Exception e) {
			System.err.println("error creating heapfile for r2de");
			e.printStackTrace();
			return;
		}
		Tuple t = null;
		try {
			while ((t = deIt2.get_next()) != null) {
				f.insertRecord(t.getTupleByteArray());
			}
		} catch (Exception e) {
			System.err.println("error writing to the r2de heap file");
			e.printStackTrace();
		}

		FldSpec[] projList = new FldSpec[_tableTypes[table1Index].length
				+ _tableTypes[table2Index].length - 1];
		for (int i = 0; i < projList.length; i++) {
			if (i < _tableTypes[table1Index].length) {
				projList[i] = new FldSpec(new RelSpec(RelSpec.outer), i + 1);
			} else {
				if (i - _tableTypes[table1Index].length + 1 >= table2Offset) {
					projList[i] = new FldSpec(new RelSpec(RelSpec.innerRel), i
							- _tableTypes[table1Index].length + 2);
				} else {
					projList[i] = new FldSpec(new RelSpec(RelSpec.innerRel), i
							- _tableTypes[table1Index].length + 1);
				}

			}
		}
		CondExpr[] outFilter = new CondExpr[2];
		outFilter[1] = null;
		outFilter[0] = new CondExpr();
		outFilter[0].op = new AttrOperator(AttrOperator.aopEQ);
		outFilter[0].type1 = new AttrType(AttrType.attrSymbol);
		outFilter[0].type2 = new AttrType(AttrType.attrSymbol);
		outFilter[0].operand1 = new Operand();
		outFilter[0].operand1.symbol = new FldSpec(new RelSpec(RelSpec.outer),
				table1Offset);
		outFilter[0].operand2 = new Operand();
		outFilter[0].operand2.symbol = new FldSpec(
				new RelSpec(RelSpec.innerRel), table2Offset);
		TopNestedLoopsJoin joinIt = new TopNestedLoopsJoin(
				_tableTypes[table1Index], _tableTypes[table1Index].length,
				_tableStrSizes[table1Index], _tableTypes[table2Index],
				_tableTypes[table2Index].length, _tableStrSizes[table2Index],
				_amountOfMemory, deIt1, _heapfiles[table2Index].getFileName()
						+ ".dehf", outFilter, null, projList, projList.length);

		int outFldCount = projList.length;
		AttrType[] outAttrTypes = new AttrType[outFldCount];
		short[] outStrSizes = TupleUtils.getOPAttrInfo(outAttrTypes,
				_tableTypes[table1Index], _tableTypes[table1Index].length,
				_tableTypes[table2Index], _tableTypes[table2Index].length,
				_tableStrSizes[table1Index], _tableStrSizes[table2Index],
				projList, outFldCount);
		System.out.print("Print joined table (1/0)? ");
		int input = readInt();
		if (input == 0) {
			// print count
			int recordCount = countIterator(joinIt, outAttrTypes);
			System.out.println("Outer table record count: "
					+ _recordCount[table1Index]);
			System.out.println("Inner table record count: "
					+ _recordCount[table2Index]);
			System.out.println("Joined table record count: " + recordCount);
		} else {
			// print both
			// Tuple temp = null;
			// int count = 0;
			// try {
			// while ((temp = joinIt.get_next()) != null) {
			// // t.print(attrTypes);
			// // System.out.println(t.getScore());
			// System.out.println(temp.toString());
			// count++;
			// if (count == readCount) {
			// break;
			// }
			// }
			// } catch (Exception e) {
			// System.err.println("error printing iterator");
			// e.printStackTrace();
			// }
			int recordCount = printIterator(joinIt, outAttrTypes);
			System.out.println("Outer table record count: "
					+ _recordCount[table1Index]);
			System.out.println("Inner table record count: "
					+ _recordCount[table2Index]);
			System.out.println("Joined table record count: " + recordCount);
		}
	}

	private static void runTopSortMergeJoin() throws NestedLoopException,
			TupleUtilsException, SortException, IOException, DuplElimException,
			JoinNewFailed, JoinLowMemory {
		System.out.println("Note 1: Duplicate Eliminaion is implicitly done.\n"
				+ "Note 2: Equi-join is assumed.");
		System.out.print("Choose outer table index [0-" + (_tableCount - 1)
				+ "]: ");
		int table1Index = readInt();
		System.out.print("Choose inner table index [0-" + (_tableCount - 1)
				+ "]: ");
		int table2Index = readInt();
		System.out.print("Enter join column offset for outer table(1-"
				+ _tableTypes[table1Index].length + "): ");
		int table1Offset = readInt();
		System.out.print("Enter join column offset for inner table(1-"
				+ _tableTypes[table2Index].length + "): ");
		int table2Offset = readInt();
		// System.out.print("Enter value for N: ");
		// int readCount = readInt();

		FileScan fs1 = null;
		try {
			FldSpec[] projection = new FldSpec[_tableTypes[table1Index].length];
			for (int i = 0; i < projection.length; i++) {
				projection[i] = new FldSpec(new RelSpec(RelSpec.outer), i + 1);
			}
			fs1 = new FileScan(_heapfiles[table1Index].getFileName(),
					_tableTypes[table1Index], _tableStrSizes[table1Index],
					(short) _tableTypes[table1Index].length,
					(short) _tableTypes[table1Index].length, projection, null);
		} catch (Exception e) {
			System.err.println("error creating file scan iterator for table: "
					+ table1Index);
			e.printStackTrace();
			return;
		}
		DuplElim deIt1 = new DuplElim(_tableTypes[table1Index],
				(short) _tableTypes[table1Index].length,
				_tableStrSizes[table1Index], fs1, _amountOfMemory, false);
		FileScan fs2 = null;
		try {
			FldSpec[] projection = new FldSpec[_tableTypes[table2Index].length];
			for (int i = 0; i < projection.length; i++) {
				projection[i] = new FldSpec(new RelSpec(RelSpec.outer), i + 1);
			}
			fs2 = new FileScan(_heapfiles[table2Index].getFileName(),
					_tableTypes[table2Index], _tableStrSizes[table2Index],
					(short) _tableTypes[table2Index].length,
					(short) _tableTypes[table2Index].length, projection, null);
		} catch (Exception e) {
			System.err.println("error creating file scan iterator for table: "
					+ table2Index);
			e.printStackTrace();
			return;
		}
		DuplElim deIt2 = new DuplElim(_tableTypes[table2Index],
				(short) _tableTypes[table2Index].length,
				_tableStrSizes[table2Index], fs2, _amountOfMemory, false);

		FldSpec[] projList = new FldSpec[_tableTypes[table1Index].length
				+ _tableTypes[table2Index].length - 1];
		for (int i = 0; i < projList.length; i++) {
			if (i < _tableTypes[table1Index].length) {
				projList[i] = new FldSpec(new RelSpec(RelSpec.outer), i + 1);
			} else {
				if (i - _tableTypes[table1Index].length + 1 >= table2Offset) {
					projList[i] = new FldSpec(new RelSpec(RelSpec.innerRel), i
							- _tableTypes[table1Index].length + 2);
				} else {
					projList[i] = new FldSpec(new RelSpec(RelSpec.innerRel), i
							- _tableTypes[table1Index].length + 1);
				}

			}
		}
		CondExpr[] outFilter = new CondExpr[2];
		outFilter[1] = null;
		outFilter[0] = new CondExpr();
		outFilter[0].op = new AttrOperator(AttrOperator.aopEQ);
		outFilter[0].type1 = new AttrType(AttrType.attrSymbol);
		outFilter[0].type2 = new AttrType(AttrType.attrSymbol);
		outFilter[0].operand1 = new Operand();
		outFilter[0].operand1.symbol = new FldSpec(new RelSpec(RelSpec.outer),
				table1Offset);
		outFilter[0].operand2 = new Operand();
		outFilter[0].operand2.symbol = new FldSpec(
				new RelSpec(RelSpec.innerRel), table2Offset);

		int table1FSize = 0;
		int table2FSize = 0;
		int count = 0;
		short[] table1StrSizesIndex = new short[_tableTypes[table1Index].length];
		for (int i = 0; i < table1StrSizesIndex.length; i++) {
			if (_tableTypes[table1Index][i].attrType == AttrType.attrString) {
				table1StrSizesIndex[i] = _tableStrSizes[table1Index][count++];
			}
		}
		switch (_tableTypes[table1Index][table1Offset - 1].attrType) {
		case AttrType.attrString:
			table1FSize = table1StrSizesIndex[table1Offset - 1];
			break;
		case AttrType.attrInteger:
			table1FSize = 4;
			break;
		case AttrType.attrReal:
			table1FSize = 4;
			break;
		default:
			break;
		}
		count = 0;
		short[] table2StrSizesIndex = new short[_tableTypes[table2Index].length];
		for (int i = 0; i < table2StrSizesIndex.length; i++) {
			if (_tableTypes[table2Index][i].attrType == AttrType.attrString) {
				table2StrSizesIndex[i] = _tableStrSizes[table2Index][count++];
			}
		}
		switch (_tableTypes[table2Index][table2Offset - 1].attrType) {
		case AttrType.attrString:
			table2FSize = table2StrSizesIndex[table2Offset - 1];
			break;
		case AttrType.attrInteger:
			table1FSize = 4;
			break;
		case AttrType.attrReal:
			table1FSize = 4;
			break;
		default:
			break;
		}

		TopSortMergeJoin joinIt = new TopSortMergeJoin(
				_tableTypes[table1Index], _tableTypes[table1Index].length,
				_tableStrSizes[table1Index], _tableTypes[table2Index],
				_tableTypes[table2Index].length, _tableStrSizes[table2Index],
				table1Offset, table1FSize, table2Offset, table2FSize,
				_amountOfMemory, deIt1, deIt2, false, false, new TupleOrder(
						TupleOrder.Descending), outFilter, projList,
				projList.length);

		int outFldCount = projList.length;
		AttrType[] outAttrTypes = new AttrType[outFldCount];
		short[] outStrSizes = TupleUtils.getOPAttrInfo(outAttrTypes,
				_tableTypes[table1Index], _tableTypes[table1Index].length,
				_tableTypes[table2Index], _tableTypes[table2Index].length,
				_tableStrSizes[table1Index], _tableStrSizes[table2Index],
				projList, outFldCount);
		System.out.print("Print joined table (1/0)? ");
		int input = readInt();
		if (input == 0) {
			// print count
			int recordCount = countIterator(joinIt, outAttrTypes);
			System.out.println("Outer table record count: "
					+ _recordCount[table1Index]);
			System.out.println("Inner table record count: "
					+ _recordCount[table2Index]);
			System.out.println("Joined table record count: " + recordCount);
		} else {
			// print both
			// Tuple temp = null;
			// int recordCount = 0;
			// try {
			// while ((temp = joinIt.get_next()) != null) {
			// // t.print(attrTypes);
			// // System.out.println(t.getScore());
			// System.out.println(temp.toString());
			// recordCount++;
			// if (recordCount == readCount) {
			// break;
			// }
			// }
			// } catch (Exception e) {
			// System.err.println("error printing iterator");
			// e.printStackTrace();
			// }
			int recordCount = printIterator(joinIt, outAttrTypes);
			System.out.println("Outer table record count: "
					+ _recordCount[table1Index]);
			System.out.println("Inner table record count: "
					+ _recordCount[table2Index]);
			System.out.println("Joined table record count: " + recordCount);
		}
	}

	private static int readInt() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		int theInt = -1;
		try {
			theInt = Integer.parseInt(in.readLine());
		} catch (IOException e) {
			System.err.println("error reading choice from user");
			return -1;
		}
		return theInt;
	}

	private static String readString() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String theString = "";
		try {
			theString = in.readLine();
		} catch (IOException e) {
			System.err.println("error reading choice from user");
			return "";
		}
		return theString;
	}

	private static Float readFloat() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		float theFloat = 0;
		try {
			theFloat = Float.parseFloat(in.readLine());
		} catch (IOException e) {
			System.err.println("error reading choice from user");
		}
		return theFloat;
	}

	public static JTextArea printInWindow(String windowTitle) {
		JTextArea fileContent = new JTextArea(10, 50);
		try {
			fileContent.setEditable(false);
			fileContent.setLineWrap(false);
			fileContent.setAutoscrolls(true);
		} catch (Exception ee) {
			StringWriter sw = new StringWriter();
			ee.printStackTrace(new PrintWriter(sw));
		}

		JScrollPane scrollPane = new JScrollPane(fileContent);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(10, 10, 400, 400);
		scrollPane.setBorder(BorderFactory.createBevelBorder(2));

		JFrame f = new JFrame(windowTitle);

		f.getContentPane().setBackground(Color.magenta);
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		f.getContentPane().add(scrollPane);
		f.pack();

		f.setVisible(true);
		return fileContent;
	}

	public static String ViewFilesInDB(ActionEvent e,
			javax.swing.text.Style style, StyledDocument doc) {
		String out = " ";
		// debug_Util_color("\n* inside viewfiles in DB before action command",
		// style, doc, Color.green);
		if (e.getActionCommand().equals("VIEWFILEINDB")) {
			// debug_Util_color("\n* inside viewfiles in DB after action command",
			// style, doc, Color.green);

			for (int i = 0; i < Utilities._maxFiles; i++) {
				if (Utilities._filesToView[i] == 1)

				{
					int recordCount = 0;
					JTextArea fileContent = new JTextArea(10, 50);

					String row = null;
					debug_Util_color("\n,scan created", style, doc, Color.green);
					Scan scan = null;
					// FileScan fileScan = new FileScan();
					try {

						fileContent.setEditable(false);
						fileContent.setLineWrap(false);
						fileContent.setAutoscrolls(true);

						scan = Utilities.p3_csvFiles[i].getHF()
								.openScan();
						debug_Util_color("\n,scan opened", style, doc,
								Color.green);
					} catch (Exception ee) {
						StringWriter sw = new StringWriter();
						ee.printStackTrace(new PrintWriter(sw));
						debug_Util_color(
								"error opening scan on HF of file index:" + i
										+ "\n" + sw.toString(), style, doc,
								Color.red);
					}

					int llen, ii = 0;

					int tupleSize = Utilities.p3_csvFiles[i]
							.getTupleSize();
					//DummyRecord rec = new DummyRecord(tupleSize);
					RID rid = new RID();
					boolean done = false;
					int countx = 0;
					while (!done) {
						try {
							Tuple tuple = new Tuple();
							tuple = scan.getNext(rid);
							if (tuple == null) {
								done = true;
							} else {
								tuple.setHdr(
										(short) Utilities.p3_csvFiles[i].cType_db_algos.length,
										Utilities.p3_csvFiles[i].cType_db_algos,
										Utilities.p3_csvFiles[i].cSize_strs_Tuple);
								// debug_Util_color("\n scanned: " +
								// tuple.toString() + " " + "*" , style, doc,
								// Color.green);
								row = tuple.toString();

								fileContent.append("\n" + recordCount++ + "\t");
								fileContent.append(row + "\t");

							}

						} catch (Exception ee) {
							done = true;
							StringWriter sw = new StringWriter();
							ee.printStackTrace(new PrintWriter(sw));
							debug_Util_color(
									"\nerror while scanning, ie reading tuples:\n"
											+ sw.toString(), style, doc,
									Color.red);

						}

					}
					scan.closescan();

					JScrollPane scrollPane = new JScrollPane(fileContent);
					scrollPane
							.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
					scrollPane.setBounds(10, 10, 400, 400);
					scrollPane.setBorder(BorderFactory.createBevelBorder(2));

					JFrame f = new JFrame(
							Utilities.p3_csvFiles[i].getFileName());

					f.getContentPane().setBackground(Color.magenta);
					f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					f.getContentPane().add(scrollPane);
					f.pack();

					f.setVisible(true);
					debug_Util_color("\n displayed:"
							+ Utilities.p3_csvFiles[i].getFileName(),
							style, doc, Color.magenta);

					// ////
					if (Utilities._filesInDB_add[i] == 2) {
						// print both
						JTextArea textArea = printInWindow( Utilities.p3_csvFiles_add[i].getFileName());
						String row1 = null;
						debug_Util_color("\n,scan created for Add", style, doc,
								Color.green);
						Scan scan1 = null;
						// FileScan fileScan = new FileScan();
						try {

							scan1 = Utilities.p3_csvFiles_add[i].getHF()
									.openScan();
							debug_Util_color("\n,scan opened for Add", style,
									doc, Color.green);
						} catch (Exception ee1) {
							StringWriter sw = new StringWriter();
							ee1.printStackTrace(new PrintWriter(sw));
							debug_Util_color(
									"error opening scan on HF of Add file index:"
											+ i + "\n" + sw.toString(), style,
									doc, Color.red);
						}

						int llen1, ii1 = 0;

						int tupleSize1 = Utilities.p3_csvFiles_add[i]
								.getTupleSize();
						//DummyRecord rec1 = new DummyRecord(tupleSize1);
						RID rid1 = new RID();
						boolean done1 = false;
						int countx1 = 0;
						while (!done1) {
							try {
								Tuple tuple = new Tuple();
								tuple = scan1.getNext(rid1);
								if (tuple == null) {
									done1 = true;
								} else {
									tuple.setHdr(
											(short) Utilities.p3_csvFiles_add[i].cType_db_algos.length,
											Utilities.p3_csvFiles_add[i].cType_db_algos,
											Utilities.p3_csvFiles_add[i].cSize_strs_Tuple);
									// debug_Util_color("\n scanned: " +
									// tuple.toString() + " " + "*" , style,
									// doc, Color.green);
									row1 = tuple.toString();
									textArea.append(++countx1 + " " + row1
											+ "\n");
									// fileContent.append("\n" + recordCount++ +
									// "\t");
									// fileContent.append(row + "\t");

								}

							} catch (Exception ee) {
								done = true;
								StringWriter sw = new StringWriter();
								ee.printStackTrace(new PrintWriter(sw));
								debug_Util_color(
										"\nerror while scanning, ie reading tuples:\n"
												+ sw.toString(), style, doc,
										Color.red);

							}
						}// end while
						scan1.closescan();
						
						debug_Util_color("\n displayed:"
								+ Utilities.p3_csvFiles_add[i].getFileName(),
								style, doc, Color.magenta);


						}//end checking to print add files in db
					
					
					// start checking to print sub files
					
					if (Utilities._filesInDB_sub[i] == 2) {
						// print both
						JTextArea textArea = printInWindow( Utilities.p3_csvFiles_sub[i].getFileName());
						String row1 = null;
						debug_Util_color("\n,scan created for sub", style, doc,
								Color.green);
						Scan scan12 = null;
						// FileScan fileScan = new FileScan();
						try {

							scan12 = Utilities.p3_csvFiles_sub[i].getHF()
									.openScan();
							
							debug_Util_color("\n,scan opened for sub :" +Utilities.p3_csvFiles_sub[i].getHFName() , style,
									doc, Color.green);
						} catch (Exception ee1) {
							StringWriter sw = new StringWriter();
							ee1.printStackTrace(new PrintWriter(sw));
							debug_Util_color(
									"error opening scan on HF of sub file index:"
											+ i + "\n" + sw.toString(), style,
									doc, Color.red);
						}

						int llen1, ii1 = 0;

						int tupleSize1 = Utilities.p3_csvFiles_sub[i]
								.getTupleSize();
						//DummyRecord rec1 = new DummyRecord(tupleSize1);
						RID rid1 = new RID();
						boolean done1 = false;
						int countx1 = 0;
						
						//debug_Util_color("\ncheck 1outside while  sub file index:", style,doc, Color.red);
						while (!done1) {
							//debug_Util_color("\n insede while :", style,doc, Color.red);	
							
							try {
								Tuple tuple = new Tuple();
								tuple = scan12.getNext(rid1);
								
								//debug_Util_color("\n insede try :", style,doc, Color.red);	
								if (tuple == null) {
									//debug_Util_color("\ninsede tuple == null :", style,doc, Color.red);	
									done1 = true;
								} else {
									tuple.setHdr(
											(short) Utilities.p3_csvFiles_sub[i].cType_db_algos.length,
											Utilities.p3_csvFiles_sub[i].cType_db_algos,
											Utilities.p3_csvFiles_sub[i].cSize_strs_Tuple);
									// debug_Util_color("\n scanned: " +
									// tuple.toString() + " " + "*" , style,
									// doc, Color.green);
									row1 = tuple.toString();
									textArea.append(++countx1 + row1	+ "\n");									
									//debug_Util_color(" insede row value" + row1, style,doc, Color.red);	
									// fileContent.append("\n" + recordCount++ +
									// "\t");
									// fileContent.append(row + "\t");

								}

							} catch (Exception ee) {
								done = true;
								StringWriter sw = new StringWriter();
								ee.printStackTrace(new PrintWriter(sw));
								debug_Util_color(
										"\nerror while scanning, ie reading tuples:\n"
												+ sw.toString(), style, doc,
										Color.red);

							}
						}// end while
						scan12.closescan();
						//debug_Util_color(" done print suble :", style,doc, Color.red);	
						// /////////////////
						debug_Util_color("\n displayed:" + Utilities.p3_csvFiles_sub[i].getFileName(),
								style, doc, Color.magenta);

					}//// end checking to print sub files in db
					
				}
				// i = _maxFiles + 1;// move after finish testing
			}

			out = "above heape file/s opened in new window/s on desktop for viewing\n";
			out = out + "you can close new window/s at will";
		}

		return out;
	}

	public static String add_NewRecordToTableInDB(ActionEvent e,
			javax.swing.text.Style style, StyledDocument doc, int tableChosen) {
		String out = null;

		// p3_Utilities_dmi.debug_Util_color("\ndebug a1 ", style, doc,
		// Color.red);
		// if (e.getActionCommand().equals("ADDFILETODB")) {
		if (e.getActionCommand().equals("ADD")) {

			int len = Utilities._filesToAdd.length;

			// p3_Utilities_dmi.debug_Util_color("\ndebug a1 ", style, doc,
			// Color.red);
			for (int i = 0; i < len; i++) {
				int value = Utilities._filesToAdd[i];
				// /////////////

				if (value == 1) {
					out = "";
					String inner = "";
					String insertedRows = "";

					// p3_Utilities_dmi.debug_Util_color("\ndebug a2  " + i,
					// style, doc, Color.red);

					String tblName = Utilities.p3_csvFiles_add[i]
							.getFileName().toString();
					String fileLoc = Utilities.p3_csvFiles_add[i]
							.getFileLocation();
					Utilities.debug_Util_color("\nadding file index: "
							+ i + ", " + tblName, style, doc, Color.red);
					// /////////////
					String[] row = null;
					CSVReader csvReader = null;
					int totRows = 0;
					int totCols = 0;
					int rowNum = 0;
					int total_str_cols = 0;

					totRows = Utilities.p3_csvFiles_add[i]
							.getTotalRows();
					try {
						csvReader = new CSVReader(new FileReader(new File(
								fileLoc)));
						// totCols = p3_Utilities_dmi. _columnsTotal;
						// totCols =
						// p3_Utilities_dmi.p3_csvFiles[tableChosen].getColCount();
						// total_str_cols =
						// p3_Utilities_dmi.p3_csvFiles[tableChosen].getTotal_str_cols();
						// inner = inner + ", opened with " + totRows;
						// p3_Utilities_dmi.debug_Util_color(inner , style, doc,
						// Color.red);

						totCols = Utilities._columnsTotal;
						total_str_cols = Utilities._total_str_cols;
						inner = inner + ", opened with " + totRows + " record/s \n";
						Utilities.debug_Util_color(inner, style, doc,Color.red);
						Utilities.debug_Util_color("before " +
								Utilities.p3_csvFiles[i].getHF().getRecCnt() + " records: " +
								Utilities.p3_csvFiles[i].getHFName() 								
								, style, doc,
								Color.red);
						
						// p3_Utilities_dmi.debug_Util_color("\ndebug a3  " + i
						// + tblName, style, doc, Color.red);
						
						
						/*
						Scan openScan1 = p3_Utilities_dmi.p3_csvFiles_add[i].getHF().openScan();
						int recCount;
                        RID rid1 = new RID();
                        Tuple tuple1 =  new Tuple();
                        int iix =0;
                       	try {
								
								recCount= p3_Utilities_dmi.p3_csvFiles_add[i].getHF().getRecCnt();
								
								debug_Util_color("\n recCount " + recCount + " ", style, doc,
										Color.red);
								for (iix = 0; iix < recCount; iix++)
								{tuple1 = openScan1.getNext(rid1);
								p3_Utilities_dmi.p3_csvFiles_add[i].getHF().deleteRecord(rid1);
								}
								
							} catch (Exception e6) {
								StringWriter sw = new StringWriter();
								e6.printStackTrace(new PrintWriter(sw));
								debug_Util_color("\n end deleting earlier added file " +iix + " " + sw.toString(), style, doc,
										Color.red);
						

							} */
                       
						
						

						for (int k = 0; k < totRows; k++) {
							row = csvReader.readNext();
							rowNum = k + 1;
							// AttrType[] cType_db = new AttrType[totCols];
							AttrType[] cType_db_algos = new AttrType[totCols - 1];
							short[] cSize_db = new short[totCols];
							short[] cSize_strs_Tuple = new short[total_str_cols];
							short ColumnsTotal_short = Utilities.p3_csvFiles_add[i]
									.get_ColumnsTotal_short();

							for (int jj = 0; jj < totCols - 1; jj++) {
								cType_db_algos[jj] = Utilities.p3_csvFiles_add[i]
										.getAttrType_algos(jj);
								if (jj < total_str_cols)
									cSize_strs_Tuple[jj] = Utilities.p3_csvFiles_add[i]
											.get_cSize_str_Tuple(jj);
							}

							// Tuple t = new
							// Tuple(p3_Utilities_dmi.p3_csvFiles[i].getTupleSize());
							Tuple t = new Tuple();
							t.setHdr(ColumnsTotal_short, cType_db_algos,
									cSize_strs_Tuple);
							// t = p3_Utilities_dmi.p3_csvFiles[i].getTuple();
							for (int ih = 0; ih < totCols; ih++) {
								if (Utilities.p3_csvFiles_add[i].getType(ih) == 2) 
								{ // p3_Utilities_dmi.p3_csvFiles[i].getTuple().setScore(Float.parseFloat(row[ih]));
									t.setScore(Float.parseFloat(row[ih]));
								} else {
									AttrType type = Utilities.p3_csvFiles_add[i]
											.getAttrType(ih);
									setValue(t, type, row[ih], ih + 1);
								}
							}
							
							// add in own heap file 
							Utilities.p3_csvFiles_add[i].getHF()
									.insertRecord(t.getTupleByteArray());
							// add to table/heapfile of file that is in fileset
							//p3_Utilities_dmi.p3_csvFiles[i].getHF()
									//.insertRecord(t.getTupleByteArray());

						}// end read file for

						int rowsPerHeapFile = Utilities.p3_csvFiles_add[i]
								.getHF().getRecCnt();
						
						csvReader.close();
						Utilities.debug_Util_color(
								"\n "
										+ totRows
										+ "_"
										+ rowsPerHeapFile
										+ " tuples inserted in Heapfile: "
										+ Utilities.p3_csvFiles_add[i]
												.getHFName(), style, doc,
								Color.magenta);
						Utilities.debug_Util_color("\nafter " +
								Utilities.p3_csvFiles[i].getHF().getRecCnt() + " records: " +
								Utilities.p3_csvFiles[i].getHFName()								
								, style, doc,
								Color.red);

					} catch (Exception ek) {
						// System.err.println("error reading from file: " +
						// _fileLoc);
						Utilities._filesInDB_add[i] = 0;
						StringWriter sw = new StringWriter();
						ek.printStackTrace(new PrintWriter(sw));
						debug_Util_color("\n errow reading file index :" + i
								+ " rowNum: " + rowNum + " " + sw.toString(),
								style, doc, Color.red);

					}

					if (rowNum == totRows) {
						Utilities._filesInDB_add[i] = 2;
						// file already in db}
					} else {
						Utilities._filesInDB_add[i] = 0;
						Utilities.debug_Util_color(
								"\need to reenter this file in set: " + i,
								style, doc, Color.black);

					}

					out = out + "complete inserting add table";

				}// endvalue == 1

			} // end check all files

		}// addfiletodb eventf
		return out;
	} // end function
	
	
	public static String sub_NewRecordToTableInDB(ActionEvent e,
			javax.swing.text.Style style, StyledDocument doc, int tableChosen) {
		String out = null;

		// p3_Utilities_dmi.debug_Util_color("\ndebug a1 ", style, doc,
		// Color.red);
		// if (e.getActionCommand().equals("ADDFILETODB")) {
		if (e.getActionCommand().equals("SUB")) {

			int len = Utilities._filesToSub.length;

			// p3_Utilities_dmi.debug_Util_color("\ndebug a1 ", style, doc,
			// Color.red);
			for (int i = 0; i < len; i++) {
				int value = Utilities._filesToSub[i];
				// /////////////

				if (value == 1) {
					out = "";
					String inner = "";
					String insertedRows = "";

					// p3_Utilities_dmi.debug_Util_color("\ndebug a2  " + i,
					// style, doc, Color.red);

					String tblName = Utilities.p3_csvFiles_sub[i]
							.getFileName().toString();
					String fileLoc = Utilities.p3_csvFiles_sub[i]
							.getFileLocation();
					Utilities.debug_Util_color("\nadding file index: "
							+ i + ", " + tblName, style, doc, Color.red);
					// /////////////
					String[] row = null;
					CSVReader csvReader = null;
					int totRows = 0;
					int totCols = 0;
					int rowNum = 0;
					int total_str_cols = 0;

					totRows = Utilities.p3_csvFiles_sub[i]
							.getTotalRows();
					try {
						csvReader = new CSVReader(new FileReader(new File(
								fileLoc)));
						// totCols = p3_Utilities_dmi. _columnsTotal;
						// totCols =
						// p3_Utilities_dmi.p3_csvFiles[tableChosen].getColCount();
						// total_str_cols =
						// p3_Utilities_dmi.p3_csvFiles[tableChosen].getTotal_str_cols();
						// inner = inner + ", opened with " + totRows;
						// p3_Utilities_dmi.debug_Util_color(inner , style, doc,
						// Color.red);

						totCols = Utilities._columnsTotal;
						total_str_cols = Utilities._total_str_cols;
						inner = inner + ", opened with " + totRows;
						Utilities.debug_Util_color(inner, style, doc,Color.red);
						Utilities.debug_Util_color("\nbefore " +
								Utilities.p3_csvFiles[i].getHF().getRecCnt() + " records: " +
								Utilities.p3_csvFiles[i].getHFName() 								
								, style, doc,
								Color.red);
						// p3_Utilities_dmi.debug_Util_color("\ndebug a3  " + i
						// + tblName, style, doc, Color.red);

						
						
						/*
						Scan openScan1 = p3_Utilities_dmi.p3_csvFiles_add[i].getHF().openScan();
						int recCount;
                        RID rid1 = new RID();
                        Tuple tuple1 =  new Tuple();
                        int iix =0;
                       	try {
								
								recCount= p3_Utilities_dmi.p3_csvFiles_add[i].getHF().getRecCnt();
								
								debug_Util_color("\n recCount " + recCount + " ", style, doc,
										Color.red);
								for (iix = 0; iix < recCount; iix++)
								{tuple1 = openScan1.getNext(rid1);
								p3_Utilities_dmi.p3_csvFiles_add[i].getHF().deleteRecord(rid1);
								}
								
							} catch (Exception e6) {
								StringWriter sw = new StringWriter();
								e6.printStackTrace(new PrintWriter(sw));
								debug_Util_color("\n end deleting earlier added file " +iix + " " + sw.toString(), style, doc,
										Color.red);
						

							} */
                       
						
						
						
						
						Scan openScan1 = Utilities.p3_csvFiles_sub[i].getHF().openScan();
                        RID rid1 = new RID();
                        Tuple tuple1 = openScan1.getNext(rid1);
                        while (tuple1 != null) {
                             Utilities.p3_csvFiles_sub[i].getHF().deleteRecord(rid1);
                            rid1 = new RID();
                            tuple1 = openScan1.getNext(rid1);
                        }
						
						
						for (int k = 0; k < totRows; k++) {
							row = csvReader.readNext();
							rowNum = k + 1;
							// AttrType[] cType_db = new AttrType[totCols];
							AttrType[] cType_db_algos = new AttrType[totCols - 1];
							short[] cSize_db = new short[totCols];
							short[] cSize_strs_Tuple = new short[total_str_cols];
							short ColumnsTotal_short = Utilities.p3_csvFiles_sub[i]
									.get_ColumnsTotal_short();

							for (int jj = 0; jj < totCols - 1; jj++) {
								cType_db_algos[jj] = Utilities.p3_csvFiles_sub[i]
										.getAttrType_algos(jj);
								if (jj < total_str_cols)
									cSize_strs_Tuple[jj] = Utilities.p3_csvFiles_sub[i]
											.get_cSize_str_Tuple(jj);
							}

							// Tuple t = new
							// Tuple(p3_Utilities_dmi.p3_csvFiles[i].getTupleSize());
							Tuple t = new Tuple();
							t.setHdr(ColumnsTotal_short, cType_db_algos,
									cSize_strs_Tuple);
							// t = p3_Utilities_dmi.p3_csvFiles[i].getTuple();
							for (int ih = 0; ih < totCols; ih++) {
								if (Utilities.p3_csvFiles_sub[i].getType(ih) == 2) 
								{ // p3_Utilities_dmi.p3_csvFiles[i].getTuple().setScore(Float.parseFloat(row[ih]));
									t.setScore(Float.parseFloat(row[ih]));
								} else {
									AttrType type = Utilities.p3_csvFiles_sub[i]
											.getAttrType(ih);
									setValue(t, type, row[ih], ih + 1);
								}
							}
							
							
							Utilities.p3_csvFiles_sub[i].getHF().insertRecord(t.getTupleByteArray());
							
								

						}// end read file for

						int rowsPerHeapFile = Utilities.p3_csvFiles_sub[i]
								.getHF().getRecCnt();
						csvReader.close();
						Utilities.debug_Util_color(
								"\n "
										+ totRows
										+ "_"
										+ rowsPerHeapFile
										+ " tuples inserted in Heapfile: "
										+ Utilities.p3_csvFiles_sub[i]
												.getHFName(), style, doc,
								Color.magenta);
						Utilities.debug_Util_color("\nafter " +
								Utilities.p3_csvFiles[i].getHF().getRecCnt() + " records: " +
								Utilities.p3_csvFiles[i].getHFName()								
								, style, doc,
								Color.red);
						// p3_Utilities_dmi.debug_Util_color("\ndebug a3  " + i
						// + tblName, style, doc, Color.red);

					} catch (Exception ek) {
						// System.err.println("error reading from file: " +
						// _fileLoc);
						Utilities._filesInDB_sub[i] = 0;
						StringWriter sw = new StringWriter();
						ek.printStackTrace(new PrintWriter(sw));
						debug_Util_color("\n errow reading file index :" + i
								+ " rowNum: " + rowNum + " " + sw.toString(),
								style, doc, Color.red);

					}

					if (rowNum == totRows) {
						Utilities._filesInDB_sub[i] = 2;
						// file already in db}
					} else {
						Utilities._filesInDB_sub[i] = 0;
						Utilities.debug_Util_color(
								"\need to reenter this file in set: " + i,
								style, doc, Color.black);

					}

					out = out + "complete inserting sub table";

				}// endvalue == 1

			} // end check all files

		}// addfiletodb eventf
		return out;
	} // end function
	

	public static String AddTableToDB(ActionEvent e,
			javax.swing.text.Style style, StyledDocument doc) {
		String out = null;

		// p3_Utilities_dmi.debug_Util_color("\ndebug a1 ", style, doc,
		// Color.red);
		// if (e.getActionCommand().equals("ADDFILETODB")) {
		if (e.getActionCommand().equals("ADDFILETOSET")) {

			int len = Utilities._filesInDB.length;

			// p3_Utilities_dmi.debug_Util_color("\ndebug a1 ", style, doc,
			// Color.red);
			for (int i = 0; i < len; i++) {
				int value = Utilities._filesInDB[i];
				// /////////////

				if (value == 1) {
					out = "";
					String inner = "";
					String insertedRows = "";

					// p3_Utilities_dmi.debug_Util_color("\ndebug a2  " + i,
					// style, doc, Color.red);

					String tblName = Utilities.p3_csvFiles[i]
							.getFileName().toString();
					String fileLoc = Utilities.p3_csvFiles[i]
							.getFileLocation();
					Utilities.debug_Util_color("\nadding file index: "
							+ i + ", " + tblName, style, doc, Color.red);
					// /////////////
					String[] row = null;
					CSVReader csvReader = null;
					int totRows = 0;
					int totCols = 0;
					int rowNum = 0;
					int total_str_cols = 0;

					totRows = Utilities.p3_csvFiles[i].getTotalRows();
					try {
						csvReader = new CSVReader(new FileReader(new File(
								fileLoc)));
						totCols = Utilities._columnsTotal;
						total_str_cols = Utilities._total_str_cols;
						inner = inner + ", opened with " + totRows;
						Utilities.debug_Util_color(inner, style, doc,
								Color.red);

						// p3_Utilities_dmi.debug_Util_color("\ndebug a3  " + i
						// + tblName, style, doc, Color.red);
						int check = 0;
						int check2 = 0;
						for (int k = 0; k < totRows; k++) {
							row = csvReader.readNext();
							rowNum = k + 1;
							// AttrType[] cType_db = new AttrType[totCols];
							AttrType[] cType_db_algos = new AttrType[totCols - 1];
							short[] cSize_db = new short[totCols];
							short[] cSize_strs_Tuple = new short[total_str_cols];
							short ColumnsTotal_short = Utilities.p3_csvFiles[i]
									.get_ColumnsTotal_short();

							for (int jj = 0; jj < totCols - 1; jj++) {
								cType_db_algos[jj] = Utilities.p3_csvFiles[i]
										.getAttrType_algos(jj);
								if (jj < total_str_cols)
									cSize_strs_Tuple[jj] = Utilities.p3_csvFiles[i]
											.get_cSize_str_Tuple(jj);
							}

							// Tuple t = new
							// Tuple(p3_Utilities_dmi.p3_csvFiles[i].getTupleSize());
							Tuple t = new Tuple();

							t.setHdr(ColumnsTotal_short, cType_db_algos,
									cSize_strs_Tuple);
							// t = p3_Utilities_dmi.p3_csvFiles[i].getTuple();
							for (int ih = 0; ih < totCols; ih++) {
								if (Utilities.p3_csvFiles[i].getType(ih) == 2) // a
																						// real
																						// //
																						// real
								{ // p3_Utilities_dmi.p3_csvFiles[i].getTuple().setScore(Float.parseFloat(row[ih]));
									t.setScore(Float.parseFloat(row[ih]));
								} else {
									// this if is for testing remove when done
									// also we did not keep track of the real
									// field may be tbd
									if ((ih == 0) && (check2++ < 10)) {
										String testString = row[ih];
										AttrType type = Utilities.p3_csvFiles[i]
												.getAttrType(ih);
										setValue(t, type, testString, ih + 1);
										// above if to be removed after debug
									} else {
										AttrType type = Utilities.p3_csvFiles[i]
												.getAttrType(ih);
										setValue(t, type, row[ih], ih + 1);

									}

									if (check++ < 1) {
									}
									// p3_Utilities_dmi
									// .debug_Util_color(
									// "\n in function p3_mainDriver_db.addTableToDB()  \n Ask group if this should be ih only or as is ih+ 1",
									// style, doc, Color.red);
								}

							}

							//RID rid = new RID();
						   //rid.
							// p3_Utilities_dmi.p3_csvFiles[i].hf.insertRecord(p3_Utilities_dmi.p3_csvFiles[i].getTuple().getTupleByteArray());
							// p3_Utilities_dmi.p3_csvFiles[i].hf.insertRecord(p3_Utilities_dmi.p3_csvFiles[i].getTuple().getTupleByteArray());
							RID rid = new RID();
							String key = null;
							
							rid = Utilities.p3_csvFiles[i].getHF()
									.insertRecord(t.getTupleByteArray());
							key = t.toString();
							
//							try
//							{
//								p3_Utilities_dmi.p3_csvFiles[i].getIndexFile().insert(new StringKey(key),rid);
//													
//							}
//							catch (Exception s)
//							{s.printStackTrace();}

						}// end read file for
						int rowsPerHeapFile = Utilities.p3_csvFiles[i]
								.getHF().getRecCnt();
						csvReader.close();
						Utilities.debug_Util_color("\n " + totRows + "_"
								+ rowsPerHeapFile
								+ " tuples inserted in Heapfile: "
								+ Utilities.p3_csvFiles[i].getHFName(),
								style, doc, Color.magenta);
						// p3_Utilities_dmi.debug_Util_color("\ndebug a3  " + i
						// + tblName, style, doc, Color.red);

					} catch (Exception ek) {
						// System.err.println("error reading from file: " +
						// _fileLoc);
						Utilities._filesInDB[i] = 0;
						StringWriter sw = new StringWriter();
						ek.printStackTrace(new PrintWriter(sw));
						debug_Util_color("\n errow reading file index :" + i
								+ " rowNum: " + rowNum + " " + sw.toString(),
								style, doc, Color.red);

					}

					if (rowNum == totRows) {
						Utilities._filesInDB[i] = 2;
						// file already in db}
					} else {
						Utilities._filesInDB[i] = 0;
						Utilities.debug_Util_color(
								"\need to reenter this file in set: " + i,
								style, doc, Color.black);

					}
					out = out + "complete inserting";

				}// endvalue == 1

			} // end check all files

		}// addfiletodb eventf
		return out;
	} // end function

	public static void debug_Util(String str, javax.swing.text.Style style,
			StyledDocument doc) {
		// //debug.append(str + "\n");
		// /style = debug.addStyle("Green",null);
		StyleConstants.setForeground(style, Color.black);
		try {
			doc.insertString(doc.getLength(), str, style);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}

	}

	public static void debug_Util_color(String str,
			javax.swing.text.Style style, StyledDocument doc, Color fg) {
		// //debug.append(str + "\n");
		// /style = debug.addStyle("Green",null);
		StyleConstants.setForeground(style, fg);
		try {
			doc.insertString(doc.getLength(), str, style);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		// StyleConstants.setForeground(style,Color.black);

	}

	private static void setValue(Tuple t, AttrType type, String value, int fldNo)
			throws FieldNumberOutOfBoundException, IOException {
		switch (type.attrType) {
		case AttrType.attrString:
			t.setStrFld(fldNo, value);
			break;
		case AttrType.attrInteger:
			t.setIntFld(fldNo, Integer.parseInt(value));
			break;
		case AttrType.attrReal:
			t.setFloFld(fldNo, Float.parseFloat(value));
			break;
		default:
			break;
		}
	}

	public static int printIterator(Iterator it, AttrType[] attrTypes) {
		Tuple t = null;
		int count = 0;
		try {
			while ((t = it.get_next()) != null) {
				// t.print(attrTypes);
				// System.out.println(t.getScore());
				System.out.println(t.toString());
				count++;
			}
		} catch (Exception e) {
			System.err.println("error printing iterator");
			e.printStackTrace();
		}
		return count;
	}

	public static int countIterator(Iterator it, AttrType[] attrTypes) {
		Tuple t = null;
		int count = 0;
		try {
			while ((t = it.get_next()) != null) {
				count++;
			}
		} catch (Exception e) {
			System.err.println("error printing iterator");
			e.printStackTrace();
		}
		return count;
	}

	public static void runFA(int countx, javax.swing.text.Style style,
			StyledDocument doc) throws JoinsException, IndexException,
		InvalidTupleSizeException, InvalidTypeException, PageNotReadException, TupleUtilsException,
		PredEvalException, LowMemException, UnknowAttrType, UnknownKeyTypeException, Exception {
		// check if add/remove for any one of these tables is set
		Parameters params = extractParameters(countx);
		int addRemoveCount = 0;
		for(int i = 0; i < Utilities._maxFiles; i++) {
			if (Utilities._filesInDB_add[i] == 2 || Utilities._filesInDB_sub[i] == 2) {
				addRemoveCount++;
			}
		}
		if (!_ignore_increments && addRemoveCount > 0) {
			// if so run the incremental fagin's
			String[] addIndexFiles = new String[params.addHeapfiles.length];
			Iterator[] addTuples = new Iterator[params.addHeapfiles.length];
			String[] addHeapfiles = new String[params.addHeapfiles.length];
			for (int i = 0; i < params.addHeapfiles.length; i++) {
				if (params.addHeapfiles[i] != null) {
					String currentHeapFileName = params.addHeapfiles[i].getFileName();
					addHeapfiles[i] = params.addHeapfiles[i].getFileName();
					String indexFile = getIndexFile(currentHeapFileName,
							params.tableTypes[i], params.tableStrSizes[i],
							params.joinColIndices[i]);
					addIndexFiles[i] = indexFile;
					FldSpec[] projection = new FldSpec[params.tableTypes[i].length];
					for (int j = 0; j < projection.length; j++) {
						projection[j] = new FldSpec(new RelSpec(RelSpec.outer), j + 1);
					}
					addTuples[i] = new FileScan(params.addHeapfiles[i].getFileName(),
							params.tableTypes[i], params.tableStrSizes[i],
							(short) params.tableTypes[i].length,
							(short) params.tableTypes[i].length, projection, null);
				}
			}

			String[] removeIndexFiles = new String[params.subHeapfiles.length];
			String[] removeHeapfiles = new String[params.subHeapfiles.length];
			Iterator[] removeTuples = new Iterator[params.subHeapfiles.length];
			for (int i = 0; i < params.subHeapfiles.length; i++) {
				if (params.subHeapfiles[i] != null) {
					String currentHeapFileName = params.subHeapfiles[i].getFileName();
					String indexFile = getIndexFile(currentHeapFileName,
							params.tableTypes[i], params.tableStrSizes[i],
							params.joinColIndices[i]);
					removeIndexFiles[i] = indexFile;
					removeHeapfiles[i] = params.subHeapfiles[i].getFileName();
					FldSpec[] projection = new FldSpec[params.tableTypes[i].length];
					for (int j = 0; j < projection.length; j++) {
						projection[j] = new FldSpec(new RelSpec(RelSpec.outer), j + 1);
					}
					removeTuples[i] = new FileScan(params.subHeapfiles[i].getFileName(),
							params.tableTypes[i], params.tableStrSizes[i],
							(short) params.tableTypes[i].length,
							(short) params.tableTypes[i].length, projection, null);
				}
			}

			Iterator[] am = new Iterator[params.numTables];
			String[] heapFiles = new String[params.numTables];
			for (int j = 0; j < am.length; j++) {
				try {
					FldSpec[] projection = new FldSpec[params.tableTypes[j].length];
					for (int k = 0; k < projection.length; k++) {
						projection[k] = new FldSpec(new RelSpec(RelSpec.outer),
								k + 1);
					}
					heapFiles[j] = params.heapfiles[j].getFileName();
					am[j] = new FileScan(params.heapfiles[j].getFileName(),
							params.tableTypes[j], params.tableStrSizes[j],
							(short) params.tableTypes[j].length,
							(short) params.tableTypes[j].length, projection, null);
				} catch (Exception k) {
					System.err
							.println("error creating file scan iterator for table: "
									+ j);
				}
			}

			for (int i = 0; i < heapFiles.length; i++) {
				Heapfile actualHeapfile = new Heapfile(heapFiles[i]);
				if (addHeapfiles[i] != null) {
					// add the tuples from the add heapfile to this heapfile
					Heapfile addHeapfile = new Heapfile(addHeapfiles[i]);
					Scan openScan = addHeapfile.openScan();
					RID rid = new RID();
					Tuple tuple = openScan.getNext(rid);
					while (tuple != null) {
						actualHeapfile.insertRecord(tuple.getTupleByteArray());
						tuple = openScan.getNext(rid);
					}
				}
				if (removeHeapfiles[i] != null) {
					// remove the tuples from the remove heapfile to this heapfile
					Heapfile removeHeapfile = new Heapfile(removeHeapfiles[i]);
					Scan openScan = removeHeapfile.openScan();
					RID rid = new RID();
					Tuple tuple = openScan.getNext(rid);
					while (tuple != null) {
						tuple.setHdr((short) params.tableTypes[i].length, params.tableTypes[i], params.tableStrSizes[i]);
						Scan scan = actualHeapfile.openScan();
						Tuple t2 = scan.getNext(rid);
						while (t2 != null) {
							t2.setHdr((short) params.tableTypes[i].length, params.tableTypes[i], params.tableStrSizes[i]);
							if (TupleUtils.Equal(tuple, t2, params.tableTypes[i], params.tableTypes[i].length)) {
								actualHeapfile.deleteRecord(rid);
							}
							t2 = scan.getNext(rid);
						}
						tuple = openScan.getNext(rid);
					}
				}
			}

			String[] indexFiles = new String[params.heapfiles.length];
			for (int k = 0; k < params.heapfiles.length; k++) {
				String currentHeapFileName = params.heapfiles[k].getFileName();
				String indexFile = getIndexFile(currentHeapFileName,
						params.tableTypes[k], params.tableStrSizes[k],
						params.joinColIndices[k]);
				indexFiles[k] = indexFile;
			}

			int[] tableSizes = new int[params.numTables];
			int totalSize = 0;
			for (int j = 0; j < tableSizes.length; j++) {
				tableSizes[j] = params.tableTypes[j].length;
				totalSize += tableSizes[j];
			}
			FldSpec[] projList = new FldSpec[totalSize];
			int count1 = 0;
			for (int j = 0; j < params.numTables; j++) {
				for (int k = 0; k < params.tableTypes[j].length; k++) {
					projList[count1++] = new FldSpec(j, k + 1);
				}
			}

			PCounter.initialize();
			TopRankJoin topTuplesIt = new TopRankJoin(tableSizes.length, params.tableTypes, tableSizes,
					params.tableStrSizes, params.joinColIndices, am, null, indexFiles, heapFiles, params.amountOfMemory,
					null, projList, projList.length, params.topK, _useIndicator);

//			List<Tuple> updateTopRankJoin = UpdateJoins.updateTopRankJoin(addTuples, removeTuples, addHeapfiles, removeHeapfiles,
//					params.tableTypes, params.tableStrSizes, params.topK, addIndexFiles, removeIndexFiles, params.amountOfMemory,
//					params.joinColIndices, topTuplesIt, heapFiles);
//			JTextArea textArea = printInWindow("FA Join (Incremental)");
//			int counter = 0;
//			for (Tuple tuple : updateTopRankJoin) {
//				textArea.append(++counter + " " + tuple.toString() + "\n");
//			}

			JTextArea textArea = printInWindow("FA Join (Incremental)");
			int counter = 0;
			Tuple topTuple = topTuplesIt.get_next();
			while (topTuple != null) {
				//if (!UpdateJoins.checkFromDeleteSet(removeIndexFiles, topTuple.getStrFld(1))) {
					String tupleString = topTuple.toString();
					textArea.append(++counter + " " + tupleString + "\n");
				//}
				topTuple = topTuplesIt.get_next();
			}

			//debug_Util("\nIteration count: " + topTuplesIt._iterationCount, style, doc);
			debug_Util("\nPage access count: " + PCounter.counter, style, doc);
		} else {
			// if not run regular fagin's
			String[] indexFiles = new String[params.heapfiles.length];
			for (int k = 0; k < params.heapfiles.length; k++) {
				String currentHeapFileName = params.heapfiles[k].getFileName();
				String indexFile = getIndexFile(currentHeapFileName,
						params.tableTypes[k], params.tableStrSizes[k],
						params.joinColIndices[k]);
				indexFiles[k] = indexFile;
			}
			int[] tableSizes = new int[params.numTables];
			int totalSize = 0;
			for (int j = 0; j < tableSizes.length; j++) {
				tableSizes[j] = params.tableTypes[j].length;
				totalSize += tableSizes[j];
			}
			FldSpec[] projList = new FldSpec[totalSize];
			int count1 = 0;
			for (int j = 0; j < params.numTables; j++) {
				for (int k = 0; k < params.tableTypes[j].length; k++) {
					projList[count1++] = new FldSpec(j, k + 1);
				}
			}
			Iterator[] am = new Iterator[params.numTables];
			String[] heapFiles = new String[params.numTables];
			for (int j = 0; j < am.length; j++) {
				try {
					FldSpec[] projection = new FldSpec[params.tableTypes[j].length];
					for (int k = 0; k < projection.length; k++) {
						projection[k] = new FldSpec(new RelSpec(RelSpec.outer),
								k + 1);
					}
					heapFiles[j] = params.heapfiles[j].getFileName();
					am[j] = new FileScan(params.heapfiles[j].getFileName(),
							params.tableTypes[j], params.tableStrSizes[j],
							(short) params.tableTypes[j].length,
							(short) params.tableTypes[j].length, projection, null);
				} catch (Exception k) {
					System.err
							.println("error creating file scan iterator for table: "
									+ j);
				}
			}
			PCounter.initialize();
			TopRankJoin topTuplesIt = new TopRankJoin(tableSizes.length, params.tableTypes, tableSizes,
					params.tableStrSizes, params.joinColIndices, am, null, indexFiles, heapFiles, params.amountOfMemory,
					null, projList, projList.length, params.topK, _useIndicator);
			JTextArea textArea = printInWindow("FA Join");
			int counter = 0;
			Tuple topTuple = topTuplesIt.get_next();
			while (topTuple != null) {
				String tupleString = topTuple.toString();
				topTuple = topTuplesIt.get_next();
				textArea.append(++counter + " " + tupleString + "\n");
			}
			//debug_Util("\nIteration count: " + topTuplesIt._iterationCount, style, doc);
			debug_Util("\nPage access count: " + PCounter.counter, style, doc);
		}
	}

	public static void runSC(int countx, javax.swing.text.Style style,
			StyledDocument doc) throws JoinsException, IndexException, InvalidTupleSizeException,
		InvalidTypeException, PageNotReadException, TupleUtilsException, PredEvalException, LowMemException,
		UnknowAttrType, UnknownKeyTypeException, Exception {
		// check if add/remove for any one of these tables is set
		Parameters params = extractParameters(countx);
		int addRemoveCount = 0;
		for(int i = 0; i < Utilities._maxFiles; i++) {
			if (Utilities._filesInDB_add[i] == 2 || Utilities._filesInDB_sub[i] == 2) {
				addRemoveCount++;
			}
		}
		if (!_ignore_increments && addRemoveCount > 0) {
			String[] addIndexFiles = new String[params.addHeapfiles.length];
			Iterator[] addTuples = new Iterator[params.addHeapfiles.length];
			String[] addHeapfiles = new String[params.addHeapfiles.length];
			for (int i = 0; i < params.addHeapfiles.length; i++) {
				if (params.addHeapfiles[i] != null) {
					String currentHeapFileName = params.addHeapfiles[i].getFileName();
					addHeapfiles[i] = params.addHeapfiles[i].getFileName();
					String indexFile = getIndexFile(currentHeapFileName,
							params.tableTypes[i], params.tableStrSizes[i],
							params.joinColIndices[i]);
					addIndexFiles[i] = indexFile;
					FldSpec[] projection = new FldSpec[params.tableTypes[i].length];
					for (int j = 0; j < projection.length; j++) {
						projection[j] = new FldSpec(new RelSpec(RelSpec.outer), j + 1);
					}
					addTuples[i] = new FileScan(params.addHeapfiles[i].getFileName(),
							params.tableTypes[i], params.tableStrSizes[i],
							(short) params.tableTypes[i].length,
							(short) params.tableTypes[i].length, projection, null);
				}
			}

			String[] removeIndexFiles = new String[params.subHeapfiles.length];
			String[] removeHeapfiles = new String[params.subHeapfiles.length];
			Iterator[] removeTuples = new Iterator[params.subHeapfiles.length];
			for (int i = 0; i < params.subHeapfiles.length; i++) {
				if (params.subHeapfiles[i] != null) {
					String currentHeapFileName = params.subHeapfiles[i].getFileName();
					String indexFile = getIndexFile(currentHeapFileName,
							params.tableTypes[i], params.tableStrSizes[i],
							params.joinColIndices[i]);
					removeIndexFiles[i] = indexFile;
					removeHeapfiles[i] = params.subHeapfiles[i].getFileName();
					FldSpec[] projection = new FldSpec[params.tableTypes[i].length];
					for (int j = 0; j < projection.length; j++) {
						projection[j] = new FldSpec(new RelSpec(RelSpec.outer), j + 1);
					}
					removeTuples[i] = new FileScan(params.subHeapfiles[i].getFileName(),
							params.tableTypes[i], params.tableStrSizes[i],
							(short) params.tableTypes[i].length,
							(short) params.tableTypes[i].length, projection, null);
				}
			}

			Iterator[] am = new Iterator[params.numTables];
			String[] heapFiles = new String[params.numTables];
			for (int j = 0; j < am.length; j++) {
				try {
					FldSpec[] projection = new FldSpec[params.tableTypes[j].length];
					for (int k = 0; k < projection.length; k++) {
						projection[k] = new FldSpec(new RelSpec(RelSpec.outer),
								k + 1);
					}
					heapFiles[j] = params.heapfiles[j].getFileName();
					am[j] = new FileScan(params.heapfiles[j].getFileName(),
							params.tableTypes[j], params.tableStrSizes[j],
							(short) params.tableTypes[j].length,
							(short) params.tableTypes[j].length, projection, null);
				} catch (Exception k) {
					System.err
							.println("error creating file scan iterator for table: "
									+ j);
				}
			}

			for (int i = 0; i < heapFiles.length; i++) {
				Heapfile actualHeapfile = new Heapfile(heapFiles[i]);
				if (addHeapfiles[i] != null) {
					// add the tuples from the add heapfile to this heapfile
					Heapfile addHeapfile = new Heapfile(addHeapfiles[i]);
					Scan openScan = addHeapfile.openScan();
					RID rid = new RID();
					Tuple tuple = openScan.getNext(rid);
					while (tuple != null) {
						actualHeapfile.insertRecord(tuple.getTupleByteArray());
						tuple = openScan.getNext(rid);
					}
				}
				if (removeHeapfiles[i] != null) {
					// remove the tuples from the remove heapfile to this heapfile
					Heapfile removeHeapfile = new Heapfile(removeHeapfiles[i]);
					Scan openScan = removeHeapfile.openScan();
					RID rid = new RID();
					Tuple tuple = openScan.getNext(rid);
					while (tuple != null) {
						tuple.setHdr((short) params.tableTypes[i].length, params.tableTypes[i], params.tableStrSizes[i]);
						Scan scan = actualHeapfile.openScan();
						Tuple t2 = scan.getNext(rid);
						while (t2 != null) {
							t2.setHdr((short) params.tableTypes[i].length, params.tableTypes[i], params.tableStrSizes[i]);
							if (TupleUtils.Equal(tuple, t2, params.tableTypes[i], params.tableTypes[i].length)) {
								actualHeapfile.deleteRecord(rid);
							}
							t2 = scan.getNext(rid);
						}
						tuple = openScan.getNext(rid);
					}
				}
			}


			int[] tableSizes = new int[params.numTables];
			int totalSize = 0;
			for (int j = 0; j < tableSizes.length; j++) {
				tableSizes[j] = params.tableTypes[j].length;
				totalSize += tableSizes[j];
			}
			FldSpec[] projList = new FldSpec[totalSize];
			int count1 = 0;
			for (int j = 0; j < params.numTables; j++) {
				for (int k = 0; k < params.tableTypes[j].length; k++) {
					projList[count1++] = new FldSpec(j, k + 1);
				}
			}

			PCounter.initialize();
			TopSCJoin topTuplesIt = new TopSCJoin(tableSizes.length, params.tableTypes, tableSizes,
					params.tableStrSizes, params.joinColIndices, am, params.amountOfMemory,
					null, projList, projList.length, params.topK, _useIndicator);

//			List<Tuple> updateTopRankJoin = UpdateJoins.updateTopRankJoin(addTuples, removeTuples, addHeapfiles, removeHeapfiles,
//					params.tableTypes, params.tableStrSizes, params.topK, addIndexFiles, removeIndexFiles, params.amountOfMemory,
//					params.joinColIndices, topTuplesIt, heapFiles);
//			JTextArea textArea = printInWindow("FA Join (Incremental)");
//			int counter = 0;
//			for (Tuple tuple : updateTopRankJoin) {
//				textArea.append(++counter + " " + tuple.toString() + "\n");
//			}

			JTextArea textArea = printInWindow("SC Join (Incremental)");
			int counter = 0;
			Tuple topTuple = topTuplesIt.get_next();
			while (topTuple != null) {
				//if (!UpdateJoins.checkFromDeleteSet(removeIndexFiles, topTuple.getStrFld(1))) {
					String tupleString = topTuple.toString();
					textArea.append(++counter + " " + tupleString + "\n");
				//}
				topTuple = topTuplesIt.get_next();
			}

			//debug_Util("\nIteration count: " + topTuplesIt._iterationCount, style, doc);
			debug_Util("\nPage access count: " + PCounter.counter, style, doc);
		} else {
			int[] tableSizes = new int[params.numTables];
			int totalSize = 0;
			for (int j = 0; j < tableSizes.length; j++) {
				tableSizes[j] = params.tableTypes[j].length;
				totalSize += tableSizes[j];
			}
			FldSpec[] projList = new FldSpec[totalSize];
			int count1 = 0;
			for (int j = 0; j < params.numTables; j++) {
				for (int k = 0; k < params.tableTypes[j].length; k++) {
					projList[count1++] = new FldSpec(j, k + 1);
				}
			}
	
			Iterator[] am = new Iterator[params.numTables];
			for (int j = 0; j < am.length; j++) {
				try {
					FldSpec[] projection = new FldSpec[params.tableTypes[j].length];
					for (int k = 0; k < projection.length; k++) {
						projection[k] = new FldSpec(new RelSpec(RelSpec.outer),
								k + 1);
					}
					am[j] = new FileScan(params.heapfiles[j].getFileName(),
							params.tableTypes[j], params.tableStrSizes[j],
							(short) params.tableTypes[j].length,
							(short) params.tableTypes[j].length, projection, null);
				} catch (Exception k) {
					System.err
							.println("error creating file scan iterator for table: "
									+ j);
				}
			}
			PCounter.initialize();
			TopSCJoin topSCJoin = new TopSCJoin(params.numTables,
					params.tableTypes, tableSizes, params.tableStrSizes,
					params.joinColIndices, am, params.amountOfMemory, null,
					projList, projList.length, params.topK, !_useIndicator);
			// print both
			JTextArea textArea = printInWindow("SC Join");
			int counter = 0;
			Tuple topTuple = topSCJoin.get_next();
			while (topTuple != null) {
				String tupleString = topTuple.toString();
				topTuple = topSCJoin.get_next();
				textArea.append(++counter + " " + tupleString + "\n");
			}
			//debug_Util("\nIteration count: " + topSCJoin._iterationCount, style, doc);
			debug_Util("\nPage access count: " + PCounter.counter, style, doc);
		}
	}

}