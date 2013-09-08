package lab.meteor.core;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MEnum extends MElement implements MType {

	private MPackage parent;
	
	private String name;
	
	private Map<String, MSymbol> symbols;
	
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
		}
		// package
		this.parent.removeEnum(this);
		super.delete();
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) throws MException {
		if (name.equals(this.name))
			return;
		if (this.parent.hasClass(this.name) || this.parent.hasEnum(this.name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.parent.removeEnum(this);
		this.name = name;
		this.parent.addEnum(this);
		this.setChanged();
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
	
	public Set<String> getSymbolNames() {
		return new HashSet<String>(this.getSymbols().keySet());
	}
	
	public MSymbol getSymbol(String sym) {
		return this.getSymbols().get(sym);
	}
	
	public boolean hasSymbol(String sym) {
		return this.getSymbols().containsKey(sym);
	}
	
	public MPackage getPackage() {
		return this.parent;
	}
	
	public void setPackage(MPackage pkg) throws MException {
		if (pkg == null)
			pkg = MPackage.DEFAULT_PACKAGE;
		if (pkg == this.parent)
			return;
		if (pkg.hasClass(this.name) || pkg.hasEnum(this.name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.parent.removeEnum(this);
		this.parent = pkg;
		this.parent.addEnum(this);
		this.setChanged();
	}

	@Override
	public MNativeDataType getNativeDataType() {
		return MNativeDataType.Enum;
	}

	@Override
	public String getTypeIdentifier() {
		return "@" + MUtility.idEncode(this.id);
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
