package lab.meteor.io;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lab.meteor.core.MClass;
import lab.meteor.core.MObject;
import lab.meteor.core.MPrimitiveType;
import lab.meteor.core.MProperty;

public class StringIndexer {
	/*
	 *	This Class will help to get all values of a property in clazz and its map to the MObject object itself. 
	 */
	
	// The map storing key-value which key is the property value, and the value is the MObject object.
	Map<String, MObject> map;
	String property;
	MClass clazz;
	
	public StringIndexer(MClass cls, String propertyName) {
		this.clazz = cls;
		this.property = propertyName;
		map = new HashMap<String, MObject>();
	}
	
	public void build() {
		// This function will import all objects of class(clazz) and all its subclass into a map,
		// in which there are key-value whose value is the "whole" object with all property values, and
		// key is the value of the property of the object.
		if (!clazz.hasProperty(property))
			return; // TODO
		MProperty p = clazz.getProperty(property);
		// Only for the property in clazz which is primity type!
		if (p.getType() != MPrimitiveType.String)
			return; // TODO
		Iterator<MObject> it = clazz.objectsIterator();
		while (it.hasNext()) {
			MObject obj = it.next();
			Object o = obj.get(property);
			map.put((String) o, obj);
		}
		for (MClass cls : clazz.getSubClasses()) {
			it = cls.objectsIterator();
			while (it.hasNext()) {
				MObject obj = it.next();
				Object o = obj.get(property);
				map.put((String) o, obj);
			}
		}
	}
	
	public MObject find(String key) {
		return map.get(key);
	}
	
	public void append(String key, MObject obj) {
		map.put(key, obj);
	}
	
	public void debugPrint() {
		for (String key : map.keySet()) {
			System.out.println(key);
		}
	}
}
