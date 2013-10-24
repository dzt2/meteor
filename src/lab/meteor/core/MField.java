package lab.meteor.core;

import lab.meteor.core.MElement.MElementType;

public interface MField {
	
	/**
	 * Get the name of field.
	 * @return
	 */
	String getName();
	
	/**
	 * Set the name of field.
	 * @param name
	 */
	void setName(String name);
	
	/**
	 * Get the owner of this field.
	 * @return
	 */
	MClass getOwner();
	
	// MElement's method.
	long getID();
	// MElement's method.
	MElementType getElementType();
}
