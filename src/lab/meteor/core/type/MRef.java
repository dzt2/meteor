package lab.meteor.core.type;

import lab.meteor.core.MAttribute;
import lab.meteor.core.MElementPointer;
import lab.meteor.core.MException;
import lab.meteor.core.MObject;
import lab.meteor.core.MElement.MElementType;

public class MRef {

	final MElementPointer target;
	
	final MElementPointer attribute;
	
	public MRef(long obj_id, long atb_id) {
		this.target = new MElementPointer(obj_id, MElementType.Object);
		this.attribute = new MElementPointer(atb_id, MElementType.Attribute);
	}
	
	public MRef(MObject obj, MAttribute atb) {
		this.target = new MElementPointer(obj);
		this.attribute = new MElementPointer(atb);
	}
	
	public MObject getTarget() {
		return (MObject) this.target.getElement();
	}
	
	public MAttribute getAttribute() {
		return (MAttribute) this.target.getElement();
	}
	
	public Object getRefValue() {
		MObject obj = this.getTarget();
		if (obj == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		MAttribute atb = this.getAttribute();
		if (atb == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		return obj.getAttribute(atb.getName());
	}
}
