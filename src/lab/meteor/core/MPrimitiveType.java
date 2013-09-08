package lab.meteor.core;


public class MPrimitiveType implements MType {

//	private static final String primitiveTypePrefix = "#";
	
	public static final MPrimitiveType Any = new MPrimitiveType(MNativeDataType.Any);
	public static final MPrimitiveType Number = new MPrimitiveType(MNativeDataType.Number);
	public static final MPrimitiveType Boolean = new MPrimitiveType(MNativeDataType.Boolean);
	public static final MPrimitiveType String = new MPrimitiveType(MNativeDataType.String);
	public static final MPrimitiveType DateTime = new MPrimitiveType(MNativeDataType.DateTime);
	public static final MPrimitiveType Binary = new MPrimitiveType(MNativeDataType.Binary);
	public static final MPrimitiveType Regex = new MPrimitiveType(MNativeDataType.Regex);
	public static final MPrimitiveType Code = new MPrimitiveType(MNativeDataType.Code);
	public static final MPrimitiveType Ref = new MPrimitiveType(MNativeDataType.Ref);
	public static final MPrimitiveType Int32 = new MPrimitiveType(MNativeDataType.Int32);
	public static final MPrimitiveType Int64 = new MPrimitiveType(MNativeDataType.Int64);
	
	public static final MPrimitiveType List = new MPrimitiveType(MNativeDataType.List);
	public static final MPrimitiveType Set = new MPrimitiveType(MNativeDataType.Set);
	public static final MPrimitiveType Dictionary = new MPrimitiveType(MNativeDataType.Dictionary);
	
	private MNativeDataType nType;
	
	MPrimitiveType(MNativeDataType nType) {
		this.nType = nType;
	}
	
	public MNativeDataType getNativeDataType() {
		return this.nType;
	}

	@Override
	public String getTypeIdentifier() {
		return this.nType.toString();
	}

}
