package lab.meteor.test;

import java.io.FileNotFoundException;
import java.io.IOException;

import lab.meteor.io.file.CsvFileLoader;
import lab.meteor.io.table.DataTable;


public class CSVTest {
	
	public static void main(String[] args) {
		CsvFileLoader cfi;
		try {
			cfi = new CsvFileLoader("data/test.csv");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		DataTable table;
		
		try {
			table = cfi.loadTable();
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String a = "Interface.name (new)".split(" ", 2)[0];
		System.out.println(a);
	}
}
