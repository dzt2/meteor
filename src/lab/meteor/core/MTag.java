package lab.meteor.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import lab.meteor.core.MDBAdapter.DBInfo;

public class MTag extends MElement implements MNotifiable {
	
	String name;
	
	private Object value;
	
	private Set<MElementPointer> elements;
	
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
		this.elements = new TreeSet<MElementPointer>();
		if (value instanceof MElement) {
			this.value = new MElementPointer((MElement) value);
		} else {
			this.value = value;
		}

		MDatabase.getDB().createElement(this);
		MDatabase.getDB().saveTagElements(this);
		target.addTag(this);
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
		this.elements = new TreeSet<MElementPointer>();
		if (value instanceof MElement) {
			this.value = new MElementPointer((MElement) value);
		} else {
			this.value = value;
		}
		
		MDatabase.getDB().createElement(this);
		MDatabase.getDB().saveTagElements(this);
		for (MElement target : targets) {
			target.addTag(this);
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
		for (MElementPointer pt : this.elements) {
			MElement ele = pt.getElement();
			ele.removeTag(this);
		}
		this.elements.clear();
		super.delete();
	}
	
	public String getName() {
		if (!isLoaded())
			load(ATTRIB_FLAG_NAME);
		return this.name;
	}
	
	public void setName(String name) {
		if (this.name.equals(name))
			return;
		relink(this.name, name);
		this.name = name;
		this.setChanged(ATTRIB_FLAG_NAME);
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
		setChanged(ATTRIB_FLAG_VALUE);
	}
	
	void addElement(MElement e) {
		MElementPointer ep = new MElementPointer(e);
		if (elements.contains(ep))
			return;
		elements.add(ep);
		changed_elements = true;
	}
	
	void removeElement(MElement e) {
		MElementPointer ep = new MElementPointer(e);
		if (!elements.contains(ep))
			return;
		elements.remove(ep);
		changed_elements = true;
		
		if (elements.size() == 0)
			delete();
	}
	
	void relink(String oldName, String newName) {
		for (MElementPointer pt : this.elements) {
			MElement e = MDatabase.getDB().getElement(pt.getID());
			if (e != null) {
				e.removeTag(oldName, this.id);
				e.addTag(newName, this.id);
			}
		}
	}
	
	@Override
	void loadFromDBInfo(DBInfo dbInfo) {
		MDBAdapter.TagDBInfo tagDBInfo = (MDBAdapter.TagDBInfo) dbInfo;
		if (dbInfo.isFlagged(ATTRIB_FLAG_NAME))
			if (!this.name.equals(tagDBInfo.name)) {
				this.name = tagDBInfo.name;
				relink(this.name, tagDBInfo.name);
			}
		if (dbInfo.isFlagged(ATTRIB_FLAG_VALUE))
			fromDBObject(this, tagDBInfo.value, null);
	}
	
	@Override
	void saveToDBInfo(DBInfo dbInfo) {
		MDBAdapter.TagDBInfo tagDBInfo = (MDBAdapter.TagDBInfo) dbInfo;
		tagDBInfo.id = this.id;
		if (dbInfo.isFlagged(ATTRIB_FLAG_NAME))
			tagDBInfo.name = this.name;
		if (dbInfo.isFlagged(ATTRIB_FLAG_VALUE))
			tagDBInfo.value = toDBObject(this.value);
	}
	
	@Override
	public void notifyChanged() {
		setChanged(ATTRIB_FLAG_VALUE);
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

	void preloadName() {
		if (this.name == null)
			MDatabase.getDB().preloadTagName(this);
	}
	
	private boolean loaded_elements = false;
	
	private boolean changed_elements = false;
	
	/**
	 * Forcibly load the target elements of tag from database.
	 */
	public void forceLoadElements() {
		if (isDeleted())
			return;
		if (this.id == NULL_ID)
			return;
		MDatabase.getDB().loadTagElements(this);
		loaded_elements = true;
		changed_elements = false;
	}
	
	/**
	 * Load the target elements of tag from database. If the target elements have been
	 * loaded once, there is no effect.
	 */
	public void loadElements() {
		if (!loaded_elements)
			forceLoadElements();
	}
	
	/**
	 * Forcibly save the target elements of tag to database.
	 */
	public void forceSaveElements() {
		if (isDeleted())
			return;
		if (this.id == NULL_ID)
			return;
		if (!loaded_elements)
			throw new MException(MException.Reason.FORBIDEN_SAVE_BEFORE_LOAD);
		MDatabase.getDB().saveTagElements(this);
		changed_elements = false;
	}
	
	/**
	 * Save the target elements of tag to database. If the target elements have not
	 * been changed, there is no effect.
	 */
	public void saveElements() {
		if (changed_elements)
			forceSaveElements();
	}
	
	void loadElementsFromDBInfo(MDBAdapter.IDList idList) {
		for (Long target_id : idList) {
			MElementType eType = MDatabase.getDB().getElementType(target_id);
			this.elements.add(new MElementPointer(target_id, eType));
		}
	}
	
	void saveElementsToDBInfo(MDBAdapter.IDList idList) {
		for (MElementPointer target_pt : this.elements) {
			idList.add(target_pt.getID());
		}
	}
	
	public static final int ATTRIB_FLAG_NAME = 0x00000001;
	public static final int ATTRIB_FLAG_VALUE = 0x00000002;

}
