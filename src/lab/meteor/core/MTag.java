package lab.meteor.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class MTag extends MElement implements MNotifiable {
	
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
		if (!MUtility.isValidValue(value))
			throw new MException(MException.Reason.INVALID_VALUE_TYPE);
		
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
		if (!MUtility.isValidValue(value))
			throw new MException(MException.Reason.INVALID_VALUE_TYPE);
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
	
	/**
	 * Create a "lazy" tag element with id.
	 * @param id ID of element.
	 */
	protected MTag(long id) {
		super(id, MElementType.Tag);
	}
	
	public void delete() {
		for (MElementPointer pt : this.targets) {
			MElement ele = pt.getElement();
			ele.removeTag(this);
		}
		this.targets.clear();
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
		if (!MUtility.isValidValue(value))
			throw new MException(MException.Reason.INVALID_VALUE_TYPE);
		if (value instanceof MElement) {
			this.value = new MElementPointer((MElement) value);
		} else if (value instanceof MCollection.Factory) {
			this.value = MCollection.createCollection((MCollection.Factory) value, this);
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
		fromDBObject(this, tagDBInfo.value, null);
		for (Long target_id : tagDBInfo.targets_id) {
			MElementType eType = MDatabase.getDB().getElementType(target_id);
			this.targets.add(new MElementPointer(target_id, eType));
		}
	}
	
	void saveToDBInfo(Object dbInfo) {
		MDBAdapter.TagDBInfo tagDBInfo = (MDBAdapter.TagDBInfo) dbInfo;
		tagDBInfo.id = this.id;
		tagDBInfo.name = this.name;
		tagDBInfo.value = toDBObject(this.value);
		for (MElementPointer target_pt : this.targets) {
			tagDBInfo.targets_id.add(target_pt.getID());
		}
	}
	
	private static void fromDBObject(MNotifiable parent, Object value, Object key) {
		if (value instanceof MDBAdapter.DataList) {
			MList list = new MList(parent);
			MDBAdapter.DataList dl = (MDBAdapter.DataList) value;
			for (Object o : dl) {
				fromDBObject(list, o, null);
			}
			value = list;
		} else if (value instanceof MDBAdapter.DataSet) {
			MSet set = new MSet(parent);
			MDBAdapter.DataSet ds = (MDBAdapter.DataSet) value;
			for (Object o : ds) {
				fromDBObject(set, o, null);
			}
			value = set;
		} else if (value instanceof MDBAdapter.DataDict) {
			MDictionary dict = new MDictionary(parent);
			MDBAdapter.DataDict dd = (MDBAdapter.DataDict) value;
			Iterator<Map.Entry<String, Object>> it = dd.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Object> entry = it.next();
				String k = entry.getKey();
				Object o = entry.getValue();
				fromDBObject(dict, o, k);
			}
			value = dict;
		}
		
		if (parent instanceof MTag) {
			((MTag) parent).value = value;
		} else if (parent instanceof MList) {
			((MList) parent).list.add(value);
		} else if (parent instanceof MSet) {
			((MSet) parent).set.add(value);
		} else if (parent instanceof MDictionary) {
			((MDictionary) parent).dict.put((String)key, value);
		}
	}
	
	private static Object toDBObject(Object value) {
		if (value instanceof MList) {
			MDBAdapter.DataList dl = new MDBAdapter.DataList();
			Iterator<Object> it = ((MList) value).list.iterator();
			while (it.hasNext()) {
				dl.add(toDBObject(it.next()));
			}
			return dl;
		} else if (value instanceof MSet) {
			MDBAdapter.DataSet ds = new MDBAdapter.DataSet();
			Iterator<Object> it = ((MSet) value).set.iterator();
			while (it.hasNext()) {
				ds.add(toDBObject(it.next()));
			}
			return ds;
		} else if (value instanceof MDictionary) {
			MDBAdapter.DataDict dd = new MDBAdapter.DataDict();
			Iterator<Map.Entry<String, Object>> it = ((MDictionary) value).dict.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Object> entry = it.next();
				dd.put(entry.getKey(), toDBObject(entry.getValue()));
			}
			return dd;
		} else if (value instanceof MElement) {
			return new MElementPointer((MElement) value);
		} else {
			return value;
		}
	}

	@Override
	public void notifyChanged() {
		this.setChanged();
	}

}
