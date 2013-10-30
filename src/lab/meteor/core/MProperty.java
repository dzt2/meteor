package lab.meteor.core;

public abstract class MProperty extends MElement {
	
	/**
	 * The name.
	 */
	protected String name;
	
	/**
	 * The owner.
	 */
	protected MClass clazz;
	
	protected MProperty(MClass cls, String name, MElementType type) {
		super(type);
		
		if (cls == null)
			throw new MException(MException.Reason.NULL_ELEMENT);
		if (cls.isDeleted())
			throw new MException(MException.Reason.ELEMENT_MISSED);
		if (cls.hasField(name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.initialize();
		this.clazz = cls;
		this.name = name;
		this.clazz.addProperty(this);
	}
	
	public MProperty(long id, MElementType type) {
		super(id, type);
	}

	/**
	 * Get the name of property.
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the name of property. It's notable that changing the name of property
	 * does not disturb the instantiated objects of the class owning this property.
	 * The name has to be unique, otherwise an exception will be thrown.
	 * @param name
	 */
	public void setName(String name) {
		if (name.equals(this.name))
			return;
		if (this.clazz.hasField(name))
			throw new MException(MException.Reason.ELEMENT_NAME_CONFILICT);
		
		this.clazz.removeProperty(this);
		this.name = name;
		this.clazz.addProperty(this);
		this.setChanged();
	}
	
	/**
	 * Get the owner of this field.
	 * @return
	 */
	public MClass getOwner() {
		return clazz;
	}
	
	@Override
	public void delete() {
		// class
		this.clazz.removeProperty(this);
		super.delete();
	}
	
	public abstract MType getType();
}
