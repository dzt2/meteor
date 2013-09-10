package lab.meteor.core;

import java.util.Map;
import java.util.TreeMap;

public class MObject extends MElement {

private Map<Long, Object> values = null;
	
	private MElementPointer class_pt = new MElementPointer();
	
	public MObject(MClass clazz) throws MException {
		super(MElementType.Object);
		
		if (clazz == null)
			throw new MException(MException.Reason.NULL_ELEMENT);
		if (clazz.isDeleted())
			throw new MException(MException.Reason.ELEMENT_MISSED);
		
		this.initialize();
		this.class_pt.setPointer(clazz);
		
		MDatabase.getDB().createElement(this);
	}
	
	public MObject(long id) {
		super(id, MElementType.Object);
	}
	
	public MClass getClazz() {
		if (!this.isLoaded())
			this.load();
		
		MClass cls = (MClass) class_pt.getElement();
		if (cls == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		return cls;
	}
	
	public boolean isInstanceOf(MClass clazz) throws MException {
		if (!isLoaded())
			this.load();
		
		if (this.class_pt.getID() == clazz.getID())
			return true;
		else {
			MClass cls = (MClass) class_pt.getElement();
			if (cls == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
			return cls.asSubClass(clazz);
		}
	}
	
	public Object getValue(String attrib) {
		if (!isLoaded())
			this.load();
		
		MClass cls = (MClass) class_pt.getElement();
		if (cls == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		
		MAttribute attribute = cls.getAttribute(attrib);
		if (attribute == null)
			return null;
		return this.getValues().get(attribute.getID());
	}
	
	public void setValue(String attrib, Object value) {
		if (!isLoaded())
			this.load();
		
		MClass cls = (MClass) class_pt.getElement();
		if (cls == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		
		MAttribute attribute = cls.getAttribute(attrib);
		if (attribute == null)
			throw new MException(MException.Reason.ATTRIBUTE_NOT_FOUND);
		
		Object old = this.getValues().get(attribute.id);
		if (value.equals(old))
			return;
		
		MType type = attribute.getType();
		if (!MUtility.checkType(attribute.getType(), value))
			throw new MException(MException.Reason.INVALID_VALUE_TYPE);
		// value is container (list or dict)
		if (type instanceof MClass) {
			MObject obj = (MObject) value;
			if (obj.getClazz() != attribute.getType()) {
				throw new MException(MException.Reason.INVALID_VALUE_TYPE);
			}
			value = new MElementPointer(obj);
		// value is enum, convert enum to it's pointer.
		} else if (type instanceof MEnum) {
			MSymbol sym = (MSymbol) value;
			if (sym.getEnum() != attribute.getType()) {
				throw new MException(MException.Reason.INVALID_VALUE_TYPE);
			}
			value = new MElementPointer(sym);
		}
		
		this.getValues().put(attribute.getID(), value);
		this.setChanged();
	}

	private Map<Long, Object> getValues() {
		if (this.values == null)
			this.values = new TreeMap<Long, Object>();
		return this.values;
	}

	@Override
	void loadFromDBInfo(Object dbInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void saveToDBInfo(Object dbInfo) {
		// TODO Auto-generated method stub
		
	}

}
