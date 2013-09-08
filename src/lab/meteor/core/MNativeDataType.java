package lab.meteor.core;

public enum MNativeDataType {
	Any,
	
	Number,
	Boolean,
	String,
	DateTime,
	Binary,
	Regex,
	Code,
	// The attribute reference, such as people[name]
	Ref,
	Int32,
	Int64,
	
	List,
	Dictionary,
	Set,
	
	Object,
	Enum
}
