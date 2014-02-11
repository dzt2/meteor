package lab.meteor.dba;

import java.util.Iterator;
import java.util.Map.Entry;

import org.bson.types.Binary;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import lab.meteor.core.MAttribute;
import lab.meteor.core.MClass;
import lab.meteor.core.MDBAdapter;
import lab.meteor.core.MElement.MElementType;
import lab.meteor.core.MElementPointer;
import lab.meteor.core.MEnum;
import lab.meteor.core.MException;
import lab.meteor.core.MObject;
import lab.meteor.core.MPackage;
import lab.meteor.core.MReference;
import lab.meteor.core.MReference.Multiplicity;
import lab.meteor.core.MSymbol;
import lab.meteor.core.MTag;
import lab.meteor.core.MUtility;
import lab.meteor.core.type.MBinary;
import lab.meteor.core.type.MCode;
import lab.meteor.core.type.MRef;

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
	
	// Element Collection is used for storing map of ID-Type.
	private void writeElementType(long id, MElementType type) {
		DBCollection eleCol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject typeObj = new BasicDBObject();
		typeObj.put("_id", id);
		typeObj.put("type", type.toString());
		eleCol.insert(typeObj);
	}
	
	private void writeObjectClass(long id, long class_id) {
		DBCollection ecol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject obj = new BasicDBObject();
		obj.put("_id", id);
		obj.put("type", MElementType.Object.toString());
		obj.put("class", class_id);
		ecol.insert(obj);
	}
	
	// Argument id is the Object's id, and return the id of Object's class.
	private long readObjectClass(long id) {
		DBCollection ecol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject que = new BasicDBObject();
		que.put("class", 1);
		DBObject obj = ecol.findOne(id, que);
		if (obj == null || !obj.containsField("class"))
			throw new MException(MException.Reason.ELEMENT_MISSED);
		return (long) obj.get("class");
	}
	
	private void checkExistence(DBCollection col, long id) {
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			DBObject o = col.findOne(id, new BasicDBObject().append("_id", true));
			if (o == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
	}
	
	@Override
	public void loadPackage(PackageDBInfo pkg) {
		// load
		DBCollection col = db.getCollection(COLLECT_NAME_PACKAGE);
		DBObject fields = new BasicDBObject();
		if (pkg.isFlagged(MPackage.ATTRIB_FLAG_NAME))
			fields.put("name", true);
		if (pkg.isFlagged(MPackage.ATTRIB_FLAG_PARENT))
			fields.put("package", true);
		DBObject obj = col.findOne(pkg.id, fields);
	
		// check existence
		if (obj == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		
		if (pkg.isFlagged(MPackage.ATTRIB_FLAG_NAME))
			pkg.name = (String) obj.get("name");
		if (pkg.isFlagged(MPackage.ATTRIB_FLAG_PARENT))
			pkg.package_id = (Long) obj.get("package");
	}

	@Override
	public void createPackage(PackageDBInfo pkg) {
		// create : 1. type collection
		writeElementType(pkg.id, MElementType.Package);
		// create : 2. class collection
		DBCollection col = db.getCollection(COLLECT_NAME_PACKAGE);
		DBObject obj = new BasicDBObject();
		obj.put("_id", pkg.id);
		obj.put("name", pkg.name);
		obj.put("package", pkg.package_id);
		col.insert(obj);
	}

	@Override
	public void updatePackage(PackageDBInfo pkg) {
		if (pkg.noFlag())
			return;
		DBCollection col = db.getCollection(COLLECT_NAME_PACKAGE);
		DBObject que = new BasicDBObject();
		DBObject obj = new BasicDBObject();
		que.put("_id", pkg.id);
		
		// double check existence
		checkExistence(col, pkg.id);
		
		// update
		if (pkg.isFlagged(MPackage.ATTRIB_FLAG_NAME))
			obj.put("name", pkg.name);
		if (pkg.isFlagged(MPackage.ATTRIB_FLAG_PARENT))
			obj.put("package", pkg.package_id);
		DBObject set = new BasicDBObject();
		set.put("$set", obj);
		col.update(que, set);
	}

	@Override
	public void deletePackage(PackageDBInfo pkg) {
		DBCollection col = db.getCollection(COLLECT_NAME_PACKAGE);
		DBCollection ecol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject que = new BasicDBObject();
		
		// double check existence
		checkExistence(col, pkg.id);
		
		// delete
		que.put("_id", pkg.id);
		col.remove(que);
		ecol.remove(que);
	}
	
	// load Class Information into ClassDBInfo
	@Override
	public void loadClass(ClassDBInfo cls) {
		DBCollection col = db.getCollection(COLLECT_NAME_CLASS);
		DBObject fields = new BasicDBObject();
		if (cls.isFlagged(MClass.ATTRIB_FLAG_NAME))
			fields.put("name", true);
		if (cls.isFlagged(MClass.ATTRIB_FLAG_PARENT))
			fields.put("package", true);
		if (cls.isFlagged(MClass.ATTRIB_FLAG_SUPERCLASS))
			fields.put("superclass", true);
		DBObject obj = col.findOne(cls.id, fields);
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			if (obj == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		// set name
		if (cls.isFlagged(MClass.ATTRIB_FLAG_NAME))
			cls.name = (String) obj.get("name");
		// set super class
		if (cls.isFlagged(MClass.ATTRIB_FLAG_SUPERCLASS))
			cls.superclass_id = (Long) obj.get("superclass");
		// set package
		if (cls.isFlagged(MClass.ATTRIB_FLAG_PARENT))
			cls.package_id = (Long) obj.get("package");
	}

	@Override
	public void createClass(ClassDBInfo cls) {
		// create : 1. type collection
		writeElementType(cls.id, MElementType.Class);
		// create : 2. class collection
		DBCollection col = db.getCollection(COLLECT_NAME_CLASS);
		DBObject obj = new BasicDBObject();
		obj.put("_id", cls.id);
		obj.put("name", cls.name);
		obj.put("superclass", cls.superclass_id);
		obj.put("package", cls.package_id);
		col.insert(obj);
	}

	@Override
	public void updateClass(ClassDBInfo cls) {
		DBCollection col = db.getCollection(COLLECT_NAME_CLASS);
		DBObject que = new BasicDBObject();
		DBObject obj = new BasicDBObject();
		que.put("_id", cls.id);
		
		// double check existence
		checkExistence(col, cls.id);
		
		// update
		if (cls.isFlagged(MClass.ATTRIB_FLAG_NAME))
			obj.put("name", cls.name);
		if (cls.isFlagged(MClass.ATTRIB_FLAG_SUPERCLASS))
			obj.put("superclass", cls.superclass_id);
		if (cls.isFlagged(MClass.ATTRIB_FLAG_PARENT))
			obj.put("package", cls.package_id);
		DBObject set = new BasicDBObject();
		set.put("$set", obj);
		col.update(que, set);
	}

	@Override
	public void deleteClass(ClassDBInfo cls) {
		DBCollection col = db.getCollection(COLLECT_NAME_CLASS);
		DBCollection ecol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject que = new BasicDBObject();
		
		// valid existence
		checkExistence(col, cls.id);
		
		// delete
		que.put("_id", cls.id);
		col.remove(que);
		ecol.remove(que);
	}

	@Override
	public void loadAttribute(AttributeDBInfo atb) {
		// load
		DBCollection col = db.getCollection(COLLECT_NAME_ATTRIBUTE);
		DBObject fields = new BasicDBObject();
		if (atb.isFlagged(MAttribute.ATTRIB_FLAG_NAME))
			fields.put("name", true);
		if (atb.isFlagged(MAttribute.ATTRIB_FLAG_DATATYPE))
			fields.put("type", true);
		if (atb.isFlagged(MAttribute.ATTRIB_FLAG_PARENT))
			fields.put("class", true);
		DBObject obj = col.findOne(atb.id, fields);
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			if (obj == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		// set name
		if (atb.isFlagged(MAttribute.ATTRIB_FLAG_NAME))
			atb.name = (String) obj.get("name");
		// set type
		if (atb.isFlagged(MAttribute.ATTRIB_FLAG_DATATYPE))
			atb.type_id = (String) obj.get("type");
		// set class
		if (atb.isFlagged(MAttribute.ATTRIB_FLAG_PARENT))
			atb.class_id = (Long) obj.get("class");
	}

	@Override
	public void createAttribute(AttributeDBInfo atb) {
		// create : 1. type collection
		writeElementType(atb.id, MElementType.Attribute);
		// create : 2. attribute collection
		DBCollection col = db.getCollection(COLLECT_NAME_ATTRIBUTE);
		DBObject obj = new BasicDBObject();
		obj.put("_id", atb.id);
		obj.put("name", atb.name);
		obj.put("type", atb.type_id);
		obj.put("class", atb.class_id);
		col.insert(obj);
	}

	@Override
	public void updateAttribute(AttributeDBInfo atb) {
		DBCollection col = db.getCollection(COLLECT_NAME_ATTRIBUTE);
		DBObject que = new BasicDBObject();
		DBObject obj = new BasicDBObject();
		que.put("_id", atb.id);
		
		// double check existence
		checkExistence(col, atb.id);
		
		// update
		if (atb.isFlagged(MAttribute.ATTRIB_FLAG_NAME))
			obj.put("name", atb.name);
		if (atb.isFlagged(MAttribute.ATTRIB_FLAG_DATATYPE))
			obj.put("type", atb.type_id);
		if (atb.isFlagged(MAttribute.ATTRIB_FLAG_PARENT))
			obj.put("class", atb.class_id);
		DBObject set = new BasicDBObject();
		set.put("$set", obj);
		col.update(que, set);
	}

	@Override
	public void deleteAttribute(AttributeDBInfo atb) {
		DBCollection col = db.getCollection(COLLECT_NAME_ATTRIBUTE);
		DBCollection ecol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject que = new BasicDBObject();
		
		// valid existence
		checkExistence(col, atb.id);
		
		que.put("_id", atb.id);
		col.remove(que);
		ecol.remove(que);
	}

	@Override
	public void loadReference(ReferenceDBInfo ref) {
		// load
		DBCollection col = db.getCollection(COLLECT_NAME_REFERENCE);
		DBObject fields = new BasicDBObject();
		if (ref.isFlagged(MReference.ATTRIB_FLAG_NAME))
			fields.put("name", true);
		if (ref.isFlagged(MReference.ATTRIB_FLAG_REFERENCE))
			fields.put("reference", true);	//type
		if (ref.isFlagged(MReference.ATTRIB_FLAG_MULTIPLICITY))
			fields.put("multiplicity", true);
		if (ref.isFlagged(MReference.ATTRIB_FLAG_OPPOSITE))
			fields.put("opposite", true);
		if (ref.isFlagged(MReference.ATTRIB_FLAG_PARENT))
			fields.put("class", true);
		
		DBObject obj = col.findOne(ref.id, fields);
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			if (obj == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		// set name
		if (ref.isFlagged(MReference.ATTRIB_FLAG_NAME))
			ref.name = (String) obj.get("name");
		// set class
		if (ref.isFlagged(MReference.ATTRIB_FLAG_PARENT))
			ref.class_id = (Long) obj.get("class");
		// set reference
		if (ref.isFlagged(MReference.ATTRIB_FLAG_REFERENCE))
			ref.reference_id = (Long) obj.get("reference");
		// set multiplicity
		if (ref.isFlagged(MReference.ATTRIB_FLAG_MULTIPLICITY))
			ref.multi = Multiplicity.valueOf((String) obj.get("multiplicity"));
		// set opposite
		if (ref.isFlagged(MReference.ATTRIB_FLAG_OPPOSITE))
			ref.opposite_id = (Long) obj.get("opposite");
	}

	@Override
	public void createReference(ReferenceDBInfo ref) {
		// create : 1. type collection
		writeElementType(ref.id, MElementType.Reference);
		// create : 2. attribute collection
		DBCollection col = db.getCollection(COLLECT_NAME_REFERENCE);
		DBObject obj = new BasicDBObject();
		obj.put("_id", ref.id);
		obj.put("name", ref.name);
		obj.put("class", ref.class_id);
		obj.put("reference", ref.reference_id);
		obj.put("multiplicity", ref.multi.toString());
		obj.put("opposite", ref.opposite_id);
		col.insert(obj);
	}

	@Override
	public void updateReference(ReferenceDBInfo ref) {
		DBCollection col = db.getCollection(COLLECT_NAME_REFERENCE);
		DBObject que = new BasicDBObject();
		DBObject obj = new BasicDBObject();
		que.put("_id", ref.id);
		
		// double check existence
		checkExistence(col, ref.id);
		
		// update
		if (ref.isFlagged(MReference.ATTRIB_FLAG_NAME))
			obj.put("name", ref.name);
		if (ref.isFlagged(MReference.ATTRIB_FLAG_PARENT))
			obj.put("calss", ref.class_id);
		if (ref.isFlagged(MReference.ATTRIB_FLAG_REFERENCE))
			obj.put("reference", ref.reference_id);
		if (ref.isFlagged(MReference.ATTRIB_FLAG_MULTIPLICITY))
			obj.put("multiplicity", ref.multi.toString());
		if (ref.isFlagged(MReference.ATTRIB_FLAG_OPPOSITE))
			obj.put("opposite", ref.opposite_id);
		DBObject set = new BasicDBObject();
		set.put("$set", obj);
		col.update(que, set);
	}

	@Override
	public void deleteReference(ReferenceDBInfo ref) {
		DBCollection col = db.getCollection(COLLECT_NAME_REFERENCE);
		DBCollection ecol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject que = new BasicDBObject();
		
		// valid existence
		checkExistence(col, ref.id);
		
		que.put("_id", ref.id);
		col.remove(que);
		ecol.remove(que);
	}

	@Override
	public void loadEnum(EnumDBInfo enm) {
		// load
		DBCollection col = db.getCollection(COLLECT_NAME_ENUM);
		DBObject fields = new BasicDBObject();
		if (enm.isFlagged(MEnum.ATTRIB_FLAG_NAME))
			fields.put("name", true);
		if (enm.isFlagged(MEnum.ATTRIB_FLAG_PARENT))
			fields.put("package", true);
		DBObject obj = col.findOne(enm.id, fields);
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			if (obj == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		// set name
		if (enm.isFlagged(MEnum.ATTRIB_FLAG_NAME))
			enm.name = (String) obj.get("name");
		// set package
		if (enm.isFlagged(MEnum.ATTRIB_FLAG_PARENT))
			enm.package_id = (Long) obj.get("package");
	}

	@Override
	public void createEnum(EnumDBInfo enm) {	
		// create : 1. type collection
		writeElementType(enm.id, MElementType.Enum);
		// create : 2. enm collection
		DBCollection col = db.getCollection(COLLECT_NAME_ENUM);
		DBObject obj = new BasicDBObject();
		obj.put("_id", enm.id);
		obj.put("name", enm.name);
		obj.put("package", enm.package_id);
		col.insert(obj);
	}

	@Override
	public void updateEnum(EnumDBInfo enm) {
		DBCollection col = db.getCollection(COLLECT_NAME_ENUM);
		DBObject que = new BasicDBObject();
		DBObject obj = new BasicDBObject();
		que.put("_id", enm.id);
		
		// double check existence
		checkExistence(col, enm.id);
		
		// update
		if (enm.isFlagged(MEnum.ATTRIB_FLAG_NAME))
			obj.put("name", enm.name);
		if (enm.isFlagged(MEnum.ATTRIB_FLAG_PARENT))
			obj.put("package", enm.package_id);
		DBObject set = new BasicDBObject();
		set.put("$set", obj);
		col.update(que, set);
	}

	@Override
	public void deleteEnum(EnumDBInfo enm) {
		DBCollection col = db.getCollection(COLLECT_NAME_ENUM);
		DBCollection ecol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject que = new BasicDBObject();
		
		// valid existence
		checkExistence(col, enm.id);
		
		// delete
		que.put("_id", enm.id);
		col.remove(que);
		ecol.remove(que);
	}

	@Override
	public void loadSymbol(SymbolDBInfo sym) {
		// load
		DBCollection col = db.getCollection(COLLECT_NAME_SYMBOL);
		DBObject fields = new BasicDBObject();
		if (sym.isFlagged(MSymbol.ATTRIB_FLAG_NAME))
			fields.put("name", true);
		if (sym.isFlagged(MSymbol.ATTRIB_FLAG_PARENT))
			fields.put("enum", true);
		DBObject obj = col.findOne(sym.id, fields);
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			if (obj == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		// set name
		if (sym.isFlagged(MSymbol.ATTRIB_FLAG_NAME))
			sym.name = (String) obj.get("name");
		// set enm
		if (sym.isFlagged(MSymbol.ATTRIB_FLAG_PARENT))
			sym.enum_id = (Long) obj.get("enum");
	}

	@Override
	public void createSymbol(SymbolDBInfo sym) {
		// create : 1. type collection
		writeElementType(sym.id, MElementType.Symbol);
		// create : 2. sym collection
		DBCollection col = db.getCollection(COLLECT_NAME_SYMBOL);
		DBObject obj = new BasicDBObject();
		obj.put("_id", sym.id);
		obj.put("name", sym.name);
		obj.put("enum", sym.enum_id);
		col.insert(obj);
	}

	@Override
	public void updateSymbol(SymbolDBInfo sym) {
		DBCollection col = db.getCollection(COLLECT_NAME_SYMBOL);
		DBObject que = new BasicDBObject();
		DBObject obj = new BasicDBObject();
		que.put("_id", sym.id);
	
		// double check existence
		checkExistence(col, sym.id);
		
		//update
		if (sym.isFlagged(MSymbol.ATTRIB_FLAG_NAME))
			obj.put("name", sym.name);
		if (sym.isFlagged(MSymbol.ATTRIB_FLAG_PARENT))
			obj.put("enum", sym.enum_id);
		DBObject set = new BasicDBObject();
		set.put("$set", obj);
		col.update(que, set);
	}

	@Override
	public void deleteSymbol(SymbolDBInfo sym) {
		DBCollection col = db.getCollection(COLLECT_NAME_SYMBOL);
		DBCollection ecol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject que = new BasicDBObject();
		
		// valid existence
		checkExistence(col, sym.id);
		
		que.put("_id", sym.id);
		col.remove(que);
		ecol.remove(que);
	}

	// object!
	@Override
	public void loadObject(ObjectDBInfo obj) {
		long cls_id = readObjectClass(obj.id);
		DBCollection col = db.getCollection(classIDToString(cls_id));
		// TODO DBObject fields = new BasicDBObject();
		
		DBObject o = col.findOne(obj.id);
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			if (o == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		// load class
		if (obj.isFlagged(MObject.ATTRIB_FLAG_CLASS))
			obj.class_id = cls_id;
		// load values
		if (obj.isFlagged(MObject.ATTRIB_FLAG_VALUES)) {
			Iterator<String> it = o.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				// Ignore the Object Id which is given in object DBInfo
				if (key.equals("_id"))
					continue;
				//remove key's first char '_'???
				String k = key.substring(1);
				obj.values.put(k, dbObjectToObject(o.get(key)));
			}
		}
	}

	@Override
	public void createObject(ObjectDBInfo obj) {
		// create : 1. type collection
		writeObjectClass(obj.id, obj.class_id);
		// create : 2. specific class collection
		String class_id = classIDToString(obj.class_id);
		DBCollection col = db.getCollection(class_id);
		DBObject o = new BasicDBObject();
		// Only create an "empty" object.
		o.put("_id", obj.id);
		col.insert(o);
	}

	@Override
	public void updateObject(ObjectDBInfo obj) {
		DBCollection col = db.getCollection(classIDToString(obj.class_id));
		DBObject o = new BasicDBObject();
		DBObject d = new BasicDBObject();
		DBObject que = new BasicDBObject();
		que.put("_id", obj.id);
		
		// valid existence
		checkExistence(col, obj.id);
		
		Iterator<Entry<String, Object>> it = obj.values.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Object> entry = it.next();
			Object value = entry.getValue();
			o.put("p" + entry.getKey(), objectToDBObject(value));
		}
		it = obj.deleteKeys.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Object> entry = it.next();
			o.put("p" + entry.getKey(), null);
		}
		
		// TODO
		DBObject set = new BasicDBObject();
		set.put("$set", o);
		set.put("$unset", d);
		col.update(que, set);
	}
	
	@Override
	public void deleteObject(ObjectDBInfo obj) {
		DBCollection col = db.getCollection(classIDToString(obj.class_id));
		DBCollection ecol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject que = new BasicDBObject();
		que.put("_id", obj.id);
		
		// valid existence
		checkExistence(col, obj.id);
		
		col.remove(que);
		ecol.remove(que);
	}

	@Override
	public void loadTag(TagDBInfo tag) {
		DBCollection col = db.getCollection(COLLECT_NAME_TAG);
		DBObject fields = new BasicDBObject();
		if (tag.isFlagged(MTag.ATTRIB_FLAG_NAME))
			fields.put("name", true);
		if (tag.isFlagged(MTag.ATTRIB_FLAG_VALUE))
			fields.put("value", true);
		DBObject obj = col.findOne(tag.id, fields);
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			if (obj == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		
		// name
		if (tag.isFlagged(MTag.ATTRIB_FLAG_NAME))
			tag.name = (String) obj.get("name");
		// value
		if (tag.isFlagged(MTag.ATTRIB_FLAG_VALUE))
			tag.value = dbObjectToObject(obj.get("value"));
	}

	@Override
	public void createTag(TagDBInfo tag) {
		// create : 1. type collection
		writeElementType(tag.id, MElementType.Tag);
		// create : 2. specific class collection
		DBCollection col = db.getCollection(COLLECT_NAME_TAG);
		DBObject obj = new BasicDBObject();
		obj.put("_id", tag.id);
		obj.put("name", tag.name);
		obj.put("value", objectToDBObject(tag.value));
		col.insert(obj);
	}

	@Override
	public void updateTag(TagDBInfo tag) {
		DBCollection col = db.getCollection(COLLECT_NAME_TAG);
		DBObject que = new BasicDBObject();
		DBObject obj = new BasicDBObject();
		que.put("_id", tag.id);
	
		// double check existence
		checkExistence(col, tag.id);
		
		if (tag.isFlagged(MTag.ATTRIB_FLAG_NAME))
			obj.put("name", tag.name);
		if (tag.isFlagged(MTag.ATTRIB_FLAG_VALUE))
			obj.put("value", objectToDBObject(tag.value));
		DBObject set = new BasicDBObject();
		set.put("$set", obj);
		col.update(que, set);
	}

	@Override
	public void deleteTag(TagDBInfo tag) {
		DBCollection col = db.getCollection(COLLECT_NAME_TAG);
		DBCollection ecol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject que = new BasicDBObject();
		
		// valid existence
		checkExistence(col, tag.id);
		
		que.put("_id", tag.id);
		col.remove(que);
		ecol.remove(que);
	}
	
	@Override
	public void loadElementTags(long id, IDList list) {
		DBCollection eleCol = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject eleObj = eleCol.findOne(id);
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			if (eleObj == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		BasicDBList tags = (BasicDBList) eleObj.get("tags");
		if (tags == null)
			return;
		Iterator<Object> it = tags.iterator();
		while (it.hasNext()) {
			Long tid = (Long) it.next();
			list.add(tid);
		}
	}

	@Override
	public void saveElementTags(long id, IDList list) {
		DBCollection col = db.getCollection(COLLECT_NAME_ELEMENT);
		DBObject que = new BasicDBObject();
		que.put("_id", id);
		
		BasicDBList l = new BasicDBList();
		Iterator<Long> it = list.iterator();
		while (it.hasNext()) {
			Long tid = it.next();
			l.add(tid);
		}
		
		DBObject obj = new BasicDBObject().append("tags", l);
		DBObject setter = new BasicDBObject();
		setter.put("$set", obj);
		col.update(que, setter);
	}

	@Override
	public void loadTagElements(long id, IDList list) {
		DBCollection tagCol = db.getCollection(COLLECT_NAME_TAG);
		DBObject field = new BasicDBObject().append("targets", 1);
		DBObject tagObj = tagCol.findOne(id, field);
		// double check existence
		if (ENABLE_DOUBLE_CHECK_EXISTENCE) {
			if (tagObj == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		
		BasicDBList targets = (BasicDBList) tagObj.get("targets");
		Iterator<Object> it = targets.iterator();
		while (it.hasNext()) {
			Long tid = (Long) it.next();
			list.add(tid);
		}
	}
	
	@Override
	public void saveTagElements(long id, IDList list) {
		DBCollection col = db.getCollection(COLLECT_NAME_TAG);
		DBObject que = new BasicDBObject();
		DBObject obj = new BasicDBObject();
		que.put("_id", id);
	
		// double check existence
		checkExistence(col, id);
		
		BasicDBList targets = new BasicDBList();
		Iterator<Long> it = list.iterator();
		while (it.hasNext()) {
			Long tid = it.next();
			targets.add(tid);
		}
		obj.put("targets", targets);
		DBObject set = new BasicDBObject();
		set.put("$set", obj);
		col.update(que, set);
	}
	
	@Override
	public long getObjectClass(long obj_id) {
		return readObjectClass(obj_id);
	}

	final static String KEY_DICT = "dict";
	final static String KEY_LIST = "list";
	final static String KEY_SET = "set";
	final static String KEY_OBJECT = "o";
	final static String KEY_SYMBOL = "s";
	final static String KEY_REF = "r";
	final static String KEY_CODE = "c";
	final static String KEY_ELEMENT = "e";
	
	/**
	 * Convert the object in meteor system to DBObject or the object that can store
	 * in MongoDB.
	 * @param obj Object in meteor system.
	 * @return Object that can store in MongoDB.
	 */
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
		} else if (value instanceof MBinary) {
			value = new Binary(((MBinary) value).getData());
		} else if (value instanceof MRef) {
			value = refToDBObject((MRef) value);
		} else if (value instanceof MCode) {
			value = codeToDBObject((MCode) value);
		}
		return value;
	}
	
	/**
	 * Convert the object in MongoDB to the object in meteor system.
	 * @param obj Object that can store in MongoDB.
	 * @return Object in meteor system.
	 */
	private static Object dbObjectToObject(Object obj) {
		if (obj instanceof DBObject) {
			DBObject dbo = (DBObject) obj;
			if (dbo.containsField(KEY_DICT)) {
				return dbObjectToDataDict(dbo);
			} else if (dbo.containsField(KEY_LIST)) {
				return dbObjectToDataList(dbo);
			} else if (dbo.containsField(KEY_SET)) {
				return dbObjectToDataSet(dbo);
			} else if (dbo.containsField(KEY_REF)) {
				return dbObjectToRef(dbo);
			} else if (dbo.containsField(KEY_CODE)) {
				return dbObjectToCode(dbo);
			} else {
				return dbObjectToElementPt(dbo);
			}
		} else if (obj instanceof Binary) {
			return new MBinary(((Binary) obj).getData());
		}
		return obj;
	}
	
	/**
	 * Convert a <code>DataDict</code> to <code>DBObject</code>.
	 * @param dd Data dictionary
	 * @return DBObject
	 */
	private static DBObject dataDictToDBObject(DataDict dd) {
		DBObject obj = new BasicDBObject();
		String key = KEY_DICT;
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
	
	/**
	 * Convert the <code>DBObject</code> to <code>DataDict</code>.
	 * @param obj DBObject
	 * @return Data dictionary
	 */
	private static DataDict dbObjectToDataDict(DBObject obj) {
		DataDict dd = new DataDict();
		DBObject dict = (DBObject) obj.get(KEY_DICT);
		Iterator<String> it = dict.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			dd.put(key, dbObjectToObject(dict.get(key)));
		}
		return dd;
	}
	
	private static DBObject dataListToDBObject(DataList dl) {
		DBObject obj = new BasicDBObject();
		String key = KEY_LIST;
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
		BasicDBList list = (BasicDBList) obj.get(KEY_LIST);
		Iterator<Object> it = list.iterator();
		while (it.hasNext()) {
			Object value = it.next();
			dl.add(dbObjectToObject(value));
		}
		return dl;
	}
	
	private static DBObject dataSetToDBObject(DataSet ds) {
		DBObject obj = new BasicDBObject();
		String key = KEY_SET;
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
		BasicDBList list = (BasicDBList) obj.get(KEY_SET);
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
			key = KEY_OBJECT;
			break;
		case Symbol:
			key = KEY_SYMBOL;
			break;
		default:
			key = KEY_ELEMENT;
			break;
		}
		obj.put(key, pt.getID());
		return obj;
	}
	
	private static MElementPointer dbObjectToElementPt(DBObject obj) {
		if (obj.containsField(KEY_OBJECT)) {
			Long id = (Long) obj.get(KEY_OBJECT);
			return new MElementPointer(id, MElementType.Object);
		} else if (obj.containsField(KEY_SYMBOL)) {
			Long id = (Long) obj.get(KEY_SYMBOL);
			return new MElementPointer(id, MElementType.Symbol);
		} else if (obj.containsField(KEY_ELEMENT)) {
			Long id = (Long) obj.get(KEY_ELEMENT);
			return new MElementPointer(id);
		}
		return new MElementPointer();
	}
	
	private static DBObject refToDBObject(MRef ref) {
		DBObject obj = new BasicDBObject();
		BasicDBList list = new BasicDBList();
		list.add(ref.getTarget().getID());
		list.add(ref.getField().getID());
		obj.put(KEY_REF, list);
		return obj;
	}
	
	private static MRef dbObjectToRef(DBObject obj) {
		BasicDBList list = (BasicDBList) obj.get(KEY_REF);
		long obj_id = (Long) list.get(0);
		long field_id = (Long) list.get(1);
		return new MRef(obj_id, field_id);
	}
	
	private static DBObject codeToDBObject(MCode code) {
		DBObject obj = new BasicDBObject();
		obj.put(KEY_CODE, code.getCode());
		return obj;
	}
	
	private static MCode dbObjectToCode(DBObject obj) {
		String code = (String) obj.get(KEY_CODE);
		return new MCode(code);
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
		
//		DBCollection eleCol = db.getCollection(COLLECT_NAME_ELEMENT);
//		eleCol.ensureIndex("type");
		
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
	
	@Override
	public void deleteAllObjects(long classID) {
		DBCollection col = db.getCollection(classIDToString(classID));
		col.drop();
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
