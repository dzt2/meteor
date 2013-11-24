package lab.meteor.test;

import lab.meteor.io.file.CsvFileImporter;
import lab.meteor.io.table.DataTable;


public class CSVTest {
	
	public static void main(String[] args) {
		CsvFileImporter cfi = new CsvFileImporter("data/test.csv");
		
		DataTable table;
		
		table = cfi.importTable();
		for (int j = 0; j < table.getColumns().size(); j++) {
			System.out.print(table.getColumns().get(j).getName());
			System.out.print("\t");
		}
		System.out.println();
		for (int i = 0; i < table.getTotalCount(); i++) {
			for (int j = 0; j < table.getColumns().size(); j++) {
				System.out.print(table.getValue(i, j));
				System.out.print("\t");
			}
			System.out.println();
		}
	}
}
