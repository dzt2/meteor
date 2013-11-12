package lab.meteor.io.table;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class DataRowCollection {

	List<DataRow> rows = new Vector<DataRow>();
	
	final DataTable table;

	DataRowCollection(DataTable table) {
		this.table = table;
	}
	
	public DataRow newRow() {
		return new DataRow(this);
	}

	public DataRow get(int index) {
		return rows.get(index);
	}

	public DataRow set(int index, DataRow row) {
		if (row == null || row.index != -1 || row.rows != this) return null;
		DataRow r = rows.set(index, row);
		r.index = -1;
		row.index = index;
		return r;
	}

	public int size() {
		return rows.size();
	}

	public boolean isEmpty() {
		return rows.isEmpty();
	}

	public void clear() {
		for (DataRow r : rows) {
			r.index = -1;
		}
		this.rows.clear();
	}
	
	public boolean add(DataRow row) {
		if (row == null || row.index != -1 || row.rows != this) return false;
		row.index = rows.size();
		rows.add(row);
		return true;
	}
	
	public boolean add(int index, DataRow row) {
		if (row == null || row.index != -1 || row.rows != this) return false;
		rows.add(index, row);
		reindexAfterAdd(index);
		row.index = index;
		return true;
	}

	public boolean remove(DataRow row) {
		if (row == null || row.index == -1 || row.rows != this) return false;
		rows.remove(row);
		reindexAfterRemove(row.index);
		row.index = -1;
		return true;
	}

	public DataRow remove(int index) {
		DataRow r = rows.remove(index);
		reindexAfterRemove(index);
		r.index = -1;
		return r;
	}

	public DataColumnCollection getColumns() {
		return table.columns;
	}
	
	public DataTable getTable() {
		return table;
	}

	public Iterator<DataRow> iterator() {
		// TODO
		return rows.iterator();
	}

	void move(int oldIndex, int newIndex) {
    	DataRow c = rows.remove(oldIndex);
    	rows.add(newIndex, c);
    	reindex(oldIndex, newIndex);
    }
	
	void reindexAfterAdd(int newIndex) {
    	reindex(rows.size() - 1, newIndex);
    }
    
    void reindexAfterRemove(int oldIndex) {
    	reindex(oldIndex, rows.size());
    }
	
	void reindex(int oldIndex, int newIndex) {
		if (oldIndex == newIndex)
    		return;
    	else if (oldIndex < newIndex) {
    		for (int i = oldIndex; i < newIndex; i++) {
    			rows.get(i).index = i;
    		}
    	} else {
    		for (int i = oldIndex; i > newIndex; i--) {
    			rows.get(i).index = i;
    		}
    	}
	}

}
