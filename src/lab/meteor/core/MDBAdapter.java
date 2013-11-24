package lab.meteor.core;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeMap;

import lab.meteor.core.MElement.MElementType;

/**
 * The adapter between the database and the system. Meteor system operates the data with this
 * adapter. So the adapter provides a way of extension. 
 * @author Qiang
 *
 */
public interface MDBAdapter {
	
	/**
	 * 
	 * @author Qiang
	 *
	 */
	public static abstract class DBInfo {
		public long id;
		int flag;
		
		DBInfo() { flag = MElement.FULL_ATTRIB_FLAG; }
		
		DBInfo(int flag) { this.flag = flag; }
		
		void flag(int attrib_flag) {
			flag |= attrib_flag;
		}
		
		public boolean isFlagged(int attrib_flag) {
			return (flag & attrib_flag) != 0;
		}
		
		public boolean noFlag() {
			return flag == 0;
		}
	}
	
	/**
	 * 
	 * @author Qiang
	 *
	 */
	public static class PackageDBInfo extends DBInfo {
		public String name;
		public long package_id;
		PackageDBInfo() { super(); }
		PackageDBInfo(int flag) { super(flag); }
	}
	
	void loadPackage(PackageDBInfo pkg);
	void createPackage(PackageDBInfo pkg);
	void updatePackage(PackageDBInfo pkg);
	void deletePackage(PackageDBInfo pkg);
	
	public static class ClassDBInfo extends DBInfo {
		public String name;
		public long package_id;
		public long superclass_id;
		ClassDBInfo() { super(); }
		ClassDBInfo(int flag) { super(flag); }
	}
	
	void loadClass(ClassDBInfo cls);
	void createClass(ClassDBInfo cls);
	void updateClass(ClassDBInfo cls);
	void deleteClass(ClassDBInfo cls);
	
	public static class AttributeDBInfo extends DBInfo {
		public String name;
		public String type_id;
		public long class_id;
		AttributeDBInfo() { super(); }
		AttributeDBInfo(int flag) { super(flag); }
	}
	
	void loadAttribute(AttributeDBInfo atb);
	void createAttribute(AttributeDBInfo atb);
	void updateAttribute(AttributeDBInfo atb);
	void deleteAttribute(AttributeDBInfo atb);
	
	public static class ReferenceDBInfo extends DBInfo {
		public long class_id;
		public String name;
		public long reference_id;
		public MReference.Multiplicity multi;
		public long opposite_id;
		ReferenceDBInfo() { super(); }
		ReferenceDBInfo(int flag) { super(flag); }
	}
	
	void loadReference(ReferenceDBInfo rol);
	void createReference(ReferenceDBInfo rol);
	void updateReference(ReferenceDBInfo rol);
	void deleteReference(ReferenceDBInfo rol);
	
	public static class EnumDBInfo extends DBInfo {
		public String name;
		public long package_id;
		EnumDBInfo() { super(); }
		EnumDBInfo(int flag) { super(flag); }
	}
	
	void loadEnum(EnumDBInfo enm);
	void createEnum(EnumDBInfo enm);
	void updateEnum(EnumDBInfo enm);
	void deleteEnum(EnumDBInfo enm);
	
	public static class SymbolDBInfo extends DBInfo {
		public String name;
		public long enum_id;
		SymbolDBInfo() { super(); }
		SymbolDBInfo(int flag) { super(flag); }
	}
	
	void loadSymbol(SymbolDBInfo syb);
	void createSymbol(SymbolDBInfo syb);
	void updateSymbol(SymbolDBInfo syb);
	void deleteSymbol(SymbolDBInfo syb);
	
	public static class ObjectDBInfo extends DBInfo {
		public long class_id;
		public DataDict values = new DataDict();
		public DataDict deleteKeys = new DataDict();
		ObjectDBInfo() { super(); }
		ObjectDBInfo(int flag) { super(flag); }
	}

	void loadObject(ObjectDBInfo obj);
	void createObject(ObjectDBInfo obj);
	void updateObject(ObjectDBInfo obj);
	void deleteObject(ObjectDBInfo obj);
	
	public static class TagDBInfo extends DBInfo {
		public String name;
		public Object value;
		TagDBInfo() { super(); }
		TagDBInfo(int flag) { super(flag); }
	}
	
	void loadTag(TagDBInfo tag);
	void createTag(TagDBInfo tag);
	void updateTag(TagDBInfo tag);
	void deleteTag(TagDBInfo tag);
	
	void loadTagElements(long id, IDList list);
	void saveTagElements(long id, IDList list);
	
	void loadElementTags(long id, IDList list);
	void saveElementTags(long id, IDList list);
	
	MElementType getElementType(long id);
	
	void checkAndPrepareDB();
	long loadLastIDAndIncrement();
	void resetDB();

	IDList listAllPackageIDs();
	IDList listAllClassIDs();
	IDList listAllAttributeIDs();
	IDList listAllReferenceIDs();
	IDList listAllEnumIDs();
	IDList listAllSymbolIDs();
	IDList listAllObjectIDs(long classID);
	
	void deleteAllObjects(long classID);
	long getObjectClass(long obj_id);
	
	/**
	 * A list of object in meteor system. It's a linked list.
	 * @author Qiang
	 *
	 */
	public static class DataList extends LinkedList<Object> {
		private static final long serialVersionUID = 4119666467334909410L;
	}
	
	/**
	 * A dictionary of object in meteor system. It's a tree map.
	 * @author Qiang
	 *
	 */
	public static class DataDict extends TreeMap<String, Object> {
		private static final long serialVersionUID = -3370705807605089825L;
	}
	
	/**
	 * A set of object in meteor system. It's a tree set.
	 * @author Qiang
	 *
	 */
	public static class DataSet extends HashSet<Object> {
		private static final long serialVersionUID = 8312477680744554417L;
	}
	
	/**
	 * A list of element ID. It's a linked list.
	 * @author Qiang
	 *
	 */
	public static class IDList extends LinkedList<Long> {
		private static final long serialVersionUID = 4824752584421148743L;
	}
}
