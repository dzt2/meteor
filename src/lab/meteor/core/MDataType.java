package lab.meteor.core;

/**
 * The type of attribute.
 * @author Qiang
 *
 */
public interface MDataType extends MType {
	
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
