package lab.meteor.core;

class MTypePointer {
	
	private String typeIdentifier;
	
	public MTypePointer(MType type) {
		if (type == null)
			this.typeIdentifier = null;
		else
			this.typeIdentifier = type.getTypeIdentifier();
	}
	
	public String getTypeIdentifier() {
		return this.typeIdentifier;
	}
	
	public MType getType() {
		return null;
	}
}
