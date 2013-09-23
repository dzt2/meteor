package lab.meteor.dba;

import java.util.Iterator;
import java.util.Map.Entry;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import lab.meteor.core.MDBAdapter;
import lab.meteor.core.MElement.MElementType;
import lab.meteor.core.MElementPointer;
import lab.meteor.core.MException;
import lab.meteor.core.MReference.Multiplicity;
import lab.meteor.core.MUtility;

public class MongoDBAdapter implements MDBAdapter {

	private static final String COLLECT_NAME_GLOBAL = "global";
	
	private static final String COLLECT_NAME_ELEMENT = "elements";
	private static final String COLLECT_NAME_TAG = "tags";
	
	private static final String COLLECT_NAME_CLASS = "classes";
	private static final String COLLECT_NAME_ATTRIBUTE = "attributes";
	private static final String COLLECT_NAME_REFERENCE = "references";
	private static final String COLLECT_NAME_ENUM = "enumes";
	private static final String COLLECT_NAME_SYMBOL = "symbols";
	private static final String COLLECT_NAME_PACKAGE = "packages";
	
	public static boolean ENABLE_DOUBLE_CHECK_EXISTENCE = true;
	
	private DB db;
	
	public MongoDBAdapter(DB db) {
		this.db = db;
	}
	
	public void setDB(DB db) {
		this.db = db;
	}
	
	private void writeElementType(long id, MElementType type) {
		DBCollection eleCol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject typeObj = new BasicDBObject();
		typeObj.put("_id", id);
		typeObj.put("type", type.toString());
		eleCol.insert(typeObj);
	}
	
	private void writeObject(long id, long class_id) {
		DBCollection eleCol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject typeObj = new BasicDBObject();
		typeObj.put("_id", id);
		typeObj.put("type", MElementType.Object.toString());
		typeObj.put("class", classIDToString(class_id));
		eleCol.insert(typeObj);
	}
	
	@Override
	public void loadPackage(PackageDBInfo pkg) {
		// load
		DBCollection pkgCol = db.getCollection(COLLECT_NAME_PACKAGE);
		DBObject pkgObj = pkgCol.findOne(pkg.id);
		// check existence
		if (pkgObj == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		// set name
		pkg.name = (String) pkgObj.get("name");
		pkg.package_id = (Long) pkgObj.get("package");
	}

	@Override
	public void createPackage(PackageDBInfo pkg) {
		// create : 1. type collection
		writeElementType(pkg.id, MElementType.Package);
		// create : 2. class collection
		DBCollection pkgCol = db.getCollection(COLLECT_NAME_PACKAGE);
		DBObject pkgObj = new BasicDBObject();
		pkgObj.put("_id", pkg.id);
		pkgObj.put("name", pkg.name);
		pkgObj.put("package", pkg.package_id);
		pkgCol.insert(pkgObj);
	}

	@Override
	public void updatePackage(PackageDBInfo pkg) {
		DBCollection pkgCol = db.getCollection(COLLECT_NAME_PACKAGE);
		DBObject pkgQue = new BasicDBObject();
		DBObject pkgObj = new BasicDBObject();
		pkgQue.put("_id", pkg.id);
		
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			DBObject pkgInDB = pkgCol.findOne(pkg.id);
			if (pkgInDB == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		
		// update
		pkgObj.put("name", pkg.name);
		pkgObj.put("package", pkg.package_id);
		pkgCol.update(pkgQue, pkgObj);
	}

	@Override
	public void deletePackage(PackageDBInfo pkg) {
		DBCollection pkgCol = db.getCollection(COLLECT_NAME_PACKAGE);
		DBCollection eleCol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject pkgQue = new BasicDBObject();
		
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			DBObject pkgInDB = pkgCol.findOne(pkg.id);
			if (pkgInDB == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		
		// delete
		pkgQue.put("_id", pkg.id);
		pkgCol.remove(pkgQue);
		eleCol.remove(pkgQue);
	}
	
	@Override
	public void loadClass(ClassDBInfo cls) {
		// load
		DBCollection clsCol = db.getCollection(COLLECT_NAME_CLASS);
		DBObject clsObj = clsCol.findOne(cls.id);
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			if (clsObj == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		// set name
		cls.name = (String) clsObj.get("name");
		// set super class
		cls.superclass_id = (Long) clsObj.get("superclass");
		// set package
		cls.package_id = (Long) clsObj.get("package");
	}

	@Override
	public void createClass(ClassDBInfo cls) {
		// create : 1. type collection
		writeElementType(cls.id, MElementType.Class);
		// create : 2. class collection
		DBCollection clsCol = db.getCollection(COLLECT_NAME_CLASS);
		DBObject clsObj = new BasicDBObject();
		clsObj.put("_id", cls.id);
		clsObj.put("name", cls.name);
		clsObj.put("superclass", cls.superclass_id);
		clsObj.put("package", cls.package_id);
		clsCol.insert(clsObj);
	}

	@Override
	public void updateClass(ClassDBInfo cls) {
		DBCollection clsCol = db.getCollection(COLLECT_NAME_CLASS);
		DBObject clsQue = new BasicDBObject();
		DBObject clsObj = new BasicDBObject();
		clsQue.put("_id", cls.id);
		
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			DBObject clsInDB = clsCol.findOne(cls.id);
			if (clsInDB == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		
		// update
		clsObj.put("name", cls.name);
		clsObj.put("superclass", cls.superclass_id);
		clsObj.put("package", cls.package_id);
		clsCol.update(clsQue, clsObj);
	}

	@Override
	public void deleteClass(ClassDBInfo cls) {
		DBCollection clsCol = db.getCollection(COLLECT_NAME_CLASS);
		DBCollection eleCol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject clsQue = new BasicDBObject();
		
		// valid existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			DBObject clsInDB = clsCol.findOne(cls.id);
			if (clsInDB == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		
		// delete
		clsQue.put("_id", cls.id);
		clsCol.remove(clsQue);
		eleCol.remove(clsQue);
	}

	@Override
	public void loadAttribute(AttributeDBInfo atb) {
		// load
		DBCollection atbCol = db.getCollection(COLLECT_NAME_ATTRIBUTE);
		DBObject atbObj = atbCol.findOne(atb.id);
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			if (atbObj == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		// set name
		atb.name = (String) atbObj.get("name");
		// set type
		atb.type_id = (String) atbObj.get("type");
		// set class
		atb.class_id = (Long) atbObj.get("class");
	}

	@Override
	public void createAttribute(AttributeDBInfo atb) {
		// create : 1. type collection
		writeElementType(atb.id, MElementType.Attribute);
		// create : 2. attribute collection
		DBCollection atbCol = db.getCollection(COLLECT_NAME_ATTRIBUTE);
		DBObject atbObj = new BasicDBObject();
		atbObj.put("_id", atb.id);
		atbObj.put("name", atb.name);
		atbObj.put("type", atb.type_id);
		atbObj.put("class", atb.class_id);
		atbCol.insert(atbObj);
	}

	@Override
	public void updateAttribute(AttributeDBInfo atb) {
		DBCollection atbCol = db.getCollection(COLLECT_NAME_ATTRIBUTE);
		DBObject atbQue = new BasicDBObject();
		DBObject atbObj = new BasicDBObject();
		atbQue.put("_id", atb.id);
		
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			DBObject atbInDB = atbCol.findOne(atb.id);
			if (atbInDB == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		
		// update
		atbObj.put("name", atb.name);
		atbObj.put("type", atb.type_id);
		atbObj.put("class", atb.class_id);
		atbCol.update(atbQue, atbObj);
	}

	@Override
	public void deleteAttribute(AttributeDBInfo atb) {
		DBCollection atbCol = db.getCollection(COLLECT_NAME_ATTRIBUTE);
		DBCollection eleCol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject atbQue = new BasicDBObject();
		
		// valid existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			DBObject atbInDB = atbCol.findOne(atb.id);
			if (atbInDB == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		
		atbQue.put("_id", atb.id);
		atbCol.remove(atbQue);
		eleCol.remove(atbQue);
	}

	@Override
	public void loadReference(ReferenceDBInfo ref) {
		// load
		DBCollection refCol = db.getCollection(COLLECT_NAME_REFERENCE);
		DBObject refObj = refCol.findOne(ref.id);
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			if (refObj == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		// set name
		ref.name = (String) refObj.get("name");
		// set class
		ref.class_id = (Long) refObj.get("class");
		// set reference
		ref.reference_id = (Long) refObj.get("reference");
		// set multiplicity
		ref.multi = Multiplicity.valueOf((String) refObj.get("multiplicity"));
		// set opposite
		ref.opposite_id = (Long) refObj.get("opposite");
	}

	@Override
	public void createReference(ReferenceDBInfo ref) {
		// create : 1. type collection
		writeElementType(ref.id, MElementType.Reference);
		// create : 2. attribute collection
		DBCollection refCol = db.getCollection(COLLECT_NAME_REFERENCE);
		DBObject refObj = new BasicDBObject();
		refObj.put("_id", ref.id);
		refObj.put("name", ref.name);
		refObj.put("calss", ref.class_id);
		refObj.put("reference", ref.reference_id);
		refObj.put("multiplicity", ref.multi.toString());
		refObj.put("opposite", ref.opposite_id);
		refCol.insert(refObj);
	}

	@Override
	public void updateReference(ReferenceDBInfo ref) {
		DBCollection refCol = db.getCollection(COLLECT_NAME_REFERENCE);
		DBObject refQue = new BasicDBObject();
		DBObject refObj = new BasicDBObject();
		refQue.put("_id", ref.id);
		
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			DBObject refInDB = refCol.findOne(ref.id);
			if (refInDB == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		
		// update
		refObj.put("name", ref.name);
		refObj.put("calss", ref.class_id);
		refObj.put("reference", ref.reference_id);
		refObj.put("multiplicity", ref.multi.toString());
		refObj.put("opposite", ref.opposite_id);
		refCol.update(refQue, refObj);
	}

	@Override
	public void deleteReference(ReferenceDBInfo ref) {
		DBCollection refCol = db.getCollection(COLLECT_NAME_REFERENCE);
		DBCollection eleCol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject refQue = new BasicDBObject();
		
		// valid existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			DBObject refInDB = refCol.findOne(ref.id);
			if (refInDB == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		
		refQue.put("_id", ref.id);
		refCol.remove(refQue);
		eleCol.remove(refQue);
	}

	@Override
	public void loadEnum(EnumDBInfo enm) {
		// load
		DBCollection enmCol = db.getCollection(COLLECT_NAME_ENUM);
		DBObject enmObj = enmCol.findOne(enm.id);
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			if (enmObj == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		// set name
		enm.name = (String) enmObj.get("name");
		// set package
		enm.package_id = (Long) enmObj.get("package");
	}

	@Override
	public void createEnum(EnumDBInfo enm) {	
		// create : 1. type collection
		writeElementType(enm.id, MElementType.Enum);
		// create : 2. enm collection
		DBCollection enmCol = db.getCollection(COLLECT_NAME_ENUM);
		DBObject enmObj = new BasicDBObject();
		enmObj.put("_id", enm.id);
		enmObj.put("name", enm.name);
		enmObj.put("package", enm.package_id);
		enmCol.insert(enmObj);
	}

	@Override
	public void updateEnum(EnumDBInfo enm) {
		DBCollection enmCol = db.getCollection(COLLECT_NAME_ENUM);
		DBObject enmQue = new BasicDBObject();
		DBObject enmObj = new BasicDBObject();
		enmQue.put("_id", enm.id);
		
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			DBObject enmInDB = enmCol.findOne(enm.id);
			if (enmInDB == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		
		// update
		enmObj.put("name", enm.name);
		enmObj.put("package", enm.package_id);
		enmCol.update(enmQue, enmObj);
	}

	@Override
	public void deleteEnum(EnumDBInfo enm) {
		DBCollection enmCol = db.getCollection(COLLECT_NAME_ENUM);
		DBCollection eleCol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject enmQue = new BasicDBObject();
		
		// valid existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			DBObject enmInDB = enmCol.findOne(enm.id);
			if (enmInDB == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		
		// delete
		enmQue.put("_id", enm.id);
		enmCol.remove(enmQue);
		eleCol.remove(enmQue);
	}

	@Override
	public void loadSymbol(SymbolDBInfo sym) {
		// load
		DBCollection symCol = db.getCollection(COLLECT_NAME_SYMBOL);
		DBObject symObj = symCol.findOne(sym.id);
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			if (symObj == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		// set name
		sym.name = (String) symObj.get("name");
		// set enm
		sym.enum_id = (Long) symObj.get("enum");
	}

	@Override
	public void createSymbol(SymbolDBInfo sym) {
		// create : 1. type collection
		writeElementType(sym.id, MElementType.Symbol);
		// create : 2. sym collection
		DBCollection symCol = db.getCollection(COLLECT_NAME_SYMBOL);
		DBObject symObj = new BasicDBObject();
		symObj.put("_id", sym.id);
		symObj.put("name", sym.name);
		symObj.put("enum", sym.enum_id);
		symCol.insert(symObj);
	}

	@Override
	public void updateSymbol(SymbolDBInfo sym) {
		DBCollection symCol = db.getCollection(COLLECT_NAME_SYMBOL);
		DBObject symQue = new BasicDBObject();
		DBObject symObj = new BasicDBObject();
		symQue.put("_id", sym.id);
	
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			DBObject symInDB = symCol.findOne(sym.id);
			if (symInDB == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		//update
		symObj.put("name", sym.name);
		symObj.put("enum", sym.enum_id);
		symCol.update(symQue, symObj);
	}

	@Override
	public void deleteSymbol(SymbolDBInfo sym) {
		DBCollection symCol = db.getCollection(COLLECT_NAME_SYMBOL);
		DBCollection eleCol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject symQue = new BasicDBObject();
		
		// valid existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			DBObject symInDB = symCol.findOne(sym.id);
			if (symInDB == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		
		symQue.put("_id", sym.id);
		symCol.remove(symQue);
		eleCol.remove(symQue);
	}

	@Override
	public void loadObject(ObjectDBInfo obj) {
		// load
		String class_id = classIDToString(obj.class_id);
		DBCollection objCol = db.getCollection(class_id);
		DBObject objObj = objCol.findOne(obj.id);
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			if (objObj == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		// load values
		Iterator<String> it = objObj.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			if (key.equals("_id"))
				continue;
			String k = key.substring(1);
			obj.values.put(k, dbObjectToObject(objObj.get(key)));
		}
	}

	@Override
	public void createObject(ObjectDBInfo obj) {
		// create : 1. type collection
		writeObject(obj.id, obj.class_id);
		// create : 2. specific class collection
		String class_id = classIDToString(obj.class_id);
		DBCollection objCol = db.getCollection(class_id);
		DBObject objObj = new BasicDBObject();
		objObj.put("_id", obj.id);
		objCol.insert(objObj);
	}

	@Override
	public void updateObject(ObjectDBInfo obj) {
		String class_id = classIDToString(obj.class_id);
		DBCollection objCol = db.getCollection(class_id);
		DBObject objObj = new BasicDBObject();
		DBObject objQue = new BasicDBObject();
		objQue.put("_id", obj.id);
		
		// valid existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			DBObject objInDB = objCol.findOne(obj.id, objQue);
			if (objInDB == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		
		objObj.put("_id", obj.id);
		Iterator<Entry<String, Object>> it = obj.values.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Object> entry = it.next();
			Object value = entry.getValue();
			objObj.put("p" + entry.getKey(), objectToDBObject(value));
		}
		objCol.update(objQue, objObj);
	}
	
	@Override
	public void deleteObject(ObjectDBInfo obj) {
		String class_id = classIDToString(obj.class_id);
		DBCollection objCol = db.getCollection(class_id);
		DBCollection eleCol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject objQue = new BasicDBObject();
		objQue.put("_id", obj.id);
		
		// valid existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			DBObject objInDB = objCol.findOne(obj.id, objQue);
			if (objInDB == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		
		objCol.remove(objQue);
		eleCol.remove(objQue);
	}

	@Override
	public void loadTag(TagDBInfo tag) {
		DBCollection tagCol = db.getCollection(COLLECT_NAME_TAG);
		DBObject tagObj = tagCol.findOne(tag.id);
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			if (tagObj == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		
		// name
		tag.name = (String) tagObj.get("name");
		// value
		tag.value = dbObjectToObject(tagObj.get("value"));
		// targets
		BasicDBList targets = (BasicDBList) tagObj.get("targets");
		Iterator<Object> it = targets.iterator();
		while (it.hasNext()) {
			Long tid = (Long) it.next();
			tag.targets_id.add(tid);
		}
	}

	@Override
	public void createTag(TagDBInfo tag) {
		// create : 1. type collection
		writeElementType(tag.id, MElementType.Tag);
		// create : 2. specific class collection
		DBCollection tagCol = db.getCollection(COLLECT_NAME_TAG);
		DBObject tagObj = new BasicDBObject();
		tagObj.put("_id", tag.id);
		tagObj.put("name", tag.name);
		tagObj.put("value", objectToDBObject(tag.value));
		BasicDBList targets = new BasicDBList();
		Iterator<Long> it = tag.targets_id.iterator();
		while (it.hasNext()) {
			Long tid = it.next();
			targets.add(tid);
		}
		tagObj.put("targets", targets);
		tagCol.insert(tagObj);
	}

	@Override
	public void updateTag(TagDBInfo tag) {
		DBCollection tagCol = db.getCollection(COLLECT_NAME_TAG);
		DBObject tagQue = new BasicDBObject();
		DBObject tagObj = new BasicDBObject();
		tagQue.put("_id", tag.id);
	
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			DBObject symInDB = tagCol.findOne(tag.id);
			if (symInDB == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		tagObj.put("name", tag.name);
		tagObj.put("value", objectToDBObject(tag.value));
		BasicDBList targets = new BasicDBList();
		Iterator<Long> it = tag.targets_id.iterator();
		while (it.hasNext()) {
			Long tid = it.next();
			targets.add(tid);
		}
		tagObj.put("targets", targets);
		
		tagCol.update(tagQue, tagObj);
	}

	@Override
	public void deleteTag(TagDBInfo tag) {
		DBCollection tagCol = db.getCollection(COLLECT_NAME_TAG);
		DBCollection eleCol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject tagQue = new BasicDBObject();
		
		// valid existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			DBObject tagInDB = tagCol.findOne(tag.id);
			if (tagInDB == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		
		tagQue.put("_id", tag.id);
		tagCol.remove(tagQue);
		eleCol.remove(tagQue);
	}

	private static Object objectToDBObject(Object obj) {
		Object value = obj;
		if (value instanceof MElementPointer) {
			value = elementPtToDBObject((MElementPointer) value);
		} else if (value instanceof DataDict) {
			value = dataDictToDBObject((DataDict) value);
		} else if (value instanceof DataList) {
			value = dataListToDBObject((DataList) value);
		} else if (value instanceof DataSet) {
			value = dataSetToDBObject((DataSet) value);
		}
		return value;
	}
	
	private static Object dbObjectToObject(Object obj) {
		if (obj instanceof DBObject) {
			DBObject dbo = (DBObject) obj;
			if (dbo.containsField("d")) {
				return dbObjectToDataDict(dbo);
			} else if (dbo.containsField("l")) {
				return dbObjectToDataList(dbo);
			} else if (dbo.containsField("s")) {
				return dbObjectToDataSet(dbo);
			} else {
				return dbObjectToElementPt(dbo);
			}
		}
		return obj;
	}
	
	private static DBObject dataDictToDBObject(DataDict dd) {
		DBObject obj = new BasicDBObject();
		String key = "d";
		DBObject dict = new BasicDBObject();
		Iterator<Entry<String, Object>> it = dd.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Object> entry = it.next();
			Object value = entry.getValue();
			dict.put(entry.getKey(), objectToDBObject(value));
		}
		obj.put(key, dict);
		return obj;
	}
	
	private static DataDict dbObjectToDataDict(DBObject obj) {
		DataDict dd = new DataDict();
		DBObject dict = (DBObject) obj.get("d");
		Iterator<String> it = dict.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			dd.put(key, dbObjectToObject(dict.get(key)));
		}
		return dd;
	}
	
	private static DBObject dataListToDBObject(DataList dl) {
		DBObject obj = new BasicDBObject();
		String key = "l";
		BasicDBList list = new BasicDBList();
		Iterator<Object> it = dl.iterator();
		while (it.hasNext()) {
			Object value = it.next();
			list.add(objectToDBObject(value));
		}
		obj.put(key, list);
		return obj;
	}
	
	private static DataList dbObjectToDataList(DBObject obj) {
		DataList dl = new DataList();
		BasicDBList list = (BasicDBList) obj.get("l");
		Iterator<Object> it = list.iterator();
		while (it.hasNext()) {
			Object value = it.next();
			dl.add(dbObjectToObject(value));
		}
		return dl;
	}
	
	private static DBObject dataSetToDBObject(DataSet ds) {
		DBObject obj = new BasicDBObject();
		String key = "s";
		BasicDBList list = new BasicDBList();
		Iterator<Object> it = ds.iterator();
		while (it.hasNext()) {
			Object value = it.next();
			list.add(objectToDBObject(value));
		}
		obj.put(key, list);
		return obj;
	}
	
	private static DataSet dbObjectToDataSet(DBObject obj) {
		DataSet ds = new DataSet();
		BasicDBList list = (BasicDBList) obj.get("l");
		Iterator<Object> it = list.iterator();
		while (it.hasNext()) {
			Object value = it.next();
			ds.add(dbObjectToObject(value));
		}
		return ds;
	}
	
	private static DBObject elementPtToDBObject(MElementPointer pt) {
		DBObject obj = new BasicDBObject();
		String key;
		switch (pt.getElementType()) {
		case Object:
			key = "o";
			break;
		case Symbol:
			key = "e";
			break;
		default:
			throw new MException(MException.Reason.NOT_SUPPORT_YET);
		}
		obj.put(key, pt.getID());
		return obj;
	}
	
	private static MElementPointer dbObjectToElementPt(DBObject obj) {
		if (obj.containsField("o")) {
			Long id = (Long) obj.get("o");
			return new MElementPointer(id, MElementType.Object);
		} else if (obj.containsField("e")) {
			Long id = (Long) obj.get("e");
			return new MElementPointer(id, MElementType.Symbol);
		}
		return new MElementPointer();
	}

	@Override
	public void loadElementTags(ElementTagDBInfo dbInfo) {
		DBCollection eleCol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject eleObj = eleCol.findOne(dbInfo.id);
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			if (eleObj == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		BasicDBList tags = (BasicDBList) eleObj.get("tags");
		Iterator<Object> it = tags.iterator();
		while (it.hasNext()) {
			Long tid = (Long) it.next();
			dbInfo.tags_id.add(tid);
		}
	}

	@Override
	public void saveElementTags(ElementTagDBInfo dbInfo) {
		DBCollection eleCol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject eleQue = new BasicDBObject();
		eleQue.put("_id", dbInfo.id);
		BasicDBList eleObj = new BasicDBList();
		Iterator<Long> it = dbInfo.tags_id.iterator();
		while (it.hasNext()) {
			Long tid = it.next();
			eleObj.add(tid);
		}
		DBObject setter = new BasicDBObject();
		setter.put("$set", eleObj);
		eleCol.update(eleQue, eleObj);
	}

	@Override
	public long loadLastIDAndIncrement() {
		DBCollection statecol = db.getCollection(COLLECT_NAME_GLOBAL);
		DBObject query = new BasicDBObject();
		DBObject update = new BasicDBObject();
		DBObject increment = new BasicDBObject();
		increment.put("lastID", 1L);
		update.put("$inc", increment);
		DBObject state = statecol.findAndModify(query, update);
		Long id = (Long) state.get("lastID");
		return id;
	}
	
	public void checkAndPrepareDB() {
		DBCollection statecol = db.getCollection(COLLECT_NAME_GLOBAL);
		DBObject obj = statecol.findOne();
		// create system state object
		if (obj == null) {
			DBObject stateObj = new BasicDBObject();
			stateObj.put("_id", 0L);
			stateObj.put("lastID", 1L);
			statecol.insert(stateObj);
		} else {
			// if no "lastID" field, create it.
			if (!obj.containsField("lastID")) {
				DBObject update = new BasicDBObject();
				update.put("lastID", 1L);
				statecol.update(obj, update);
			}
		}
		DBCollection eleCol = db.getCollection(COLLECT_NAME_ELEMENT);
		eleCol.ensureIndex("type");
		// TODO
		
	}

	@Override
	public MElementType getElementType(long id) {
		DBCollection eleCol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject typeObj = eleCol.findOne(id);
		if (typeObj == null)
			return null;
		String type = (String) typeObj.get("type");
		return MElementType.valueOf(type);
	}

	@Override
	public IDList listAllPackageIDs() {
		return listIDs(COLLECT_NAME_PACKAGE);
	}

	@Override
	public IDList listAllClassIDs() {
		return listIDs(COLLECT_NAME_CLASS);
	}

	@Override
	public IDList listAllAttributeIDs() {
		return listIDs(COLLECT_NAME_ATTRIBUTE);
	}
	
	@Override
	public IDList listAllReferenceIDs() {
		return listIDs(COLLECT_NAME_REFERENCE);
	}

	@Override
	public IDList listAllEnumIDs() {
		return listIDs(COLLECT_NAME_ENUM);
	}

	@Override
	public IDList listAllSymbolIDs() {
		return listIDs(COLLECT_NAME_SYMBOL);
	}
	
	@Override
	public IDList listAllObjectIDs(long id) {
		return listIDs(classIDToString(id));
	}
	
	private IDList listIDs(String collectionName) {
		DBCollection col = db.getCollection(collectionName);
		DBObject projection = new BasicDBObject().append("_id", true);
		DBObject query = new BasicDBObject();
		DBCursor cursor = col.find(query, projection);
		IDList list = new IDList();
		while (cursor.hasNext()) {
			DBObject o = cursor.next();
			list.add((Long) o.get("_id"));
		}
		return list;
	}
	
	private static String classIDToString(long id) {
		return "_" + MUtility.stringID(id);
	}
	
	@Override
	public void resetDB() {
		this.db.dropDatabase();
		this.checkAndPrepareDB();
	}
	
}
