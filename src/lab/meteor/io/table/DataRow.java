package lab.meteor.io.table;

import java.util.HashMap;
import java.util.Map;

public class DataRow {

    int index = -1;

    final DataRowCollection rows;

    private Map<DataColumn, Object> itemMap = new HashMap<DataColumn, Object>();

    DataRow(DataRowCollection rows) {
        this.rows = rows;
    }

    public int getIndex() {
        return index;
    }
    
    public boolean setIndex(int index) {
		if (index == -1) return false;
		rows.move(this.index, index);
	    this.index = index;
	    return true;
	}

	public Object getValue(DataColumn column) {
		return itemMap.get(column);
	}

	public Object getValue(int columnIndex) {
		DataColumn c = rows.table.columns.get(columnIndex);
	    return itemMap.get(c);
	}

	public Object getValue(String columnName) {
		DataColumn c = rows.table.columns.get(columnName);
	    return itemMap.get(c);
	}

	public boolean setValue(DataColumn column, Object value) {
		if (column == null ||
				column.columns.table != rows.table ||
				column.index == -1) return false;
	    itemMap.put(column, value);
	    return true;
	}

	public boolean setValue(int index, Object value) {
		DataColumn c = rows.table.columns.get(index);
		itemMap.put(c, value);
		return true;
    }

    public boolean setValue(String columnName, Object value) {
        return setValue(rows.table.columns.get(columnName), value);
    }

    public void copyFrom(DataRow row) {
        this.itemMap.clear();
        for (DataColumn c : row.itemMap.keySet()) {
            this.itemMap.put(c, row.getValue(c));
        }
    }

    public String toString() {
        return "Row : " + index;
    }

	public DataRowCollection getRows() {
		return rows;
	}

	public DataTable getTable() {
	    return this.rows.table;
	}
}
