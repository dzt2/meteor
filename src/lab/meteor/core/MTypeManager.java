package lab.meteor.core;

import java.util.HashMap;
import java.util.Map;

class MTypeManager {
	
	private static MTypeManager lib;
	
	private Map<String, MPrimitiveType> types;
	
	public static MTypeManager getLib() {
		if (lib == null)
			lib = new MTypeManager();
		return lib;
	}
	
	private MTypeManager() {
		this.types = new HashMap<String, MPrimitiveType>();
	}
	
	public MPrimitiveType getType(String identifier) {
		// TODO
		return null;
	}
	
	public void register(MPrimitiveType type) {
		this.types.put(type.getTypeIdentifier(), type);
	}
}
