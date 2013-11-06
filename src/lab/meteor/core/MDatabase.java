package lab.meteor.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lab.meteor.core.MElement.MElementType;

public class MDatabase {
	
	// TODO
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
	/**
	 * The singleton.
	 */
	private static MDatabase database = null;
	
	/**
	 * Get the singleton of MDatabase.
	 * @return singleton
	 */
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
	/**
	 * Private constructor.
	 */
	private MDatabase() {
		metaElements = new HashMap<Long, MElement>();
		objectsCache = new HashMap<Long, MObject>();
		tagsCache = new HashMap<Long, MTag>();
		dbAdapter = null;
	}
	
	/*
	 * ********************************
	 *          MEMORY CACHE
	 * ********************************
	 */
	
	/**
	 * The meta of system, i.e. the model, include class, attribute, reference, 
	 * enum and symbol(enumeration literal).
	 */
	private Map<Long, MElement> metaElements;
	
	/**
	 * The objects' cache.
	 */
	private Map<Long, MObject> objectsCache;
	
	/**
	 * The tags' cache.
	 */
	private Map<Long, MTag> tagsCache;
	
	/**
	 * Get a meta element with id.
	 * @param id
	 * @return the model, which could be class, attribute, reference, enum
	 * and symbol(enumeration literal).
	 */
	protected MElement getMetaElement(long id) {
		return metaElements.get(id);
	}
	
	/**
	 * Get an object with id.
	 * @param id
	 * @return the object.
	 */
	protected MObject getObjectElement(long id) {
		return objectsCache.get(id);
	}
	
	/**
	 * Get a tag with id.
	 * @param id
	 * @return the tag.
	 */
	protected MTag getTagElement(long id) {
		return tagsCache.get(id);
	}
	
	/**
	 * Check that an element has been loaded into cache.
	 * @param id
	 * @return true if element with specific id is in cache.
	 */
	protected boolean isElementLoaded(long id) {
		if (metaElements.containsKey(id))
			return true;
		if (objectsCache.containsKey(id))
			return true;
		if (tagsCache.containsKey(id))
			return true;
		return false;
	}
	
	/**
	 * Get an element in cache.
	 * @param id
	 * @return
	 */
	protected MElement getElement(long id) {
		MElement ele = metaElements.get(id);
		if (ele == null)
			ele = objectsCache.get(id);
		if (ele == null)
			ele = tagsCache.get(id);
		return ele;
	}
	
	/**
	 * Add an element into system cache.
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
	 * Remove an element from system cache.
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
	 * DB adapter. The adapter contributes a data storage system for the system.
	 */
	private MDBAdapter dbAdapter = null;
	
	/*
	 * ********************************
	 *         INITIALIZATION
	 * ********************************
	 */
	
	/**
	 * Initialize the database, include : <br>
	 * 1. Prepare the data storage with call of <code>MDBAdapter.checkAndPrepareDB()</code>;<br>
	 * 2. Load model (all meta elements, i.g. classes, attributes, enumes).<br>
	 * This method must be called before using the system.
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
		// load all references
		List<Long> refIDList = this.dbAdapter.listAllReferenceIDs();
		for (Long refID : refIDList) {
			this.getReference(refID);
		}
		// load all symbols
		List<Long> symIDList = this.dbAdapter.listAllSymbolIDs();
		for (Long symID : symIDList) {
			this.getSymbol(symID);
		}
	}
	
	/**
	 * Reset the system, include cache and storage.
	 * This method calls <code>MDBAdapter.resetDB()</code>.
	 */
	public void reset() {
		if (this.dbAdapter == null)
			return;
		this.dbAdapter.resetDB();
		this.metaElements.clear();
		this.objectsCache.clear();
		this.tagsCache.clear();
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
	 * Allocate a new ID (read the last ID in attached DB and increment the ID) for 
	 * an element. This method should be called only by <code>MElement</code>'s constructor.
	 * @return the new ID
	 */
	protected long getNewID() {
		if (dbAdapter == null)
			throw new MException(MException.Reason.DB_ADAPTER_NOT_ATTACHED);
		long id = dbAdapter.loadLastIDAndIncrement();
		// in case of the id is NULL_ID
		if (id == MElement.NULL_ID)
			id = dbAdapter.loadLastIDAndIncrement();
		return id;
	}
	
	/**
	 * Get the element type of an element. If element with given ID is in cache, find it
	 * and return it's type. Otherwise, call <code>MDBAdapter.getElementType(long)</code>, which
	 * will find the element in database.
	 * @param id
	 * @return type of element.
	 */
	public MElementType getElementType(long id) {
		if (dbAdapter == null)
			throw new MException(MException.Reason.DB_ADAPTER_NOT_ATTACHED);
		if (this.metaElements.containsKey(id)) {
			MElement e = this.metaElements.get(id);
			return e.getElementType();
		} else if (this.objectsCache.containsKey(id)) {
			return MElementType.Object;
		} else if (this.tagsCache.containsKey(id)) {
			return MElementType.Tag;
		} else {
			MElementType type = this.dbAdapter.getElementType(id);
			return type;
		}
	}
	
	/**
	 * Check whether the element with specific ID is exist and if exist, whether the type of
	 * element is the expect type. Throw a <code>MException</code> if not. This method always
	 * check the information in database, instead of cache.
	 * @param id the specific ID.
	 * @param type the expect type.
	 */
	private void checkExistenceAndType(long id, MElementType type) {
		MElementType t = dbAdapter.getElementType(id);
		if (t == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		if (t != type)
			throw new MException(MException.Reason.MISMATCHED_ELEMENT_TYPE);
	}
	
	/**
	 * Check whether the element with specific ID is exist. Throw a <code>MException</code> if not.
	 * This method always check the information in database, instead of cache.
	 * @param id the specific ID.
	 */
	private void checkExistence(long id) {
		MElementType t = dbAdapter.getElementType(id);
		if (t == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
	}
	
	/**
	 * Check whether there is already an element in database with specific ID. If there is,
	 * throw a <code>MException</code>.
	 * @param id the specific ID.
	 */
	private void checkConflict(long id) {
		MElementType t = dbAdapter.getElementType(id);
		if (t != null)
			throw new MException(MException.Reason.ELEMENT_CONFILICT);
	}
	
	/**
	 * Load the element information from database.
	 * @param ele The element to be loaded.
	 */
	void loadElement(MElement ele, int flag) {
		if (dbAdapter == null)
			throw new MException(MException.Reason.DB_ADAPTER_NOT_ATTACHED);
		if (ele == null)
			return;
		checkExistenceAndType(ele.id, ele.getElementType());
		
		switch (ele.getElementType()) {
		case Package:
			MPackage pkg = (MPackage) ele;
			MDBAdapter.PackageDBInfo pkgDBInfo = new MDBAdapter.PackageDBInfo(flag);
			pkgDBInfo.id = pkg.id;
			dbAdapter.loadPackage(pkgDBInfo);
			pkg.loadFromDBInfo(pkgDBInfo);
			break;
		case Class:
			MClass cls = (MClass) ele;
			MDBAdapter.ClassDBInfo clsDBInfo = new MDBAdapter.ClassDBInfo(flag);
			clsDBInfo.id = cls.id;
			dbAdapter.loadClass(clsDBInfo);
			cls.loadFromDBInfo(clsDBInfo);
			break;
		case Enum:
			MEnum enm = (MEnum) ele;
			MDBAdapter.EnumDBInfo enmDBInfo = new MDBAdapter.EnumDBInfo(flag);
			enmDBInfo.id = enm.id;
			dbAdapter.loadEnum(enmDBInfo);
			enm.loadFromDBInfo(enmDBInfo);
			break;
		case Attribute:
			MAttribute atb = (MAttribute) ele;
			MDBAdapter.AttributeDBInfo atbDBInfo = new MDBAdapter.AttributeDBInfo(flag);
			atbDBInfo.id = atb.id;
			dbAdapter.loadAttribute(atbDBInfo);
			atb.loadFromDBInfo(atbDBInfo);
			break;
		case Symbol:
			MSymbol sym = (MSymbol) ele;
			MDBAdapter.SymbolDBInfo symDBInfo = new MDBAdapter.SymbolDBInfo(flag);
			symDBInfo.id = sym.id;
			dbAdapter.loadSymbol(symDBInfo);
			sym.loadFromDBInfo(symDBInfo);
			break;
		case Reference:
			MReference rol = (MReference) ele;
			MDBAdapter.ReferenceDBInfo refDBInfo = new MDBAdapter.ReferenceDBInfo(flag);
			refDBInfo.id = rol.id;
			dbAdapter.loadReference(refDBInfo);
			rol.loadFromDBInfo(refDBInfo);
			break;
		case Object:
			MObject obj = (MObject) ele;
			MDBAdapter.ObjectDBInfo objDBInfo = new MDBAdapter.ObjectDBInfo(flag);
			objDBInfo.id = obj.id;
			dbAdapter.loadObject(objDBInfo);
			obj.loadFromDBInfo(objDBInfo);
			break;
		case Tag:
			MTag tag = (MTag) ele;
			MDBAdapter.TagDBInfo tagDBInfo = new MDBAdapter.TagDBInfo(flag);
			tagDBInfo.id = tag.id;
			dbAdapter.loadTag(tagDBInfo);
			tag.loadFromDBInfo(tagDBInfo);
			break;
		default:
			throw new MException(MException.Reason.NOT_SUPPORT_YET);
		}
	}
	
	/**
	 * Save the element information from database.
	 * @param ele The element to be saved.
	 */
	void saveElement(MElement ele, int flag) {
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
				MDBAdapter.PackageDBInfo pkgDBInfo = new MDBAdapter.PackageDBInfo(flag);
				pkg.saveToDBInfo(pkgDBInfo);
				dbAdapter.updatePackage(pkgDBInfo);
				break;
			case Class:
				MClass cls = (MClass) ele;
				MDBAdapter.ClassDBInfo clsDBInfo = new MDBAdapter.ClassDBInfo(flag);
				cls.saveToDBInfo(clsDBInfo);
				dbAdapter.updateClass(clsDBInfo);
				break;
			case Enum:
				MEnum enm = (MEnum) ele;
				MDBAdapter.EnumDBInfo enmDBInfo = new MDBAdapter.EnumDBInfo(flag);
				enm.saveToDBInfo(enmDBInfo);
				dbAdapter.updateEnum(enmDBInfo);
				break;
			case Attribute:
				MAttribute atb = (MAttribute) ele;
				MDBAdapter.AttributeDBInfo atbDBInfo = new MDBAdapter.AttributeDBInfo(flag);
				atb.saveToDBInfo(atbDBInfo);
				dbAdapter.updateAttribute(atbDBInfo);
				break;
			case Symbol:
				MSymbol sym = (MSymbol) ele;
				MDBAdapter.SymbolDBInfo symDBInfo = new MDBAdapter.SymbolDBInfo(flag);
				sym.saveToDBInfo(symDBInfo);
				dbAdapter.updateSymbol(symDBInfo);
				break;
			case Reference:
				MReference rol = (MReference) ele;
				MDBAdapter.ReferenceDBInfo refDBInfo = new MDBAdapter.ReferenceDBInfo(flag);
				rol.saveToDBInfo(refDBInfo);
				dbAdapter.updateReference(refDBInfo);
				break;
			case Object:
				MObject obj = (MObject) ele;
				MDBAdapter.ObjectDBInfo objDBInfo = new MDBAdapter.ObjectDBInfo(flag);
				obj.saveToDBInfo(objDBInfo);
				dbAdapter.updateObject(objDBInfo);
				break;
			case Tag:
				MTag tag = (MTag) ele;
				MDBAdapter.TagDBInfo tagDBInfo = new MDBAdapter.TagDBInfo(flag);
				tag.saveToDBInfo(tagDBInfo);
				dbAdapter.updateTag(tagDBInfo);
				break;
			default:
				throw new MException(MException.Reason.NOT_SUPPORT_YET);
			}
		}
	}
	
	/**
	 * Create a new element and save it into database.
	 * @param ele The element to be created.
	 */
	void createElement(MElement ele) {
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
		case Reference:
			MReference rol = (MReference) ele;
			MDBAdapter.ReferenceDBInfo refDBInfo = new MDBAdapter.ReferenceDBInfo();
			rol.saveToDBInfo(refDBInfo);
			dbAdapter.createReference(refDBInfo);
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
	
	/**
	 * Delete an element from database.
	 * @param ele The element to be deleted.
	 */
	void deleteElement(MElement ele) {
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
		case Reference:
			MDBAdapter.ReferenceDBInfo refDBInfo = new MDBAdapter.ReferenceDBInfo();
			refDBInfo.id = ele.id;
			dbAdapter.deleteReference(refDBInfo);
			break;
		case Object:
			MDBAdapter.ObjectDBInfo objDBInfo = new MDBAdapter.ObjectDBInfo();
			objDBInfo.id = ele.id;
			objDBInfo.class_id = ((MObject) ele).getClazzID();
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
	
	/**
	 * Load the tags of an element.
	 * @param ele The element.
	 */
	void loadElementTags(MElement ele) {
		if (dbAdapter == null)
			throw new MException(MException.Reason.DB_ADAPTER_NOT_ATTACHED);
		if (ele == null)
			return;
		if (ele.isDeleted())
			return;
		checkExistence(ele.id);

		MDBAdapter.IDList idList = new MDBAdapter.IDList();
		this.dbAdapter.loadElementTags(ele.id, idList);
		ele.loadTagsFromDBInfo(idList);
	}
	
	/**
	 * Save the tags of an element.
	 * @param ele The element.
	 */
	void saveElementTags(MElement ele) {
		if (dbAdapter == null)
			throw new MException(MException.Reason.DB_ADAPTER_NOT_ATTACHED);
		if (ele == null)
			return;
		if (ele.isDeleted())
			return;
		checkExistence(ele.id);
		
		MDBAdapter.IDList idList = new MDBAdapter.IDList();
		ele.saveTagsToDBInfo(idList);
		this.dbAdapter.saveElementTags(ele.id, idList);
	}
	
	void loadTagElements(MTag tag) {
		if (dbAdapter == null)
			throw new MException(MException.Reason.DB_ADAPTER_NOT_ATTACHED);
		if (tag == null)
			return;
		if (tag.isDeleted())
			return;
		
		MDBAdapter.IDList idList = new MDBAdapter.IDList();
		this.dbAdapter.loadTagElements(tag.id, idList);
		tag.loadElementsFromDBInfo(idList);
	}
	
	void saveTagElements(MTag tag) {
		if (dbAdapter == null)
			throw new MException(MException.Reason.DB_ADAPTER_NOT_ATTACHED);
		if (tag == null)
			return;
		if (tag.isDeleted())
			return;
		
		MDBAdapter.IDList idList = new MDBAdapter.IDList();
		tag.saveElementsToDBInfo(idList);
		this.dbAdapter.saveTagElements(tag.id, idList);
	}
	
	void preloadTagName(MTag tag) {
		if (dbAdapter == null)
			throw new MException(MException.Reason.DB_ADAPTER_NOT_ATTACHED);
		if (tag == null)
			return;
		if (tag.isDeleted())
			return;
		
		MDBAdapter.TagDBInfo tagDBInfo = new MDBAdapter.TagDBInfo(MTag.ATTRIB_FLAG_NAME);
		tagDBInfo.id = tag.id;
		dbAdapter.loadTag(tagDBInfo);
		
	}
	
	List<Long> listAllObjectsID(MClass cls) {
		if (dbAdapter == null)
			throw new MException(MException.Reason.DB_ADAPTER_NOT_ATTACHED);
		if (cls == null)
			return null;
		
		return dbAdapter.listAllObjectIDs(cls.id);
	}
	
	void deleteAllObjects(MClass cls) {
		if (dbAdapter == null)
			throw new MException(MException.Reason.DB_ADAPTER_NOT_ATTACHED);
		if (cls == null)
			return;
		
		dbAdapter.deleteAllObjects(cls.id);
	}
	
	/**
	 * Get package with specific ID. Check if the element is in cache first, and then
	 * if not, load the element from database. If the element is not exist, there will be
	 * a <code>MException</code> to be thrown.
	 * @param id The specific ID.
	 * @return package
	 */
	protected MPackage getPackage(long id) {
		if (id == MElement.NULL_ID)
			return MPackage.DEFAULT_PACKAGE;
		MElement meta = metaElements.get(id);
		MPackage pkg = null;
		if (meta == null) {
			pkg = new MPackage(id);
			pkg.forceLoad();
			metaElements.put(id, pkg);
		} else if (meta instanceof MPackage) {
			pkg = (MPackage) meta;
		} else {
			throw new MException(MException.Reason.MISMATCHED_ELEMENT_TYPE);
		}
		return pkg;
	}

	/**
	 * Get class with specific ID. Check if the element is in cache first, and then
	 * if not, load the element from database. If the element is not exist, there will be
	 * a <code>MException</code> to be thrown.
	 * @param id The specific ID.
	 * @return class
	 */
	protected MClass getClass(long id) {
		if (id == MElement.NULL_ID)
			return null;
		MElement meta = metaElements.get(id);
		MClass cls = null;
		if (meta == null) {
			cls = new MClass(id);
			cls.forceLoad();
			metaElements.put(id, cls);
		} else if (meta instanceof MClass) {
			cls = (MClass) meta;
		} else {
			throw new MException(MException.Reason.MISMATCHED_ELEMENT_TYPE);
		}
		return cls;
	}
	
	/**
	 * Get attribute with specific ID. Check if the element is in cache first, and then
	 * if not, load the element from database. If the element is not exist, there will be
	 * a <code>MException</code> to be thrown.
	 * @param id The specific ID.
	 * @return attribute
	 */
	protected MAttribute getAttribute(long id) {
		if (id == MElement.NULL_ID)
			return null;
		MElement meta = metaElements.get(id);
		MAttribute atb = null;
		if (meta == null) {
			atb = new MAttribute(id);
			atb.forceLoad();
			metaElements.put(id, atb);
		} else if (meta instanceof MAttribute) {
			atb = (MAttribute) meta;
		} else {
			throw new MException(MException.Reason.MISMATCHED_ELEMENT_TYPE);
		}
		return atb;
	}
	
	/**
	 * Get reference with specific ID. Check if the element is in cache first, and then
	 * if not, load the element from database. If the element is not exist, there will be
	 * a <code>MException</code> to be thrown.
	 * @param id The specific ID.
	 * @return reference
	 */
	protected MReference getReference(long id) {
		if (id == MElement.NULL_ID)
			return null;
		MElement meta = metaElements.get(id);
		MReference ref = null;
		if (meta == null) {
			ref = new MReference(id);
			ref.forceLoad();
			metaElements.put(id, ref);
		} else if (meta instanceof MAttribute) {
			ref = (MReference) meta;
		} else {
			throw new MException(MException.Reason.MISMATCHED_ELEMENT_TYPE);
		}
		return ref;
	}
	
	/**
	 * Get enum with specific ID. Check if the element is in cache first, and then
	 * if not, load the element from database. If the element is not exist, there will be
	 * a <code>MException</code> to be thrown.
	 * @param id The specific ID.
	 * @return enum
	 */
	protected MEnum getEnum(long id) {
		if (id == MElement.NULL_ID)
			return null;
		MElement meta = metaElements.get(id);
		MEnum enm = null;
		if (meta == null) {
			enm = new MEnum(id);
			enm.forceLoad();
			metaElements.put(id, enm);
		} else if (meta instanceof MEnum) {
			enm = (MEnum) meta;
		} else {
			throw new MException(MException.Reason.MISMATCHED_ELEMENT_TYPE);
		}
		return enm;
	}
	
	/**
	 * Get symbol with specific ID. Check if the element is in cache first, and then
	 * if not, load the element from database. If the element is not exist, there will be
	 * a <code>MException</code> to be thrown.
	 * @param id The specific ID.
	 * @return symbol
	 */
	protected MSymbol getSymbol(long id) {
		if (id == MElement.NULL_ID)
			return null;
		MElement meta = metaElements.get(id);
		MSymbol sym = null;
		if (meta == null) {
			sym = new MSymbol(id);
			sym.forceLoad();
			metaElements.put(id, sym);
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
	 * Get the object by id. The loading will also load the object into cache.
	 * @param id The ID of object.
	 * @return object
	 */
	protected MObject getObject(long id) {
		MObject obj = getLazyObject(id);
		if (obj == null)
			return null;
		try {
			if (!obj.isLoaded())
				obj.forceLoad();
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
	 * content of "lazy" object is empty. Call the {@code MDatabase.loadObject(MObject)}
	 * to load the content.
	 * @param id The ID of object.
	 * @return A lazy object if it's not in cache, otherwise an object with complete content.
	 */
	protected MObject getLazyObject(long id) {
		if (id == MElement.NULL_ID)
			return null;
		MObject obj = objectsCache.get(id);
		if (obj == null) {
			obj = new MObject(id);
			long cls_id = this.dbAdapter.getObjectClass(id);
			obj.class_pt.setPointer(this.getClass(cls_id));
			objectsCache.put(id, obj);
		}
		return obj;
	}
	
	/**
	 * Get the tag by id. The loading will also load the tag into cache.
	 * @param id The ID of tag.
	 * @return tag
	 */
	protected MTag getTag(long id) {
		MTag tag = getLazyTag(id);
		if (tag == null)
			return null;
		try {
			if (!tag.isLoaded())
				tag.forceLoad();
		} catch (MException e) {
			if (e.getReason() == MException.Reason.ELEMENT_MISSED) {
				tag.delete();
				return null;
			} else
				throw e;
		}
		return tag;
	}
	
	/**
	 * Load the tag by id, but if the tag has not been loaded, a new tag that
	 * only has the ID handler will be created, which is named as a "lazy" tag. The 
	 * content of "lazy" tag is empty. Call the {@code MDatabase.loadTag(MTag)}
	 * to load the content.
	 * @param id The ID of tag.
	 * @return A lazy tag if it's not in cache, otherwise a tag with complete content.
	 */
	protected MTag getLazyTag(long id) {
		if (id == MElement.NULL_ID)
			return null;
		MTag tag = tagsCache.get(id);
		if (tag == null) {
			tag = new MTag(id);
			tagsCache.put(id, tag);
		}
		return tag;
	}
	
	/**
	 * Get all objects of a class.
	 * @param class_id The ID of class.
	 * @return A list of objects.
	 */
	protected List<Long> getObjects(long class_id) {
		return this.dbAdapter.listAllObjectIDs(class_id);
	}
}
