package lab.meteor.core;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class MClass extends MElement implements MType {

	private String name;
	
	private Map<String, MAttribute> attributes = null;
	
	private MClass superclazz;
	private MPackage parent;
	
	private Set<MClass> subclasses;
	
	/* 
	 * ********************************
	 *          CONSTRUCTORS
	 * ********************************
	 */
	
	public MClass(String name) {
		this(name, null, null);
	}
	
	public MClass(String name, MClass superclazz) {
		this(name, superclazz, MPackage.DEFAULT_PACKAGE);
	}
	
	public MClass(String name, MPackage pkg) {
		this(name, null, pkg);
	}
	/**
	 * Create a new class with specified name, superclass and package.
	 * When created, there is no attribute of a class.
	 * @param name the name of class
	 * @throws MException when database has no adapter (i.e. MDatabase.dbAdapter is 
	 * null), it's not able to assign an id to class intended to create.
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
		this.superclazz = supercls;
		if (this.superclazz != null)
			this.superclazz.getSubclasses().add(this);
		this.parent = pkg;
		this.parent.addClass(this);
		
		MDatabase.getInstance().createElement(this);
	}
	
	/**
	 * Create a "lazy" class element with id.
	 * @param id
	 */
	protected MClass(long id) {
		super(id, MElementType.Class);
	}
	
	/*
	 * ********************************
	 *          DESTRUCTORS
	 * ********************************
	 */
	
	@Override
	public void delete() throws MException {
		// delete attributes
		if (this.attributes != null) {
			for (MAttribute atb : this.attributes.values()) {
				atb.delete();
			}
		}
		// unlink super-sub relations
		if (this.subclasses != null) {
			for (MClass cls : this.subclasses) {
				cls.superclazz = this.superclazz;
			}
		}
		if (this.superclazz != null)
			this.superclazz.getSubclasses().remove(this);
		
		// package
		this.parent.removeClass(this);
		super.delete();
	}

	public void deleteAllInstances() {
		// TODO
	}
	
	/* 
	 * ********************************
	 *           PROPERTIES
	 * ********************************
	 */
	
	/**
	 * Get the name of class.
	 * @return
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Set the name of class. It's notable that changing the name of class
	 * does not disturb the instantiated objects of this class.
	 * @param name
	 * @throws MException 
	 */
	public void setName(String name) throws MException {
		if (name.equals(this.name))
			return;
		if (this.parent.hasChild(name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.parent.removeClass(this);
		this.name = name;
		this.parent.addClass(this);
		this.setChanged();
	}
	
	/**
	 * The super class of the class.
	 * @return
	 * @throws MException 
	 */
	public MClass getSuperClass() {
		return this.superclazz;
	}
	
	/**
	 * Set the super class of the class. It's forbidden to set class itself or its sub-
	 * class to be its super class.
	 * @param clazz
	 */
	public void setSuperClass(MClass clazz) {
		if (this.superclazz == clazz)
			return;
		MClass cp = clazz;
		while (cp != null) {
			if (cp == this)
				throw new MException(MException.Reason.INVALID_SUPER_CLASS);
			cp = cp.superclazz;
		}
		
		if (this.superclazz != null)
			this.superclazz.getSubclasses().remove(this);
		this.superclazz = clazz;
		if (this.superclazz != null)
			this.superclazz.getSubclasses().add(this);
		this.setChanged();
	}
	
	public boolean asSubClass(MClass clazz) {
		MClass cp = this.superclazz;
		do {
			if (cp == clazz)
				return true;
		} while (cp != null);
		return false;
	}
	
	private Set<MClass> getSubclasses() {
		if (this.subclasses == null)
			this.subclasses = new TreeSet<MClass>();
		return this.subclasses;
	}
	
	/**
	 * The package of the class.
	 * @return
	 * @throws MException 
	 */
	public MPackage getPackage() throws MException {
		return this.parent;
	}
	
	/**
	 * Set the package of the class.
	 * @param packaga
	 * @throws MException 
	 */
	public void setPackage(MPackage pkg) throws MException {
		if (pkg == null)
			pkg = MPackage.DEFAULT_PACKAGE;
		if (pkg == this.parent)
			return;
		if (pkg.hasChild(this.name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.parent.removeClass(this);
		this.parent = pkg;
		this.parent.addClass(this);
		this.setChanged();
	}
	
	/* 
	 * ********************************
	 *      ATTRIBUTE OPERATIONS
	 * ********************************
	 */
	
	public Set<String> getAttributeNames() {
		return new TreeSet<String>(this.getAttributes().keySet());
	}
	
	public Set<String> getAllAttributeNames() {
		Set<String> names = new TreeSet<String>();
		MClass cls = this;
		while (cls != null) {
			names.addAll(cls.getAttributes().keySet());
			cls = cls.superclazz;
		}
		return names;
	}
	
	/**
	 * Get the attribute with specific name.
	 * @param attrib the attribute name
	 * @return attribute with specific name
	 * @throws MException 
	 */
	public MAttribute getAttribute(String attrib) {
		MAttribute atb = null;
		MClass cls = this;
		while (cls != null) {
			atb = cls.getAttribute(attrib);
			if (atb != null)
				break;
			cls = cls.superclazz;
		}
		return atb;
	}

	public boolean hasAttribute(String name) {
		MClass cls = this;
		while (cls != null) {
			if (cls.getAttributes().containsKey(name))
				return true;
			cls = cls.superclazz;
		}
		return false;
	}

	/**
	 * Get the attributes.
	 * @return the map from name to attribute
	 */
	private Map<String, MAttribute> getAttributes() {
		if (this.attributes == null)
			this.attributes = new TreeMap<String, MAttribute>();
		return this.attributes;
	}

	protected void addAttribute(MAttribute atb) {
		this.getAttributes().put(atb.getName(), atb);
	}
	
	protected void removeAtttribute(MAttribute atb) {
		this.getAttributes().remove(atb.getName());
	}

	@Override
	public MNativeDataType getNativeDataType() {
		return MNativeDataType.Object;
	}

	@Override
	public String getTypeIdentifier() {
		// TODO
		return null;
	}
	
	/*
	 * ********************************
	 *        DATA LOAD & SAVE
	 * ********************************
	 */

	@Override
	void loadFromDBInfo(Object dbInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void saveToDBInfo(Object dbInfo) {
		// TODO Auto-generated method stub
		
	}

}
