package lab.meteor.core;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import lab.meteor.core.MReference.Multiplicity;

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

	private Map<Long, Object> getValues() {
		if (this.values == null)
			this.values = new TreeMap<Long, Object>();
		return this.values;
	}
	
	private Object getAttribute(MAttribute atb) {
		return this.getValues().get(atb.id);
	}
	
	public void setAttribute(MAttribute atb, Object obj) {
		this.getValues().put(atb.id, obj);
	}
	
	public Object getAttribute(String name) {
		if (!isLoaded())
			this.load();
		MClass cls = (MClass) class_pt.getElement();
		if (cls == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		MAttribute atb = cls.getAttribute(name);
		if (atb == null)
			throw new MException(MException.Reason.ATTRIBUTE_NOT_FOUND);
		
		return this.getAttribute(atb);
	}
	
	public void setAttribute(String name, Object obj) {
		if (!isLoaded())
			this.load();
		MClass cls = (MClass) class_pt.getElement();
		if (cls == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		MAttribute atb = cls.getAttribute(name);
		if (atb == null)
			throw new MException(MException.Reason.ATTRIBUTE_NOT_FOUND);
		if (!MUtility.checkType(atb.getType(), obj))
			throw new MException(MException.Reason.INVALID_VALUE_TYPE);
		
		this.setAttribute(atb, obj);
	}
	
	private Object getReference(MReference ref) {
		Object value = this.getValues().get(ref.id);
		if (ref.getMultiplicity() == Multiplicity.Multiple) {
			if (value == null || !(value instanceof MPointerSet)) {
				value = new MPointerSet();
				this.getValues().put(ref.id, value);
				this.setChanged();
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
		if (!isLoaded())
			this.load();
		MClass cls = (MClass) class_pt.getElement();
		if (cls == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		MReference ref = cls.getReference(name);
		if (ref == null)
			throw new MException(MException.Reason.REFERENCE_NOT_FOUND);
		if (ref.getMultiplicity() == Multiplicity.One)
			return;
		if (!obj.isInstanceOf(ref.getClazz()))
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
		this.setChanged();
	}
	
	public void removeReference(String name, MObject obj) {
		if (obj == null)
			return;
		if (!isLoaded())
			this.load();
		MClass cls = (MClass) class_pt.getElement();
		if (cls == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		MReference ref = cls.getReference(name);
		if (ref == null)
			throw new MException(MException.Reason.REFERENCE_NOT_FOUND);
		if (ref.getMultiplicity() == Multiplicity.One)
			return;
		if (!obj.isInstanceOf(ref.getClazz()))
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
		this.setChanged();
	}
	
	public void setReference(String name, MObject obj) {
		if (!isLoaded())
			this.load();
		MClass cls = (MClass) class_pt.getElement();
		if (cls == null)
			throw new MException(MException.Reason.ELEMENT_MISSED);
		MReference ref = cls.getReference(name);
		if (ref == null)
			throw new MException(MException.Reason.REFERENCE_NOT_FOUND);
		if (ref.getMultiplicity() == Multiplicity.Multiple)
			return;
		if (!obj.isInstanceOf(ref.getClazz()))
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
		this.setChanged();
	}
	
	public MObject getReference(String name) {
		if (!isLoaded())
			this.load();
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
				this.setChanged();
			}
			return mo;
		} else {
			this.setReference(ref, null);
			this.setChanged();
			return null;
		}
	}
	
	public Set<MObject> getReferences(String name) {
		if (!isLoaded())
			this.load();
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
		Set<MObject> oset = new TreeSet<MObject>();
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
			this.setChanged();
		return oset;
	}

	@Override
	void loadFromDBInfo(Object dbInfo) {
		MDBAdapter.ObjectDBInfo objDBInfo = new MDBAdapter.ObjectDBInfo();
		
	}

	@Override
	void saveToDBInfo(Object dbInfo) {
		// TODO Auto-generated method stub
		
	}
	
	static class MPointerSet extends TreeSet<MElementPointer> {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4911228345476040087L;
		
	}

}
