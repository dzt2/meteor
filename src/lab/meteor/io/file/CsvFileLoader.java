package lab.meteor.io.file;

import java.io.FileNotFoundException;
import java.io.IOException;

import lab.meteor.io.TableLoader;
import lab.meteor.io.table.DataColumn;
import lab.meteor.io.table.DataRow;
import lab.meteor.io.table.DataTable;

import com.csvreader.*;

public class CsvFileLoader implements TableLoader {

	String filepath;
	
	CsvReader reader;
	
	public CsvFileLoader(String filepath) throws FileNotFoundException {
		this.filepath = filepath;
		reader = new CsvReader(filepath);
	}

	@Override
	public DataTable loadTable() throws IOException {
		DataTable table = new DataTable();
		// For Column
		reader.readHeaders();
		String[] headers = reader.getHeaders();
		for (String header : headers) {
			DataColumn column = table.newColumn(header);
			table.getColumns().add(column);
		}
		
		// For Rows
		while (reader.readRecord()) {
			String[] values = reader.getValues();
			DataRow row = table.newRow();
			for (int i = 0; i < values.length; i++) {
				System.out.println(values[i]);
				row.setValue(i, values[i]);
			}
			table.getRows().add(row);
		}
		return table;
	}

}
