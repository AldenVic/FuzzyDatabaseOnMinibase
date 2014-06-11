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
import iterator.TopNestedLoopsJoin;
import iterator.TopRankJoin;
import iterator.TopSortMergeJoin;
import iterator.TopTAJoin;
import iterator.TupleUtils;
import iterator.TupleUtilsException;
import iterator.UnknowAttrType;
import iterator.UnknownKeyTypeException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import btree.BTreeFile;
import btree.IntegerKey;
import btree.StringKey;
import bufmgr.PageNotReadException;
import diskmgr.PCounter;





import java.awt.Color;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;




public class CSVUtilities {
	
	private static final String PREFIX = "table";
	// load csvFile information
	private int myIndex;
	private String fileLocation = null;
	private String fileName = null;
	private String[][] loadedFile = null;
	private int rowsTotal = 0;
	private int ColumnsTotal;
	private int total_str_cols = 0;
	private int[] cType = null;
	private int[] cType_algos = null;
	private short[] cSize = null;
	
	private int atLoc=0;//use to debug
	// database Attributes

	public AttrType[] cType_db = null;
	public AttrType[] cType_db_algos = null;
	
	private short[] cSize_db = null; 
	public short[] cSize_strs_Tuple = null;
	private short ColumnsTotal_short;
	private Tuple t = new Tuple(); 
	private int tupleSize;
	private Scan scan = null;
	private String heapFileName = null;
	private Heapfile hf = null;
	private String indexFileName = null;
	private BTreeFile if_b3 = null;
	
	private int addFileIndex=-1; 
	private int subFileIndex = -1;

	
	public int get_ColumnsTotal()
	{return ColumnsTotal;}
	public short get_ColumnsTotal_short()
	{return ColumnsTotal_short;}

	public int getTotal_str_cols()
	{
		return total_str_cols;
	}
	public short[] get_cSize_str_Tuple()
	{return  cSize_strs_Tuple;}	
	public short get_cSize_str_Tuple(int i)
	{return  cSize_strs_Tuple[i];}
	
	public int getTupleSize()
	{return tupleSize;}
	
    public AttrType[] getcType_db()
	{return cType_db;}
    public AttrType[] getcType_db_algos()
	{return cType_db_algos;}
	public int[] getcType()
	{return cType;}
	public short getcSize(int columnNum)
	{return cSize[columnNum];}
	
	public int[] getcType_algos() {
		return cType_algos;
	}
	
    public AttrType getAttrType(int k)
	{return cType_db[k];}
    public AttrType getAttrType_algos(int k)
	{return cType_db_algos[k];}
    
	public int getType(int a)
	{return cType[a];}
	public int getType_algos(int a)
	{return cType_algos[a];}
	
	public Tuple getTuple()
	{return t;}

	public int getTotalRows()
	 {return rowsTotal;}
	public String getFileLocation()
	{ return fileLocation;}
	public int getColCount()
	{return ColumnsTotal;}
	
	
	public String getFileName()
	{return fileName;}
	
	
	public String getHFName()
	{return heapFileName;}	
	public Heapfile getHF()
	{return hf;}
	
	public String getIndexFileName()
	{return indexFileName;}	
	public BTreeFile getIndexFile()
	{return if_b3;}

	CSVUtilities(int addFileIndex, int rows, int subFileIndex) {
		if (subFileIndex > -1)
			this.subFileIndex = subFileIndex;
		
		if (addFileIndex > -1)
			this.addFileIndex=addFileIndex;

		rowsTotal = rows;
	}

	CSVUtilities() {
		//this.addFileIndex=addFileIndex;
		
	} 
	 
	
	public void createFileInfo(javax.swing.text.Style style, StyledDocument doc) {
		if (addFileIndex == -1)
			{
			if (subFileIndex == -1)
				{myIndex = Utilities._totalFiles;
				rowsTotal = Utilities._rowsTotal;
				heapFileName = PREFIX + "_" + myIndex + ".in";
				indexFileName = PREFIX + "_" + myIndex +  "_indx_.in";
				}
			else
				{myIndex = subFileIndex;
				heapFileName = PREFIX + "_" + myIndex+"_sub" + ".in";
				indexFileName = PREFIX + "_" + myIndex+"_sub" + "_indx_.in";
				}	
			
			}
		else
			{
			myIndex = addFileIndex;
			heapFileName = PREFIX + "_" + myIndex+"_add" + ".in";
			indexFileName = PREFIX + "_" + myIndex+"_add" + "_indx_.in";
			}

		fileLocation = Utilities._fileLocation;
		
		fileName = Utilities._fileName;
		ColumnsTotal = Utilities._columnsTotal;
		ColumnsTotal_short = (short) (Utilities._columnsTotal - 1);
		total_str_cols = Utilities._total_str_cols;

		cType = new int[ColumnsTotal];
		cType_algos = new int[ColumnsTotal-1];
		cSize = new short[ColumnsTotal];
		cSize_db = new short[ColumnsTotal];
		cType_db = new AttrType[ColumnsTotal];
		cType_db_algos = new AttrType[ColumnsTotal-1];
		cSize_strs_Tuple = new short[total_str_cols];

		int indxx = 0;
		for (int i9 = 0; i9 < ColumnsTotal; i9++) {
			cType[i9] = Utilities._cType[i9];
			cSize[i9] = Utilities._cSize[i9];
			switch (cType[i9]) {
			case 0:
				cType_db[i9] = new AttrType(AttrType.attrString);
				cSize_db[i9] = cSize[i9];
				cSize_strs_Tuple[indxx++] = cSize[i9];
				break;
			case 1:
				cType_db[i9] = new AttrType(AttrType.attrInteger);
				break;
			case 2:
				cType_db[i9] = new AttrType(AttrType.attrReal);
				break;
			default:
				Utilities
						.debug_Util_color(
								"problem, arrType tht is not str, int or real:  you may want to re add this file",
								style, doc, Color.red);
				break;
			}

		}
		for (int i9 = 0; i9 < ColumnsTotal-1; i9++) {
			cType_db_algos[i9] = cType_db[i9];
			cType_algos[i9] = cType[i9];
		}

	
		Utilities.debug_Util_color("\nTuple Info:",style, doc, Color.black);;
		Utilities.debug_Util_color("\n    Tot Cols: " + ColumnsTotal_short, style, doc, Color.black);
		Utilities.debug_Util_color("\n       Types:",style, doc, Color.black);
			for(int y =0; y < ColumnsTotal; y++)
				Utilities.debug_Util_color("" + cType_db[y].attrType,style, doc, Color.black);
		Utilities.debug_Util_color("\n  StrSizeArr:", style, doc, Color.black);
			for(int y =0; y < total_str_cols; y++)
				Utilities.debug_Util_color("" + cSize_strs_Tuple[y],style, doc, Color.black);
		
		t = new Tuple();
		try {
			t.setHdr(ColumnsTotal_short, cType_db_algos, cSize_strs_Tuple);
		} catch (Exception e) {
			e.printStackTrace();
			Utilities.debug_Util_color("error in creating tuple header",
					style, doc, Color.red);

		}
		//debug_Util_color("\n scanned: " + t.getLength() + " " + t.noOfFlds() + "*" , style, doc, Color.green);
		
		tupleSize = t.size();
		Utilities.debug_Util_color("\n   TupleSize:" + tupleSize, style, doc, Color.black);
		
		 
			try {
				
			hf = new Heapfile(heapFileName);
			try {
				int recCount = 0;
				recCount = hf.getRecCnt();
				if (recCount > 0) {
					hf.deleteFile();
					hf = new Heapfile(heapFileName);
				}

			} catch (Exception e6) {
				StringWriter sw = new StringWriter();
				e6.printStackTrace(new PrintWriter(sw));
				debug_Util_color(
						"\n deleting existing hf/counting records of exiting hf: "
								+ heapFileName + sw.toString(), style, doc,
						Color.red);
			}

			Utilities.debug_Util_color("\n    HeapFile created:" + heapFileName, style, doc, Color.black);
				
		} catch (Exception e2) {
			Utilities.debug_Util_color(
					"re file: error creating Heapfile for: " + fileName
							+ " -> " + heapFileName, style, doc, Color.red);
			if(addFileIndex>-1)
				{Utilities._filesInDB_add[myIndex] = 0;}
			else
				{	if (subFileIndex>-1)
					{Utilities._filesInDB_sub[myIndex] = 0;}
					else
					{Utilities._filesInDB[myIndex] = 0;}
				}
			
			e2.printStackTrace();
		}
		
//		try {
//			//create index
//			if_b3 = new BTreeFile(indexFileName,AttrType.attrString, tupleSize, 1 /*delete*/ );
//			p3_Utilities_dmi.debug_Util_color("\n    IndexFile created:" + indexFileName, style, doc, Color.black);
//			
//		} catch (Exception e2) {
//			p3_Utilities_dmi.debug_Util_color(
//					"re file: error creating Indexfile for: " + fileName
//							+ " -> " + indexFileName, style, doc, Color.red);
//			if(addFileIndex>-1)
//				{p3_Utilities_dmi._filesInDB_add[myIndex] = 0;}
//			else
//				{	if (subFileIndex>-1)
//					{p3_Utilities_dmi._filesInDB_sub[myIndex] = 0;}
//					else
//					{p3_Utilities_dmi._filesInDB[myIndex] = 0;}
//				}
//			
//			e2.printStackTrace();
//		}
		
		
		
		
		t = new Tuple(tupleSize);
		
		try {
			t.setHdr(ColumnsTotal_short, cType_db_algos, cSize_strs_Tuple);
		} catch (Exception e) {
			e.printStackTrace();
			Utilities.debug_Util_color(
					"error in creating tuple header with tuple size", style,
					doc, Color.red);
		}
		
	}
	
	
	public void createFileInfo_add_notUsed(javax.swing.text.Style style, StyledDocument doc, int tableChosen) {
		myIndex = Utilities._totalFiles_add;
		
		fileLocation = Utilities._fileLocation_add;
		
		fileName = Utilities._fileName_add;
		rowsTotal = Utilities._rowsTotal_add;
		ColumnsTotal = Utilities.p3_csvFiles[tableChosen].getColCount();
		ColumnsTotal_short = Utilities.p3_csvFiles[tableChosen].get_ColumnsTotal_short();
		total_str_cols = Utilities.p3_csvFiles[tableChosen].getTotal_str_cols();

		cType = new int[ColumnsTotal];
		cType_algos = new int[ColumnsTotal];
		cSize = new short[ColumnsTotal];
		cSize_db = new short[ColumnsTotal];
		cType_db = new AttrType[ColumnsTotal];
		cType_db_algos = new AttrType[ColumnsTotal];
		cSize_strs_Tuple = new short[total_str_cols];

		int indxx = 0;
		for (int i9 = 0; i9 < ColumnsTotal; i9++) {
			cType[i9] = Utilities.p3_csvFiles[tableChosen].cType[i9];
			cSize[i9] = Utilities.p3_csvFiles[tableChosen].cSize[i9];
			switch (cType[i9]) {
			case 0:
				cType_db[i9] = new AttrType(AttrType.attrString);
				cSize_db[i9] = cSize[i9];
				cSize_strs_Tuple[indxx++] = cSize[i9];
				break;
			case 1:
				cType_db[i9] = new AttrType(AttrType.attrInteger);
				break;
			case 2:
				cType_db[i9] = new AttrType(AttrType.attrReal);
				break;
			default:
				Utilities
						.debug_Util_color(
								"problem, arrType tht is not str, int or real:  you may want to re add this file",
								style, doc, Color.red);
				break;
			}

		}
		for (int i9 = 0; i9 < ColumnsTotal; i9++) {
			cType_db_algos[i9] = cType_db[i9];
			cType_algos[i9] = cType[i9];
		}

	
		Utilities.debug_Util_color("\nTuple Info:",style, doc, Color.black);;
		Utilities.debug_Util_color("\n    Tot Cols: " + ColumnsTotal_short, style, doc, Color.black);
		Utilities.debug_Util_color("\n       Types:",style, doc, Color.black);
			for(int y =0; y < ColumnsTotal; y++)
				Utilities.debug_Util_color("" + cType_db[y].attrType,style, doc, Color.black);
		Utilities.debug_Util_color("\n  StrSizeArr:", style, doc, Color.black);
			for(int y =0; y < total_str_cols; y++)
				Utilities.debug_Util_color("" + cSize_strs_Tuple[y],style, doc, Color.black);
		
			
		t = new Tuple();
		try {// not needed for this object, we used the one in the caller adder table
			t.setHdr(ColumnsTotal_short, cType_db_algos, cSize_strs_Tuple);
		} catch (Exception e) {
			e.printStackTrace();
			Utilities.debug_Util_color("error in creating tuple header",
					style, doc, Color.red);

		}
		//debug_Util_color("\n scanned: " + t.getLength() + " " + t.noOfFlds() + "*" , style, doc, Color.green);
	
		
		tupleSize = t.size();
		Utilities.debug_Util_color("\n   TupleSize:" + tupleSize, style, doc, Color.black);
		if(addFileIndex>-1)
		{
			heapFileName = PREFIX + "_" + addFileIndex+"_add" + ".in";
		}
		else
		{
			heapFileName = PREFIX + "_" + myIndex + ".in";
		
			
		}
		
		Utilities.debug_Util_color("\n    HeapFile name:" + heapFileName, style, doc, Color.black);
		try {
			hf = new Heapfile(heapFileName);
		} catch (Exception e2) {
			Utilities.debug_Util_color(
					"re add file: error creating Heapfile: " + fileName
							+ " -> " + heapFileName, style, doc, Color.red);
			Utilities._filesInDB[myIndex] = 0;
			e2.printStackTrace();
		}
		t = new Tuple(tupleSize);

		try {
			t.setHdr(ColumnsTotal_short, cType_db_algos, cSize_strs_Tuple);
		} catch (Exception e) {
			e.printStackTrace();
			Utilities.debug_Util_color(
					"error in creating tuple header with tuple size", style,
					doc, Color.red);
		}
		
		
		
	}
	
	
	public void printInfo(javax.swing.text.Style style, StyledDocument doc)
	{ 	Utilities.debug_Util_color("\n" + myIndex + ": " + fileName + ": " + fileLocation, style, doc, Color.blue);
		Utilities.debug_Util("\nTotal Rows   : " + rowsTotal , style, doc);
			debug_Util_color("	TupleInfo:", style, doc, Color.blue);	
			
		Utilities.debug_Util("\nTotal Columns: "+ ColumnsTotal, style, doc);
			debug_Util_color("	Tot Cols: " + ColumnsTotal_short, style, doc, Color.blue);
		Utilities.debug_Util("\nColumn Types :", style, doc);	
		for(int i9 = 0; i9 < ColumnsTotal; i9++)
			{Utilities.debug_Util(" " + cType[i9], style, doc);}
			debug_Util_color("	Types:",style, doc, Color.blue);
				for(int y =0; y < ColumnsTotal; y++)
			{debug_Util_color("" + cType_db[y].attrType,style, doc, Color.blue);}
				
		Utilities.debug_Util("\nColumn sizes :", style, doc);
		for (int i9 = 0; i9 < ColumnsTotal; i9 ++)
			{Utilities.debug_Util(" " + cSize[i9] , style, doc);}
			debug_Util_color("	StrSizeArr:", style, doc, Color.blue);
				for(int y =0; y < total_str_cols; y++)
					{debug_Util_color("" + cSize_strs_Tuple[y],style, doc, Color.blue);}
			debug_Util_color("	TupleSize:" + tupleSize, style, doc, Color.blue);
			debug_Util_color("\n		HeapFile name:" + heapFileName, style, doc, Color.blue);
				
		Utilities.debug_Util("\n", style, doc);
		
		
	}
	
	public static void debug_Util(String str,javax.swing.text.Style style, StyledDocument doc) {
		////debug.append(str + "\n");
		///style = debug.addStyle("Green",null);
		StyleConstants.setForeground(style,Color.black);
		try {
			doc.insertString(doc.getLength(), str, style);
		} catch (BadLocationException e1) {			
			e1.printStackTrace();
		}
		
	}
	

	public static void debug_Util_color(String str,javax.swing.text.Style style, StyledDocument doc, Color fg ) {
		////debug.append(str + "\n");
		///style = debug.addStyle("Green",null);
	   StyleConstants.setForeground(style,fg);
		try {
			doc.insertString(doc.getLength(), str, style);
		} catch (BadLocationException e1) {			
			e1.printStackTrace();
		}
	  // StyleConstants.setForeground(style,Color.black);
		
	}


	
	
	}






