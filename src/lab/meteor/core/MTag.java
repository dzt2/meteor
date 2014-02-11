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
	
	// Target is the element for storing tags and name-value is the map to search tags.
	public MTag(MElement target, String name, Object value) {
		super(MElementType.Tag);
		
		if (target == null || target.getID() == MElement.NULL_ID)
			throw new MException(MException.Reason.NULL_ELEMENT);
		if (target.isDeleted())
			throw new MException(MException.Reason.ELEMENT_MISSED);
		if (!MUtility.isValidValue(value))
			throw new MException(MException.Reason.INVALID_VALUE_TYPE);
		
		loaded_elements = true;
		changed_elements = false;
		
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
		// this must be called after create operation.
		target.addTag(this);
		
		//this.elements.add(new MElementPointer(target));
	}
	
	public MTag(Collection<MElement> targets, String name) {
		this(targets, name, null);
	}
	
	// Add all target in targets with name-value tags
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
		
		loaded_elements = true;
		changed_elements = false;
		
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
		
		// this must be called after create operation. !!!
		for (MElement target : targets) {
			target.addTag(this);
			//this.elements.add(new MElementPointer(target));
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
	
	@Override
	protected void finalize() throws Throwable {
		saveElements();
		super.finalize();
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		load();
		if (this.name.equals(name))
			return;
		relink(this.name, name);
		this.name = name;
		this.setChanged(ATTRIB_FLAG_NAME);
	}
	
	public Object get() {
		load();
		if (this.value instanceof MElementPointer)
			return ((MElementPointer) value).getElement();
		else
			return this.value;
	}
	
	public void set(Object value) {
		load();
		if (!MUtility.isValidValue(value))
			throw new MException(MException.Reason.INVALID_VALUE_TYPE);
		if (value instanceof MElement) {
			this.value = new MElementPointer((MElement) value);
		} else if (value instanceof MCollection.Factory) {
			//?????
			this.value = MCollection.createCollection((MCollection.Factory) value, this, null);
		} else {
			this.value = value;
		}
		setChanged(ATTRIB_FLAG_VALUE);
	}
	
	void addElement(MElement e) {
		if (isDeleted() || e == null || e.isDeleted())
			return;
		loadElements();
		MElementPointer ep = new MElementPointer(e);
		if (elements.contains(ep))
			return;
		elements.add(ep);
		elementsChanged();
	}
	
	void removeElement(MElement e) {
		if (isDeleted() || e == null || e.isDeleted())
			return;
		loadElements();
		MElementPointer ep = new MElementPointer(e);
		if (!elements.contains(ep))
			return;
		elements.remove(ep);
		
		if (elements.size() == 0) {
			//!!! At the very first time, elements.size() == 0!!!
			delete();
			return;
		}
		elementsChanged();
	}
	
	// changed the name in elements whose tags has "this" tag!
	void relink(String oldName, String newName) {
		for (MElementPointer pt : this.elements) {
			MElement e = MDatabase.getDB().getElementInCache(pt.getID());
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
			tagDBInfo.value = MCollection.toDBObject(this.value);
	}
	
	private static void fromDBObject(MTag tag, Object value, Object key) {
		if (value instanceof MDBAdapter.DataList) {
			MList list = new MList(tag, null);
			MDBAdapter.DataList dl = (MDBAdapter.DataList) value;
			for (Object o : dl) {
				MCollection.fromDBObject(list, o, null);
			}
			value = list;
		} else if (value instanceof MDBAdapter.DataSet) {
			MSet set = new MSet(tag, null);
			MDBAdapter.DataSet ds = (MDBAdapter.DataSet) value;
			for (Object o : ds) {
				MCollection.fromDBObject(set, o, null);
			}
			value = set;
		} else if (value instanceof MDBAdapter.DataDict) {
			MDictionary dict = new MDictionary(tag, null);
			MDBAdapter.DataDict dd = (MDBAdapter.DataDict) value;
			Iterator<Map.Entry<String, Object>> it = dd.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Object> entry = it.next();
				String k = entry.getKey();
				Object o = entry.getValue();
				MCollection.fromDBObject(dict, o, k);
			}
			value = dict;
		}
		tag.value = value;
	}

	void preloadName() {
		if (this.name == null)
			MDatabase.getDB().preloadTagName(this);
	}
	
	private boolean loaded_elements = false;
	
	private boolean changed_elements = false;
	
	private void elementsChanged() {
		changed_elements = true;
		if (MDatabase.getDB().isAutoSave()) {
			MDatabase.getDB().autoSaveTags(this);
		}
	}
	
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

	@Override
	public void setChanged(MElementPointer property) {
		setChanged(ATTRIB_FLAG_VALUE);
	}
	
	@Override
	public String toString() {
		return "Tag(" + this.id + ") : " + this.name;
	}
	
	@Override
	public String details() {
		StringBuilder sb = new StringBuilder();
		sb.append("Tag(").append(id).append(")\n");
		sb.append(name).append("\n  ").append(get());
		return sb.toString();
	}

}
