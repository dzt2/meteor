package lab.meteor.io;

import java.io.IOException;

import lab.meteor.io.table.DataTable;

public interface TableLoader {
	// To Load a DataTable from a outside resource.
	DataTable loadTable() throws IOException;
}
