package lab.meteor.core;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
		this.name = null;
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
		this.parent.addPackage(this);
		
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
		
		this.parent.removePackage(this);
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
		this.setChanged();
	}
	
	/**
	 * Get the parent package.
	 * @return The parent package.
	 */
	public MPackage getParent() {
		return this.parent;
	}
	
	/**
	 * Set the parent package. If there is a package with same name in package, there will
	 * be a <code>MException</code> to be thrown.
	 * @param pkg
	 */
	public void setParent(MPackage pkg) {
		if (pkg == null)
			pkg = DEFAULT_PACKAGE;
		if (pkg == this.parent)
			return;
		if (pkg.hasChild(this.name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.parent.removePackage(this);
		this.parent = pkg;
		this.parent.addPackage(this);
		this.setChanged();
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
		return this.getClasses().containsKey(name);
	}

	/**
	 * If there is an enum with a specific name.
	 * @param name The enum's name
	 * @return <code>true</code> if there is
	 */
	public boolean hasEnum(String name) {
		return this.getEnumes().containsKey(name);
	}
	
	/**
	 * If there is a package with a specific name.
	 * @param name The package's name
	 * @return <code>true</code> if there is
	 */
	public boolean hasPackage(String name) {
		return this.getPackages().containsKey(name);
	}
	
	/**
	 * Get the child (package, class or enum) with a specific name.
	 * @param name The name
	 * @return package, class or enum
	 */
	public MElement getChild(String name) {
		MElement child = this.getPackages().get(name);
		if (child == null)
			child = this.getClasses().get(name);
		if (child == null)
			child = this.getEnumes().get(name);
		return child;
	}
	
	/**
	 * Get the class in this package with a specific name.
	 * @param name The class's name
	 * @return The class
	 */
	public MClass getClazz(String name) {
		return this.getClasses().get(name);
	}
	
	/**
	 * Get the enum in this package with a specific name.
	 * @param name The enum's name
	 * @return The enum
	 */
	public MEnum getEnum(String name) {
		return this.getEnumes().get(name);
	}
	
	/**
	 * Get the package in this package with a specific name.
	 * @param name The package's name
	 * @return The package
	 */
	public MPackage getPackage(String name) {
		return this.getPackages().get(name);
	}
	
	/**
	 * Get all names of classes in this package.
	 * @return All classes' names
	 */
	public Set<String> getClassNames() {
		return new TreeSet<String>(this.getClasses().keySet());
	}
	
	/**
	 * Get all names of enumes in this package.
	 * @return All enumes' names
	 */
	public Set<String> getEnumNames() {
		return new TreeSet<String>(this.getEnumes().keySet());
	}
	
	/**
	 * Get all names of packages in this package.
	 * @return All packages' names
	 */
	public Set<String> getPackageNames() {
		return new TreeSet<String>(this.getPackages().keySet());
	}

	/**
	 * Get class map.
	 * @return
	 */
	private Map<String, MClass> getClasses() {
		if (this.classes == null)
			this.classes = new TreeMap<String, MClass>();
		return this.classes;
	}
	
	/**
	 * Get enum map.
	 * @return
	 */
	private Map<String, MEnum> getEnumes() {
		if (this.enumes == null)
			this.enumes = new TreeMap<String, MEnum>();
		return this.enumes;
	}
	
	/**
	 * Get package map.
	 * @return
	 */
	private Map<String, MPackage> getPackages() {
		if (this.packages == null)
			this.packages = new TreeMap<String, MPackage>();
		return this.packages;
	}
	
	/**
	 * Add a class to this package.
	 * @param cls The class
	 */
	protected void addClass(MClass cls) {
		this.getClasses().put(cls.getName(), cls);
	}
	
	/**
	 * Remove a class from this package.
	 * @param cls The class
	 */
	protected void removeClass(MClass cls) {
		this.getClasses().remove(cls.getName());
	}
	
	/**
	 * Add an enum to this package.
	 * @param enm The enum
	 */
	protected void addEnum(MEnum enm) {
		this.getEnumes().put(enm.getName(), enm);
	}
	
	/**
	 * Remove an enum from this package.
	 * @param enm The enum
	 */
	protected void removeEnum(MEnum enm) {
		this.getEnumes().remove(enm.getName());
	}
	
	/**
	 * Add a package to this package.
	 * @param pkg The package
	 */
	protected void addPackage(MPackage pkg) {
		this.getPackages().put(pkg.getName(), pkg);
	}
	
	/**
	 * Remove a package from this package.
	 * @param pkg The package
	 */
	protected void removePackage(MPackage pkg) {
		this.getPackages().remove(pkg.getName());
	}

	/*
	 * ********************************
	 *        DATA LOAD & SAVE
	 * ********************************
	 */
	
	@Override
	void loadFromDBInfo(DBInfo dbInfo) {
		MDBAdapter.PackageDBInfo pkgDBInfo = (MDBAdapter.PackageDBInfo) dbInfo;
		this.name = pkgDBInfo.name;
		this.parent = MDatabase.getDB().getPackage(pkgDBInfo.package_id);
		
		// link
		this.parent.addPackage(this);
	}

	@Override
	void saveToDBInfo(DBInfo dbInfo) {
		MDBAdapter.PackageDBInfo pkgDBInfo = (MDBAdapter.PackageDBInfo) dbInfo;
		pkgDBInfo.id = this.id;
		pkgDBInfo.name = this.name;
		pkgDBInfo.package_id = MElement.getElementID(this.parent);
	}
	
	@Override
	public String toString() {
		if (this.parent == DEFAULT_PACKAGE) {
			return this.name;
		}
		return this.parent.toString() + "::" + this.name;
	}
	
}
