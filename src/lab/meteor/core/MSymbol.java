package lab.meteor.core;

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
		this.envm.addSymbol(this);
		
		MDatabase.getInstance().createElement(this);
	}
	
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
		this.envm.removeSymbol(this);
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
		this.setChanged();
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
