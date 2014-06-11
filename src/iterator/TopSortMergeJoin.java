package iterator;

import global.AttrType;
import global.TupleOrder;
import heap.Heapfile;
import heap.InvalidTupleSizeException;
import heap.InvalidTypeException;
import heap.Scan;
import heap.Tuple;
import index.IndexException;

import java.io.IOException;
import java.sql.Driver;

import tests.MainDriver;

import bufmgr.PageNotReadException;

public class TopSortMergeJoin extends Iterator {

	private AttrType _in1[], _in2[];
	private int _in1Len, _in2Len;
	private Iterator _outerIt;
	private String _innerRelationName;
	private short _t1StrSizes[];
	private short _t2StrSizes[];
	private CondExpr _outputFilter[];
	private CondExpr _rightFilter[];
	private int _bufCount; // # of buffer pages available.
	private boolean _done, // Is the join complete
	_getFromOuter; // if TRUE, a tuple is got from outer
	private Tuple _outerTuple, _innerTuple;
	private Tuple _jTuple; // Joined tuple
	private FldSpec _outFlds[];
	private int _outFldCount;
	private Heapfile _hf;
	private Iterator _innerIt;
	private int _in1JoinCol;
	private int _in1SortFieldIndex;
	private int _in2JoinCol;
	private int _in2SortFieldIndex;
	private boolean _in1Sorted;
	private boolean _in2Sorted;
	private TupleOrder _order;
	private SortMerge _smIt;
	private Sort _sortedIt;


	/**constructor
	 *Initialize the two relations which are joined, including relation type,
	 *@param in1  Array containing field types of R.
	 *@param len_in1  # of columns in R.
	 *@param t1_str_sizes shows the length of the string fields.
	 *@param in2  Array containing field types of S
	 *@param len_in2  # of columns in S
	 *@param  t2_str_sizes shows the length of the string fields.
	 *@param amt_of_mem  IN PAGES
	 *@param am1  access method for left i/p to join
	 *@param relationName  access hfapfile for right i/p to join
	 *@param outFilter   select expressions
	 *@param rightFilter reference to filter applied on right i/p
	 *@param proj_list shows what input fields go where in the output tuple
	 *@param n_out_flds number of outer relation fileds
	 *@exception IOException some I/O fault
	 *@exception NestedLoopException exception from this class
	 * @throws TupleUtilsException 
	 * @throws SortException 
	 * @throws JoinLowMemory 
	 * @throws JoinNewFailed 
	 */
	public TopSortMergeJoin (AttrType    in1[],               
			   int     len_in1,                        
			   short   t1_str_sizes[],
			   AttrType    in2[],                
			   int     len_in2,                        
			   short   t2_str_sizes[],
			   
			   int     join_col_in1,                
			   int      sortFld1Len,
			   int     join_col_in2,                
			   int      sortFld2Len,
			   
			   int     amt_of_mem,               
			   Iterator     am1,                
			   Iterator     am2,                
			   
			   boolean     in1_sorted,                
			   boolean     in2_sorted,                
			   TupleOrder order,
			   
			   CondExpr  outFilter[],                
			   FldSpec   proj_list[],
			   int       n_out_flds
			   ) throws IOException,NestedLoopException, JoinNewFailed, JoinLowMemory, SortException, TupleUtilsException {

		_in1 = new AttrType[in1.length];
		_in2 = new AttrType[in2.length];
		System.arraycopy(in1,0,_in1,0,in1.length);
		System.arraycopy(in2,0,_in2,0,in2.length);
		_in1Len = len_in1;
		_in2Len = len_in2;


		_outerIt = am1;
		_t1StrSizes = t1_str_sizes;
		_t2StrSizes =  t2_str_sizes;
		_innerTuple = new Tuple();
		_jTuple = new Tuple();
		_outputFilter = outFilter;

		_bufCount    = amt_of_mem;
		_innerIt = null;
		_done  = false;
		_getFromOuter = true;

		_innerIt = am2;
		_in1JoinCol = join_col_in1;
		_in1SortFieldIndex = sortFld1Len;
		_in2JoinCol = join_col_in2;
		_in2SortFieldIndex = sortFld2Len;
		_in1Sorted = in1_sorted;
		_in2Sorted = in2_sorted;
		_order = order;
		_outFlds = proj_list;
		_outFldCount = n_out_flds;

		_smIt = new SortMerge(_in1, _in1Len, _t1StrSizes, _in2, _in2Len, _t2StrSizes, _in1JoinCol,
				_in1SortFieldIndex, _in2JoinCol, _in2SortFieldIndex, _bufCount, _outerIt, _innerIt,
				_in1Sorted, _in2Sorted, _order, _outputFilter, _outFlds, _outFldCount);

		AttrType[] outputAttrTypes = new AttrType[_outFldCount + 1];
		short[] outputStrSizes = TupleUtils.getOPAttrInfo(outputAttrTypes, _in1, _in1Len,
				_in2, _in2Len, _t1StrSizes, _t2StrSizes, _outFlds, _outFldCount);
		
		//System.out.println(MainDriver.printIterator(_smIt, outputAttrTypes));
		outputAttrTypes[_outFldCount] = new AttrType(AttrType.attrReal);
		
		_sortedIt = new Sort(outputAttrTypes, (short)outputAttrTypes.length, outputStrSizes,
				_smIt, _outFldCount + 1, new TupleOrder(TupleOrder.Descending), 4, _bufCount, true);

	}

	/**  
	 *@return The joined tuple is returned
	 *@exception IOException I/O errors
	 *@exception JoinsException some join exception
	 *@exception IndexException exception from super class
	 *@exception InvalidTupleSizeException invalid tuple size
	 *@exception InvalidTypeException tuple type not valid
	 *@exception PageNotReadException exception from lower layer
	 *@exception TupleUtilsException exception from using tuple utilities
	 *@exception PredEvalException exception from PredEval class
	 *@exception SortException sort exception
	 *@exception LowMemException memory error
	 *@exception UnknowAttrType attribute type unknown
	 *@exception UnknownKeyTypeException key type unknown
	 *@exception Exception other exceptions

	 */
	public Tuple get_next()
			throws IOException,
			JoinsException ,
			IndexException,
			InvalidTupleSizeException,
			InvalidTypeException, 
			PageNotReadException,
			TupleUtilsException, 
			PredEvalException,
			SortException,
			LowMemException,
			UnknowAttrType,
			UnknownKeyTypeException,
			Exception {
		return _sortedIt.get_next();
	} 

	/**
	 * implement the abstract method close() from super class Iterator
	 *to finish cleaning up
	 *@exception IOException I/O error from lower layers
	 *@exception JoinsException join error from lower layers
	 *@exception IndexException index access error 
	 */
	public void close() throws JoinsException, IOException,IndexException 
	{
		if (!closeFlag) {

			try {
				_outerIt.close();
			}catch (Exception e) {
				throw new JoinsException(e, "NestedLoopsJoin.java: error in closing iterator.");
			}
			closeFlag = true;
		}
	}
}
