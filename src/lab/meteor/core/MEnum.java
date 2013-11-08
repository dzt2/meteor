package lab.meteor.core;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import lab.meteor.core.MDBAdapter.DBInfo;

/**
 * Enumeration type.
 * @author Qiang
 *
 */
public class MEnum extends MElement implements MDataType {

	private MPackage parent;
	
	private String name;
	
	private Map<String, MSymbol> symbols;
	
	private Set<MAttribute> utilizers;
	
	public MEnum(String name) throws MException {
		this(name, null);
	}
	
	public MEnum(String name, MPackage pkg) throws MException {
		super(MElementType.Enum);
		
		if (pkg == null)
			pkg = MPackage.DEFAULT_PACKAGE;
		if (pkg.isDeleted())
			throw new MException(MException.Reason.ELEMENT_MISSED);
		if (pkg.hasClass(name) || pkg.hasEnum(name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.initialize();
		this.name = name;
		this.parent = pkg;
		this.parent.addEnum(this);
		
		MDatabase.getDB().createElement(this);
	}
	
	/**
	 * Create a "lazy" enumerator element with id.
	 * @param id ID of element.
	 */
	protected MEnum(long id) {
		super(id, MElementType.Enum);
	}

	@Override
	public void delete() throws MException {
		// delete symbols
		if (this.symbols != null) {
			for (MSymbol sym : this.symbols.values()) {
				sym.delete();
			}
			this.symbols.clear();
		}
		// delete utilizers
		if (this.utilizers != null) {
			for (MAttribute atb : this.utilizers) {
				atb.delete();
			}
			this.utilizers.clear();
		}
		// package
		this.parent.removeEnum(this);
		super.delete();
	}
	
	public String getName() {
		if (isDeleted())
			return null;
		return this.name;
	}
	
	public void setName(String name) throws MException {
		if (isDeleted())
			return;
		
		if (name.equals(this.name))
			return;
		if (this.parent.hasClass(this.name) || this.parent.hasEnum(this.name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.parent.removeEnum(this);
		this.name = name;
		this.parent.addEnum(this);
		this.setChanged(ATTRIB_FLAG_NAME);
	}
	
	private Map<String, MSymbol> getSymbols() {
		if (this.symbols == null)
			this.symbols = new TreeMap<String, MSymbol>();
		return this.symbols;
	}
	
	protected void addSymbol(MSymbol sym) {
		this.getSymbols().put(sym.getName(), sym);
	}
	
	protected void removeSymbol(MSymbol sym) {
		this.getSymbols().remove(sym.getName());
	}
	
	public String[] getSymbolNames() {
		if (isDeleted())
			return null;
		return this.getSymbols().keySet().toArray(new String[0]);
	}
	
	public MSymbol getSymbol(String sym) {
		if (isDeleted())
			return null;
		return this.getSymbols().get(sym);
	}
	
	public boolean hasSymbol(String sym) {
		if (isDeleted())
			return false;
		return this.getSymbols().containsKey(sym);
	}
	
	public MPackage getPackage() {
		if (isDeleted())
			return null;
		return this.parent;
	}
	
	public void setPackage(MPackage pkg) throws MException {
		if (isDeleted())
			return;
		if (pkg == null)
			pkg = MPackage.DEFAULT_PACKAGE;
		if (pkg == this.parent)
			return;
		if (pkg.hasClass(this.name) || pkg.hasEnum(this.name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.parent.removeEnum(this);
		this.parent = pkg;
		this.parent.addEnum(this);
		this.setChanged(ATTRIB_FLAG_PARENT);
	}
	
	void addUtilizer(MAttribute atb) {
		this.getUtilizers().add(atb);
	}
	
	void removeUtilizer(MAttribute atb) {
		this.getUtilizers().remove(atb);
	}
	
	private Set<MAttribute> getUtilizers() {
		if (this.utilizers == null)
			this.utilizers = new TreeSet<MAttribute>();
		return this.utilizers;
	}

	@Override
	public MNativeDataType getNativeDataType() {
		if (isDeleted())
			return null;
		return MNativeDataType.Enum;
	}

	@Override
	public String getTypeIdentifier() {
		if (isDeleted())
			return null;
		return String.valueOf(MElement.ID_PREFIX) + MUtility.stringID(this.id);
	}
	
	private void link() {
		if (name != null)
			this.parent.addEnum(this);
	}
	
	private void unlink() {
		if (name != null)
			this.parent.removeEnum(this);
	}
	
	/*
	 * ********************************
	 *        DATA LOAD & SAVE
	 * ********************************
	 */

	@Override
	void loadFromDBInfo(DBInfo dbInfo) {
		MDBAdapter.EnumDBInfo enmDBInfo = (MDBAdapter.EnumDBInfo) dbInfo;
		// check conflict
		MPackage pkg = this.parent;
		String name = this.name;
		if (dbInfo.isFlagged(ATTRIB_FLAG_PARENT))
			pkg = MDatabase.getDB().getPackage(enmDBInfo.package_id);
		if (dbInfo.isFlagged(ATTRIB_FLAG_NAME))
			name = enmDBInfo.name;
		if (pkg != null && name != null && pkg.hasChild(name) && pkg.getEnum(name) != this)
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
			this.parent = pkg;
		}
		// link
		if (relink)
			link();
	}

	@Override
	void saveToDBInfo(DBInfo dbInfo) {
		MDBAdapter.EnumDBInfo enmDBInfo = (MDBAdapter.EnumDBInfo) dbInfo;
		enmDBInfo.id = this.id;
		if (dbInfo.isFlagged(ATTRIB_FLAG_NAME)) {
			enmDBInfo.name = this.name;
		}
		if (dbInfo.isFlagged(ATTRIB_FLAG_PARENT)) {
			enmDBInfo.package_id = MElement.getElementID(this.parent);
		}
	}
	
	@Override
	public String toString() {
		if (this.parent == MPackage.DEFAULT_PACKAGE)
			return this.name;
		return this.parent.toString() + "::" + this.name;
	}
	
	@Override
	public String details() {
		StringBuilder sb = new StringBuilder();
		sb.append("Enum(").append(id).append(")\n");
		sb.append(toString()).append('\n');
		String[] symnames = getSymbolNames();
		for (String name : symnames) {
			MSymbol sym = getSymbol(name);
			sb.append(sym.toString()).append('\n');
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}
	
	public static final int ATTRIB_FLAG_PARENT = 0x00000001;
	public static final int ATTRIB_FLAG_NAME = 0x00000002;

}
