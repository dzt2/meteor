package lab.meteor.core;

/**
 * The native type of data.
 * @author Qiang
 *
 */
public enum MNativeDataType {
	/**
	 * Any type of native types.
	 */
	Any,
	/**
	 * The number, which is same as double value.
	 */
	Number,
	/**
	 * The boolean.
	 */
	Boolean,
	/**
	 * The string.
	 */
	String,
	/**
	 * The date and time.
	 */
	DateTime,
	/**
	 * The binary.
	 * @see MBinary
	 */
	Binary,
	/**
	 * The regular expression.
	 */
	Regex,
	/**
	 * The code.
	 * @see MCode
	 */
	Code,
	/**
	 * The attribute reference, such as people[name]
	 */
	Ref,
	Integer,
	Int64,
	
	List,
	Dictionary,
	Set,
	
//	Object,
	Enum
}
