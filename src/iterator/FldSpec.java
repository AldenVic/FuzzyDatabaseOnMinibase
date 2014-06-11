package iterator;

public class FldSpec {

	public RelSpec relation;
	public int offset;
	public int table;

	/**
	 * constructor for binary join
	 * 
	 * @param _relation the relation is outer or inner
	 * @param _offset the offset of the field
	 */
	public FldSpec(RelSpec _relation, int _offset) {
		relation = _relation;
		offset = _offset;
	}

	/**
	 * constructor for multi-join
	 * 
	 * @param _table The table index
	 * @param _offset Column offset in that table
	 */
	public FldSpec(int _table, int _offset) {
		table = _table;
		offset = _offset;
	}

}
