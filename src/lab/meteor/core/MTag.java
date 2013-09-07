package lab.meteor.core;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class MTag extends MElement {
	
	private String name;
	
	private Object value;
	
	private Set<MElementPointer> targets;
	
	public MTag(MElement target, String name) {
		this(target, name, null);
	}
	
	public MTag(MElement target, String name, Object value) {
		super(MElementType.Tag);
		
		if (target == null || target.getID() == MElement.NULL_ID)
			throw new MException(MException.Reason.NULL_ELEMENT);
		if (target.isDeleted())
			throw new MException(MException.Reason.ELEMENT_MISSED);
//		if (!MUtil.checkType(type, value))
//			throw new MException(MException.Reason.INVALID_VALUE_TYPE);
		
		this.initialize();
		this.name = name;
		this.targets = new TreeSet<MElementPointer>();
		if (value instanceof MElement) {
			this.value = new MElementPointer((MElement) value);
		} else {
			this.value = value;
		}

		MDatabase.getDB().createElement(this);
		this.addTarget(target);
	}
	
	public MTag(Collection<MElement> targets, String name) {
		this(targets, name, null);
	}
	
	public MTag(Collection<MElement> targets, String name, Object value) {
		super(MElementType.Tag);
		
		if (targets == null)
			throw new MException(MException.Reason.NULL_ELEMENT);
//		if (!MUtil.checkType(type, value))
//			throw new MException(MException.Reason.INVALID_VALUE_TYPE);
		for (MElement target : targets) {
			if (target == null || target.getID() == MElement.NULL_ID)
				throw new MException(MException.Reason.NULL_ELEMENT);
			if (target.isDeleted())
				throw new MException(MException.Reason.ELEMENT_MISSED);
		}
		
		this.initialize();
		this.name = name;
		this.targets = new TreeSet<MElementPointer>();
		if (value instanceof MElement) {
			this.value = new MElementPointer((MElement) value);
		} else {
			this.value = value;
		}
		
		MDatabase.getDB().createElement(this);
		
		for (MElement target : targets) {
			this.addTarget(target);
		}
	}
	
	protected MTag(long id) {
		super(id, MElementType.Tag);
	}
	
	public void delete() {
		// TODO
		super.delete();
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		if (this.name.equals(name))
			return;
		for (MElementPointer pt : this.targets) {
			MElement e = pt.getElement();
			if (e != null) {
				e.removeTag(this.name, this.id);
				this.name = name;
				e.addTag(this.name, this.id);
			}
		}
		this.setChanged();
	}
	
	public Object getValue() {
		if (this.value instanceof MElementPointer)
			return ((MElementPointer) value).getElement();
		else
			return this.value;
	}
	
	public void setValue(Object value) {
//		if (type == this.type && value.equals(this.value))
//			return;
//		if (!MUtil.checkType(type, value))
//			throw new MException(MException.Reason.INVALID_VALUE_TYPE);
		if (value instanceof MElement) {
			this.value = new MElementPointer((MElement) value);
		} else {
			this.value = value;
		}
		this.setChanged();
	}
	
	void addTarget(long id, MElementType eType) {
		this.targets.add(new MElementPointer(id, eType));
	}
	
	void removeTarget(long id, MElementType eType) {
		this.targets.remove(new MElementPointer(id, eType));
		if (this.targets.size() == 0)
			this.delete();
	}
	
	public void addTarget(MElement target) {
		addTarget(target.getID(), target.getElementType());
		target.addTag(this.name, this.id);
	}
	
	public void removeTarget(MElement target) {
		removeTarget(target.getID(), target.getElementType());
		target.removeTag(this.name, this.id);
	}
	
	void loadFromDBInfo(Object dbInfo) {
		MDBAdapter.TagDBInfo tagDBInfo = (MDBAdapter.TagDBInfo) dbInfo;
		this.name = tagDBInfo.name;
		this.value = tagDBInfo.value;
		for (Long target_id : tagDBInfo.targets_id) {
			MElementType eType = MDatabase.getDB().getElementType(target_id);
			this.targets.add(new MElementPointer(target_id, eType));
		}
	}
	
	void saveToDBInfo(Object dbInfo) {
		MDBAdapter.TagDBInfo tagDBInfo = (MDBAdapter.TagDBInfo) dbInfo;
		tagDBInfo.id = this.id;
		tagDBInfo.name = this.name;
		tagDBInfo.value = this.value;
		for (MElementPointer target_pt : this.targets) {
			tagDBInfo.targets_id.add(target_pt.getID());
		}
	}

}
