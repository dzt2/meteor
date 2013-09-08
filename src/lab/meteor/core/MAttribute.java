package lab.meteor.core;


public class MAttribute extends MElement {

	private String name;
	
	private MTypePointer type_pt;

	private MClass clazz;
	
	/* 
	 * ********************************
	 *          CONSTRUCTORS
	 * ********************************
	 */
	
	public MAttribute(MClass clazz, String name, MType type) throws MException {
		super(MElementType.Attribute);
		
		if (clazz == null)
			throw new MException(MException.Reason.NULL_ELEMENT);
		if (clazz.isDeleted())
			throw new MException(MException.Reason.ELEMENT_MISSED);
		if (clazz.hasAttribute(name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.initialize();
		this.clazz = clazz;
		this.name = name;
		this.type_pt = new MTypePointer(type);
		this.clazz.addAttribute(this);
		
		MDatabase.getDB().createElement(this);
	}
	
	protected MAttribute(long id) {
		super(id, MElementType.Attribute);
	}
	
	/*
	 * ********************************
	 *          DESTRUCTORS
	 * ********************************
	 */
	
	@Override
	public void delete() throws MException {
		// class
		this.clazz.removeAtttribute(this);
		super.delete();
	}
	
	/* 
	 * ********************************
	 *           PROPERTIES
	 * ********************************
	 */

	public String getName() {
		return name;
	}
	
	public void setName(String name) throws MException {
		if (name.equals(this.name))
			return;
		if (this.clazz.hasAttribute(name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.clazz.removeAtttribute(this);
		this.name = name;
		this.clazz.addAttribute(this);
		this.setChanged();
	}
	
	public MType getType() {
		return type_pt.getType();
	}
	
	public void setType(MType type) {
		this.type_pt = new MTypePointer(type);
		this.setChanged();
	}

	public MClass getClazz() {
		return clazz;
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
