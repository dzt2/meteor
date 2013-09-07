package lab.meteor.core;

public class MGenericType extends MPrimitiveType {

	private MTypePointer argument_pt;
	
	MGenericType(MNativeDataType nType, MType argument) {
		super(nType);
		this.argument_pt = new MTypePointer(argument);
	}
	
	@Override
	public String getTypeIdentifier() {
		return super.getTypeIdentifier() + "(" + argument_pt.getTypeIdentifier() + ")";
	}
	
}
