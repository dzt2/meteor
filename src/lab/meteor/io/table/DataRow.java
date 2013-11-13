package lab.meteor.io.table;

import java.util.LinkedHashMap;
import java.util.Map;

public class DataRow {

    int index = -1;

    final DataRowCollection rows;

    private Map<DataColumn, Object> values;

    DataRow(DataRowCollection rows) {
        this.rows = rows;
        values = new LinkedHashMap<DataColumn, Object>();
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
		return values.get(column);
	}

	public Object getValue(int columnIndex) {
		DataColumn c = rows.table.columns.get(columnIndex);
	    return values.get(c);
	}

	public Object getValue(String columnName) {
		DataColumn c = rows.table.columns.get(columnName);
	    return values.get(c);
	}

	public boolean setValue(DataColumn column, Object value) {
		if (column == null ||
				column.columns.table != rows.table ||
				column.index == -1) return false;
	    values.put(column, value);
	    return true;
	}

	public boolean setValue(int index, Object value) {
		DataColumn c = rows.table.columns.get(index);
		values.put(c, value);
		return true;
    }

    public boolean setValue(String columnName, Object value) {
        return setValue(rows.table.columns.get(columnName), value);
    }

    public void copyFrom(DataRow row) {
        this.values.clear();
        for (DataColumn c : row.values.keySet()) {
            this.values.put(c, row.getValue(c));
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
