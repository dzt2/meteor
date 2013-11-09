package lab.meteor.core;

import java.util.Map;
import java.util.TreeMap;

import lab.meteor.core.MDBAdapter.DBInfo;

public class MPackage extends MElement {

	/**
	 * The default package.
	 * Default package is the root of all packages, classes and enumes.
	 */
	public static final MPackage DEFAULT_PACKAGE = new MPackage();
	
	/**
	 * The classes.
	 */
	private Map<String, MClass> classes;
	/**
	 * The enumes.
	 */
	private Map<String, MEnum> enumes;
	/**
	 * The packages.
	 */
	private Map<String, MPackage> packages;
	/**
	 * The name.
	 */
	private String name;
	/**
	 * The parent package.
	 */
	private MPackage parent;
	
	/**
	 * Create the default package.
	 */
	private MPackage() {
		super(MElementType.Package);
		this.name = "";
		this.id = MElement.NULL_ID;
		this.parent = null;
	}
	
	/**
	 * Create package with specific name.
	 * @param name
	 */
	public MPackage(String name) {
		this(name, MPackage.DEFAULT_PACKAGE);
	}
	
	/**
	 * Create package with specific name and parent package.
	 * @param name The name of package
	 * @param pkg The parent package
	 */
	public MPackage(String name, MPackage pkg) {
		super(MElementType.Package);
		
		if (pkg == null)
			pkg = MPackage.DEFAULT_PACKAGE;
		if (pkg.isDeleted())
			throw new MException(MException.Reason.ELEMENT_MISSED);
		if (pkg.hasChild(name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.initialize();
		this.name = name;
		this.parent = DEFAULT_PACKAGE;
		link();
		
		MDatabase.getDB().createElement(this);
	}
	
	/**
	 * Create a "lazy" package element with id.
	 * @param id ID of element.
	 */
	protected MPackage(long id) {
		super(id, MElementType.Package);
	}
	
	/*
	 * ********************************
	 *          DESTRUCTORS
	 * ********************************
	 */
	
	@Override
	public void delete() {
		// delete classes
		if (this.classes != null) {
			for (MClass cls : this.classes.values()) {
				cls.delete();
			}
			this.classes.clear();
		}
		// delete enumes
		if (this.enumes != null) {
			for (MEnum enm : this.enumes.values()) {
				enm.delete();
			}
			this.enumes.clear();
		}
		// delete packages
		if (this.packages != null) {
			for (MPackage pkg : this.packages.values()) {
				pkg.delete();
			}
			this.packages.clear();
		}
		
		unlink();
		super.delete();
	}
	
	/* 
	 * ********************************
	 *           PROPERTIES
	 * ********************************
	 */

	/**
	 * Get the name of package.
	 * @return
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Set the name of package.
	 * @param name The name
	 */
	public void setName(String name) {
		if (name.equals(this.name))
			return;
		if (this.parent.hasChild(name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.parent.removePackage(this);
		this.name = name;
		this.parent.addPackage(this);
		this.setChanged(ATTRIB_FLAG_NAME);
	}
	
	/**
	 * Get the parent package.
	 * @return The parent package.
	 */
	public MPackage getPackage() {
		return this.parent;
	}
	
	/**
	 * Set the parent package. If there is a package with same name in package, there will
	 * be a <code>MException</code> to be thrown.
	 * @param pkg
	 */
	public void setPackage(MPackage pkg) {
		if (pkg == null)
			pkg = DEFAULT_PACKAGE;
		if (pkg == this.parent)
			return;
		if (pkg.hasChild(this.name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.parent.removePackage(this);
		this.parent = pkg;
		this.parent.addPackage(this);
		this.setChanged(ATTRIB_FLAG_PARENT);
	}
	
	/* 
	 * ********************************
	 *      CHILDREN OPERATIONS
	 * ********************************
	 */
	
	/**
	 * If there is a child (class, enum or package) with a specific name.
	 * @param name The child's name
	 * @return <code>true</code> if there is
	 */
	public boolean hasChild(String name) {
		return this.hasClass(name) || this.hasEnum(name) || this.hasPackage(name);
	}
	
	/**
	 * If there is a class with a specific name.
	 * @param name The class's name
	 * @return <code>true</code> if there is
	 */
	public boolean hasClass(String name) {
		return this.classes().containsKey(name);
	}

	/**
	 * If there is an enum with a specific name.
	 * @param name The enum's name
	 * @return <code>true</code> if there is
	 */
	public boolean hasEnum(String name) {
		return this.enumes().containsKey(name);
	}
	
	/**
	 * If there is a package with a specific name.
	 * @param name The package's name
	 * @return <code>true</code> if there is
	 */
	public boolean hasPackage(String name) {
		return this.packages().containsKey(name);
	}
	
	/**
	 * Get the child (package, class or enum) with a specific name.
	 * @param name The name
	 * @return package, class or enum
	 */
	public MElement getChild(String name) {
		MElement child = this.packages().get(name);
		if (child == null)
			child = this.classes().get(name);
		if (child == null)
			child = this.enumes().get(name);
		return child;
	}
	
	/**
	 * Get the class in this package with a specific name.
	 * @param name The class's name
	 * @return The class
	 */
	public MClass getClazz(String name) {
		return this.classes().get(name);
	}
	
	/**
	 * Get the enum in this package with a specific name.
	 * @param name The enum's name
	 * @return The enum
	 */
	public MEnum getEnum(String name) {
		return this.enumes().get(name);
	}
	
	/**
	 * Get the package in this package with a specific name.
	 * @param name The package's name
	 * @return The package
	 */
	public MPackage getPackage(String name) {
		return this.packages().get(name);
	}
	
	/**
	 * Get all names of classes in this package.
	 * @return All classes' names
	 */
	public String[] getClassNames() {
		return this.classes().keySet().toArray(new String[0]);
	}
	
	/**
	 * Get all names of enumes in this package.
	 * @return All enumes' names
	 */
	public String[] getEnumNames() {
		return this.enumes().keySet().toArray(new String[0]);
	}
	
	/**
	 * Get all names of packages in this package.
	 * @return All packages' names
	 */
	public String[] getPackageNames() {
		return this.packages().keySet().toArray(new String[0]);
	}

	/**
	 * Get class map.
	 * @return
	 */
	private Map<String, MClass> classes() {
		if (this.classes == null)
			this.classes = new TreeMap<String, MClass>();
		return this.classes;
	}
	
	/**
	 * Get enum map.
	 * @return
	 */
	private Map<String, MEnum> enumes() {
		if (this.enumes == null)
			this.enumes = new TreeMap<String, MEnum>();
		return this.enumes;
	}
	
	/**
	 * Get package map.
	 * @return
	 */
	private Map<String, MPackage> packages() {
		if (this.packages == null)
			this.packages = new TreeMap<String, MPackage>();
		return this.packages;
	}
	
	/**
	 * Add a class to this package.
	 * @param cls The class
	 */
	protected void addClass(MClass cls) {
		this.classes().put(cls.getName(), cls);
	}
	
	/**
	 * Remove a class from this package.
	 * @param cls The class
	 */
	protected void removeClass(MClass cls) {
		this.classes().remove(cls.getName());
	}
	
	/**
	 * Add an enum to this package.
	 * @param enm The enum
	 */
	protected void addEnum(MEnum enm) {
		this.enumes().put(enm.getName(), enm);
	}
	
	/**
	 * Remove an enum from this package.
	 * @param enm The enum
	 */
	protected void removeEnum(MEnum enm) {
		this.enumes().remove(enm.getName());
	}
	
	/**
	 * Add a package to this package.
	 * @param pkg The package
	 */
	protected void addPackage(MPackage pkg) {
		this.packages().put(pkg.getName(), pkg);
	}
	
	/**
	 * Remove a package from this package.
	 * @param pkg The package
	 */
	protected void removePackage(MPackage pkg) {
		this.packages().remove(pkg.getName());
	}

	private void link() {
		if (name != null)
			this.parent.addPackage(this);
	}
	
	private void unlink() {
		if (name != null)
			this.parent.removePackage(this);
	}
	
	/*
	 * ********************************
	 *        DATA LOAD & SAVE
	 * ********************************
	 */
	
	@Override
	void loadFromDBInfo(DBInfo dbInfo) {
		MDBAdapter.PackageDBInfo pkgDBInfo = (MDBAdapter.PackageDBInfo) dbInfo;
		// check conflict
		MPackage pkg = this.parent;
		String name = this.name;
		if (dbInfo.isFlagged(ATTRIB_FLAG_PARENT))
			pkg = MDatabase.getDB().getPackage(pkgDBInfo.package_id);
		if (dbInfo.isFlagged(ATTRIB_FLAG_NAME))
			name = pkgDBInfo.name;
		if (pkg != null && name != null && pkg.hasChild(name) && pkg.getPackage(name) != this)
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		boolean relink = false;
		if (pkg != this.parent || !name.equals(this.name))
			relink = true;
		// unlink
		if (relink)
			unlink();
		if (dbInfo.isFlagged(ATTRIB_FLAG_NAME)) {
			this.name = name;
		}
		if (dbInfo.isFlagged(ATTRIB_FLAG_PARENT)) {
			this.parent = MDatabase.getDB().getPackage(pkgDBInfo.package_id);
		}
		// link
		if (relink)
			link();
	}

	@Override
	void saveToDBInfo(DBInfo dbInfo) {
		MDBAdapter.PackageDBInfo pkgDBInfo = (MDBAdapter.PackageDBInfo) dbInfo;
		pkgDBInfo.id = this.id;
		if (dbInfo.isFlagged(ATTRIB_FLAG_NAME)) {
			pkgDBInfo.name = this.name;
		}
		if (dbInfo.isFlagged(ATTRIB_FLAG_PARENT)) {
			pkgDBInfo.package_id = MElement.getElementID(this.parent);
		}
	}
	
	@Override
	public String toString() {
		if (parent == null || parent == DEFAULT_PACKAGE) {
			return this.name;
		}
		return this.parent.toString() + "::" + this.name;
	}
	
	@Override
	public String details() {
		StringBuilder sb = new StringBuilder();
		if (this == MPackage.DEFAULT_PACKAGE) {
			sb.append("Root Package\n");
		} else {
			sb.append("Package(").append(id).append(") - ");
			sb.append(toString()).append(" {\n");
		}
		String[] names = getPackageNames();
		for (String name : names) {
			sb.append("  package ").append(name).append("\n");
		}
		names = getClassNames();
		for (String name : names) {
			sb.append("  class ").append(name).append("\n");
		}
		names = getEnumNames();
		for (String name : names) {
			sb.append("  enum ").append(name).append("\n");
		}
		sb.append("}");
		return sb.toString();
	}
	
	public static final int ATTRIB_FLAG_PARENT = 0x00000001;
	public static final int ATTRIB_FLAG_NAME = 0x00000002;
	
}
