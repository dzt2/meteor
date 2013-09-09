package lab.meteor.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lab.meteor.core.MElement.MElementType;

public class MDatabase {
	
	private static Pattern validNamePattern = Pattern.compile("[a-zA-Z0-9_ ]");
	public static boolean validateName(String name) {
		Matcher matcher = validNamePattern.matcher(name);
		return matcher.matches();
	}
	
	/*
	 * ********************************
	 *            SINGLETON
	 * ********************************
	 */
	private static MDatabase database = null;
	
	public static MDatabase getDB() {
		if (database == null)
			database = new MDatabase();
		return database;
	}
	
	/*
	 * ********************************
	 *           CONSTRUCTOR
	 * ********************************
	 */
	private MDatabase() {
		objectsCache = new HashMap<Long, MObject>();
		metaElements = new HashMap<Long, MElement>();
		dbAdapter = null;
	}
	
	/*
	 * ********************************
	 *          MEMORY CACHE
	 * ********************************
	 */
	
	/**
	 * The meta of system, i.e. the model, include class, attribute, enumeration
	 * and symbol.
	 */
	private Map<Long, MElement> metaElements;
	
	/**
	 * The objects' cache.
	 */
	private Map<Long, MObject> objectsCache;
	
	private Map<Long, MTag> tagsCache;
	
	/**
	 * Get a meta element through id.
	 * @param id
	 * @return
	 */
	protected MElement getMetaElement(long id) {
		return metaElements.get(id);
	}
	
	protected MObject getObjectElement(long id) {
		return objectsCache.get(id);
	}
	
	protected MTag getTagElement(long id) {
		return tagsCache.get(id);
	}
	
	protected boolean isElementLoaded(long id) {
		if (metaElements.containsKey(id))
			return true;
		if (objectsCache.containsKey(id))
			return true;
		if (tagsCache.containsKey(id))
			return true;
		return false;
	}
	
	protected MElement getElement(long id) {
		MElement ele = metaElements.get(id);
		if (ele == null)
			ele = objectsCache.get(id);
		if (ele == null)
			ele = tagsCache.get(id);
		return ele;
	}
	
	/**
	 * Add an element into system.
	 * @param meta
	 */
	protected void addElement(MElement ele) {
		if (ele instanceof MObject)
			objectsCache.put(ele.id, (MObject) ele);
		else if (ele instanceof MTag)
			tagsCache.put(ele.id, (MTag) ele);
		else
			metaElements.put(ele.id, ele);
	}
	
	/**
	 * Remove an element from system.
	 * @param meta
	 */
	protected void removeElement(MElement ele) {
		if (ele instanceof MObject)
			objectsCache.remove(ele.id);
		else if (ele instanceof MTag)
			tagsCache.remove(ele.id);
		else
			metaElements.remove(ele.id);
	}
	
	/*
	 * ********************************
	 *            DATABASE
	 * ********************************
	 */
	
	/**
	 * DB adapter. The adapter contributes a outside DB basement for the system.
	 */
	private MDBAdapter dbAdapter = null;
	
	/*
	 * ********************************
	 *         INITIALIZATION
	 * ********************************
	 */
	
	public void initialize() {
		if (dbAdapter == null)
			throw new MException(MException.Reason.DB_ADAPTER_NOT_ATTACHED);
		
		dbAdapter.checkAndPrepareDB();
		
		// load all packages
		List<Long> pkgIDList = this.dbAdapter.listAllPackageIDs();
		for (Long pkgID : pkgIDList) {
			this.getPackage(pkgID);
		}
		// load all classes
		List<Long> clsIDList = this.dbAdapter.listAllClassIDs();
		for (Long clsID : clsIDList) {
			this.getClass(clsID);
		}
		// load all enumes
		List<Long> enmIDList = this.dbAdapter.listAllEnumIDs();
		for (Long enmID : enmIDList) {
			this.getEnum(enmID);
		}
		// load all attributes
		List<Long> atbIDList = this.dbAdapter.listAllAttributeIDs();
		for (Long atbID : atbIDList) {
			this.getAttribute(atbID);
		}
		// load all symbols
		List<Long> symIDList = this.dbAdapter.listAllSymbolIDs();
		for (Long symID : symIDList) {
			this.getSymbol(symID);
		}
	}
	
	/*
	 * ********************************
	 *            DATABASE
	 * ********************************
	 */
	
	/**
	 * Set the DB adapter.
	 * @param adapter
	 */
	public void setDBAdapter(MDBAdapter adapter) {
		this.dbAdapter = adapter;
	}
	
	/**
	 * The DB adapter that has been attached.
	 * @return
	 */
	public MDBAdapter getDBAdapter() {
		return this.dbAdapter;
	}
	
	/**
	 * Assign a new ID (read the last ID in attached DB and increment the ID) to 
	 * a data. This method should be called only by MData's constructor.
	 * @return the new ID
	 * @throws MException
	 */
	protected long getNewID() throws MException {
		if (dbAdapter == null)
			throw new MException(MException.Reason.DB_ADAPTER_NOT_ATTACHED);
		long id = dbAdapter.loadLastIDAndIncrement();
		// in case of the id is NULL_ID
		if (id == MElement.NULL_ID)
			id = dbAdapter.loadLastIDAndIncrement();
		return id;
	}
	
	public MElementType getElementType(long id) {
		if (dbAdapter == null)
			throw new MException(MException.Reason.DB_ADAPTER_NOT_ATTACHED);
		MElementType type = this.dbAdapter.getElementType(id);
		return type;
	}
	
	private void checkExistenceAndType(long id, MElementType type) throws MException {
		MElementType t = dbAdapter.getElementType(id);
		if (t == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		if (t != type)
			throw new MException(MException.Reason.MISMATCHED_ELEMENT_TYPE);
	}
	
	private void checkExistence(long id) {
		MElementType t = dbAdapter.getElementType(id);
		if (t == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
	}
	
	private void checkConflict(long id) throws MException {
		MElementType t = dbAdapter.getElementType(id);
		if (t != null)
			throw new MException(MException.Reason.ELEMENT_CONFILICT);
	}
	
	protected void loadElement(MElement ele) {
		if (dbAdapter == null)
			throw new MException(MException.Reason.DB_ADAPTER_NOT_ATTACHED);
		if (ele == null)
			return;
		checkExistenceAndType(ele.id, ele.getElementType());
		
		switch (ele.getElementType()) {
		case Package:
			MPackage pkg = (MPackage) ele;
			MDBAdapter.PackageDBInfo pkgDBInfo = new MDBAdapter.PackageDBInfo();
			pkgDBInfo.id = pkg.id;
			dbAdapter.loadPackage(pkgDBInfo);
			pkg.loadFromDBInfo(pkgDBInfo);
			break;
		case Class:
			MClass cls = (MClass) ele;
			MDBAdapter.ClassDBInfo clsDBInfo = new MDBAdapter.ClassDBInfo();
			clsDBInfo.id = cls.id;
			dbAdapter.loadClass(clsDBInfo);
			cls.loadFromDBInfo(clsDBInfo);
			break;
		case Enum:
			MEnum enm = (MEnum) ele;
			MDBAdapter.EnumDBInfo enmDBInfo = new MDBAdapter.EnumDBInfo();
			enmDBInfo.id = enm.id;
			dbAdapter.loadEnum(enmDBInfo);
			enm.loadFromDBInfo(enmDBInfo);
			break;
		case Attribute:
			MAttribute atb = (MAttribute) ele;
			MDBAdapter.AttributeDBInfo atbDBInfo = new MDBAdapter.AttributeDBInfo();
			atbDBInfo.id = atb.id;
			dbAdapter.loadAttribute(atbDBInfo);
			atb.loadFromDBInfo(atbDBInfo);
			break;
		case Symbol:
			MSymbol sym = (MSymbol) ele;
			MDBAdapter.SymbolDBInfo symDBInfo = new MDBAdapter.SymbolDBInfo();
			symDBInfo.id = sym.id;
			dbAdapter.loadSymbol(symDBInfo);
			sym.loadFromDBInfo(symDBInfo);
			break;
		case Role:
			MRole rol = (MRole) ele;
			MDBAdapter.RoleDBInfo rolDBInfo = new MDBAdapter.RoleDBInfo();
			rolDBInfo.id = rol.id;
			dbAdapter.loadRole(rolDBInfo);
			rol.loadFromDBInfo(rolDBInfo);
			break;
		case Object:
			MObject obj = (MObject) ele;
			MDBAdapter.ObjectDBInfo objDBInfo = new MDBAdapter.ObjectDBInfo();
			objDBInfo.id = obj.id;
			dbAdapter.loadObject(objDBInfo);
			obj.loadFromDBInfo(objDBInfo);
			break;
		case Tag:
			MTag tag = (MTag) ele;
			MDBAdapter.TagDBInfo tagDBInfo = new MDBAdapter.TagDBInfo();
			tagDBInfo.id = tag.id;
			dbAdapter.loadTag(tagDBInfo);
			tag.loadFromDBInfo(tagDBInfo);
			break;
		default:
			throw new MException(MException.Reason.NOT_SUPPORT_YET);
		}
	}
	
	protected void saveElement(MElement ele) {
		if (dbAdapter == null)
			throw new MException(MException.Reason.DB_ADAPTER_NOT_ATTACHED);
		if (ele == null)
			return;
		if (ele.isDeleted())
			return;
		
		if (ele.isChanged() && ele.isLoaded()) {
			checkExistenceAndType(ele.id, ele.getElementType());
			
			switch (ele.getElementType()) {
			case Package:
				MPackage pkg = (MPackage) ele;
				MDBAdapter.PackageDBInfo pkgDBInfo = new MDBAdapter.PackageDBInfo();
				pkg.saveToDBInfo(pkgDBInfo);
				dbAdapter.updatePackage(pkgDBInfo);
				break;
			case Class:
				MClass cls = (MClass) ele;
				MDBAdapter.ClassDBInfo clsDBInfo = new MDBAdapter.ClassDBInfo();
				cls.saveToDBInfo(clsDBInfo);
				dbAdapter.updateClass(clsDBInfo);
				break;
			case Enum:
				MEnum enm = (MEnum) ele;
				MDBAdapter.EnumDBInfo enmDBInfo = new MDBAdapter.EnumDBInfo();
				enm.saveToDBInfo(enmDBInfo);
				dbAdapter.updateEnum(enmDBInfo);
				break;
			case Attribute:
				MAttribute atb = (MAttribute) ele;
				MDBAdapter.AttributeDBInfo atbDBInfo = new MDBAdapter.AttributeDBInfo();
				atb.saveToDBInfo(atbDBInfo);
				dbAdapter.updateAttribute(atbDBInfo);
				break;
			case Symbol:
				MSymbol sym = (MSymbol) ele;
				MDBAdapter.SymbolDBInfo symDBInfo = new MDBAdapter.SymbolDBInfo();
				sym.saveToDBInfo(symDBInfo);
				dbAdapter.updateSymbol(symDBInfo);
				break;
			case Role:
				MRole rol = (MRole) ele;
				MDBAdapter.RoleDBInfo rolDBInfo = new MDBAdapter.RoleDBInfo();
				rol.saveToDBInfo(rolDBInfo);
				dbAdapter.updateRole(rolDBInfo);
				break;
			case Object:
				MObject obj = (MObject) ele;
				MDBAdapter.ObjectDBInfo objDBInfo = new MDBAdapter.ObjectDBInfo();
				obj.saveToDBInfo(objDBInfo);
				dbAdapter.updateObject(objDBInfo);
				break;
			case Tag:
				MTag tag = (MTag) ele;
				MDBAdapter.TagDBInfo tagDBInfo = new MDBAdapter.TagDBInfo();
				tag.saveToDBInfo(tagDBInfo);
				dbAdapter.updateTag(tagDBInfo);
				break;
			default:
				throw new MException(MException.Reason.NOT_SUPPORT_YET);
			}
		}
	}
	
	protected void createElement(MElement ele) {
		if (dbAdapter == null)
			throw new MException(MException.Reason.DB_ADAPTER_NOT_ATTACHED);
		checkConflict(ele.id);
		
		switch (ele.getElementType()) {
		case Package:
			MPackage pkg = (MPackage) ele;
			MDBAdapter.PackageDBInfo pkgDBInfo = new MDBAdapter.PackageDBInfo();
			pkg.saveToDBInfo(pkgDBInfo);
			dbAdapter.createPackage(pkgDBInfo);
			break;
		case Class:
			MClass cls = (MClass) ele;
			MDBAdapter.ClassDBInfo clsDBInfo = new MDBAdapter.ClassDBInfo();
			cls.saveToDBInfo(clsDBInfo);
			dbAdapter.createClass(clsDBInfo);
			break;
		case Enum:
			MEnum enm = (MEnum) ele;
			MDBAdapter.EnumDBInfo enmDBInfo = new MDBAdapter.EnumDBInfo();
			enm.saveToDBInfo(enmDBInfo);
			dbAdapter.createEnum(enmDBInfo);
			break;
		case Attribute:
			MAttribute atb = (MAttribute) ele;
			MDBAdapter.AttributeDBInfo atbDBInfo = new MDBAdapter.AttributeDBInfo();
			atb.saveToDBInfo(atbDBInfo);
			dbAdapter.createAttribute(atbDBInfo);
			break;
		case Symbol:
			MSymbol sym = (MSymbol) ele;
			MDBAdapter.SymbolDBInfo symDBInfo = new MDBAdapter.SymbolDBInfo();
			sym.saveToDBInfo(symDBInfo);
			dbAdapter.createSymbol(symDBInfo);
			break;
		case Role:
			MRole rol = (MRole) ele;
			MDBAdapter.RoleDBInfo rolDBInfo = new MDBAdapter.RoleDBInfo();
			rol.saveToDBInfo(rolDBInfo);
			dbAdapter.createRole(rolDBInfo);
			break;
		case Object:
			MObject obj = (MObject) ele;
			MDBAdapter.ObjectDBInfo objDBInfo = new MDBAdapter.ObjectDBInfo();
			obj.saveToDBInfo(objDBInfo);
			dbAdapter.createObject(objDBInfo);
			break;
		case Tag:
			MTag tag = (MTag) ele;
			MDBAdapter.TagDBInfo tagDBInfo = new MDBAdapter.TagDBInfo();
			tag.saveToDBInfo(tagDBInfo);
			dbAdapter.createTag(tagDBInfo);
			break;
		default:
			throw new MException(MException.Reason.NOT_SUPPORT_YET);
		}
	}
	
	protected void deleteElement(MElement ele) {
		if (dbAdapter == null)
			throw new MException(MException.Reason.DB_ADAPTER_NOT_ATTACHED);
		if (ele == null)
			return;
		if (ele.isDeleted())
			return;
		checkExistenceAndType(ele.id, ele.getElementType());
		
		switch (ele.getElementType()) {
		case Package:
			MDBAdapter.PackageDBInfo pkgDBInfo = new MDBAdapter.PackageDBInfo();
			pkgDBInfo.id = ele.id;
			dbAdapter.deletePackage(pkgDBInfo);
			break;
		case Class:
			MDBAdapter.ClassDBInfo clsDBInfo = new MDBAdapter.ClassDBInfo();
			clsDBInfo.id = ele.id;
			dbAdapter.deleteClass(clsDBInfo);
			break;
		case Enum:
			MDBAdapter.EnumDBInfo enmDBInfo = new MDBAdapter.EnumDBInfo();
			enmDBInfo.id = ele.id;
			dbAdapter.deleteEnum(enmDBInfo);
			break;
		case Attribute:
			MDBAdapter.AttributeDBInfo atbDBInfo = new MDBAdapter.AttributeDBInfo();
			atbDBInfo.id = ele.id;
			dbAdapter.deleteAttribute(atbDBInfo);
			break;
		case Symbol:
			MDBAdapter.SymbolDBInfo symDBInfo = new MDBAdapter.SymbolDBInfo();
			symDBInfo.id = ele.id;
			dbAdapter.deleteSymbol(symDBInfo);
			break;
		case Role:
			MDBAdapter.RoleDBInfo rolDBInfo = new MDBAdapter.RoleDBInfo();
			rolDBInfo.id = ele.id;
			dbAdapter.deleteRole(rolDBInfo);
			break;
		case Object:
			MDBAdapter.ObjectDBInfo objDBInfo = new MDBAdapter.ObjectDBInfo();
			objDBInfo.id = ele.id;
			dbAdapter.deleteObject(objDBInfo);
			break;
		case Tag:
			MDBAdapter.TagDBInfo tagDBInfo = new MDBAdapter.TagDBInfo();
			tagDBInfo.id = ele.id;
			dbAdapter.deleteTag(tagDBInfo);
			break;
		default:
			throw new MException(MException.Reason.NOT_SUPPORT_YET);
		}
	}
	
	protected void loadElementTags(MElement ele) {
		if (dbAdapter == null)
			throw new MException(MException.Reason.DB_ADAPTER_NOT_ATTACHED);
		if (ele == null)
			return;
		if (ele.isDeleted())
			return;
		checkExistence(ele.id);
		
		MDBAdapter.ElementTagDBInfo dbInfo = new MDBAdapter.ElementTagDBInfo();
		dbInfo.id = ele.id;
		this.dbAdapter.loadElementTags(dbInfo);
		ele.loadTagsFromDBInfo(dbInfo);
	}
	
	protected void saveElementTags(MElement ele) {
		if (dbAdapter == null)
			throw new MException(MException.Reason.DB_ADAPTER_NOT_ATTACHED);
		if (ele == null)
			return;
		if (ele.isDeleted())
			return;
		checkExistence(ele.id);
		
		MDBAdapter.ElementTagDBInfo dbInfo = new MDBAdapter.ElementTagDBInfo();
		ele.saveTagsFromDBInfo(dbInfo);
		this.dbAdapter.saveElementTags(dbInfo);
	}
	
	public MPackage getPackage(long id) {
		if (id == MElement.NULL_ID)
			return MPackage.DEFAULT_PACKAGE;
		MElement meta = metaElements.get(id);
		MPackage pkg = null;
		if (meta == null) {
			pkg = new MPackage(id);
			metaElements.put(id, pkg);
			pkg.load();
		} else if (meta instanceof MPackage) {
			pkg = (MPackage) meta;
		} else {
			throw new MException(MException.Reason.MISMATCHED_ELEMENT_TYPE);
		}
		return pkg;
	}

	
	public MClass getClass(long id) {
		if (id == MElement.NULL_ID)
			return null;
		MElement meta = metaElements.get(id);
		MClass cls = null;
		if (meta == null) {
			cls = new MClass(id);
			metaElements.put(id, cls);
			cls.load();
		} else if (meta instanceof MClass) {
			cls = (MClass) meta;
		} else {
			throw new MException(MException.Reason.MISMATCHED_ELEMENT_TYPE);
		}
		return cls;
	}
	
	public MAttribute getAttribute(long id) {
		if (id == MElement.NULL_ID)
			return null;
		MElement meta = metaElements.get(id);
		MAttribute atb = null;
		if (meta == null) {
			atb = new MAttribute(id);
			metaElements.put(id, atb);
			atb.load();
		} else if (meta instanceof MAttribute) {
			atb = (MAttribute) meta;
		} else {
			throw new MException(MException.Reason.MISMATCHED_ELEMENT_TYPE);
		}
		return atb;
	}
	
	public MEnum getEnum(long id) {
		if (id == MElement.NULL_ID)
			return null;
		MElement meta = metaElements.get(id);
		MEnum enm = null;
		if (meta == null) {
			enm = new MEnum(id);
			metaElements.put(id, enm);
			enm.load();
		} else if (meta instanceof MEnum) {
			enm = (MEnum) meta;
		} else {
			throw new MException(MException.Reason.MISMATCHED_ELEMENT_TYPE);
		}
		return enm;
	}
	
	public MSymbol getSymbol(long id) {
		if (id == MElement.NULL_ID)
			return null;
		MElement meta = metaElements.get(id);
		MSymbol sym = null;
		if (meta == null) {
			sym = new MSymbol(id);
			metaElements.put(id, sym);
			sym.load();
		} else if (meta instanceof MSymbol) {
			sym = (MSymbol) meta;
		} else {
			throw new MException(MException.Reason.MISMATCHED_ELEMENT_TYPE);
		}
		return sym;
	}
	
	/*
	 * ********************************
	 *       DATABASE - OBJECT
	 * ********************************
	 */
	
	/**
	 * Load the object by id.
	 * @param id
	 * @return
	 * @throws MException
	 */
	public MObject getObject(long id) {
		MObject obj = getLazyObject(id);
		if (obj == null)
			return null;
		try {
			if (!obj.isLoaded())
				obj.load();
		} catch (MException e) {
			if (e.getReason() == MException.Reason.ELEMENT_MISSED) {
				obj.delete();
				return null;
			} else
				throw e;
		}
		return obj;
	}
	
	/**
	 * Load the object by id, but if the object has not been loaded, a new object that
	 * only has the ID handler will be created, which is named as a "lazy" object. The 
	 * content of "lazy" object is empty. Call the {@code loadObject(MObject)}
	 * to load the content.
	 * @param id
	 * @return
	 * @throws MException
	 */
	public MObject getLazyObject(long id) {
		if (id == MElement.NULL_ID)
			return null;
		MObject obj = objectsCache.get(id);
		if (obj == null) {
			obj = new MObject(id);
			objectsCache.put(id, obj);
		}
		return obj;
	}
	
	public MTag getTag(long id) {
		MTag tag = getLazyTag(id);
		if (tag == null)
			return null;
		try {
			if (!tag.isLoaded())
				tag.load();
		} catch (MException e) {
			if (e.getReason() == MException.Reason.ELEMENT_MISSED) {
				tag.delete();
				return null;
			} else
				throw e;
		}
		return tag;
	}
	
	public MTag getLazyTag(long id) {
		if (id == MElement.NULL_ID)
			return null;
		MTag tag = tagsCache.get(id);
		if (tag == null) {
			tag = new MTag(id);
			tagsCache.put(id, tag);
		}
		return tag;
	}
}
