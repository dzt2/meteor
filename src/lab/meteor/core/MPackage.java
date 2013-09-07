package lab.meteor.core;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class MPackage extends MElement {

	public static MPackage DEFAULT_PACKAGE = new MPackage();
	
	private Map<String, MClass> classes;
	
	private Map<String, MEnum> enumes;
	
	private Map<String, MPackage> packages;
	
	private String name;
	
	private MPackage parent;
	
	private MPackage() {
		super(MElementType.Package);
		this.name = null;
		this.id = MElement.NULL_ID;
		this.parent = null;
	}
	
	public MPackage(String name) {
		this(name, MPackage.DEFAULT_PACKAGE);
	}
	
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
		}
		// delete enumes
		if (this.enumes != null) {
			for (MEnum enm : this.enumes.values()) {
				enm.delete();
			}
		}
		// delete packages
		if (this.packages != null) {
			for (MPackage pkg : this.packages.values()) {
				pkg.delete();
			}
		}
		
		this.parent.removePackage(this);
		super.delete();
	}
	
	/* 
	 * ********************************
	 *           PROPERTIES
	 * ********************************
	 */

	public String getName() {
		return this.name;
	}
	
	public void setName(String name) throws MException {
		if (name.equals(this.name))
			return;
		if (this.parent.hasChild(name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.parent.removePackage(this);
		this.name = name;
		this.parent.addPackage(this);
		this.setChanged();
	}
	
	public MPackage getParent() {
		return this.parent;
	}
	
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
	
	public boolean hasChild(String name) {
		return this.hasClass(name) || this.hasEnum(name) || this.hasPackage(name);
	}
	
	public boolean hasClass(String name) {
		return this.getClasses().containsKey(name);
	}

	public boolean hasEnum(String name) {
		return this.getEnumes().containsKey(name);
	}
	
	public boolean hasPackage(String name) {
		return this.getPackages().containsKey(name);
	}
	
	public MClass getClazz(String name) {
		return this.getClasses().get(name);
	}
	
	public MEnum getEnum(String name) {
		return this.getEnumes().get(name);
	}
	
	public MPackage getPackage(String name) {
		return this.getPackages().get(name);
	}
	
	public Set<String> getClassNames() {
		return new TreeSet<String>(this.getClasses().keySet());
	}
	
	public Set<String> getEnumNames() {
		return new TreeSet<String>(this.getEnumes().keySet());
	}
	
	public Set<String> getPackageNames() {
		return new TreeSet<String>(this.getPackages().keySet());
	}

	private Map<String, MClass> getClasses() {
		if (this.classes == null)
			this.classes = new TreeMap<String, MClass>();
		return this.classes;
	}
	
	private Map<String, MEnum> getEnumes() {
		if (this.enumes == null)
			this.enumes = new TreeMap<String, MEnum>();
		return this.enumes;
	}
	
	private Map<String, MPackage> getPackages() {
		if (this.packages == null)
			this.packages = new TreeMap<String, MPackage>();
		return this.packages;
	}
	
	protected void addClass(MClass cls) {
		this.getClasses().put(cls.getName(), cls);
	}
	
	protected void removeClass(MClass cls) {
		this.getClasses().remove(cls.getName());
	}
	
	protected void addEnum(MEnum enm) {
		this.getEnumes().put(enm.getName(), enm);
	}
	
	protected void removeEnum(MEnum enm) {
		this.getEnumes().remove(enm.getName());
	}
	
	protected void addPackage(MPackage pkg) {
		this.getPackages().put(pkg.getName(), pkg);
	}
	
	protected void removePackage(MPackage pkg) {
		this.getPackages().remove(pkg.getName());
	}

	@Override
	void loadFromDBInfo(Object dbInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void saveToDBInfo(Object dbInfo) {
		// TODO Auto-generated method stub
		
	}
	
}
