package lab.meteor.core;

import java.util.HashMap;
import java.util.Map;

class MTypeManager {
	
	public Map<String, MPrimitiveType> primitiveTypes;
	
	public static MTypeManager manager;
	
	public static MTypeManager getManager() {
		if (manager == null)
			manager = new MTypeManager();
		return manager;
	}
	
	private MTypeManager() {
		this.primitiveTypes = new HashMap<String, MPrimitiveType>();
	}
	
	public void register(MPrimitiveType type) {
		this.primitiveTypes.put(type.getTypeIdentifier(), type);
	}
	
	public MType getType(String identifier) {
		if (identifier.charAt(0) == MElement.ID_PREFIX) {
			long id = MUtility.idDecode(identifier.substring(1));
			return MDatabase.getDB().getClass(id);
		} else {
			return this.primitiveTypes.get(identifier);
		}
	}
	
}
