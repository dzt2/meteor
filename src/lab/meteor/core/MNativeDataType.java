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
	 * The attribute reference, such as people.name
	 */
	Ref,
	/**
	 * The integer, 32-bit data.
	 */
	Integer,
	/**
	 * The big integer, 64-bit data.
	 */
	Int64,
	/**
	 * The list, a collection type.
	 */
	List,
	/**
	 * The set, a collection type.
	 */
	Set,
	/**
	 * The dictionary, a collection type.
	 */
	Dictionary,
	/**
	 * The enumeration.
	 */
	Enum
}
