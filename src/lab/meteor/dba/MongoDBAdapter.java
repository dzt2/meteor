package lab.meteor.dba;

import java.util.LinkedList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import lab.meteor.core.MDBAdapter;
import lab.meteor.core.MElement.MElementType;
import lab.meteor.core.MException;
import lab.meteor.core.MReference.Multiplicity;
import lab.meteor.core.MUtility;

public class MongoDBAdapter implements MDBAdapter {

	private static final String COLLECT_NAME_CLASS = "classes";
	private static final String COLLECT_NAME_ATTRIBUTE = "attributes";
	private static final String COLLECT_NAME_REFERENCE = "references";
	private static final String COLLECT_NAME_ENUM = "enumes";
	private static final String COLLECT_NAME_SYMBOL = "symbols";
	private static final String COLLECT_NAME_PACKAGE = "packages";
	// TODO
	private static final String COLLECT_NAME_TAG = "tags";
	private static final String COLLECT_NAME_GLOBAL = "global";
	private static final String COLLECT_NAME_ELEMENT = "elements";
	
	public static boolean ENABLE_DOUBLE_CHECK_EXISTENCE = true;
	
	private DB db;
	
	public void setDB(DB db) {
		this.db = db;
	}
	
	private void writeElementType(long id, MElementType type) {
		DBCollection typeCol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject typeObj = new BasicDBObject();
		typeObj.put("_id", id);
		typeObj.put("type", type.toString());
		typeCol.insert(typeObj);
	}
	
	private void writeObject(long id, long class_id) {
		DBCollection typeCol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject typeObj = new BasicDBObject();
		typeObj.put("_id", id);
		typeObj.put("type", MElementType.Object.toString());
		typeObj.put("class", classIDToString(class_id));
		typeCol.insert(typeObj);
	}
	
	@Override
	public void loadPackage(PackageDBInfo pkg) {
		// load
		DBCollection pkgCol = db.getCollection(COLLECT_NAME_PACKAGE);
		long id = pkg.id;
		DBObject pkgObj = pkgCol.findOne(id);
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
		DBCollection typeCol = db.getCollection(COLLECT_NAME_ELEMENT);
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
		typeCol.remove(pkgQue);
	}
	
	@Override
	public void loadClass(ClassDBInfo cls) {
		// load
		DBCollection clsCol = db.getCollection(COLLECT_NAME_CLASS);
		long id = cls.id;
		DBObject clsObj = clsCol.findOne(id);
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
		DBCollection typeCol = db.getCollection(COLLECT_NAME_ELEMENT);
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
		typeCol.remove(clsQue);
	}

	@Override
	public void loadAttribute(AttributeDBInfo atb) {
		// load
		DBCollection atbCol = db.getCollection(COLLECT_NAME_ATTRIBUTE);
		long id = atb.id;
		DBObject atbObj = atbCol.findOne(id);
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
		DBCollection typeCol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject atbQue = new BasicDBObject();
		
		// valid existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			DBObject atbInDB = atbCol.findOne(atb.id);
			if (atbInDB == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		
		atbQue.put("_id", atb.id);
		atbCol.remove(atbQue);
		typeCol.remove(atbQue);
	}

	@Override
	public void loadReference(ReferenceDBInfo ref) {
		// load
		DBCollection refCol = db.getCollection(COLLECT_NAME_REFERENCE);
		long id = ref.id;
		DBObject refObj = refCol.findOne(id);
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
		refObj.put("multiplicity", ref.multi);
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
		refObj.put("multiplicity", ref.multi);
		refObj.put("opposite", ref.opposite_id);
		refCol.update(refQue, refObj);
	}

	@Override
	public void deleteReference(ReferenceDBInfo ref) {
		DBCollection refCol = db.getCollection(COLLECT_NAME_REFERENCE);
		DBCollection typeCol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject refQue = new BasicDBObject();
		
		// valid existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			DBObject refInDB = refCol.findOne(ref.id);
			if (refInDB == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		
		refQue.put("_id", ref.id);
		refCol.remove(refQue);
		typeCol.remove(refQue);
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
		DBCollection typeCol = db.getCollection(COLLECT_NAME_ELEMENT);
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
		typeCol.remove(enmQue);
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
		symObj.put("_id", sym.id);
	
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
		DBCollection typeCol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject symQue = new BasicDBObject();
		
		// valid existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			DBObject symInDB = symCol.findOne(sym.id);
			if (symInDB == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		
		symQue.put("_id", sym.id);
		symCol.remove(symQue);
		typeCol.remove(symQue);
	}

	@Override
	public void loadObject(ObjectDBInfo obj) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteObject(ObjectDBInfo obj) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void loadTag(TagDBInfo tag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createTag(TagDBInfo tag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateTag(TagDBInfo tag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteTag(TagDBInfo tag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadElementTags(ElementTagDBInfo dbInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveElementTags(ElementTagDBInfo dbInfo) {
		// TODO Auto-generated method stub
		
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
		DBCollection typeCol = db.getCollection(COLLECT_NAME_ELEMENT);
		typeCol.ensureIndex("type");
		// TODO
		
	}

	@Override
	public MElementType getElementType(long id) {
		DBCollection typeCol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject typeObj = typeCol.findOne(id);
		if (typeObj == null)
			return null;
		String type = (String) typeObj.get("type");
		return MElementType.valueOf(type);
	}

	@Override
	public IDList listAllPackageIDs() {
		DBCollection col = db.getCollection(COLLECT_NAME_PACKAGE);
		DBObject projection = new BasicDBObject();
		projection.put("_id", true);
		DBCursor cursor = col.find(new BasicDBObject(), projection);
		IDList list = new IDList();
		while (cursor.hasNext()) {
			DBObject o = cursor.next();
			list.add((Long) o.get("_id"));
		}
		return list;
	}

	@Override
	public IDList listAllClassIDs() {
		DBCollection col = db.getCollection(COLLECT_NAME_CLASS);
		DBObject projection = new BasicDBObject();
		projection.put("_id", true);
		DBCursor cursor = col.find(new BasicDBObject(), projection);
		IDList list = new IDList();
		while (cursor.hasNext()) {
			DBObject o = cursor.next();
			list.add((Long) o.get("_id"));
		}
		return list;
	}

	@Override
	public IDList listAllAttributeIDs() {
		DBCollection col = db.getCollection(COLLECT_NAME_ATTRIBUTE);
		DBObject projection = new BasicDBObject();
		projection.put("_id", true);
		DBCursor cursor = col.find(new BasicDBObject(), projection);
		IDList list = new IDList();
		while (cursor.hasNext()) {
			DBObject o = cursor.next();
			list.add((Long) o.get("_id"));
		}
		return list;
	}

	@Override
	public IDList listAllEnumIDs() {
		DBCollection col = db.getCollection(COLLECT_NAME_ENUM);
		DBObject projection = new BasicDBObject();
		projection.put("_id", true);
		DBCursor cursor = col.find(new BasicDBObject(), projection);
		IDList list = new IDList();
		while (cursor.hasNext()) {
			DBObject o = cursor.next();
			list.add((Long) o.get("_id"));
		}
		return list;
	}

	@Override
	public IDList listAllSymbolIDs() {
		DBCollection col = db.getCollection(COLLECT_NAME_SYMBOL);
		DBObject projection = new BasicDBObject();
		projection.put("_id", true);
		DBCursor cursor = col.find(new BasicDBObject(), projection);
		IDList list = new IDList();
		while (cursor.hasNext()) {
			DBObject o = cursor.next();
			list.add((Long) o.get("_id"));
		}
		return list;
	}
	
	@Override
	public IDList listAllObjectIDs(long id) {
		DBCollection col = db.getCollection(classIDToString(id));
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
		return "_" + MUtility.idEncode(id);
	}
	
	@Override
	public void resetDB() {
		this.db.dropDatabase();
		this.checkAndPrepareDB();
	}
	
}
