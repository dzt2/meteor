package lab.meteor.core;

/**
 * The type of attribute.
 * @author Qiang
 *
 */
public interface MType {
	
	/**
	 * Get the native data type.
	 * @return
	 */
	MNativeDataType getNativeDataType();
	
	/**
	 * Convert the <code>MCode</code> to a string identifier.
	 * @return String identifier
	 */
	String getTypeIdentifier();
	
}
