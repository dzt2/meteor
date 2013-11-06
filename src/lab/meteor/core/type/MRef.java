package lab.meteor.core.type;

import lab.meteor.core.MAttribute;
import lab.meteor.core.MElementPointer;
import lab.meteor.core.MProperty;
import lab.meteor.core.MObject;
import lab.meteor.core.MElement.MElementType;
import lab.meteor.core.MReference;

public class MRef {

	final MElementPointer target;
	
	final MElementPointer field;
	
	public MRef(long obj_id, long field_id) {
		this.target = new MElementPointer(obj_id, MElementType.Object);
		this.field = new MElementPointer(field_id);
	}
	
	public MRef(MObject obj, MAttribute atb) {
		this.target = new MElementPointer(obj);
		this.field = new MElementPointer(atb);
	}
	
	public MRef(MObject obj, MReference ref) {
		this.target = new MElementPointer(obj);
		this.field = new MElementPointer(ref);
	}
	
	public MObject getTarget() {
		return (MObject) this.target.getElement();
	}
	
	public MProperty getField() {
		return (MProperty) this.target.getElement();
	}

}
