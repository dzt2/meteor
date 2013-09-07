package lab.meteor.core;

public class MPrimitiveType implements MType {

	private MNativeDataType nType;
	
	private MPrimitiveType(MNativeDataType nType) {
		this.nType = nType;
	}
	
	public MNativeDataType getNativeDataType() {
		return this.nType;
	}

	@Override
	public String getTypeIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

}
