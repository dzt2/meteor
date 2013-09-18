package lab.meteor.core;

import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;

import lab.meteor.core.MElement.MElementType;

public interface MDBAdapter {
	
	public static class PackageDBInfo {
		public long id;
		public String name;
		public long package_id;
	}
	
	void loadPackage(PackageDBInfo pkg);
	void createPackage(PackageDBInfo pkg);
	void updatePackage(PackageDBInfo pkg);
	void deletePackage(PackageDBInfo pkg);
	
	public static class ClassDBInfo {
		public long id;
		public String name;
		public long package_id;
		public long superclass_id;
	}
	
	void loadClass(ClassDBInfo cls);
	void createClass(ClassDBInfo cls);
	void updateClass(ClassDBInfo cls);
	void deleteClass(ClassDBInfo cls);
	
	public static class AttributeDBInfo {
		public long id;
		public String name;
		public String type_id;
		public long class_id;
	}
	
	void loadAttribute(AttributeDBInfo atb);
	void createAttribute(AttributeDBInfo atb);
	void updateAttribute(AttributeDBInfo atb);
	void deleteAttribute(AttributeDBInfo atb);
	
	public static class ReferenceDBInfo {
		public long id;
		public long class_id;
		public String name;
		public long reference_id;
		public MReference.Multiplicity multi;
		public long opposite_id;
	}
	
	void loadReference(ReferenceDBInfo rol);
	void createReference(ReferenceDBInfo rol);
	void updateReference(ReferenceDBInfo rol);
	void deleteReference(ReferenceDBInfo rol);
	
	public static class EnumDBInfo {
		public long id;
		public String name;
		public long package_id;
	}
	
	void loadEnum(EnumDBInfo enm);
	void createEnum(EnumDBInfo enm);
	void updateEnum(EnumDBInfo enm);
	void deleteEnum(EnumDBInfo enm);
	
	public static class SymbolDBInfo {
		public long id;
		public String name;
		public long enum_id;
	}
	
	void loadSymbol(SymbolDBInfo syb);
	void createSymbol(SymbolDBInfo syb);
	void updateSymbol(SymbolDBInfo syb);
	void deleteSymbol(SymbolDBInfo syb);
	
	public static class ObjectDBInfo {
		public long id;
		public long class_id;
		public DataDict values = new DataDict();
	}

	void loadObject(ObjectDBInfo obj);
	void createObject(ObjectDBInfo obj);
	void updateObject(ObjectDBInfo obj);
	void deleteObject(ObjectDBInfo obj);
	
	public static class TagDBInfo {
		public long id;
		public String name;
		public Object value;
		public IDList targets_id = new IDList();
	}
	
	void loadTag(TagDBInfo tag);
	void createTag(TagDBInfo tag);
	void updateTag(TagDBInfo tag);
	void deleteTag(TagDBInfo tag);
	
	public static class ElementTagDBInfo {
		public long id;
		public IDList tags_id = new IDList();
	}
	
	void loadElementTags(ElementTagDBInfo dbInfo);
	void saveElementTags(ElementTagDBInfo dbInfo);
	
	MElementType getElementType(long id);
	
	void checkAndPrepareDB();
	long loadLastIDAndIncrement();
	void resetDB();

	IDList listAllPackageIDs();
	IDList listAllClassIDs();
	IDList listAllAttributeIDs();
	IDList listAllEnumIDs();
	IDList listAllSymbolIDs();
	IDList listAllObjectIDs(long classID);
	
	@SuppressWarnings("serial")
	public static class DataList extends LinkedList<Object> {
		
	}
	
	@SuppressWarnings("serial")
	public static class DataDict extends TreeMap<String, Object> {
		
	}
	
	@SuppressWarnings("serial")
	public static class DataSet extends TreeSet<Object> {
		
	}
	
	@SuppressWarnings("serial")
	public static class IDList extends LinkedList<Long> {
		
	}
}
