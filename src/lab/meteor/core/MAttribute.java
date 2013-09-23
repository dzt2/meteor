package lab.meteor.core;

public class MAttribute extends MElement {

	/**
	 * Attribute's name.
	 */
	private String name;
	
	/**
	 * Attribute's type.
	 */
	private MType type;

	/**
	 * Attribute's owner.
	 */
	private MClass clazz;
	
	/* 
	 * ********************************
	 *          CONSTRUCTORS
	 * ********************************
	 */
	
	/**
	 * Create an attribute with specified class(owner), name and type.
	 * @param clazz the owner of this attribute.
	 * @param name the name of attribute.
	 * @param type the type of attribute, MType's instance.
	 */
	public MAttribute(MClass clazz, String name, MType type) {
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
	
	/**
	 * Create a "lazy" attribute element with id.
	 * @param id ID of element.
	 */
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

	/**
	 * Get the name of attribute.
	 * @return name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the name of attribute. It's notable that changing the name of attribute
	 * does not disturb the instantiated objects of the class owning this attribute.
	 * The name has to be unique, otherwise an exception will be thrown.
	 * @param name
	 */
	public void setName(String name) {
		if (name.equals(this.name))
			return;
		if (this.clazz.hasChild(name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.clazz.removeAtttribute(this);
		this.name = name;
		this.clazz.addAttribute(this);
		this.setChanged();
	}
	
	/**
	 * Get the type of attribute.
	 * @return type.
	 */
	public MType getType() {
		return type;
	}
	
	/**
	 * Set the type of attribute.
	 * @param type a MType instance, can be MPrimitiveType or MEnum.
	 */
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

	/**
	 * Get the owner of this attribute.
	 * @return the owner class.
	 */
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
			long id = MUtility.parseID(atbDBInfo.type_id.substring(1));
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
	
	/*
	 * ********************************
	 *             STRING
	 * ********************************
	 */
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.name).append(" : ").append(this.type);
		return builder.toString();
	}

}
