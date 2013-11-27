package lab.meteor.io;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lab.meteor.core.MClass;
import lab.meteor.core.MObject;
import lab.meteor.core.MPrimitiveType;
import lab.meteor.core.MProperty;

public class StringIndexer {
	Map<String, MObject> map;
	String property;
	MClass clazz;
	
	public StringIndexer(MClass cls, String propertyName) {
		this.clazz = cls;
		this.property = propertyName;
		map = new HashMap<String, MObject>();
	}
	
	public void build() {
		if (!clazz.hasProperty(property))
			return; // TODO
		MProperty p = clazz.getProperty(property);
		if (p.getType() != MPrimitiveType.String)
			return; // TODO
		Iterator<MObject> it = clazz.objectsIterator();
		while (it.hasNext()) {
			MObject obj = it.next();
			Object o = obj.get(property);
			map.put((String) o, obj);
		}
	}
	
	public MObject find(String key) {
		return map.get(key);
	}
	
	public void append(String key, MObject obj) {
		map.put(key, obj);
	}
}
