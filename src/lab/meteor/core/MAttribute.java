package lab.meteor.core;


public class MAttribute extends MElement {

	private String name;
	
	private MType type;

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
		if (clazz.hasChild(name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.initialize();
		this.clazz = clazz;
		this.name = name;
		this.type = type;
		this.clazz.addAttribute(this);
		if (this.type instanceof MEnum) {
			((MEnum) this.type).addUtilizer(this);
		}
		
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
		if (this.type instanceof MEnum) {
			((MEnum) this.type).removeUtilizer(this);
		}
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
		if (this.clazz.hasChild(name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.clazz.removeAtttribute(this);
		this.name = name;
		this.clazz.addAttribute(this);
		this.setChanged();
	}
	
	public MType getType() {
		return type;
	}
	
	public void setType(MType type) {
		if (type == this.type)
			return;
		
		if (this.type instanceof MEnum) {
			((MEnum) this.type).removeUtilizer(this);
		}
		this.type = type;
		if (this.type instanceof MEnum) {
			((MEnum) this.type).addUtilizer(this);
		}
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
		MDBAdapter.AttributeDBInfo atbDBInfo = (MDBAdapter.AttributeDBInfo) dbInfo;
		this.name = atbDBInfo.name;
		this.clazz = MDatabase.getDB().getClass(atbDBInfo.class_id);
		if (atbDBInfo.type_id.charAt(0) == MElement.ID_PREFIX) {
			long id = MUtility.idDecode(atbDBInfo.type_id.substring(1));
			this.type = MDatabase.getDB().getEnum(id);
		} else {
			this.type = MPrimitiveType.getPrimitiveType(atbDBInfo.type_id);
		}
		
		// link
		this.clazz.addAttribute(this);
		if (this.type instanceof MEnum) {
			((MEnum) this.type).addUtilizer(this);
		}
	}

	@Override
	void saveToDBInfo(Object dbInfo) {
		MDBAdapter.AttributeDBInfo atbDBInfo = (MDBAdapter.AttributeDBInfo) dbInfo;
		atbDBInfo.id = this.id;
		atbDBInfo.name = this.name;
		atbDBInfo.type_id = this.type.getTypeIdentifier();
		atbDBInfo.class_id = MElement.getElementID(this.clazz);
	}

}
