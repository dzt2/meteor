package lab.meteor.io;

import java.io.IOException;

import lab.meteor.io.table.DataTable;

public interface TableLoader {
	DataTable loadTable() throws IOException;
}
