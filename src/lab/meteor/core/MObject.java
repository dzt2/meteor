package lab.meteor.core;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import lab.meteor.core.MCollection.Factory;
import lab.meteor.core.MDBAdapter.DBInfo;
import lab.meteor.core.MReference.Multiplicity;

/**
 * The object.
 * @author Qiang
 *
 */
public class MObject extends MElement implements MNotifiable {

	private Map<Long, Object> values = null;
	
	MElementPointer class_pt = new MElementPointer();
	
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
	
	/**
	 * Create a "lazy" object element with id.
	 * @param id ID of element.
	 */
	public MObject(long id) {
		super(id, MElementType.Object);
	}
	
	public MClass getClazz() {
		if (!this.isLoaded())
			this.forceLoad();
		
		MClass cls = (MClass) class_pt.getElement();
		if (cls == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		return cls;
	}
	
	public boolean isInstanceOf(MClass clazz) throws MException {
		load();
		
		if (this.class_pt.getID() == clazz.getID())
			return true;
		else {
			MClass cls = (MClass) class_pt.getElement();
			if (cls == null)
				throw new MException(MException.Reason.ELEMENT_MISSED);
			return cls.asSubClass(clazz);
		}
	}
	
	public Object getProperty(String name) {
		load();
		
		MClass cls = (MClass) class_pt.getElement();
		if (cls == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		MProperty field = cls.getProperty(name);
		if (field == null)
			return null;
		switch (field.getElementType()) {
		case Attribute:
			return this.getAttribute((MAttribute) field);
		case Reference:
			return this.getReference((MReference) field);
		default:
			return null;
		}
	}

	private Map<Long, Object> getValues() {
		if (this.values == null)
			this.values = new TreeMap<Long, Object>();
		return this.values;
	}
	
	private Object getAttribute(MAttribute atb) {
		boolean changeFlag = false;
		Object o = this.getValues().get(atb.id);
		if (o != null) {
			if (!MUtility.checkType(atb.getDataType(), o)) {
				this.getValues().remove(atb.id);
				changeFlag = true;
			}
		}
		
		MNativeDataType nType = atb.getDataType().getNativeDataType();
		switch (nType) {
		case List:
			if (o == null) {
				o = MCollection.createCollection(Factory.List, this);
				this.getValues().put(atb.id, o);
			}
			break;
		case Set:
			if (o == null) {
				o = MCollection.createCollection(Factory.Set, this);
				this.getValues().put(atb.id, o);
			}
			break;
		case Dictionary:
			if (o == null) {
				o = MCollection.createCollection(Factory.Dictionary, this);
				this.getValues().put(atb.id, o);
			}
			break;
		case Enum:
			if (o != null) {
				o = ((MElementPointer) o).getElement();
			}
			break;
		default:
			break;
		}
		if (changeFlag)
			this.setChanged(ATTRIB_FLAG_VALUES);
		return o;
	}
	
	private void setAttribute(MAttribute atb, Object obj) {
		Object o;
		if (obj instanceof MElement) {
			o = new MElementPointer((MElement) obj);
		} else if (obj instanceof MCollection.Factory) {
			o =  MCollection.createCollection((MCollection.Factory) obj, this);
		} else {
			o = obj;
		}
		this.getValues().put(atb.id, o);
	}
	
	public Object getAttribute(String name) {
		load();
		
		MClass cls = (MClass) class_pt.getElement();
		if (cls == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		MAttribute atb = cls.getAttribute(name);
		if (atb == null)
			throw new MException(MException.Reason.ATTRIBUTE_NOT_FOUND);
		
		return this.getAttribute(atb);
	}
	
	public void setAttribute(String name, Object obj) {
		load();
		
		MClass cls = (MClass) class_pt.getElement();
		if (cls == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		MAttribute atb = cls.getAttribute(name);
		if (atb == null)
			throw new MException(MException.Reason.ATTRIBUTE_NOT_FOUND);
		if (!MUtility.checkType(atb.getDataType(), obj))
			throw new MException(MException.Reason.INVALID_VALUE_TYPE);
		
		this.setAttribute(atb, obj);
		this.notifyChanged();
	}
	
	private Object getReference(MReference ref) {
		Object value = this.getValues().get(ref.id);
		if (ref.getMultiplicity() == Multiplicity.Multiple) {
			if (value == null || !(value instanceof MPointerSet)) {
				value = new MPointerSet();
				this.getValues().put(ref.id, value);
				this.setChanged(ATTRIB_FLAG_VALUES);
			}
		}
		return value;
	}

	private void addReference(MReference ref, MObject obj) {
		if (ref.getMultiplicity() == Multiplicity.One)
			return;
		MPointerSet set = (MPointerSet) this.getReference(ref);
		set.add(new MElementPointer(obj));
	}
	
	private void removeReference(MReference ref, MObject obj) {
		if (ref.getMultiplicity() == Multiplicity.One)
			return;
		MPointerSet set = (MPointerSet) this.getReference(ref);
		set.remove(new MElementPointer(obj));
	}
	
	private void setReference(MReference ref, MObject obj) {
		if (ref.getMultiplicity() == Multiplicity.Multiple)
			return;
		if (obj == null)
			this.getValues().remove(ref.id);
		else
			this.getValues().put(ref.id, obj);
	}
	
	public void addReference(String name, MObject obj) {
		if (obj == null)
			return;
		load();
		MClass cls = (MClass) class_pt.getElement();
		if (cls == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		MReference ref = cls.getReference(name);
		if (ref == null)
			throw new MException(MException.Reason.REFERENCE_NOT_FOUND);
		if (ref.getMultiplicity() == Multiplicity.One)
			return;
		if (!obj.isInstanceOf(ref.getOwner()))
			throw new MException(MException.Reason.INVALID_VALUE_CLASS);
		
		MReference ref_a = ref;
		MReference ref_b = ref.getOpposite();
		
		// have to handle the opposite references
		if (ref_b != null) {
			// B One
			if (ref_b.getMultiplicity() == Multiplicity.One) {
				Object o = obj.getReference(ref_b);
				if (o != null && o instanceof MElementPointer) {
					MObject old_a = (MObject) ((MElementPointer) o).getElement();
					// ref_a multiplicity is Multiple
					old_a.removeReference(ref_a, obj);
				}
				obj.setReference(ref_b, this);
			} else { // B Multiple
				obj.addReference(ref_b, this);
			}
		}
		this.addReference(ref_a, obj);
		this.setChanged(ATTRIB_FLAG_VALUES);
	}
	
	public void removeReference(String name, MObject obj) {
		if (obj == null)
			return;
		load();
		MClass cls = (MClass) class_pt.getElement();
		if (cls == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		MReference ref = cls.getReference(name);
		if (ref == null)
			throw new MException(MException.Reason.REFERENCE_NOT_FOUND);
		if (ref.getMultiplicity() == Multiplicity.One)
			return;
		if (!obj.isInstanceOf(ref.getOwner()))
			throw new MException(MException.Reason.INVALID_VALUE_CLASS);
		
		MReference ref_a = ref;
		MReference ref_b = ref.getOpposite();
		
		// have to handle the opposite references
		if (ref_b != null) {
			// B One
			if (ref_b.getMultiplicity() == Multiplicity.One) {
				obj.setReference(ref_b, null);
			} else { // B Multiple
				obj.removeReference(ref_b, this);
			}
		}
		this.removeReference(ref_a, obj);
		this.setChanged(ATTRIB_FLAG_VALUES);
	}
	
	public void setReference(String name, MObject obj) {
		load();
		MClass cls = (MClass) class_pt.getElement();
		if (cls == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		MReference ref = cls.getReference(name);
		if (ref == null)
			throw new MException(MException.Reason.REFERENCE_NOT_FOUND);
		if (ref.getMultiplicity() == Multiplicity.Multiple)
			return;
		if (!obj.isInstanceOf(ref.getOwner()))
			throw new MException(MException.Reason.INVALID_VALUE_CLASS);
		
		MReference ref_a = ref;
		MReference ref_b = ref.getOpposite();
		
		// have to handle the opposite references
		if (ref_b != null) {
			// this old reference
			Object o = this.getReference(ref_a);
			if (o != null && o instanceof MElementPointer) {
				MObject old_b = (MObject) ((MElementPointer) o).getElement();
				if (ref_b.getMultiplicity() == Multiplicity.One) {
					old_b.setReference(ref_b, null);
				} else {
					old_b.removeReference(ref_b, this);
				}
			}
			// obj old references
			if (obj != null) {
				// B One
				if (ref_b.getMultiplicity() == Multiplicity.One) {
					o = obj.getReference(ref_b);
					if (o != null && o instanceof MElementPointer) {
						MObject old_a = (MObject) ((MElementPointer) o).getElement();
						// ref_a multiplicity is One
						old_a.setReference(ref_a, null);
					}
					obj.setReference(ref_b, this);
				} else { // B Multiple
					obj.addReference(ref_b, this);
				}
			}
		}
		this.setReference(ref_a, obj);
		this.setChanged(ATTRIB_FLAG_VALUES);
	}
	
	public MObject getReference(String name) {
		load();
		MClass cls = (MClass) class_pt.getElement();
		if (cls == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		MReference ref = cls.getReference(name);
		if (ref == null)
			throw new MException(MException.Reason.REFERENCE_NOT_FOUND);
		if (ref.getMultiplicity() == Multiplicity.Multiple)
			return null;
		
		Object o = this.getReference(ref);
		if (o == null)
			return null;
		if (o instanceof MElementPointer) {
			MObject mo = (MObject) ((MElementPointer) o).getElement();
			if (mo == null) {
				this.setReference(ref, null);
				this.setChanged(ATTRIB_FLAG_VALUES);
			}
			return mo;
		} else {
			this.setReference(ref, null);
			this.setChanged(ATTRIB_FLAG_VALUES);
			return null;
		}
	}
	
	public Set<MObject> getReferences(String name) {
		load();
		MClass cls = (MClass) class_pt.getElement();
		if (cls == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		MReference ref = cls.getReference(name);
		if (ref == null)
			throw new MException(MException.Reason.REFERENCE_NOT_FOUND);
		if (ref.getMultiplicity() == Multiplicity.One)
			return null;
		
		Object o = this.getReference(ref);
		MPointerSet set = (MPointerSet) o;
		Set<MObject> oset = new HashSet<MObject>();
		boolean changeFlag = false;
		Iterator<MElementPointer> it = set.iterator();
		while (it.hasNext()) {
			MElementPointer pt = it.next();
			MObject mo = (MObject) pt.getElement();
			// remove overdue pointer
			if (mo == null) {
				it.remove();
				changeFlag = true;
			} else {
				oset.add(mo);
			}
		}
		if (changeFlag)
			this.setChanged(ATTRIB_FLAG_VALUES);
		return oset;
	}

	@Override
	void loadFromDBInfo(DBInfo dbInfo) {
		boolean changeFlag = false;
		
		MDBAdapter.ObjectDBInfo objDBInfo = (MDBAdapter.ObjectDBInfo) dbInfo;
		this.class_pt = new MElementPointer(objDBInfo.class_id, MElementType.Class);
		
		Iterator<Map.Entry<String, Object>> it = objDBInfo.values.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> entry = it.next();
			long id = MUtility.parseID(entry.getKey());
			MElement ele = MDatabase.getDB().getElement(id);
			Object value = entry.getValue();
			
			// if attribute
			if (ele.getElementType() == MElementType.Attribute) {
				MAttribute atb = (MAttribute) ele;
				if (!MUtility.checkType(atb.getDataType(), value)) {
					changeFlag = true;
				}
				fromDBObject(this, value, id);
			// if reference
			} else if (ele.getElementType() == MElementType.Reference) {
				MReference ref = (MReference) ele;
				// multiplicity one
				if (value instanceof MElementPointer) {
					if (ref.getMultiplicity() == Multiplicity.One) {
						this.getValues().put(id, value);
					} else {
						changeFlag = true;
					}
				// multiplicity multiple
				} else if (value instanceof MDBAdapter.DataSet) {
					if (ref.getMultiplicity() == Multiplicity.Multiple) {
						MPointerSet ps = new MPointerSet();
						MDBAdapter.DataSet ds = (MDBAdapter.DataSet) value;
						for (Object o : ds) {
							ps.add((MElementPointer) o);
						}
						this.getValues().put(id, ps);
					} else {
						changeFlag = true;
					}
				}
			} else {
				changeFlag = true;
			}
		}
		
		if (changeFlag)
			this.setChanged(ATTRIB_FLAG_VALUES);
	}
	
	@Override
	void saveToDBInfo(DBInfo dbInfo) {
		MDBAdapter.ObjectDBInfo objDBInfo = (MDBAdapter.ObjectDBInfo) dbInfo;
		objDBInfo.id = this.id;
		if (dbInfo.isFlagged(ATTRIB_FLAG_CLASS))
			objDBInfo.class_id = this.class_pt.getID();
		if (dbInfo.isFlagged(ATTRIB_FLAG_VALUES)) {
			if (this.values != null) {
				Iterator<Map.Entry<Long, Object>> it = this.values.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<Long, Object> entry = it.next();
					long id = entry.getKey();
					Object value = entry.getValue();
					
					if (value instanceof MPointerSet) {
						MDBAdapter.DataSet ds = new MDBAdapter.DataSet();
						for (MElementPointer pt : (MPointerSet) value) {
							ds.add(pt);
						}
						objDBInfo.values.put(MUtility.stringID(id), ds);
					} else {
						Object o = toDBObject(value);
						objDBInfo.values.put(MUtility.stringID(id), o);
					}
				}
			}
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
		if (parent instanceof MObject) {
			((MObject) parent).getValues().put((Long)key, value);
		} else if (parent instanceof MList) {
			((MList) parent).list.add(value);
		} else if (parent instanceof MSet) {
			((MSet) parent).set.add(value);
		} else if (parent instanceof MDictionary) {
			((MDictionary) parent).dict.put((String)key, value);
		}
	}
	
	long getClazzID() {
		return this.class_pt.getID();
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

	@SuppressWarnings("serial")
	static class MPointerSet extends HashSet<MElementPointer> {

	}
	
	public String print() {
		StringBuilder sb = new StringBuilder();
		sb.append("Object(").append(this.id).append(")\n");
		for (Long id : this.getValues().keySet()) {
			MElement e = MDatabase.getDB().getElement(id);
			MProperty p = (MProperty) e;
			sb.append("  ").append(p.name).append(" : ");
			if (e.getElementType() == MElementType.Attribute) {
				sb.append(this.values.get(id).toString()).append("\n");
			} else {
				MReference r = (MReference) e;
				if (r.getMultiplicity() == Multiplicity.Multiple) {
					MPointerSet set = (MPointerSet)this.values.get(r.name);
					sb.append("  \n{\n");
					for (MElementPointer pt : set) {
						sb.append("    ").append(pt.getElement().toString()).append("\n");
					}
					sb.append("  }\n");
				} else {
					MElementPointer pt = (MElementPointer)this.values.get(r.name);
					sb.append(pt.getElement().toString()).append("\n");
				}
			}
		}
		return sb.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClazz().toString()).append("(").append(this.id).append(")");
		return sb.toString();
	}

	@Override
	public void notifyChanged() {
		this.setChanged(ATTRIB_FLAG_VALUES);
	}
	
	public static final int ATTRIB_FLAG_CLASS = 0x00000001;
	public static final int ATTRIB_FLAG_VALUES = 0x00000002;

}
