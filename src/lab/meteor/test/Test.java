package lab.meteor.test;

import lab.meteor.io.table.DataRow;
import lab.meteor.io.table.DataTable;


public class Test {
	
	public static void main(String[] args) {
		DataTable table = new DataTable("Test");
		table.getColumns().add(table.getColumns().newColumn("index"));
		table.getColumns().add(table.getColumns().newColumn("name"));
		table.getColumns().add(table.getColumns().newColumn("age"));
		table.getColumns().add(table.getColumns().newColumn("address"));
		table.getColumns().add(table.getColumns().newColumn("phone"));
		for (int i = 0; i < 10; i++) {
			DataRow dr = table.getRows().newRow();
			dr.setValue("index", i);
			dr.setValue("name", "apple" + i);
			dr.setValue("address", "add" + (10 - i));
			dr.setValue("phone", 1001010101);
			table.getRows().add(dr);
		}
		table.getColumns().get("name").setIndex(2);
		table.getRows().get(5).setIndex(9);
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
