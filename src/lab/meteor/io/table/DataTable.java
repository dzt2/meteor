package lab.meteor.io.table;

public final class DataTable {

    final DataRowCollection rows;
    final DataColumnCollection columns;
    
    private String name;
    
    public DataTable() {
        this("");
    }

    public DataTable(String name) {
        this.name = name;
        this.columns = new DataColumnCollection(this);
        this.rows = new DataRowCollection(this);
    }

    public int getTotalCount() {
        return rows.size();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DataRowCollection getRows() {
        return this.rows;
    }

    public DataColumnCollection getColumns() {
        return this.columns;
    }

    public Object getValue(int row, String colName) {
        return rows.get(row).getValue(colName);
    }

    public Object getValue(int row, int col) {
        return rows.get(row).getValue(col);
    }

    public boolean setValue(int row, int col, Object value) {
        return rows.get(row).setValue(col, value);
    }

    public boolean setValue(int row, String colName, Object value) {
        return rows.get(row).setValue(colName, value);
    }
    
    public DataRow newRow() {
    	return rows.newRow();
    }
    
    public DataColumn newColumn(String name) {
    	return columns.newColumn(name);
    }
    
}
