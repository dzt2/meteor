package lab.meteor.core;

import lab.meteor.core.MDBAdapter.DBInfo;

public class MReference extends MProperty {

	private MClass reference;
	private Multiplicity multi;
	
	private MReference opposite = null;
	
	public static enum Multiplicity {
		One,
		Multiple
	}
	
	public MReference(MClass cls, String name, MClass reference, Multiplicity multi) {
		super(cls, name, MElementType.Reference);
		
		this.reference = reference;
		this.multi = multi;
		link();
		
		MDatabase.getDB().createElement(this);
	}
	
	/**
	 * Create a "lazy" reference element with id.
	 * @param id ID of element.
	 */
	protected MReference(long id) {
		super(id, MElementType.Reference);
	}
	
	@Override
	public void delete() throws MException {
		unlink();
		super.delete();
	}
	
	@Override
	public MType getType() {
		return getReference();
	}
	
	public MClass getReference() {
		return this.reference;
	}
	
	public void setReference(MClass reference) {
		if (reference == null || reference.isDeleted())
			throw new MException(MException.Reason.ELEMENT_MISSED);
		this.reference.removeUtilizer(this);
		this.reference = reference;
		this.reference.addUtilizer(this);
		this.setChanged(ATTRIB_FLAG_REFERENCE);
	}
	
	public Multiplicity getMultiplicity() {
		return this.multi;
	}
	
	public void setMultiplicity(Multiplicity multi) {
		this.multi = multi;
		this.setChanged(ATTRIB_FLAG_MULTIPLICITY);
	}
	
	public MReference getOpposite() {
		return this.opposite;
	}
	
	public void setOpposite(MReference opposite) {
		if (opposite == this.opposite)
			return;
//		if (opposite == this)
//			throw new MException(MException.Reason.INVALID_OPPOSITE);
		if (opposite != null) {
			if (opposite.reference != this.clazz)
				throw new MException(MException.Reason.INVALID_OPPOSITE);
		}
		if (this.opposite != null)
			this.opposite.opposite = null;
		this.opposite = opposite;
		if (this.opposite != null)
			this.opposite.opposite = this;
		this.setChanged(ATTRIB_FLAG_OPPOSITE);
		if (this.opposite != null)
			this.opposite.setChanged(ATTRIB_FLAG_OPPOSITE);
	}
	
	private void link() {
		if (name != null)
			this.clazz.addReference(this);
		if (reference != null)
			this.reference.addUtilizer(this);
	}
	
	private void unlink() {
		if (name != null)
			this.clazz.removeReference(this);
		if (reference != null)
			this.reference.removeUtilizer(this);
	}

	/*
	 * ********************************
	 *        DATA LOAD & SAVE
	 * ********************************
	 */
	
	@Override
	void loadFromDBInfo(DBInfo dbInfo) {
		MDBAdapter.ReferenceDBInfo refDBInfo = (MDBAdapter.ReferenceDBInfo) dbInfo;
		// check conflict
		MClass cls = this.clazz;
		String name = this.name;
		if (dbInfo.isFlagged(ATTRIB_FLAG_PARENT))
			cls = MDatabase.getDB().getClass(refDBInfo.class_id);
		if (dbInfo.isFlagged(ATTRIB_FLAG_NAME))
			name = refDBInfo.name;
		if (cls != null && name != null && cls.hasProperty(name) && cls.getReference(name) != this)
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
		if (dbInfo.isFlagged(ATTRIB_FLAG_MULTIPLICITY)) {
			this.multi = refDBInfo.multi;
		}
		if (dbInfo.isFlagged(ATTRIB_FLAG_REFERENCE)) {
			this.reference = MDatabase.getDB().getClass(refDBInfo.reference_id);
		}
		if (dbInfo.isFlagged(ATTRIB_FLAG_OPPOSITE)) {
			this.opposite = MDatabase.getDB().getReference(refDBInfo.opposite_id);
		}
		// link
		if (relink)
			link();
	}

	@Override
	void saveToDBInfo(DBInfo dbInfo) {
		MDBAdapter.ReferenceDBInfo refDBInfo = (MDBAdapter.ReferenceDBInfo) dbInfo;
		refDBInfo.id = this.id;
		if (dbInfo.isFlagged(ATTRIB_FLAG_NAME)) {
			refDBInfo.name = this.name;
		}
		if (dbInfo.isFlagged(ATTRIB_FLAG_PARENT)) {
			refDBInfo.class_id = MElement.getElementID(this.clazz);
		}
		if (dbInfo.isFlagged(ATTRIB_FLAG_MULTIPLICITY)) {
			refDBInfo.multi = this.multi;
		}
		if (dbInfo.isFlagged(ATTRIB_FLAG_REFERENCE)) {
			refDBInfo.reference_id = MElement.getElementID(this.reference);
		}
		if (dbInfo.isFlagged(ATTRIB_FLAG_OPPOSITE)) {
			refDBInfo.opposite_id = MElement.getElementID(this.opposite);
		}
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.name).append(" : ").append(this.reference);
		return builder.toString();
	}
	
	public static final int ATTRIB_FLAG_REFERENCE = 0x00000001;
	public static final int ATTRIB_FLAG_MULTIPLICITY = 0x00000002;
	public static final int ATTRIB_FLAG_OPPOSITE = 0x00000004;
}
