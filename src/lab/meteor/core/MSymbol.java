package lab.meteor.core;

import lab.meteor.core.MDBAdapter.DBInfo;

public class MSymbol extends MElement {

	private String name;
	
	private MEnum envm;
	
	/* 
	 * ********************************
	 *          CONSTRUCTORS
	 * ********************************
	 */
	
	public MSymbol(MEnum enm, String name) throws MException {
		super(MElementType.Symbol);
		
		if (enm == null)
			throw new MException(MException.Reason.NULL_ELEMENT);
		if (enm.isDeleted())
			throw new MException(MException.Reason.ELEMENT_MISSED);
		if (enm.hasSymbol(name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.initialize();
		this.envm = enm;
		this.name = name;
		link();
		
		MDatabase.getDB().createElement(this);
	}
	
	/**
	 * Create a "lazy" symbol element with id.
	 * @param id ID of element.
	 */
	protected MSymbol(long id) {
		super(id, MElementType.Symbol);
	}
	
	/*
	 * ********************************
	 *          DESTRUCTORS
	 * ********************************
	 */
	
	@Override
	public void delete() throws MException {
		unlink();
		super.delete();
	}
	
	/* 
	 * ********************************
	 *           PROPERTIES
	 * ********************************
	 */

	public MEnum getEnum() {
		return this.envm;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) throws MException {
		if (name.equals(this.name))
			return;
		if (this.envm.hasSymbol(name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.envm.removeSymbol(this);
		this.name = name;
		this.envm.addSymbol(this);
		this.setChanged(ATTRIB_FLAG_NAME);
	}
	
	private void link() {
		if (name != null)
			this.envm.addSymbol(this);
	}
	
	private void unlink() {
		if (name != null)
			this.envm.removeSymbol(this);
	}

	@Override
	void loadFromDBInfo(DBInfo dbInfo) {
		MDBAdapter.SymbolDBInfo symDBInfo = (MDBAdapter.SymbolDBInfo) dbInfo;
		// check conflict
		MEnum enm = this.envm;
		String name = this.name;
		if (dbInfo.isFlagged(ATTRIB_FLAG_PARENT))
			enm = MDatabase.getDB().getEnum(symDBInfo.enum_id);
		if (dbInfo.isFlagged(ATTRIB_FLAG_NAME))
			name = symDBInfo.name;
		if (enm != null && name != null && enm.hasSymbol(name) && enm.getSymbol(name) != this)
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		boolean relink = false;
		if (enm != this.envm || !name.equals(this.name))
			relink = true;
		// unlink
		if (relink)
			unlink();
		if (dbInfo.isFlagged(ATTRIB_FLAG_NAME)) {
			this.name = name;
		}
		if (dbInfo.isFlagged(ATTRIB_FLAG_PARENT)) {
			this.envm = enm;
		}
		// link
		if (relink)
			link();
	}

	@Override
	void saveToDBInfo(DBInfo dbInfo) {
		MDBAdapter.SymbolDBInfo symDBInfo = (MDBAdapter.SymbolDBInfo) dbInfo;
		symDBInfo.id = this.id;
		if (dbInfo.isFlagged(ATTRIB_FLAG_NAME)) {
			symDBInfo.name = this.name;
		}
		if (dbInfo.isFlagged(ATTRIB_FLAG_PARENT)) {
			symDBInfo.enum_id = MElement.getElementID(this.envm);		
		}
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	public static final int ATTRIB_FLAG_PARENT = 0x00000001;
	public static final int ATTRIB_FLAG_NAME = 0x00000002;
	
}
