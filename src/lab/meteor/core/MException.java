package lab.meteor.core;

public class MException extends RuntimeException {

	/**
	 * Auto generated serial version UID.
	 */
	private static final long serialVersionUID = 3763675619106227381L;

	/**
	 * The reason of the exception.
	 */
	private Reason reason;
	
	/**
	 * The reason of the exception.
	 * @return
	 */
	public Reason getReason() {
		return this.reason;
	}
	
	/**
	 * 
	 * @author Qiang
	 *
	 */
	public enum Reason {
		NOT_SUPPORT_YET,
		/**
		 * When execute CURD operations, the DB adapter has not been attached to MDatabase.
		 */
		DB_ADAPTER_NOT_ATTACHED,
		/**
		 * When assign a value to a object'attribute, the attribute does not exist.
		 */
		ATTRIBUTE_NOT_FOUND,
		/**
		 * The type of element (Class, Attribute, Enum, etc.) in memory is different with the
		 * element in DB.
		 */
		MISMATCHED_ELEMENT_TYPE,
		/**
		 * When set a class's super class, attempt to set the class itself or its sub-class
		 * as the super class.
		 */
		INVALID_SUPER_CLASS,
		INVALID_ELEMENT_NAME,
		/**
		 * When load, update and delete element, the element with specific id does not exist in DB.
		 */
		ELEMENT_MISSED,
		/**
		 * When create element, the data with specific id has existed in DB.
		 */
		ELEMENT_CONFILICT,
		/**
		 * When create an element with name, or change the name, the name should be unique in
		 * corresponding domain.
		 */
		ELEMENT_NAME_CONFILICT,
		/**
		 * It's invalid for some methods to call them by null element.
		 */
		NULL_ELEMENT,
		/**
		 * The type of value set to an object's attribute is different with the attribute's type.
		 */
		INVALID_VALUE_TYPE,
		UNKNOWN_VALUE_TYPE,
		INVALID_OPPOSITE
	}
	
	/**
	 * Default constructor.
	 * @param reason
	 */
	public MException(Reason reason) {
		this.reason = reason;
	}
	
	@Override
	public String toString() {
		return super.toString() + ": " + this.reason.toString();
	}
	
}
