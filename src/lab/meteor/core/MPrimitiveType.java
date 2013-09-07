package lab.meteor.core;

public class MPrimitiveType implements MType {

	private static final String primitiveTypePrefix = "#";
	
	private MNativeDataType nType;
	
	MPrimitiveType(MNativeDataType nType) {
		this.nType = nType;
		MTypeManager.getLib().register(this);
	}
	
	public MNativeDataType getNativeDataType() {
		return this.nType;
	}

	@Override
	public String getTypeIdentifier() {
		return primitiveTypePrefix + this.nType.toString();
	}

}
