package lab.meteor.core;

import lab.meteor.core.MDBAdapter.DBInfo;

public class MReference extends MElement implements MField {
	
	private MClass clazz;
	private String name;
	private MClass reference;
	private Multiplicity multi;
	
	private MReference opposite = null;
	
	public static enum Multiplicity {
		One,
		Multiple
	}
	
	public MReference(MClass cls, String name, MClass reference, Multiplicity multi) {
		super(MElementType.Reference);
		
		if (cls == null || cls.isDeleted())
			throw new MException(MException.Reason.ELEMENT_MISSED);
		if (reference == null || reference.isDeleted())
			throw new MException(MException.Reason.ELEMENT_MISSED);
		if (cls.hasField(name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.initialize();
		this.clazz = cls;
		this.name = name;
		this.reference = reference;
		this.multi = multi;
		this.clazz.addReference(this);
		this.reference.addUtilizer(this);
		
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
		// class
		this.clazz.removeReference(this);
		this.reference.removeUtilizer(this);
		super.delete();
	}
	
	@Override
	public MClass getOwner() {
		return this.clazz;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public void setName(String name) {
		if (name.equals(this.name))
			return;
		if (this.clazz.hasField(name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.clazz.removeReference(this);
		this.name = name;
		this.clazz.addReference(this);
		this.setChanged();
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
	}
	
	public Multiplicity getMultiplicity() {
		return this.multi;
	}
	
	public void setMultiplicity(Multiplicity multi) {
		this.multi = multi;
		this.setChanged();
	}
	
	public MReference getOpposite() {
		return this.opposite;
	}
	
	public void setOpposite(MReference opposite) {
		if (opposite == this.opposite)
			return;
		if (opposite == this)
			throw new MException(MException.Reason.INVALID_OPPOSITE);
		if (opposite != null) {
			if (opposite.reference != this.clazz)
				throw new MException(MException.Reason.INVALID_OPPOSITE);
		}
		if (this.opposite != null)
			this.opposite.opposite = null;
		this.opposite = opposite;
		if (this.opposite != null)
			this.opposite.opposite = this;
		this.setChanged();
		if (this.opposite != null)
			this.opposite.setChanged();
	}

	/*
	 * ********************************
	 *        DATA LOAD & SAVE
	 * ********************************
	 */
	
	@Override
	void loadFromDBInfo(DBInfo dbInfo) {
		MDBAdapter.ReferenceDBInfo refDBInfo = (MDBAdapter.ReferenceDBInfo) dbInfo;
		this.name = refDBInfo.name;
		this.clazz = MDatabase.getDB().getClass(refDBInfo.class_id);
		this.multi = refDBInfo.multi;
		this.reference = MDatabase.getDB().getClass(refDBInfo.reference_id);
		this.opposite = MDatabase.getDB().getReference(refDBInfo.opposite_id);
		
		// link
		this.clazz.addReference(this);
		this.reference.addUtilizer(this);
	}

	@Override
	void saveToDBInfo(DBInfo dbInfo) {
		MDBAdapter.ReferenceDBInfo refDBInfo = (MDBAdapter.ReferenceDBInfo) dbInfo;
		refDBInfo.id = this.id;
		refDBInfo.name = this.name;
		refDBInfo.class_id = MElement.getElementID(this.clazz);
		refDBInfo.multi = this.multi;
		refDBInfo.reference_id = MElement.getElementID(this.reference);
		refDBInfo.opposite_id = MElement.getElementID(this.opposite);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.name).append(" : ").append(this.reference);
		return builder.toString();
	}
}
