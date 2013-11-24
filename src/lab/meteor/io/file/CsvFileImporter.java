package lab.meteor.io.file;

import java.io.FileNotFoundException;
import java.io.IOException;

import lab.meteor.io.TableImporter;
import lab.meteor.io.table.DataColumn;
import lab.meteor.io.table.DataRow;
import lab.meteor.io.table.DataTable;

import com.csvreader.*;

public class CsvFileImporter implements TableImporter {

	String filepath;
	
	CsvReader reader;
	
	public CsvFileImporter(String filepath) {
		this.filepath = filepath;
		try {
			reader = new CsvReader(filepath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public DataTable importTable() {
		DataTable table = new DataTable();
		try {
			reader.readHeaders();
			String[] headers = reader.getHeaders();
			for (String header : headers) {
				DataColumn column = table.newColumn(header);
				table.getColumns().add(column);
			}
			while (reader.readRecord()) {
				String[] values = reader.getValues();
				DataRow row = table.newRow();
				for (int i = 0; i < values.length; i++) {
					row.setValue(i, values[i]);
				}
				table.getRows().add(row);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return table;
	}

}
