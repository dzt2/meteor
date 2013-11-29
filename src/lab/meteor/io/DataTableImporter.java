package lab.meteor.io;

import lab.meteor.core.MClass;
import lab.meteor.core.MEnum;
import lab.meteor.core.MObject;
import lab.meteor.core.MPackage;
import lab.meteor.core.MPrimitiveType;
import lab.meteor.core.MProperty;
import lab.meteor.core.MElement.MElementType;
import lab.meteor.core.MReference;
import lab.meteor.core.MReference.Multiplicity;
import lab.meteor.core.MUtility;
import lab.meteor.io.table.DataColumn;
import lab.meteor.io.table.DataRow;
import lab.meteor.io.table.DataTable;

public class DataTableImporter extends Importer<DataTable> {

	MPackage pkg;
	
	Result r = new Result();
	
	public Result getResult() {
		return r;
	}
	
	public void setPackage(MPackage pkg) {
		this.pkg = pkg;
	}
	
	public MPackage getPackage() {
		return pkg;
	}
	
	@Override
	public void importData(DataTable data) {
		int errorCount = 0;
		boolean createMode = false;
		DataColumn firstColumn = data.getColumns().get(0);
		if (firstColumn.getName().contains("(new)"))
			createMode = true;
		String first = firstColumn.getName().split(" ", 2)[0];
		String[] tokens = first.split("\\.");
		MClass cls = pkg.getClazz(tokens[0]);
		if (cls == null) {
			r.message = "class is not found.";
			r.state = ResultState.Error;
			return;
		}
		MProperty p = cls.getProperty(tokens[1]);
		if (p == null) {
			r.message = "property at column 0 is not found.";
			r.state = ResultState.Error;
			return;
		}
		if (p.getType() != MPrimitiveType.String) {
			r.message = "type of property at column 0 should be string.";
			r.state = ResultState.Error;
			return;
		}
		firstColumn.getColumns().get(0).setTag("index", new StringIndexer(cls, p.getName()));
		
		firstColumn.setTag("property", p);
		for (int i = 1; i < data.getColumns().size(); i++) {
			DataColumn column = data.getColumns().get(i);
			tokens = column.getName().split("\\.");
			if (!tokens[0].equals(cls.getName())) {
				r.message = "class in each column is inconsistence.";
				r.state = ResultState.Error;
				return;
			}
			p = cls.getProperty(tokens[1]);
			if (p == null) {
				r.message = "property at column " + i + " is not found.";
				r.state = ResultState.Error;
				return;
			}
			column.setTag("property", p);
			if (p.getElementType() == MElementType.Reference) {
				if (tokens.length < 3) {
					r.message = "it's necessary point out the index"
							+ " property of reference property, at column " + i + ".";
					r.state = ResultState.Error;
					return;
				}
				MProperty p2 = ((MReference) p).getReference().getProperty(tokens[2]);
				if (p2 == null) {
					r.message = "index property at column " + i + " is not found.";
					r.state = ResultState.Error;
					return;
				}
				if (p2.getType() != MPrimitiveType.String) {
					r.message = "index property should be string type.";
					r.state = ResultState.Error;
					return;
				}
				StringIndexer si = new StringIndexer(((MReference)p).getReference(), p2.getName());
				column.setTag("index", si);
			}
		}
		for (int i = 0; i < data.getColumns().size(); i++) {
			StringIndexer si = (StringIndexer) data.getColumns().get(i).getTag("index");
			if (si != null)
				si.build();
		}
		
		StringIndexer sindex = (StringIndexer) data.getColumns().get(0).getTag("index");
		for (int i = 0; i < data.getTotalCount(); i++) {
			DataRow row = data.getRows().get(i);
			String idname = (String) row.getValue(0);
			MObject obj = sindex.find(idname);
			if (obj == null) {
				if (createMode) {
					obj = new MObject(cls);
					MProperty pp = (MProperty) data.getColumns().get(0).getTag("property");
					obj.set(pp.getName(), idname);
					sindex.append(idname, obj);
				} else {
					row.setTag("error", 0);
					errorCount++;
					continue;
				}
			}
			for (int j = 1; j < data.getColumns().size(); j++) {
				String raw = (String) row.getValue(j);
				Object content = null;
				MProperty pp = (MProperty) data.getColumns().get(j).getTag("property");
				if (pp.getType() == MPrimitiveType.String) {
					content = raw;
				} else if (pp.getType() == MPrimitiveType.Integer) {
					content = MUtility.stringToInteger(raw);
				} else if (pp.getType() == MPrimitiveType.Boolean) {
					content = MUtility.stringToBoolean(raw);
				} else if (pp.getType() == MPrimitiveType.Int64) {
					content = MUtility.stringToInt64(raw);
				} else if (pp.getType() == MPrimitiveType.Binary) {
					content = MUtility.stringToBoolean(raw);
				} else if (pp.getType() == MPrimitiveType.Number) {
					content = MUtility.stringToNumber(raw);
				} else if (pp.getType() == MPrimitiveType.DateTime) {
					content = MUtility.stringToDateTime(raw, "yyyy-MM-dd HH:mm:ss");
				} else if (pp.getType() instanceof MEnum) {
					MEnum e = (MEnum) pp.getType();
					content = e.getSymbol(raw);
				} else if (pp.getType() instanceof MClass) {
					StringIndexer si = (StringIndexer) data.getColumns().get(j).getTag("index");
					content = si.find(raw);
				} else {
					r.state = ResultState.Error;
					r.message = "fatel error.";
					System.out.println(pp.getType());
					throw new RuntimeException("DataTable Importer: not support data type.");
				}
				if (content != null) {
					if (pp.getElementType() == MElementType.Attribute)
						obj.set(pp.getName(), content);
					else {
						MReference r = (MReference) pp;
						if (r.getMultiplicity() == Multiplicity.Multiple) {
							obj.getReferences(pp.getName()).add((MObject) content);
						} else {
							obj.setReference(pp.getName(), (MObject) content);
						}
					}
				} else {
					row.setTag("error", null);
					errorCount++;
				}
			}
			this.makeProgress(i, data.getTotalCount());
		}
		for (int i = 0; i < data.getColumns().size(); i++) {
			data.getColumns().get(i).clearTags();
		}
		if (errorCount == 0) {
			r.state = ResultState.Success;
			r.message = "finished.";
		} else {
			r.state = ResultState.Warning;
			r.message = "there are " + errorCount + " problems while importing.";
		}
	}

	public class Result {
		private Result() {  }
		String message;
		ResultState state;
		
		public String getMessage() {
			return message;
		}
		
		public ResultState getResultState() {
			return state;
		}
	}
	
	public enum ResultState {
		Error,
		Warning,
		Success
	}
	
}
