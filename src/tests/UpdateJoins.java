package tests;

import global.AttrType;
import heap.FieldNumberOutOfBoundException;
import heap.InvalidTupleSizeException;
import heap.InvalidTypeException;
import heap.Tuple;
import index.IndexException;
import iterator.FldSpec;
import iterator.Iterator;
import iterator.JoinsException;
import iterator.LowMemException;
import iterator.PredEvalException;
import iterator.TopNRAJoin;
import iterator.TopRankJoin;
import iterator.TopSCJoin;
import iterator.TopTAJoin;
import iterator.TupleUtilsException;
import iterator.UnknowAttrType;
import iterator.UnknownKeyTypeException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import btree.BTFileScan;
import btree.BTreeFile;
import btree.ConstructPageException;
import btree.GetFileEntryException;
import btree.IteratorException;
import btree.KeyClass;
import btree.KeyDataEntry;
import btree.KeyNotMatchException;
import btree.PinPageException;
import btree.ScanIteratorException;
import btree.StringKey;
import btree.UnpinPageException;
import bufmgr.PageNotReadException;

public class UpdateJoins {

	private static final float EPSILON = (float) 0.0000001;

	public static List<Tuple> updateTopRankJoin(Iterator[] addTuples, Iterator[] removeTuples, String[] addHeapfiles,
			String[] removeHeapfiles, AttrType[][] tableTypes, short[][] strSizes, int topN, String[] addIndexFiles,
			String[] removeIndexFiles, int amountOfMemory, int[] joinColIndices, TopRankJoin topTuplesIt, String[] heapFiles)
					throws JoinsException, IndexException, InvalidTupleSizeException, InvalidTypeException,
					PageNotReadException, TupleUtilsException, PredEvalException, LowMemException, UnknowAttrType,
					UnknownKeyTypeException, Exception {
		// create complete tuples doing random access
		// run FA on removeTuples...
		// get top K out of these remove tuples..
		// run sort compare to calculate d and the actual tuples
		
		// run FA on addTuples
		// get top K out of these add tuples..
		// run sort compare to calculate a and the actual tuples
		
		// d - a > 0 resume FA for d - a more tuples
		// d == a or d - a < 0 do nothing
		
		// remove d from prev top K
		// add a to prev. top  using sort merge

		int[] tableSizes = new int[tableTypes.length];
		int allFieldCount = 0;
		for (int i = 0; i < tableTypes.length; i++) {
			allFieldCount += tableTypes[i].length;
			tableSizes[i] = tableTypes[i].length;
		}
		FldSpec[] projList = new FldSpec[allFieldCount];
		int count = 0;
		for (int i = 0; i < tableTypes.length; i++) {
			for (int j = 0; j < tableTypes[i].length; j++) {
				projList[count++] = new FldSpec(i, j+1);
			}
		}
		TopRankJoin removeTopK = null;
		if (removeTuples != null) {
			removeTopK = new TopRankJoin(tableTypes.length, tableTypes, tableSizes, strSizes, joinColIndices, removeTuples, null,
				removeIndexFiles, removeHeapfiles, amountOfMemory, null, projList, projList.length, topN, MainDriver._useIndicator);
		}

		List<Tuple> mergedTuples = new ArrayList<Tuple>();
//		for (Tuple tuple : topTuples) {
//			mergedTuples.add(tuple);
//		}
//		List<Tuple> effectiveRemoveTuples = new ArrayList<Tuple>();
//		Tuple removeTuple = removeTopK.get_next();
//		int topKTuple = 0;
//		while(removeTuple != null && topKTuple < topTuples.size()) {
//			Tuple topTuple = topTuples.get(topKTuple);
//			if (Math.abs(topTuple.getScore() - removeTuple.getScore()) < EPSILON) {
//				topKTuple++;
//				removeTuple = removeTopK.get_next();
//				effectiveRemoveTuples.add(removeTuple);
//				mergedTuples.remove(topTuple);
//			} else if (topTuple.getScore() > removeTuple.getScore()){
//				topKTuple++;
//			} else {
//				removeTuple = removeTopK.get_next();
//			}
//		}

		TopRankJoin addTopK = new TopRankJoin(tableTypes.length, tableTypes, tableSizes, strSizes, joinColIndices, addTuples, null,
				addIndexFiles, addHeapfiles, amountOfMemory, null, projList, projList.length, topN, MainDriver._useIndicator);

//		List<Tuple> effectiveAddTuples = new ArrayList<Tuple>();
//		Tuple addTuple = addTopK.get_next();
//		topKTuple = 0;
//		int totalMergeCount = 0;
//		while(totalMergeCount < topN) {
//			Tuple topTuple = topTuples.get(topKTuple);
//			if (topTuple.getScore() > addTuple.getScore()) {
//				topKTuple++;
//				totalMergeCount++;
//				if (topKTuple >= topTuples.size()) {
//					break;
//				}
//			} else {
//				effectiveAddTuples.add(addTuple);
//				addTuple = addTopK.get_next();
//				totalMergeCount++;
//				if(addTuple == null) {
//					break;
//				}
//			}
//		}
//
//		int d = effectiveRemoveTuples.size();
//		int a = effectiveAddTuples.size();
//
//		List<Tuple> finalTuples = new ArrayList<Tuple>();
//		if (d > a) {
//			TopRankJoin finalTopK = new TopRankJoin(tableTypes.length, tableTypes, tableSizes, strSizes, joinColIndices, addTuples, null,
//					indexFiles, amountOfMemory, null, projList, projList.length, topN);
//		} else if (d < a) {
//			int i = 0;
//			int j = 0;
//			while (i < (a - d)  && j < mergedTuples.size()) {
//				addTuple = effectiveAddTuples.get(i);
//				Tuple mergeTuple = mergedTuples.get(j);
//				if (addTuple.getScore() > mergeTuple.getScore()) {
//					finalTuples.add(addTuple);
//					i++;
//				} else {
//					finalTuples.add(mergeTuple);
//					j++;
//				}
//			}
//		} else {
//			int i = 0;
//			int j = 0;
//			while (i < effectiveAddTuples.size() && j < mergedTuples.size()) {
//				addTuple = effectiveAddTuples.get(i);
//				Tuple mergeTuple = mergedTuples.get(j);
//				if (addTuple.getScore() > mergeTuple.getScore()) {
//					finalTuples.add(addTuple);
//					i++;
//				} else {
//					finalTuples.add(mergeTuple);
//					j++;
//				}
//			}
//		}

		topTuplesIt.setSecondaryIndexFiles(addIndexFiles, addHeapfiles);
		Tuple addTuple = addTopK == null? null: addTopK.getOne();
		Tuple topTuple = topTuplesIt.getOne();
		Tuple removeTuple = removeTopK == null? null: removeTopK.getOne();

		while (mergedTuples.size() < topN) {
			if (topTuple == null || addTuple == null) {
				if (topTuple == null && addTuple != null) {
					mergedTuples.add(addTuple);
					addTuple = addTopK.getOne();
				} else if (addTuple == null && topTuple != null) {
					if (removeTuple == null) {
						mergedTuples.add(topTuple);
						topTuple = topTuplesIt.getOne();
					} else {
						String joinColumn = topTuple.getStrFld(1);
						if (!checkFromDeleteSet(removeIndexFiles, joinColumn)) {
							mergedTuples.add(topTuple);
						}
						topTuple = topTuplesIt.getOne();
					}
				} else {
					break;
				}
			} else {
				if (addTuple.getScore() > topTuple.getScore()) {
					mergedTuples.add(addTuple);
					addTuple = addTopK.getOne();
				} else {
					if (removeTuple == null) {
						mergedTuples.add(topTuple);
						topTuple = topTuplesIt.getOne();
					} else {
						String joinColumn = topTuple.getStrFld(1);
						if (!checkFromDeleteSet(removeIndexFiles, joinColumn)) {
							mergedTuples.add(topTuple);
						}
						topTuple = topTuplesIt.getOne();
					}
				}
			}
		}

		topTuplesIt.setSecondaryIndexFiles(null, null);
		return mergedTuples;
	}

	public static boolean checkFromDeleteSet(String[] removeIndexFiles, String joinColumn)
			throws GetFileEntryException, PinPageException, ConstructPageException,
			KeyNotMatchException, IteratorException, UnpinPageException, IOException, ScanIteratorException {
		for (int i = 0; i < removeIndexFiles.length; i++) {
			KeyClass btreeKey = new StringKey(joinColumn);
			if (removeIndexFiles[i] == null) {
				continue;
			}
			BTreeFile btf = new BTreeFile(removeIndexFiles[i]);
			BTFileScan btfScan = btf.new_scan(btreeKey, btreeKey);
			KeyDataEntry keyDataEntry = btfScan.get_next();
			if (keyDataEntry != null) {
				return true;
			}
		}
		return false;
	}

	public static List<Tuple> updateTopTAJoin(Iterator[] addTuples,
			Iterator[] removeTuples, String[] addHeapfiles,
			String[] removeHeapfiles, AttrType[][] tableTypes,
			short[][] tableStrSizes, int topK, String[] addIndexFiles,
			String[] removeIndexFiles, int amountOfMemory,
			int[] joinColIndices, TopTAJoin topTuplesIt) throws FieldNumberOutOfBoundException,
			GetFileEntryException, PinPageException, ConstructPageException, KeyNotMatchException,
			IteratorException, UnpinPageException, ScanIteratorException, UnknowAttrType, LowMemException,
			JoinsException, Exception {
		int[] tableSizes = new int[tableTypes.length];
		int allFieldCount = 0;
		for (int i = 0; i < tableTypes.length; i++) {
			allFieldCount += tableTypes[i].length;
			tableSizes[i] = tableTypes[i].length;
		}
		FldSpec[] projList = new FldSpec[allFieldCount];
		int count = 0;
		for (int i = 0; i < tableTypes.length; i++) {
			for (int j = 0; j < tableTypes[i].length; j++) {
				projList[count++] = new FldSpec(i, j+1);
			}
		}
		List<Tuple> mergedTuples = new ArrayList<Tuple>();
		TopTAJoin addTopK = new TopTAJoin(tableTypes.length, tableTypes, tableSizes, tableStrSizes, joinColIndices, addTuples, null,
				addIndexFiles, addHeapfiles, amountOfMemory, null, projList, projList.length, topK, MainDriver._useIndicator);

		topTuplesIt.setSecondaryIndexFiles(addIndexFiles, addHeapfiles);
		Tuple addTuple = addTopK == null? null: addTopK.getOne();
		Tuple topTuple = topTuplesIt.getOne();


		while (mergedTuples.size() < topK) {
			if (topTuple == null || addTuple == null) {
				if (topTuple == null && addTuple != null) {
					mergedTuples.add(addTuple);
					addTuple = addTopK.getOne();
				} else if (addTuple == null && topTuple != null) {
					String joinColumn = topTuple.getStrFld(1);
					if (!checkFromDeleteSet(removeIndexFiles, joinColumn)) {
						mergedTuples.add(topTuple);
					}
					topTuple = topTuplesIt.getOne();
				} else {
					break;
				}
			} else {
				if (addTuple.getScore() > topTuple.getScore()) {
					mergedTuples.add(addTuple);
					addTuple = addTopK.getOne();
				} else {
					String joinColumn = topTuple.getStrFld(1);
					if (!checkFromDeleteSet(removeIndexFiles, joinColumn)) {
						mergedTuples.add(topTuple);
					}
					topTuple = topTuplesIt.getOne();
				}
			}
		}

		topTuplesIt.setSecondaryIndexFiles(null, null);

		return mergedTuples;
	}

	public static List<Tuple> updateTopNRAJoin(Iterator[] addTuples,
			Iterator[] removeTuples, String[] addHeapfiles,
			String[] removeHeapfiles, AttrType[][] tableTypes,
			short[][] tableStrSizes, int topK,
			String[] removeIndexFiles, int amountOfMemory,
			int[] joinColIndices, TopNRAJoin topTuplesIt) throws UnknowAttrType, LowMemException, JoinsException, Exception {
		int[] tableSizes = new int[tableTypes.length];
		int allFieldCount = 0;
		for (int i = 0; i < tableTypes.length; i++) {
			allFieldCount += tableTypes[i].length;
			tableSizes[i] = tableTypes[i].length;
		}
		FldSpec[] projList = new FldSpec[allFieldCount];
		int count = 0;
		for (int i = 0; i < tableTypes.length; i++) {
			for (int j = 0; j < tableTypes[i].length; j++) {
				projList[count++] = new FldSpec(i, j+1);
			}
		}
		List<Tuple> mergedTuples = new ArrayList<Tuple>();
		TopNRAJoin addTopK = new TopNRAJoin(tableTypes.length, tableTypes, tableSizes, tableStrSizes,
				joinColIndices, addTuples, amountOfMemory, null, projList, projList.length, topK, MainDriver._useIndicator);

		Tuple addTuple = addTopK == null? null: addTopK.getOne();
		Tuple topTuple = topTuplesIt.getOne();


		while (mergedTuples.size() < topK) {
			if (topTuple == null || addTuple == null) {
				if (topTuple == null && addTuple != null) {
					mergedTuples.add(addTuple);
					addTuple = addTopK.getOne();
				} else if (addTuple == null && topTuple != null) {
					String joinColumn = topTuple.getStrFld(1);
					if (!checkFromDeleteSet(removeIndexFiles, joinColumn)) {
						mergedTuples.add(topTuple);
					}
					topTuple = topTuplesIt.getOne();
				} else {
					break;
				}
			} else {
				if (addTuple.getScore() > topTuple.getScore()) {
					mergedTuples.add(addTuple);
					addTuple = addTopK.getOne();
				} else {
					String joinColumn = topTuple.getStrFld(1);
					if (!checkFromDeleteSet(removeIndexFiles, joinColumn)) {
						mergedTuples.add(topTuple);
					}
					topTuple = topTuplesIt.getOne();
				}
			}
		}

		return mergedTuples;
	}

	public static List<Tuple> updateTopSCJoin(Iterator[] addTuples,
			Iterator[] removeTuples, String[] addHeapfiles,
			String[] removeHeapfiles, AttrType[][] tableTypes,
			short[][] tableStrSizes, int topK, String[] removeIndexFiles,
			int amountOfMemory, int[] joinColIndices, TopSCJoin topTuplesIt)
			throws UnknowAttrType, LowMemException, JoinsException, Exception {
		int[] tableSizes = new int[tableTypes.length];
		int allFieldCount = 0;
		for (int i = 0; i < tableTypes.length; i++) {
			allFieldCount += tableTypes[i].length;
			tableSizes[i] = tableTypes[i].length;
		}
		FldSpec[] projList = new FldSpec[allFieldCount];
		int count = 0;
		for (int i = 0; i < tableTypes.length; i++) {
			for (int j = 0; j < tableTypes[i].length; j++) {
				projList[count++] = new FldSpec(i, j+1);
			}
		}
		List<Tuple> mergedTuples = new ArrayList<Tuple>();
		TopSCJoin addTopK = new TopSCJoin(tableTypes.length, tableTypes, tableSizes, tableStrSizes,
				joinColIndices, addTuples, amountOfMemory, null, projList, projList.length, topK, !MainDriver._useIndicator);

		Tuple addTuple = addTopK == null? null: addTopK.getOne();
		Tuple topTuple = topTuplesIt.getOne();


		while (mergedTuples.size() < topK) {
			if (topTuple == null || addTuple == null) {
				if (topTuple == null && addTuple != null) {
					mergedTuples.add(addTuple);
					addTuple = addTopK.getOne();
				} else if (addTuple == null && topTuple != null) {
					String joinColumn = topTuple.getStrFld(1);
					if (!checkFromDeleteSet(removeIndexFiles, joinColumn)) {
						mergedTuples.add(topTuple);
					}
					topTuple = topTuplesIt.getOne();
				} else {
					break;
				}
			} else {
				if (addTuple.getScore() > topTuple.getScore()) {
					mergedTuples.add(addTuple);
					addTuple = addTopK.getOne();
				} else {
					String joinColumn = topTuple.getStrFld(1);
					if (!checkFromDeleteSet(removeIndexFiles, joinColumn)) {
						mergedTuples.add(topTuple);
					}
					topTuple = topTuplesIt.getOne();
				}
			}
		}

		return mergedTuples;
	}

}
