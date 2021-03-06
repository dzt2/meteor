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
		link();
		
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
		unlink();
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
		if (isDeleted())
			return null;
		return type;
	}
	
	/**
	 * Set the type of attribute.
	 * @param type a MType instance, can be MPrimitiveType or MEnum.
	 */
	public void setDataType(MDataType type) {
		if (isDeleted())
			return;
		
		if (type == this.type)
			return;
		if (this.type instanceof MEnum) {
			((MEnum) this.type).removeUtilizer(this);
		}
		this.type = type;
		if (this.type instanceof MEnum) {
			((MEnum) this.type).addUtilizer(this);
		}
		this.setChanged(ATTRIB_FLAG_DATATYPE);
	}
	
	@Override
	public MType getType() {
		if (isDeleted())
			return null;
		return getDataType();
	}
	
	/*
	 * ********************************
	 *            LINKAGE
	 * ********************************
	 */
	
	private void link() {
		if (name != null)
			this.clazz.addAttribute(this);
		if (this.type instanceof MEnum) {
			((MEnum) this.type).addUtilizer(this);
		}
	}
	
	private void unlink() {
		if (name != null)
			this.clazz.removeAttribute(this);
		if (this.type instanceof MEnum) {
			((MEnum) this.type).removeUtilizer(this);
		}
	}
	
	/*
	 * ********************************
	 *        DATA LOAD & SAVE
	 * ********************************
	 */

	@Override
	void loadFromDBInfo(DBInfo dbInfo) {
		MDBAdapter.AttributeDBInfo atbDBInfo = (MDBAdapter.AttributeDBInfo) dbInfo;
		// check conflict
		MClass cls = this.clazz;
		String name = this.name;
		if (dbInfo.isFlagged(ATTRIB_FLAG_PARENT))
			cls = MDatabase.getDB().getClass(atbDBInfo.class_id);
		if (dbInfo.isFlagged(ATTRIB_FLAG_NAME))
			name = atbDBInfo.name;
		if (cls != null && name != null && cls.hasProperty(name) && cls.getAttribute(name) != this)
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		boolean relink = false;
		if (cls != this.clazz || !name.equals(this.name))
			relink = true;
		// unlink
		if (relink)
			unlink();
		if (dbInfo.isFlagged(ATTRIB_FLAG_NAME)) {
			this.name = name;
		}
		if (dbInfo.isFlagged(ATTRIB_FLAG_PARENT)) {
			this.clazz = cls;
		}
		if (dbInfo.isFlagged(ATTRIB_FLAG_DATATYPE)) {
			if (atbDBInfo.type_id.charAt(0) == MElement.ID_PREFIX) {
				long id = MUtility.parseID(atbDBInfo.type_id.substring(1));
				this.type = MDatabase.getDB().getEnum(id);
			} else {
				this.type = MPrimitiveType.getPrimitiveType(atbDBInfo.type_id);
			}
		}
		// link
		if (relink)
			link();
	}

	@Override
	void saveToDBInfo(DBInfo dbInfo) {
		MDBAdapter.AttributeDBInfo atbDBInfo = (MDBAdapter.AttributeDBInfo) dbInfo;
		atbDBInfo.id = this.id;
		if (dbInfo.isFlagged(ATTRIB_FLAG_NAME)) {
			atbDBInfo.name = this.name;
		}
		if (dbInfo.isFlagged(ATTRIB_FLAG_PARENT)) {
			atbDBInfo.class_id = MElement.getElementID(this.clazz);
		}
		if (dbInfo.isFlagged(ATTRIB_FLAG_DATATYPE)) {
			atbDBInfo.type_id = this.type.getTypeIdentifier();
		}
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
	
	@Override
	public String details() {
		StringBuilder sb = new StringBuilder();
		sb.append("Attribute(").append(id).append(")\n  ")
			.append(clazz.toString()).append('.')
			.append(this.name).append(" : ").append(this.type);
		return sb.toString();
	}
	
	public static final int ATTRIB_FLAG_DATATYPE = 0x00000004;
	
}
