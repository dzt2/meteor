package lab.meteor.core;

public enum MNativeDataType {
	Any,
	Number,
	Boolean,
	String,
	DateTime,
	Object,
	Enum,
	List,
	Dictionary,
	// The attribute reference, such as people[name]
	Ref,
	Binary,
	Regex,
	Code,
	Int32,
	Int64
}
