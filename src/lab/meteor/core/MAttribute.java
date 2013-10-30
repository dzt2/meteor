package lab.meteor.core;

import lab.meteor.core.MDBAdapter.DBInfo;

/**
 * The attribute of class. The type of attribute can only be primitive type and enum.
 * @author Qiang
 * @see MPrimitiveType
 * @see MEnum
 * @see MDataType
 */
public class MAttribute extends MProperty {
	
	/**
	 * Attribute's type.
	 */
	private MDataType type;
	
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
	public MAttribute(MClass cls, String name, MDataType type) {
		super(cls, name, MElementType.Attribute);
		
		this.type = type;
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
	 * Get the type of attribute.
	 * @return type.
	 */
	public MDataType getDataType() {
		return type;
	}
	
	/**
	 * Set the type of attribute.
	 * @param type a MType instance, can be MPrimitiveType or MEnum.
	 */
	public void setDataType(MDataType type) {
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
	
	@Override
	public MType getType() {
		return getDataType();
	}
	
	/*
	 * ********************************
	 *        DATA LOAD & SAVE
	 * ********************************
	 */

	@Override
	void loadFromDBInfo(DBInfo dbInfo) {
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
		this.clazz.addProperty(this);
		if (this.type instanceof MEnum) {
			((MEnum) this.type).addUtilizer(this);
		}
	}

	@Override
	void saveToDBInfo(DBInfo dbInfo) {
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
