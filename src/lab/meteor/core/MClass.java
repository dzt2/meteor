package lab.meteor.core;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import lab.meteor.core.MDBAdapter.DBInfo;

/**
 * The class of a kind of objects. <br>
 * A class contains several fields, which could be attribute
 * (<code>MAttribute</code>) or reference(<code>MReference</code>). The difference is that 
 * attribute's type can only be primitive type and enum, and reference's type can only be 
 * class (so it means referring to a class).
 * @author Qiang
 * @see MAttribute
 * @see MReference
 */
public class MClass extends MElement implements MType {

	/**
	 * Class's name.
	 */
	private String name;
	
	/**
	 * The attributes.
	 */
	private Map<String, MAttribute> attributes;
	/**
	 * The references.
	 */
	private Map<String, MReference> references;
	
	/**
	 * The super class.
	 */
	private MClass superclass;
	
	/**
	 * The package.
	 */
	private MPackage parent;
	
	/**
	 * All subclasses.
	 */
	private Set<MClass> subclasses;
	
	/**
	 * All references that refer to this class.
	 */
	private Set<MReference> utilizers;
	
	/* 
	 * ********************************
	 *          CONSTRUCTORS
	 * ********************************
	 */
	
	/**
	 * Create a new class in default package, without superclass.
	 * When created, there is no attribute of a class.
	 * @param name the name of class.
	 */
	public MClass(String name) {
		this(name, null, null);
	}
	
	/**
	 * Create a new class in default package with superclass.
	 * When created, there is no attribute of a class.
	 * @param name the name of class.
	 * @param superclazz superclass.
	 */
	public MClass(String name, MClass superclazz) {
		this(name, superclazz, MPackage.DEFAULT_PACKAGE);
	}
	
	/**
	 * Create a new class, without superclass.
	 * When created, there is no attribute of a class.
	 * @param name the name of class.
	 * @param pkg package.
	 */
	public MClass(String name, MPackage pkg) {
		this(name, null, pkg);
	}
	
	/**
	 * Create a new class with specified name, superclass and package(owner).
	 * When created, there is no attribute of a class.
	 * @param name the name of class.
	 * @param supercls superclass.
	 * @param pkg package.
	 */
	public MClass(String name, MClass supercls, MPackage pkg) {
		super(MElementType.Class);
		
		if (pkg == null)
			pkg = MPackage.DEFAULT_PACKAGE;
		if (pkg.isDeleted())
			throw new MException(MException.Reason.ELEMENT_MISSED);
		if (pkg.hasChild(name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		if (supercls != null && supercls.isDeleted())
			throw new MException(MException.Reason.ELEMENT_MISSED);
		
		this.initialize();
		this.name = name;
		this.superclass = supercls;
		this.parent = pkg;
		link();
		
		MDatabase.getDB().createElement(this);
	}
	
	/**
	 * Create a "lazy" class element with id.
	 * @param id ID of element.
	 */
	protected MClass(long id) {
		super(id, MElementType.Class);
	}
	
	/*
	 * ********************************
	 *          DESTRUCTORS
	 * ********************************
	 */
	
	/**
	 * Delete all MElement associated with this class, include attributes, 
	 * references, the references refer to this class. If there is(are) sub-class(es),
	 * all of its sub-classes's superclass automatically set to be this class's 
	 * superclass.
	 */
	@Override
	public void delete() {
		// delete attributes
		if (this.attributes != null) {
			for (MAttribute atb : this.attributes.values()) {
				atb.delete();
			}
			this.attributes.clear();
		}
		// delete references
		if (this.references != null) {
			for (MReference ref : this.references.values()) {
				ref.delete();
			}
			this.references.clear();
		}
		// delete utilizers
		if (this.utilizers != null) {
			for (MReference ref : this.utilizers) {
				ref.delete();
			}
			this.utilizers.clear();
		}
		// unlink super-sub relations
		if (this.subclasses != null) {
			for (MClass cls : this.subclasses) {
				cls.superclass = this.superclass;
			}
		}
		
		unlink();
		MDatabase.getDB().deleteAllObjects(this);
		super.delete();
	}
	
	public Iterator<MObject> objectsIterator() {
		return new ObjItr(MDatabase.getDB().listAllObjectsID(this));
	}
	
	private class ObjItr implements Iterator<MObject> {

		Iterator<Long> it;
		Long last = null;
		
		ObjItr(List<Long> objects) {
			it = objects.iterator();
		}
		
		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public MObject next() {
			last = it.next();
			return MDatabase.getDB().getLazyObject(last);
		}

		@Override
		public void remove() {
			it.remove();
			MDatabase.getDB().getLazyObject(last).delete();
		}
		
	}
	
	/* 
	 * ********************************
	 *           PROPERTIES
	 * ********************************
	 */
	
	/**
	 * Get the name of class.
	 * @return name.
	 */
	@Override
	public String getName() {
		if (isDeleted())
			return null;
		return this.name;
	}
	
	/**
	 * Set the name of class. It's notable that changing the name of class
	 * does not disturb the instantiated objects of this class. The name has
	 * to be unique, otherwise an exception will be thrown.
	 * @param name the name of class.
	 */
	public void setName(String name) {
		if (isDeleted())
			return;
		
		if (name.equals(this.name))
			return;
		if (this.parent.hasChild(name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.parent.removeClass(this);
		this.name = name;
		this.parent.addClass(this);
		this.setChanged(ATTRIB_FLAG_NAME);
	}
	
	/**
	 * The superclass of the class.
	 * @return superclass.
	 */
	public MClass getSuperClass() {
		if (isDeleted())
			return null;
		return this.superclass;
	}
	
	/**
	 * Set the super class of the class. It's forbidden to set class itself or its sub-
	 * class to be its super class.
	 * @param clazz superclass.
	 */
	public void setSuperClass(MClass clazz) {
		if (isDeleted())
			return;
		
		if (this.superclass == clazz)
			return;
		MClass cp = clazz;
		while (cp != null) {
			if (cp == this)
				throw new MException(MException.Reason.INVALID_SUPER_CLASS);
			cp = cp.superclass;
		}
		
		if (this.superclass != null)
			this.superclass.subclasses().remove(this);
		this.superclass = clazz;
		if (this.superclass != null)
			this.superclass.subclasses().add(this);
		this.setChanged(ATTRIB_FLAG_SUPERCLASS);
	}
	
	/**
	 * Find whether this class is a sub-class of another.
	 * @param clazz another class
	 * @return {@code true} if is sub-class.
	 */
	public boolean asSubClass(MClass clazz) {
		if (isDeleted())
			return false;
		
		MClass cp = this.superclass;
		do {
			if (cp == clazz)
				return true;
		} while (cp != null);
		return false;
	}
	
	public MClass[] getSubClasses() {
		return subclasses().toArray(new MClass[0]);
	}
	
	/**
	 * Get all sub-classes.
	 * @return the set of sub-classes.
	 */
	private Set<MClass> subclasses() {
		if (this.subclasses == null)
			this.subclasses = new TreeSet<MClass>();
		return this.subclasses;
	}
	
	/**
	 * The package of the class.
	 * @return package.
	 */
	public MPackage getPackage() {
		if (isDeleted())
			return null;
		return this.parent;
	}
	
	/**
	 * Set the package of the class.
	 * @param pkg package.
	 */
	public void setPackage(MPackage pkg) {
		if (isDeleted())
			return;
		
		if (pkg == null)
			pkg = MPackage.DEFAULT_PACKAGE;
		if (pkg == this.parent)
			return;
		if (pkg.hasChild(this.name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.parent.removeClass(this);
		this.parent = pkg;
		this.parent.addClass(this);
		this.setChanged(ATTRIB_FLAG_PARENT);
	}
	
	/**
	 * Find whether this class has an attribute or a reference named with
	 * the specified name.
	 * @param name the specified name.
	 * @return {@code true} if there is.
	 */
	public boolean hasProperty(String name) {
		if (isDeleted())
			return false;
		return this.hasAttribute(name) || this.hasReference(name);
	}
	
	/**
	 * Get a property of the class.
	 * @param name The name of field.
	 * @return <code>MAttribute</code> if property is attribute, <code>MReference</code> if field
	 * is reference, <code>null</code> if there is no property named after the given name. 
	 */
	public MProperty getProperty(String name) {
		if (isDeleted())
			return null;
		MProperty p = this.getAttribute(name);
		if (p == null)
			p = this.getReference(name);
		return p;
	}
	
	protected void addProperty(MProperty p) {
		if (p.getElementType() == MElementType.Attribute)
			addAttribute((MAttribute) p);
		else if (p.getElementType() == MElementType.Reference)
			addReference((MReference) p);
	}
	
	protected void removeProperty(MProperty p) {
		if (p.getElementType() == MElementType.Attribute)
			removeAttribute((MAttribute) p);
		else if (p.getElementType() == MElementType.Reference)
			removeReference((MReference) p);
	}
	
	/* 
	 * ********************************
	 *      ATTRIBUTE OPERATIONS
	 * ********************************
	 */
	
	/**
	 * Get all names of attributes owned by this class.
	 * @return the set of attribute names.
	 */
	public String[] getAttributeNames() {
		if (isDeleted())
			return null;
		return this.attributes().keySet().toArray(new String[0]);
	}
	
	/**
	 * Get all names of attributes, include the attributes owned by superclass.
	 * @return the set of attribute names.
	 */
	public String[] getAllAttributeNames() {
		if (isDeleted())
			return null;
		Set<String> names = new TreeSet<String>();
		MClass cls = this;
		while (cls != null) {
			names.addAll(cls.attributes().keySet());
			cls = cls.superclass;
		}
		return names.toArray(new String[0]);
	}
	
	/**
	 * Get the attribute with specific name.
	 * @param name the specific name.
	 * @return attribute with specific name. {@code null} if there is no one.
	 */
	public MAttribute getAttribute(String name) {
		if (isDeleted())
			return null;
		MAttribute atb = null;

		MClass cls = this;
		while (cls != null) {
			atb = cls.attributes().get(name);
			if (atb != null)
				break;
			cls = cls.superclass;
		}
		return atb;
	}

	/**
	 * Find whether this class own an attribute named with specified name.
	 * @param name the specified name.
	 * @return {@code true} if there is.
	 */
	public boolean hasAttribute(String name) {
		if (isDeleted())
			return false;
		MClass cls = this;
		while (cls != null) {
			if (cls.attributes().containsKey(name))
				return true;
			cls = cls.superclass;
		}
		return false;
	}

	/**
	 * Get the attributes.
	 * @return the map from name to attribute.
	 */
	private Map<String, MAttribute> attributes() {
		if (this.attributes == null)
			this.attributes = new TreeMap<String, MAttribute>();
		return this.attributes;
	}
	
	/**
	 * Add attribute.
	 * @param atb attribute.
	 */
	void addAttribute(MAttribute atb) {
		this.attributes().put(atb.getName(), atb);
	}

	/**
	 * Remove attribute.
	 * @param atb attribute.
	 */
	void removeAttribute(MAttribute atb) {
		this.attributes().remove(atb.getName());
	}
	
	/* 
	 * ********************************
	 *       REFERENCE OPERATIONS
	 * ********************************
	 */

	/**
	 * Get all names of references owned by this class.
	 * @return the set of reference names.
	 */
	public String[] getReferenceNames() {
		if (isDeleted())
			return null;
		return this.references().keySet().toArray(new String[0]);
	}
	
	/**
	 * Get all names of references, include the references owned by superclass.
	 * @return the set of reference names.
	 */
	public String[] getAllReferenceNames() {
		if (isDeleted())
			return null;
		Set<String> names = new TreeSet<String>();
		MClass cls = this;
		while (cls != null) {
			names.addAll(cls.references().keySet());
			cls = cls.superclass;
		}
		return names.toArray(new String[0]);
	}
	
	/**
	 * Get the reference with specific name.
	 * @param name the specific name.
	 * @return reference with specific name. {@code null} if there is no one.
	 */
	public MReference getReference(String name) {
		if (isDeleted())
			return null;
		MReference ref = null;
		
		MClass cls = this;
		while (cls != null) {
			ref = cls.references().get(name);
			if (ref != null)
				break;
			cls = cls.superclass;
		}
		return ref;
	}
	
	/**
	 * Find whether this class own a reference named with specified name.
	 * @param name the specified name.
	 * @return {@code true} if there is.
	 */
	public boolean hasReference(String name) {
		if (isDeleted())
			return false;
		MClass cls = this;
		while (cls != null) {
			if (cls.references().containsKey(name))
				return true;
			cls = cls.superclass;
		}
		return false;
	}
	
	/**
	 * Get the references.
	 * @return the map from name to reference.
	 */
	private Map<String, MReference> references() {
		if (this.references == null)
			this.references = new TreeMap<String, MReference>();
		return this.references;
	}

	/**
	 * Add reference.
	 * @param ref reference.
	 */
	void addReference(MReference ref) {
		this.references().put(ref.getName(), ref);
	}
	
	/**
	 * Remove reference.
	 * @param ref reference.
	 */
	void removeReference(MReference ref) {
		this.references().remove(ref.getName());
	}
	
	/*
	 * ********************************
	 *            UTILIZE
	 * ********************************
	 */
	
	public MReference[] getUtilizers() {
		return utilizers().toArray(new MReference[0]);
	}
	
	/**
	 * Utilizers are the references({@code MReference}) refer to this class.
	 * @return the utilizers.
	 */
	private Set<MReference> utilizers() {
		if (this.utilizers == null)
			this.utilizers = new TreeSet<MReference>();
		return this.utilizers;
	}
	
	/**
	 * Add utilizer.
	 * @param utilizer a reference.
	 */
	void addUtilizer(MReference utilizer) {
		this.utilizers().add(utilizer);
	}
	
	/**
	 * Remove utilizer.
	 * @param utilizer a reference.
	 */
	void removeUtilizer(MReference utilizer) {
		this.utilizers().remove(utilizer);
	}
	
	private void link() {
		if (name != null)
			this.parent.addClass(this);
		if (this.superclass != null)
			this.superclass.subclasses().add(this);
	}
	
	private void unlink() {
		if (name != null)
			this.parent.removeClass(this);
		if (this.superclass != null)
			this.superclass.subclasses().remove(this);
	}
	
	/*
	 * ********************************
	 *        DATA LOAD & SAVE
	 * ********************************
	 */

	@Override
	void loadFromDBInfo(DBInfo dbInfo) {
		MDBAdapter.ClassDBInfo clsDBInfo = (MDBAdapter.ClassDBInfo) dbInfo;
		// check conflict
		MPackage pkg = this.parent;
		String name = this.name;
		if (dbInfo.isFlagged(ATTRIB_FLAG_PARENT))
			pkg = MDatabase.getDB().getPackage(clsDBInfo.package_id);
		if (dbInfo.isFlagged(ATTRIB_FLAG_NAME))
			name = clsDBInfo.name;
		if (pkg != null && name != null && pkg.hasChild(name) && pkg.getClazz(name) != this)
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		boolean relink = false;
		if (pkg != this.parent || !name.equals(this.name) || dbInfo.isFlagged(ATTRIB_FLAG_SUPERCLASS))
			relink = true;
		// unlink
		if (relink)
			unlink();
		if (dbInfo.isFlagged(ATTRIB_FLAG_NAME)) {
			this.name = name;
		}
		if (dbInfo.isFlagged(ATTRIB_FLAG_SUPERCLASS)) {
			this.superclass = MDatabase.getDB().getClass(clsDBInfo.superclass_id);
		}
		if (dbInfo.isFlagged(ATTRIB_FLAG_PARENT)) {
			this.parent = pkg;
		}
		// link
		if (relink)
			link();
	}

	@Override
	void saveToDBInfo(DBInfo dbInfo) {
		MDBAdapter.ClassDBInfo clsDBInfo = (MDBAdapter.ClassDBInfo) dbInfo;
		clsDBInfo.id = this.id;
		if (dbInfo.isFlagged(ATTRIB_FLAG_NAME)) {
			clsDBInfo.name = this.name;
		}
		if (dbInfo.isFlagged(ATTRIB_FLAG_SUPERCLASS)) {
			clsDBInfo.superclass_id = MElement.getElementID(this.superclass);
		}
		if (dbInfo.isFlagged(ATTRIB_FLAG_PARENT)) {
			clsDBInfo.package_id = MElement.getElementID(this.parent);
		}
	}
	
	/*
	 * ********************************
	 *             STRING
	 * ********************************
	 */
	
	@Override
	public String toString() {
		if (this.parent == MPackage.DEFAULT_PACKAGE)
			return this.name;
		return this.parent.toString() + "::" + this.name;
	}
	
	@Override
	public String details() {
		StringBuilder sb = new StringBuilder();
		sb.append("Class(").append(id).append(") - ");
		sb.append(toString());
		if (getSuperClass() != null)
			sb.append(" : ").append(getSuperClass().getName());
		sb.append(" {\n");
		String[] atbnames = getAllAttributeNames();
		for (String name : atbnames) {
			MAttribute atb = getAttribute(name);
			sb.append("  ").append(atb.toString());
			if (atb.clazz == this)
				sb.append(" [a]\n");
			else
				sb.append(" [A]\n");
		}
		String[] refnames = getAllReferenceNames();
		for (String name : refnames) {
			MReference ref = getReference(name);
			sb.append("  ").append(ref.toString());
			if (ref.clazz == this)
				sb.append(" [r]\n");
			else
				sb.append(" [R]\n");
		}
		sb.append("}");
		return sb.toString();
	}
	
	public static final int ATTRIB_FLAG_PARENT = 0x00000001;
	public static final int ATTRIB_FLAG_NAME = 0x00000002;
	public static final int ATTRIB_FLAG_SUPERCLASS = 0x00000004;

}
