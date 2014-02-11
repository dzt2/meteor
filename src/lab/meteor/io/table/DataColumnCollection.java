package lab.meteor.io.table;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class DataColumnCollection implements Iterable<DataColumn> {

    final Map<String, DataColumn> nameMap;

    final List<DataColumn> columns;
    
    final DataTable table;

    DataColumnCollection(DataTable table) {
    	this.table = table;
        this.nameMap = new LinkedHashMap<String, DataColumn>();
        this.columns = new Vector<DataColumn>();
    }
    
    public DataColumn get(int index) {
	    return this.columns.get(index);
	}

	public DataColumn get(String name) {
	    return this.nameMap.get(name);
	}

	public int size() {
        return this.columns.size();
    }

    public boolean isEmpty() {
	    return this.columns.isEmpty();
	}

	public void clear() {
		for (DataColumn c : columns) {
			c.index = -1;
		}
        this.columns.clear();
        this.nameMap.clear();
    }
    
    public boolean contains(String name) {
	    return this.nameMap.containsKey(name);
	}

	public boolean add(DataColumn column) {
    	if (column == null || column.index != -1 || column.columns != this) return false;
        if (!this.nameMap.containsKey(column.name)) {
            column.index = this.columns.size();
            this.nameMap.put(column.name, column);
            this.columns.add(column);
            // not require to re-index
            return true;
        }
        return false;
    }

    public boolean add(int index, DataColumn column) {
    	if (column == null || column.index != -1 || column.columns != this) return false;
        if (!nameMap.containsKey(column.name)) {
            columns.add(index, column);
            nameMap.put(column.name, column);
            reindexAfterAdd(index);
            column.index = index;
            return true;
        }
        return false;
    }
    
    public boolean remove(DataColumn column) {
    	if (column == null || column.index == -1 || column.columns != this) return false;
	    if (this.nameMap.containsKey(column.name)) {
	        this.columns.remove(column.index);
	        this.nameMap.remove(column.name);
	        reindexAfterRemove(column.index);
	        column.index = -1;
	        return true;
	    }
	    return false;
	}

	public DataColumn remove(int index) {
	    DataColumn column = columns.remove(index);
	    this.nameMap.remove(column.name);
	    reindexAfterRemove(index);
	    column.index = -1;
	    return column;
	}

	public DataColumn remove(String columnName) {
		DataColumn column = nameMap.remove(columnName);
		if (column == null) return null;
	    columns.remove(column.index);
	    reindexAfterRemove(column.index);
	    column.index = -1;
	    return column;
	}

	public DataColumn set(int index, DataColumn column) {
		if (column == null || column.index != -1 || column.columns != this) return null;
		DataColumn c = columns.set(index, column);
		c.index = -1;
		column.index = index;
	    return c;
	}

	public DataColumn newColumn(String name) {
	    return new DataColumn(name, this);
	}

	void move(int oldIndex, int newIndex) {
    	DataColumn c = columns.remove(oldIndex);
    	columns.add(newIndex, c);
    	reindex(oldIndex, newIndex);
    }
    
    void reindex(int oldIndex, int newIndex) {
    	if (oldIndex == newIndex)
    		return;
    	else if (oldIndex < newIndex) {
    		for (int i = oldIndex; i < newIndex; i++) {
    			// remove
    			columns.get(i).index = i;
    		}
    	} else {
    		// add
    		for (int i = oldIndex; i > newIndex; i--) {
    			columns.get(i).index = i;
    		}
    	}
    }
    
    void reindexAfterAdd(int newIndex) {
    	reindex(columns.size() - 1, newIndex);
    }
    
    void reindexAfterRemove(int oldIndex) {
    	reindex(oldIndex, columns.size());
    }
    
    public DataTable getTable() {
    	return this.table;
    }

    public Iterator<DataColumn> iterator() {
    	// TODO
        return this.columns.iterator();
    }
}