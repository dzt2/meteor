package lab.meteor.shell;

import java.util.Iterator;

import lab.meteor.core.MElement;
import lab.meteor.core.MObject;
import lab.meteor.core.MPackage;
import lab.meteor.core.MElement.MElementType;

public class MScript {
	
	public Iterator<MObject> objects(String className) {
		// TODO
		return null;
	}
	
	public Object get(MObject obj, String exp) {
		// TODO
		return null;
	}
	
	public void set(MObject obj, String exp, Object value) {
		// TODO
	}
	
	public void add(MObject obj, String exp, MObject value) {
		
	}
	
	public void remove(MObject obj, String exp, MObject value) {
		
	}
	
	/**
	 * A example of identifier:
	 * <blockquote>pkg_name::pkg_name::pkg_name::class_name</blockquote>
	 * @param identifier
	 * @return
	 */
	public static MElement getElement(String identifier) {
		return getElement(identifier, MPackage.DEFAULT_PACKAGE);
	}
	
	/**
	 * 
	 * @param identifier
	 * @param pkg
	 * @return
	 */
	public static MElement getElement(String identifier, MPackage pkg) {
		String[] names = identifier.split("::");
		MElement pt = pkg;
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			if (pt.getElementType() == MElementType.Package) {
				pt = ((MPackage) pt).getChild(name);
				if (pt == null)
					return null;
			} else if (i < names.length - 1) {
				return null;
			}
		}
		return pt;
	}
	
}
