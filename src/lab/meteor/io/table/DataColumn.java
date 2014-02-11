package lab.meteor.io.table;

public class DataColumn extends DataTagable {
	 
    final DataColumnCollection columns;
 
    String name;
  
    // The position or index of this column in its Column Collection!
    int index = -1;
 
    DataColumn(DataColumnCollection columns) {
        this("", columns);
    }
 
    public DataColumn(String name, DataColumnCollection columns) {
    	this.name = name;
    	this.columns = columns;
    }
 
    public String getName() {
        return this.name;
    }
 
    public boolean setName(String name) throws Exception {
    	if (columns.nameMap.containsKey(name))
    		return false;
    	if (index != -1)
    		columns.nameMap.remove(this.name);
        this.name = name;
        if (index != -1)
        	columns.nameMap.put(this.name, this);
        return true;
    }
 
    public int getIndex() {
	    return index;
	}

	public boolean setIndex(int index) {
		boolean has = columns.nameMap.containsKey(this.name);
		if (!has) return false;
	    columns.move(this.index, index);
	    this.index = index;
	    return true;
	}

	@Override
    public String toString(){
        return "DataColumn : " + this.name;
    }

	public DataColumnCollection getColumns() {
		return this.columns;
	}

	public DataTable getTable() {
	    return this.columns.table;
	}
	
}